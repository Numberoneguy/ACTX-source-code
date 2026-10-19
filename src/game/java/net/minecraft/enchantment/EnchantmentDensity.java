package net.minecraft.enchantment;

import net.minecraft.item.Item;
import net.minecraft.item.ItemMace;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class EnchantmentDensity extends Enchantment {

	public EnchantmentDensity(int enchID, ResourceLocation enchName, int enchWeight) {
		super(enchID, enchName, enchWeight, EnumEnchantmentType.WEAPON);
		this.setName("density");
	}

	public int getMinEnchantability(int level) {
		return 5 + (level - 1) * 8;
	}

	public int getMaxEnchantability(int level) {
		return this.getMinEnchantability(level) + 20;
	}

	public int getMaxLevel() {
		return 5;
	}

	public boolean canApplyTogether(Enchantment other) {
		return !(other instanceof EnchantmentDamage) && !(other instanceof EnchantmentDensity)
				&& !(other instanceof EnchantmentBreach) && super.canApplyTogether(other);
	}

	public boolean canApply(ItemStack stack) {
		return isMace(stack.getItem());
	}

	public static boolean isMace(Item item) {
		return item instanceof ItemMace;
	}

	public static float getBonusDamage(int level, float fallDistance) {
		return level > 0 ? level * 0.5F * fallDistance : 0.0F;
	}
}