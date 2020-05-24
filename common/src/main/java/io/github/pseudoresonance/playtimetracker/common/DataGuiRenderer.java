package io.github.pseudoresonance.playtimetracker.common;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.pseudoresonance.playtimetracker.common.Datastore.DataHolder;
import io.github.pseudoresonance.playtimetracker.common.Datastore.ListItem;

public class DataGuiRenderer {

	private final IPlaytimeTracker playtimeTracker;

	private String multiplayer = "";
	private String singleplayer = "";

	private static final int padding = 2;
	private static final int border = 1;

	private static final int bar_vertical_padding = 0;
	private static final int bar_left_padding = 230;
	private static final int bar_right_padding = 15;
	
	private boolean renderTooltip = false;
	private ArrayList<String> tooltipText = null;
	private int tooltipX = 0;
	private int tooltipY = 0;
	
	private boolean displaySession;
	private IButton toggleButton;

	private final HashMap<String, DataHolder> data = new HashMap<String, DataHolder>();
	private IGuiList list;
	private String total = "";

	private final HashMap<String, DataHolder> sessionData = new HashMap<String, DataHolder>();
	private IGuiList sessionList;
	private String sessionTotal = "";
	
	public DataGuiRenderer(IPlaytimeTracker playtimeTracker) {
		this.playtimeTracker = playtimeTracker;
	}
	
	public void setupListRenderer(IDataGui dataGui) {
		multiplayer = playtimeTracker.getLanguageManager().translate("menu.multiplayer");
		singleplayer = playtimeTracker.getLanguageManager().translate("menu.singleplayer");
		this.toggleButton = dataGui.addButton(dataGui.getWidth() / 2 - 152, dataGui.getHeight() - 30, 150, 20, displaySession ? playtimeTracker.getLanguageManager().translate("playtimetracker.menu.current") : playtimeTracker.getLanguageManager().translate("playtimetracker.menu.all"), () -> {
			displaySession = !displaySession;
			this.toggleButton.setText(displaySession ? playtimeTracker.getLanguageManager().translate("playtimetracker.menu.current") : playtimeTracker.getLanguageManager().translate("playtimetracker.menu.all"));
		});
		dataGui.addButton(dataGui.getWidth() / 2 + 2, dataGui.getHeight() - 30, 150, 20, playtimeTracker.getLanguageManager().translate("gui.done"), () -> {
			dataGui.openLastScreen();
		});

		this.data.clear();
		for (java.util.Map.Entry<String, DataHolder> entry : playtimeTracker.getDatastore().getPlaytimeMap().entrySet())
			data.put(entry.getKey(), entry.getValue().clone());

		this.sessionData.clear();
		for (java.util.Map.Entry<String, DataHolder> entry : playtimeTracker.getDatastore().getSessionPlaytimeMap().entrySet())
			sessionData.put(entry.getKey(), entry.getValue().clone());

		if (playtimeTracker.getDatastore().isLoggedOn()) {
			String name = playtimeTracker.getDatastore().getCurrentServerName();
			long update = System.currentTimeMillis() - playtimeTracker.getDatastore().getLastUpdateTime();
			DataHolder dh = null;
			if (data.containsKey(name)) {
				dh = data.get(name);
				dh.time += update;
			} else {
				dh = new DataHolder(name.substring(0, 2).equals("sp") ? name.substring(40) : name.substring(3), name.substring(0, 2).equals("sp"), "", playtimeTracker.getGame().getGameDirectory().getAbsolutePath(), playtimeTracker.getDatastore().getGameDirectoryUuid(), update);
				data.put(name, dh);
			}
			if (sessionData.containsKey(name))
				sessionData.get(name).time += update;
			else
				sessionData.put(name, new DataHolder(dh.name, dh.singleplayer, dh.nickname, dh.gameDirectory, dh.directoryUuid, update));
		}

		this.list = dataGui.initGuiList(data, dataGui.getWidth(), dataGui.getHeight(), 8 + playtimeTracker.getRenderer().getStringHeight() + 8, dataGui.getHeight() - 40, playtimeTracker.getRenderer().getStringHeight() + padding * 2);
		total = playtimeTracker.getUtils().currentTime(playtimeTracker.getDatastore().getTotalPlaytime());
		
		this.sessionList = dataGui.initGuiList(sessionData, dataGui.getWidth(), dataGui.getHeight(), 8 + playtimeTracker.getRenderer().getStringHeight() + 8, dataGui.getHeight() - 40, playtimeTracker.getRenderer().getStringHeight() + padding * 2);
		sessionTotal = playtimeTracker.getUtils().currentTime(playtimeTracker.getDatastore().getSessionTotalPlaytime());
	}
	
