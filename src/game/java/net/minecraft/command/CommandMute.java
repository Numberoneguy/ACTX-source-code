package net.minecraft.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

public class CommandMute extends CommandBase {

    public static final Set<String> SESSION_MUTES = new HashSet<>();

    @Override
    public String getCommandName() {
        return "mute";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return StatCollector.translateToLocal("commands.mute.usage");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        String targetName = args[0].toLowerCase();

        if (targetName.equalsIgnoreCase(sender.getName())) {
            ChatComponentTranslation chatMsg = new ChatComponentTranslation("commands.mute.self");
            chatMsg.getChatStyle().setColor(EnumChatFormatting.RED);
            sender.addChatMessage(chatMsg);
            return;
        }

        MinecraftServer server = MinecraftServer.getServer();
        String serverOwner = server.getServerOwner();

        if (serverOwner != null && targetName.equalsIgnoreCase(serverOwner)) {
            ChatComponentTranslation chatMsg = new ChatComponentTranslation("commands.mute.owner");
            chatMsg.getChatStyle().setColor(EnumChatFormatting.RED);
            sender.addChatMessage(chatMsg);
            return;
        }

        if (SESSION_MUTES.contains(targetName)) {
            SESSION_MUTES.remove(targetName);
            
            ChatComponentTranslation senderMsg = new ChatComponentTranslation("commands.mute.unmuted.sender", args[0]);
            senderMsg.getChatStyle().setColor(EnumChatFormatting.GREEN);
            sender.addChatMessage(senderMsg);

            EntityPlayerMP targetPlayer = server.getConfigurationManager().getPlayerByUsername(args[0]);
            if (targetPlayer != null) {
                ChatComponentTranslation targetMsg = new ChatComponentTranslation("commands.mute.unmuted.target");
                targetMsg.getChatStyle().setColor(EnumChatFormatting.GREEN);
                targetPlayer.addChatMessage(targetMsg);
            }
        } else {
            SESSION_MUTES.add(targetName);

            ChatComponentTranslation senderMsg = new ChatComponentTranslation("commands.mute.success", args[0]);
            senderMsg.getChatStyle().setColor(EnumChatFormatting.GREEN);
            sender.addChatMessage(senderMsg);

            EntityPlayerMP targetPlayer = server.getConfigurationManager().getPlayerByUsername(args[0]);
            if (targetPlayer != null) {
                ChatComponentTranslation targetMsg = new ChatComponentTranslation("commands.mute.target");
                targetMsg.getChatStyle().setColor(EnumChatFormatting.RED);
                targetPlayer.addChatMessage(targetMsg);
            }
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();
            for (String username : MinecraftServer.getServer().getAllUsernames()) {
                if (username.toLowerCase().startsWith(input)) {
                    matches.add(username);
                }
            }
            return matches;
        }
        return null;
    }
}