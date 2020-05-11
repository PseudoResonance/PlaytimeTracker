package io.github.pseudoresonance.playtimetracker;

import net.fabricmc.api.ClientModInitializer;

public class PlaytimeTracker implements ClientModInitializer {

    public static final String MODID = "playtimetracker";
    public static final String MODNAME = "Playtime Tracker";
    public static final String VERSION = "1.2";
    
    private static Config config;
    private static Datastore datastore;
    private static ClockHud clockHud;
    
    private static PlaytimeTracker instance;
    
	@Override
	public void onInitializeClient() {
		instance = this;
		config = new Config();
		datastore = new Datastore();
		clockHud = new ClockHud();
	}
	
	public static PlaytimeTracker getInstance() {
		return instance;
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
