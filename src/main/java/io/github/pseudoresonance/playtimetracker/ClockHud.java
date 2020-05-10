package io.github.pseudoresonance.playtimetracker;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClockHud {

	private final static DecimalFormat df = new DecimalFormat("00");

	private PlaytimeTracker playtimeTracker;

	private TextHitbox hitbox = null;

	ClockHud(PlaytimeTracker playtimeTracker) {
		this.playtimeTracker = playtimeTracker;
	}

	@SubscribeEvent
	public void onInitGuiEvent(final InitGuiEvent event) {
		final Screen gui = event.getGui();
		if (gui instanceof IngameMenuScreen || gui instanceof MainMenuScreen) {
			hitbox = new TextHitbox();
			hitbox.active = false;
			event.addWidget(hitbox);
		}
	}

	private int getX(int width, String alignMode, int xOffset) {
		if (alignMode.equalsIgnoreCase("topcenter") || alignMode.equalsIgnoreCase("bottomcenter"))
			return Minecraft.getInstance().func_228018_at_().getScaledWidth() / 2 - width / 2 + xOffset;
		else if (alignMode.equalsIgnoreCase("topright") || alignMode.equalsIgnoreCase("bottomright"))
			return Minecraft.getInstance().func_228018_at_().getScaledWidth() - width - xOffset;
		else
			return xOffset;
	}

	private int getY(int height, String alignMode, int yOffset) {
		if (alignMode.equalsIgnoreCase("bottomleft") || alignMode.equalsIgnoreCase("bottomcenter") || alignMode.equalsIgnoreCase("bottomright"))
			return Minecraft.getInstance().func_228018_at_().getScaledHeight() - height - yOffset;
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

	private class TextHitbox extends Widget {
		
		public TextHitbox() {
			super(0, 0, 0, 0, "");
		}

		@SuppressWarnings("resource")
		public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
			if (this.active && this.visible) {
				if (this.isValidClickButton(p_mouseClicked_5_)) {
					boolean flag = this.clicked(p_mouseClicked_1_, p_mouseClicked_3_);
					if (flag) {
						this.playDownSound(Minecraft.getInstance().getSoundHandler());
						Minecraft.getInstance().displayGuiScreen(new DataGui(playtimeTracker, Minecraft.getInstance().currentScreen));
						return true;
					}
				}
				return false;
			} else {
				return false;
			}
		}

		protected boolean clicked(double x, double y) {
			return this.active && this.visible && x >= (double) this.x && y >= (double) this.y && x < (double) (this.x + this.width) && y < (double) (this.y + this.height);
		}

		public void setBounds(int xIn, int yIn, int widthIn, int heightIn) {
			this.x = xIn;
			this.y = yIn;
			this.width = widthIn;
			this.height = heightIn;
			active = true;
		}

		@SuppressWarnings("resource")
		public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			if (ConfigHandler.enabled && Minecraft.getInstance().currentScreen instanceof IngameMenuScreen) {
				String clock = I18n.format("playtimetracker.playtime.session", currentClock(playtimeTracker.getDatastore().getSessionCurrentPlaytime()));
				int width = Minecraft.getInstance().fontRenderer.getStringWidth(clock);
				int height = Minecraft.getInstance().fontRenderer.FONT_HEIGHT;
				int xBase = getX(width, ConfigHandler.alignMode, ConfigHandler.xOffset);
				int yBase = getY(height, ConfigHandler.alignMode, ConfigHandler.yOffset);
				Minecraft.getInstance().fontRenderer.drawStringWithShadow(clock, xBase, yBase, 0xffffff);
				setBounds(xBase, yBase, width, height);
			} else if (ConfigHandler.enabled && Minecraft.getInstance().currentScreen instanceof MainMenuScreen) {
				String clock = I18n.format("playtimetracker.playtime.total.session", currentClock(playtimeTracker.getDatastore().getSessionTotalPlaytime()));
				int width = Minecraft.getInstance().fontRenderer.getStringWidth(clock);
				int height = Minecraft.getInstance().fontRenderer.FONT_HEIGHT;
				int xBase = getX(width, ConfigHandler.alignModeMain, ConfigHandler.xOffsetMain);
				int yBase = getY(height, ConfigHandler.alignModeMain, ConfigHandler.yOffsetMain);
				Minecraft.getInstance().fontRenderer.drawStringWithShadow(clock, xBase, yBase, 0xffffff);
				setBounds(xBase, yBase, width, height);
			}
		}

	}

}
