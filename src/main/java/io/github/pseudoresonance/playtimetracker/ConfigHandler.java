package io.github.pseudoresonance.playtimetracker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ConfigHandler {

	private static PlaytimeTracker playtimeTracker;

	private Configuration config;

	ConfigHandler(PlaytimeTracker playtimeTracker, File file) {
		ConfigHandler.playtimeTracker = playtimeTracker;
		config = new Configuration(file);
		config.load();
		enabled = config.getBoolean("enabled", Configuration.CATEGORY_GENERAL, enabled, "Enable display in pause menu");
		database = config.getString("database", Configuration.CATEGORY_GENERAL, database, "Database file location");
		saveInterval = config.getInt("saveInterval", Configuration.CATEGORY_GENERAL, saveInterval, 0, Integer.MAX_VALUE, "Save interval in seconds");
		alignMode = config.getString("alignMode", Configuration.CATEGORY_GENERAL, alignMode, "Where the HUD should be rendered (topleft, topcenter, topright, bottomleft, bottomcenter, bottomright)");
		xOffset = config.getInt("xOffset", Configuration.CATEGORY_GENERAL, xOffset, Integer.MIN_VALUE, Integer.MAX_VALUE, "X offset");
		yOffset = config.getInt("yOffset", Configuration.CATEGORY_GENERAL, yOffset, Integer.MIN_VALUE, Integer.MAX_VALUE, "Y offset");
		alignModeMain = config.getString("alignModeMain", Configuration.CATEGORY_GENERAL, alignModeMain, "Where the HUD should be rendered on the main menu (topleft, topcenter, topright, bottomleft, bottomcenter, bottomright)");
		xOffsetMain = config.getInt("xOffsetMain", Configuration.CATEGORY_GENERAL, xOffsetMain, Integer.MIN_VALUE, Integer.MAX_VALUE, "X offset on the main menu");
		yOffsetMain = config.getInt("yOffsetMain", Configuration.CATEGORY_GENERAL, yOffsetMain, Integer.MIN_VALUE, Integer.MAX_VALUE, "Y offset on the main menu");
		multiInstance = config.getBoolean("multiInstance", Configuration.CATEGORY_GENERAL, multiInstance, "Whether multiple instances are using the database file");
		barColor = config.getInt("barColor", Configuration.CATEGORY_GENERAL, barColor, Integer.MIN_VALUE, Integer.MAX_VALUE, "Playtime graph bar color");
		barBackgroundColor = config.getInt("barBackgroundColor", Configuration.CATEGORY_GENERAL, barBackgroundColor, Integer.MIN_VALUE, Integer.MAX_VALUE, "Playtime graph bar background color");
		borderColor = config.getInt("borderColor", Configuration.CATEGORY_GENERAL, borderColor, Integer.MIN_VALUE, Integer.MAX_VALUE, "Playtime graph border color");
		currentColor = config.getInt("currentColor", Configuration.CATEGORY_GENERAL, currentColor, Integer.MIN_VALUE, Integer.MAX_VALUE, "Playtime current server text color");
		config.save();
	}

	public static boolean enabled = true;
	public static String database = "./playtime";
	public static int saveInterval = 120;
	public static String alignMode = "topcenter";
	public static int xOffset = 0;
	public static int yOffset = 3;
	public static String alignModeMain = "topcenter";
	public static int xOffsetMain = 0;
	public static int yOffsetMain = 3;
	public static boolean multiInstance = false;
	public static int barColor = 0xFFFF0000;
	public static int barBackgroundColor = 0xFF555555;
	public static int borderColor = 0xFF000000;
	public static int currentColor = 0xFFFF0000;
	
	@SubscribeEvent
	public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.modID.equals(PlaytimeTracker.MODID)) {
			ConfigCategory cat = config.getCategory(Configuration.CATEGORY_GENERAL);
			enabled = cat.get("enabled").getBoolean();
			database = cat.get("database").getString();
			saveInterval = cat.get("saveInterval").getInt();
			alignMode = cat.get("alignMode").getString();
			xOffset = cat.get("xOffset").getInt();
			yOffset = cat.get("yOffset").getInt();
			alignModeMain = cat.get("alignModeMain").getString();
			xOffsetMain = cat.get("xOffsetMain").getInt();
			yOffsetMain = cat.get("yOffsetMain").getInt();
			multiInstance = cat.get("multiInstance").getBoolean();
			barColor = cat.get("barColor").getInt();
			barBackgroundColor = cat.get("barBackgroundColor").getInt();
			borderColor = cat.get("borderColor").getInt();
			currentColor = cat.get("currentColor").getInt();
			if (multiInstance)
				playtimeTracker.getDatastore().startClock();
			else
				playtimeTracker.getDatastore().stopClock();
		}
	}

	protected void setDatabase(String location) {
		database = location;
		config.get(Configuration.CATEGORY_GENERAL, "database", database).set(location);
		config.save();
	}

	protected Configuration getConfiguration() {
		return config;
	}

}
