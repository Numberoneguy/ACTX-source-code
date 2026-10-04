package net.minecraft.client;

import net.minecraft.command.WorldEditCommand;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

public class WorldEditInit {

    /**
     * Handles mouse clicks for the WorldEdit wand.
     * @param button 0 for Left Click (Pos 1), 1 for Right Click (Pos 2)
     * @return true if the player is holding a valid wand, looking at a block, and actually pressing the click binding
     */
    public static boolean handleMouseClick(int button) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.objectMouseOver == null) {
            return false;
        }

        // FIX: Verify the keybindings are physically active to block automatic look-spamming
        if (button == 0 && !mc.gameSettings.keyBindAttack.isKeyDown()) {
            return false;
        }
        if (button == 1 && !mc.gameSettings.keyBindUseItem.isKeyDown()) {
            return false;
        }

        ItemStack heldItem = mc.thePlayer.getHeldItem();
        // Check if the player is holding the designated WorldEdit wand with the specific NBT tag
        if (!WorldEditCommand.isWand(heldItem)) {
            return false;
        }

        // Only intercept actions if the player is looking at a block
        if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos pos = mc.objectMouseOver.getBlockPos();

            if (button == 0) {
                // Left Click -> Set Position 1
                SingleplayerServerController.sendWandPos(pos, 1);
                return true;
            } else if (button == 1) {
                // Right Click -> Set Position 2
                SingleplayerServerController.sendWandPos(pos, 2);
                return true;
            }
        }

        return false;
    }
}
