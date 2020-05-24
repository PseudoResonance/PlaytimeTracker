package io.github.pseudoresonance.playtimetracker.forge;

import io.github.pseudoresonance.playtimetracker.common.IRenderer.IHitbox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClockHud {

	private TextHitbox hitbox = null;

	@SubscribeEvent
	public void onInitGuiEvent(final InitGuiEvent event) {
		final Screen gui = event.getGui();
		if (gui instanceof IngameMenuScreen || gui instanceof MainMenuScreen) {
			hitbox = new TextHitbox();
			hitbox.active = false;
			event.addWidget(hitbox);
		}
	}

	private class TextHitbox extends Widget implements IHitbox {
		
		public TextHitbox() {
			super(0, 0, 0, 0, "");
		}

		@SuppressWarnings("resource")
		public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
			if (this.active && this.visible) {
				if (this.isValidClickButton(p_mouseClicked_5_)) {
					boolean flag = this.clicked(p_mouseClicked_1_, p_mouseClicked_3_);
					if (flag) {
						this.playDownSound(Minecraft.getInstance().getSoundHandler());
						Minecraft.getInstance().displayGuiScreen(new DataGui(Minecraft.getInstance().currentScreen));
						return true;
					}
				}
				return false;
			} else {
				return false;
			}
		}

		protected boolean clicked(double x, double y) {
			return this.active && this.visible && x >= (double) this.x && y >= (double) this.y && x < (double) (this.x + this.width) && y < (double) (this.y + this.height);
		}

		public void setBounds(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			active = true;
		}

		public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
			PlaytimeTracker.getInstance().getUtils().renderClock(p_renderButton_1_, p_renderButton_2_, p_renderButton_3_, this);
		}

	}

}
