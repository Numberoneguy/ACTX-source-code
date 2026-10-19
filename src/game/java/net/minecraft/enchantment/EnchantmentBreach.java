package net.minecraft.enchantment;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class EnchantmentBreach extends Enchantment {

	public EnchantmentBreach(int enchID, ResourceLocation enchName, int enchWeight) {
		super(enchID, enchName, enchWeight, EnumEnchantmentType.WEAPON);
		this.setName("breach");
	}

	public int getMinEnchantability(int level) {
		return 15 + (level - 1) * 9;
	}

	public int getMaxEnchantability(int level) {
		return this.getMinEnchantability(level) + 20;
	}

	public int getMaxLevel() {
		return 4;
	}

	public boolean canApplyTogether(Enchantment other) {
		return !(other instanceof EnchantmentDamage) && !(other instanceof EnchantmentDensity)
				&& !(other instanceof EnchantmentBreach) && super.canApplyTogether(other);
	}

	public boolean canApply(ItemStack stack) {
		return EnchantmentDensity.isMace(stack.getItem());
	}

	/** Multiplier applied to the target's armor value: 15% less per level. */
	public static float getArmorFactor(int level) {
		return level > 0 ? Math.max(0.0F, 1.0F - 0.15F * level) : 1.0F;
	}
}
