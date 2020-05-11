package io.github.pseudoresonance.playtimetracker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.pseudoresonance.playtimetracker.PlaytimeTracker;
import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class ServerDisconnectMixin {
	
	@Inject(method="disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at=@At("HEAD"))
	private void onDisconnect(CallbackInfo ci) {
		System.out.println("MIXIN DISCONNECT");
		PlaytimeTracker.getInstance().getDatastore().logOff();
	}

}
