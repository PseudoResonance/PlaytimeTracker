package io.github.pseudoresonance.playtimetracker;

import java.io.File;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class ConfigGui extends GuiConfig {

	private static PlaytimeTracker playtimeTracker;

	protected static void setup(PlaytimeTracker playtimeTracker) {
		ConfigGui.playtimeTracker = playtimeTracker;
	}

	public ConfigGui(GuiScreen parent) {
		super(parent, new ConfigElement(playtimeTracker.getConfig().getConfiguration().getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), PlaytimeTracker.MODID, false, false, "PlaytimeTracker Config");
	}

	@Override
	public void initGui() {
		super.initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
	}

}
