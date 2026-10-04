package net.minecraft.client.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.command.WorldEditCommand;

public class WorldEditWandHandler {

    public static boolean isWand(ItemStack stack) {
        if (stack == null || stack.getTagCompound() == null) return false;
        return stack.getTagCompound().hasKey("worldedit_wand");
    }

    public static void onLeftClick(EntityPlayer player, BlockPos pos) {
        if (!WorldEditCommand.isEnabled(player)) return;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        String key = WorldEditCommand.playerKey(player);

        WorldEditCommand.pos1.put(key, new int[]{x, y, z});
        
        // Compute volume display if position 2 exists
        long volume = 1;
        if (WorldEditCommand.pos2.containsKey(key)) {
            int[] p2 = WorldEditCommand.pos2.get(key);
            volume = (long)(Math.abs(x - p2[0]) + 1) * (Math.abs(y - p2[1]) + 1) * (Math.abs(z - p2[2]) + 1);
        }

        WorldEditCommand.sendMsg(player, net.minecraft.util.EnumChatFormatting.GREEN + "First position set to (" + x + ", " + y + ", " + z + ") (" + volume + ").");
    }

    public static void onRightClick(EntityPlayer player, BlockPos pos) {
        if (!WorldEditCommand.isEnabled(player)) return;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        String key = WorldEditCommand.playerKey(player);

        WorldEditCommand.pos2.put(key, new int[]{x, y, z});

        // Compute volume display if position 1 exists
        long volume = 1;
        if (WorldEditCommand.pos1.containsKey(key)) {
            int[] p1 = WorldEditCommand.pos1.get(key);
            volume = (long)(Math.abs(p1[0] - x) + 1) * (Math.abs(p1[1] - y) + 1) * (Math.abs(p1[2] - z) + 1);
        }

        WorldEditCommand.sendMsg(player, net.minecraft.util.EnumChatFormatting.LIGHT_PURPLE + "Second position set to (" + x + ", " + y + ", " + z + ") (" + volume + ").");
    }
}
