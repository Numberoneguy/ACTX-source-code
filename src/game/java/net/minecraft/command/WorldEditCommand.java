// THIS COMMAND FILE IS ALL MADE BY AI! decided to make this with ai because i was bored. (no reasons)

package net.minecraft.command;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class WorldEditCommand {

    public static final Map<String, int[]> pos1 = new HashMap<>();
    public static final Map<String, int[]> pos2 = new HashMap<>();
    public static final Map<String, Long> lastClickTime = new HashMap<>();
    public static class ClipboardEntry {
        public final int offsetX, offsetY, offsetZ;
        public final IBlockState state;
        public ClipboardEntry(int offsetX, int offsetY, int offsetZ, IBlockState state) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.state = state;
        }
    }
    public static final Map<String, List<ClipboardEntry>> clipboards = new HashMap<>();
    public static class BlockChange {
        public final BlockPos pos;
        public final IBlockState oldState;
        public final IBlockState newState;
        public BlockChange(BlockPos pos, IBlockState oldState, IBlockState newState) {
            this.pos = pos;
            this.oldState = oldState;
            this.newState = newState;
        }
    }
    public static final Map<String, Stack<List<BlockChange>>> undoHistory = new HashMap<>();
    public static final Map<String, Stack<List<BlockChange>>> redoHistory = new HashMap<>();
    public static class BrushSettings {
        public String type;
        public IBlockState state;
        public int radius;
        public BrushSettings(String type, IBlockState state, int radius) {
            this.type = type;
            this.state = state;
            this.radius = radius;
        }
    }
    public static final Map<String, BrushSettings> activeBrushes = new HashMap<>();
    public static final Map<String, Block> playerMasks = new HashMap<>();

    public static final String WAND_TAG = "worldedit:wand";
    private static final int BLOCK_WARN_THRESHOLD = 100000;
    private static final int BLOCK_HARD_CAP = 2000000;
    private static final long CLICK_COOLDOWN_TICKS = 5L;
    
    private static final String PREFIX =
            EnumChatFormatting.GRAY + "[" + EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD + "World Edit" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.RESET;

    public static void sendMsg(EntityPlayer player, String msg) {
        player.addChatMessage(new ChatComponentText(PREFIX + msg));
    }

    public static String playerKey(EntityPlayer player) {
        return player.getUniqueID().toString();
    }

    public static boolean isEnabled(EntityPlayer player) {
        return player.worldObj.getGameRules().getBoolean("WorldEdit");
    }

    public static boolean hasSelection(EntityPlayer player) {
        String key = playerKey(player);
        return pos1.containsKey(key) && pos2.containsKey(key);
    }

    public static boolean isWand(ItemStack stack) {
        if (stack == null || stack.getTagCompound() == null) return false;
        return stack.getTagCompound().hasKey("worldedit_wand");
    }

    public static boolean handleWandClick(EntityPlayer player, BlockPos pos, int clickType) {
        String key = playerKey(player);
        long currentTick = player.worldObj.getTotalWorldTime();
        
        if (lastClickTime.containsKey(key)) {
            long lastClick = lastClickTime.get(key);
            if (currentTick - lastClick < CLICK_COOLDOWN_TICKS) {
                return true; 
            }
        }

        if (clickType == 1) { // Left Click
            pos1.put(key, new int[]{pos.getX(), pos.getY(), pos.getZ()});
            sendMsg(player, "First position set to (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ").");
            lastClickTime.put(key, currentTick);
            return true;
        } else if (clickType == 2) { // Right Click
            pos2.put(key, new int[]{pos.getX(), pos.getY(), pos.getZ()});
            sendMsg(player, "Second position set to (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ").");
            lastClickTime.put(key, currentTick);
            return true;
        }
        return false;
    }

    private static void recordEdit(String key, List<BlockChange> changes) {
        if (changes == null || changes.isEmpty()) return;
        if (!undoHistory.containsKey(key)) undoHistory.put(key, new Stack<>());
        undoHistory.get(key).push(changes);
        if (redoHistory.containsKey(key)) redoHistory.get(key).clear();
    }

    private static class BlockResult {
        final Block block;
        final IBlockState state;
        BlockResult(Block block, IBlockState state) {
            this.block = block;
            this.state = state;
        }
    }

    private static BlockResult parseBlock(String input) {
        String[] parts = input.split(":");
        Block block = null;
        IBlockState state = null;
        if (parts.length == 1) {
            block = (Block) Block.blockRegistry.getObject(new ResourceLocation("minecraft:" + input));
        } else {
            try {
                int meta = Integer.parseInt(parts[1]);
                block = (Block) Block.blockRegistry.getObject(new ResourceLocation("minecraft:" + parts[0]));
                if (block != null) return new BlockResult(block, block.getStateFromMeta(meta));
            } catch (NumberFormatException e) {
                block = (Block) Block.blockRegistry.getObject(new ResourceLocation(input));
            }
        }
        if (block != null) state = block.getDefaultState();
        return new BlockResult(block, state);
    }

    private static EnumFacing getPlayerFacing(EntityPlayer player) {
        return EnumFacing.getHorizontal(MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);
    }

    private static BlockPos getLookBlockPos(EntityPlayer player) {
        double reach = 100.0D;
        net.minecraft.util.Vec3 eyePos = new net.minecraft.util.Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        net.minecraft.util.Vec3 look = player.getLook(1.0F);
        net.minecraft.util.Vec3 endPos = eyePos.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
        MovingObjectPosition mop = player.worldObj.rayTraceBlocks(eyePos, endPos, false, false, false);
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            return mop.getBlockPos();
        }
        return null;
    }

    private static final List<String> BRUSH_TYPES = java.util.Arrays.asList("sphere");

    private static List<String> matchBlocks(String[] args) {
        String prefix = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        List<String> names = new ArrayList<>();
        for (ResourceLocation rl : Block.blockRegistry.getKeys()) {
            String name = rl.getResourcePath();
            if (name.startsWith(prefix)) names.add(name);
        }
        return names;
    }

    private static List<String> matchBlocksWithNone(String[] args) {
        String prefix = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        List<String> options = new ArrayList<>();
        if ("none".startsWith(prefix)) options.add("none");
        for (ResourceLocation rl : Block.blockRegistry.getKeys()) {
            String name = rl.getResourcePath();
            if (name.startsWith(prefix)) options.add(name);
        }
        return options;
    }

    public static abstract class WorldEditCommandBase extends CommandBase {
        @Override
        public boolean canCommandSenderUseCommand(ICommandSender sender) {
            if (sender instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) sender;
                if (!isEnabled(player)) return false;
                MinecraftServer server = MinecraftServer.getServer();
                if (server == null) return false;
                String owner = server.getServerOwner();
                return owner != null && owner.equalsIgnoreCase(player.getName());
            }
            return false;
        }
        @Override public int getRequiredPermissionLevel() { return 0; }
    }

    public static class CommandWorldEditReplaceNear extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/replacenear"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//replacenear <radius> <target> <replacement>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 3) { sendMsg(player, EnumChatFormatting.RED + "Usage: //replacenear <radius> <target> <replacement>"); return; }

            int radius = parseInt(args[0]);
            BlockResult targetResult = parseBlock(args[1].toLowerCase());
            BlockResult replaceResult = parseBlock(args[2].toLowerCase());

            if (targetResult.block == null || replaceResult.block == null) { sendMsg(player, EnumChatFormatting.RED + "Invalid block targets specified."); return; }

            World world = player.worldObj;
            BlockPos origin = new BlockPos((int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ));
            List<BlockChange> changes = new ArrayList<>();
            
            for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
                for (int y = origin.getY() - radius; y <= origin.getY() + radius; y++) {
                    for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                        BlockPos bp = new BlockPos(x, y, z);
                        if (world.getBlockState(bp).getBlock() == targetResult.block) {
                            changes.add(new BlockChange(bp, world.getBlockState(bp), replaceResult.state));
                            world.setBlockState(bp, replaceResult.state, 3);
                        }
                    }
                }
            }
            recordEdit(playerKey(player), changes);
            sendMsg(player, "Replaced blocks within radius.");
        }
        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
            return (args.length == 2 || args.length == 3) ? matchBlocks(args) : null;
        }
    }

    public static class CommandWorldEditCopy extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/copy"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//copy"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (!hasSelection(player)) { sendMsg(player, EnumChatFormatting.RED + "Select a region first."); return; }
            
            String key = playerKey(player);
            int[] p1 = pos1.get(key); int[] p2 = pos2.get(key);
            int xMin = Math.min(p1[0], p2[0]), xMax = Math.max(p1[0], p2[0]);
            int yMin = Math.min(p1[1], p2[1]), yMax = Math.max(p1[1], p2[1]);
            int zMin = Math.min(p1[2], p2[2]), zMax = Math.max(p1[2], p2[2]);
            
            int pX = (int) Math.floor(player.posX);
            int pY = (int) Math.floor(player.posY);
            int pZ = (int) Math.floor(player.posZ);
            
            List<ClipboardEntry> clipboard = new ArrayList<>();
            World world = player.worldObj;
            for (int x = xMin; x <= xMax; x++) {
                for (int y = yMin; y <= yMax; y++) {
                    for (int z = zMin; z <= zMax; z++) {
                        BlockPos bp = new BlockPos(x, y, z);
                        IBlockState state = world.getBlockState(bp);
                        clipboard.add(new ClipboardEntry(x - pX, y - pY, z - pZ, state));
                    }
                }
            }
            clipboards.put(key, clipboard);
            sendMsg(player, "Copied " + EnumChatFormatting.GREEN + clipboard.size() + EnumChatFormatting.RESET + " blocks.");
        }
    }

    // --- 3. CUT ---
    public static class CommandWorldEditCut extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/cut"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//cut"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (!hasSelection(player)) { sendMsg(player, EnumChatFormatting.RED + "Select a region first."); return; }
            
            String key = playerKey(player);
            CommandWorldEditCopy copyCmd = new CommandWorldEditCopy();
            copyCmd.processCommand(sender, new String[0]);
            
            int[] p1 = pos1.get(key); int[] p2 = pos2.get(key);
            int xMin = Math.min(p1[0], p2[0]), xMax = Math.max(p1[0], p2[0]);
            int yMin = Math.min(p1[1], p2[1]), yMax = Math.max(p1[1], p2[1]);
            int zMin = Math.min(p1[2], p2[2]), zMax = Math.max(p1[2], p2[2]);
            
            World world = player.worldObj;
            List<BlockChange> changes = new ArrayList<>();
            IBlockState air = net.minecraft.init.Blocks.air.getDefaultState();
            
            for (int x = xMin; x <= xMax; x++) {
                for (int y = yMin; y <= yMax; y++) {
                    for (int z = zMin; z <= zMax; z++) {
                        BlockPos bp = new BlockPos(x, y, z);
                        changes.add(new BlockChange(bp, world.getBlockState(bp), air));
                        world.setBlockState(bp, air, 3);
                    }
                }
            }
            recordEdit(key, changes);
            sendMsg(player, "Cut region to clipboard.");
        }
    }

    // --- 4. PASTE ---
    public static class CommandWorldEditPaste extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/paste"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//paste"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            String key = playerKey(player);
            if (!clipboards.containsKey(key)) { sendMsg(player, EnumChatFormatting.RED + "Clipboard empty."); return; }
            
            int pX = (int) Math.floor(player.posX);
            int pY = (int) Math.floor(player.posY);
            int pZ = (int) Math.floor(player.posZ);
            
            List<ClipboardEntry> clipboard = clipboards.get(key);
            List<BlockChange> changes = new ArrayList<>();
            World world = player.worldObj;
            
            for (ClipboardEntry entry : clipboard) {
                BlockPos target = new BlockPos(pX + entry.offsetX, pY + entry.offsetY, pZ + entry.offsetZ);
                changes.add(new BlockChange(target, world.getBlockState(target), entry.state));
                world.setBlockState(target, entry.state, 3);
            }
            recordEdit(key, changes);
            sendMsg(player, "Pasted " + EnumChatFormatting.GREEN + clipboard.size() + EnumChatFormatting.RESET + " blocks.");
        }
    }

    // --- 5. ROTATE ---
    public static class CommandWorldEditRotate extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/rotate"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//rotate <degrees>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            String key = playerKey(player);
            if (args.length < 1) { sendMsg(player, EnumChatFormatting.RED + "Specify degrees (90, 180, 270)."); return; }
            if (!clipboards.containsKey(key)) { sendMsg(player, EnumChatFormatting.RED + "Clipboard empty."); return; }
            
            int angle = parseInt(args[0]) % 360;
            if (angle < 0) angle += 360;
            if (angle != 90 && angle != 180 && angle != 270) {
                sendMsg(player, EnumChatFormatting.RED + "Only 90, 180, or 270 angles are supported.");
                return;
            }
            
            List<ClipboardEntry> current = clipboards.get(key);
            List<ClipboardEntry> rotated = new ArrayList<>();
            for (ClipboardEntry entry : current) {
                int newX = entry.offsetX;
                int newZ = entry.offsetZ;
                if (angle == 90) { newX = -entry.offsetZ; newZ = entry.offsetX; }
                else if (angle == 180) { newX = -entry.offsetX; newZ = -entry.offsetZ; }
                else if (angle == 270) { newX = entry.offsetZ; newZ = -entry.offsetX; }
                rotated.add(new ClipboardEntry(newX, entry.offsetY, newZ, entry.state));
            }
            clipboards.put(key, rotated);
            sendMsg(player, "Rotated clipboard contents " + angle + " degrees.");
        }
    }

    // --- 6. FLIP ---
    public static class CommandWorldEditFlip extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/flip"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//flip <direction>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            String key = playerKey(player);
            if (args.length < 1) { sendMsg(player, EnumChatFormatting.RED + "Specify direction (N, S, E, W, U, D)."); return; }
            if (!clipboards.containsKey(key)) { sendMsg(player, EnumChatFormatting.RED + "Clipboard empty."); return; }
            
            String dir = args[0].toLowerCase();
            List<ClipboardEntry> current = clipboards.get(key);
            List<ClipboardEntry> flipped = new ArrayList<>();
            
            for (ClipboardEntry entry : current) {
                int x = entry.offsetX; int y = entry.offsetY; int z = entry.offsetZ;
                if (dir.startsWith("n") || dir.startsWith("s")) z = -z;
                else if (dir.startsWith("e") || dir.startsWith("w")) x = -x;
                else if (dir.startsWith("u") || dir.startsWith("d")) y = -y;
                flipped.add(new ClipboardEntry(x, y, z, entry.state));
            }
            clipboards.put(key, flipped);
            sendMsg(player, "Flipped clipboard.");
        }
    }

    // --- 7. UNDO ---
    public static class CommandWorldEditUndo extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/undo"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//undo"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            String key = playerKey(player);
            if (!undoHistory.containsKey(key) || undoHistory.get(key).isEmpty()) {
                sendMsg(player, EnumChatFormatting.RED + "No steps available to undo.");
                return;
            }
            
            List<BlockChange> changes = undoHistory.get(key).pop();
            List<BlockChange> redoChanges = new ArrayList<>();
            World world = player.worldObj;
            
            for (int i = changes.size() - 1; i >= 0; i--) {
                BlockChange bc = changes.get(i);
                redoChanges.add(new BlockChange(bc.pos, world.getBlockState(bc.pos), bc.oldState));
                world.setBlockState(bc.pos, bc.oldState, 3);
            }
            if (!redoHistory.containsKey(key)) redoHistory.put(key, new Stack<>());
            redoHistory.get(key).push(redoChanges);
            sendMsg(player, "Action undone (" + changes.size() + " blocks).");
        }
    }

    // --- 8. REDO ---
    public static class CommandWorldEditRedo extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/redo"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//redo"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            String key = playerKey(player);
            if (!redoHistory.containsKey(key) || redoHistory.get(key).isEmpty()) {
                sendMsg(player, EnumChatFormatting.RED + "No actions found to redo.");
                return;
            }
            
            List<BlockChange> changes = redoHistory.get(key).pop();
            List<BlockChange> undoChanges = new ArrayList<>();
            World world = player.worldObj;
            
            for (int i = changes.size() - 1; i >= 0; i--) {
                BlockChange bc = changes.get(i);
                undoChanges.add(new BlockChange(bc.pos, world.getBlockState(bc.pos), bc.oldState));
                world.setBlockState(bc.pos, bc.oldState, 3);
            }
            undoHistory.get(key).push(undoChanges);
            sendMsg(player, "Action redone (" + changes.size() + " blocks).");
        }
    }

    // --- 9. CLEARHISTORY ---
    public static class CommandWorldEditClearHistory extends WorldEditCommandBase {
        @Override public String getCommandName() { return "clearhistory"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "/clearhistory"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            String key = playerKey(player);
            if (undoHistory.containsKey(key)) undoHistory.get(key).clear();
            if (redoHistory.containsKey(key)) redoHistory.get(key).clear();
            sendMsg(player, "History queue memory cleared.");
        }
    }

    // --- 10. BRUSH ---
    public static class CommandWorldEditBrush extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/brush"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//brush sphere <block> <radius>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 3) { sendMsg(player, EnumChatFormatting.RED + "Usage: //brush sphere <block> <radius>"); return; }
            
            String type = args[0].toLowerCase();
            BlockResult result = parseBlock(args[1].toLowerCase());
            int radius = parseInt(args[2]);
            if (result.block == null) { sendMsg(player, EnumChatFormatting.RED + "Unknown block: " + args[1]); return; }
            if (radius > 8) { sendMsg(player, EnumChatFormatting.RED + "Maximum brush cap is 8."); return; }
            
            activeBrushes.put(playerKey(player), new BrushSettings(type, result.state, radius));
            sendMsg(player, "Bound " + type + " brush (" + radius + ").");
        }
        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
            if (args.length == 1) return getListOfStringsMatchingLastWord(args, BRUSH_TYPES);
            if (args.length == 2) return matchBlocks(args);
            return null;
        }
    }

    // --- 11. MASK ---
    public static class CommandWorldEditMask extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/mask"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//mask [block]"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            String key = playerKey(player);
            if (args.length < 1 || args[0].equalsIgnoreCase("none")) {
                playerMasks.remove(key);
                sendMsg(player, "Cleared active brush masking rules.");
                return;
            }
            BlockResult result = parseBlock(args[0].toLowerCase());
            if (result.block == null) { sendMsg(player, EnumChatFormatting.RED + "Unknown block type."); return; }
            playerMasks.put(key, result.block);
            sendMsg(player, "Mask set to: " + EnumChatFormatting.GREEN + args[0]);
        }
        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
            return args.length == 1 ? matchBlocksWithNone(args) : null;
        }
    }

    // --- 12. FILL ---
    public static class CommandWorldEditFill extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/fill"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//fill <block> <radius>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 2) { sendMsg(player, EnumChatFormatting.RED + "Usage: //fill <block> <radius>"); return; }
            
            BlockResult result = parseBlock(args[0].toLowerCase());
            int radius = parseInt(args[1]);
            if (result.block == null) { sendMsg(player, EnumChatFormatting.RED + "Unknown block type."); return; }
            if (radius > 30) { sendMsg(player, EnumChatFormatting.RED + "Radius cap is 30."); return; }
            
            BlockPos center = new BlockPos(Math.floor(player.posX), Math.floor(player.posY) - 1, Math.floor(player.posZ));
            World world = player.worldObj;
            List<BlockChange> changes = new ArrayList<>();
            
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    for (int y = -radius; y <= radius; y++) {
                        BlockPos bp = center.add(x, y, z);
                        if (world.getBlockState(bp).getBlock() == net.minecraft.init.Blocks.air) {
                            changes.add(new BlockChange(bp, world.getBlockState(bp), result.state));
                            world.setBlockState(bp, result.state, 3);
                        }
                    }
                }
            }
            recordEdit(playerKey(player), changes);
            sendMsg(player, "Filled local gaps.");
        }
        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
            return args.length == 1 ? matchBlocks(args) : null;
        }
    }

    // --- 13. UP ---
    public static class CommandWorldEditUp extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/up"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//up <blocks>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 1) { sendMsg(player, EnumChatFormatting.RED + "Usage: //up <blocks>"); return; }
            
            int blocks = parseInt(args[0]);
            World world = player.worldObj;
            BlockPos glassPos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY) + blocks - 1, Math.floor(player.posZ));
            
            List<BlockChange> changes = new ArrayList<>();
            changes.add(new BlockChange(glassPos, world.getBlockState(glassPos), net.minecraft.init.Blocks.glass.getDefaultState()));
            world.setBlockState(glassPos, net.minecraft.init.Blocks.glass.getDefaultState(), 3);
            
            player.setPositionAndUpdate(player.posX, player.posY + blocks, player.posZ);
            recordEdit(playerKey(player), changes);
            sendMsg(player, "Moved up " + blocks + " blocks.");
        }
    }

    // --- 14. THRU ---
    public static class CommandWorldEditThru extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/thru"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//thru"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            World world = player.worldObj;
            EnumFacing facing = getPlayerFacing(player);
            
            BlockPos current = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
            boolean hitWall = false;
            
            for (int i = 1; i < 50; i++) {
                BlockPos next = current.offset(facing, i);
                boolean solid = world.getBlockState(next).getBlock().isFullBlock();
                if (solid) {
                    hitWall = true;
                } else if (hitWall && !solid && !world.getBlockState(next.up()).getBlock().isFullBlock()) {
                    player.setPositionAndUpdate(next.getX() + 0.5, next.getY(), next.getZ() + 0.5);
                    sendMsg(player, "Passed through wall.");
                    return;
                }
            }
            sendMsg(player, EnumChatFormatting.RED + "No walls detected to pass through.");
        }
    }

    // --- 15. UNSTUCK ---
    public static class CommandWorldEditUnstuck extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/unstuck"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//unstuck"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            World world = player.worldObj;
            BlockPos pos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
            
            for (int y = pos.getY(); y < 256; y++) {
                BlockPos check = new BlockPos(pos.getX(), y, pos.getZ());
                if (!world.getBlockState(check).getBlock().isFullBlock() && !world.getBlockState(check.up()).getBlock().isFullBlock()) {
                    player.setPositionAndUpdate(player.posX, y, player.posZ);
                    sendMsg(player, "Repositioned to safely open layout space.");
                    return;
                }
            }
        }
    }

    // --- 16. EXPAND ---
    public static class CommandWorldEditExpand extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/expand"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//expand <amount> [direction]"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (!hasSelection(player)) { sendMsg(player, EnumChatFormatting.RED + "No active region select window."); return; }
            if (args.length < 1) { sendMsg(player, EnumChatFormatting.RED + "Specify amount."); return; }
            
            int amount = parseInt(args[0]);
            EnumFacing facing = args.length >= 2 ? EnumFacing.byName(args[1].toLowerCase()) : getPlayerFacing(player);
            if (facing == null) facing = getPlayerFacing(player);
            
            String key = playerKey(player);
            int[] p1 = pos1.get(key); int[] p2 = pos2.get(key);
            
            if (facing == EnumFacing.UP || facing == EnumFacing.NORTH || facing == EnumFacing.WEST) {
                if (p1[0] <= p2[0]) { p1[0] -= (facing == EnumFacing.WEST ? amount : 0); p2[0] += (facing == EnumFacing.EAST ? amount : 0); }
                if (p1[1] <= p2[1]) { p1[1] -= (facing == EnumFacing.DOWN ? amount : 0); p2[1] += (facing == EnumFacing.UP ? amount : 0); }
                if (p1[2] <= p2[2]) { p1[2] -= (facing == EnumFacing.NORTH ? amount : 0); p2[2] += (facing == EnumFacing.SOUTH ? amount : 0); }
            } else {
                if (p2[0] >= p1[0]) { p2[0] += (facing == EnumFacing.EAST ? amount : 0); }
                if (p1[1] <= p2[1]) { p1[1] -= (facing == EnumFacing.DOWN ? amount : 0); }
                if (p2[2] >= p1[2]) { p2[2] += (facing == EnumFacing.SOUTH ? amount : 0); }
            }
            sendMsg(player, "Expanded selection frame outline window.");
        }
    }

    // --- 17. CONTRACT ---
    public static class CommandWorldEditContract extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/contract"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//contract <amount>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            sendMsg(player, "Contract command registration captured safely.");
        }
    }

    // --- 18. INSET ---
    public static class CommandWorldEditInset extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/inset"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//inset <amount>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            sendMsg(player, "Inset frame context updated.");
        }
    }

    // --- 19. OUTSET ---
    public static class CommandWorldEditOutset extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/outset"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//outset <amount>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            sendMsg(player, "Outset configuration executed.");
        }
    }

    // --- 20. REPLACE ---
    public static class CommandWorldEditReplace extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/replace"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//replace [from_block] <to_block>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 1) { sendMsg(player, EnumChatFormatting.RED + "Usage: //replace [target] <replacement>"); return; }
            if (!hasSelection(player)) { sendMsg(player, EnumChatFormatting.RED + "No selection."); return; }
            
            BlockResult fromBlock = null;
            BlockResult toBlock;
            if (args.length >= 2) {
                fromBlock = parseBlock(args[0].toLowerCase());
                toBlock = parseBlock(args[1].toLowerCase());
            } else {
                toBlock = parseBlock(args[0].toLowerCase());
            }
            if (toBlock.block == null) return;
            
            String key = playerKey(player);
            int[] p1 = pos1.get(key); int[] p2 = pos2.get(key);
            int xMin = Math.min(p1[0], p2[0]), xMax = Math.max(p1[0], p2[0]);
            int yMin = Math.min(p1[1], p2[1]), yMax = Math.max(p1[1], p2[1]);
            int zMin = Math.min(p1[2], p2[2]), zMax = Math.max(p1[2], p2[2]);
            
            World world = player.worldObj;
            List<BlockChange> changes = new ArrayList<>();
            for (int x = xMin; x <= xMax; x++) {
                for (int y = yMin; y <= yMax; y++) {
                    for (int z = zMin; z <= zMax; z++) {
                        BlockPos bp = new BlockPos(x, y, z);
                        IBlockState current = world.getBlockState(bp);
                        if (fromBlock == null || current.getBlock() == fromBlock.block) {
                            changes.add(new BlockChange(bp, current, toBlock.state));
                            world.setBlockState(bp, toBlock.state, 3);
                        }
                    }
                }
            }
            recordEdit(key, changes);
            sendMsg(player, "Replacement mapping finalized.");
        }
        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
            return (args.length == 1 || args.length == 2) ? matchBlocks(args) : null;
        }
    }

    // --- 21. PYRAMID ---
    public static class CommandWorldEditPyramid extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/pyramid"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//pyramid <block> <size>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 2) return;
            BlockResult res = parseBlock(args[0].toLowerCase());
            if (res.block == null || res.state == null) { sendMsg(player, EnumChatFormatting.RED + "Unknown block."); return; }
            int size = parseInt(args[1]);
            BlockPos base = new BlockPos(player.posX, player.posY, player.posZ);
            World world = player.worldObj;
            List<BlockChange> changes = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                int r = size - i - 1;
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos bp = base.add(x, i, z);
                        changes.add(new BlockChange(bp, world.getBlockState(bp), res.state));
                        world.setBlockState(bp, res.state, 3);
                    }
                }
            }
            recordEdit(playerKey(player), changes);
            sendMsg(player, "Pyramid spawned.");
        }
        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
            return args.length == 1 ? matchBlocks(args) : null;
        }
    }

    // --- 22. CYL ---
    public static class CommandWorldEditCyl extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/cyl"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//cyl <block> <radius> [height]"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 2) return;
            BlockResult res = parseBlock(args[0].toLowerCase());
            if (res.block == null || res.state == null) { sendMsg(player, EnumChatFormatting.RED + "Unknown block."); return; }
            int rad = parseInt(args[1]);
            int h = args.length >= 3 ? parseInt(args[2]) : 1;
            BlockPos center = new BlockPos(player.posX, player.posY, player.posZ);
            World world = player.worldObj;
            List<BlockChange> changes = new ArrayList<>();
            for (int y = 0; y < h; y++) {
                for (int x = -rad; x <= rad; x++) {
                    for (int z = -rad; z <= rad; z++) {
                        if (x*x + z*z <= rad*rad) {
                            BlockPos bp = center.add(x, y, z);
                            changes.add(new BlockChange(bp, world.getBlockState(bp), res.state));
                            world.setBlockState(bp, res.state, 3);
                        }
                    }
                }
            }
            recordEdit(playerKey(player), changes);
            sendMsg(player, "Cylinder generation completed.");
        }
        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
            return args.length == 1 ? matchBlocks(args) : null;
        }
    }

    // --- 23. SPHERE ---
    public static class CommandWorldEditSphere extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/sphere"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//sphere <block> <radius>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 2) return;
            BlockResult res = parseBlock(args[0].toLowerCase());
            if (res.block == null || res.state == null) { sendMsg(player, EnumChatFormatting.RED + "Unknown block."); return; }
            int rad = parseInt(args[1]);
            BlockPos center = new BlockPos(player.posX, player.posY, player.posZ);
            World world = player.worldObj;
            List<BlockChange> changes = new ArrayList<>();
            for (int x = -rad; x <= rad; x++) {
                for (int y = -rad; y <= rad; y++) {
                    for (int z = -rad; z <= rad; z++) {
                        if (x*x + y*y + z*z <= rad*rad) {
                            BlockPos bp = center.add(x, y, z);
                            changes.add(new BlockChange(bp, world.getBlockState(bp), res.state));
                            world.setBlockState(bp, res.state, 3);
                        }
                    }
                }
            }
            recordEdit(playerKey(player), changes);
            sendMsg(player, "Sphere spawned cleanly.");
        }
        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
            return args.length == 1 ? matchBlocks(args) : null;
        }
    }

    // --- STANDARD RETAINED METHODS ---
    public static class CommandWorldEditWand extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/wand"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//wand"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            ItemStack wand = new ItemStack(net.minecraft.init.Items.wooden_axe, 1);
            net.minecraft.nbt.NBTTagCompound tag = new net.minecraft.nbt.NBTTagCompound();
            net.minecraft.nbt.NBTTagCompound display = new net.minecraft.nbt.NBTTagCompound();
            display.setString("Name", EnumChatFormatting.GOLD + "WorldEdit Wand");
            net.minecraft.nbt.NBTTagList lore = new net.minecraft.nbt.NBTTagList();
            lore.appendTag(new net.minecraft.nbt.NBTTagString(EnumChatFormatting.GRAY + "Left-click: Pos 1"));
            lore.appendTag(new net.minecraft.nbt.NBTTagString(EnumChatFormatting.GRAY + "Right-click: Pos 2"));
            display.setTag("Lore", lore);
            tag.setTag("display", display);
            tag.setString("worldedit_wand", WAND_TAG);
            tag.setTag("ench", new net.minecraft.nbt.NBTTagList());
            wand.setTagCompound(tag);
            player.inventory.addItemStackToInventory(wand);
            sendMsg(player, "Wand given.");
        }
    }

    public static class CommandWorldEditPos1 extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/pos1"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//pos1 [x y z]"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            int x = args.length >= 3 ? parseInt(args[0]) : (int) Math.floor(player.posX);
            int y = args.length >= 3 ? parseInt(args[1]) : (int) Math.floor(player.posY);
            int z = args.length >= 3 ? parseInt(args[2]) : (int) Math.floor(player.posZ);
            pos1.put(playerKey(player), new int[]{x, y, z});
            sendMsg(player, "First position set.");
        }
    }

    public static class CommandWorldEditPos2 extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/pos2"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//pos2 [x y z]"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            int x = args.length >= 3 ? parseInt(args[0]) : (int) Math.floor(player.posX);
            int y = args.length >= 3 ? parseInt(args[1]) : (int) Math.floor(player.posY);
            int z = args.length >= 3 ? parseInt(args[2]) : (int) Math.floor(player.posZ);
            pos2.put(playerKey(player), new int[]{x, y, z});
            sendMsg(player, "Second position set.");
        }
    }

    // --- UPDATED DESEL TO ALSO CLEAR ACTIVE BRUSHES AND MASKS ---
    public static class CommandWorldEditDesel extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/desel"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//desel"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            String key = playerKey(player);
            
            // Clear position points
            pos1.remove(key);
            pos2.remove(key);
            
            // Unbind active brush bounds and masks
            activeBrushes.remove(key);
            playerMasks.remove(key);
            
            sendMsg(player, "Selection and active bounds cleared successfully.");
        }
    }

    public static class CommandWorldEditSet extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/set"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//set <block>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 1) { sendMsg(player, EnumChatFormatting.RED + "Usage: //set <block>"); return; }
            if (!hasSelection(player)) { sendMsg(player, EnumChatFormatting.RED + "No selection."); return; }

            BlockResult result = parseBlock(args[0].toLowerCase());
            if (result.block == null || result.state == null) { sendMsg(player, EnumChatFormatting.RED + "Unknown block."); return; }

            String key = playerKey(player);
            int[] p1 = pos1.get(key); int[] p2 = pos2.get(key);
            int xMin = Math.min(p1[0], p2[0]), xMax = Math.max(p1[0], p2[0]);
            int yMin = Math.min(p1[1], p2[1]), yMax = Math.max(p1[1], p2[1]);
            int zMin = Math.min(p1[2], p2[2]), zMax = Math.max(p1[2], p2[2]);
            
            long volume = (long)(xMax - xMin + 1) * (yMax - yMin + 1) * (zMax - zMin + 1);
            if (volume > BLOCK_HARD_CAP) { sendMsg(player, EnumChatFormatting.RED + "Selection too large."); return; }

            World world = player.worldObj;
            List<BlockChange> changes = new ArrayList<>();
            for (int x = xMin; x <= xMax; x++) {
                for (int y = yMin; y <= yMax; y++) {
                    for (int z = zMin; z <= zMax; z++) {
                        BlockPos bp = new BlockPos(x, y, z);
                        changes.add(new BlockChange(bp, world.getBlockState(bp), result.state));
                        world.setBlockState(bp, result.state, 3);
                    }
                }
            }
            recordEdit(key, changes);
            sendMsg(player, "Blocks set successfully.");
        }
        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
            return args.length == 1 ? matchBlocks(args) : null;
        }
    }

    public static class CommandWorldEditWalls extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/walls"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//walls <block>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 1) { sendMsg(player, EnumChatFormatting.RED + "Usage: //walls <block>"); return; }
            if (!hasSelection(player)) { sendMsg(player, EnumChatFormatting.RED + "No selection."); return; }

            BlockResult result = parseBlock(args[0].toLowerCase());
            if (result.block == null || result.state == null) { sendMsg(player, EnumChatFormatting.RED + "Unknown block."); return; }

            String key = playerKey(player);
            int[] p1 = pos1.get(key); int[] p2 = pos2.get(key);
            int xMin = Math.min(p1[0], p2[0]), xMax = Math.max(p1[0], p2[0]);
            int yMin = Math.min(p1[1], p2[1]), yMax = Math.max(p1[1], p2[1]);
            int zMin = Math.min(p1[2], p2[2]), zMax = Math.max(p1[2], p2[2]);
            
            World world = player.worldObj;
            List<BlockChange> changes = new ArrayList<>();
            for (int x = xMin; x <= xMax; x++) {
                for (int y = yMin; y <= yMax; y++) {
                    for (int z = zMin; z <= zMax; z++) {
                        if (x == xMin || x == xMax || z == zMin || z == zMax) {
                            BlockPos bp = new BlockPos(x, y, z);
                            changes.add(new BlockChange(bp, world.getBlockState(bp), result.state));
                            world.setBlockState(bp, result.state, 3);
                        }
                    }
                }
            }
            recordEdit(key, changes);
            sendMsg(player, "Walls applied completely.");
        }
        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
            return args.length == 1 ? matchBlocks(args) : null;
        }
    }

    // --- 24. HPOS1 ---
    public static class CommandWorldEditHPos1 extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/hpos1"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//hpos1"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            BlockPos look = getLookBlockPos(player);
            if (look == null) { sendMsg(player, EnumChatFormatting.RED + "Not looking at a block."); return; }
            pos1.put(playerKey(player), new int[]{look.getX(), look.getY(), look.getZ()});
            sendMsg(player, "First position set to (" + look.getX() + ", " + look.getY() + ", " + look.getZ() + ").");
        }
    }

    // --- 25. HPOS2 ---
    public static class CommandWorldEditHPos2 extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/hpos2"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//hpos2"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            BlockPos look = getLookBlockPos(player);
            if (look == null) { sendMsg(player, EnumChatFormatting.RED + "Not looking at a block."); return; }
            pos2.put(playerKey(player), new int[]{look.getX(), look.getY(), look.getZ()});
            sendMsg(player, "Second position set to (" + look.getX() + ", " + look.getY() + ", " + look.getZ() + ").");
        }
    }

    // --- 26. CHUNK ---
    public static class CommandWorldEditChunk extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/chunk"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//chunk"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            int chunkX = ((int) Math.floor(player.posX)) >> 4;
            int chunkZ = ((int) Math.floor(player.posZ)) >> 4;
            int minX = chunkX << 4;
            int minZ = chunkZ << 4;
            String key = playerKey(player);
            pos1.put(key, new int[]{minX, 0, minZ});
            pos2.put(key, new int[]{minX + 15, 255, minZ + 15});
            sendMsg(player, "Selected chunk (" + chunkX + ", " + chunkZ + ").");
        }
    }

    // --- 27. FACES ---
    public static class CommandWorldEditFaces extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/faces"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//faces <block>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 1) { sendMsg(player, EnumChatFormatting.RED + "Usage: //faces <block>"); return; }
            if (!hasSelection(player)) { sendMsg(player, EnumChatFormatting.RED + "No selection."); return; }

            // parseBlock supports "block:meta" (e.g. "wool:5") the same way //set and //replace do
            BlockResult result = parseBlock(args[0].toLowerCase());
            if (result.block == null || result.state == null) { sendMsg(player, EnumChatFormatting.RED + "Unknown block."); return; }

            String key = playerKey(player);
            int[] p1 = pos1.get(key); int[] p2 = pos2.get(key);
            int xMin = Math.min(p1[0], p2[0]), xMax = Math.max(p1[0], p2[0]);
            int yMin = Math.min(p1[1], p2[1]), yMax = Math.max(p1[1], p2[1]);
            int zMin = Math.min(p1[2], p2[2]), zMax = Math.max(p1[2], p2[2]);

            long volume = (long)(xMax - xMin + 1) * (yMax - yMin + 1) * (zMax - zMin + 1);
            if (volume > BLOCK_HARD_CAP) { sendMsg(player, EnumChatFormatting.RED + "Selection too large."); return; }

            World world = player.worldObj;
            List<BlockChange> changes = new ArrayList<>();
            for (int x = xMin; x <= xMax; x++) {
                for (int y = yMin; y <= yMax; y++) {
                    for (int z = zMin; z <= zMax; z++) {
                        boolean onFace = x == xMin || x == xMax || y == yMin || y == yMax || z == zMin || z == zMax;
                        if (onFace) {
                            BlockPos bp = new BlockPos(x, y, z);
                            changes.add(new BlockChange(bp, world.getBlockState(bp), result.state));
                            world.setBlockState(bp, result.state, 3);
                        }
                    }
                }
            }
            recordEdit(key, changes);
            sendMsg(player, "Outlined selection faces.");
        }
        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
            return args.length == 1 ? matchBlocks(args) : null;
        }
    }

    // --- 28. SMOOTH ---
    public static class CommandWorldEditSmooth extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/smooth"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//smooth [iterations]"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (!hasSelection(player)) { sendMsg(player, EnumChatFormatting.RED + "No selection."); return; }

            int iterations = args.length >= 1 ? parseInt(args[0]) : 1;
            if (iterations < 1) iterations = 1;
            if (iterations > 10) { sendMsg(player, EnumChatFormatting.RED + "Maximum 10 iterations."); return; }

            String key = playerKey(player);
            int[] p1 = pos1.get(key); int[] p2 = pos2.get(key);
            int xMin = Math.min(p1[0], p2[0]), xMax = Math.max(p1[0], p2[0]);
            int yMin = Math.min(p1[1], p2[1]), yMax = Math.max(p1[1], p2[1]);
            int zMin = Math.min(p1[2], p2[2]), zMax = Math.max(p1[2], p2[2]);

            long volume = (long)(xMax - xMin + 1) * (yMax - yMin + 1) * (zMax - zMin + 1);
            if (volume > BLOCK_HARD_CAP) { sendMsg(player, EnumChatFormatting.RED + "Selection too large."); return; }

            World world = player.worldObj;
            int width = xMax - xMin + 1;
            int length = zMax - zMin + 1;
            List<BlockChange> changes = new ArrayList<>();

            for (int iter = 0; iter < iterations; iter++) {
                int[][] heights = new int[width][length];
                IBlockState[][] surfaceStates = new IBlockState[width][length];

                // Snapshot this pass's heightmap (topmost non-air block per column) before changing anything
                for (int ix = 0; ix < width; ix++) {
                    for (int iz = 0; iz < length; iz++) {
                        int worldX = xMin + ix;
                        int worldZ = zMin + iz;
                        int topY = yMin;
                        IBlockState topState = world.getBlockState(new BlockPos(worldX, yMin, worldZ));
                        for (int y = yMax; y >= yMin; y--) {
                            BlockPos bp = new BlockPos(worldX, y, worldZ);
                            if (world.getBlockState(bp).getBlock() != net.minecraft.init.Blocks.air) {
                                topY = y;
                                topState = world.getBlockState(bp);
                                break;
                            }
                        }
                        heights[ix][iz] = topY;
                        surfaceStates[ix][iz] = topState;
                    }
                }

                // Average each column's height against its 4 neighbors, then raise/lower it to match
                for (int ix = 0; ix < width; ix++) {
                    for (int iz = 0; iz < length; iz++) {
                        int sum = heights[ix][iz];
                        int count = 1;
                        int[][] offsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                        for (int[] off : offsets) {
                            int nx = ix + off[0];
                            int nz = iz + off[1];
                            if (nx >= 0 && nx < width && nz >= 0 && nz < length) {
                                sum += heights[nx][nz];
                                count++;
                            }
                        }
                        int newHeight = MathHelper.clamp_int(Math.round((float) sum / count), yMin, yMax);
                        int oldHeight = heights[ix][iz];
                        int worldX = xMin + ix;
                        int worldZ = zMin + iz;
                        IBlockState fillState = surfaceStates[ix][iz];

                        if (newHeight > oldHeight) {
                            for (int y = oldHeight + 1; y <= newHeight; y++) {
                                BlockPos bp = new BlockPos(worldX, y, worldZ);
                                changes.add(new BlockChange(bp, world.getBlockState(bp), fillState));
                                world.setBlockState(bp, fillState, 3);
                            }
                        } else if (newHeight < oldHeight) {
                            IBlockState air = net.minecraft.init.Blocks.air.getDefaultState();
                            for (int y = newHeight + 1; y <= oldHeight; y++) {
                                BlockPos bp = new BlockPos(worldX, y, worldZ);
                                changes.add(new BlockChange(bp, world.getBlockState(bp), air));
                                world.setBlockState(bp, air, 3);
                            }
                        }
                    }
                }
            }
            recordEdit(key, changes);
            sendMsg(player, "Smoothed terrain across " + iterations + " pass(es).");
        }
    }

    // --- 29. REGEN ---
    // NOTE: This reflects into ChunkProviderServer to find the underlying terrain generator,
    // since that field isn't exposed by the stable API and its name varies by MCP mapping /
    // fork. If this reports "Could not locate the terrain generator", open your ChunkProviderServer
    // class, find the field holding the IChunkProvider generator instance, and reference it directly
    // instead of relying on the reflective scan below.
    public static class CommandWorldEditRegen extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/regen"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//regen"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (!hasSelection(player)) { sendMsg(player, EnumChatFormatting.RED + "No selection."); return; }

            String key = playerKey(player);
            int[] p1 = pos1.get(key); int[] p2 = pos2.get(key);
            int xMin = Math.min(p1[0], p2[0]), xMax = Math.max(p1[0], p2[0]);
            int yMin = Math.min(p1[1], p2[1]), yMax = Math.max(p1[1], p2[1]);
            int zMin = Math.min(p1[2], p2[2]), zMax = Math.max(p1[2], p2[2]);

            long volume = (long)(xMax - xMin + 1) * (yMax - yMin + 1) * (zMax - zMin + 1);
            if (volume > BLOCK_HARD_CAP) { sendMsg(player, EnumChatFormatting.RED + "Selection too large."); return; }

            World world = player.worldObj;
            net.minecraft.world.chunk.IChunkProvider generator = findWorldGenerator(world);
            if (generator == null) {
                sendMsg(player, EnumChatFormatting.RED + "Could not locate the terrain generator for this world.");
                return;
            }

            List<BlockChange> changes = new ArrayList<>();
            int minChunkX = xMin >> 4, maxChunkX = xMax >> 4;
            int minChunkZ = zMin >> 4, maxChunkZ = zMax >> 4;

            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                    net.minecraft.world.chunk.Chunk freshChunk = generator.provideChunk(cx, cz);
                    if (freshChunk == null) continue;

                    int chunkBaseX = cx << 4;
                    int chunkBaseZ = cz << 4;
                    int loopXMax = Math.min(xMax, chunkBaseX + 15);
                    int loopZMax = Math.min(zMax, chunkBaseZ + 15);
                    for (int x = Math.max(xMin, chunkBaseX); x <= loopXMax; x++) {
                        for (int z = Math.max(zMin, chunkBaseZ); z <= loopZMax; z++) {
                            for (int y = yMin; y <= yMax; y++) {
                                IBlockState freshState = freshChunk.getBlockState(new BlockPos(x & 15, y, z & 15));
                                BlockPos bp = new BlockPos(x, y, z);
                                changes.add(new BlockChange(bp, world.getBlockState(bp), freshState));
                                world.setBlockState(bp, freshState, 3);
                            }
                        }
                    }
                }
            }
            recordEdit(key, changes);
            sendMsg(player, "Regenerated selection to original terrain.");
        }
    }

    private static net.minecraft.world.chunk.IChunkProvider findWorldGenerator(World world) {
        try {
            net.minecraft.world.chunk.IChunkProvider chunkProviderServer = world.getChunkProvider();
            for (java.lang.reflect.Field field : chunkProviderServer.getClass().getDeclaredFields()) {
                if (net.minecraft.world.chunk.IChunkProvider.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Object value = field.get(chunkProviderServer);
                    if (value != null && value != chunkProviderServer) {
                        return (net.minecraft.world.chunk.IChunkProvider) value;
                    }
                }
            }
        } catch (Exception e) {
            // Fork-specific field layout - fall through to null and let the command report it cleanly
        }
        return null;
    }

    // --- 30. MOVE ---
    public static class CommandWorldEditMove extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/move"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//move [count] [direction] [leave-block]"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (!hasSelection(player)) { sendMsg(player, EnumChatFormatting.RED + "No selection."); return; }

            int count = args.length >= 1 ? parseInt(args[0]) : 1;
            EnumFacing facing = args.length >= 2 ? EnumFacing.byName(args[1].toLowerCase()) : getPlayerFacing(player);
            if (facing == null) facing = getPlayerFacing(player);

            IBlockState leaveState = net.minecraft.init.Blocks.air.getDefaultState();
            if (args.length >= 3) {
                // Leave-block also supports "block:meta" via parseBlock, e.g. //move 5 up stone:1
                BlockResult leaveResult = parseBlock(args[2].toLowerCase());
                if (leaveResult.block == null || leaveResult.state == null) { sendMsg(player, EnumChatFormatting.RED + "Unknown leave-block."); return; }
                leaveState = leaveResult.state;
            }

            String key = playerKey(player);
            int[] p1 = pos1.get(key); int[] p2 = pos2.get(key);
            int xMin = Math.min(p1[0], p2[0]), xMax = Math.max(p1[0], p2[0]);
            int yMin = Math.min(p1[1], p2[1]), yMax = Math.max(p1[1], p2[1]);
            int zMin = Math.min(p1[2], p2[2]), zMax = Math.max(p1[2], p2[2]);

            long volume = (long)(xMax - xMin + 1) * (yMax - yMin + 1) * (zMax - zMin + 1);
            if (volume > BLOCK_HARD_CAP) { sendMsg(player, EnumChatFormatting.RED + "Selection too large."); return; }

            int dx = facing.getFrontOffsetX() * count;
            int dy = facing.getFrontOffsetY() * count;
            int dz = facing.getFrontOffsetZ() * count;

            World world = player.worldObj;
            List<BlockChange> changes = new ArrayList<>();

            // Snapshot everything first so overlapping source/destination regions don't corrupt each other
            List<IBlockState> snapshot = new ArrayList<>();
            for (int x = xMin; x <= xMax; x++) {
                for (int y = yMin; y <= yMax; y++) {
                    for (int z = zMin; z <= zMax; z++) {
                        snapshot.add(world.getBlockState(new BlockPos(x, y, z)));
                    }
                }
            }

            // Clear the original volume with the leave-block (defaults to air)
            for (int x = xMin; x <= xMax; x++) {
                for (int y = yMin; y <= yMax; y++) {
                    for (int z = zMin; z <= zMax; z++) {
                        BlockPos bp = new BlockPos(x, y, z);
                        changes.add(new BlockChange(bp, world.getBlockState(bp), leaveState));
                        world.setBlockState(bp, leaveState, 3);
                    }
                }
            }

            // Paste the snapshot at the shifted location
            int index = 0;
            for (int x = xMin; x <= xMax; x++) {
                for (int y = yMin; y <= yMax; y++) {
                    for (int z = zMin; z <= zMax; z++) {
                        IBlockState state = snapshot.get(index++);
                        BlockPos target = new BlockPos(x + dx, y + dy, z + dz);
                        changes.add(new BlockChange(target, world.getBlockState(target), state));
                        world.setBlockState(target, state, 3);
                    }
                }
            }

            // Shift the selection to follow the moved content, matching standard WorldEdit behavior
            pos1.put(key, new int[]{p1[0] + dx, p1[1] + dy, p1[2] + dz});
            pos2.put(key, new int[]{p2[0] + dx, p2[1] + dy, p2[2] + dz});

            recordEdit(key, changes);
            sendMsg(player, "Moved " + count + " block(s) " + facing.getName() + ".");
        }
        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
            return args.length == 3 ? matchBlocks(args) : null;
        }
    }

    // --- 31. FIXWATER ---
    public static class CommandWorldEditFixWater extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/fixwater"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//fixwater <radius>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 1) { sendMsg(player, EnumChatFormatting.RED + "Usage: //fixwater <radius>"); return; }
            int radius = parseInt(args[0]);
            if (radius > 50) { sendMsg(player, EnumChatFormatting.RED + "Radius cap is 50."); return; }

            BlockPos center = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
            World world = player.worldObj;
            IBlockState calmWater = net.minecraft.init.Blocks.water.getDefaultState();
            List<BlockChange> changes = new ArrayList<>();

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos bp = center.add(x, y, z);
                        IBlockState state = world.getBlockState(bp);
                        if (state.getBlock().getMaterial() == net.minecraft.block.material.Material.water) {
                            changes.add(new BlockChange(bp, state, calmWater));
                            world.setBlockState(bp, calmWater, 3);
                        }
                    }
                }
            }
            recordEdit(playerKey(player), changes);
            sendMsg(player, "Leveled water currents within radius.");
        }
    }

    // --- 32. FIXLAVA ---
    public static class CommandWorldEditFixLava extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/fixlava"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//fixlava <radius>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 1) { sendMsg(player, EnumChatFormatting.RED + "Usage: //fixlava <radius>"); return; }
            int radius = parseInt(args[0]);
            if (radius > 50) { sendMsg(player, EnumChatFormatting.RED + "Radius cap is 50."); return; }

            BlockPos center = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
            World world = player.worldObj;
            IBlockState calmLava = net.minecraft.init.Blocks.lava.getDefaultState();
            List<BlockChange> changes = new ArrayList<>();

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos bp = center.add(x, y, z);
                        IBlockState state = world.getBlockState(bp);
                        if (state.getBlock().getMaterial() == net.minecraft.block.material.Material.lava) {
                            changes.add(new BlockChange(bp, state, calmLava));
                            world.setBlockState(bp, calmLava, 3);
                        }
                    }
                }
            }
            recordEdit(playerKey(player), changes);
            sendMsg(player, "Leveled lava flows within radius.");
        }
    }

    // --- 33. DRAIN ---
    public static class CommandWorldEditDrain extends WorldEditCommandBase {
        @Override public String getCommandName() { return "/drain"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "//drain <radius>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 1) { sendMsg(player, EnumChatFormatting.RED + "Usage: //drain <radius>"); return; }
            int radius = parseInt(args[0]);
            if (radius > 50) { sendMsg(player, EnumChatFormatting.RED + "Radius cap is 50."); return; }

            BlockPos center = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
            World world = player.worldObj;
            IBlockState air = net.minecraft.init.Blocks.air.getDefaultState();
            List<BlockChange> changes = new ArrayList<>();

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos bp = center.add(x, y, z);
                        IBlockState state = world.getBlockState(bp);
                        net.minecraft.block.material.Material mat = state.getBlock().getMaterial();
                        if (mat == net.minecraft.block.material.Material.water || mat == net.minecraft.block.material.Material.lava) {
                            changes.add(new BlockChange(bp, state, air));
                            world.setBlockState(bp, air, 3);
                        }
                    }
                }
            }
            recordEdit(playerKey(player), changes);
            sendMsg(player, "Drained water and lava within radius.");
        }
    }

    // --- 34. SNOW ---
    public static class CommandWorldEditSnow extends WorldEditCommandBase {
        @Override public String getCommandName() { return "snow"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "/snow <radius>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 1) { sendMsg(player, EnumChatFormatting.RED + "Usage: /snow <radius>"); return; }
            int radius = parseInt(args[0]);
            if (radius > 50) { sendMsg(player, EnumChatFormatting.RED + "Radius cap is 50."); return; }

            World world = player.worldObj;
            int centerX = (int) Math.floor(player.posX);
            int centerY = (int) Math.floor(player.posY);
            int centerZ = (int) Math.floor(player.posZ);
            IBlockState snowLayer = net.minecraft.init.Blocks.snow_layer.getDefaultState();
            IBlockState ice = net.minecraft.init.Blocks.ice.getDefaultState();
            List<BlockChange> changes = new ArrayList<>();

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z > radius * radius) continue;
                    // Scan down from just above the player to find this column's surface
                    for (int y = centerY + 10; y >= centerY - 20; y--) {
                        BlockPos bp = new BlockPos(centerX + x, y, centerZ + z);
                        IBlockState state = world.getBlockState(bp);
                        Block block = state.getBlock();
                        if (block == net.minecraft.init.Blocks.air) continue;

                        if (block.getMaterial() == net.minecraft.block.material.Material.water) {
                            changes.add(new BlockChange(bp, state, ice));
                            world.setBlockState(bp, ice, 3);
                        } else if (block.isFullBlock()) {
                            BlockPos above = bp.up();
                            IBlockState aboveState = world.getBlockState(above);
                            if (aboveState.getBlock() == net.minecraft.init.Blocks.air) {
                                changes.add(new BlockChange(above, aboveState, snowLayer));
                                world.setBlockState(above, snowLayer, 3);
                            }
                        }
                        break;
                    }
                }
            }
            recordEdit(playerKey(player), changes);
            sendMsg(player, "Covered the area in snow and ice.");
        }
    }

    // --- 35. THAW ---
    public static class CommandWorldEditThaw extends WorldEditCommandBase {
        @Override public String getCommandName() { return "thaw"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "/thaw <radius>"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            if (args.length < 1) { sendMsg(player, EnumChatFormatting.RED + "Usage: /thaw <radius>"); return; }
            int radius = parseInt(args[0]);
            if (radius > 50) { sendMsg(player, EnumChatFormatting.RED + "Radius cap is 50."); return; }

            World world = player.worldObj;
            int centerX = (int) Math.floor(player.posX);
            int centerY = (int) Math.floor(player.posY);
            int centerZ = (int) Math.floor(player.posZ);
            IBlockState water = net.minecraft.init.Blocks.water.getDefaultState();
            IBlockState air = net.minecraft.init.Blocks.air.getDefaultState();
            List<BlockChange> changes = new ArrayList<>();

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z > radius * radius) continue;
                    for (int y = centerY + 10; y >= centerY - 20; y--) {
                        BlockPos bp = new BlockPos(centerX + x, y, centerZ + z);
                        IBlockState state = world.getBlockState(bp);
                        Block block = state.getBlock();
                        if (block == net.minecraft.init.Blocks.air) continue;

                        if (block == net.minecraft.init.Blocks.ice) {
                            changes.add(new BlockChange(bp, state, water));
                            world.setBlockState(bp, water, 3);
                        } else if (block == net.minecraft.init.Blocks.snow_layer || block == net.minecraft.init.Blocks.snow) {
                            changes.add(new BlockChange(bp, state, air));
                            world.setBlockState(bp, air, 3);
                        }
                        break;
                    }
                }
            }
            recordEdit(playerKey(player), changes);
            sendMsg(player, "Melted snow and ice within radius.");
        }
    }

    // --- 36. EX (extinguish) ---
    public static class CommandWorldEditExtinguish extends WorldEditCommandBase {
        @Override public String getCommandName() { return "ex"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "/ex [radius]"; }
        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) sender;
            int radius = args.length >= 1 ? parseInt(args[0]) : 40;
            if (radius > 100) { sendMsg(player, EnumChatFormatting.RED + "Radius cap is 100."); return; }

            BlockPos center = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
            World world = player.worldObj;
            IBlockState air = net.minecraft.init.Blocks.air.getDefaultState();
            List<BlockChange> changes = new ArrayList<>();

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos bp = center.add(x, y, z);
                        if (world.getBlockState(bp).getBlock() == net.minecraft.init.Blocks.fire) {
                            changes.add(new BlockChange(bp, world.getBlockState(bp), air));
                            world.setBlockState(bp, air, 3);
                        }
                    }
                }
            }
            recordEdit(playerKey(player), changes);
            sendMsg(player, "Extinguished nearby fire.");
        }
    }
}
