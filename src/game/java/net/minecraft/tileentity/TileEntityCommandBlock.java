package net.minecraft.tileentity;

import net.lax1dude.eaglercraft.v1_8.netty.ByteBuf;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TileEntityCommandBlock extends TileEntity implements ITickable {

	private boolean auto = false;
	private int CBMode = 0; // 0 = Impulse, 1 = Chain, 2 = Repeating
	private boolean powered = false;
	private boolean conditionMet = true;
	private boolean triggered = false;

	private final CommandBlockLogic commandBlockLogic = new CommandBlockLogic() {
		public BlockPos getPosition() {
			return TileEntityCommandBlock.this.pos;
		}

		public Vec3 getPositionVector() {
			return new Vec3((double) TileEntityCommandBlock.this.pos.getX() + 0.5D,
					(double) TileEntityCommandBlock.this.pos.getY() + 0.5D,
					(double) TileEntityCommandBlock.this.pos.getZ() + 0.5D);
		}

		public World getEntityWorld() {
			return TileEntityCommandBlock.this.getWorld();
		}

		public void setCommand(String s) {
			super.setCommand(s);
			TileEntityCommandBlock.this.markDirty();
		}

		public void updateCommand() {
			TileEntityCommandBlock.this.getWorld().markBlockForUpdate(TileEntityCommandBlock.this.pos);
		}

		public int func_145751_f() {
			return 0;
		}

		public void func_145757_a(ByteBuf bytebuf) {
			bytebuf.writeInt(TileEntityCommandBlock.this.pos.getX());
			bytebuf.writeInt(TileEntityCommandBlock.this.pos.getY());
			bytebuf.writeInt(TileEntityCommandBlock.this.pos.getZ());
		}

		public Entity getCommandSenderEntity() {
			return null;
		}

		public MinecraftServer getServer() {
			return MinecraftServer.getServer();
		}
	};

	@Override
	public void update() {
		World world = this.getWorld();
		if (world == null || world.isRemote) {
			return;
		}

		boolean isActivated = this.auto || this.powered;

		// Repeating Command Block Mode (Mode 2)
		if (this.CBMode == 2) {
			if (isActivated && this.conditionMet) {
				this.executeAndPropagate(world);
			}
		}
	}

	public void onRedstonePulse(World world) {
		boolean isActivated = this.auto || this.powered;

		// Impulse Command Block Mode (Mode 0)
		if (this.CBMode == 0 && isActivated && this.conditionMet) {
			this.executeAndPropagate(world);
		}
	}

	public void executeChainTrigger(World world) {
		boolean isActivated = this.auto || this.powered;

		// Chain Command Block Mode (Mode 1)
		if (this.CBMode == 1 && isActivated && this.conditionMet) {
			this.executeAndPropagate(world);
		}
	}

	private void executeAndPropagate(World world) {
		this.commandBlockLogic.trigger(world);

		// The command that just ran (or a comparator/redstone cascade it
		// triggers) can end up replacing or removing this exact block. When
		// that happens this tile entity is no longer attached to the world,
		// and continuing would NPE inside getBlockType() - this is the fix
		// for a crash where exactly that happened ("Exception while
		// updating neighbours", NPE at TileEntity.getBlockType()).
		if (this.isInvalid() || this.getWorld() == null) {
			return;
		}

		this.updateComparatorAndNeighbors(world);

		if (this.isInvalid() || this.getWorld() == null) {
			return;
		}

		this.propagateToNextChain(world);
	}

	private void updateComparatorAndNeighbors(World world) {
		if (this.isInvalid() || this.getWorld() == null) {
			return;
		}
		world.updateComparatorOutputLevel(this.pos, this.getBlockType());

		// updateComparatorOutputLevel() above can itself cascade into a
		// redstone/comparator update that loops back and replaces this
		// exact block - re-check before touching this tile entity again.
		// This is the precise failure point from the crash report: the NPE
		// was on this second getBlockType() call, meaning invalidation
		// happened during updateComparatorOutputLevel, not before it.
		if (this.isInvalid() || this.getWorld() == null) {
			return;
		}
		world.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
	}

	private void propagateToNextChain(World world) {
		if (this.isInvalid() || this.getWorld() == null) {
			return;
		}
		IBlockState state = world.getBlockState(this.pos);
		if (state.getBlock() instanceof BlockCommandBlock) {
			EnumFacing facing = (EnumFacing) state.getValue(BlockCommandBlock.FACING);
			BlockPos nextPos = this.pos.offset(facing);

			TileEntity te = world.getTileEntity(nextPos);
			if (te instanceof TileEntityCommandBlock) {
				TileEntityCommandBlock nextCB = (TileEntityCommandBlock) te;
				if (nextCB.getCBMode() == 1) { // Mode 1 = Chain
					nextCB.executeChainTrigger(world);
				}
			}
		}
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		this.commandBlockLogic.writeDataToNBT(nbttagcompound);

		nbttagcompound.setBoolean("auto", this.auto);
		nbttagcompound.setInteger("CBMode", this.CBMode);
		nbttagcompound.setBoolean("powered", this.powered);
		nbttagcompound.setBoolean("conditionMet", this.conditionMet);
		nbttagcompound.setBoolean("triggered", this.triggered);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		this.commandBlockLogic.readDataFromNBT(nbttagcompound);

		if (nbttagcompound.hasKey("auto")) {
			this.auto = nbttagcompound.getBoolean("auto");
		}
		if (nbttagcompound.hasKey("CBMode")) {
			this.CBMode = nbttagcompound.getInteger("CBMode");
		}
		if (nbttagcompound.hasKey("powered")) {
			this.powered = nbttagcompound.getBoolean("powered");
		}
		if (nbttagcompound.hasKey("conditionMet")) {
			this.conditionMet = nbttagcompound.getBoolean("conditionMet");
		}
		if (nbttagcompound.hasKey("triggered")) {
			this.triggered = nbttagcompound.getBoolean("triggered");
		}
	}

	public Packet getDescriptionPacket() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		this.writeToNBT(nbttagcompound);
		return new S35PacketUpdateTileEntity(this.pos, 2, nbttagcompound);
	}

	public boolean func_183000_F() {
		return true;
	}

	public CommandBlockLogic getCommandBlockLogic() {
		return this.commandBlockLogic;
	}

	public CommandResultStats getCommandResultStats() {
		return this.commandBlockLogic.getCommandResultStats();
	}

	public void setAuto(boolean auto) {
		this.auto = auto;
		this.markDirty();
	}

	public boolean isAuto() {
		return this.auto;
	}

	public void setCBMode(int mode) {
		this.CBMode = mode;
		this.markDirty();
		World world = this.getWorld();
		if (world != null) {
			IBlockState state = world.getBlockState(this.pos);
			world.setBlockState(this.pos, state.withProperty(BlockCommandBlock.MODE, mode), 3);
			world.markBlockForUpdate(this.pos);
		}
	}

	public int getCBMode() {
		return this.CBMode;
	}

	public void setPowered(boolean powered) {
		this.powered = powered;
		this.markDirty();
	}

	public boolean isPowered() {
		return this.powered;
	}

	public void setConditionMet(boolean conditionMet) {
		this.conditionMet = conditionMet;
		this.markDirty();
	}

	public boolean isConditionMet() {
		return this.conditionMet;
	}

	public void setTriggered(boolean triggered) {
		this.triggered = triggered;
		this.markDirty();
	}

	public boolean isTriggered() {
		return this.triggered;
	}
}