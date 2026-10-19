package net.minecraft.enchantment;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class EnchantmentMending extends Enchantment {

	public EnchantmentMending(int enchID, ResourceLocation enchName, int enchWeight) {
		super(enchID, enchName, enchWeight, EnumEnchantmentType.BREAKABLE);
		this.setName("mending");
	}

	public int getMinEnchantability(int level) {
		return level * 25;
	}

	public int getMaxEnchantability(int level) {
		return this.getMinEnchantability(level) + 50;
	}

	public int getMaxLevel() {
		return 1;
	}

	public boolean canApplyTogether(Enchantment other) {
		return other != Enchantment.infinity && super.canApplyTogether(other);
	}

	/**
	 * Spends XP repairing a random damaged Mending item the player has
	 * equipped (held + armor). Returns the XP left over for the player.
	 */
	public static int repairWithXp(EntityPlayer player, int xp) {
		ItemStack stack = getRandomMendable(player);
		if (stack != null) {
			int repair = Math.min(xp * 2, stack.getItemDamage());
			stack.setItemDamage(stack.getItemDamage() - repair);
			xp -= repair / 2;
		}
		return xp;
	}

	private static ItemStack getRandomMendable(EntityPlayer player) {
		List<ItemStack> list = new ArrayList<ItemStack>();
		addIfMendable(list, player.getHeldItem());
		ItemStack[] armor = player.inventory.armorInventory;
		for (int i = 0; i < armor.length; ++i) {
			addIfMendable(list, armor[i]);
		}
		return list.isEmpty() ? null : list.get(player.getRNG().nextInt(list.size()));
	}

	private static void addIfMendable(List<ItemStack> list, ItemStack stack) {
		if (stack != null && stack.isItemDamaged()
				&& EnchantmentHelper.getEnchantmentLevel(Enchantment.mending.effectId, stack) > 0) {
			list.add(stack);
		}
	}
}
