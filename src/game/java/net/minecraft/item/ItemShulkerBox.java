package net.minecraft.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

public class ItemShulkerBox extends ItemBlock {
	public ItemShulkerBox(Block block) {
		super(block);
		this.setMaxStackSize(1);
	}

	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("BlockEntityTag", 10)) {
			NBTTagCompound entityTag = tag.getCompoundTag("BlockEntityTag");
			if (entityTag.hasKey("Items", 9)) {
				NBTTagList list = entityTag.getTagList("Items", 10);
				int shown = 0;
				int total = 0;
				for (int i = 0; i < list.tagCount(); ++i) {
					ItemStack itemstack = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
					if (itemstack != null) {
						++total;
						if (shown <= 4) {
							++shown;
							tooltip.add(StatCollector.translateToLocalFormatted("container.shulkerBox.itemCount",
									new Object[] { itemstack.getDisplayName(), Integer.valueOf(itemstack.stackSize) }));
						}
					}
				}

				if (total - shown > 0) {
					tooltip.add(EnumChatFormatting.ITALIC
							+ StatCollector.translateToLocalFormatted("container.shulkerBox.more",
									new Object[] { Integer.valueOf(total - shown) }));
				}
			}
		}
	}
}