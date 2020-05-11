package io.github.pseudoresonance.playtimetracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static java.util.Collections.reverseOrder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;

public class DataGui extends Screen {

	private final Screen lastScreen;

	private String multiplayer = "";
	private String singleplayer = "";
	private static boolean displaySession = false;
	private ButtonWidget toggleButton = null;

	private final HashMap<String, Long> data = new HashMap<String, Long>();
	private DataGui.List list;
	private String total = "";

	private final HashMap<String, Long> sessionData = new HashMap<String, Long>();
	private DataGui.List sessionList;
	private String sessionTotal = "";

	DataGui(Screen lastScreen) {
		super(new TranslatableText(""));
		this.lastScreen = lastScreen;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when
	 * the GUI is displayed and when the window resizes, the buttonList is cleared
	 * beforehand.
	 */
	@Override
	public void init() {
		multiplayer = I18n.translate("menu.multiplayer");
		singleplayer = I18n.translate("menu.singleplayer");
		this.toggleButton = this.addButton(new ButtonWidget(this.width / 2 - 152, this.height - 30, 150, 20, displaySession ? I18n.translate("playtimetracker.menu.current") : I18n.translate("playtimetracker.menu.all"), (p_213109_1_) -> {
			displaySession = !displaySession;
			this.toggleButton.setMessage(displaySession ? I18n.translate("playtimetracker.menu.current") : I18n.translate("playtimetracker.menu.all"));
		}));
		this.addButton(new ButtonWidget(this.width / 2 + 2, this.height - 30, 150, 20, I18n.translate("gui.done"), (p_213109_1_) -> {
			MinecraftClient.getInstance().openScreen(this.lastScreen);
		}));
		this.data.clear();
		data.putAll(PlaytimeTracker.getInstance().getDatastore().getPlaytimeMap());

		this.sessionData.clear();
		sessionData.putAll(PlaytimeTracker.getInstance().getDatastore().getSessionPlaytimeMap());

		if (PlaytimeTracker.getInstance().getDatastore().isLoggedOn()) {
			String name = PlaytimeTracker.getInstance().getDatastore().getCurrentServerName();
			long update = System.currentTimeMillis() - PlaytimeTracker.getInstance().getDatastore().getLastUpdateTime();
			long newVal = update;
			if (data.containsKey(name))
				newVal += data.get(name);
			data.put(name, newVal);
			long newSessionVal = update;
			if (sessionData.containsKey(name))
				newSessionVal += sessionData.get(name);
			sessionData.put(name, newSessionVal);
		}

		this.list = new DataGui.List(data);
		total = PlaytimeTracker.getInstance().getClockHud().currentTime(PlaytimeTracker.getInstance().getDatastore().getTotalPlaytime());

		this.sessionList = new DataGui.List(sessionData);
		sessionTotal = PlaytimeTracker.getInstance().getClockHud().currentTime(PlaytimeTracker.getInstance().getDatastore().getSessionTotalPlaytime());
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		this.renderBackground();
		if (displaySession)
			this.sessionList.render(mouseX, mouseY, partialTicks);
		else
			this.list.render(mouseX, mouseY, partialTicks);
		this.drawCenteredString(this.font, I18n.translate("playtimetracker.playtime.total" + (displaySession ? ".session" : ""), displaySession ? sessionTotal : total), this.width / 2, 8, -1);
		super.render(mouseX, mouseY, partialTicks);
	}

	class List extends EntryListWidget<DataGui.List.Entry> {

		private static final int padding = 2;

		private ArrayList<String> keys = new ArrayList<String>();
		private long maxVal;

		public List(HashMap<String, Long> data) {
			super(DataGui.this.minecraft, DataGui.this.width, DataGui.this.height, 8 + DataGui.this.font.fontHeight + 8, DataGui.this.height - 40, DataGui.this.font.fontHeight + padding * 2);
			data.entrySet().stream().sorted(reverseOrder(Map.Entry.comparingByValue())).forEach(entry -> keys.add(entry.getKey()));
			if (keys.size() > 0)
				maxVal = data.get(keys.get(0));

			for (String server : keys) {
				long time = data.get(server);
				Entry entry = new Entry(server, PlaytimeTracker.getInstance().getClockHud().currentTime(time), time, maxVal, this.itemHeight);
				this.addEntry(entry);
			}
		}

		protected int getItemCount() {
			return keys.size();
		}

		/**
		 * Returns true if the element passed in is currently selected
		 */
		protected boolean isSelectedItem(int slotIndex) {
			return false;
		}

		protected void renderBackground() {
		}

		protected void renderItem(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
		}

		protected int getScrollBarX() {
			return this.width - 10;
		}

		@Environment(EnvType.CLIENT)
		public class Entry extends EntryListWidget.Entry<DataGui.List.Entry> {

			private final String server;
			private final String value;
			private final long time;
			private final long maxVal;
			private final int itemHeight;

			private Entry(String server, String value, long time, long maxVal, int itemHeight) {
				this.server = server;
				this.value = value;
				this.time = time;
				this.maxVal = maxVal;
				this.itemHeight = itemHeight;
			}

			public void render(int slotIndex, int yPos, int xPos, int widthIn, int heightIn, int mouseXIn, int mouseYIn, boolean hover, float partialTicks) {
				if (slotIndex >= keys.size())
					return;
				String text = "";
				if (server.startsWith("mp."))
					text = server.substring(3, server.length()).replace(',', '.') + " (" + multiplayer + ")";
				else if (server.startsWith("sp."))
					text = server.substring(3, server.length()).replaceAll("%2E", ".") + " (" + singleplayer + ")";
				DataGui.this.font.draw(text, 10 + padding, yPos + padding, server.equals(PlaytimeTracker.getInstance().getDatastore().getCurrentServerName()) ? PlaytimeTracker.getInstance().getConfig().currentColor : -1);
				int maxWidth = (DataGui.this.width - 15) - 230;
				double percentMax = (double) time / this.maxVal;
				int width = (int) Math.ceil(maxWidth * percentMax);
				// Bar
				Screen.fill(230, yPos, DataGui.this.width - 15, yPos + this.itemHeight - padding, PlaytimeTracker.getInstance().getConfig().barBackgroundColor); // Background
				Screen.fill(230, yPos, 230 + width, yPos + this.itemHeight - padding, PlaytimeTracker.getInstance().getConfig().barColor); // Progress
				// Borders
				Screen.fill(230, yPos, DataGui.this.width - 15, yPos + 1, PlaytimeTracker.getInstance().getConfig().borderColor); // Top
				Screen.fill(230, yPos + this.itemHeight - 1 - padding, DataGui.this.width - 15, yPos + this.itemHeight - padding, PlaytimeTracker.getInstance().getConfig().borderColor); // Bottom
				Screen.fill(230, yPos, 231, yPos + this.itemHeight - padding, PlaytimeTracker.getInstance().getConfig().borderColor); // Left
				Screen.fill(DataGui.this.width - 16, yPos, DataGui.this.width - 15, yPos + this.itemHeight - padding, PlaytimeTracker.getInstance().getConfig().borderColor); // Right
				DataGui.this.font.draw(value, 230 + padding, yPos + padding, -1);
			}
		}

	}

}
