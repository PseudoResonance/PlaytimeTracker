package io.github.pseudoresonance.playtimetracker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.logging.Level;

import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.utils.Validate;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class ConfigFile {

	private final String FILENAME;
	private final File FOLDER;
	private FileConfiguration config;
	private File configFile;

	/**
	 * Constructs new {@link ConfigFile} with given parameters
	 * 
	 * @param folder
	 *                     Folder that config file is stored in
	 * @param filename
	 *                     Filename of config file
	 * @param instance
	 *                     {@link PseudoPlugin} config file is for
	 */
	public ConfigFile(File folder, String filename) {
		if (!filename.endsWith(".yml")) {
			filename += ".yml";
		}
		this.FILENAME = filename;
		this.FOLDER = folder;
		this.config = null;
		this.configFile = null;
		reload();
	}

	/**
	 * Returns {@link FileConfiguration} from config file
	 * 
	 * @return {@link FileConfiguration} from config file
	 */
	public FileConfiguration getConfig() {
		if (config == null) {
			reload();
		}
		return config;
	}

	/**
	 * Returns {@link File} of the config file location
	 * 
	 * @return {@link File} of the config file location
	 */
	public File getConfigFile() {
		if (configFile == null) {
			reload();
		}
		return configFile;
	}

	/**
	 * Reloads config file from disk
	 */
	public void reload() {
		if (!this.FOLDER.exists()) {
			try {
				if (this.FOLDER.mkdir()) {
					System.err.println("Folder " + this.FOLDER.getName() + " created.");
				} else {
					System.err.println("Unable to create folder " + this.FOLDER.getName() + ".");
				}
			} catch (Exception e) {

			}
		}
		configFile = new File(this.FOLDER, this.FILENAME);
		if (!configFile.exists()) {
			if (!configFile.exists()) {
				try {
					configFile.createNewFile();
				} catch (Exception e) {

				}
			}
		}
		config = YamlConfiguration.loadConfiguration(configFile);
	}

	/**
	 * Reloads config file from disk
	 */
	public void reload(InputStreamReader in) {
		if (!this.FOLDER.exists()) {
			try {
				if (this.FOLDER.mkdir()) {
					System.err.println("Folder " + this.FOLDER.getName() + " created.");
				} else {
					System.err.println("Unable to create folder " + this.FOLDER.getName() + ".");
				}
			} catch (Exception e) {

			}
		}
		configFile = new File(this.FOLDER, this.FILENAME);
		if (!configFile.exists()) {
			if (!configFile.exists()) {
				try {
					configFile.createNewFile();
				} catch (Exception e) {

				}
			}
		}
		config = YamlConfiguration.loadConfiguration(in);
	}

	/**
	 * Saves config file to disk
	 */
	public void save() {
		if (config == null || configFile == null) {
			return;
		}
		try {
			getConfig().save(configFile);
		} catch (IOException ex) {
			System.err.println("Could not save config to " + configFile.getName());
			ex.printStackTrace();
		}
	}

	/**
	 * Saves config file to disk
	 */
	public void save(OutputStream out) {
		if (config == null || configFile == null) {
			return;
		}
		try {
			Validate.notNull(configFile, "File cannot be null");
			Files.createParentDirs(configFile);
			String data = config.saveToString();
			OutputStreamWriter writer = new OutputStreamWriter(out, config.UTF8_OVERRIDE && !config.UTF_BIG ? Charsets.UTF_8 : Charset.defaultCharset());
			try {
				writer.write(data);
			} finally {
				writer.close();
			}
		} catch (IOException ex) {
			System.err.println("Could not save config to " + configFile.getName());
			ex.printStackTrace();
		}
	}

	/**
	 * Sets data at the given path
	 * 
	 * @param path
	 *                 Path to set data to
	 * @param o
	 *                 Data to set
	 */
	public void set(String path, Object o) {
		getConfig().set(path, o);
	}
}