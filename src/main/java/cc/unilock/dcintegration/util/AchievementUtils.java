package cc.unilock.dcintegration.util;

import cc.unilock.dcintegration.mixin.MixinStatBase;
import net.minecraft.stats.Achievement;
import net.minecraft.util.StatCollector;

public class AchievementUtils {
    public static String getAdvName(Achievement achievement) {
        return ForgeMessageUtils.getTextWithoutFormattingCodes(((MixinStatBase) achievement).getStatName().getUnformattedTextForChat());
    }

    // TODO: may be finicky.
    public static String getAdvDesc(Achievement achievement) {
        String statId = achievement.statId.startsWith("achievement.") ? achievement.statId : "achievement."+achievement.statId;

        return ForgeMessageUtils.getTextWithoutFormattingCodes(
            statId.equals("achievement.openInventory")
                ? StatCollector.translateToLocal(statId + ".desc").replace("%1$s", "E")
                : StatCollector.translateToLocal(statId + ".desc")
        );
    }
}
