package cc.unilock.dcintegration.util;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;

import java.util.regex.Pattern;

public class ForgeMessageUtils {
    //private static final FMLControlledNamespacedRegistry<Item> itemreg = GameData.getItemRegistry();
    private static final Pattern formattingCodePattern = Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]");

    public static String formatPlayerName(EntityPlayer player) {
        return ForgeMessageUtils.getTextWithoutFormattingCodes(player.getCommandSenderName());
    }

    public static String formatPlayerNameForGameMsg(EntityPlayer player) {
        if(player.getDisplayName() != null)
            return ForgeMessageUtils.getTextWithoutFormattingCodes(player.getDisplayName());
        else
            return ForgeMessageUtils.getTextWithoutFormattingCodes(player.getCommandSenderName());
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

    public static String getTextWithoutFormattingCodes(String text) {
        return text == null ? null : formattingCodePattern.matcher(text).replaceAll("");
    }
}
