package cc.unilock.dcintegration.compat;

import cc.unilock.dcintegration.compat.chromaticraft.CCEventListener;
import cpw.mods.fml.common.Loader;
import net.minecraftforge.common.MinecraftForge;

import static de.erdbeerbaerlp.dcintegration.common.DiscordIntegration.LOGGER;

public class ModCompat {
    public static void register() {
        if (Loader.isModLoaded("ChromatiCraft")) {
            LOGGER.info("ChromatiCraft detected - loading support");
            MinecraftForge.EVENT_BUS.register(new CCEventListener());
        }
    }
}
