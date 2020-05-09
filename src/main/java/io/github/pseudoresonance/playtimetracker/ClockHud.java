package io.github.pseudoresonance.playtimetracker;

import java.awt.Color;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

public class ClockHud {

	private final static DecimalFormat df = new DecimalFormat("00");

	private PlaytimeTracker playtimeTracker;

	private ScaledResolution scaledResolution;
	private int touchValue = 0;

	ClockHud(PlaytimeTracker playtimeTracker) {
		this.playtimeTracker = playtimeTracker;
	}

	@SubscribeEvent
	public void onRenderTick(RenderTickEvent event) {
		if (event.phase.equals(Phase.START))
			return;
		onTickInGame(Minecraft.getMinecraft());
	}

	public boolean onTickInGame(Minecraft mc) {
		if (playtimeTracker.getConfig().enabled && mc.currentScreen instanceof GuiIngameMenu) {
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			scaledResolution = new ScaledResolution(mc);
			display(mc);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		} else if (playtimeTracker.getConfig().enabled && mc.currentScreen instanceof GuiMainMenu) {
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			scaledResolution = new ScaledResolution(mc);
			displayTotal(mc);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		}
		return true;
	}

	private void testMouse(Minecraft mc, int x, int y, int width, int height) {
		int i = Mouse.getEventX() * mc.currentScreen.width / mc.displayWidth;
		int j = mc.currentScreen.height - Mouse.getEventY() * mc.currentScreen.height / mc.displayHeight - 1;
		int k = Mouse.getEventButton();
		if (Mouse.getEventButtonState()) {
			if (mc.gameSettings.touchscreen && this.touchValue++ > 0) {
				return;
			}
			if (i >= x && j >= y && i < x + width && j < y + height) {
				mc.displayGuiScreen(new DataGui(playtimeTracker, mc.currentScreen));
			}
		} else if (k != -1) {
			if (mc.gameSettings.touchscreen && --this.touchValue > 0) {
				return;
			}
		}
	}

	private void display(Minecraft mc) {
		String clock = I18n.format("playtimetracker.playtime.session", currentClock(playtimeTracker.getDatastore().getSessionCurrentPlaytime()));
		int width = mc.fontRenderer.getStringWidth(clock);
		int height = mc.fontRenderer.FONT_HEIGHT;
		int xBase = getX(width, playtimeTracker.getConfig().alignMode, playtimeTracker.getConfig().xOffset);
		int yBase = getY(height, playtimeTracker.getConfig().alignMode, playtimeTracker.getConfig().yOffset);
		mc.fontRenderer.drawStringWithShadow(clock, xBase, yBase, 0xffffff);
		testMouse(mc, xBase, yBase, width, height);
	}

	private void displayTotal(Minecraft mc) {
		String clock = I18n.format("playtimetracker.playtime.total.session", currentClock(playtimeTracker.getDatastore().getSessionTotalPlaytime()));
		int width = mc.fontRenderer.getStringWidth(clock);
		int height = mc.fontRenderer.FONT_HEIGHT;
		int xBase = getX(width, playtimeTracker.getConfig().alignModeMain, playtimeTracker.getConfig().xOffsetMain);
		int yBase = getY(height, playtimeTracker.getConfig().alignModeMain, playtimeTracker.getConfig().yOffsetMain);
		mc.fontRenderer.drawStringWithShadow(clock, xBase, yBase, 0xffffff);
		testMouse(mc, xBase, yBase, width, height);
	}

	private int getX(int width, String alignMode, int xOffset) {
		if (alignMode.equalsIgnoreCase("topcenter") || alignMode.equalsIgnoreCase("bottomcenter"))
			return scaledResolution.getScaledWidth() / 2 - width / 2 + xOffset;
		else if (alignMode.equalsIgnoreCase("topright") || alignMode.equalsIgnoreCase("bottomright"))
			return scaledResolution.getScaledWidth() - width - xOffset;
		else
			return xOffset;
	}

	private int getY(int height, String alignMode, int yOffset) {
		if (alignMode.equalsIgnoreCase("bottomleft") || alignMode.equalsIgnoreCase("bottomcenter") || alignMode.equalsIgnoreCase("bottomright"))
			return scaledResolution.getScaledHeight() - height - yOffset;
		else
			return yOffset;
	}

	public String currentClock(long time) {
		LocalDateTime temp = LocalDateTime.from(new Timestamp(System.currentTimeMillis() - time).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
		LocalDateTime now = LocalDateTime.now();
		long hours = temp.until(now, ChronoUnit.HOURS);
		temp = temp.plusHours(hours);
		long minutes = temp.until(now, ChronoUnit.MINUTES);
		temp = temp.plusMinutes(minutes);
		long seconds = temp.until(now, ChronoUnit.SECONDS);
		return hours + ":" + df.format(minutes) + ":" + df.format(seconds);
	}

	public String currentTime(long time) {
		return formatTimeAgo(new Timestamp(System.currentTimeMillis() - time).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
	}

	/**
	 * Formats time ago
	 * 
	 * @param dateTime
	 *                     {@link LocalDateTime} to format
	 * @return Formatted time ago
	 */
	public String formatTimeAgo(LocalDateTime dateTime) {
		LocalDateTime temp = LocalDateTime.from(dateTime);
		LocalDateTime now = LocalDateTime.now();
		long years = temp.until(now, ChronoUnit.YEARS);
		temp = temp.plusYears(years);
		long months = temp.until(now, ChronoUnit.MONTHS);
		temp = temp.plusMonths(months);
		long days = temp.until(now, ChronoUnit.DAYS);
		temp = temp.plusDays(days);
		long hours = temp.until(now, ChronoUnit.HOURS);
		temp = temp.plusHours(hours);
		long minutes = temp.until(now, ChronoUnit.MINUTES);
		temp = temp.plusMinutes(minutes);
		long seconds = temp.until(now, ChronoUnit.SECONDS);
		String sYears = "";
		String sMonths = "";
		String sDays = "";
		String sHours = "";
		String sMinutes = "";
		String sSeconds = "";
		if (years > 0)
			if (years == 1)
				sYears = I18n.format("playtimetracker.date.relative.years.singular", String.valueOf(years)) + " ";
			else
				sYears = I18n.format("playtimetracker.date.relative.years", String.valueOf(years)) + " ";
		if (months > 0 || years > 0)
			if (months == 1)
				sMonths = I18n.format("playtimetracker.date.relative.months.singular", String.valueOf(months)) + " ";
			else
				sMonths = I18n.format("playtimetracker.date.relative.months", String.valueOf(months)) + " ";
		if (days > 0 || months > 0 || years > 0)
			if (days == 1)
				sDays = I18n.format("playtimetracker.date.relative.days.singular", String.valueOf(days)) + " ";
			else
				sDays = I18n.format("playtimetracker.date.relative.days", String.valueOf(days)) + " ";
		sHours = String.valueOf(hours);
		sMinutes = df.format(minutes);
		sSeconds = df.format(seconds);
		String ret = sYears + sMonths + sDays + sHours + ":" + sMinutes + ":" + sSeconds;
		return ret;
	}

}
