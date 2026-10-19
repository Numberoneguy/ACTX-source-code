package net.minecraft.item;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntitySelectors;
import net.minecraft.world.World;

public class ItemElytra extends Item {
	private static final int chest_position = 3;

	private static final IBehaviorDispenseItem dispenserBehavior = new BehaviorDefaultDispenseItem() {
		protected ItemStack dispenseStack(IBlockSource iblocksource, ItemStack itemstack) {
			BlockPos blockpos = iblocksource.getBlockPos()
					.offset(BlockDispenser.getFacing(iblocksource.getBlockMetadata()));
			int i = blockpos.getX();
			int j = blockpos.getY();
			int k = blockpos.getZ();
			AxisAlignedBB axisalignedbb = new AxisAlignedBB((double) i, (double) j, (double) k, (double) (i + 1),
					(double) (j + 1), (double) (k + 1));
			List list = iblocksource.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb,
					Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
						public boolean apply(Entity entity) {
							if (!entity.isEntityAlive() || !(entity instanceof EntityLivingBase)) {
								return false;
							}
							EntityLivingBase living = (EntityLivingBase) entity;
							if (living.getEquipmentInSlot(chest_position) != null) {
								return false;
							}
							if (living instanceof EntityLiving) {
								return ((EntityLiving) living).canPickUpLoot();
							}
							return living instanceof EntityArmorStand || living instanceof EntityPlayer;
						}
					}));
			if (list.size() > 0) {
				EntityLivingBase target = (EntityLivingBase) list.get(0);
				ItemStack equipped = itemstack.copy();
				equipped.stackSize = 1;
				if (target instanceof EntityPlayer) {
					((EntityPlayer) target).inventory.armorInventory[chest_position - 1] = equipped;
				} else {
					target.setCurrentItemOrArmor(chest_position, equipped);
				}
				if (target instanceof EntityLiving) {
					((EntityLiving) target).setEquipmentDropChance(chest_position, 2.0F);
				}

				--itemstack.stackSize;
				return itemstack;
			} else {
				return super.dispenseStack(iblocksource, itemstack);
			}
		}
	};

	public ItemElytra() {
		this.setMaxStackSize(1);
		this.setMaxDamage(432);
		this.setCreativeTab(CreativeTabs.tabCombat);
		BlockDispenser.dispenseBehaviorRegistry.putObject(this, dispenserBehavior);
	}

	public static boolean isUsable(ItemStack stack) {
		return stack.getItemDamage() < stack.getMaxDamage() - 1;
	}

	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return repair.getItem() == Items.leather;
	}

	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
		if (entityplayer.inventory.armorInventory[chest_position - 1] == null) {
			entityplayer.inventory.armorInventory[chest_position - 1] = itemstack.copy();
			itemstack.stackSize = 0;
		}

		return itemstack;
	}
}