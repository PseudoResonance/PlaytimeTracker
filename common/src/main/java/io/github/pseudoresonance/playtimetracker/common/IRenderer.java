package io.github.pseudoresonance.playtimetracker.common;

import java.util.List;

public interface IRenderer {
	
	public int getStringWidth(String text);
	
	public int getStringHeight();
	
	public int drawStringShadow(String text, float x, float y, int color);
	
	public int drawString(String text, float x, float y, int color);
	
	public void drawTooltip(String text, int x, int y);
	
	public void drawTooltip(List<String> text, int x, int y);
	
	public boolean isMainMenu();
	
	public boolean isIngameMenu();
	
	public int getStringX(int width, AlignMode alignMode, int xOffset);
	
	public int getStringY(int height, AlignMode alignMode, int yOffset);
	
	public void drawRect(int x1, int y1, int x2, int y2, int color);
	
	public enum AlignMode {
		TOPLEFT, TOPCENTER, TOPRIGHT, BOTTOMLEFT, BOTTOMCENTER, BOTTOMRIGHT;
	}
	
	public interface IHitbox {
		
		public void setBounds(int x, int y, int width, int height);
		
	}

}
