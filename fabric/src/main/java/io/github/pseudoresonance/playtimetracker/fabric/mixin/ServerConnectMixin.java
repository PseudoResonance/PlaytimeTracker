package io.github.pseudoresonance.playtimetracker.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.pseudoresonance.playtimetracker.fabric.PlaytimeTracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;

@Mixin(ClientPlayNetworkHandler.class)
public class ServerConnectMixin {
	
	@Inject(method="onGameJoin", at=@At("RETURN"))
	private void onConnect(CallbackInfo ci) {
		PlaytimeTracker.getInstance().getUtils().logOn();
	}

}
