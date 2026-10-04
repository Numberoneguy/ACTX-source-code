package net.minecraft.command;

import java.util.List;
import net.lax1dude.eaglercraft.v1_8.sp.ipc.IPCPacket1FBanPlayer;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;

public class CommandBan extends CommandBase {

    @Override
    public String getCommandName() { 
        return "ban"; 
    }

    @Override
    public String getCommandUsage(ICommandSender sender) { 
        return StatCollector.translateToLocal("commands.ban.usage"); 
    }

    @Override
    public int getRequiredPermissionLevel() { 
        return 2; 
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || args[0].length() == 0) {
            actxchatutils.sendError(sender, getCommandUsage(sender));
            return;
        }

        MinecraftServer server = MinecraftServer.getServer();
        ServerConfigurationManager scm = server.getConfigurationManager();
        actxmiscdata.ensureLoaded(scm);
        String targetName = args[0];

        EntityPlayerMP targetPlayer = scm.getPlayerByUsername(targetName);
        if (targetPlayer != null) {
            targetName = targetPlayer.getName();
        }

        if (targetName.equalsIgnoreCase(sender.getName())) {
            actxchatutils.sendWarning(sender, "commands.ban.cannotBanSelf");
            return;
        }

        String serverOwner = server.getServerOwner();
        if (serverOwner != null && targetName.equalsIgnoreCase(serverOwner)) {
            actxchatutils.sendWarning(sender, "commands.ban.cannotBanOwner");
            return;
        }

        if (actxmiscdata.session_bans.contains(targetName.toLowerCase())) {
            actxchatutils.sendWarning(sender, "commands.ban.failed", targetName);
            return;
        }

        String reason = args.length > 1 ? buildString(args, 1) : StatCollector.translateToLocal("commands.ban.reason.default");

        IPCPacket1FBanPlayer banPkt = new IPCPacket1FBanPlayer(targetName, true);
        actxmiscdata.session_bans.add(banPkt.playerName.toLowerCase());
        actxmiscdata.save(scm);

        if (targetPlayer != null) {
            targetPlayer.playerNetServerHandler.kickPlayerFromServer(reason);
        }

        notifyOperators(sender, this, "commands.ban.success", targetName, reason);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args,
                    MinecraftServer.getServer().getConfigurationManager().getAllUsernames());
        }
        return null;
    }
}