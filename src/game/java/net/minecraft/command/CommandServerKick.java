package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringUtils;

public class CommandServerKick extends CommandBase {

    public String getCommandName() {
        return "kick";
    }

    public int getRequiredPermissionLevel() {
        return 3;
    }

    public String getCommandUsage(ICommandSender var1) {
        return "commands.kick.usage";
    }

    public void processCommand(ICommandSender id, String[] args) throws CommandException {
        if (id instanceof EntityPlayerMP) {
            EntityPlayerMP senderPlayer = (EntityPlayerMP) id;
            MinecraftServer permServer = MinecraftServer.getServer();
            boolean isOwner = permServer.getServerOwner().equals(senderPlayer.getName());
            boolean isOpped = permServer.getConfigurationManager().isOpped(senderPlayer.getName());
            if (!isOwner && !isOpped) {
                throw new CommandException(StatCollector.translateToLocal("commands.kick.noPermission"));
            }
        }

        if (args.length > 0 && args[0].length() > 0) {
            MinecraftServer server = MinecraftServer.getServer();
            EntityPlayerMP entityplayermp = server.getConfigurationManager().getPlayerByUsername(args[0]);

            if (entityplayermp == null) {
                throw new PlayerNotFoundException();
            }

            if (entityplayermp.getName().equalsIgnoreCase(id.getName())) {
                String text = StatCollector.translateToLocal("commands.kick.cannotKickSelf");
                IChatComponent failedMsg = new ChatComponentText(text);
                failedMsg.getChatStyle().setColor(EnumChatFormatting.YELLOW);
                id.addChatMessage(failedMsg);
                return;
            }

            String serverOwner = server.getServerOwner();
            if (serverOwner != null && entityplayermp.getName().equalsIgnoreCase(serverOwner)) {
                String text = StatCollector.translateToLocal("commands.kick.cannotKickOwner");
                IChatComponent failedMsg = new ChatComponentText(text);
                failedMsg.getChatStyle().setColor(EnumChatFormatting.YELLOW);
                id.addChatMessage(failedMsg);
                return;
            }

            String s = StatCollector.translateToLocal("commands.kick.default");
            boolean flag = false;

            if (args.length >= 2) {
                s = getChatComponentFromNthArg(id, args, 1).getUnformattedText();
                if (server.worldServers[0].getWorldInfo().getGameRulesInstance().getBoolean("colorCodes")) {
                    s = StringUtils.translateControlCodesAlternate(s);
                }
                flag = true;
            }

            entityplayermp.playerNetServerHandler.kickPlayerFromServer(s);
            if (flag) {
                notifyOperators(id, this, "commands.kick.success.reason",
                        new Object[] { entityplayermp.getName(), s });
            } else {
                notifyOperators(id, this, "commands.kick.success",
                        new Object[] { entityplayermp.getName() });
            }
        } else {
            throw new WrongUsageException(StatCollector.translateToLocal("commands.kick.usage"), new Object[0]);
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender var1, String[] astring, BlockPos var3) {
        return astring.length >= 1
                ? getListOfStringsMatchingLastWord(astring, MinecraftServer.getServer().getAllUsernames())
                : null;
    }
}