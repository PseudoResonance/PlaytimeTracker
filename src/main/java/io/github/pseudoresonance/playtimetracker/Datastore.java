package io.github.pseudoresonance.playtimetracker;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Datastore {

	private final static String fileName = "playtime.json";

	private PlaytimeTracker playtimeTracker;
	private File dataFile;
	private File dataFolder;
	private Gson gson = new Gson();
	private Type mapType = new TypeToken<ConcurrentHashMap<String, Long>>() {
	}.getType();

	private ConcurrentHashMap<String, Long> data = new ConcurrentHashMap<String, Long>();
	private volatile long totalTime = 0;

	private ConcurrentHashMap<String, Long> sessionData = new ConcurrentHashMap<String, Long>();
	private volatile long sessionTotalTime = 0;

	private volatile boolean loggedOn = false;
	private String serverName = "";
	private volatile long logOnTime = 0;
	private volatile long lastUpdateTime = 0;
	private volatile long lastChange = 0;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	private volatile ScheduledFuture<?> scheduledTask = null;

	Datastore(PlaytimeTracker playtimeTracker) {
		this.playtimeTracker = playtimeTracker;
		try {
			dataFolder = new File(ConfigHandler.database);
			dataFolder.mkdirs();
		} catch (SecurityException e) {
			try {
				ConfigHandler.setDatabase("./playtime");
				dataFolder = new File("./playtime");
				dataFolder.mkdirs();
			} catch (SecurityException ex) {
				playtimeTracker.getLogger().error("Unable to save data to " + dataFolder.getAbsolutePath() + "! Please check file permissions!");
			}
		}
		dataFile = new File(dataFolder, fileName);
		FileLock lock = null;
		try (FileChannel channel = new RandomAccessFile(dataFile, "rw").getChannel()) {
			lock = channel.lock();
			readConfig(true, channel);
		} catch (Exception e) {
			playtimeTracker.getLogger().error("Unable to read from " + dataFolder.getAbsolutePath() + "! Please check file permissions!");
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
				try (FileChannel channel = new RandomAccessFile(dataFile, "rw").getChannel()) {
					lock = channel.lock();
					readConfig(false, channel);
				} catch (Exception e) {
					playtimeTracker.getLogger().error("Unable to read from " + dataFolder.getAbsolutePath() + "! Please check file permissions!");
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

	private void readConfig(boolean overwrite, FileChannel channel) {
		long newTotalTime = 0;
		if (!overwrite)
			newTotalTime = totalTime;
		ConcurrentHashMap<String, Long> newData = gson.fromJson(new InputStreamReader(Channels.newInputStream(channel), Charsets.UTF_8), mapType);
		if (newData != null) {
			for (String key : newData.keySet()) {
				long val = newData.get(key);
				newTotalTime += val;
				if (!overwrite && data.containsKey(key))
					val += data.get(key);
				data.put(key, val);
			}
		}
		totalTime = newTotalTime;
	}

	private void updateData() {
		FileLock lock = null;
		try (RandomAccessFile raf = new RandomAccessFile(dataFile, "rw")) {
			lock = raf.getChannel().lock();
			if (ConfigHandler.multiInstance) {
				readConfig(true, raf.getChannel());
			}
			if (loggedOn) {
				long now = System.currentTimeMillis();
				long newChange = now - logOnTime;
				long change = newChange - lastChange;
				lastChange = newChange;
				lastUpdateTime = now;
				long val = 0;
				if (data.containsKey(serverName))
					val = data.get(serverName);
				long sessionVal = 0;
				if (sessionData.containsKey(serverName))
					sessionVal = sessionData.get(serverName);
				val += change;
				totalTime += change;
				sessionVal += change;
				sessionTotalTime += change;
				data.put(serverName, val);
				sessionData.put(serverName, sessionVal);
				String json = gson.toJson(data, mapType);
				raf.setLength(0);
				OutputStreamWriter osw = new OutputStreamWriter(Channels.newOutputStream(raf.getChannel()), Charsets.UTF_8);
				osw.write(json);
				osw.close();
				raf.getChannel().close();
				raf.close();
			}
		} catch (Exception e) {
			playtimeTracker.getLogger().error("Unable to read from " + dataFolder.getAbsolutePath() + "! Please check file permissions!");
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
			}, ConfigHandler.saveInterval, ConfigHandler.saveInterval, TimeUnit.SECONDS);
		}
	}

	public void stopClock() {
		if (scheduledTask != null && !scheduledTask.isCancelled() && !ConfigHandler.multiInstance && !loggedOn)
			scheduledTask.cancel(false);
	}

	public long getCurrentPlaytime() {
		long val = 0;
		if (data.containsKey(serverName))
			val = data.get(serverName);
		if (loggedOn)
			val += System.currentTimeMillis() - lastUpdateTime;
		return val;
	}

	public long getPlaytime(String server) {
		if (server.equals(serverName))
			return getCurrentPlaytime();
		long val = 0;
		if (data.containsKey(serverName))
			val = data.get(serverName);
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

	public ConcurrentHashMap<String, Long> getPlaytimeMap() {
		return data;
	}

	public long getSessionCurrentPlaytime() {
		long val = 0;
		if (sessionData.containsKey(serverName))
			val = sessionData.get(serverName);
		if (loggedOn)
			val += System.currentTimeMillis() - lastUpdateTime;
		return val;
	}

	public long getSessionPlaytime(String server) {
		if (server.equals(serverName))
			return getCurrentPlaytime();
		long val = 0;
		if (sessionData.containsKey(serverName))
			val = sessionData.get(serverName);
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

	public ConcurrentHashMap<String, Long> getSessionPlaytimeMap() {
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

}
