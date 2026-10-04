package net.minecraft.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lax1dude.eaglercraft.v1_8.EaglercraftUUID;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.util.StatCollector;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class CommandIgnore extends CommandBase {

    public static final Map<EaglercraftUUID, Set<String>> ignored_players = new HashMap<>();

    @Override
    public String getCommandName() {
        return "ignore";
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
    public String getCommandUsage(ICommandSender sender) {
        return StatCollector.translateToLocal("commands.ignore.usage");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            actxchatutils.sendError(sender, getCommandUsage(sender));
            return;
        }

        if (!(sender instanceof EntityPlayerMP)) {
            actxchatutils.sendError(sender, "commands.ignore.playersOnly");
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) sender;
        String targetName = args[0].toLowerCase();

        if (targetName.equalsIgnoreCase(player.getName())) {
            actxchatutils.sendError(sender, "commands.ignore.self");
            return;
        }

        Set<String> ignored = ignored_players.computeIfAbsent(player.getUniqueID(), k -> new HashSet<>());

        if (ignored.contains(targetName)) {
            ignored.remove(targetName);
            actxchatutils.sendSuccess(sender, "commands.ignore.unignored", args[0]);
        } else {
            ignored.add(targetName);
            actxchatutils.sendSuccess(sender, "commands.ignore.success", args[0]);
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
    }
}