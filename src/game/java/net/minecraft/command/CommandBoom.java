package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import net.minecraft.actx.utils.actxchatutils;
import net.minecraft.util.StatCollector;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class CommandBoom extends CommandBase {

    @Override
    public String getCommandName() {
        return "boom";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return StatCollector.translateToLocal("commands.boom.usage");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        if (sender instanceof CommandBlockLogic) {
            return false;
        }
        return super.canCommandSenderUseCommand(sender);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof CommandBlockLogic) {
            throw new CommandException("commands.generic.permission");
        }

        List<Entity> targetEntities;

        if (args.length > 0) {
            targetEntities = func_175763_c(sender, args[0]);
        } else {
            targetEntities = Collections.singletonList(getCommandSenderAsPlayer(sender));
        }

        if (targetEntities.isEmpty()) {
            throw new PlayerNotFoundException("commands.generic.player.notFound", args.length > 0 ? args[0] : sender.getName());
        }

        int radius = 2;

        for (Entity targetEntity : targetEntities) {
            World world = targetEntity.worldObj;
            double targetX = targetEntity.posX;
            double targetY = targetEntity.posY;
            double targetZ = targetEntity.posZ;

            EntityLivingBase igniter = (targetEntity instanceof EntityLivingBase) ? (EntityLivingBase) targetEntity : null;

            for (int y = 0; y < 3; y++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        EntityTNTPrimed tnt = new EntityTNTPrimed(
                            world,
                            targetX + x,
                            targetY + y,
                            targetZ + z,
                            igniter
                        );
                        
                        tnt.fuse = 0;
                        world.spawnEntityInWorld(tnt);
                    }
                }
            }

            if (targetEntity instanceof ICommandSender) {
                actxchatutils.sendError((ICommandSender) targetEntity, "commands.boom.getboomed");
            }
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        }
        return null;
    }
}