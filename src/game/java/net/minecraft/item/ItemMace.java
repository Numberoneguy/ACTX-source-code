package net.minecraft.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class ItemMace extends ItemSword {
	public ItemMace(Item.ToolMaterial material) {
		super(material);
	}

	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.NONE;
	}

	public int getMaxItemUseDuration(ItemStack stack) {
		return 0;
	}

	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
		return itemStackIn;
	}
}
