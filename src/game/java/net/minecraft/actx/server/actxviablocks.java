package net.minecraft.actx.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;

public final class actxviablocks {
    private actxviablocks() {
    }

    private static final int first_unknown_id = 198;

    public static boolean debug = false;

    private static void log(String message) {
        if (!debug) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("[viablocks] " + message));
        }
    }

    private static String name(Block block) {
        return Block.blockRegistry.getNameForObject(block) + "(" + Block.getIdFromBlock(block) + ")";
    }

    private static final class entry {
        final IBlockState state;
        final Block previous;

        entry(IBlockState state, Block previous) {
            this.state = state;
            this.previous = previous;
        }
    }

    private static final Map<Long, entry> placed = new HashMap<Long, entry>();
    private static final Map<Integer, Block> standins = new HashMap<Integer, Block>();

    static {
        seed();
    }

    private static void seed() {
        standins.put(Integer.valueOf(208), Blocks.grass);
        standins.put(Integer.valueOf(210), Blocks.command_block);
        standins.put(Integer.valueOf(211), Blocks.command_block);
    }

    public static void clear() {
        placed.clear();
        standins.clear();
        seed();
    }

    private static IBlockState inferFromOthers(BlockPos pos, IBlockState incoming) {
        Block inc = incoming.getBlock();
        if (inc == Blocks.air || !standins.containsValue(inc)) {
            return null;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null) {
            return null;
        }
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        for (EntityPlayer p : mc.theWorld.playerEntities) {
            if (p == mc.thePlayer || !p.isSwingInProgress || p.getDistanceSq(x, y, z) > 49.0D) {
                continue;
            }
            ItemStack held = p.getHeldItem();
            if (held == null || !(held.getItem() instanceof ItemBlock)) {
                continue;
            }
            Block real = ((ItemBlock) held.getItem()).getBlock();
            int id = Block.getIdFromBlock(real);
            if (id < first_unknown_id || standins.get(Integer.valueOf(id)) != inc) {
                continue;
            }
            IBlockState state = real.getDefaultState();
            if (real instanceof BlockCommandBlock && inc instanceof BlockCommandBlock) {
                state = state.withProperty(BlockCommandBlock.FACING, incoming.getValue(BlockCommandBlock.FACING))
                        .withProperty(BlockCommandBlock.TRIGGERED, incoming.getValue(BlockCommandBlock.TRIGGERED));
            }
            log("guessed " + name(real) + " placed by " + p.getName());
            return state;
        }
        return null;
    }

    public static void remember(BlockPos pos, IBlockState state, Block previous) {
        if (Block.getIdFromBlock(state.getBlock()) < first_unknown_id) {
            return;
        }
        placed.put(Long.valueOf(pos.toLong()), new entry(state, previous));
        log("remembered " + name(state.getBlock()) + " at " + pos.getX() + "," + pos.getY() + "," + pos.getZ());
    }

    public static IBlockState override(BlockPos pos, IBlockState incoming) {
        Long key = Long.valueOf(pos.toLong());
        entry mine = placed.isEmpty() ? null : placed.get(key);
        if (mine == null) {
            IBlockState guess = inferFromOthers(pos, incoming);
            if (guess != null) {
                placed.put(key, new entry(guess, null));
                return guess;
            }
            return incoming;
        }
        Block inc = incoming.getBlock();
        log("server sent " + name(inc) + " over remembered " + name(mine.state.getBlock()));
        if (inc == Blocks.air) {
            placed.remove(key);
            log("-> air, forgotten");
            return incoming;
        }
        if (inc == mine.state.getBlock()) {
            placed.put(key, new entry(incoming, mine.previous));
            return incoming;
        }
        if (inc == mine.previous) {
            placed.remove(key);
            log("-> same as before placement, treated as rejected");
            return incoming;
        }
        Integer id = Integer.valueOf(Block.getIdFromBlock(mine.state.getBlock()));
        Block known = standins.get(id);
        if (known == null) {
            standins.put(id, inc);
            log("-> learned stand-in " + name(inc) + ", keeping yours");
            return mine.state;
        }
        if (inc == known) {
            log("-> stand-in, keeping yours");
            return mine.state;
        }
        placed.remove(key);
        log("-> not the known stand-in " + name(known) + ", forgotten");
        return incoming;
    }

    public static void applyToChunk(Chunk chunk) {
        if (placed.isEmpty()) {
            return;
        }
        List<Long> keys = new ArrayList<Long>();
        for (Long key : placed.keySet()) {
            BlockPos p = BlockPos.fromLong(key.longValue());
            if ((p.getX() >> 4) == chunk.xPosition && (p.getZ() >> 4) == chunk.zPosition) {
                keys.add(key);
            }
        }
        for (Long key : keys) {
            BlockPos p = BlockPos.fromLong(key.longValue());
            IBlockState incoming = chunk.getBlockState(p);
            IBlockState result = override(p, incoming);
            if (result != incoming) {
                chunk.setBlockState(p, result);
            }
        }
    }
}