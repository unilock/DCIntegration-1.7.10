package cc.unilock.dcintegration.mixin;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.LinkManager;
import net.minecraft.server.management.ServerConfigurationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

import static de.erdbeerbaerlp.dcintegration.common.DiscordIntegration.INSTANCE;

@Mixin(value = ServerConfigurationManager.class, priority = 1001)
public class MixinPlayerLogin {
    @Inject(method = "Lnet/minecraft/server/management/ServerConfigurationManager;allowUserToConnect(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void canLogin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<String> cir) {
        if (Configuration.instance().linking.whitelistMode && FMLCommonHandler.instance().getMinecraftServerInstance().isServerInOnlineMode()) {
            LinkManager.checkGlobalAPI(profile.getId());
            try {
                if (!LinkManager.isPlayerLinked(profile.getId())) {
                    cir.setReturnValue(Localization.instance().linking.notWhitelistedCode.replace("%code%",""+LinkManager.genLinkNumber(profile.getId())));
                }else if(!INSTANCE.canPlayerJoin(profile.getId())){
                    cir.setReturnValue(Localization.instance().linking.notWhitelistedRole);
                }
            } catch (IllegalStateException e) {
                cir.setReturnValue("An error occured\nPlease check Server Log for more information\n\n" + e);
            }
        }
    }
}
