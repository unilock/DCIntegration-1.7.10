package cc.unilock.dcintegration;

import cc.unilock.dcintegration.api.ForgeDiscordEventHandler;
import cc.unilock.dcintegration.util.ForgeMessageUtils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dcshadow.dev.vankka.mcdiscordreserializer.discord.DiscordSerializer;
import dcshadow.net.kyori.adventure.text.Component;
import dcshadow.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.LinkManager;
import de.erdbeerbaerlp.dcintegration.common.util.DiscordMessage;
import de.erdbeerbaerlp.dcintegration.common.util.MessageUtils;
import de.erdbeerbaerlp.dcintegration.common.util.TextColors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AchievementEvent;
import org.apache.commons.lang3.ArrayUtils;

import java.util.UUID;

import static de.erdbeerbaerlp.dcintegration.common.DiscordIntegration.INSTANCE;

public class MFEventListener {
    @SubscribeEvent
    public void achievement(AchievementEvent ev) {
        Achievement achievement = ev.achievement;
        EntityPlayerMP player = (EntityPlayerMP) ev.entityPlayer;

        if (Localization.instance().advancementMessage.trim().isEmpty()) return;
        if (LinkManager.isPlayerLinked(player.getUniqueID()) && LinkManager.getLink(null, player.getUniqueID()).settings.hideFromDiscord)
            return;
        if (player.func_147099_x().canUnlockAchievement(achievement) && !player.func_147099_x().hasAchievementUnlocked(achievement))
            if (INSTANCE != null && player.mcServer.func_147136_ar())
                if (!Localization.instance().advancementMessage.trim().isEmpty()) {
                    if (Configuration.instance().embedMode.enabled && Configuration.instance().embedMode.advancementMessage.asEmbed) {
                        final String avatarURL = Configuration.instance().webhook.playerAvatarURL.replace("%uuid%", player.getUniqueID().toString()).replace("%uuid_dashless%", player.getUniqueID().toString().replace("-", "")).replace("%name%", player.getCommandSenderName()).replace("%randomUUID%", UUID.randomUUID().toString());
                        if (!Configuration.instance().embedMode.advancementMessage.customJSON.trim().isEmpty()) {
                            final EmbedBuilder b = Configuration.instance().embedMode.advancementMessage.toEmbedJson(Configuration.instance().embedMode.advancementMessage.customJSON
                                .replace("%uuid%", player.getUniqueID().toString())
                                .replace("%uuid_dashless%", player.getUniqueID().toString().replace("-", ""))
                                .replace("%name%", ForgeMessageUtils.formatPlayerName(player))
                                .replace("%randomUUID%", UUID.randomUUID().toString())
                                .replace("%avatarURL%", avatarURL)
                                .replace("%advName%", ForgeMessageUtils.getTextWithoutFormattingCodes(StatCollector.translateToLocal(achievement.statId)))
                                .replace("%advDesc%", ForgeMessageUtils.getTextWithoutFormattingCodes(achievement.statId.equals("achievement.openInventory") ? StatCollector.translateToLocal(achievement.statId + ".desc").replace("%1$s", "E") : StatCollector.translateToLocal(achievement.statId + ".desc")))
                                .replace("%avatarURL%", avatarURL)
                                .replace("%playerColor%", "" + TextColors.generateFromUUID(player.getUniqueID()).getRGB())
                            );
                            INSTANCE.sendMessage(new DiscordMessage(b.build()));
                        } else {
                            EmbedBuilder b = Configuration.instance().embedMode.advancementMessage.toEmbed();
                            b = b.setAuthor(ForgeMessageUtils.formatPlayerName(ev.entity), null, avatarURL)
                                .setDescription(Localization.instance().advancementMessage.replace("%player%", ForgeMessageUtils.formatPlayerName(ev.entity))
                                    .replace("%advName%", ForgeMessageUtils.getTextWithoutFormattingCodes(StatCollector.translateToLocal(achievement.statId)))
                                    .replace("%advDesc%", ForgeMessageUtils.getTextWithoutFormattingCodes(achievement.statId.equals("achievement.openInventory") ? StatCollector.translateToLocal(achievement.statId + ".desc").replace("%1$s", "E") : StatCollector.translateToLocal(achievement.statId + ".desc")))
                                    .replace("\\n", "\n"));
                            INSTANCE.sendMessage(new DiscordMessage(b.build()));
                        }
                    } else INSTANCE.sendMessage(Localization.instance().advancementMessage.replace("%player%", ForgeMessageUtils.getTextWithoutFormattingCodes(ForgeMessageUtils.formatPlayerName(player)))
                        .replace("%advName%", ForgeMessageUtils.getTextWithoutFormattingCodes(StatCollector.translateToLocal(achievement.statId)))
                        .replace("%advDesc%", ForgeMessageUtils.getTextWithoutFormattingCodes(achievement.statId.equals("achievement.openInventory") ? StatCollector.translateToLocal(achievement.statId + ".desc").replace("%1$s", "E") : StatCollector.translateToLocal(achievement.statId + ".desc")))
                        .replace("\\n", "\n"));
                }
    }

