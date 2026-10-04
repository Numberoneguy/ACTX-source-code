package net.minecraft.command;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.IAdminCommand;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.CommandBase;
import net.minecraft.command.server.CommandAchievement;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.command.server.CommandBroadcast;
import net.minecraft.command.server.CommandEmote;
import net.minecraft.command.server.CommandListPlayers;
import net.minecraft.command.server.CommandMessage;
import net.minecraft.command.server.CommandMessageRaw;
import net.minecraft.command.server.CommandScoreboard;
import net.minecraft.command.server.CommandSetBlock;
import net.minecraft.command.server.CommandSetDefaultSpawnpoint;
import net.minecraft.command.server.CommandSummon;
import net.minecraft.command.server.CommandTeleport;
import net.minecraft.command.server.CommandTestForBlock;
import net.minecraft.command.server.CommandTestFor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.List;

import net.lax1dude.eaglercraft.v1_8.sp.server.ClientCommandDummy;

public class ServerCommandManager extends CommandHandler implements IAdminCommand {

    public ServerCommandManager() {
        this.registerCommand(new WorldEditCommand.CommandWorldEditWand());
        this.registerCommand(new WorldEditCommand.CommandWorldEditPos1());
        this.registerCommand(new WorldEditCommand.CommandWorldEditPos2());
        this.registerCommand(new WorldEditCommand.CommandWorldEditDesel());
        this.registerCommand(new WorldEditCommand.CommandWorldEditSet());
        this.registerCommand(new WorldEditCommand.CommandWorldEditWalls());
        this.registerCommand(new WorldEditCommand.CommandWorldEditReplaceNear());
        this.registerCommand(new WorldEditCommand.CommandWorldEditCopy());
        this.registerCommand(new WorldEditCommand.CommandWorldEditCut());
        this.registerCommand(new WorldEditCommand.CommandWorldEditPaste());
        this.registerCommand(new WorldEditCommand.CommandWorldEditRotate());
        this.registerCommand(new WorldEditCommand.CommandWorldEditFlip());
        this.registerCommand(new WorldEditCommand.CommandWorldEditUndo());
        this.registerCommand(new WorldEditCommand.CommandWorldEditRedo());
        this.registerCommand(new WorldEditCommand.CommandWorldEditClearHistory());
        this.registerCommand(new WorldEditCommand.CommandWorldEditBrush());
        this.registerCommand(new WorldEditCommand.CommandWorldEditMask());
        this.registerCommand(new WorldEditCommand.CommandWorldEditFill());
        this.registerCommand(new WorldEditCommand.CommandWorldEditUp());
        this.registerCommand(new WorldEditCommand.CommandWorldEditThru());
        this.registerCommand(new WorldEditCommand.CommandWorldEditUnstuck());
        this.registerCommand(new WorldEditCommand.CommandWorldEditExpand());
        this.registerCommand(new WorldEditCommand.CommandWorldEditContract());
        this.registerCommand(new WorldEditCommand.CommandWorldEditInset());
        this.registerCommand(new WorldEditCommand.CommandWorldEditOutset());
        this.registerCommand(new WorldEditCommand.CommandWorldEditReplace());
        this.registerCommand(new WorldEditCommand.CommandWorldEditPyramid());
        this.registerCommand(new WorldEditCommand.CommandWorldEditCyl());
        this.registerCommand(new WorldEditCommand.CommandWorldEditSphere());
        this.registerCommand(new WorldEditCommand.CommandWorldEditHPos1());
        this.registerCommand(new WorldEditCommand.CommandWorldEditHPos2());
        this.registerCommand(new WorldEditCommand.CommandWorldEditChunk());
        this.registerCommand(new WorldEditCommand.CommandWorldEditFaces());
        this.registerCommand(new WorldEditCommand.CommandWorldEditSmooth());
        this.registerCommand(new WorldEditCommand.CommandWorldEditRegen());
        this.registerCommand(new WorldEditCommand.CommandWorldEditMove());
        this.registerCommand(new WorldEditCommand.CommandWorldEditFixWater());
        this.registerCommand(new WorldEditCommand.CommandWorldEditFixLava());
        this.registerCommand(new WorldEditCommand.CommandWorldEditDrain());
        this.registerCommand(new WorldEditCommand.CommandWorldEditSnow());
        this.registerCommand(new WorldEditCommand.CommandWorldEditThaw());
        this.registerCommand(new WorldEditCommand.CommandWorldEditExtinguish());
        this.registerCommand(new CommandBlock());
        this.registerCommand(new CommandIgnore());
        this.registerCommand(new CommandMute());
        this.registerCommand(new CommandCustomMessageJoin());
        this.registerCommand(new CommandCustomMessageLeave());
        this.registerCommand(new CommandCustomChatRegex());
        this.registerCommand(new CommandTime());
        this.registerCommand(new CommandGameMode());
        this.registerCommand(new CommandDifficulty());
        this.registerCommand(new CommandDefaultGameMode());
        this.registerCommand(new CommandKill());
        this.registerCommand(new CommandWeather());
        this.registerCommand(new CommandXP());
        this.registerCommand(new CommandTeleport());
        this.registerCommand(new CommandGive());
        this.registerCommand(new CommandReplaceItem());
        this.registerCommand(new CommandStats());
        this.registerCommand(new CommandEffect());
        this.registerCommand(new CommandEnchant());
        this.registerCommand(new CommandParticle());
        this.registerCommand(new CommandEmote());
        this.registerCommand(new CommandShowSeed());
        this.registerCommand(new CommandHelp());
        this.registerCommand(new CommandMessage());
        this.registerCommand(new CommandBroadcast());
        this.registerCommand(new CommandSetSpawnpoint());
        this.registerCommand(new CommandSetDefaultSpawnpoint());
        this.registerCommand(new CommandGameRule());
        this.registerCommand(new CommandClearInventory());
        this.registerCommand(new CommandTestFor());
        this.registerCommand(new CommandSpreadPlayers());
        this.registerCommand(new CommandPlaySound());
        this.registerCommand(new CommandScoreboard());
        this.registerCommand(new CommandExecuteAt());
        this.registerCommand(new CommandTrigger());
        this.registerCommand(new CommandAchievement());
        this.registerCommand(new CommandSummon());
        this.registerCommand(new CommandSetBlock());
        this.registerCommand(new CommandFill());
        this.registerCommand(new CommandClone());
        this.registerCommand(new CommandCompare());
        this.registerCommand(new CommandBlockData());
        this.registerCommand(new CommandTestForBlock());
        this.registerCommand(new CommandMessageRaw());
        this.registerCommand(new CommandWorldBorder());
        this.registerCommand(new CommandTitle());
        this.registerCommand(new CommandEntityData());
        this.registerCommand(new CommandOp());
        this.registerCommand(new CommandDeop());
        this.registerCommand(new CommandBan());
        this.registerCommand(new CommandServerKick());
        this.registerCommand(new CommandPardon());
        this.registerCommand(new CommandListPlayers());
        this.registerCommand(new CommandSetPlayerTimeout());
        this.registerCommand(new CommandLogin());
        this.registerCommand(new CommandRegister());
        this.registerCommand(new CommandUnregister());
        this.registerCommand(new CommandResetPassword());
        this.registerCommand(new CommandPassTitle());
        this.registerCommand(new CommandChangePassword());
        this.registerCommand(new CommandBoom());
        this.registerCommand(new ClientCommandDummy("eagskull", 2, "command.skull.usage"));
        CommandBase.setAdminCommander(this);
    }

