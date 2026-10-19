package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityEnderChest;

/**+
 * This portion of EaglercraftX contains deobfuscated Minecraft 1.8 source code.
 * 
 * Minecraft 1.8.8 bytecode is (c) 2015 Mojang AB. "Do not distribute!"
 * Mod Coder Pack v9.18 deobfuscation configs are (c) Copyright by the MCP Team
 * 
 * EaglercraftX 1.8 patch files (c) 2022-2025 lax1dude, ayunami2000. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
public class InventoryEnderChest extends InventoryBasic {
	private static final int MAX_SLOTS = 54;
	private TileEntityEnderChest associatedChest;
	private int capacity = 1;

	public InventoryEnderChest() {
		super("container.enderchest", false, MAX_SLOTS);
	}

	public int getSizeInventory() {
		return this.capacity * 27;
	}

	public ItemStack func_174894_a(ItemStack stack) {
		ItemStack itemstack = stack.copy();

		for (int i = 0; i < this.getSizeInventory(); ++i) {
			ItemStack itemstack1 = this.getStackInSlot(i);
			if (itemstack1 == null) {
				this.setInventorySlotContents(i, itemstack);
				this.markDirty();
				return null;
			}

			if (ItemStack.areItemsEqual(itemstack1, itemstack)) {
				int j = Math.min(this.getInventoryStackLimit(), itemstack1.getMaxStackSize());
				int k = Math.min(itemstack.stackSize, j - itemstack1.stackSize);
				if (k > 0) {
					itemstack1.stackSize += k;
					itemstack.stackSize -= k;
					if (itemstack.stackSize <= 0) {
						this.markDirty();
						return null;
					}
				}
			}
		}

		if (itemstack.stackSize != stack.stackSize) {
			this.markDirty();
		}

		return itemstack;
	}

	public void setChestTileEntity(TileEntityEnderChest chestTileEntity) {
		this.associatedChest = chestTileEntity;
		this.capacity = chestTileEntity != null ? chestTileEntity.getCapacity() : 1;
	}

	public void loadInventoryFromNBT(NBTTagList parNBTTagList) {
		for (int i = 0; i < MAX_SLOTS; ++i) {
			this.setInventorySlotContents(i, (ItemStack) null);
		}

		for (int k = 0; k < parNBTTagList.tagCount(); ++k) {
			NBTTagCompound nbttagcompound = parNBTTagList.getCompoundTagAt(k);
			int j = nbttagcompound.getByte("Slot") & 255;
			if (j >= 0 && j < MAX_SLOTS) {
				this.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound));
			}
		}

	}

	public NBTTagList saveInventoryToNBT() {
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < MAX_SLOTS; ++i) {
			ItemStack itemstack = this.getStackInSlot(i);
			if (itemstack != null) {
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) i);
				itemstack.writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}

		return nbttaglist;
	}

	/**+
	 * Do not make give this method the name canInteractWith because
	 * it clashes with Container
	 */
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return this.associatedChest != null && !this.associatedChest.canBeUsed(entityplayer) ? false
				: super.isUseableByPlayer(entityplayer);
	}

	public void openInventory(EntityPlayer entityplayer) {
		if (this.associatedChest != null) {
			this.associatedChest.openChest();
		}

		super.openInventory(entityplayer);
	}

	public void closeInventory(EntityPlayer entityplayer) {
		if (this.associatedChest != null) {
			this.associatedChest.closeChest();
		}

		super.closeInventory(entityplayer);
		this.associatedChest = null;
		this.capacity = 1;
	}
}