package io.github.pseudoresonance.playtimetracker;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("playtimetracker")
public class PlaytimeTracker {
	
	public static final String MODID = "playtimetracker";
	public static final String NAME = "Playtime Tracker";
	public static final String VERSION = "1.2";
	
	private static final Logger logger = LogManager.getLogger();

	private static Datastore datastore = null;
	private static ClockHud clockHud = null;
	
	private String currentServerIp = "";
	
	public PlaytimeTracker() {
		clockHud = new ClockHud(this);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(clockHud);
		ConfigHandler.setup(this);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
	}

	private void setup(final FMLCommonSetupEvent event) {
		datastore = new Datastore(this);
		if (ConfigHandler.multiInstance)
			datastore.startClock();
	}

	@SubscribeEvent
	public void disabled(FMLServerStoppingEvent event) {
		datastore.logOff();
	}

	@SubscribeEvent
	public void connected(ClientPlayerNetworkEvent.LoggedInEvent event) {
		currentServerIp = "mp." + event.getNetworkManager().getRemoteAddress().toString().toLowerCase().split("/")[0].trim().replaceAll("^\\.+", "").replaceAll("\\.+$", "");
		if (Minecraft.getInstance().isSingleplayer())
			currentServerIp = "sp." + Minecraft.getInstance().getIntegratedServer().getWorlds().iterator().next().getSaveHandler().getWorldDirectory().getName().trim();
		datastore.logOn(currentServerIp);
	}

	@SubscribeEvent
	public void disconnected(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		datastore.logOff();
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