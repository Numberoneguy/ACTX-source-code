package net.minecraft.tileentity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerShulkerBox;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class TileEntityShulkerBox extends TileEntityLockable implements ITickable, ISidedInventory {
	private static final int[] SLOTS = new int[27];
	private ItemStack[] contents = new ItemStack[27];
	private boolean cleared;
	private int openCount;
	private TileEntityShulkerBox.AnimationStatus animationStatus = TileEntityShulkerBox.AnimationStatus.CLOSED;
	private float progress;
	private float progressOld;
	private EnumDyeColor color;
	private boolean destroyedByCreativePlayer;
	private String customName;

	static {
		for (int i = 0; i < SLOTS.length; ++i) {
			SLOTS[i] = i;
		}
	}

	public TileEntityShulkerBox() {
		this((EnumDyeColor) null);
	}

	public TileEntityShulkerBox(EnumDyeColor color) {
		this.color = color;
	}

	public void update() {
		this.updateAnimation();
		if (this.animationStatus == TileEntityShulkerBox.AnimationStatus.OPENING
				|| this.animationStatus == TileEntityShulkerBox.AnimationStatus.CLOSING) {
			this.moveCollidedEntities();
		}
	}

	protected void updateAnimation() {
		this.progressOld = this.progress;
		switch (this.animationStatus) {
		case CLOSED:
			this.progress = 0.0F;
			break;
		case OPENING:
			this.progress += 0.1F;
			if (this.progress >= 1.0F) {
				this.moveCollidedEntities();
				this.animationStatus = TileEntityShulkerBox.AnimationStatus.OPENED;
				this.progress = 1.0F;
			}
			break;
		case CLOSING:
			this.progress -= 0.1F;
			if (this.progress <= 0.0F) {
				this.animationStatus = TileEntityShulkerBox.AnimationStatus.CLOSED;
				this.progress = 0.0F;
			}
			break;
		case OPENED:
			this.progress = 1.0F;
		}
	}

	public TileEntityShulkerBox.AnimationStatus getAnimationStatus() {
		return this.animationStatus;
	}

	/**
	 * Local-space bounds (0..1) extended by the lid opening.
	 */
	public AxisAlignedBB getBoundingBox(EnumFacing facing) {
		float ext = 0.5F * this.getProgress(1.0F);
		return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D).addCoord(
				(double) (ext * (float) facing.getFrontOffsetX()), (double) (ext * (float) facing.getFrontOffsetY()),
				(double) (ext * (float) facing.getFrontOffsetZ()));
	}

	/**
	 * World-space slab that the lid occupies beyond the cube, at the given
	 * thickness (0.5 = fully open).
	 */
	private AxisAlignedBB getSlab(EnumFacing facing, double thickness) {
		double x1 = 0.0D, y1 = 0.0D, z1 = 0.0D, x2 = 1.0D, y2 = 1.0D, z2 = 1.0D;
		switch (facing) {
		case UP:
			y1 = 1.0D;
			y2 = 1.0D + thickness;
			break;
		case DOWN:
			y2 = 0.0D;
			y1 = -thickness;
			break;
		case NORTH:
			z2 = 0.0D;
			z1 = -thickness;
			break;
		case SOUTH:
			z1 = 1.0D;
			z2 = 1.0D + thickness;
			break;
		case WEST:
			x2 = 0.0D;
			x1 = -thickness;
			break;
		case EAST:
			x1 = 1.0D;
			x2 = 1.0D + thickness;
			break;
		}
		return new AxisAlignedBB(x1, y1, z1, x2, y2, z2).offset((double) this.pos.getX(), (double) this.pos.getY(),
				(double) this.pos.getZ());
	}

	/**
	 * World-space space that must be free for the lid to open.
	 */
	public AxisAlignedBB getOpeningSpace(EnumFacing facing) {
		return this.getSlab(facing, 0.5D);
	}

	private void moveCollidedEntities() {
		IBlockState iblockstate = this.worldObj.getBlockState(this.getPos());
		if (iblockstate.getBlock() instanceof BlockShulkerBox) {
			EnumFacing enumfacing = (EnumFacing) iblockstate.getValue(BlockShulkerBox.FACING);
			AxisAlignedBB slab = this.getSlab(enumfacing, 0.5D * (double) this.getProgress(1.0F));
			List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity((Entity) null, slab);
			for (int i = 0; i < list.size(); ++i) {
				Entity entity = list.get(i);
				if (entity.noClip) {
					continue;
				}

				double d0 = 0.0D;
				double d1 = 0.0D;
				double d2 = 0.0D;
				AxisAlignedBB bb = entity.getEntityBoundingBox();
				boolean positive = enumfacing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
				switch (enumfacing.getAxis()) {
				case X:
					d0 = (positive ? slab.maxX - bb.minX : bb.maxX - slab.minX) + 0.01D;
					break;
				case Y:
					d1 = (positive ? slab.maxY - bb.minY : bb.maxY - slab.minY) + 0.01D;
					break;
				case Z:
					d2 = (positive ? slab.maxZ - bb.minZ : bb.maxZ - slab.minZ) + 0.01D;
				}

				entity.moveEntity(d0 * (double) enumfacing.getFrontOffsetX(), d1 * (double) enumfacing.getFrontOffsetY(),
						d2 * (double) enumfacing.getFrontOffsetZ());
			}
		}
	}

	public int getSizeInventory() {
		return this.contents.length;
	}

	public int getInventoryStackLimit() {
		return 64;
	}

	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			this.openCount = type;
			if (type == 0) {
				this.animationStatus = TileEntityShulkerBox.AnimationStatus.CLOSING;
			}

			if (type == 1) {
				this.animationStatus = TileEntityShulkerBox.AnimationStatus.OPENING;
			}

			return true;
		} else {
			return super.receiveClientEvent(id, type);
		}
	}

	public void openInventory(EntityPlayer player) {
		if (!player.isSpectator()) {
			if (this.openCount < 0) {
				this.openCount = 0;
			}

			++this.openCount;
			this.worldObj.addBlockEvent(this.pos, this.getBlockType(), 1, this.openCount);
			if (this.openCount == 1) {
				this.worldObj.playSoundEffect((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D,
						(double) this.pos.getZ() + 0.5D, "block.shulker_box.open", 0.5F,
						this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			}
		}
	}

	public void closeInventory(EntityPlayer player) {
		if (!player.isSpectator()) {
			--this.openCount;
			this.worldObj.addBlockEvent(this.pos, this.getBlockType(), 1, this.openCount);
			if (this.openCount <= 0) {
				this.worldObj.playSoundEffect((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D,
						(double) this.pos.getZ() + 0.5D, "block.shulker_box.close", 0.5F,
						this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			}
		}
	}

	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		return new ContainerShulkerBox(playerInventory, this, playerIn);
	}

	public String getGuiID() {
		return "minecraft:shulker_box";
	}

	public String getName() {
		return this.hasCustomName() ? this.customName : "container.shulkerBox";
	}

	public boolean hasCustomName() {
		return this.customName != null && this.customName.length() > 0;
	}

	public void setCustomName(String name) {
		this.customName = name;
	}

	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.loadContents(compound);
	}

	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		this.saveContents(compound);
	}

	public void loadContents(NBTTagCompound compound) {
		this.contents = new ItemStack[this.getSizeInventory()];
		if (compound.hasKey("Items", 9)) {
			NBTTagList nbttaglist = compound.getTagList("Items", 10);
			for (int i = 0; i < nbttaglist.tagCount(); ++i) {
				NBTTagCompound tag = nbttaglist.getCompoundTagAt(i);
				int j = tag.getByte("Slot") & 255;
				if (j >= 0 && j < this.contents.length) {
					this.contents[j] = ItemStack.loadItemStackFromNBT(tag);
				}
			}
		}

		if (compound.hasKey("CustomName", 8)) {
			this.customName = compound.getString("CustomName");
		}
	}

	public void saveContents(NBTTagCompound compound) {
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < this.contents.length; ++i) {
			if (this.contents[i] != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				this.contents[i].writeToNBT(tag);
				nbttaglist.appendTag(tag);
			}
		}

		compound.setTag("Items", nbttaglist);
		if (this.hasCustomName()) {
			compound.setString("CustomName", this.customName);
		}

		if (!compound.hasKey("Lock") && this.isLocked()) {
			this.getLockCode().toNBT(compound);
		}
	}

	public ItemStack getStackInSlot(int index) {
		return this.contents[index];
	}

	public ItemStack decrStackSize(int index, int count) {
		if (this.contents[index] != null) {
			ItemStack itemstack;
			if (this.contents[index].stackSize <= count) {
				itemstack = this.contents[index];
				this.contents[index] = null;
			} else {
				itemstack = this.contents[index].splitStack(count);
				if (this.contents[index].stackSize == 0) {
					this.contents[index] = null;
				}
			}

			this.markDirty();
			return itemstack;
		} else {
			return null;
		}
	}

	public ItemStack removeStackFromSlot(int index) {
		if (this.contents[index] != null) {
			ItemStack itemstack = this.contents[index];
			this.contents[index] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	public void setInventorySlotContents(int index, ItemStack stack) {
		this.contents[index] = stack;
		if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
			stack.stackSize = this.getInventoryStackLimit();
		}

		this.markDirty();
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		return this.worldObj.getTileEntity(this.pos) != this ? false
				: player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D,
						(double) this.pos.getZ() + 0.5D) <= 64.0D;
	}

	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return !(Block.getBlockFromItem(stack.getItem()) instanceof BlockShulkerBox);
	}

	public int getField(int id) {
		return 0;
	}

	public void setField(int id, int value) {
	}

	public int getFieldCount() {
		return 0;
	}

	public boolean isEmpty() {
		for (int i = 0; i < this.contents.length; ++i) {
			if (this.contents[i] != null) {
				return false;
			}
		}

		return true;
	}

	public int[] getSlotsForFace(EnumFacing side) {
		return SLOTS;
	}

	public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
		return !(Block.getBlockFromItem(stack.getItem()) instanceof BlockShulkerBox);
	}

	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return true;
	}

	public void clear() {
		this.cleared = true;
		for (int i = 0; i < this.contents.length; ++i) {
			this.contents[i] = null;
		}
	}

	public boolean isCleared() {
		return this.cleared;
	}

	public float getProgress(float partialTicks) {
		return this.progressOld + (this.progress - this.progressOld) * partialTicks;
	}

	public EnumDyeColor getColor() {
		if (this.color == null) {
			this.color = this.hasWorldObj() ? BlockShulkerBox.getColorOf(this.getBlockType()) : EnumDyeColor.PURPLE;
		}

		return this.color;
	}

	public boolean isDestroyedByCreativePlayer() {
		return this.destroyedByCreativePlayer;
	}

	public void setDestroyedByCreativePlayer(boolean flag) {
		this.destroyedByCreativePlayer = flag;
	}

	/**
	 * Whether breaking this box should drop an item (creative players don't drop
	 * empty, unnamed boxes).
	 */
	public boolean shouldDrop() {
		return !this.isDestroyedByCreativePlayer() || !this.isEmpty() || this.hasCustomName();
	}

	public static enum AnimationStatus {
		CLOSED, OPENING, OPENED, CLOSING;
	}
}
