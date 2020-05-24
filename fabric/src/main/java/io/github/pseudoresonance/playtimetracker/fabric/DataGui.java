package io.github.pseudoresonance.playtimetracker.fabric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.github.pseudoresonance.playtimetracker.common.Datastore;
import io.github.pseudoresonance.playtimetracker.common.IButton;
import io.github.pseudoresonance.playtimetracker.common.IDataGui;
import io.github.pseudoresonance.playtimetracker.common.IGuiList;
import io.github.pseudoresonance.playtimetracker.common.Datastore.DataHolder;
import io.github.pseudoresonance.playtimetracker.common.Datastore.ListItem;

import static java.util.Collections.reverseOrder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.TranslatableText;

public class DataGui extends Screen implements IDataGui {

	private final Screen lastScreen;

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
		PlaytimeTracker.getInstance().getDataGuiRenderer().setupListRenderer(this);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		this.renderBackground();
		PlaytimeTracker.getInstance().getDataGuiRenderer().renderListPage(this.width, this.height, mouseX, mouseY, partialTicks);
		super.render(mouseX, mouseY, partialTicks);
	}

	class List extends EntryListWidget<DataGui.List.Entry> implements IGuiList {

		private ArrayList<String> keys = new ArrayList<String>();

		public List(HashMap<String, DataHolder> data, int width, int height, int top, int bottom, int slotHeight) {
			super(DataGui.this.minecraft, width, height, top, bottom, slotHeight);
			data.entrySet().stream().sorted(reverseOrder(Map.Entry.comparingByValue())).forEach(entry -> keys.add(entry.getKey()));
			long maxVal = 0;
			if (keys.size() > 0)
				maxVal = data.get(keys.get(0)).time;

			for (String server : keys) {
				DataHolder dh = data.get(server);
				Entry entry = new Entry(new Datastore.ListItem(dh.name, dh.nickname, PlaytimeTracker.getInstance().getUtils().currentTime(dh.time), dh.singleplayer, dh.gameDirectory, dh.directoryUuid == null ? true : dh.directoryUuid.equals(PlaytimeTracker.getInstance().getDatastore().getGameDirectoryUuid()), PlaytimeTracker.getInstance().getDatastore().getCurrentServerName().equals(server), dh.time, maxVal), this.itemHeight);
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

			private final ListItem listItem;
			private final int itemHeight;

			private Entry(ListItem listItem, int itemHeight) {
				this.listItem = listItem;
				this.itemHeight = itemHeight;
			}

			public void render(int slotIndex, int yPos, int xPos, int widthIn, int heightIn, int mouseXIn, int mouseYIn, boolean hover, float partialTicks) {
				if (slotIndex >= keys.size())
					return;
				PlaytimeTracker.getInstance().getDataGuiRenderer().renderListItem(listItem, xPos, yPos, DataGui.this.width, this.itemHeight, mouseXIn, mouseYIn);
			}
		}
	}
	
	@Override
	public IGuiList initGuiList(HashMap<String, DataHolder> data, int width, int height, int top, int bottom, int slotHeight) {
		return new DataGui.List(data, width, height, top, bottom, slotHeight);
	}

	@Override
	public IButton addButton(int x, int y, int width, int height, String text, Runnable run) {
		return this.addButton(new Button(x, y, width, height, text, var -> {run.run();}));
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void removeButton(IButton button) {
		this.buttons.remove(button);
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}
	
	@Override
	public void openLastScreen() {
		if (this.lastScreen != null)
			MinecraftClient.getInstance().openScreen(this.lastScreen);
	}

}
