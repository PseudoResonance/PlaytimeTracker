package io.github.pseudoresonance.playtimetracker.common;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import io.github.pseudoresonance.playtimetracker.common.IRenderer.IHitbox;

public class Utils {

	private final static DecimalFormat df = new DecimalFormat("00");

	private final IPlaytimeTracker playtimeTracker;
	
	public Utils(IPlaytimeTracker playtimeTracker) {
		this.playtimeTracker = playtimeTracker;
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
				sYears = playtimeTracker.getLanguageManager().translate("playtimetracker.date.relative.years.singular", String.valueOf(years)) + " ";
			else
				sYears = playtimeTracker.getLanguageManager().translate("playtimetracker.date.relative.years", String.valueOf(years)) + " ";
		if (months > 0 || years > 0)
			if (months == 1)
				sMonths = playtimeTracker.getLanguageManager().translate("playtimetracker.date.relative.months.singular", String.valueOf(months)) + " ";
			else
				sMonths = playtimeTracker.getLanguageManager().translate("playtimetracker.date.relative.months", String.valueOf(months)) + " ";
		if (days > 0 || months > 0 || years > 0)
			if (days == 1)
				sDays = playtimeTracker.getLanguageManager().translate("playtimetracker.date.relative.days.singular", String.valueOf(days)) + " ";
			else
				sDays = playtimeTracker.getLanguageManager().translate("playtimetracker.date.relative.days", String.valueOf(days)) + " ";
		sHours = String.valueOf(hours);
		sMinutes = df.format(minutes);
		sSeconds = df.format(seconds);
		String ret = sYears + sMonths + sDays + sHours + ":" + sMinutes + ":" + sSeconds;
		return ret;
	}
	
	public void logOn() {
		String server = "";
		if (playtimeTracker.getGame().isSingleplayer())
			server = "sp." + playtimeTracker.getDatastore().getGameDirectoryUuid() + "." + playtimeTracker.getGame().getWorldName().trim();
		else
			server = "mp." + playtimeTracker.getGame().getServerName().toLowerCase().split("/")[0].trim().replaceAll("^\\.+", "").replaceAll("\\.+$", "");;
		playtimeTracker.getDatastore().logOn(server);
	}
	
	public void renderClock(int x, int y, float val, IHitbox hitbox) {
		if (playtimeTracker.getConfig().enabled && playtimeTracker.getRenderer().isIngameMenu()) {
			String clock = playtimeTracker.getLanguageManager().translate("playtimetracker.playtime.session", currentClock(playtimeTracker.getDatastore().getSessionCurrentPlaytime()));
			int width = playtimeTracker.getRenderer().getStringWidth(clock);
			int height = playtimeTracker.getRenderer().getStringHeight();
			int xBase = playtimeTracker.getRenderer().getStringX(width, playtimeTracker.getConfig().alignMode, playtimeTracker.getConfig().xOffset);
			int yBase = playtimeTracker.getRenderer().getStringY(height, playtimeTracker.getConfig().alignMode, playtimeTracker.getConfig().yOffset);
			playtimeTracker.getRenderer().drawStringShadow(clock, xBase, yBase, 0xffffff);
			hitbox.setBounds(xBase, yBase, width, height);
		} else if (playtimeTracker.getConfig().enabled && playtimeTracker.getRenderer().isMainMenu()) {
			String clock = playtimeTracker.getLanguageManager().translate("playtimetracker.playtime.total.session", currentClock(playtimeTracker.getDatastore().getSessionTotalPlaytime()));
			int width = playtimeTracker.getRenderer().getStringWidth(clock);
			int height = playtimeTracker.getRenderer().getStringHeight();
			int xBase = playtimeTracker.getRenderer().getStringX(width, playtimeTracker.getConfig().alignModeMain, playtimeTracker.getConfig().xOffsetMain);
			int yBase = playtimeTracker.getRenderer().getStringY(height, playtimeTracker.getConfig().alignModeMain, playtimeTracker.getConfig().yOffsetMain);
			playtimeTracker.getRenderer().drawStringShadow(clock, xBase, yBase, 0xffffff);
			hitbox.setBounds(xBase, yBase, width, height);
		}
	}

}
