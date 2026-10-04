package net.minecraft.command;

import java.util.List;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.util.StatCollector;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class CommandResetPassword extends CommandBase {

    @Override
    public String getCommandName() {
        return "resetpassword";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return StatCollector.translateToLocal("commands.resetpassword.usage");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            actxchatutils.sendError(sender, getCommandUsage(sender));
            return;
        }

        String target = args[0];
        actxmiscdata.ensureLoaded();

        if (!actxmiscdata.isRegistered(target)) {
            actxchatutils.sendError(sender, "commands.login.notRegistered");
            return;
        }

        actxmiscdata.unregisterUser(target);
        actxchatutils.sendSuccess(sender, "commands.resetpassword.success");

        EntityPlayerMP targetPlayer = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(target);
        if (targetPlayer != null) {
            actxchatutils.sendWarning(targetPlayer, "commands.resetpassword.targetNotify");
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
    }
}