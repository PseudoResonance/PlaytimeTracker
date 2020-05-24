package io.github.pseudoresonance.playtimetracker.fabric;

import io.github.pseudoresonance.playtimetracker.common.IButton;
import net.minecraft.client.gui.widget.ButtonWidget;

public class Button extends ButtonWidget implements IButton {

	public Button(int x, int y, int width, int height, String message, PressAction onPress) {
		super(x, y, width, height, message, onPress);
	}

	@Override
	public String getText() {
		return this.getMessage();
	}

	@Override
	public void setText(String text) {
		this.setMessage(text);
	}

	@Override
	public boolean isPressed() {
		return this.isPressed();
	}

	@Override
	public boolean isHovered() {
		return this.isHovered();
	}

}
