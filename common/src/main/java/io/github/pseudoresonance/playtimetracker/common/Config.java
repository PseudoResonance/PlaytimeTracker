package io.github.pseudoresonance.playtimetracker.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import com.google.common.base.Charsets;
import com.google.gson.Gson;

import io.github.pseudoresonance.playtimetracker.common.IRenderer.AlignMode;

public class Config {
	
	private transient File config;
	private transient Gson gson = new Gson();
	
	public boolean enabled = true;
	public String database = "./playtime";
	public long saveInterval = 120;
	public boolean multiInstance = false;
	public AlignMode alignMode = AlignMode.TOPCENTER;
	public int xOffset = 0;
	public int yOffset = 3;
	public AlignMode alignModeMain = AlignMode.TOPCENTER;
	public int xOffsetMain = 0;
	public int yOffsetMain = 3;
	public int barColor = 0xFFFF0000;
	public int barBackgroundColor = 0xFF555555;
	public int borderColor = 0xFF000000;
	public int currentColor = 0xFFFF0000;
	
	public Config(File configFile) {
		config = configFile;
		boolean first = !config.exists() || config.length() == 0;
		if (first)
			save();
		reload();
	}
	
	public void reload() {
		FileLock lock = null;
		try (FileChannel channel = new RandomAccessFile(config, "rw").getChannel()) {
			lock = channel.lock();
			Config newData = gson.fromJson(new InputStreamReader(Channels.newInputStream(channel), Charsets.UTF_8), Config.class);
			if (newData != null) {
				this.enabled = newData.enabled;
				this.database = newData.database;
				this.saveInterval = newData.saveInterval;
				this.multiInstance = newData.multiInstance;
				this.alignMode = newData.alignMode;
				this.xOffset = newData.xOffset;
				this.yOffset = newData.yOffset;
				this.alignModeMain = newData.alignModeMain;
				this.xOffsetMain = newData.xOffsetMain;
				this.yOffsetMain = newData.yOffsetMain;
				this.barColor = newData.barColor;
				this.barBackgroundColor = newData.barBackgroundColor;
				this.borderColor = newData.borderColor;
				this.currentColor = newData.currentColor;
			}
		} catch (Exception e) {
			System.err.println("Unable to read from " + config.getAbsolutePath() + "! Please check file permissions!");
			e.printStackTrace();
		} finally {
			try {
				if (lock != null)
					lock.release();
			} catch (IOException e) {
			}
		}
	}
	
	public void save() {
		FileLock lock = null;
		try (RandomAccessFile raf = new RandomAccessFile(config, "rw")) {
			lock = raf.getChannel().lock();
			raf.setLength(0);
			String json = gson.toJson(this, Config.class);
			OutputStreamWriter osw = new OutputStreamWriter(Channels.newOutputStream(raf.getChannel()), Charsets.UTF_8);
			osw.write(json);
			osw.close();
			raf.getChannel().close();
			raf.close();
		} catch (Exception e) {
			System.err.println("Unable to write to " + config.getAbsolutePath() + "! Please check file permissions!");
			e.printStackTrace();
		} finally {
			try {
				if (lock != null)
					lock.release();
			} catch (IOException e) {
			}
		}
	}

}
