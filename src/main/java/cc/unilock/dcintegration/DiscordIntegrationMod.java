package cc.unilock.dcintegration;

import cc.unilock.dcintegration.proxy.IProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

import java.util.ArrayList;
import java.util.UUID;

@Mod(
    modid = "dcintegration",
    version = Tags.VERSION,
    name = "DCIntegration",
    acceptedMinecraftVersions = "[1.7.10]",
    acceptableRemoteVersions = "*",
    dependencies = "after: ChromatiCraft"
)
public class DiscordIntegrationMod {
    public static final ArrayList<UUID> timeouts = new ArrayList<>();
    public static boolean stopped = false;

    @SidedProxy(clientSide = "cc.unilock.dcintegration.proxy.ClientProxy", serverSide = "cc.unilock.dcintegration.proxy.ServerProxy")
    public static IProxy proxy;

    @Mod.EventHandler
    public void modConstruction(FMLConstructionEvent event) {
        proxy.modConstruction(event);
    }

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
}
