package cc.unilock.dcintegration.proxy;

import cpw.mods.fml.common.event.*;

public interface IProxy {
    void modConstruction(FMLConstructionEvent event);

    void preInit(FMLPreInitializationEvent event);

    void serverStarting(FMLServerStartingEvent event);

    void serverStarted(FMLServerStartedEvent event);

    void serverStopping(FMLServerStoppingEvent event);
}
