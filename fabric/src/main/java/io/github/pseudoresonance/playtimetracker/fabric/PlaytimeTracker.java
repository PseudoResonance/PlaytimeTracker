package io.github.pseudoresonance.playtimetracker.fabric;

import java.io.File;

import io.github.pseudoresonance.playtimetracker.common.*;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class PlaytimeTracker implements ClientModInitializer, IPlaytimeTracker {

    public static final String MODID = Constants.MODID;
    public static final String MODNAME = Constants.MODNAME;
    public static final String VERSION = Constants.VERSION;
    
    private static Config config;
    private static Datastore datastore;
    private static ClockHud clockHud;
    private static Utils utils;
    private static DataGuiRenderer dataGuiRenderer;
    private static IRenderer renderer;
    private static ILanguage languageManager;
    private static IGame game;
    
    private static PlaytimeTracker instance;
    
	@Override
	public void onInitializeClient() {
		instance = this;
		renderer = new Renderer();
		languageManager = new Language();
		game = new Game();
		config = new Config(new File(FabricLoader.getInstance().getConfigDirectory(), PlaytimeTracker.MODID + ".json"));
		datastore = new Datastore(this);
		clockHud = new ClockHud();
		utils = new Utils(this);
		dataGuiRenderer = new DataGuiRenderer(this);
	}
	
	public static PlaytimeTracker getInstance() {
		return instance;
	}
	
	public IRenderer getRenderer() {
		return renderer;
	}
	
	public ILanguage getLanguageManager() {
		return languageManager;
	}
	
	public IGame getGame() {
		return game;
	}
	
	public Utils getUtils() {
		return utils;
	}
	
	public DataGuiRenderer getDataGuiRenderer() {
		return dataGuiRenderer;
	}
	
	public Config getConfig() {
		return config;
	}
	
	public Datastore getDatastore() {
		return datastore;
	}
	
	public ClockHud getClockHud() {
		return clockHud;
	}
}
