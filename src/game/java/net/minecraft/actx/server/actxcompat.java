package net.minecraft.actx.server;

import net.lax1dude.eaglercraft.v1_8.EaglercraftVersion;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemMace;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

public final class actxcompat {
    private actxcompat() {
    }

    private static final int SHULKER_BASE_ID = 219;
    private static final int[] custom_block_ids = { 208, 210, 211 };
    private static final int[] custom_item_ids = { 208, 210, 211, 426, 432, 449, 450, 451, 452, 453, 459, 743, 744, 745, 746, 747, 748, 749, 750, 751 };

    public static boolean isActxClient(EntityPlayerMP player) {
        if (player == null) {
            return false;
        }
        MinecraftServer server = player.mcServer;
        String owner = server == null ? null : server.getServerOwner();
        if (owner != null && owner.equalsIgnoreCase(player.getName())) {
            return true;
        }
        return player.clientBrandUUID != null && player.clientBrandUUID.equals(EaglercraftVersion.clientBrandUUID);
    }

    private static boolean isShulkerId(int id) {
        return id >= SHULKER_BASE_ID && id < SHULKER_BASE_ID + 16;
    }

    public static boolean isCustomBlockId(int id) {
        if (isShulkerId(id)) {
            return true;
        }
        for (int i = 0; i < custom_block_ids.length; ++i) {
            if (custom_block_ids[i] == id) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCustomItem(Item item) {
        if (item == null) {
            return false;
        }
        if (item instanceof ItemElytra || item instanceof ItemMace) {
            return true;
        }
        int id = Item.getIdFromItem(item);
        if (isShulkerId(id)) {
            return true;
        }
        if ((id > 422 && id < 2256) || id > 2267) {
            return true;
        }
        for (int i = 0; i < custom_item_ids.length; ++i) {
            if (custom_item_ids[i] == id) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCustomBlockState(IBlockState state) {
        if (state == null) {
            return false;
        }
        int id = Block.getIdFromBlock(state.getBlock());
        return isCustomBlockId(id) || id == 137;
    }

    private static boolean isCommandBlockId(int id) {
        return id == 210 || id == 211;
    }

    public static IBlockState vanillaState(IBlockState state) {
        if (!isCustomBlockState(state)) {
            return state;
        }
        int id = Block.getIdFromBlock(state.getBlock());
        if (isShulkerId(id)) {
            return Blocks.dispenser.getDefaultState().withProperty(BlockDispenser.FACING,
                    (EnumFacing) state.getValue(BlockShulkerBox.FACING));
        }
        if (id == 137 || isCommandBlockId(id)) {
            return Blocks.command_block.getDefaultState();
        }
        return Blocks.stone.getDefaultState();
    }

    public static byte[] vanillaChunkBlocks(byte[] data, int sectionMask) {
        int blocks = Integer.bitCount(sectionMask) * 4096;
        byte[] out = data;
        for (int i = 0; i < blocks; ++i) {
            int lo = data[i * 2] & 255;
            int hi = data[i * 2 + 1] & 255;
            int id = (hi << 4) | (lo >> 4);
            if (isCustomBlockId(id) || id == 137) {
                if (out == data) {
                    out = data.clone();
                }
                int replacement;
                int meta = 0;
                if (isShulkerId(id)) {
                    replacement = Block.getIdFromBlock(Blocks.dispenser);
                    meta = lo & 15;
                } else {
                    replacement = id == 137 || isCommandBlockId(id) ? 137 : 1;
                }
                out[i * 2] = (byte) ((replacement << 4) | meta);
                out[i * 2 + 1] = (byte) (replacement >> 4);
            }
        }
        return out;
    }

    private static Item substituteItem(Item item) {
        if (item instanceof ItemMace) return Items.diamond_axe;
        if (item == Items.totem) return Items.iron_horse_armor;
        if (item == Items.end_crystal) return Items.ender_eye;
        if (item instanceof ItemElytra) return Items.leather_chestplate;
        if (item == Items.netherite_ingot) return Items.netherbrick;
        if (item == Items.netherite_sword) return Items.diamond_sword;
        if (item == Items.netherite_shovel) return Items.diamond_shovel;
        if (item == Items.netherite_pickaxe) return Items.diamond_pickaxe;
        if (item == Items.netherite_axe) return Items.diamond_axe;
        if (item == Items.netherite_hoe) return Items.diamond_hoe;
        if (item == Items.netherite_helmet) return Items.diamond_helmet;
        if (item == Items.netherite_chestplate) return Items.diamond_chestplate;
        if (item == Items.netherite_leggings) return Items.diamond_leggings;
        if (item == Items.netherite_boots) return Items.diamond_boots;
        if (isCommandBlockId(Item.getIdFromItem(item))) return Item.getItemFromBlock(Blocks.command_block);
        if (isShulkerId(Item.getIdFromItem(item))) return Item.getItemFromBlock(Blocks.dispenser);
        return Item.getItemFromBlock(Blocks.stone);
    }

    private static boolean isCustomEnchant(int id) {
        return id == Enchantment.mending.effectId || id == Enchantment.density.effectId
                || id == Enchantment.breach.effectId || id == Enchantment.windBurst.effectId;
    }

    private static void addEnchantLore(NBTTagList list, List<String> lore) {
        for (int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            int id = entry.getShort("id");
            if (isCustomEnchant(id)) {
                lore.add("\u00a7r" + Enchantment.getEnchantmentById(id).getTranslatedName(entry.getShort("lvl")));
            }
        }
    }

    private static List<String> shulkerContentLore(ItemStack stack) {
        List<String> lore = new ArrayList<String>();
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("BlockEntityTag", 10)) {
            return lore;
        }
        NBTTagCompound entity = stack.getTagCompound().getCompoundTag("BlockEntityTag");
        if (!entity.hasKey("Items", 9)) {
            return lore;
        }
        NBTTagList list = entity.getTagList("Items", 10);
        int shown = 0;
        int total = 0;
        for (int i = 0; i < list.tagCount(); ++i) {
            ItemStack content = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
            if (content != null) {
                ++total;
                if (shown <= 4) {
                    ++shown;
                    lore.add("\u00a77" + StatCollector.translateToLocalFormatted("container.shulkerBox.itemCount",
                            new Object[] { content.getDisplayName(), Integer.valueOf(content.stackSize) }));
                }
            }
        }
        if (total - shown > 0) {
            lore.add("\u00a77\u00a7o" + StatCollector.translateToLocalFormatted("container.shulkerBox.more",
                    new Object[] { Integer.valueOf(total - shown) }));
        }
        return lore;
    }

    private static Packet translateOpenWindow(S2DPacketOpenWindow packet) {
        if (!"minecraft:shulker_box".equals(packet.getGuiId())) {
            return packet;
        }
        IChatComponent title = packet.getWindowTitle();
        if (!(title instanceof ChatComponentTranslation)) {
            return packet;
        }
        String text = StatCollector.translateToLocal(((ChatComponentTranslation) title).getKey());
        return new S2DPacketOpenWindow(packet.getWindowId(), packet.getGuiId(), new ChatComponentText(text),
                packet.getSlotCount());
    }

    private static List<String> customEnchantLore(ItemStack stack) {
        List<String> lore = new ArrayList<String>();
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            addEnchantLore(tag.getTagList("ench", 10), lore);
            addEnchantLore(tag.getTagList("StoredEnchantments", 10), lore);
        }
        return lore;
    }

    public static ItemStack stackForClient(ItemStack stack, boolean actxClient) {
        if (actxClient || stack == null) {
            return stack;
        }
        Item real = stack.getItem();
        boolean custom = isCustomItem(real);
        List<String> lore = customEnchantLore(stack);
        if (isShulkerId(Item.getIdFromItem(real))) {
            lore.addAll(shulkerContentLore(stack));
        }
        if (!custom && lore.isEmpty()) {
            return stack;
        }
        NBTTagCompound tag = stack.hasTagCompound() ? (NBTTagCompound) stack.getTagCompound().copy() : new NBTTagCompound();
        NBTTagCompound display = tag.hasKey("display", 10) ? tag.getCompoundTag("display") : new NBTTagCompound();
        NBTTagCompound actx = new NBTTagCompound();
        ItemStack out;
        if (custom) {
            Item sub = substituteItem(real);
            int realMax = real.getMaxDamage();
            int subMax = sub.getMaxDamage();
            int damage = realMax > 0 && subMax > 0 ? stack.getItemDamage() * subMax / realMax : 0;
            boolean shulker = isShulkerId(Item.getIdFromItem(real));
            out = new ItemStack(sub, stack.stackSize, damage);
            if (!display.hasKey("Name", 8)) {
                String nameKey = real.getUnlocalizedName(stack) + ".name";
                String name = StatCollector.translateToLocal(nameKey).trim();
                if (shulker && name.equals(nameKey)) {
                    name = "Shulker Box";
                }
                display.setString("Name", "\u00a7r" + name);
                actx.setBoolean("n", true);
            }
            if (real instanceof ItemElytra) {
                display.setInteger("color", 0x808080);
                actx.setBoolean("c", true);
            }
            actx.setInteger("id", Item.getIdFromItem(real));
            actx.setShort("Damage", (short) stack.getItemDamage());
        } else {
            out = new ItemStack(real, stack.stackSize, stack.getItemDamage());
        }
        if (!lore.isEmpty()) {
            NBTTagList oldLore = display.hasKey("Lore", 9) ? display.getTagList("Lore", 8) : new NBTTagList();
            NBTTagList newLore = new NBTTagList();
            for (String line : lore) {
                newLore.appendTag(new NBTTagString(line));
            }
            for (int i = 0; i < oldLore.tagCount(); ++i) {
                newLore.appendTag(new NBTTagString(oldLore.getStringTagAt(i)));
            }
            display.setTag("Lore", newLore);
            actx.setInteger("l", lore.size());
        }
        tag.setTag("display", display);
        tag.setTag("ActX", actx);
        out.setTagCompound(tag);
        return out;
    }

    public static ItemStack restoreStack(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("ActX", 10)) {
            return stack;
        }
        NBTTagCompound tag = (NBTTagCompound) stack.getTagCompound().copy();
        NBTTagCompound actx = tag.getCompoundTag("ActX");
        boolean substituted = actx.hasKey("id");
        Item real = substituted ? Item.getItemById(actx.getInteger("id")) : stack.getItem();
        if (real == null) {
            return stack;
        }
        int damage = stack.getItemDamage();
        if (substituted) {
            int realMax = real.getMaxDamage();
            int subMax = stack.getItem().getMaxDamage();
            damage = realMax > 0 && subMax > 0 ? stack.getItemDamage() * realMax / subMax : actx.getShort("Damage");
        }
        tag.removeTag("ActX");
        if (tag.hasKey("display", 10)) {
            NBTTagCompound display = tag.getCompoundTag("display");
            if (actx.getBoolean("n")) {
                display.removeTag("Name");
            }
            if (actx.getBoolean("c")) {
                display.removeTag("color");
            }
            int added = actx.getInteger("l");
            if (added > 0 && display.hasKey("Lore", 9)) {
                NBTTagList oldLore = display.getTagList("Lore", 8);
                NBTTagList kept = new NBTTagList();
                for (int i = added; i < oldLore.tagCount(); ++i) {
                    kept.appendTag(new NBTTagString(oldLore.getStringTagAt(i)));
                }
                if (kept.tagCount() > 0) {
                    display.setTag("Lore", kept);
                } else {
                    display.removeTag("Lore");
                }
            }
            if (display.hasNoTags()) {
                tag.removeTag("display");
            }
        }
        ItemStack out = new ItemStack(real, stack.stackSize, damage);
        if (!tag.hasNoTags()) {
            out.setTagCompound(tag);
        }
        return out;
    }

    public static ItemStack[] restoreStacks(ItemStack[] stacks) {
        if (stacks == null) {
            return null;
        }
        ItemStack[] out = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; ++i) {
            out[i] = restoreStack(stacks[i]);
        }
        return out;
    }

