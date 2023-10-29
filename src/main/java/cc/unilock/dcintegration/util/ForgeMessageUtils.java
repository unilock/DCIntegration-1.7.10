package cc.unilock.dcintegration.util;

import dcshadow.org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.Map;
import java.util.UUID;

public class ForgeMessageUtils {
    //private static final FMLControlledNamespacedRegistry<Item> itemreg = GameData.getItemRegistry();

    public static String formatPlayerName(Map.Entry<UUID, String> p) {
        return formatPlayerName(p, true);
    }

    public static String formatPlayerName(Map.Entry<UUID, String> p, boolean chatFormat) {
        return EnumChatFormatting.getTextWithoutFormattingCodes(p.getValue());
    }

    /**
     * Attempts to generate an {@link MessageEmbed} showing item info from an {@link IChatComponent} instance
     *
     * @param component The TextComponent to scan for item info
     * @return an {@link MessageEmbed} when there was an Item info, or {@link null} if there was no item info OR the item info was disabled
     */
    public static MessageEmbed genItemStackEmbedIfAvailable(final IChatComponent component) {
        return null;
        // NYI
    }

    public static String formatPlayerName(Entity p) {
        final Map.Entry<UUID, String> e = new DefaultMapEntry<>(p.getUniqueID(), p.getCommandSenderName());
        return formatPlayerName(e);
    }
}
