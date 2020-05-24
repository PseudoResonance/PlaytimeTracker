package io.github.pseudoresonance.playtimetracker.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.pseudoresonance.playtimetracker.fabric.PlaytimeTracker;
import net.minecraft.client.gui.screen.Screen;

@Mixin(Screen.class)
public class ScreenInitMixin {
	
	@Inject(method="init(Lnet/minecraft/client/MinecraftClient;II)V", at=@At("RETURN"))
	private void onInitScreen(CallbackInfo ci) {
		PlaytimeTracker.getInstance().getClockHud().onInitScreen((Screen) (Object) this);
	}

}
