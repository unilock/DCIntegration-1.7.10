package cc.unilock.dcintegration.util;

import cc.unilock.dcintegration.command.DCCommandSender;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import dcshadow.com.vdurmont.emoji.EmojiParser;
import dcshadow.net.kyori.adventure.text.Component;
import dcshadow.net.kyori.adventure.text.TextReplacementConfig;
import dcshadow.net.kyori.adventure.text.event.ClickEvent;
import dcshadow.net.kyori.adventure.text.event.HoverEvent;
import dcshadow.net.kyori.adventure.text.format.Style;
import dcshadow.net.kyori.adventure.text.format.TextColor;
import dcshadow.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import dcshadow.net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.LinkManager;
import de.erdbeerbaerlp.dcintegration.common.util.ComponentUtils;
import de.erdbeerbaerlp.dcintegration.common.util.McServerInterface;
import de.erdbeerbaerlp.dcintegration.common.util.MessageUtils;
import de.erdbeerbaerlp.dcintegration.common.util.MinecraftPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static de.erdbeerbaerlp.dcintegration.common.DiscordIntegration.INSTANCE;

public class ForgeServerInterface implements McServerInterface {
    @Override
    public int getMaxPlayers() {
        return FMLCommonHandler.instance().getMinecraftServerInstance() == null ? -1 : FMLCommonHandler.instance().getMinecraftServerInstance().getMaxPlayers();
    }

    @Override
    public int getOnlinePlayers() {
        return FMLCommonHandler.instance().getMinecraftServerInstance() == null ? -1 : FMLCommonHandler.instance().getMinecraftServerInstance().getCurrentPlayerCount();
    }

