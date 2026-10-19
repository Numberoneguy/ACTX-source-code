package net.minecraft.item;

import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemEndCrystal extends Item {
	public ItemEndCrystal() {
		this.setUnlocalizedName("end_crystal");
		this.setCreativeTab(CreativeTabs.tabCombat);
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		IBlockState iblockstate = worldIn.getBlockState(pos);

		if (iblockstate.getBlock() != Blocks.obsidian && iblockstate.getBlock() != Blocks.bedrock) {
			return false;
		}

		BlockPos blockpos = pos.up();

		if (!playerIn.canPlayerEdit(blockpos, side, stack)) {
			return false;
		}

		BlockPos blockpos1 = blockpos.up();
		boolean blocked = !worldIn.isAirBlock(blockpos)
				&& !worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
		blocked = blocked | (!worldIn.isAirBlock(blockpos1)
				&& !worldIn.getBlockState(blockpos1).getBlock().isReplaceable(worldIn, blockpos1));

		if (blocked) {
			return false;
		}

		double d0 = (double) blockpos.getX();
		double d1 = (double) blockpos.getY();
		double d2 = (double) blockpos.getZ();
		List<Entity> list = worldIn.getEntitiesWithinAABBExcludingEntity((Entity) null,
				AxisAlignedBB.fromBounds(d0, d1, d2, d0 + 1.0D, d1 + 2.0D, d2 + 1.0D));

		if (!list.isEmpty()) {
			return false;
		}

		if (!worldIn.isRemote) {
			EntityEnderCrystal entityendercrystal = new EntityEnderCrystal(worldIn,
					(double) ((float) pos.getX() + 0.5F), (double) (pos.getY() + 1),
					(double) ((float) pos.getZ() + 0.5F));
			worldIn.spawnEntityInWorld(entityendercrystal);
		}

		--stack.stackSize;
		return true;
	}

	public boolean hasEffect(ItemStack stack) {
		return true;
	}
}