	public void renderListPage(int width, int height, int mouseX, int mouseY, float delta) {
		renderTooltip = false;
		if (displaySession)
			sessionList.render(mouseX, mouseY, delta);
		else
			list.render(mouseX, mouseY, delta);
		String text = playtimeTracker.getLanguageManager().translate("playtimetracker.playtime.total" + (displaySession ? ".session" : ""), displaySession ? sessionTotal : total);
		playtimeTracker.getRenderer().drawString(text, (width / 2) - (playtimeTracker.getRenderer().getStringWidth(text) / 2), 8, -1);
		if (renderTooltip)
			playtimeTracker.getRenderer().drawTooltip(tooltipText, tooltipX, tooltipY);
	}
	
	public void renderListItem(ListItem listItem, int x, int y, int itemWidth, int itemHeight, int mouseX, int mouseY) {
		String text = "";
		if (listItem.singleplayer)
			text = (listItem.nickname.isEmpty() ? listItem.name : listItem.nickname) + " (" + singleplayer + ")";
		else
			text = (listItem.nickname.isEmpty() ? listItem.name : listItem.nickname) + " (" + multiplayer + ")";
		playtimeTracker.getRenderer().drawString(text, 10 + padding, y + padding + border, listItem.currentItem ? playtimeTracker.getConfig().currentColor : -1);
		int maxWidth = (itemWidth - bar_right_padding) - bar_left_padding;
		double percentMax = (double) listItem.time / listItem.maxTime;
		int width = (int) Math.ceil(maxWidth * percentMax);
		// Bar
		playtimeTracker.getRenderer().drawRect(bar_left_padding + border, y + border + bar_vertical_padding, itemWidth - bar_right_padding - border, y + itemHeight - bar_vertical_padding - border, playtimeTracker.getConfig().barBackgroundColor); // Background
		playtimeTracker.getRenderer().drawRect(bar_left_padding + border, y + border + bar_vertical_padding, bar_left_padding + width - border, y + itemHeight - bar_vertical_padding - border, playtimeTracker.getConfig().barColor); // Progress
		// Borders
		playtimeTracker.getRenderer().drawRect(bar_left_padding, y + bar_vertical_padding, itemWidth - bar_right_padding, y + border + bar_vertical_padding, playtimeTracker.getConfig().borderColor); // Top
		playtimeTracker.getRenderer().drawRect(bar_left_padding, y + itemHeight - border - bar_vertical_padding, itemWidth - bar_right_padding, y + itemHeight - bar_vertical_padding, playtimeTracker.getConfig().borderColor); // Bottom
		playtimeTracker.getRenderer().drawRect(bar_left_padding, y + bar_vertical_padding, bar_left_padding + border, y + itemHeight - bar_vertical_padding, playtimeTracker.getConfig().borderColor); // Left
		playtimeTracker.getRenderer().drawRect(itemWidth - bar_right_padding - border, y + bar_vertical_padding, itemWidth - bar_right_padding, y + itemHeight - bar_vertical_padding, playtimeTracker.getConfig().borderColor); // Right
		playtimeTracker.getRenderer().drawString(listItem.value, bar_left_padding + padding, y + padding + border, -1);
		if (checkHover(10 + padding, y + padding + border, playtimeTracker.getRenderer().getStringWidth(text), playtimeTracker.getRenderer().getStringHeight(), mouseX, mouseY)) {
			ArrayList<String> list = new ArrayList<String>();
			if (!listItem.nickname.isEmpty())
				list.add(listItem.name);
			if (listItem.singleplayer)
				if (listItem.currentDirectory)
					list.add(playtimeTracker.getLanguageManager().translate("playtimetracker.menu.currentdirectory"));
				else
					list.add(playtimeTracker.getLanguageManager().translate("playtimetracker.menu.fromdirectory", listItem.gameDirectory));
			if (!list.isEmpty()) {
				renderTooltip = true;
				tooltipText = list;
				tooltipX = mouseX;
				tooltipY = mouseY;
			}
		}
	}
	
	private boolean checkHover(int x, int y, int width, int height, int mouseX, int mouseY) {
		return mouseX >= (double) x && mouseY >= (double) y && mouseX < (double) (x + width) && mouseY < (double) (y + height);
	}

}
