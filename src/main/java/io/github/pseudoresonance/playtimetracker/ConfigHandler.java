package io.github.pseudoresonance.playtimetracker;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = PlaytimeTracker.MODID)
@Config.LangKey("playtimetracker.config.title")
public class ConfigHandler {

	private static PlaytimeTracker playtimeTracker;

	ConfigHandler(PlaytimeTracker playtimeTracker) {
		ConfigHandler.playtimeTracker = playtimeTracker;
	}

	@Config.Comment("Enable display in pause menu")
	public static boolean enabled = true;

	@Config.Comment("Database file location")
	public static String database = "./playtime";

	@Config.Comment("Save interval in seconds")
	public static int saveInterval = 120;

	@Config.Comment("Where the HUD should be rendered (topleft, topcenter, topright, bottomleft, bottomcenter, bottomright)")
    public static String alignMode = "topcenter";

	@Config.Comment("X offset")
    public static int xOffset = 0;

	@Config.Comment("Y offset")
    public static int yOffset = 3;

	@Config.Comment("Where the HUD should be rendered on the main menu (topleft, topcenter, topright, bottomleft, bottomcenter, bottomright)")
    public static String alignModeMain = "topcenter";

	@Config.Comment("X offset on the main menu")
    public static int xOffsetMain = 0;

	@Config.Comment("Y offset on the main menu")
    public static int yOffsetMain = 3;

	@Config.Comment("Whether multiple instances are using the database file")
	public static boolean multiInstance = false;

	@Config.Comment("Playtime graph bar color")
    public static int barColor = 0xFFFF0000;

	@Config.Comment("Playtime graph bar background color")
    public static int barBackgroundColor = 0xFF555555;

	@Config.Comment("Playtime graph border color")
    public static int borderColor = 0xFF000000;

	@Config.Comment("Playtime current server text color")
    public static int currentColor = 0xFFFF0000;

	@Mod.EventBusSubscriber(modid = PlaytimeTracker.MODID)
	private static class ConfigEventHandler {
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.getModID().equals(PlaytimeTracker.MODID)) {
				ConfigManager.sync(PlaytimeTracker.MODID, Config.Type.INSTANCE);
				if (saveInterval <= 0)
					saveInterval = 120;
				if (multiInstance)
					playtimeTracker.getDatastore().startClock();
				else
					playtimeTracker.getDatastore().stopClock();
				ConfigManager.sync(PlaytimeTracker.MODID, Config.Type.INSTANCE);
			}
		}
	}
	
	protected static void setDatabase(String location) {
		database = location;
		ConfigManager.sync(PlaytimeTracker.MODID, Config.Type.INSTANCE);
	}

}
