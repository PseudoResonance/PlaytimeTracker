package io.github.pseudoresonance.playtimetracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static java.util.Collections.reverseOrder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.SlotGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;

public class DataGui extends Screen {

	private PlaytimeTracker playtimeTracker;
	private final Screen lastScreen;

	private String multiplayer = "";
	private String singleplayer = "";
	private static boolean displaySession = false;
	private Button toggleButton = null;

	private final HashMap<String, Long> data = new HashMap<String, Long>();
	private ArrayList<String> keys = new ArrayList<String>();
	private ArrayList<String> values = new ArrayList<String>();
	private DataGui.List list;
	private long maxVal = 0;
	private String total = "";

	private final HashMap<String, Long> sessionData = new HashMap<String, Long>();
	private ArrayList<String> sessionKeys = new ArrayList<String>();
	private ArrayList<String> sessionValues = new ArrayList<String>();
	private DataGui.List sessionList;
	private long sessionMaxVal = 0;
	private String sessionTotal = "";

	DataGui(PlaytimeTracker playtimeTracker, Screen lastScreen) {
	      super(new TranslationTextComponent(""));
		this.playtimeTracker = playtimeTracker;
		this.lastScreen = lastScreen;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when
	 * the GUI is displayed and when the window resizes, the buttonList is cleared
	 * beforehand.
	 */
	@Override
	public void init() {
		multiplayer = I18n.format("menu.multiplayer");
		singleplayer = I18n.format("menu.singleplayer");
		this.toggleButton = this.addButton(new Button(this.width / 2 - 152, this.height - 30, 150, 20, displaySession ? I18n.format("playtimetracker.menu.current") : I18n.format("playtimetracker.menu.all"), (p_213109_1_) -> {
			displaySession = !displaySession;
			this.toggleButton.setMessage(displaySession ? I18n.format("playtimetracker.menu.current") : I18n.format("playtimetracker.menu.all"));
		}));
		this.addButton(new Button(this.width / 2 + 2, this.height - 30, 150, 20, I18n.format("gui.done"), (p_213109_1_) -> {
			Minecraft.getInstance().displayGuiScreen(this.lastScreen);
		}));
		this.data.clear();
		this.keys.clear();
		data.putAll(playtimeTracker.getDatastore().getPlaytimeMap());

		this.sessionData.clear();
		this.sessionKeys.clear();
		sessionData.putAll(playtimeTracker.getDatastore().getSessionPlaytimeMap());

		if (playtimeTracker.getDatastore().isLoggedOn()) {
			String name = playtimeTracker.getDatastore().getCurrentServerName();
			long update = System.currentTimeMillis() - playtimeTracker.getDatastore().getLastUpdateTime();
			long newVal = update;
			if (data.containsKey(name))
				newVal += data.get(name);
			data.put(name, newVal);
			long newSessionVal = update;
			if (sessionData.containsKey(name))
				newSessionVal += sessionData.get(name);
			sessionData.put(name, newSessionVal);
		}

		data.entrySet().stream().sorted(reverseOrder(Map.Entry.comparingByValue())).forEach(entry -> keys.add(entry.getKey()));
		if (keys.size() > 0)
			maxVal = data.get(keys.get(0));
		for (String key : keys)
			values.add(playtimeTracker.getClockHud().currentTime(data.get(key)));
		total = playtimeTracker.getClockHud().currentTime(playtimeTracker.getDatastore().getTotalPlaytime());

		sessionData.entrySet().stream().sorted(reverseOrder(Map.Entry.comparingByValue())).forEach(entry -> sessionKeys.add(entry.getKey()));
		if (sessionKeys.size() > 0)
			sessionMaxVal = sessionData.get(sessionKeys.get(0));
		for (String key : sessionKeys)
			sessionValues.add(playtimeTracker.getClockHud().currentTime(sessionData.get(key)));
		sessionTotal = playtimeTracker.getClockHud().currentTime(playtimeTracker.getDatastore().getSessionTotalPlaytime());
		
		this.list = new DataGui.List(data, keys, values, maxVal);
		this.sessionList = new DataGui.List(sessionData, sessionKeys, sessionValues, sessionMaxVal);
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
		this.drawCenteredString(this.font, I18n.format("playtimetracker.playtime.total" + (displaySession ? ".session" : ""), displaySession ? sessionTotal : total), this.width / 2, 8, -1);
		super.render(mouseX, mouseY, partialTicks);
	}

	class List extends SlotGui {

		private static final int padding = 2;

		private final HashMap<String, Long> data;
		private ArrayList<String> keys;
		private ArrayList<String> values;
		private long maxVal;

		public List(HashMap<String, Long> data, ArrayList<String> keys, ArrayList<String> values, long maxVal) {
			super(DataGui.this.minecraft, DataGui.this.width, DataGui.this.height, 8 + DataGui.this.font.FONT_HEIGHT + 8, DataGui.this.height - 40, DataGui.this.font.FONT_HEIGHT + padding * 2);
			this.data = data;
			this.keys = keys;
			this.values = values;
			this.maxVal = maxVal;
		}

		protected int getItemCount() {
			return DataGui.this.data.size();
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
			if (slotIndex >= keys.size())
				return;
			String server = this.keys.get(slotIndex);
			String text = "";
			if (server.startsWith("mp."))
				text = server.substring(3, server.length()).replace(',', '.') + " (" + multiplayer + ")";
			else if (server.startsWith("sp."))
				text = server.substring(3, server.length()).replaceAll("%2E", ".") + " (" + singleplayer + ")";
			DataGui.this.font.drawString(text, 10 + padding, yPos + padding, server.equals(playtimeTracker.getDatastore().getCurrentServerName()) ? ConfigHandler.currentColor : -1);
			int maxWidth = (DataGui.this.width - 15) - 230;
			long time = this.data.get(server);
			double percentMax = (double) time / this.maxVal;
			int width = (int) Math.ceil(maxWidth * percentMax);
			// Bar
			Screen.fill(230, yPos, DataGui.this.width - 15, yPos + this.getItemHeight() - padding, ConfigHandler.barBackgroundColor); // Background
			Screen.fill(230, yPos, 230 + width, yPos + this.getItemHeight() - padding, ConfigHandler.barColor); // Progress
			// Borders
			Screen.fill(230, yPos, DataGui.this.width - 15, yPos + 1, ConfigHandler.borderColor); // Top
			Screen.fill(230, yPos + this.getItemHeight() - 1 - padding, DataGui.this.width - 15, yPos + this.getItemHeight() - padding, ConfigHandler.borderColor); // Bottom
			Screen.fill(230, yPos, 231, yPos + this.getItemHeight() - padding, ConfigHandler.borderColor); // Left
			Screen.fill(DataGui.this.width - 16, yPos, DataGui.this.width - 15, yPos + this.getItemHeight() - padding, ConfigHandler.borderColor); // Right
			DataGui.this.font.drawString(this.values.get(slotIndex), 230 + padding, yPos + padding, -1);
		}

		protected int getScrollBarX() {
			return this.width - 10;
		}
	}

}
