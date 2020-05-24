package io.github.pseudoresonance.playtimetracker.common;

public interface IPlaytimeTracker {
	
	public Utils getUtils();
	
	public DataGuiRenderer getDataGuiRenderer();
	
	public Config getConfig();
	
	public ILanguage getLanguageManager();

	public IRenderer getRenderer();
	
	public IGame getGame();
	
	public Datastore getDatastore();

}
