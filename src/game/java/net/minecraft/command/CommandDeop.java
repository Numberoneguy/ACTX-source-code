package net.minecraft.command;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.StatCollector;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;

public class CommandDeop extends CommandBase {

    @Override
    public String getCommandName() { 
        return "deop"; 
    }

    @Override
    public String getCommandUsage(ICommandSender sender) { 
        return StatCollector.translateToLocal("commands.deop.usage"); 
    }

    @Override
    public int getRequiredPermissionLevel() { 
        return 3; 
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP senderPlayer = (EntityPlayerMP) sender;
            if (!MinecraftServer.getServer().getServerOwner().equals(senderPlayer.getName())) {
                actxchatutils.sendError(sender, "commands.generic.permission");
                return;
            }
        }

        if (args.length < 1 || args[0].length() == 0) {
            actxchatutils.sendError(sender, getCommandUsage(sender));
            return;
        }

        ServerConfigurationManager scm = MinecraftServer.getServer().getConfigurationManager();
        actxmiscdata.ensureLoaded(scm);
        String targetName = args[0];

        if (!scm.isOpped(targetName)) {
            actxchatutils.sendWarning(sender, "commands.deop.failed", targetName);
            return;
        }

        EntityPlayerMP target = scm.getPlayerByUsername(targetName);
        if (target != null) {
            targetName = target.getName();
        }

        scm.removeOp(targetName);
        actxmiscdata.save(scm);

        if (target != null) {
            actxchatutils.sendError(target, "commands.deop.deactivated");
        }

        notifyOperators(sender, this, "commands.deop.made", targetName);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            ServerConfigurationManager scm = MinecraftServer.getServer().getConfigurationManager();
            String[] allUsernames = scm.getAllUsernames();
            List<String> opped = new ArrayList<>();
            for (String username : allUsernames) {
                if (scm.isOpped(username)) {
                    opped.add(username);
                }
            }
            return getListOfStringsMatchingLastWord(args, opped);
        }
        return null;
    }
}