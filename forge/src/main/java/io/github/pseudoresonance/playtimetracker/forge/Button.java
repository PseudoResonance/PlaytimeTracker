package io.github.pseudoresonance.playtimetracker.forge;

import io.github.pseudoresonance.playtimetracker.common.IButton;

public class Button extends net.minecraft.client.gui.widget.button.Button implements IButton {

	public Button(int p_i51141_1_, int p_i51141_2_, int p_i51141_3_, int p_i51141_4_, String p_i51141_5_, IPressable p_i51141_6_) {
		super(p_i51141_1_, p_i51141_2_, p_i51141_3_, p_i51141_4_, p_i51141_5_, p_i51141_6_);
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
