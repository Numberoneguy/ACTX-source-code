package net.minecraft.command;

import java.util.Arrays;
import java.util.List;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.util.StatCollector;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommandLogin extends CommandBase {

    @Override
    public String getCommandName() {
        return "login";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("l");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return StatCollector.translateToLocal("commands.login.usage");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            if (player.isServerHost()) {
                actxchatutils.sendError(player, "actx.cannotdo");
                return;
            }
        }

        if (args.length < 1) {
            actxchatutils.sendError(sender, getCommandUsage(sender));
            return;
        }

        String username = sender.getName();
        actxmiscdata.ensureLoaded();

        if (!actxmiscdata.isRegistered(username)) {
            actxchatutils.sendError(sender, "commands.login.notRegistered");
            return;
        }

        if (actxmiscdata.isLoggedIn(username)) {
            actxchatutils.sendWarning(sender, "commands.login.alreadyLoggedIn");
            return;
        }

        if (actxmiscdata.checkPassword(username, args[0])) {
            actxmiscdata.setLoggedIn(username, true);
            actxchatutils.sendSuccess(sender, "commands.login.success");
        } else {
            actxchatutils.sendError(sender, "commands.login.wrongPassword");
        }
    }
}