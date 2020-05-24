package io.github.pseudoresonance.playtimetracker.forge;

import java.io.File;

import io.github.pseudoresonance.playtimetracker.common.IGame;
import net.minecraft.client.Minecraft;

public class Game implements IGame {
	
	@Override
	public boolean isSingleplayer() {
		return Minecraft.getInstance().isSingleplayer();
	}

	@Override
	public String getServerName() {
		return Minecraft.getInstance().getConnection().getNetworkManager().getRemoteAddress().toString();
	}

	@Override
	public String getWorldName() {
		return Minecraft.getInstance().getIntegratedServer().getWorlds().iterator().next().getSaveHandler().getWorldDirectory().getName();
	}

	@SuppressWarnings("resource")
	@Override
	public File getGameDirectory() {
		return Minecraft.getInstance().gameDir;
	}

}
