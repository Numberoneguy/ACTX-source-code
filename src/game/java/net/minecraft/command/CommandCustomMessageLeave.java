package net.minecraft.command;

import java.util.List;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.util.BlockPos;

public class CommandCustomMessageLeave extends CommandBase {

    private static final String[] placekeys = {
            "commands.custommessageleave.help.placeholders.prefix",
            "commands.custommessageleave.help.placeholders.suffix",
            "commands.custommessageleave.help.placeholders.player",
            "commands.custommessageleave.help.placeholders.displayname",
    };
    private static final int pp_page = 5;
    private static final int pp_count =
            (placekeys.length + pp_page - 1) / pp_page;
    private static final int total_pages4_help = pp_count + 1;

    @Override
    public String getCommandName() {
        return "custommessageleave";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return StatCollector.translateToLocal("commands.custommessageleave.usage");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        actxmiscdata.ensureLoaded();

        if (args.length >= 1 && ("help".equalsIgnoreCase(args[0]) || "?".equalsIgnoreCase(args[0]))) {
            int page = args.length >= 2 ? parseInt(args[1], 1, total_pages4_help) : 1;
            sendHelpPage(sender, page);
            return;
        }

        if (args.length == 1 && "reset".equalsIgnoreCase(args[0])) {
            actxmiscdata.setLeaveMessage(actxmiscdata.default_leave);
            actxchatutils.sendSuccess(sender, "commands.custommessageleave.reset");
            return;
        }
        String msg = buildString(args, 0).replace('\u00a7', '&').replace("&", "\u00a7");
        String msgLower = msg.toLowerCase();
        // hasPP lolololololololololololololololololoolololol
        boolean hasPlayerPlaceholder = msgLower.contains("%player%")
                || msgLower.contains("%displayname%")
                || msgLower.contains("%display_name%");
        if (!hasPlayerPlaceholder) {
            actxchatutils.sendError(sender, "commands.custommessageleave.error.missingPlayer");
            return;
        }

        actxmiscdata.setLeaveMessage(msg);
        actxchatutils.sendSuccess(sender, "commands.custommessageleave.success", msg);
    }

    private void sendHelpPage(ICommandSender sender, int page) {
        actxchatutils.sendSuccess(sender, "commands.custommessageleave.hoverHelp", page, total_pages4_help);

        if (page <= pp_count) {
            actxchatutils.sendTranslated(sender, EnumChatFormatting.DARK_GREEN, "commands.custommessageleave.help.placeholders.header");
            int start = (page - 1) * pp_page;
            int end = Math.min(start + pp_page, placekeys.length);
            for (int i = start; i < end; i++) {
                actxchatutils.sendTranslated(sender, EnumChatFormatting.DARK_GREEN, placekeys[i]);
            }
        } else {
            actxchatutils.sendSuccess(sender, "commands.custommessageleave.usage");
            actxchatutils.sendSuccess(sender, "commands.custommessageleave.help.example");
            actxchatutils.sendSuccess(sender, "commands.custommessageleave.help.current", actxmiscdata.getLeaveMessage());
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "reset", "help");
        }
        if (args.length == 2 && "help".equalsIgnoreCase(args[0])) {
            String[] pages = new String[total_pages4_help];
            for (int i = 0; i < total_pages4_help; i++) pages[i] = String.valueOf(i + 1);
            return getListOfStringsMatchingLastWord(args, pages);
        }
        return null;
    }
}