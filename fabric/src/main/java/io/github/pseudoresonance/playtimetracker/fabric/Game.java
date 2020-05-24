package io.github.pseudoresonance.playtimetracker.fabric;

import java.io.File;

import io.github.pseudoresonance.playtimetracker.common.IGame;
import net.minecraft.client.MinecraftClient;

public class Game implements IGame {
	
	@Override
	public boolean isSingleplayer() {
		return MinecraftClient.getInstance().isInSingleplayer();
	}

	@Override
	public String getServerName() {
		return MinecraftClient.getInstance().getNetworkHandler().getConnection().getAddress().toString();
	}

	@Override
	public String getWorldName() {
		return MinecraftClient.getInstance().getServer().getWorlds().iterator().next().getSaveHandler().getWorldDir().getName();
	}

	@SuppressWarnings("resource")
	@Override
	public File getGameDirectory() {
		return MinecraftClient.getInstance().runDirectory;
	}

}
