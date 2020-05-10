package io.github.pseudoresonance.playtimetracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.reverseOrder;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiSnooper;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DataGui extends GuiScreen {

	private PlaytimeTracker playtimeTracker;
	private final GuiScreen lastScreen;

	private String multiplayer = "";
	private String singleplayer = "";
	private static boolean displaySession = false;
	private GuiButton toggleButton = null;

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

	DataGui(PlaytimeTracker playtimeTracker, GuiScreen lastScreen) {
		this.playtimeTracker = playtimeTracker;
		this.lastScreen = lastScreen;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when
	 * the GUI is displayed and when the window resizes, the buttonList is cleared
	 * beforehand.
	 */
	@Override
	public void initGui() {
		multiplayer = I18n.format("menu.multiplayer");
		singleplayer = I18n.format("menu.singleplayer");
		this.toggleButton = new GuiButton(1, this.width / 2 - 152, this.height - 30, 150, 20, displaySession ? I18n.format("playtimetracker.menu.current") : I18n.format("playtimetracker.menu.all"));
		this.buttonList.add(this.toggleButton);
		this.buttonList.add(new GuiButton(2, this.width / 2 + 2, this.height - 30, 150, 20, I18n.format("gui.done")));
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
	 * Handles mouse input.
	 */
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.list.handleMouseInput();
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for
	 * buttons)
	 */
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			if (button.id == 2) {
				this.mc.displayGuiScreen(this.lastScreen);
			} else if (button.id == 1) {
				displaySession = !displaySession;
				this.toggleButton.displayString = displaySession ? I18n.format("playtimetracker.menu.current") : I18n.format("playtimetracker.menu.all");
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		if (displaySession)
			this.sessionList.drawScreen(mouseX, mouseY, partialTicks);
		else
			this.list.drawScreen(mouseX, mouseY, partialTicks);
		this.drawCenteredString(this.fontRendererObj, I18n.format("playtimetracker.playtime.total" + (displaySession ? ".session" : ""), displaySession ? sessionTotal : total), this.width / 2, 8, -1);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@SideOnly(Side.CLIENT)
	class List extends GuiSlot {

		private static final int padding = 2;

		private final HashMap<String, Long> data;
		private ArrayList<String> keys;
		private ArrayList<String> values;
		private long maxVal;

		public List(HashMap<String, Long> data, ArrayList<String> keys, ArrayList<String> values, long maxVal) {
			super(DataGui.this.mc, DataGui.this.width, DataGui.this.height, 8 + DataGui.this.mc.fontRendererObj.FONT_HEIGHT + 8, DataGui.this.height - 40, DataGui.this.fontRendererObj.FONT_HEIGHT + padding * 2);
			this.data = data;
			this.keys = keys;
			this.values = values;
			this.maxVal = maxVal;
		}

		protected int getSize() {
			return DataGui.this.data.size();
		}

		/**
		 * The element in the slot that was clicked, boolean for whether it was double
		 * clicked or not
		 */
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
		}

		/**
		 * Returns true if the element passed in is currently selected
		 */
		protected boolean isSelected(int slotIndex) {
			return false;
		}

		protected void drawBackground() {
		}

		protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn) {
			if (slotIndex >= keys.size())
				return;
			String server = this.keys.get(slotIndex);
			String text = "";
			if (server.startsWith("mp."))
				text = server.substring(3, server.length()).replace(',', '.') + " (" + multiplayer + ")";
			else if (server.startsWith("sp."))
				text = server.substring(3, server.length()).replaceAll("%2E", ".") + " (" + singleplayer + ")";
			DataGui.this.fontRendererObj.drawString(text, 10 + padding, yPos + padding, server.equals(playtimeTracker.getDatastore().getCurrentServerName()) ? playtimeTracker.getConfig().currentColor : -1);
			int maxWidth = (DataGui.this.width - 15) - 230;
			long time = this.data.get(server);
			double percentMax = (double) time / this.maxVal;
			int width = (int) Math.ceil(maxWidth * percentMax);
			// Bar
			Minecraft.getMinecraft().currentScreen.drawRect(230, yPos, DataGui.this.width - 15, yPos + this.slotHeight - padding, playtimeTracker.getConfig().barBackgroundColor); // Background
			Minecraft.getMinecraft().currentScreen.drawRect(230, yPos, 230 + width, yPos + this.slotHeight - padding, playtimeTracker.getConfig().barColor); // Progress
			// Borders
			Minecraft.getMinecraft().currentScreen.drawRect(230, yPos, DataGui.this.width - 15, yPos + 1, playtimeTracker.getConfig().borderColor); // Top
			Minecraft.getMinecraft().currentScreen.drawRect(230, yPos + this.slotHeight - 1 - padding, DataGui.this.width - 15, yPos + this.slotHeight - padding, playtimeTracker.getConfig().borderColor); // Bottom
			Minecraft.getMinecraft().currentScreen.drawRect(230, yPos, 231, yPos + this.slotHeight - padding, playtimeTracker.getConfig().borderColor); // Left
			Minecraft.getMinecraft().currentScreen.drawRect(DataGui.this.width - 16, yPos, DataGui.this.width - 15, yPos + this.slotHeight - padding, playtimeTracker.getConfig().borderColor); // Right
			DataGui.this.fontRendererObj.drawString(this.values.get(slotIndex), 230 + padding, yPos + padding, -1);
		}

		protected int getScrollBarX() {
			return this.width - 10;
		}
	}

}
