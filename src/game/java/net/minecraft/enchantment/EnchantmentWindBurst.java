package net.minecraft.enchantment;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class EnchantmentWindBurst extends Enchantment {

	private static final float[] LAUNCH_VELOCITY = { 1.099F, 1.491F, 1.757F };
	private static final float[] LAUNCH_HEIGHT = { 7.0F, 12.0F, 16.0F };

	public EnchantmentWindBurst(int enchID, ResourceLocation enchName, int enchWeight) {
		super(enchID, enchName, enchWeight, EnumEnchantmentType.WEAPON);
		this.setName("wind_burst");
	}

	public int getMinEnchantability(int level) {
		return 15 + (level - 1) * 9;
	}

	public int getMaxEnchantability(int level) {
		return this.getMinEnchantability(level) + 15;
	}

	public int getMaxLevel() {
		return 3;
	}

	public boolean canApply(ItemStack stack) {
		return EnchantmentDensity.isMace(stack.getItem());
	}

	/** Upward motionY after a smash attack (tuned to 1.8 gravity/drag). */
	public static float getLaunchVelocity(int level) {
		return LAUNCH_VELOCITY[Math.min(Math.max(level, 1), LAUNCH_VELOCITY.length) - 1];
	}

	/** Approximate rise in blocks for the launch, used to cancel the fall back down. */
	public static float getLaunchHeight(int level) {
		return LAUNCH_HEIGHT[Math.min(Math.max(level, 1), LAUNCH_HEIGHT.length) - 1];
	}
}
