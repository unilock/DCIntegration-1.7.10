package cc.unilock.dcintegration.command;

import cpw.mods.fml.common.FMLCommonHandler;
import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.util.MessageUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;

public class DCCommandSender implements ICommandSender {
    private final CompletableFuture<InteractionHook> cmdMsg;
    private final String name;
    private CompletableFuture<Message> cmdMessage;
    final StringBuilder message = new StringBuilder();

    private final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

    public DCCommandSender(CompletableFuture<InteractionHook> cmdMsg, User user) {
        final Member member = DiscordIntegration.INSTANCE.getMemberById(user.getId());

        if (member != null)
            name = "@" + (!member.getUser().getDiscriminator().equals("0000") ? member.getUser().getAsTag() : member.getEffectiveName());
        else
            name = "@" + (!user.getDiscriminator().equals("0000") ? user.getAsTag() : user.getEffectiveName());

        this.cmdMsg = cmdMsg;
    }

    private static String textComponentToDiscordMessage(IChatComponent component) {
        if (component == null) return "";
        return MessageUtils.convertMCToMarkdown(component.getFormattedText());
    }

    @Override
    public void addChatMessage(IChatComponent p_215097_) {
        message.append(textComponentToDiscordMessage(p_215097_)).append("\n");
        if (cmdMessage == null)
            cmdMsg.thenAccept((msg) -> {
                cmdMessage = msg.editOriginal(message.toString().trim()).submit();
            });
        else
            cmdMessage.thenAccept((msg) -> {
                cmdMessage = msg.editMessage(message.toString().trim()).submit();
            });
    }

    @Override
    public String getCommandSenderName() {
        return name;
    }

    @Override
    public IChatComponent func_145748_c_() {
        return new ChatComponentText(name);
    }

    @Override
    public boolean canCommandSenderUseCommand(int permissionLevel, String command) {
        return true; // TODO
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates() {
        return new ChunkCoordinates(0, 0, 0);
    }

    @Override
    public World getEntityWorld() {
        return server.worldServerForDimension(0);
    }
}