    public void notifyOperators(ICommandSender sender, ICommand command, int flags, String msgFormat,
            Object... msgParams) {
        boolean flag = true;
        MinecraftServer minecraftserver = MinecraftServer.getServer();
        if (!sender.sendCommandFeedback()) {
            flag = false;
        }

        // 1. Build a proper translation component that preserves nested IChatComponents
        IChatComponent baseMessage = new ChatComponentTranslation(msgFormat, msgParams);

        // 2. Broadcast to OTHER operators with [Sender: ...] formatting
        IChatComponent adminMessage = new ChatComponentText("[" + sender.getName() + ": ");
        adminMessage.appendSibling(baseMessage);
        adminMessage.appendText("]");
        adminMessage.getChatStyle().setColor(EnumChatFormatting.GRAY);
        adminMessage.getChatStyle().setItalic(Boolean.valueOf(true));

        if (flag) {
            List<EntityPlayerMP> players = minecraftserver.getConfigurationManager().func_181057_v();
            for (int i = 0, l = players.size(); i < l; ++i) {
                EntityPlayerMP entityplayer = players.get(i);
                if (entityplayer != sender
                        && minecraftserver.getConfigurationManager().canSendCommands(entityplayer.getGameProfile())
                        && command.canCommandSenderUseCommand(sender)) {
                    entityplayer.addChatMessage(adminMessage);
                }
            }
        }

        if (sender != minecraftserver
                && minecraftserver.worldServers[0].getGameRules().getBoolean("logAdminCommands")) {
            minecraftserver.addChatMessage(adminMessage);
        }

        boolean flag3 = minecraftserver.worldServers[0].getGameRules().getBoolean("sendCommandFeedback");
        if (sender instanceof CommandBlockLogic) {
            flag3 = ((CommandBlockLogic) sender).shouldTrackOutput();
        }

        // 3. Send clean direct feedback to the command EXECUTOR
        if ((flags & 1) != 1 && flag3 || sender instanceof MinecraftServer) {
            IChatComponent directFeedback = new ChatComponentTranslation(msgFormat, msgParams);
            directFeedback.getChatStyle().setColor(EnumChatFormatting.RESET);
            sender.addChatMessage(directFeedback);
        }
    }
}