package net.minecraft.command;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StatCollector;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;

public class CommandOp extends CommandBase {

    @Override
    public String getCommandName() { 
        return "op"; 
    }

    @Override
    public String getCommandUsage(ICommandSender sender) { 
        return StatCollector.translateToLocal("commands.op.usage"); 
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

        if (scm.isOpped(targetName)) {
            actxchatutils.sendWarning(sender, "commands.op.failed", targetName);
            return;
        }

        EntityPlayerMP target = scm.getPlayerByUsername(targetName);
        if (target != null) {
            targetName = target.getName();
        }

        scm.addOp(targetName);
        String targetUuid = target != null
                ? target.getUniqueID().toString()
                : EntityPlayer.getOfflineUUID(targetName).toString();
        actxmiscdata.addOpRecord(targetName, targetUuid);
        actxmiscdata.save(scm);

        if (target != null) {
            actxchatutils.sendSuccess(target, "commands.op.activated");
        }

        notifyOperators(sender, this, "commands.op.made", targetName);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            String[] allUsernames = MinecraftServer.getServer().getConfigurationManager().getAllUsernames();
            List<String> unOpped = new ArrayList<>();
            ServerConfigurationManager scm = MinecraftServer.getServer().getConfigurationManager();
            for (String username : allUsernames) {
                if (!scm.isOpped(username)) {
                    unOpped.add(username);
                }
            }
            return getListOfStringsMatchingLastWord(args, unOpped);
        }
        return null;
    }
}