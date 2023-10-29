package cc.unilock.dcintegration.mixin;

import cc.unilock.dcintegration.DiscordIntegrationMod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin used to detect player timeouts
 */
@Mixin(value = NetHandlerPlayServer.class, priority = 1001)
public class MixinNetHandlerPlayServer {
    @Shadow
    public EntityPlayerMP playerEntity;

    @Inject(method = "Lnet/minecraft/network/NetHandlerPlayServer;onDisconnect(Lnet/minecraft/util/IChatComponent;)V", at = @At("HEAD"))
    private void onDisconnect(IChatComponent reason, CallbackInfo ci) {
        if (reason.equals(new ChatComponentTranslation("disconnect.timeout")))
            DiscordIntegrationMod.timeouts.add(this.playerEntity.getUniqueID());
    }
}