    //@SubscribeEvent
    //public void registerCommands(... ev) {
    //
    //}

    @SubscribeEvent
    public void command(CommandEvent ev) {
        StringBuilder sb = new StringBuilder(ev.command.getCommandName() + " ");
        for (String s : ev.parameters) {
            sb.append(s).append(" ");
        }
        String command = sb.toString();
        if (!Configuration.instance().commandLog.channelID.equals("0")) {
            if (!ArrayUtils.contains(Configuration.instance().commandLog.ignoredCommands, command.split(" ")[0]))
                INSTANCE.sendMessage(Configuration.instance().commandLog.message
                    .replace("%sender%", ev.sender.getCommandSenderName())
                    .replace("%cmd%", command)
                    .replace("%cmd-no-args%", command.split(" ")[0]), INSTANCE.getChannel(Configuration.instance().commandLog.channelID));
        }
        if (INSTANCE != null) {
            final Entity sourceEntity = ev.sender instanceof Entity ? (Entity) ev.sender : null;
            boolean raw = false;

            if (((command.startsWith("say")) && Configuration.instance().messages.sendOnSayCommand) || (command.startsWith("me") && Configuration.instance().messages.sendOnMeCommand)) {
                String msg = command.replace("say ", "");
                if (command.startsWith("say"))
                    msg = msg.replaceFirst("say ", "");
                if (command.startsWith("me")) {
                    raw = true;
                    msg = "*" + MessageUtils.escapeMarkdown(msg.replaceFirst("me ", "").trim()) + "*";
                }
                INSTANCE.sendMessage(ev.sender.getCommandSenderName(), sourceEntity != null ? sourceEntity.getUniqueID().toString() : "0000000", new DiscordMessage(null, msg, !raw), INSTANCE.getChannel(Configuration.instance().advanced.chatOutputChannelID));
            }

            if(command.startsWith("tellraw ") && !Configuration.instance().messages.tellrawSelector.trim().isEmpty()){
                final String[] args = command.replace("tellraw ", "").replace("dc ", "").split(" ");
                if(args[0].equals(Configuration.instance().messages.tellrawSelector)){
                    INSTANCE.sendMessage(DiscordSerializer.INSTANCE.serialize(GsonComponentSerializer.gson().deserialize(command.replace("tellraw " + args[0], ""))));
                }
            }
            /*
            if (command.startsWith("discord ") || command.startsWith("dc ")) {
                final String[] args = command.replace("discord ", "").replace("dc ", "").split(" ");
                for (MCSubCommand mcSubCommand : McCommandRegistry.getCommands()) {
                    if (args[0].equals(mcSubCommand.getName())) {
                        final String[] cmdArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
                        switch (mcSubCommand.getType()) {
                            case CONSOLE_ONLY:
                                try {
                                    source.getPlayerOrException();
                                    source.sendFailure(net.minecraft.network.chat.Component.nullToEmpty(Localization.instance().commands.consoleOnly));
                                } catch (CommandSyntaxException e) {
                                    final String txt = GsonComponentSerializer.gson().serialize(mcSubCommand.execute(cmdArgs, null));
                                    source.sendSuccess(() -> {
                                        try {
                                            return ComponentArgument.textComponent().parse(new StringReader(txt));
                                        } catch (CommandSyntaxException ignored) {
                                            return null;
                                        }
                                    }, false);
                                }
                                break;
                            case PLAYER_ONLY:
                                try {
                                    final ServerPlayer player = source.getPlayerOrException();
                                    if (!mcSubCommand.needsOP()) {
                                        final String txt = GsonComponentSerializer.gson().serialize(mcSubCommand.execute(cmdArgs, player.getUUID()));

                                        source.sendSuccess(() -> {
                                            try {
                                                return ComponentArgument.textComponent().parse(new StringReader(txt));
                                            } catch (CommandSyntaxException ignored) {
                                                return null;
                                            }
                                        }, false);

                                    } else if (source.hasPermission(4)) {
                                        final String txt = GsonComponentSerializer.gson().serialize(mcSubCommand.execute(cmdArgs, player.getUUID()));
                                        source.sendSuccess(() -> {
                                            try {
                                                return ComponentArgument.textComponent().parse(new StringReader(txt));
                                            } catch (CommandSyntaxException ignored) {
                                                return null;
                                            }
                                        }, false);

                                    } else {
                                        source.sendFailure(net.minecraft.network.chat.Component.nullToEmpty(Localization.instance().commands.noPermission));
                                    }
                                } catch (CommandSyntaxException e) {
                                    source.sendFailure(net.minecraft.network.chat.Component.nullToEmpty(Localization.instance().commands.ingameOnly));

                                }
                                break;
                            case BOTH:

                                try {
                                    final ServerPlayer player = source.getPlayerOrException();
                                    if (!mcSubCommand.needsOP()) {
                                        final String txt = GsonComponentSerializer.gson().serialize(mcSubCommand.execute(cmdArgs, player.getUUID()));
                                        source.sendSuccess(() -> {
                                            try {
                                                return ComponentArgument.textComponent().parse(new StringReader(txt));
                                            } catch (CommandSyntaxException ignored) {
                                                return null;
                                            }
                                        }, false);

                                    } else if (source.hasPermission(4)) {
                                        final String txt = GsonComponentSerializer.gson().serialize(mcSubCommand.execute(cmdArgs, player.getUUID()));

                                        source.sendSuccess(() -> {
                                            try {
                                                return ComponentArgument.textComponent().parse(new StringReader(txt));
                                            } catch (CommandSyntaxException ignored) {
                                                return null;
                                            }
                                        }, false);
                                    } else {
                                        source.sendFailure(net.minecraft.network.chat.Component.nullToEmpty(Localization.instance().commands.noPermission));
                                    }

                                } catch (CommandSyntaxException e) {
                                    final String txt = GsonComponentSerializer.gson().serialize(mcSubCommand.execute(cmdArgs, null));
                                    source.sendSuccess(() -> {
                                        try {
                                            return ComponentArgument.textComponent().parse(new StringReader(txt));
                                        } catch (CommandSyntaxException ignored) {
                                            return null;
                                        }
                                    }, false);

                                }
                                break;
                        }
                    }
                    ev.setCanceled(true);
                }
            }
             */
        }
    }

