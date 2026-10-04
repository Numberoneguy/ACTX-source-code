package net.minecraft.command;

import java.util.Arrays;
import java.util.List;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.util.StatCollector;
import net.minecraft.util.BlockPos;

public class CommandPassTitle extends CommandBase {

    @Override
    public String getCommandName() {
        return "passtitle";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("authtitle");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return StatCollector.translateToLocal("commands.passtitle.usage");
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

        String type = args[0].toLowerCase();

        actxmiscdata.ensureLoaded();

        if ("reset".equals(type)) {
            actxmiscdata.resetAuthTitles();
            actxchatutils.sendSuccess(sender, "commands.passtitle.reset");
            return;
        }

        if (args.length < 2) {
            actxchatutils.sendError(sender, getCommandUsage(sender));
            return;
        }

        String rawJson = buildString(args, 1).replace('&', '\u00a7');

        if ("title".equals(type) || "subtitle".equals(type)) {
            if ("title".equals(type)) {
                actxmiscdata.setAuthTitle(rawJson);
            } else {
                actxmiscdata.setAuthSubtitle(rawJson);
            }
            actxchatutils.sendSuccess(sender, "commands.passtitle.success");
        } else {
            actxchatutils.sendError(sender, getCommandUsage(sender));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "title", "subtitle", "reset") : null;
    }
}