package net.minecraft.command;

import java.util.Arrays;
import java.util.List;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.util.StatCollector;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommandChangePassword extends CommandBase {

    @Override
    public String getCommandName() {
        return "changepassword";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("changepass");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return StatCollector.translateToLocal("commands.changepassword.usage");
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

        if (args.length < 2) {
            actxchatutils.sendError(sender, getCommandUsage(sender));
            return;
        }

        String username = sender.getName();
        actxmiscdata.ensureLoaded();

        if (!actxmiscdata.checkPassword(username, args[0])) {
            actxchatutils.sendError(sender, "commands.changepassword.wrongOld");
            return;
        }

        actxmiscdata.registerUser(username, args[1]);
        actxchatutils.sendSuccess(sender, "commands.changepassword.success");
    }
}