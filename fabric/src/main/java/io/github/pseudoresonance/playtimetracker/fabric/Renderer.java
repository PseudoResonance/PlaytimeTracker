package io.github.pseudoresonance.playtimetracker.fabric;

import java.util.List;

import io.github.pseudoresonance.playtimetracker.common.IRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;

public class Renderer implements IRenderer {

	@SuppressWarnings("resource")
	@Override
	public int getStringWidth(String text) {
		return MinecraftClient.getInstance().textRenderer.getStringWidth(text);
	}

	@SuppressWarnings("resource")
	@Override
	public int getStringHeight() {
		return MinecraftClient.getInstance().textRenderer.fontHeight;
	}

	@SuppressWarnings("resource")
	@Override
	public int drawStringShadow(String text, float x, float y, int color) {
		return MinecraftClient.getInstance().textRenderer.drawWithShadow(text, x, y, color);
	}

	@SuppressWarnings("resource")
	@Override
	public int drawString(String text, float x, float y, int color) {
		return MinecraftClient.getInstance().textRenderer.draw(text, x, y, color);
	}

	@SuppressWarnings("resource")
	@Override
	public void drawTooltip(String text, int x, int y) {
		MinecraftClient.getInstance().currentScreen.renderTooltip(text, x, y);
	}

	@SuppressWarnings("resource")
	@Override
	public void drawTooltip(List<String> text, int x, int y) {
		MinecraftClient.getInstance().currentScreen.renderTooltip(text, x, y);
	}

	@SuppressWarnings("resource")
	@Override
	public boolean isMainMenu() {
		return MinecraftClient.getInstance().currentScreen instanceof TitleScreen;
	}

	@SuppressWarnings("resource")
	@Override
	public boolean isIngameMenu() {
		return MinecraftClient.getInstance().currentScreen instanceof GameMenuScreen;
	}
	
	public int getStringX(int width, AlignMode alignMode, int xOffset) {
		if (alignMode == AlignMode.TOPCENTER || alignMode == AlignMode.BOTTOMCENTER)
			return MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 - width / 2 + xOffset;
		else if (alignMode == AlignMode.TOPRIGHT || alignMode == AlignMode.BOTTOMRIGHT)
			return MinecraftClient.getInstance().getWindow().getScaledWidth() - width - xOffset;
		else
			return xOffset;
	}
	
	public int getStringY(int height, AlignMode alignMode, int yOffset) {
		if (alignMode == AlignMode.BOTTOMLEFT || alignMode == AlignMode.BOTTOMCENTER || alignMode == AlignMode.BOTTOMRIGHT)
			return MinecraftClient.getInstance().getWindow().getScaledHeight() - height - yOffset;
		else
			return yOffset;
	}

	@Override
	public void drawRect(int x1, int y1, int x2, int y2, int color) {
		Screen.fill(x1, y1, x2, y2, color);
	}

}
