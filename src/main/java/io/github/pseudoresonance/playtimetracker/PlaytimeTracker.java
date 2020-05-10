package io.github.pseudoresonance.playtimetracker;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

import org.apache.logging.log4j.Logger;

@Mod(modid = PlaytimeTracker.MODID, name = PlaytimeTracker.NAME, version = PlaytimeTracker.VERSION, clientSideOnly = true, acceptedMinecraftVersions = "[1.8,1.8.9]", canBeDeactivated = false, guiFactory = "io.github.pseudoresonance.playtimetracker.ConfigGuiFactory")
public class PlaytimeTracker {
	public static final String MODID = "playtimetracker";
	public static final String NAME = "PlaytimeTracker";
	public static final String VERSION = "1.1";

	private static Logger logger;

	private static ConfigHandler config = null;
	private static Datastore datastore = null;
	private static ClockHud clockHud = null;
	
	private String currentServerIp = "";

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		ConfigGui.setup(this);
		config = new ConfigHandler(this, event.getSuggestedConfigurationFile());
		datastore = new Datastore(this);
		clockHud = new ClockHud(this);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(config);
		MinecraftForge.EVENT_BUS.register(clockHud);
		if (config.multiInstance)
			datastore.startClock();
	}

	@EventHandler
	public void disabled(FMLModDisabledEvent event) {
		datastore.logOff();
	}

	@SubscribeEvent
	public void connected(ClientConnectedToServerEvent event) {
		currentServerIp = "mp." + event.manager.getRemoteAddress().toString().toLowerCase().split("/")[0].replace('.', ',');
		if (event.isLocal)
			currentServerIp = "sp." + Minecraft.getMinecraft().getIntegratedServer().worldServers[0].getSaveHandler().getWorldDirectory().getName().replaceAll("\\.", "%2E");
		datastore.logOn(currentServerIp);
	}

	@SubscribeEvent
	public void disconnected(ClientDisconnectionFromServerEvent event) {
		datastore.logOff();
	}
	
	public ConfigHandler getConfig() {
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