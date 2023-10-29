package cc.unilock.dcintegration;

import cc.unilock.dcintegration.proxy.IProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]")
public class DiscordIntegrationMod {
    public static final ArrayList<UUID> timeouts = new ArrayList<>();
    public static boolean stopped = false;

    @SidedProxy(clientSide = "cc.unilock.dcintegration.proxy.ClientProxy", serverSide = "cc.unilock.dcintegration.proxy.ServerProxy")
    public static IProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        proxy.serverStarted(event);
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        proxy.serverStopping(event);
    }

    @NetworkCheckHandler
    public boolean checkModLists(Map<String,String> modList, Side side) {
        return true;
    }
}
