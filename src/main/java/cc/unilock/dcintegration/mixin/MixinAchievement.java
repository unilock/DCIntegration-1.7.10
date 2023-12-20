package cc.unilock.dcintegration.mixin;

import net.minecraft.stats.Achievement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Achievement.class)
public interface MixinAchievement {
    @Accessor
    String getAchievementDescription();
}
