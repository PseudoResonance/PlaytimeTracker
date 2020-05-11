package io.github.pseudoresonance.playtimetracker;

import java.util.Arrays;
import java.util.function.Function;

import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;

public class ConfigGui implements ModMenuApi {

	@Override
	public String getModId() {
		return PlaytimeTracker.MODID;
	}

	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return this::getConfigScreenByCloth;
	}

	public Screen getConfigScreenByCloth(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle("playtimetracker.config.title");
		ConfigEntryBuilder eb = builder.entryBuilder();
		ConfigCategory general = builder.getOrCreateCategory("playtimetracker.config.general");
		general.addEntry(eb.startBooleanToggle("enabled", PlaytimeTracker.getInstance().getConfig().enabled).setDefaultValue(true).setTooltip("Enable display in pause menu").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().enabled = val).build());
		general.addEntry(eb.startStrField("database", PlaytimeTracker.getInstance().getConfig().database).setDefaultValue("./playtime").setTooltip("Database file location").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().database = val).build());
		general.addEntry(eb.startLongField("saveInterval", PlaytimeTracker.getInstance().getConfig().saveInterval).setDefaultValue(120).setTooltip("Save interval in seconds").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().saveInterval = val).build());
		general.addEntry(eb.startBooleanToggle("multiInstance", PlaytimeTracker.getInstance().getConfig().multiInstance).setDefaultValue(false).setTooltip("Whether multiple instances are using the database file").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().multiInstance = val).build());

		ConfigCategory hud = builder.getOrCreateCategory("playtimetracker.config.hud");
		hud.addEntry(eb.startStringDropdownMenu("alignMode", PlaytimeTracker.getInstance().getConfig().alignMode).setDefaultValue("topcenter").setTooltip("Where the HUD should be rendered").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().alignMode = val).setSelections(Arrays.asList("topleft", "topcenter", "topright", "bottomleft", "bottomcenter", "bottomright")).build());
		hud.addEntry(eb.startIntField("xOffset", PlaytimeTracker.getInstance().getConfig().xOffset).setDefaultValue(0).setTooltip("X offset").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().xOffset = val).build());
		hud.addEntry(eb.startIntField("yOffset", PlaytimeTracker.getInstance().getConfig().yOffset).setDefaultValue(3).setTooltip("Y offset").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().yOffset = val).build());

		ConfigCategory hudMain = builder.getOrCreateCategory("playtimetracker.config.hud.main");
		hudMain.addEntry(eb.startStringDropdownMenu("alignModeMain", PlaytimeTracker.getInstance().getConfig().alignModeMain).setDefaultValue("topcenter").setTooltip("Where the HUD should be rendered on the main menu").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().alignModeMain = val).setSelections(Arrays.asList("topleft", "topcenter", "topright", "bottomleft", "bottomcenter", "bottomright")).build());
		hudMain.addEntry(eb.startIntField("xOffsetMain", PlaytimeTracker.getInstance().getConfig().xOffsetMain).setDefaultValue(0).setTooltip("X offset on the main menu").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().xOffsetMain = val).build());
		hudMain.addEntry(eb.startIntField("yOffsetMain", PlaytimeTracker.getInstance().getConfig().yOffsetMain).setDefaultValue(3).setTooltip("Y offset on the main menu").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().yOffsetMain = val).build());

		ConfigCategory colors = builder.getOrCreateCategory("playtimetracker.config.colors");
		colors.addEntry(eb.startIntField("barColor", PlaytimeTracker.getInstance().getConfig().barColor).setDefaultValue(0xFFFF0000).setTooltip("Playtime graph bar color").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().barColor = val).build());
		colors.addEntry(eb.startIntField("barBackgroundColor", PlaytimeTracker.getInstance().getConfig().barBackgroundColor).setDefaultValue(0xFF555555).setTooltip("Playtime graph bar background color").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().barBackgroundColor = val).build());
		colors.addEntry(eb.startIntField("borderColor", PlaytimeTracker.getInstance().getConfig().borderColor).setDefaultValue(0xFF000000).setTooltip("Playtime graph border color").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().borderColor = val).build());
		colors.addEntry(eb.startIntField("currentColor", PlaytimeTracker.getInstance().getConfig().currentColor).setDefaultValue(0xFFFF0000).setTooltip("Playtime current server text color").setSaveConsumer(val -> PlaytimeTracker.getInstance().getConfig().currentColor = val).build());

		return builder.setSavingRunnable(() -> PlaytimeTracker.getInstance().getConfig().save()).build();
	}

}