    @SubscribeEvent
    public void chat(ServerChatEvent ev) {
        if (Localization.instance().discordChatMessage.trim().isEmpty()) return;
        if (LinkManager.isPlayerLinked(ev.player.getUniqueID()) && LinkManager.getLink(null, ev.player.getUniqueID()).settings.hideFromDiscord)
            return;
        final ChatComponentTranslation msg = ev.component;
        if (INSTANCE.callEvent((e) -> {
            if (e instanceof ForgeDiscordEventHandler) {
                return ((ForgeDiscordEventHandler) e).onMcChatMessage(ev);
            }
            return false;
        })) return;

        final String text = MessageUtils.escapeMarkdown(ev.message.replace("@everyone", "[at]everyone").replace("@here", "[at]here"));
        final MessageEmbed embed = ForgeMessageUtils.genItemStackEmbedIfAvailable(msg);
        if (INSTANCE != null) {
            GuildMessageChannel channel = INSTANCE.getChannel(Configuration.instance().advanced.chatOutputChannelID);
            if (channel == null) return;
            if (!Localization.instance().discordChatMessage.trim().isEmpty())
                if (Configuration.instance().embedMode.enabled && Configuration.instance().embedMode.chatMessages.asEmbed) {
                    final String avatarURL = Configuration.instance().webhook.playerAvatarURL.replace("%uuid%", ev.player.getUniqueID().toString()).replace("%uuid_dashless%", ev.player.getUniqueID().toString().replace("-", "")).replace("%name%", ev.player.getCommandSenderName()).replace("%randomUUID%", UUID.randomUUID().toString());
                    if (!Configuration.instance().embedMode.chatMessages.customJSON.trim().isEmpty()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.chatMessages.toEmbedJson(Configuration.instance().embedMode.chatMessages.customJSON
                            .replace("%uuid%", ev.player.getUniqueID().toString())
                            .replace("%uuid_dashless%", ev.player.getUniqueID().toString().replace("-", ""))
                            .replace("%name%", ForgeMessageUtils.formatPlayerName(ev.player))
                            .replace("%randomUUID%", UUID.randomUUID().toString())
                            .replace("%avatarURL%", avatarURL)
                            .replace("%msg%", text)
                            .replace("%playerColor%", "" + TextColors.generateFromUUID(ev.player.getUniqueID()).getRGB())
                        );
                        INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else {
                        EmbedBuilder b = Configuration.instance().embedMode.chatMessages.toEmbed();
                        if (Configuration.instance().embedMode.chatMessages.generateUniqueColors)
                            b = b.setColor(TextColors.generateFromUUID(ev.player.getUniqueID()));
                        b = b.setAuthor(ForgeMessageUtils.formatPlayerName(ev.player), null, avatarURL)
                            .setDescription(text);
                        INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    }
                } else
                    INSTANCE.sendMessage(ForgeMessageUtils.formatPlayerName(ev.player), ev.player.getUniqueID().toString(), new DiscordMessage(embed, text, true), channel);
            if(!Configuration.instance().compatibility.disableParsingMentionsIngame) {
                final String json = IChatComponent.Serializer.func_150696_a(msg);
                Component comp = GsonComponentSerializer.gson().deserialize(json);
                final String editedJson = GsonComponentSerializer.gson().serialize(MessageUtils.mentionsToNames(comp, channel.getGuild()));
                ev.component = (ChatComponentTranslation) IChatComponent.Serializer.func_150699_a(editedJson);
            }
        }
    }

    @SubscribeEvent
    public void death(LivingDeathEvent ev) {
        if (Localization.instance().playerDeath.trim().isEmpty()) return;
        if (ev.entityLiving instanceof EntityPlayer player) {
            if (LinkManager.isPlayerLinked(player.getUniqueID()) && LinkManager.getLink(null, player.getUniqueID()).settings.hideFromDiscord)
                return;
            if (INSTANCE != null) {
                final IChatComponent deathMessage = ev.source.func_151519_b(player);
                final MessageEmbed embed = ForgeMessageUtils.genItemStackEmbedIfAvailable(deathMessage);
                if (Configuration.instance().embedMode.enabled && Configuration.instance().embedMode.deathMessage.asEmbed) {
                    final String avatarURL = Configuration.instance().webhook.playerAvatarURL.replace("%uuid%", player.getUniqueID().toString()).replace("%uuid_dashless%", player.getUniqueID().toString().replace("-", "")).replace("%name%", player.getCommandSenderName()).replace("%randomUUID%", UUID.randomUUID().toString());
                    if (!Configuration.instance().embedMode.playerJoinMessage.customJSON.trim().isEmpty()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.playerJoinMessage.toEmbedJson(Configuration.instance().embedMode.playerJoinMessage.customJSON
                            .replace("%uuid%", player.getUniqueID().toString())
                            .replace("%uuid_dashless%", player.getUniqueID().toString().replace("-", ""))
                            .replace("%name%", ForgeMessageUtils.formatPlayerName(player))
                            .replace("%randomUUID%", UUID.randomUUID().toString())
                            .replace("%avatarURL%", avatarURL)
                            .replace("%deathMessage%", ForgeMessageUtils.getTextWithoutFormattingCodes(deathMessage.getUnformattedTextForChat()).replace(ForgeMessageUtils.formatPlayerName(player) + " ", ""))
                            .replace("%playerColor%", "" + TextColors.generateFromUUID(player.getUniqueID()).getRGB())
                        );
                        if (embed != null) {
                            b.addBlankField(false);
                            b.addField(embed.getTitle() + " *(" + embed.getFooter().getText() + ")*", embed.getDescription(), false);
                        }
                        INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else {
                        final EmbedBuilder b = Configuration.instance().embedMode.deathMessage.toEmbed();
                        b.setDescription(":skull: " + Localization.instance().playerDeath.replace("%player%", ForgeMessageUtils.formatPlayerName(player)).replace("%msg%", ForgeMessageUtils.getTextWithoutFormattingCodes(deathMessage.getUnformattedTextForChat()).replace(ForgeMessageUtils.formatPlayerName(player) + " ", "")));
                        if (embed != null) {
                            b.addBlankField(false);
                            b.addField(embed.getTitle() + " *(" + embed.getFooter().getText() + ")*", embed.getDescription(), false);
                        }
                        INSTANCE.sendMessage(new DiscordMessage(b.build()), INSTANCE.getChannel(Configuration.instance().advanced.deathsChannelID));
                    }
                } else
                    INSTANCE.sendMessage(new DiscordMessage(embed, Localization.instance().playerDeath.replace("%player%", ForgeMessageUtils.formatPlayerName(player)).replace("%msg%", ForgeMessageUtils.getTextWithoutFormattingCodes(deathMessage.getUnformattedTextForChat()).replace(ForgeMessageUtils.formatPlayerName(player) + " ", ""))), INSTANCE.getChannel(Configuration.instance().advanced.deathsChannelID));
            }
        }
    }
}
