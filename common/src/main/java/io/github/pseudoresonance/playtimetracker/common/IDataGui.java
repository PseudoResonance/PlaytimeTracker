package io.github.pseudoresonance.playtimetracker.common;

import java.util.HashMap;

import io.github.pseudoresonance.playtimetracker.common.Datastore.DataHolder;

public interface IDataGui {
	
	public IGuiList initGuiList(HashMap<String, DataHolder> data, int width, int height, int top, int bottom, int slotHeight);
	
	public IButton addButton(int x, int y, int width, int height, String text, Runnable run);
	
	public void removeButton(IButton button);
	
	public int getWidth();

	public int getHeight();
	
	public void openLastScreen();

}
