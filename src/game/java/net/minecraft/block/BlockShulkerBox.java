package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockShulkerBox extends BlockContainer {
	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	private static final BlockShulkerBox[] BOXES = new BlockShulkerBox[16];
	private final EnumDyeColor color;

	public BlockShulkerBox(EnumDyeColor color) {
		super(Material.rock);
		this.color = color;
		BOXES[color.getMetadata()] = this;
		this.setCreativeTab(CreativeTabs.tabDecorations);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
	}

	public static BlockShulkerBox getBlock(EnumDyeColor color) {
		return BOXES[color.getMetadata()];
	}

	public static Block[] getAllBoxes() {
		return BOXES.clone();
	}

	public static EnumDyeColor getColorOf(Block block) {
		return block instanceof BlockShulkerBox ? ((BlockShulkerBox) block).getColor() : EnumDyeColor.PURPLE;
	}

	public static EnumDyeColor getColorOf(Item item) {
		return getColorOf(Block.getBlockFromItem(item));
	}

	public static ItemStack getItemStack(EnumDyeColor color) {
		return new ItemStack(getBlock(color));
	}

	public EnumDyeColor getColor() {
		return this.color;
	}

	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityShulkerBox(this.color);
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean isFullCube() {
		return false;
	}

	/**
	 * 2 = TESR only, the block model is never drawn
	 */
	public int getRenderType() {
		return 2;
	}

	public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		IBlockState state = worldIn.getBlockState(pos);
		if (tileentity instanceof TileEntityShulkerBox && state.getBlock() == this) {
			AxisAlignedBB bb = ((TileEntityShulkerBox) tileentity).getBoundingBox((EnumFacing) state.getValue(FACING));
			this.setBlockBounds((float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY,
					(float) bb.maxZ);
		} else {
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileEntityShulkerBox && state.getBlock() == this) {
			return ((TileEntityShulkerBox) tileentity).getBoundingBox((EnumFacing) state.getValue(FACING))
					.offset((double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
		}
		return super.getCollisionBoundingBox(worldIn, pos, state);
	}

	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		} else if (playerIn.isSpectator()) {
			return true;
		} else {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof TileEntityShulkerBox) {
				TileEntityShulkerBox box = (TileEntityShulkerBox) tileentity;
				boolean canOpen;
				if (box.getAnimationStatus() == TileEntityShulkerBox.AnimationStatus.CLOSED) {
					EnumFacing facing = (EnumFacing) state.getValue(FACING);
					AxisAlignedBB space = box.getOpeningSpace(facing);
					BlockPos adjacent = pos.offset(facing);
					IBlockState adjacentState = worldIn.getBlockState(adjacent);
					AxisAlignedBB adjacentBox = adjacentState.getBlock().getCollisionBoundingBox(worldIn, adjacent,
							adjacentState);
					canOpen = adjacentBox == null || !adjacentBox.intersectsWith(space);
				} else {
					canOpen = true;
				}

				if (canOpen) {
					playerIn.displayGUIChest((IInventory) box);
				}

				return true;
			} else {
				return false;
			}
		}
	}

	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, facing);
	}

	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[] { FACING });
	}

	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing) state.getValue(FACING)).getIndex();
	}

	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(meta));
	}

	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileEntityShulkerBox) {
			((TileEntityShulkerBox) tileentity).setDestroyedByCreativePlayer(player.capabilities.isCreativeMode);
		}
	}

	/**
	 * Drops are handled in breakBlock so the contents stay inside the item.
	 */
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
	}

	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		if (stack.hasDisplayName()) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof TileEntityShulkerBox) {
				((TileEntityShulkerBox) tileentity).setCustomName(stack.getDisplayName());
			}
		}
	}

	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileEntityShulkerBox) {
			TileEntityShulkerBox box = (TileEntityShulkerBox) tileentity;
			if (!box.isCleared() && box.shouldDrop()) {
				ItemStack itemstack = new ItemStack(Item.getItemFromBlock(this));
				NBTTagCompound tag = new NBTTagCompound();
				NBTTagCompound contents = new NBTTagCompound();
				box.saveContents(contents);
				tag.setTag("BlockEntityTag", contents);
				itemstack.setTagCompound(tag);
				if (box.hasCustomName()) {
					itemstack.setStackDisplayName(box.getName());
					box.setCustomName("");
				}

				spawnAsEntity(worldIn, pos, itemstack);
			}

			worldIn.updateComparatorOutputLevel(pos, state.getBlock());
		}

		super.breakBlock(worldIn, pos, state);
	}

	public boolean hasComparatorInputOverride() {
		return true;
	}

	public int getComparatorInputOverride(World worldIn, BlockPos pos) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity instanceof IInventory ? Container.calcRedstoneFromInventory((IInventory) tileentity) : 0;
	}
}