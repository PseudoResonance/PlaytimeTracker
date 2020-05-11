package io.github.pseudoresonance.playtimetracker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.pseudoresonance.playtimetracker.PlaytimeTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

@Mixin(ClientPlayNetworkHandler.class)
public class ServerConnectMixin {
	
	@Inject(method="onGameJoin", at=@At("RETURN"))
	private void onConnect(CallbackInfo ci) {
		MinecraftClient mc = MinecraftClient.getInstance();
		String server = "";
		if (mc.isInSingleplayer())
			server = "sp." + mc.getServer().getWorlds().iterator().next().getSaveHandler().getWorldDir().getName().trim();
		else
			server = "mp." + mc.getNetworkHandler().getConnection().getAddress().toString().toLowerCase().split("/")[0].trim().replaceAll("^\\.+", "").replaceAll("\\.+$", "");
		PlaytimeTracker.getInstance().getDatastore().logOn(server);
	}

}
