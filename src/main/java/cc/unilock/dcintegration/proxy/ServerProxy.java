package cc.unilock.dcintegration.proxy;

import cc.unilock.dcintegration.DiscordIntegrationMod;
import cc.unilock.dcintegration.FMLEventListener;
import cc.unilock.dcintegration.MFEventListener;
import cc.unilock.dcintegration.compat.ModCompat;
import cc.unilock.dcintegration.util.ForgeServerInterface;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.storage.CommandRegistry;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.util.DiscordMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;

import static de.erdbeerbaerlp.dcintegration.common.DiscordIntegration.*;

public class ServerProxy implements IProxy {
    @Override
    public void modConstruction(FMLConstructionEvent event) {
        LOGGER.info("Version is " + VERSION);

        try {
            if (!discordDataDir.exists()) discordDataDir.mkdir();
            DiscordIntegration.loadConfigs();
            if (Configuration.instance().general.botToken.equals("INSERT BOT TOKEN HERE")) { // Prevent events when token not set
                LOGGER.error("Please check the config file and set an bot token");
            } else {
                FMLCommonHandler.instance().bus().register(new FMLEventListener());
                MinecraftForge.EVENT_BUS.register(new MFEventListener());
                ModCompat.register();
            }
        } catch (IOException e) {
            LOGGER.error("Config loading failed");
            if (!discordDataDir.exists())
                LOGGER.error("Please create the folder " + discordDataDir.getAbsolutePath() + " manually");
            LOGGER.error(e.getMessage());
            LOGGER.error(e.getCause());
        } catch (IllegalStateException e) {
            LOGGER.error("Failed to read config file! Please check your config file!\nError description: " + e.getMessage());
            LOGGER.error("\nStacktrace: ");
            e.printStackTrace();
        }
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        INSTANCE = new DiscordIntegration(new ForgeServerInterface());
        try {
            //Wait a short time to allow JDA to get initialized
            LOGGER.info("Waiting for JDA to initialize to send starting message... (max 5 seconds before skipping)");
            for (int i = 0; i <= 5; i++) {
                if (INSTANCE.getJDA() == null) Thread.sleep(1000);
                else break;
            }
            if (INSTANCE.getJDA() != null) {
                Thread.sleep(2000); //Wait for it to cache the channels
                CommandRegistry.registerDefaultCommands();
                if (!Localization.instance().serverStarting.trim().isEmpty())
                    if (INSTANCE.getChannel() != null) {
                        final MessageCreateData m;
                        if (Configuration.instance().embedMode.enabled && Configuration.instance().embedMode.startMessages.asEmbed)
                            m = new MessageCreateBuilder().setEmbeds(Configuration.instance().embedMode.startMessages.toEmbed().setDescription(Localization.instance().serverStarting).build()).build();
                        else
                            m = new MessageCreateBuilder().addContent(Localization.instance().serverStarting).build();
                        DiscordIntegration.startingMsg = INSTANCE.sendMessageReturns(m, INSTANCE.getChannel(Configuration.instance().advanced.serverChannelID));
                    }
            }
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        // no-op?
    }

    @Override
    public void serverStarted(FMLServerStartedEvent event) {
        LOGGER.info("Started");
        //started = new Date().getTime();
        if (INSTANCE != null) {
            if (DiscordIntegration.startingMsg != null) {
                if (Configuration.instance().embedMode.enabled && Configuration.instance().embedMode.startMessages.asEmbed) {
                    if (!Configuration.instance().embedMode.startMessages.customJSON.trim().isEmpty()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.startMessages.toEmbedJson(Configuration.instance().embedMode.startMessages.customJSON);
                        DiscordIntegration.startingMsg.thenAccept((a) -> a.editMessageEmbeds(b.build()).queue());
                    } else
                        DiscordIntegration.startingMsg.thenAccept((a) -> a.editMessageEmbeds(Configuration.instance().embedMode.startMessages.toEmbed().setDescription(Localization.instance().serverStarted).build()).queue());
                } else
                    DiscordIntegration.startingMsg.thenAccept((a) -> a.editMessage(Localization.instance().serverStarted).queue());
            } else {
                if (Configuration.instance().embedMode.enabled && Configuration.instance().embedMode.startMessages.asEmbed) {
                    if (!Configuration.instance().embedMode.startMessages.customJSON.trim().isEmpty()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.startMessages.toEmbedJson(Configuration.instance().embedMode.startMessages.customJSON);
                        INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else
                        INSTANCE.sendMessage(new DiscordMessage(Configuration.instance().embedMode.startMessages.toEmbed().setDescription(Localization.instance().serverStarted).build()));
                } else
                    INSTANCE.sendMessage(Localization.instance().serverStarted);
            }
            INSTANCE.startThreads();
        }
        //UpdateChecker.runUpdateCheck("https://raw.githubusercontent.com/ErdbeerbaerLP/Discord-Chat-Integration/1.20.1/update_checker.json");
        //if (Objects.nonNull(Loader.instance().getIndexedModList().get("dynmap"))) {
        //    new DynmapListener().register();
        //}


        /*
        if (!DownloadSourceChecker.checkDownloadSource(new File(DiscordIntegrationMod.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("%")[0]))) {
            LOGGER.warn("You likely got this mod from a third party website.");
            LOGGER.warn("Some of such websites are distributing malware or old versions.");
            LOGGER.warn("Download this mod from an official source (https://www.curseforge.com/minecraft/mc-mods/dcintegration) to hide this message");
            LOGGER.warn("This warning can also be suppressed in the config file");
        }
         */
    }

    @Override
    public void serverStopping(FMLServerStoppingEvent event) {
        if (INSTANCE != null) {
            INSTANCE.stopThreads();
            if (!FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning() && !Localization.instance().serverStopped.trim().isEmpty())
                if (Configuration.instance().embedMode.enabled && Configuration.instance().embedMode.stopMessages.asEmbed) {
                    if (!Configuration.instance().embedMode.stopMessages.customJSON.trim().isEmpty()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.stopMessages.toEmbedJson(Configuration.instance().embedMode.stopMessages.customJSON);
                        INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else
                        INSTANCE.sendMessage(new DiscordMessage(Configuration.instance().embedMode.stopMessages.toEmbed().setDescription(Localization.instance().serverStopped).build()));
                } else
                    INSTANCE.sendMessage(Localization.instance().serverStopped);
            else if (FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning() && !Localization.instance().serverCrash.trim().isEmpty()) {
                if (Configuration.instance().embedMode.enabled && Configuration.instance().embedMode.stopMessages.asEmbed) {
                    if (!Configuration.instance().embedMode.stopMessages.customJSON.trim().isEmpty()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.stopMessages.toEmbedJson(Configuration.instance().embedMode.stopMessages.customJSON);
                        INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else
                        INSTANCE.sendMessage(new DiscordMessage(Configuration.instance().embedMode.stopMessages.toEmbed().setDescription(Localization.instance().serverStopped).build()));
                } else
                    INSTANCE.sendMessage(Localization.instance().serverCrash);
            }
            INSTANCE.kill(false);
            INSTANCE = null;
            DiscordIntegrationMod.stopped = true;
            LOGGER.info("Shut-down successfully!");
        }
    }
}
