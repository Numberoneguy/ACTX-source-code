package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCustomGateway;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BlockCustomGateway extends BlockContainer {

    protected BlockCustomGateway() {
        super(Material.portal);
        this.setLightLevel(1.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCustomGateway();
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        return null; 
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (!worldIn.isRemote && entityIn instanceof EntityPlayer) {
            BlockPos destination = findChainDestination(worldIn, pos);
            
            if (destination != null && !destination.equals(BlockPos.ORIGIN)) {
                EntityPlayer player = (EntityPlayer) entityIn;
                player.setPositionAndUpdate(destination.getX() + 0.5D, destination.getY(), destination.getZ() + 0.5D);
            }
        }
    }

    private BlockPos findChainDestination(World world, BlockPos startPos) {
        Set<BlockPos> checked = new HashSet<>();
        Set<BlockPos> toCheck = new HashSet<>();
        toCheck.add(startPos);

        while (!toCheck.isEmpty() && checked.size() < 50) {
            BlockPos current = toCheck.iterator().next();
            toCheck.remove(current);
            checked.add(current);

            TileEntity te = world.getTileEntity(current);
            if (te instanceof TileEntityCustomGateway) {
                BlockPos dest = ((TileEntityCustomGateway) te).getDestination();
                if (dest != null && !dest.equals(BlockPos.ORIGIN)) {
                    return dest;
                }
            }

            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos neighbor = current.offset(facing);
                if (world.getBlockState(neighbor).getBlock() == this && !checked.contains(neighbor)) {
                    toCheck.add(neighbor);
                }
            }
        }
        return null;
    }
}
