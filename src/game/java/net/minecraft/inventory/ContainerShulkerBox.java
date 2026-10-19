package net.minecraft.inventory;

import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class ContainerShulkerBox extends Container {
	private final IInventory shulkerInventory;

	public ContainerShulkerBox(InventoryPlayer playerInventory, IInventory shulkerInventory, EntityPlayer player) {
		this.shulkerInventory = shulkerInventory;
		shulkerInventory.openInventory(player);

		for (int k = 0; k < 3; ++k) {
			for (int l = 0; l < 9; ++l) {
				this.addSlotToContainer(new SlotShulkerBox(shulkerInventory, l + k * 9, 8 + l * 18, 18 + k * 18));
			}
		}

		for (int i1 = 0; i1 < 3; ++i1) {
			for (int k1 = 0; k1 < 9; ++k1) {
				this.addSlotToContainer(new Slot(playerInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 84 + i1 * 18));
			}
		}

		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot(playerInventory, j1, 8 + j1 * 18, 142));
		}
	}

	public boolean canInteractWith(EntityPlayer playerIn) {
		return this.shulkerInventory.isUseableByPlayer(playerIn);
	}

	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index < this.shulkerInventory.getSizeInventory()) {
				if (!this.mergeItemStack(itemstack1, this.shulkerInventory.getSizeInventory(),
						this.inventorySlots.size(), true)) {
					return null;
				}
			} else {
				if (Block.getBlockFromItem(itemstack1.getItem()) instanceof BlockShulkerBox) {
					return null;
				}
				if (!this.mergeItemStack(itemstack1, 0, this.shulkerInventory.getSizeInventory(), false)) {
					return null;
				}
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		this.shulkerInventory.closeInventory(playerIn);
	}
}