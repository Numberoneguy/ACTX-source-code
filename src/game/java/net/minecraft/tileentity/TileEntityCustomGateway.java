package net.minecraft.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

public class TileEntityCustomGateway extends TileEntity {
    private BlockPos destination = BlockPos.ORIGIN;

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("DestX", this.destination.getX());
        compound.setInteger("DestY", this.destination.getY());
        compound.setInteger("DestZ", this.destination.getZ());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        int x = compound.getInteger("DestX");
        int y = compound.getInteger("DestY");
        int z = compound.getInteger("DestZ");
        this.destination = new BlockPos(x, y, z);
    }

    public BlockPos getDestination() {
        return this.destination;
    }

    public void setDestination(BlockPos pos) {
        this.destination = pos;
        this.markDirty();
    }
}
