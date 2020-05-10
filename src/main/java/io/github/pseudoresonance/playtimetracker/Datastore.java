package io.github.pseudoresonance.playtimetracker;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.FileConfiguration;
import com.google.common.base.Charsets;

public class Datastore {

	private final static String fileName = "playtime.yml";

	private PlaytimeTracker playtimeTracker;
	private ConfigFile dataFile;
	private File dataFolder;

	private ConcurrentHashMap<String, Long> data = new ConcurrentHashMap<String, Long>();
	private volatile long totalTime = 0;

	private ConcurrentHashMap<String, Long> sessionData = new ConcurrentHashMap<String, Long>();
	private volatile long sessionTotalTime = 0;

	private volatile boolean loggedOn = false;
	private String serverName = "";
	private volatile long lastUpdateTime = 0;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	private volatile ScheduledFuture<?> scheduledTask = null;

	@SuppressWarnings("deprecation")
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
		dataFile = new ConfigFile(dataFolder, fileName);
		FileLock lock = null;
		try (FileChannel channel = new RandomAccessFile(dataFile.getConfigFile(), "rw").getChannel();) {
			lock = channel.lock();
			dataFile.reload(new InputStreamReader(Channels.newInputStream(channel), FileConfiguration.UTF8_OVERRIDE && !FileConfiguration.UTF_BIG ? Charsets.UTF_8 : Charset.defaultCharset()));
			readConfig(false, null);
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

	@SuppressWarnings("deprecation")
	public void updateLocation(String location) {
		try {
			File newFolder = new File(location);
			newFolder.mkdirs();
			dataFolder = newFolder;
			dataFile.save();
			dataFile = new ConfigFile(dataFolder, fileName);
			scheduler.schedule(() -> {
				FileLock lock = null;
				try (FileChannel channel = new RandomAccessFile(dataFile.getConfigFile(), "rw").getChannel();) {
					lock = channel.lock();
					dataFile.reload(new InputStreamReader(Channels.newInputStream(channel), FileConfiguration.UTF8_OVERRIDE && !FileConfiguration.UTF_BIG ? Charsets.UTF_8 : Charset.defaultCharset()));
					readConfig(false, null);
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

	private void readConfig(boolean overwrite, InputStreamReader in) {
		long newTotalTime = 0;
		if (overwrite) {
			dataFile.reload(in);
		} else
			newTotalTime = totalTime;
		ConfigurationSection section = dataFile.getConfig().getConfigurationSection("mp");
		if (section != null) {
			for (String key : section.getKeys(false)) {
				long val = section.getLong(key);
				key = "mp." + key;
				if (data.containsKey(key) && !overwrite) {
					long add = data.get(key) + val;
					data.put(key, add);
					newTotalTime += add;
				} else {
					data.put(key, val);
					newTotalTime += val;
				}
			}
		}
		section = dataFile.getConfig().getConfigurationSection("sp");
		if (section != null) {
			for (String key : section.getKeys(false)) {
				long val = section.getLong(key);
				key = "sp." + key;
				if (data.containsKey(key) && !overwrite) {
					long add = data.get(key) + val;
					data.put(key, add);
					newTotalTime += add;
				} else {
					data.put(key, val);
					newTotalTime += val;
				}
			}
		}
		totalTime = newTotalTime;
	}

	@SuppressWarnings("deprecation")
	private void updateData() {
		FileLock lock = null;
		try (RandomAccessFile raf = new RandomAccessFile(dataFile.getConfigFile(), "rw")) {
			lock = raf.getChannel().lock();
			if (ConfigHandler.multiInstance) {
				readConfig(true, new InputStreamReader(Channels.newInputStream(raf.getChannel()), FileConfiguration.UTF8_OVERRIDE && !FileConfiguration.UTF_BIG ? Charsets.UTF_8 : Charset.defaultCharset()));
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
		try (RandomAccessFile raf = new RandomAccessFile(dataFile.getConfigFile(), "rw")) {
			lock = raf.getChannel().lock();
			if (loggedOn) {
				long now = System.currentTimeMillis();
				long change = now - lastUpdateTime;
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
				dataFile.set(serverName, val);
				dataFile.save(Channels.newOutputStream(raf.getChannel()));
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
		lastUpdateTime = System.currentTimeMillis();
		startClock();
	}

	public void logOff() {
		updateData();
		loggedOn = false;
		stopClock();
		lastUpdateTime = 0;
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

	public long clockUpdateMs() {
		return lastUpdateTime % 1000;
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
