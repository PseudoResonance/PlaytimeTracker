package io.github.pseudoresonance.playtimetracker;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.ForgeConfigSpec.LongValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber(modid = PlaytimeTracker.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ConfigHandler {
	
	private static PlaytimeTracker playtimeTracker;
	
	protected static void setup(PlaytimeTracker playtimeTracker) {
		ConfigHandler.playtimeTracker = playtimeTracker;
	}

	public static final ClientConfig CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;
	static {
		final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
		CLIENT_SPEC = specPair.getRight();
		CLIENT = specPair.getLeft();
	}
	
	public static boolean enabled;
	public static String database;
	public static long saveInterval;
	public static boolean multiInstance;
	public static String alignMode;
	public static int xOffset;
	public static int yOffset;
	public static String alignModeMain;
	public static int xOffsetMain;
	public static int yOffsetMain;
	public static int barColor;
	public static int barBackgroundColor;
	public static int borderColor;
	public static int currentColor;

	public static void bakeConfig() {
		enabled = CLIENT.enabled.get();
		database = CLIENT.database.get();
		saveInterval = CLIENT.saveInterval.get();
		multiInstance = CLIENT.multiInstance.get();
		alignMode = CLIENT.alignMode.get();
		xOffset = CLIENT.xOffset.get();
		yOffset = CLIENT.yOffset.get();
		alignModeMain = CLIENT.alignModeMain.get();
		xOffsetMain = CLIENT.xOffsetMain.get();
		yOffsetMain = CLIENT.yOffsetMain.get();
		barColor = CLIENT.barColor.get();
		barBackgroundColor = CLIENT.barBackgroundColor.get();
		borderColor = CLIENT.borderColor.get();
		currentColor = CLIENT.currentColor.get();
	}
	
	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
		if (configEvent.getConfig().getSpec() == ConfigHandler.CLIENT_SPEC) {
			String oldDatabase = database;
			bakeConfig();
			if (playtimeTracker.getDatastore() != null) {
				if (oldDatabase == null || !oldDatabase.equals(database))
					playtimeTracker.getDatastore().updateLocation(database);
				if (multiInstance)
					playtimeTracker.getDatastore().startClock();
				else
					playtimeTracker.getDatastore().stopClock();
			}
		}
	}

	
	public static class ClientConfig {
		
		public final BooleanValue enabled;
		public final ConfigValue<String> database;
		public final LongValue saveInterval;
		public final BooleanValue multiInstance;
		public final ConfigValue<String> alignMode;
		public final IntValue xOffset;
		public final IntValue yOffset;
		public final ConfigValue<String> alignModeMain;
		public final IntValue xOffsetMain;
		public final IntValue yOffsetMain;
		public final IntValue barColor;
		public final IntValue barBackgroundColor;
		public final IntValue borderColor;
		public final IntValue currentColor;

		public ClientConfig(ForgeConfigSpec.Builder builder) {
			builder.push("general");
			enabled = builder
					.comment("Enable display in pause menu")
					.translation(PlaytimeTracker.MODID + ".config." + "enabled")
					.define("enabled", true);
			database = builder
					.comment("Database file location")
					.translation(PlaytimeTracker.MODID + ".config." + "database")
					.define("database", "./playtime");
			saveInterval = builder
					.comment("Save interval in seconds")
					.translation(PlaytimeTracker.MODID + ".config." + "saveInterval")
					.defineInRange("saveInterval", 120, 0, Long.MAX_VALUE);
			multiInstance = builder
					.comment("Whether multiple instances are using the database file")
					.translation(PlaytimeTracker.MODID + ".config." + "multiInstance")
					.define("multiInstance", false);
			builder.pop();
			builder.push("HUD Settings");
			alignMode = builder
					.comment("Where the HUD should be rendered (topleft, topcenter, topright, bottomleft, bottomcenter, bottomright)")
					.translation(PlaytimeTracker.MODID + ".config." + "alignMode")
					.define("alignMode", "topcenter");
			xOffset = builder
					.comment("X offset")
					.translation(PlaytimeTracker.MODID + ".config." + "xOffset")
					.defineInRange("xOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
			yOffset = builder
					.comment("Y offset")
					.translation(PlaytimeTracker.MODID + ".config." + "yOffset")
					.defineInRange("yOffset", 3, Integer.MIN_VALUE, Integer.MAX_VALUE);
			builder.pop();
			builder.push("HUD Settings - Main Menu");
			alignModeMain = builder
					.comment("Where the HUD should be rendered on the main menu (topleft, topcenter, topright, bottomleft, bottomcenter, bottomright)")
					.translation(PlaytimeTracker.MODID + ".config." + "alignModeMain")
					.define("alignModeMain", "topcenter");
			xOffsetMain = builder
					.comment("X offset on the main menu")
					.translation(PlaytimeTracker.MODID + ".config." + "xOffsetMain")
					.defineInRange("xOffsetMain", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
			yOffsetMain = builder
					.comment("Y offset on the main menu")
					.translation(PlaytimeTracker.MODID + ".config." + "yOffsetMain")
					.defineInRange("yOffsetMain", 3, Integer.MIN_VALUE, Integer.MAX_VALUE);
			builder.pop();
			builder.push("Colors");
			barColor = builder
					.comment("Playtime graph bar color")
					.translation(PlaytimeTracker.MODID + ".config." + "barColor")
					.defineInRange("barColor", 0xFFFF0000, Integer.MIN_VALUE, Integer.MAX_VALUE);
			barBackgroundColor = builder
					.comment("Playtime graph bar background color")
					.translation(PlaytimeTracker.MODID + ".config." + "barBackgroundColor")
					.defineInRange("barBackgroundColor", 0xFF555555, Integer.MIN_VALUE, Integer.MAX_VALUE);
			borderColor = builder
					.comment("Playtime graph border color")
					.translation(PlaytimeTracker.MODID + ".config." + "borderColor")
					.defineInRange("borderColor", 0xFF000000, Integer.MIN_VALUE, Integer.MAX_VALUE);
			currentColor = builder
					.comment("Playtime current server text color")
					.translation(PlaytimeTracker.MODID + ".config." + "currentColor")
					.defineInRange("currentColor", 0xFFFF0000, Integer.MIN_VALUE, Integer.MAX_VALUE);
			builder.pop();
		}

	}

	protected static void setDatabase(String location) {
		database = location;
		CLIENT.database.set(location);
		CLIENT_SPEC.save();
	}

}
