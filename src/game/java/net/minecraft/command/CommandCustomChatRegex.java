package net.minecraft.command;

import java.util.List;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.util.BlockPos;

public class CommandCustomChatRegex extends CommandBase {

    private static final String[] placekeys = {
            "commands.customchatregex.help.placeholders.prefix",
            "commands.customchatregex.help.placeholders.suffix",
            "commands.customchatregex.help.placeholders.player",
            "commands.customchatregex.help.placeholders.displayname",
            "commands.customchatregex.help.placeholders.message",
            "commands.customchatregex.help.placeholders.biome",
            "commands.customchatregex.help.placeholders.world",
            "commands.customchatregex.help.placeholders.gamemode",
            "commands.customchatregex.help.placeholders.time",
    };
    private static final int pp_page = 5;
    private static final int pp_count =
            (placekeys.length + pp_page - 1) / pp_page;
    private static final int total_pages4_help = pp_count + 1; // +1 = final usage/example/current page

    @Override
    public String getCommandName() {
        return "customchatregex";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return StatCollector.translateToLocal("commands.customchatregex.usage");
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
            actxmiscdata.setChatFormat(actxmiscdata.default_chat);
            actxchatutils.sendSuccess(sender, "commands.customchatregex.reset");
            return;
        }

        String fmt = buildString(args, 0).replace('\u00a7', '&').replace("&", "\u00a7");
        String fmtLower = fmt.toLowerCase();

        boolean hasPlayerPlaceholder = fmtLower.contains("%player%")
                || fmtLower.contains("%displayname%")
                || fmtLower.contains("%display_name%");
        if (!hasPlayerPlaceholder) {
            actxchatutils.sendError(sender, "commands.customchatregex.error.missingPlayer");
            return;
        }
        if (!fmtLower.contains("%message%")) {
            actxchatutils.sendError(sender, "commands.customchatregex.error.missingMessage");
            return;
        }

        actxmiscdata.setChatFormat(fmt);
        actxchatutils.sendSuccess(sender, "commands.customchatregex.success", fmt);
    }

    private void sendHelpPage(ICommandSender sender, int page) {
        actxchatutils.sendSuccess(sender, "commands.customchatregex.hoverHelp", page, total_pages4_help);

        if (page <= pp_count) {
            actxchatutils.sendTranslated(sender, EnumChatFormatting.DARK_GREEN, "commands.customchatregex.help.placeholders.header");
            int start = (page - 1) * pp_page;
            int end = Math.min(start + pp_page, placekeys.length);
            for (int i = start; i < end; i++) {
                actxchatutils.sendTranslated(sender, EnumChatFormatting.DARK_GREEN, placekeys[i]);
            }
        } else {
            actxchatutils.sendSuccess(sender, "commands.customchatregex.usage");
            actxchatutils.sendSuccess(sender, "commands.customchatregex.help.example");
            actxchatutils.sendSuccess(sender, "commands.customchatregex.help.current", actxmiscdata.getChatFormat());
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