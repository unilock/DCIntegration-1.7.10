package cc.unilock.dcintegration.command;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.util.MessageUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DCCommandSender extends FakePlayer {
    private static final UUID uuid = UUID.fromString(Configuration.instance().commands.senderUUID);
    private final CompletableFuture<InteractionHook> cmdMsg;
    private CompletableFuture<Message> cmdMessage;
    final StringBuilder message = new StringBuilder();

    public DCCommandSender(CompletableFuture<InteractionHook> cmdMsg, User user) {
        super(FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0), new GameProfile(uuid, "@" + (!user.getDiscriminator().equals("0000") ? user.getAsTag() : user.getName())));
        this.cmdMsg = cmdMsg;
    }

    private static String textComponentToDiscordMessage(IChatComponent component) {
        if (component == null) return "";
        return MessageUtils.convertMCToMarkdown(component.getFormattedText());
    }

    @Override
    public void addChatComponentMessage(IChatComponent message) {
        message.appendText(textComponentToDiscordMessage(message.createCopy())).appendText("\n");
        if (cmdMessage == null)
            cmdMsg.thenAccept((msg) -> {
                cmdMessage = msg.editOriginal(message.toString().trim()).submit();
            });
        else
            cmdMessage.thenAccept((msg)->{
                cmdMessage = msg.editMessage(message.toString().trim()).submit();
            });
    }

    @Override
    public void addChatMessage(IChatComponent message) {
        message.appendText(textComponentToDiscordMessage(message.createCopy())).appendText("\n");
        if (cmdMessage == null)
            cmdMsg.thenAccept((msg) -> {
                cmdMessage = msg.editOriginal(message.toString().trim()).submit();
            });
        else
            cmdMessage.thenAccept((msg)->{
                cmdMessage = msg.editMessage(message.toString().trim()).submit();
            });
    }
}
