package net.minecraft.command;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StatCollector;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;

public class CommandPardon extends CommandBase {

    @Override
    public String getCommandName() { 
        return "pardon"; 
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("unban");
        return aliases;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) { 
        return StatCollector.translateToLocal("commands.unban.usage"); 
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

        ServerConfigurationManager scm = MinecraftServer.getServer().getConfigurationManager();
        actxmiscdata.ensureLoaded(scm);
        String targetName = args[0];
        String lowerCaseName = targetName.toLowerCase();

        if (!actxmiscdata.session_bans.contains(lowerCaseName)) {
            actxchatutils.sendWarning(sender, "commands.unban.failed");
            return;
        }

        actxmiscdata.session_bans.remove(lowerCaseName);
        actxmiscdata.save(scm);

        notifyOperators(sender, this, "commands.unban.success", new Object[] { targetName });
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            String[] bannedPlayers = actxmiscdata.session_bans.toArray(new String[0]);
            return getListOfStringsMatchingLastWord(args, bannedPlayers);
        }
        return null;
    }
}