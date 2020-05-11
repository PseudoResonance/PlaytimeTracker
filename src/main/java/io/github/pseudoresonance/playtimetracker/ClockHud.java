package io.github.pseudoresonance.playtimetracker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.resource.language.I18n;

public class ClockHud {

	private final static DecimalFormat df = new DecimalFormat("00");

	private static Method addButtonMethod = null;

	private TextHitbox hitbox = null;

	public void onInitScreen(Screen screen) {
		try {
			if (addButtonMethod == null) {
				addButtonMethod = Screen.class.getDeclaredMethod("addButton", AbstractButtonWidget.class);
				addButtonMethod.setAccessible(true);
			}
			if (screen instanceof GameMenuScreen || screen instanceof TitleScreen) {
				hitbox = new TextHitbox();
				hitbox.active = false;
				addButtonMethod.invoke(screen, hitbox);
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			System.err.println("Unable to inject playtime display!");
			e.printStackTrace();
		}
	}

	private int getX(int width, String alignMode, int xOffset) {
		if (alignMode.equalsIgnoreCase("topcenter") || alignMode.equalsIgnoreCase("bottomcenter"))
			return MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 - width / 2 + xOffset;
		else if (alignMode.equalsIgnoreCase("topright") || alignMode.equalsIgnoreCase("bottomright"))
			return MinecraftClient.getInstance().getWindow().getScaledWidth() - width - xOffset;
		else
			return xOffset;
	}

	private int getY(int height, String alignMode, int yOffset) {
		if (alignMode.equalsIgnoreCase("bottomleft") || alignMode.equalsIgnoreCase("bottomcenter") || alignMode.equalsIgnoreCase("bottomright"))
			return MinecraftClient.getInstance().getWindow().getScaledHeight() - height - yOffset;
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
				sYears = I18n.translate("playtimetracker.date.relative.years.singular", String.valueOf(years)) + " ";
			else
				sYears = I18n.translate("playtimetracker.date.relative.years", String.valueOf(years)) + " ";
		if (months > 0 || years > 0)
			if (months == 1)
				sMonths = I18n.translate("playtimetracker.date.relative.months.singular", String.valueOf(months)) + " ";
			else
				sMonths = I18n.translate("playtimetracker.date.relative.months", String.valueOf(months)) + " ";
		if (days > 0 || months > 0 || years > 0)
			if (days == 1)
				sDays = I18n.translate("playtimetracker.date.relative.days.singular", String.valueOf(days)) + " ";
			else
				sDays = I18n.translate("playtimetracker.date.relative.days", String.valueOf(days)) + " ";
		sHours = String.valueOf(hours);
		sMinutes = df.format(minutes);
		sSeconds = df.format(seconds);
		String ret = sYears + sMonths + sDays + sHours + ":" + sMinutes + ":" + sSeconds;
		return ret;
	}

	private class TextHitbox extends AbstractPressableButtonWidget {

		public TextHitbox() {
			super(0, 0, 0, 0, "");
		}

		protected boolean clicked(double x, double y) {
			return this.active && this.visible && x >= (double) this.x && y >= (double) this.y && x < (double) (this.x + this.width) && y < (double) (this.y + this.height);
		}

		@SuppressWarnings("resource")
		@Override
		public void onPress() {
			MinecraftClient.getInstance().openScreen(new DataGui(MinecraftClient.getInstance().currentScreen));
			active = true;
		}

		public void setBounds(int xIn, int yIn, int widthIn, int heightIn) {
			this.x = xIn;
			this.y = yIn;
			this.width = widthIn;
			this.height = heightIn;
			active = true;
		}

		@SuppressWarnings("resource")
		public void render(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			if (PlaytimeTracker.getInstance().getConfig().enabled && MinecraftClient.getInstance().currentScreen instanceof GameMenuScreen) {
				String clock = I18n.translate("playtimetracker.playtime.session", currentClock(PlaytimeTracker.getInstance().getDatastore().getSessionCurrentPlaytime()));
				int width = MinecraftClient.getInstance().textRenderer.getStringWidth(clock);
				int height = MinecraftClient.getInstance().textRenderer.fontHeight;
				int xBase = getX(width, PlaytimeTracker.getInstance().getConfig().alignMode, PlaytimeTracker.getInstance().getConfig().xOffset);
				int yBase = getY(height, PlaytimeTracker.getInstance().getConfig().alignMode, PlaytimeTracker.getInstance().getConfig().yOffset);
				MinecraftClient.getInstance().textRenderer.drawWithShadow(clock, xBase, yBase, 0xffffff);
				setBounds(xBase, yBase, width, height);
			} else if (PlaytimeTracker.getInstance().getConfig().enabled && MinecraftClient.getInstance().currentScreen instanceof TitleScreen) {
				String clock = I18n.translate("playtimetracker.playtime.total.session", currentClock(PlaytimeTracker.getInstance().getDatastore().getSessionTotalPlaytime()));
				int width = MinecraftClient.getInstance().textRenderer.getStringWidth(clock);
				int height = MinecraftClient.getInstance().textRenderer.fontHeight;
				int xBase = getX(width, PlaytimeTracker.getInstance().getConfig().alignModeMain, PlaytimeTracker.getInstance().getConfig().xOffsetMain);
				int yBase = getY(height, PlaytimeTracker.getInstance().getConfig().alignModeMain, PlaytimeTracker.getInstance().getConfig().yOffsetMain);
				MinecraftClient.getInstance().textRenderer.drawWithShadow(clock, xBase, yBase, 0xffffff);
				setBounds(xBase, yBase, width, height);
			}
		}

	}

}
