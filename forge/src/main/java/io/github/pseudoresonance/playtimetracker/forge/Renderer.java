package io.github.pseudoresonance.playtimetracker.forge;

import java.util.List;

import io.github.pseudoresonance.playtimetracker.common.IRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;

public class Renderer implements IRenderer {

	@SuppressWarnings("resource")
	@Override
	public int getStringWidth(String text) {
		return Minecraft.getInstance().fontRenderer.getStringWidth(text);
	}

	@SuppressWarnings("resource")
	@Override
	public int getStringHeight() {
		return Minecraft.getInstance().fontRenderer.FONT_HEIGHT;
	}

	@SuppressWarnings("resource")
	@Override
	public int drawStringShadow(String text, float x, float y, int color) {
		return Minecraft.getInstance().fontRenderer.drawStringWithShadow(text, x, y, color);
	}

	@SuppressWarnings("resource")
	@Override
	public int drawString(String text, float x, float y, int color) {
		return Minecraft.getInstance().fontRenderer.drawString(text, x, y, color);
	}

	@SuppressWarnings("resource")
	@Override
	public void drawTooltip(String text, int x, int y) {
		Minecraft.getInstance().currentScreen.renderTooltip(text, x, y);
	}

	@SuppressWarnings("resource")
	@Override
	public void drawTooltip(List<String> text, int x, int y) {
		Minecraft.getInstance().currentScreen.renderTooltip(text, x, y);
	}

	@SuppressWarnings("resource")
	@Override
	public boolean isMainMenu() {
		return Minecraft.getInstance().currentScreen instanceof MainMenuScreen;
	}

	@SuppressWarnings("resource")
	@Override
	public boolean isIngameMenu() {
		return Minecraft.getInstance().currentScreen instanceof IngameMenuScreen;
	}
	
	public int getStringX(int width, AlignMode alignMode, int xOffset) {
		if (alignMode == AlignMode.TOPCENTER || alignMode == AlignMode.BOTTOMCENTER)
			return Minecraft.getInstance().getMainWindow().getScaledWidth() / 2 - width / 2 + xOffset;
		else if (alignMode == AlignMode.TOPRIGHT || alignMode == AlignMode.BOTTOMRIGHT)
			return Minecraft.getInstance().getMainWindow().getScaledWidth() - width - xOffset;
		else
			return xOffset;
	}
	
	public int getStringY(int height, AlignMode alignMode, int yOffset) {
		if (alignMode == AlignMode.BOTTOMLEFT || alignMode == AlignMode.BOTTOMCENTER || alignMode == AlignMode.BOTTOMRIGHT)
			return Minecraft.getInstance().getMainWindow().getScaledHeight() - height - yOffset;
		else
			return yOffset;
	}

	@Override
	public void drawRect(int x1, int y1, int x2, int y2, int color) {
		Screen.fill(x1, y1, x2, y2, color);
	}

}