    @Override
    public void sendIngameMessage(Component msg) {
        final List<EntityPlayerMP> l = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList;
        try {
            for (final EntityPlayerMP p : l) {
                if (!INSTANCE.ignoringPlayers.contains(p.getUniqueID()) && !(LinkManager.isPlayerLinked(p.getUniqueID()) && LinkManager.getLink(null, p.getUniqueID()).settings.ignoreDiscordChatIngame)) {
                    final Map.Entry<Boolean, Component> ping = ComponentUtils.parsePing(msg, p.getUniqueID(), p.getCommandSenderName());
                    final String jsonComp = GsonComponentSerializer.gson().serialize(ping.getValue()).replace("\\\\n", "\n");
                    final IChatComponent comp = IChatComponent.Serializer.func_150699_a(jsonComp);
                    p.addChatMessage(comp);
                    if (ping.getKey()) {
                        if (LinkManager.isPlayerLinked(p.getUniqueID()) && LinkManager.getLink(null, p.getUniqueID()).settings.pingSound) {
                            p.playerNetServerHandler.sendPacket(new S29PacketSoundEffect("note.harp", p.posX, p.posY, p.posZ, 1, 1));
                        }
                    }
                }
            }
            //Send to server console too
            final String jsonComp = GsonComponentSerializer.gson().serialize(msg).replace("\\\\n", "\n");
            final IChatComponent comp = IChatComponent.Serializer.func_150699_a(jsonComp);
            FMLCommonHandler.instance().getMinecraftServerInstance().addChatMessage(comp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendIngameMessage(String msg, UUID player) {
        final EntityPlayerMP p = getPlayerForUUID(player);
        if (p != null)
            p.addChatMessage(new ChatComponentText(msg));
    }

    @Override
    public void sendIngameReaction(Member member, RestAction<Message> retrieveMessage, UUID targetUniqueID, EmojiUnion reactionEmote) {
        final List<EntityPlayerMP> l = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList;
        for (final EntityPlayerMP p : l) {
            if (p.getUniqueID().equals(targetUniqueID) && !INSTANCE.ignoringPlayers.contains(p.getUniqueID()) && (LinkManager.isPlayerLinked(p.getUniqueID()) && !LinkManager.getLink(null, p.getUniqueID()).settings.ignoreDiscordChatIngame && !LinkManager.getLink(null, p.getUniqueID()).settings.ignoreReactions)) {
                final String emote = reactionEmote.getType() == Emoji.Type.UNICODE ? EmojiParser.parseToAliases(reactionEmote.getName()) : ":" + reactionEmote.getName() + ":";

                Style.Builder memberStyle = Style.style();
                if (Configuration.instance().messages.discordRoleColorIngame)
                    memberStyle = memberStyle.color(TextColor.color(member.getColorRaw()));

                final Component user = Component.text(member.getEffectiveName()).style(memberStyle
                    .clickEvent(ClickEvent.suggestCommand("<@" + member.getId() + ">"))
                    .hoverEvent(HoverEvent.showText(Component.text(Localization.instance().discordUserHover.replace("%user#tag%", member.getUser().getAsTag()).replace("%user%", member.getEffectiveName()).replace("%id%", member.getUser().getId())))));
                final TextReplacementConfig userReplacer = ComponentUtils.replaceLiteral("%user%", user);
                final TextReplacementConfig emoteReplacer = ComponentUtils.replaceLiteral("%emote%", emote);

                final Component out = LegacyComponentSerializer.legacySection().deserialize(Localization.instance().reactionMessage)
                    .replaceText(userReplacer).replaceText(emoteReplacer);

                if (Localization.instance().reactionMessage.contains("%msg%"))
                    retrieveMessage.submit().thenAccept((m) -> {
                        final String msg = MessageUtils.formatEmoteMessage(m.getMentions().getCustomEmojis(), m.getContentDisplay());
                        final TextReplacementConfig msgReplacer = ComponentUtils.replaceLiteral("%msg%", msg);
                        sendReactionMCMessage(p, out.replaceText(msgReplacer));
                    });
                else sendReactionMCMessage(p, out);
            }
        }
    }

    @Override
    public void runMcCommand(String cmd, CompletableFuture<InteractionHook> cmdMsg, User user) {
        final DCCommandSender s = new DCCommandSender(cmdMsg, user);
        if (s.canCommandSenderUseCommand(4, "")) {
            try {
                FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager().executeCommand(s, cmd.trim());
            } catch (CommandException e) {
                s.addChatMessage(new ChatComponentText(e.getMessage()));
            }
        } else {
            s.addChatMessage(new ChatComponentText("Sorry, but the bot has no permissions...\nAdd this into the servers ops.json:\n```json\n {\n   \"uuid\": \"" + Configuration.instance().commands.senderUUID + "\",\n   \"name\": \"DiscordFakeUser\",\n   \"level\": 4,\n   \"bypassesPlayerLimit\": false\n }\n```"));
        }
    }

    @Override
    public HashMap<UUID, String> getPlayers() {
        final HashMap<UUID, String> players = new HashMap<>();
        for (EntityPlayerMP p : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList) {
            players.put(p.getUniqueID(), p.getCommandSenderName());
        }
        return players;
    }

    @Override
    public boolean isOnlineMode() {
        return Configuration.instance().bungee.isBehindBungee || FMLCommonHandler.instance().getMinecraftServerInstance().isServerInOnlineMode();
    }

    @Override
    public String getNameFromUUID(UUID uuid) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().func_147130_as().fillProfileProperties(new GameProfile(uuid, ""), false).getName();
    }

    @Override
    public String getLoaderName() {
        return "Forge";
    }

    @Override
    public boolean playerHasPermissions(UUID player, String... permissions) {
        return false;
    }

    @Override
    public boolean playerHasPermissions(UUID player, MinecraftPermission... permissions) {
        return McServerInterface.super.playerHasPermissions(player, permissions);
    }

    private void sendReactionMCMessage(EntityPlayerMP target, Component msgComp) {
        final String jsonComp = GsonComponentSerializer.gson().serialize(msgComp).replace("\\\\n", "\n");
        try {
            final IChatComponent comp = IChatComponent.Serializer.func_150699_a(jsonComp);
            target.addChatMessage(comp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EntityPlayerMP getPlayerForUUID(UUID uuid) {
        Iterator<EntityPlayerMP> iterator = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList.iterator();
        EntityPlayerMP entityplayermp;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            entityplayermp = iterator.next();
        } while (!entityplayermp.getUniqueID().equals(uuid));

        return entityplayermp;
    }
}
