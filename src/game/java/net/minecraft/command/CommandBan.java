package net.minecraft.command;

import java.util.List;
import net.lax1dude.eaglercraft.v1_8.sp.ipc.IPCPacket1FBanPlayer;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;

public class CommandBan extends CommandBase {

    @Override
    public String getCommandName() {
        return "ban";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return StatCollector.translateToLocal("commands.ban.usage");
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

        MinecraftServer server = MinecraftServer.getServer();
        ServerConfigurationManager scm = server.getConfigurationManager();
        actxmiscdata.ensureLoaded(scm);

        String reason = args.length > 1 ? buildString(args, 1)
                : StatCollector.translateToLocal("commands.ban.reason.default");

        String input = args[0];

        if (input.indexOf('*') >= 0 || input.indexOf('?') >= 0) {
            banWildcard(sender, server, scm, input, reason);
            return;
        }

        String targetName = input;
        EntityPlayerMP targetPlayer = scm.getPlayerByUsername(targetName);
        if (targetPlayer != null) {
            targetName = targetPlayer.getName();
        }

        if (targetName.equalsIgnoreCase(sender.getName())) {
            actxchatutils.sendWarning(sender, "commands.ban.cannotBanSelf");
            return;
        }

        String serverOwner = server.getServerOwner();
        if (serverOwner != null && targetName.equalsIgnoreCase(serverOwner)) {
            actxchatutils.sendWarning(sender, "commands.ban.cannotBanOwner");
            return;
        }

        if (actxmiscdata.session_bans.contains(targetName.toLowerCase())) {
            actxchatutils.sendWarning(sender, "commands.ban.failed", targetName);
            return;
        }

        applyBan(scm, targetName, targetPlayer, reason);
        notifyOperators(sender, this, "commands.ban.success", targetName, reason);
    }

    private void banWildcard(ICommandSender sender, MinecraftServer server, ServerConfigurationManager scm,
            String pattern, String reason) {
        // Refuse patterns like "*" or "?*" that would ban everyone.
        int literalChars = 0;
        for (int i = 0; i < pattern.length(); ++i) {
            char c = pattern.charAt(i);
            if (c != '*' && c != '?') {
                ++literalChars;
            }
        }
        if (literalChars < 2) {
            actxchatutils.sendWarning(sender, "commands.ban.wildcard.plain");
            return;
        }

        String serverOwner = server.getServerOwner();
        int kicked = 0;

        // getAllUsernames() returns a copy, so kicking while iterating is safe
        String[] online = scm.getAllUsernames();
        for (int i = 0; i < online.length; ++i) {
            String name = online[i];
            if (!globglob(pattern, name)) {
                continue;
            }
            if (name.equalsIgnoreCase(sender.getName())) {
                continue;
            }
            if (serverOwner != null && name.equalsIgnoreCase(serverOwner)) {
                continue;
            }

            EntityPlayerMP p = scm.getPlayerByUsername(name);
            if (p != null) {
                p.playerNetServerHandler.kickPlayerFromServer(reason);
                ++kicked;
            }
        }

        // Store the pattern itself so joins are blocked too if they match the pattern.
        if (actxmiscdata.session_bans.add(pattern.toLowerCase())) {
            actxmiscdata.save(scm);
        }

        if (kicked == 0) {
            actxchatutils.sendWarning(sender, "commands.ban.wildcard.nomatch", pattern);
        } else {
            notifyOperators(sender, this, "commands.ban.wildcard.success", Integer.valueOf(kicked), pattern, reason);
        }
    }

    private void applyBan(ServerConfigurationManager scm, String name, EntityPlayerMP player, String reason) {
        IPCPacket1FBanPlayer banPkt = new IPCPacket1FBanPlayer(name, true);
        actxmiscdata.session_bans.add(banPkt.playerName.toLowerCase());
        actxmiscdata.save(scm);

        if (player != null) {
            player.playerNetServerHandler.kickPlayerFromServer(reason);
        }
    }

    public static boolean globglob(String pattern, String text) {
        pattern = pattern.toLowerCase();
        text = text.toLowerCase();
        int p = 0, t = 0, star = -1, mark = 0;
        while (t < text.length()) {
            if (p < pattern.length() && (pattern.charAt(p) == '?' || pattern.charAt(p) == text.charAt(t))) {
                ++p;
                ++t;
            } else if (p < pattern.length() && pattern.charAt(p) == '*') {
                star = p++;
                mark = t;
            } else if (star != -1) {
                p = star + 1;
                t = ++mark;
            } else {
                return false;
            }
        }
        while (p < pattern.length() && pattern.charAt(p) == '*') {
            ++p;
        }
        return p == pattern.length();
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args,
                    MinecraftServer.getServer().getConfigurationManager().getAllUsernames());
        }
        return null;
    }
}