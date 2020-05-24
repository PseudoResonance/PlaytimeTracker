package io.github.pseudoresonance.playtimetracker.common;

public interface IButton {
	
	public String getText();
	
	public void setText(String text);
	
	public boolean isPressed();
	
	public boolean isHovered();

}