    public static Packet filterPacket(EntityPlayerMP player, Packet packet) {
        if (packet instanceof S21PacketChunkData) {
            return isActxClient(player) ? packet : ((S21PacketChunkData) packet).actxVanillaCopy();
        }
        if (packet instanceof S26PacketMapChunkBulk) {
            return isActxClient(player) ? packet : ((S26PacketMapChunkBulk) packet).actxVanillaCopy();
        }
        if (packet instanceof S23PacketBlockChange) {
            return isActxClient(player) ? packet : ((S23PacketBlockChange) packet).actxVanillaCopy();
        }
        if (packet instanceof S22PacketMultiBlockChange) {
            return isActxClient(player) ? packet : ((S22PacketMultiBlockChange) packet).actxVanillaCopy();
        }
        if (packet instanceof S2FPacketSetSlot) {
            return isActxClient(player) ? packet : ((S2FPacketSetSlot) packet).actxVanillaCopy();
        }
        if (packet instanceof S30PacketWindowItems) {
            return isActxClient(player) ? packet : ((S30PacketWindowItems) packet).actxVanillaCopy();
        }
        if (packet instanceof S1CPacketEntityMetadata) {
            return isActxClient(player) ? packet : ((S1CPacketEntityMetadata) packet).actxVanillaCopy();
        }
        if (packet instanceof S2DPacketOpenWindow) {
            return isActxClient(player) ? packet : translateOpenWindow((S2DPacketOpenWindow) packet);
        }
        if (packet instanceof S04PacketEntityEquipment) {
            return isActxClient(player) ? packet : ((S04PacketEntityEquipment) packet).actxVanillaCopy();
        }
        return packet;
    }
}