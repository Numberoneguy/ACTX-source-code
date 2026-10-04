package net.minecraft.entity.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import java.util.List;

public class ItemMergeManager {

    public static void doCustomItemMerge(EntityItem item) {
        if (!item.isEntityAlive() || item.getEntityItem() == null) {
            return;
        }

        ItemStack itemStack = item.getEntityItem();
        if (itemStack.stackSize >= itemStack.getMaxStackSize()) {
            return;
        }

        double radius = 5.0D;
        AxisAlignedBB searchBox = new AxisAlignedBB(
                item.posX - radius, item.posY - radius, item.posZ - radius,
                item.posX + radius, item.posY + radius, item.posZ + radius
        );

        List<EntityItem> nearbyItems = item.worldObj.getEntitiesWithinAABB(EntityItem.class, searchBox);

        for (EntityItem otherItem : nearbyItems) {
            if (otherItem == item) {
                continue;
            }

            if (otherItem.isEntityAlive() && otherItem.getEntityItem() != null) {
                attemptMerge(item, otherItem);
            }

            if (itemStack.stackSize >= itemStack.getMaxStackSize()) {
                break;
            }
        }
    }

    private static void attemptMerge(EntityItem mainItem, EntityItem otherItem) {
        ItemStack mainStack = mainItem.getEntityItem();
        ItemStack otherStack = otherItem.getEntityItem();

        if (otherStack.getItem() != mainStack.getItem()) {
            return;
        }

        if (otherStack.getHasSubtypes() && (otherStack.getMetadata() != mainStack.getMetadata())) {
            return;
        }

        if (mainStack.hasTagCompound() && !mainStack.getTagCompound().equals(otherStack.getTagCompound())) {
            return;
        }
        if (otherStack.hasTagCompound() && !mainStack.hasTagCompound()) {
            return;
        }

        int maxCanInput = mainStack.getMaxStackSize() - mainStack.stackSize;

        if (otherStack.stackSize <= maxCanInput) {
            mainStack.stackSize += otherStack.stackSize;
            mainItem.setEntityItemStack(mainStack);
            otherItem.setDead();
        } else {
            otherStack.stackSize -= maxCanInput;
            otherItem.setEntityItemStack(otherStack);
            mainStack.stackSize = mainStack.getMaxStackSize();
            mainItem.setEntityItemStack(mainStack);
        }
    }
}
