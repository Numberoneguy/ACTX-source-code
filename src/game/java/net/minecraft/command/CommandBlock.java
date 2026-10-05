package net.minecraft.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;

public class CommandBlock extends CommandBase {

    private static final Set<String> auth_cmd_names = new HashSet<String>(Arrays.asList("login", "l", "register", "reg", "changepassword", "changepass", "unregister", "resetpassword", "passtitle", "authtitle"));

    @Override
    public String getCommandName() {
        return "block";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return StatCollector.translateToLocal("commands.block.usage");
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        MinecraftServer server = MinecraftServer.getServer();
        String serverOwner = server.getServerOwner();
        
        if (serverOwner != null && sender.getName().equalsIgnoreCase(serverOwner)) {
            return true;
        }
        
        return false;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        MinecraftServer server = MinecraftServer.getServer();
        String serverOwner = server.getServerOwner();
        if (serverOwner == null || !sender.getName().equalsIgnoreCase(serverOwner)) {
            return;
        }

        ServerConfigurationManager scm = server.getConfigurationManager();
        actxmiscdata.ensureLoaded(scm);

        if (args.length < 1) {
            actxchatutils.sendError(sender, getCommandUsage(sender));
            return;
        }

        String target = args[0].toLowerCase().trim();

        boolean forceToggle = false;
        if (target.equals("toggle")) {
            if (args.length < 2) {
                actxchatutils.sendError(sender, getCommandUsage(sender));
                return;
            }
            target = args[1].toLowerCase().trim();
            forceToggle = true;
        }

        if (target.startsWith("/")) {
            target = target.substring(1);
        }

        if (!forceToggle && target.equals("list")) {
            Set<String> blocked = actxmiscdata.getBlockedCommands();
            if (blocked.isEmpty()) {
                actxchatutils.sendWarning(sender, "commands.block.list.empty");
            } else {
                actxchatutils.sendSuccess(sender, "commands.block.list.success", blocked.toString());
            }
            return;
        }

        if (target.equals("block") || target.equals("unblock")) {
            actxchatutils.sendError(sender, "commands.block.self");
            return;
        }

        if (actxmiscdata.isCommandBlocked(target)) {
            actxmiscdata.removeBlockedCommand(target, scm);
            actxchatutils.sendSuccess(sender, "commands.block.unblocked", target);
        } else {
            actxmiscdata.addBlockedCommand(target, scm);
            actxchatutils.sendError(sender, "commands.block.blocked", target);
        }
    }

    public static boolean isCommandBlocked(String commandName, ICommandSender sender) {
        actxmiscdata.ensureLoaded();

        if (commandName.startsWith("/")) {
            commandName = commandName.substring(1);
        }
        
        String baseCommand = commandName.split(" ")[0].toLowerCase().trim();

        if (auth_cmd_names.contains(baseCommand) && !isAuthEnabled(sender)) {
            return true;
        }

        if (!actxmiscdata.isCommandBlocked(baseCommand)) {
            return false;
        }

        MinecraftServer server = MinecraftServer.getServer();
        String serverOwner = server.getServerOwner();

        if (serverOwner != null && sender != null && sender.getName().equalsIgnoreCase(serverOwner)) {
            return false;
        }

        if (sender instanceof CommandBlockLogic) {
            return true;
        }

        if (sender instanceof TileEntitySign) {
            return true;
        }

        if (sender instanceof EntityPlayer) {
            return true;
        }

        return actxmiscdata.isCommandBlocked(baseCommand);
    }

    private static boolean isAuthEnabled(ICommandSender sender) {
        if (sender == null) return true;
        try {
            return sender.getEntityWorld().getGameRules().getBoolean("doAuth");
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        MinecraftServer server = MinecraftServer.getServer();
        String serverOwner = server.getServerOwner();
        if (serverOwner == null || !sender.getName().equalsIgnoreCase(serverOwner)) {
            return null;
        }

        actxmiscdata.ensureLoaded();

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();

            if ("list".startsWith(input)) {
                completions.add("list");
            }
            if ("toggle".startsWith(input)) {
                completions.add("toggle");
            }

            for (Object obj : MinecraftServer.getServer().getCommandManager().getCommands().keySet()) {
                String cmdName = (String) obj;
                if (cmdName.startsWith(input) && !cmdName.equals("block") && !completions.contains(cmdName)) {
                    completions.add(markBlocked(cmdName));
                }
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            List<String> completions = new ArrayList<>();
            String input = args[1].toLowerCase();
            for (Object obj : MinecraftServer.getServer().getCommandManager().getCommands().keySet()) {
                String cmdName = (String) obj;
                if (cmdName.startsWith(input) && !cmdName.equals("block")) {
                    completions.add(markBlocked(cmdName));
                }
            }
            return completions;
        }
        return null;
    }

    private static String markBlocked(String cmdName) {
        return actxmiscdata.isCommandBlocked(cmdName) ? "\u00a7c" + cmdName : cmdName;
    }
}