package cc.unilock.dcintegration.proxy;

import cpw.mods.fml.common.event.*;

public class ClientProxy implements IProxy {
    @Override
    public void modConstruction(FMLConstructionEvent event) {
        throw new RuntimeException("DCIntegration cannot run client-side");
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        throw new RuntimeException("DCIntegration cannot run client-side");
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        throw new RuntimeException("DCIntegration cannot run client-side");
    }

    @Override
    public void serverStarted(FMLServerStartedEvent event) {
        throw new RuntimeException("DCIntegration cannot run client-side");
    }

    @Override
    public void serverStopping(FMLServerStoppingEvent event) {
        throw new RuntimeException("DCIntegration cannot run client-side");
    }
}
