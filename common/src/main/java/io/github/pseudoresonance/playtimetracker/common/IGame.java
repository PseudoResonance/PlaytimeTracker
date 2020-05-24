package io.github.pseudoresonance.playtimetracker.common;

import java.io.File;

public interface IGame {
	
	public boolean isSingleplayer();
	
	public String getServerName();
	
	public String getWorldName();
	
	public File getGameDirectory();

}
