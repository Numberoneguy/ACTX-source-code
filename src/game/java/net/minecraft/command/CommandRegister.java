package net.minecraft.command;

import java.util.Arrays;
import java.util.List;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.util.StatCollector;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommandRegister extends CommandBase {

    @Override
    public String getCommandName() {
        return "register";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("reg");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return StatCollector.translateToLocal("commands.register.usage");
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

        if (actxmiscdata.isRegistered(username)) {
            actxchatutils.sendError(sender, "commands.register.alreadyRegistered");
            return;
        }

        String password = args[0];
        if (args.length >= 2 && !password.equals(args[1])) {
            actxchatutils.sendError(sender, "commands.register.passwordMismatch");
            return;
        }

        String uuid = sender instanceof EntityPlayerMP ? ((EntityPlayerMP) sender).getUniqueID().toString() : null;
        actxmiscdata.registerUser(username, uuid, password);
        actxmiscdata.setLoggedIn(username, true);
        actxchatutils.sendSuccess(sender, "commands.register.success");
    }
}