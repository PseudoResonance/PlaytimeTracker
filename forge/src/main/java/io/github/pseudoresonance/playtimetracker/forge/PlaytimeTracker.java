package io.github.pseudoresonance.playtimetracker.forge;

import java.io.File;

import io.github.pseudoresonance.playtimetracker.common.*;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("playtimetracker")
public class PlaytimeTracker implements IPlaytimeTracker {

    public static final String MODID = Constants.MODID;
    public static final String MODNAME = Constants.MODNAME;
    public static final String VERSION = Constants.VERSION;
	
	private static final Logger logger = LogManager.getLogger();

    private static Config config;
	private static Datastore datastore = null;
	private static ClockHud clockHud = null;
    private static Utils utils;
    private static DataGuiRenderer dataGuiRenderer;
    private static IRenderer renderer;
    private static ILanguage languageManager;
    private static IGame game;
    
    private static PlaytimeTracker instance;
	
	public PlaytimeTracker() {
		instance = this;
		renderer = new Renderer();
		languageManager = new Language();
		game = new Game();
		clockHud = new ClockHud();
		utils = new Utils(this);
		dataGuiRenderer = new DataGuiRenderer(this);
		config = new Config(new File("./config", PlaytimeTracker.MODID + ".json"));
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(clockHud);
		ConfigGui.register();
	}

	private void setup(final FMLCommonSetupEvent event) {
		datastore = new Datastore(this);
		if (config.multiInstance)
			datastore.startClock();
	}

	@SubscribeEvent
	public void disabled(FMLServerStoppingEvent event) {
		datastore.logOff();
	}

	@SubscribeEvent
	public void connected(ClientPlayerNetworkEvent.LoggedInEvent event) {
		utils.logOn();
	}

	@SubscribeEvent
	public void disconnected(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		datastore.logOff();
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
	
	public Logger getLogger() {
		return logger;
	}
}