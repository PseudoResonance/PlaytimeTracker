package io.github.pseudoresonance.playtimetracker.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.nio.channels.Channels;
import java.nio.channels.FileLock;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Datastore {

	private final IPlaytimeTracker playtimeTracker;

	private final static String fileName = "playtime.json";

	private UUID gameDirectoryUuid;
	private File dataFile;
	private File dataFolder;
	private Gson gson = new Gson();
	private Type mapType = new TypeToken<ConcurrentHashMap<String, DataHolder>>() {
	}.getType();

	private ConcurrentHashMap<String, DataHolder> data = new ConcurrentHashMap<String, DataHolder>();
	private volatile long totalTime = 0;

	private ConcurrentHashMap<String, DataHolder> sessionData = new ConcurrentHashMap<String, DataHolder>();
	private volatile long sessionTotalTime = 0;

	private volatile boolean loggedOn = false;
	private String serverName = "";
	private volatile long logOnTime = 0;
	private volatile long lastUpdateTime = 0;
	private volatile long lastChange = 0;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	private volatile ScheduledFuture<?> scheduledTask = null;

	public Datastore(IPlaytimeTracker playtimeTracker) {
		this.playtimeTracker = playtimeTracker;
		File gameDir = playtimeTracker.getGame().getGameDirectory();
		if (gameDir.isDirectory()) {
			File uuidFile = new File(gameDir, "playtimeTracker.uuid");
			if (uuidFile.exists()) {
				try {
					String fileStr = Files.readFirstLine(uuidFile, Charsets.UTF_8);
					gameDirectoryUuid = UUID.fromString(fileStr);
				} catch (IOException | IllegalArgumentException e) {
					System.err.println("Unable to read UUID file: " + uuidFile.getAbsolutePath());
					e.printStackTrace();
					gameDirectoryUuid = UUID.randomUUID();
					try {
						Files.write(gameDirectoryUuid.toString().getBytes(), uuidFile);
					} catch (IOException e1) {
						System.err.println("Unable to write to UUID file: " + uuidFile.getAbsolutePath());
						e1.printStackTrace();
					}
				}
			} else {
				gameDirectoryUuid = UUID.randomUUID();
				try {
					Files.write(gameDirectoryUuid.toString().getBytes(), uuidFile);
				} catch (IOException e) {
					System.err.println("Unable to write to UUID file: " + uuidFile.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}
		try {
			dataFolder = new File(playtimeTracker.getConfig().database);
			dataFolder.mkdirs();
		} catch (SecurityException e) {
			try {
				playtimeTracker.getConfig().database = "./playtime";
				dataFolder = new File("./playtime");
				dataFolder.mkdirs();
			} catch (SecurityException ex) {
				System.err.println("Unable to save data to " + dataFolder.getAbsolutePath() + "! Please check file permissions!");
			}
		}
		dataFile = new File(dataFolder, fileName);
		FileLock lock = null;
		try (RandomAccessFile raf = new RandomAccessFile(dataFile, "rw")) {
			lock = raf.getChannel().lock();
			readConfig(true, raf);
		} catch (Exception e) {
			System.err.println("Unable to read from " + dataFolder.getAbsolutePath() + "! Please check file permissions!");
			e.printStackTrace();
		} finally {
			try {
				if (lock != null)
					lock.release();
			} catch (IOException e) {
			}
		}
	}

	public void updateLocation(String location) {
		try {
			File newFolder = new File(location);
			newFolder.mkdirs();
			dataFolder = newFolder;
			dataFile = new File(dataFolder, fileName);
			scheduler.schedule(() -> {
				FileLock lock = null;
				try (RandomAccessFile raf = new RandomAccessFile(dataFile, "rw")) {
					lock = raf.getChannel().lock();
					readConfig(false, raf);
				} catch (Exception e) {
					System.err.println("Unable to read from " + dataFolder.getAbsolutePath() + "! Please check file permissions!");
					e.printStackTrace();
				} finally {
					try {
						if (lock != null)
							lock.release();
					} catch (IOException e) {
					}
				}
			}, 0, TimeUnit.NANOSECONDS);
		} catch (SecurityException e) {
			throw new IllegalArgumentException(location);
		}
	}

	private void readConfig(boolean overwrite, RandomAccessFile raf) {
		long newTotalTime = 0;
		if (!overwrite)
			newTotalTime = totalTime;
		try {
			ConcurrentHashMap<String, DataHolder> newData = gson.fromJson(new InputStreamReader(Channels.newInputStream(raf.getChannel()), Charsets.UTF_8), mapType);
			if (newData != null) {
				for (String key : newData.keySet()) {
					DataHolder newHolder = newData.get(key);
					newTotalTime += newData.get(key).time;
					if (!overwrite && data.containsKey(key))
						newData.get(key).time += data.get(key).time;
					data.put(key, newHolder);
				}
			} else {
				System.out.println("Migrating old data file to new format!");
				// Old data updater
				try {
				raf.getChannel().position(0);
				} catch (IOException ex) {
				}
				ConcurrentHashMap<String, Long> oldData = gson.fromJson(new InputStreamReader(Channels.newInputStream(raf.getChannel()), Charsets.UTF_8), new TypeToken<ConcurrentHashMap<String, Long>>() {
				}.getType());
				if (oldData != null) {
					for (String key : oldData.keySet()) {
						long val = oldData.get(key);
						DataHolder newHolder = new DataHolder(key.substring(3), key.substring(0, 2).equals("sp"), "", key.substring(0, 2).equals("sp") ? playtimeTracker.getGame().getGameDirectory().getAbsolutePath() : null, key.substring(0, 2).equals("sp") ? gameDirectoryUuid : null, val);
						newTotalTime += val;
						data.put(key, newHolder);
					}
				}
				try {
					String json = gson.toJson(data, mapType);
					raf.setLength(0);
					OutputStreamWriter osw = new OutputStreamWriter(Channels.newOutputStream(raf.getChannel()), Charsets.UTF_8);
					osw.write(json);
					osw.close();
				} catch (IOException ex) {
					System.err.println("Unable to write to " + dataFolder.getAbsolutePath() + "! Please check file permissions!");
					ex.printStackTrace();
				}
			}
		} catch (JsonSyntaxException | JsonIOException e) {
			System.out.println("Migrating old data file to new format!");
			// Old data updater
			try {
			raf.getChannel().position(0);
			} catch (IOException ex) {
			}
			ConcurrentHashMap<String, Long> oldData = gson.fromJson(new InputStreamReader(Channels.newInputStream(raf.getChannel()), Charsets.UTF_8), new TypeToken<ConcurrentHashMap<String, Long>>() {
			}.getType());
			if (oldData != null) {
				for (String key : oldData.keySet()) {
					long val = oldData.get(key);
					DataHolder newHolder = new DataHolder(key.substring(3), key.substring(0, 2).equals("sp"), "", key.substring(0, 2).equals("sp") ? playtimeTracker.getGame().getGameDirectory().getAbsolutePath() : null, key.substring(0, 2).equals("sp") ? gameDirectoryUuid : null, val);
					newTotalTime += val;
					data.put(key, newHolder);
				}
			}
			try {
				String json = gson.toJson(data, mapType);
				raf.setLength(0);
				OutputStreamWriter osw = new OutputStreamWriter(Channels.newOutputStream(raf.getChannel()), Charsets.UTF_8);
				osw.write(json);
				osw.close();
			} catch (IOException ex) {
				System.err.println("Unable to write to " + dataFolder.getAbsolutePath() + "! Please check file permissions!");
				ex.printStackTrace();
			}
		}
		totalTime = newTotalTime;
	}

	/**
	 * Method to update data to/from the database file
	 */
	public void updateData() {
		updateData(null, null);
	}

	/**
	 * Method to update data to/from the database file
	 * 
	 * If <code>updateKey</code> is provided, but <code>toUpdate</code> is null, the
	 * data at the given key will be deleted. If <code>updateKey</code> is provided
	 * and <code>toUpdate</code> is null, the editable data at the given key will be
	 * edited to match <code>toUpdate</code>.
	 * 
	 * @param updateKey
	 *                      key to update
	 * @param toUpdate
	 *                      data to update with
	 */
	public void updateData(String updateKey, DataHolder toUpdate) {
		FileLock lock = null;
		try (RandomAccessFile raf = new RandomAccessFile(dataFile, "rw")) {
			lock = raf.getChannel().lock();
			if (playtimeTracker.getConfig().multiInstance) {
				readConfig(true, raf);
			}
			if (loggedOn) {
				long now = System.currentTimeMillis();
				long newChange = now - logOnTime;
				long change = newChange - lastChange;
				lastChange = newChange;
				lastUpdateTime = now;
				DataHolder val = null;
				if (data.containsKey(serverName))
					val = data.get(serverName);
				else {
					val = new DataHolder(serverName.substring(0, 2).equals("sp") ? serverName.substring(40) : serverName.substring(3), serverName.substring(0, 2).equals("sp"), "", serverName.substring(0, 2).equals("sp") ? playtimeTracker.getGame().getGameDirectory().getAbsolutePath() : null, serverName.substring(0, 2).equals("sp") ? gameDirectoryUuid : null, 0);
					data.put(serverName, val);
				}
				DataHolder sessionVal = null;
				if (sessionData.containsKey(serverName))
					sessionVal = sessionData.get(serverName);
				else {
					sessionVal = new DataHolder(val.name, val.singleplayer, val.nickname, val.gameDirectory, val.directoryUuid, 0);
					sessionData.put(serverName, sessionVal);
				}
				val.time += change;
				totalTime += change;
				sessionVal.time += change;
				sessionTotalTime += change;
				if (updateKey != null) {
					if (toUpdate != null) {
						if (data.containsKey(updateKey)) {
							DataHolder updateObj = data.get(updateKey);
							updateObj.nickname = toUpdate.nickname;
						}
						if (sessionData.containsKey(updateKey)) {
							DataHolder updateObj = sessionData.get(updateKey);
							updateObj.nickname = toUpdate.nickname;
						}
					} else {
						data.remove(updateKey);
						sessionData.remove(updateKey);
					}
				}
				String json = gson.toJson(data, mapType);
				raf.setLength(0);
				OutputStreamWriter osw = new OutputStreamWriter(Channels.newOutputStream(raf.getChannel()), Charsets.UTF_8);
				osw.write(json);
				osw.close();
				raf.getChannel().close();
				raf.close();
			}
		} catch (Exception e) {
			System.err.println("Unable to read from " + dataFolder.getAbsolutePath() + "! Please check file permissions!");
			e.printStackTrace();
		} finally {
			try {
				if (lock != null)
					lock.release();
			} catch (IOException e) {
			}
		}
	}

	public void logOn(String serverName) {
		loggedOn = true;
		this.serverName = serverName;
		logOnTime = System.currentTimeMillis();
		lastUpdateTime = logOnTime;
		lastChange = 0;
		startClock();
	}

	public void logOff() {
		updateData();
		loggedOn = false;
		stopClock();
		lastUpdateTime = 0;
		logOnTime = 0;
		lastChange = 0;
		serverName = "";
	}

	public void startClock() {
		if (scheduledTask == null || scheduledTask.isCancelled()) {
			scheduledTask = scheduler.scheduleAtFixedRate(() -> {
				updateData();
			}, playtimeTracker.getConfig().saveInterval, playtimeTracker.getConfig().saveInterval, TimeUnit.SECONDS);
		}
	}

	public void stopClock() {
		if (scheduledTask != null && !scheduledTask.isCancelled() && !playtimeTracker.getConfig().multiInstance && !loggedOn)
			scheduledTask.cancel(false);
	}

	public long getCurrentPlaytime() {
		long val = 0;
		if (data.containsKey(serverName))
			val = data.get(serverName).time;
		if (loggedOn)
			val += System.currentTimeMillis() - lastUpdateTime;
		return val;
	}

	public long getPlaytime(String server) {
		if (server.equals(serverName))
			return getCurrentPlaytime();
		long val = 0;
		if (data.containsKey(serverName))
			val = data.get(serverName).time;
		return val;
	}

	public long getTotalPlaytime() {
		long val = totalTime;
		if (loggedOn)
			val += System.currentTimeMillis() - lastUpdateTime;
		return val;
	}

	public Set<String> getPlaytimeKeys() {
		return data.keySet();
	}

	public ConcurrentHashMap<String, DataHolder> getPlaytimeMap() {
		return data;
	}

	public long getSessionCurrentPlaytime() {
		long val = 0;
		if (sessionData.containsKey(serverName))
			val = sessionData.get(serverName).time;
		if (loggedOn)
			val += System.currentTimeMillis() - lastUpdateTime;
		return val;
	}

	public long getSessionPlaytime(String server) {
		if (server.equals(serverName))
			return getCurrentPlaytime();
		long val = 0;
		if (sessionData.containsKey(serverName))
			val = sessionData.get(serverName).time;
		return val;
	}

	public long getSessionTotalPlaytime() {
		long val = sessionTotalTime;
		if (loggedOn)
			val += System.currentTimeMillis() - lastUpdateTime;
		return val;
	}

	public Set<String> getSessionPlaytimeKeys() {
		return sessionData.keySet();
	}

	public ConcurrentHashMap<String, DataHolder> getSessionPlaytimeMap() {
		return sessionData;
	}

	public String getCurrentServerName() {
		return serverName;
	}

	public boolean isLoggedOn() {
		return loggedOn;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public UUID getGameDirectoryUuid() {
		return gameDirectoryUuid;
	}

	public static class DataHolder implements Cloneable, Comparator<DataHolder>, Comparable<DataHolder> {

		public final String name;
		public final boolean singleplayer;
		public String nickname;
		public final String gameDirectory;
		public final UUID directoryUuid;
		public long time;

		public DataHolder(String name, boolean singleplayer, String nickname, String gameDirectory, UUID directoryUuid, long time) {
			this.name = name;
			this.singleplayer = singleplayer;
			this.nickname = nickname;
			this.gameDirectory = gameDirectory;
			this.directoryUuid = directoryUuid;
			this.time = time;
		}

		public DataHolder clone() {
			return new DataHolder(name, singleplayer, nickname, gameDirectory, directoryUuid, time);
		}

		@Override
		public int compareTo(DataHolder var1) {
			return compare(this, var1);
		}

		@Override
		public int compare(DataHolder var1, DataHolder var2) {
			return var1.time < var2.time ? -1 : (var1.time == var2.time ? 0 : 1);
		}
	}

	public static class ListItem implements Cloneable, Comparator<ListItem>, Comparable<ListItem> {

		public final String name;
		public final String nickname;
		public final String value;
		public final boolean singleplayer;
		public final String gameDirectory;
		public final boolean currentDirectory;
		public final boolean currentItem;
		public final long time;
		public final long maxTime;

		public ListItem(String name, String nickname, String value, boolean singleplayer, String gameDirectory, boolean currentDirectory, boolean currentItem, long time, long maxTime) {
			this.name = name;
			this.nickname = nickname;
			this.value = value;
			this.singleplayer = singleplayer;
			this.gameDirectory = gameDirectory;
			this.currentDirectory = currentDirectory;
			this.currentItem = currentItem;
			this.time = time;
			this.maxTime = maxTime;
		}

		public ListItem clone() {
			return new ListItem(name, nickname, value, singleplayer, gameDirectory, currentDirectory, currentItem, time, maxTime);
		}

		@Override
		public int compareTo(ListItem var1) {
			return compare(this, var1);
		}

		@Override
		public int compare(ListItem var1, ListItem var2) {
			return var1.time < var2.time ? -1 : (var1.time == var2.time ? 0 : 1);
		}
	}

}
