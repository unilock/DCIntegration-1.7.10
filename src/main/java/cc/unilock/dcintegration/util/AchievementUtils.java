package cc.unilock.dcintegration.util;

import cc.unilock.dcintegration.mixin.MixinAchievement;
import cc.unilock.dcintegration.mixin.MixinStatBase;
import net.minecraft.stats.Achievement;
import net.minecraft.util.StatCollector;

public class AchievementUtils {
    public static String getAdvName(Achievement achievement) {
        return ForgeMessageUtils.getTextWithoutFormattingCodes(((MixinStatBase) achievement).getStatName().getUnformattedTextForChat());
    }

    // TODO: may be finicky.
    public static String getAdvDesc(Achievement achievement) {
        return ForgeMessageUtils.getTextWithoutFormattingCodes(
            achievement.statId.equals("achievement.openInventory")
                ? StatCollector.translateToLocal(((MixinAchievement) achievement).getAchievementDescription()).replace("%1$s", "E")
                : StatCollector.translateToLocal(((MixinAchievement) achievement).getAchievementDescription())
        );
    }
}
