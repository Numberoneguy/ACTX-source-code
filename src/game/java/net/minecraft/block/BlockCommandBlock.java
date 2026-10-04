package net.minecraft.block;

import net.lax1dude.eaglercraft.v1_8.EaglercraftRandom;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCommandBlock extends BlockContainer {
	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	public static final PropertyBool TRIGGERED = PropertyBool.create("triggered");
	public static final PropertyInteger MODE = PropertyInteger.create("mode", 0, 2);

	public BlockCommandBlock() {
		super(Material.iron, MapColor.adobeColor);
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(FACING, EnumFacing.NORTH)
				.withProperty(TRIGGERED, Boolean.valueOf(false))
				.withProperty(MODE, Integer.valueOf(0)));
	}

	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityCommandBlock();
	}

	public void onNeighborBlockChange(World world, BlockPos blockpos, IBlockState iblockstate, Block var4) {
		if (!world.isRemote) {
			TileEntity te = world.getTileEntity(blockpos);
			if (te instanceof TileEntityCommandBlock) {
				TileEntityCommandBlock cbTE = (TileEntityCommandBlock) te;
				boolean isPoweredNow = world.isBlockPowered(blockpos) || world.isBlockPowered(blockpos.up());
				boolean wasPowered = cbTE.isPowered();

				cbTE.setPowered(isPoweredNow);

				if (isPoweredNow && !wasPowered) {
					cbTE.onRedstonePulse(world);
				}
			}
		}
	}

	public boolean onBlockActivated(World world, BlockPos blockpos, IBlockState var3, EntityPlayer entityplayer,
			EnumFacing var5, float var6, float var7, float var8) {
		TileEntity tileentity = world.getTileEntity(blockpos);
		return tileentity instanceof TileEntityCommandBlock
				? ((TileEntityCommandBlock) tileentity).getCommandBlockLogic().tryOpenEditCommandBlock(entityplayer)
				: false;
	}

	public boolean hasComparatorInputOverride() {
		return true;
	}

	public int getComparatorInputOverride(World world, BlockPos blockpos) {
		TileEntity tileentity = world.getTileEntity(blockpos);
		return tileentity instanceof TileEntityCommandBlock
				? ((TileEntityCommandBlock) tileentity).getCommandBlockLogic().getSuccessCount()
				: 0;
	}

	public void onBlockPlacedBy(World world, BlockPos blockpos, IBlockState state, EntityLivingBase placer, ItemStack itemstack) {
		TileEntity tileentity = world.getTileEntity(blockpos);
		if (tileentity instanceof TileEntityCommandBlock) {
			TileEntityCommandBlock te = (TileEntityCommandBlock) tileentity;
			CommandBlockLogic commandblocklogic = te.getCommandBlockLogic();

			int mode = itemstack.getMetadata();
			te.setCBMode(mode);

			if (itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("BlockEntityTag", 10)) {
				te.readFromNBT(itemstack.getTagCompound().getCompoundTag("BlockEntityTag"));
			}

			if (itemstack.hasDisplayName()) {
				commandblocklogic.setName(itemstack.getDisplayName());
			}

			if (!world.isRemote) {
				commandblocklogic.setTrackOutput(world.getGameRules().getBoolean("sendCommandFeedback"));
			}

			EnumFacing facing = BlockPistonBase.getFacingFromEntity(world, blockpos, placer);
			world.setBlockState(blockpos, state.withProperty(FACING, facing).withProperty(MODE, Integer.valueOf(mode)), 2);
		}
	}

	public int quantityDropped(EaglercraftRandom var1) {
		return 0;
	}

	public int getRenderType() {
		return 3;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return ((Integer) state.getValue(MODE)).intValue();
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileEntityCommandBlock) {
			TileEntityCommandBlock te = (TileEntityCommandBlock) tileentity;
			return state.withProperty(MODE, Integer.valueOf(te.getCBMode()));
		}
		return state;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		// Ensures meta 0 -> mode 0, meta 1 -> mode 1 (Chain), meta 2 -> mode 2 (Repeating)
		int mode = (meta > 2) ? 0 : meta;
		return this.getDefaultState()
				.withProperty(FACING, EnumFacing.NORTH)
				.withProperty(TRIGGERED, Boolean.valueOf(false))
				.withProperty(MODE, Integer.valueOf(mode));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((Integer) state.getValue(MODE)).intValue();
	}

	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[] { FACING, TRIGGERED, MODE });
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState()
				.withProperty(FACING, BlockPistonBase.getFacingFromEntity(world, pos, placer))
				.withProperty(TRIGGERED, Boolean.valueOf(false))
				.withProperty(MODE, Integer.valueOf(meta));
	}
}