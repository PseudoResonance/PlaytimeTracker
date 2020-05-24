package io.github.pseudoresonance.playtimetracker.fabric;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.github.pseudoresonance.playtimetracker.common.IRenderer.IHitbox;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;

public class ClockHud {

	private static Method addButtonMethod = null;

	private TextHitbox hitbox = null;

	public void onInitScreen(Screen screen) {
		try {
			if (addButtonMethod == null) {
				addButtonMethod = Screen.class.getDeclaredMethod("addButton", AbstractButtonWidget.class);
				addButtonMethod.setAccessible(true);
			}
			if (screen instanceof GameMenuScreen || screen instanceof TitleScreen) {
				hitbox = new TextHitbox();
				hitbox.active = false;
				addButtonMethod.invoke(screen, hitbox);
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			System.err.println("Unable to inject playtime display!");
			e.printStackTrace();
		}
	}

	private class TextHitbox extends AbstractPressableButtonWidget implements IHitbox {

		public TextHitbox() {
			super(0, 0, 0, 0, "");
		}

		protected boolean clicked(double x, double y) {
			return this.active && this.visible && x >= (double) this.x && y >= (double) this.y && x < (double) (this.x + this.width) && y < (double) (this.y + this.height);
		}

		@SuppressWarnings("resource")
		@Override
		public void onPress() {
			MinecraftClient.getInstance().openScreen(new DataGui(MinecraftClient.getInstance().currentScreen));
			active = true;
		}

		public void setBounds(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			active = true;
		}

		public void render(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
			PlaytimeTracker.getInstance().getUtils().renderClock(p_renderButton_1_, p_renderButton_2_, p_renderButton_3_, this);
		}

	}

}
