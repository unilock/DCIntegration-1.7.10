package cc.unilock.dcintegration;

import cc.unilock.dcintegration.util.ForgeMessageUtils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import de.erdbeerbaerlp.dcintegration.common.WorkThread;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.LinkManager;
import de.erdbeerbaerlp.dcintegration.common.util.DiscordMessage;
import de.erdbeerbaerlp.dcintegration.common.util.TextColors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

import static de.erdbeerbaerlp.dcintegration.common.DiscordIntegration.INSTANCE;

public class FMLEventListener {
    @SubscribeEvent
    public void playerJoin(PlayerEvent.PlayerLoggedInEvent ev) {
        if (INSTANCE != null) {
            if (LinkManager.isPlayerLinked(ev.player.getUniqueID()) && LinkManager.getLink(null, ev.player.getUniqueID()).settings.hideFromDiscord)
                return;
            LinkManager.checkGlobalAPI(ev.player.getUniqueID());
            if (!Localization.instance().playerJoin.trim().isEmpty()) {
                final EntityPlayer p = ev.player;
                if (Configuration.instance().embedMode.enabled && Configuration.instance().embedMode.playerJoinMessage.asEmbed) {
                    final String avatarURL = Configuration.instance().webhook.playerAvatarURL.replace("%uuid%", p.getUniqueID().toString()).replace("%uuid_dashless%", p.getUniqueID().toString().replace("-", "")).replace("%name%", p.getCommandSenderName()).replace("%randomUUID%", UUID.randomUUID().toString());
                    if (!Configuration.instance().embedMode.playerJoinMessage.customJSON.trim().isEmpty()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.playerJoinMessage.toEmbedJson(Configuration.instance().embedMode.playerJoinMessage.customJSON
                            .replace("%uuid%", p.getUniqueID().toString())
                            .replace("%uuid_dashless%", p.getUniqueID().toString().replace("-", ""))
                            .replace("%name%", ForgeMessageUtils.formatPlayerName(p))
                            .replace("%randomUUID%", UUID.randomUUID().toString())
                            .replace("%avatarURL%", avatarURL)
                            .replace("%playerColor%", "" + TextColors.generateFromUUID(p.getUniqueID()).getRGB())
                        );
                        INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else {
                        final EmbedBuilder b = Configuration.instance().embedMode.playerJoinMessage.toEmbed();
                        b.setAuthor(ForgeMessageUtils.formatPlayerName(p), null, avatarURL)
                            .setDescription(Localization.instance().playerJoin.replace("%player%", ForgeMessageUtils.formatPlayerName(p)));
                        INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    }
                } else
                    INSTANCE.sendMessage(Localization.instance().playerJoin.replace("%player%", ForgeMessageUtils.formatPlayerName(p)));
            }
            // Fix link status (if user does not have role, give the role to the user, or vice versa)
            WorkThread.executeJob(() -> {
                if (Configuration.instance().linking.linkedRoleID.equals("0")) return;
                final UUID uuid = ev.player.getUniqueID();
                if (!LinkManager.isPlayerLinked(uuid)) return;
                final Guild guild = INSTANCE.getChannel().getGuild();
                final Role linkedRole = guild.getRoleById(Configuration.instance().linking.linkedRoleID);
                if (LinkManager.isPlayerLinked(uuid)) {
                    final Member member = INSTANCE.getMemberById(LinkManager.getLink(null, uuid).discordID);
                    if (!member.getRoles().contains(linkedRole))
                        guild.addRoleToMember(member, linkedRole).queue();
                }
            });
        }
    }

    @SubscribeEvent
    public void playerLeave(PlayerEvent.PlayerLoggedOutEvent ev) {
        if (DiscordIntegrationMod.stopped) return; //Try to fix player leave messages after stop!
        if (Localization.instance().playerLeave.trim().isEmpty()) return;
        final EntityPlayer player = ev.player;
        final String avatarURL = Configuration.instance().webhook.playerAvatarURL.replace("%uuid%", player.getUniqueID().toString()).replace("%uuid_dashless%", player.getUniqueID().toString().replace("-", "")).replace("%name%", player.getCommandSenderName()).replace("%randomUUID%", UUID.randomUUID().toString());
        if (INSTANCE != null && !DiscordIntegrationMod.timeouts.contains(player.getUniqueID())) {
            if (!Localization.instance().playerLeave.trim().isEmpty()) {
                if (Configuration.instance().embedMode.enabled && Configuration.instance().embedMode.playerLeaveMessages.asEmbed) {
                    if (!Configuration.instance().embedMode.playerLeaveMessages.customJSON.trim().isEmpty()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.playerLeaveMessages.toEmbedJson(Configuration.instance().embedMode.playerLeaveMessages.customJSON
                            .replace("%uuid%", player.getUniqueID().toString())
                            .replace("%uuid_dashless%", player.getUniqueID().toString().replace("-", ""))
                            .replace("%name%", ForgeMessageUtils.formatPlayerName(player))
                            .replace("%randomUUID%", UUID.randomUUID().toString())
                            .replace("%avatarURL%", avatarURL)
                            .replace("%playerColor%", "" + TextColors.generateFromUUID(player.getUniqueID()).getRGB())
                        );
                        INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else {
                        EmbedBuilder b = Configuration.instance().embedMode.playerLeaveMessages.toEmbed();
                        b = b.setAuthor(ForgeMessageUtils.formatPlayerName(player), null, avatarURL)
                            .setDescription(Localization.instance().playerLeave.replace("%player%", ForgeMessageUtils.formatPlayerName(player)));
                        INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    }
                } else
                    INSTANCE.sendMessage(Localization.instance().playerLeave.replace("%player%", ForgeMessageUtils.formatPlayerName(player)));
            }
        } else if (INSTANCE != null && DiscordIntegrationMod.timeouts.contains(player.getUniqueID())) {
            if (!Localization.instance().playerTimeout.trim().isEmpty()) {
                if (Configuration.instance().embedMode.enabled && Configuration.instance().embedMode.playerLeaveMessages.asEmbed) {
                    EmbedBuilder b = Configuration.instance().embedMode.playerLeaveMessages.toEmbed();
                    b = b.setAuthor(ForgeMessageUtils.formatPlayerName(player), null, avatarURL)
                        .setDescription(Localization.instance().playerTimeout.replace("%player%", ForgeMessageUtils.formatPlayerName(player)));
                    INSTANCE.sendMessage(new DiscordMessage(b.build()));
                } else
                    INSTANCE.sendMessage(Localization.instance().playerTimeout.replace("%player%", ForgeMessageUtils.formatPlayerName(player)));
            }
            DiscordIntegrationMod.timeouts.remove(player.getUniqueID());
        }
    }
}
