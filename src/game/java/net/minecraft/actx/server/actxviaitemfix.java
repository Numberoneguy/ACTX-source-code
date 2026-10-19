package net.minecraft.actx.server;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public final class actxviaitemfix {
    private actxviaitemfix() {
    }
    private static final Logger logger = LogManager.getLogger();
    private static final String ID_SUFFIX = "|id";
    private static final String DATA_SUFFIX = "|data";
    private static final String NAME_SUFFIX = "|customName";
    private static final class Port {
        final Item client;
        final Item standin;
        final int standinData;
        final int clientData;
        final int model;
        final String[] keys;
        final int[] values;
        Port(Item client, Item standin, int standinData, int clientData, int model, Object[] tags) {
            this.client = client;
            this.standin = standin;
            this.standinData = standinData;
            this.clientData = clientData;
            this.model = model;
            this.keys = new String[tags.length / 2];
            this.values = new int[tags.length / 2];
            for (int i = 0; i < keys.length; i++) {
                keys[i] = (String) tags[i * 2];
                values[i] = ((Integer) tags[i * 2 + 1]).intValue();
            }
        }
    }
    private static Port port(Item client, Item standin, int standinData, int clientData, int model,
            Object... tags) {
        return new Port(client, standin, standinData, clientData, model, tags);
    }
    // just fucking kill me
    // literally this shit is REALLY buggy but i`m glad i fixed it.
    private static final Port[] ports = {
            port(Items.mace, Items.diamond_axe, -1, -1, 647),
            port(Items.netherite_helmet, Items.diamond_helmet, -1, -1, 257, "VB|Protocol1_16To1_15_2|id", 642),
            port(Items.netherite_chestplate, Items.diamond_chestplate, -1, -1, 258, "VB|Protocol1_16To1_15_2|id", 643),
            port(Items.netherite_leggings, Items.diamond_leggings, -1, -1, 259, "VB|Protocol1_16To1_15_2|id", 644),
            port(Items.netherite_boots, Items.diamond_boots, -1, -1, 260, "VB|Protocol1_16To1_15_2|id", 645),
            port(Items.netherite_sword, Items.diamond_sword, -1, -1, 252, "VB|Protocol1_16To1_15_2|id", 606),
            port(Items.netherite_shovel, Items.diamond_shovel, -1, -1, 253, "VB|Protocol1_16To1_15_2|id", 607),
            port(Items.netherite_pickaxe, Items.diamond_pickaxe, -1, -1, 254, "VB|Protocol1_16To1_15_2|id", 608),
            port(Items.netherite_axe, Items.diamond_axe, -1, -1, 255, "VB|Protocol1_16To1_15_2|id", 609),
            port(Items.netherite_hoe, Items.diamond_hoe, -1, -1, 256, "VB|Protocol1_16To1_15_2|id", 618),
            port(Items.netherite_ingot, Items.iron_ingot, 0, 0, 250, "VB|Protocol1_16To1_15_2|id", 584),
    };
    private static final Map<Item, NBTTagCompound> learned = new HashMap<Item, NBTTagCompound>();
    private static final Map<Item, NBTTagCompound> seeds = new HashMap<Item, NBTTagCompound>();
    private static boolean remote = false;
    public static boolean debug = true;
    static {
        seedNetherite(Items.netherite_helmet, "Helmet", 257, 642, 916);
        seedNetherite(Items.netherite_chestplate, "Chestplate", 258, 643, 917);
        seedNetherite(Items.netherite_leggings, "Leggings", 259, 644, 918);
        seedNetherite(Items.netherite_boots, "Boots", 260, 645, 919);
        seedNetherite(Items.netherite_sword, "Sword", 252, 606, 883);
        seedNetherite(Items.netherite_shovel, "Shovel", 253, 607, 884);
        seedNetherite(Items.netherite_pickaxe, "Pickaxe", 254, 608, 885);
        seedNetherite(Items.netherite_axe, "Axe", 255, 609, 886);
        seedNetherite(Items.netherite_hoe, "Hoe", 256, 618, 887);
        seedNetherite(Items.netherite_ingot, "Ingot", 250, 584, 856);
        seedMace();
    }
    public static boolean isElytra(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        Item elytra = Item.getByNameOrId("elytra");
        return elytra != null && stack.getItem() == elytra;
    }
    public static void cleanArmor(EntityPlayer player, String source) {
        if (player == null || player.inventory == null) {
            return;
        }
        ItemStack[] armor = player.inventory.armorInventory;
        if (isElytra(armor[3]) && isElytra(armor[2])) {
            armor[3] = null;
            logger.info("duplicate elytra removed from the helmet slot after {}", source);
        }
    }
    public static void onJoin(boolean isRemote) {
        remote = isRemote;
        learned.clear();
    }
    private static Object currentManager() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.thePlayer != null && mc.thePlayer.sendQueue != null) {
            return mc.thePlayer.sendQueue.getNetworkManager();
        }
        return null;
    }
    private static boolean remoteNow() {
        Object manager = currentManager();
        if (manager == null) {
            return remote;
        }
        return !(manager instanceof net.lax1dude.eaglercraft.v1_8.sp.socket.ClientIntegratedServerNetworkManager)
                && !(manager instanceof net.lax1dude.eaglercraft.v1_8.sp.lan.LANClientNetworkManager);
    }
    public static ItemStack prepareForServer(ItemStack stack) {
        boolean via = remoteNow();
        ItemStack out = via ? toServer(stack, true) : stack;
        if (debug && stack != null) {
            Object manager = currentManager();
            logger.info("prepareForServer flag={} now={} manager={} in={} out={}", Boolean.valueOf(remote),
                    Boolean.valueOf(via), manager == null ? "none" : manager.getClass().getSimpleName(),
                    stack.getItem(), out == null ? null : out.getItem());
        }
        return out;
    }
    private static NBTTagCompound hashes(int id) {
        NBTTagCompound h = new NBTTagCompound();
        h.setTag("removed", new NBTTagList());
        h.setInteger("id", id);
        h.setString("backup_tag", "VB|Protocol1_21_5To1_21_4");
        return h;
    }
    private static void seedNetherite(Item client, String name, int model, int vb16id, int hashId) {
        if (client == null) {
            return;
        }
        NBTTagCompound t = new NBTTagCompound();
        NBTTagCompound d = new NBTTagCompound();
        d.setString("Name", "&f1.16 Netherite " + name);
        d.setString("VB|Protocol1_13To1_12_2|2|Name",
                "{\"italic\":false,\"color\":\"white\",\"text\":\"1.16 Netherite " + name + "\"}");
        d.setByte("VB|Protocol1_16To1_15_2|customName", (byte) 0);
        t.setTag("display", d);
        t.setInteger("CustomModelData", model);
        NBTTagCompound inner = new NBTTagCompound();
        inner.setByte("VV|custom_data", (byte) 1);
        inner.setTag("VV|original_hashes", hashes(hashId));
        t.setTag("VB|Protocol1_20_5To1_20_3", inner);
        t.setInteger("VB|Protocol1_16To1_15_2|id", vb16id);
        t.setByte("VV|custom_data", (byte) 1);
        t.setTag("VV|original_hashes", hashes(hashId));
        seeds.put(client, t);
    }

    private static void seedMace() {
        if (Items.mace == null) {
            return;
        }
        NBTTagCompound t = new NBTTagCompound();
        NBTTagCompound d = new NBTTagCompound();
        d.setString("Name", "&f1.20.5 Mace");
        d.setString("VB|Protocol1_13To1_12_2|2|Name",
                "{\"color\":\"white\",\"italic\":false,\"translate\":\"1.20.5 Mace\"}");
        d.setString("VB|Protocol1_19_4To1_19_3|Name",
                "{\"color\":\"white\",\"fallback\":\"1.20.5 Mace\",\"italic\":false,\"translate\":\"vb.item.mace\"}");
        t.setTag("display", d);
        t.setInteger("CustomModelData", 647);
        t.setInteger("VB|Protocol1_20_5To1_20_3|id", 1093);
        t.setByte("VB|Protocol1_20_5To1_20_3|added_custom_name", (byte) 1);
        NBTTagCompound inner = new NBTTagCompound();
        inner.setInteger("VB|Protocol1_20_5To1_20_3|id", 1093);
        inner.setByte("VB|Protocol1_20_5To1_20_3|added_custom_name", (byte) 1);
        inner.setByte("VV|custom_data", (byte) 1);
        inner.setTag("VV|original_hashes", hashes(1155));
        t.setTag("VB|Protocol1_20_5To1_20_3", inner);
        t.setInteger("VB|Protocol1_16To1_15_2|id", 609);
        t.setByte("VV|custom_data", (byte) 1);
        t.setTag("VV|original_hashes", hashes(1155));
        seeds.put(Items.mace, t);
    }
    private static boolean hasAddedName(NBTTagCompound tag, NBTTagCompound display) {
        for (String key : tag.getKeySet()) {
            if (isTagKey(key) && key.endsWith("|added_custom_name") && tag.hasKey(key, 99)
                    && tag.getInteger(key) != 0) {
                return true;
            }
        }
        for (String key : display.getKeySet()) {
            if (isTagKey(key) && key.endsWith(NAME_SUFFIX) && display.hasKey(key, 99)
                    && display.getInteger(key) == 0) {
                return true;
            }
        }
        return false;
    }

    private static void mergeDisplay(NBTTagCompound out, NBTTagCompound own) {
        NBTTagCompound d = out.hasKey("display", 10) ? out.getCompoundTag("display") : new NBTTagCompound();
        for (String key : new ArrayList<String>(own.getKeySet())) {
            if (isTagKey(key)) {
                continue;
            }
            d.setTag(key, own.getTag(key).copy());
            if (key.equals("Name")) {
                for (String k : new ArrayList<String>(d.getKeySet())) {
                    if (isTagKey(k) && k.endsWith(NAME_SUFFIX)) {
                        d.setByte(k, (byte) 1);
                    }
                }
                for (String k : new ArrayList<String>(out.getKeySet())) {
                    if (isTagKey(k) && k.endsWith("|added_custom_name")) {
                        out.setByte(k, (byte) 0);
                    }
                }
            }
        }
        out.setTag("display", d);
    }

    private static void dropStandinColor(NBTTagCompound display, Item restored) {
        boolean dyeable = restored instanceof ItemArmor
                && ((ItemArmor) restored).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER;
        if (!dyeable) {
            display.removeTag("color");
        }
    }

    private static boolean isViaKey(String key) {
        return key.startsWith("VB|") || key.startsWith("VR|") || key.startsWith("VV|");
    }

    private static boolean isTagKey(String key) {
        return key.startsWith("VB|") || key.startsWith("VR|");
    }

    private static boolean isLegacyBase(String base) {
        int at = base.indexOf("Protocol");
        int to = base.indexOf("To", at + 1);
        if (at < 0 || to < 0) {
            return false;
        }
        String[] parts = base.substring(at + "Protocol".length(), to).split("_");
        if (parts.length < 2) {
            return false;
        }
        try {
            return Integer.parseInt(parts[0]) == 1 && Integer.parseInt(parts[1]) <= 12;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String findBase(NBTTagCompound tag) {
        for (String key : tag.getKeySet()) {
            if (isTagKey(key) && key.endsWith(ID_SUFFIX) && tag.hasKey(key, 99)) {
                String base = key.substring(0, key.length() - ID_SUFFIX.length());
                if (isLegacyBase(base)) {
                    return base;
                }
            }
        }
        return null;
    }

    private static void stripViaKeys(NBTTagCompound compound) {
        List<String> remove = new ArrayList<String>();
        for (String key : compound.getKeySet()) {
            if (isViaKey(key)) {
                remove.add(key);
            }
        }
        for (String key : remove) {
            compound.removeTag(key);
        }
    }

    private static boolean sameShape(NBTTagCompound a, NBTTagCompound b) {
        if (a.getKeySet().size() != b.getKeySet().size()) {
            return false;
        }
        for (String key : a.getKeySet()) {
            if (!b.hasKey(key)) {
                return false;
            }
            NBTBase x = a.getTag(key);
            NBTBase y = b.getTag(key);
            if (x.getId() != y.getId()) {
                return false;
            }
            if (x.getId() == 10) {
                if (!sameShape((NBTTagCompound) x, (NBTTagCompound) y)) {
                    return false;
                }
            } else if (x.getId() == 9) {
                if (((NBTTagList) x).tagCount() != ((NBTTagList) y).tagCount()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void learn(Item client, NBTTagCompound tag) {
        for (String key : tag.getKeySet()) {
            if (!isViaKey(key) && !key.equals("CustomModelData") && !key.equals("display")) {
                return;
            }
        }
        NBTTagCompound via = new NBTTagCompound();
        for (String key : tag.getKeySet()) {
            if (isViaKey(key) || key.equals("CustomModelData")) {
                via.setTag(key, tag.getTag(key).copy());
            }
        }
        if (tag.hasKey("display", 10)) {
            NBTTagCompound display = (NBTTagCompound) tag.getCompoundTag("display").copy();
            if (!hasAddedName(tag, display)) {
                display.removeTag("Name");
            }
            via.setTag("display", display);
        }
        NBTTagCompound seed = seeds.get(client);
        if (seed != null && !sameShape(via, seed)) {
            return;
        }
        if (via.hasKey("CustomModelData", 99) || via.getKeySet().size() > 0) {
            learned.put(client, via);
        }
    }

    private static Port findPort(ItemStack stack, NBTTagCompound tag) {
        if (tag.hasKey("CustomModelData", 99)) {
            int model = tag.getInteger("CustomModelData");
            Item standin = stack.getItem();
            for (Port p : ports) {
                if (p.model == model && p.standin == standin) {
                    return p;
                }
            }
            return null;
        }
        Port best = null;
        for (Port p : ports) {
            if (p.keys.length == 0) {
                continue;
            }
            boolean all = true;
            for (int i = 0; i < p.keys.length; i++) {
                if (!tag.hasKey(p.keys[i], 99) || tag.getInteger(p.keys[i]) != p.values[i]) {
                    all = false;
                    break;
                }
            }
            if (all && (best == null || p.keys.length > best.keys.length)) {
                best = p;
            }
        }
        return best;
    }

    private static ItemStack restorePort(ItemStack stack, NBTTagCompound tag, Port p) {
        Item item = p.client;
        if (item == null) {
            return stack;
        }
        learn(p.client, tag);
        ItemStack restored = new ItemStack(item, stack.stackSize, p.clientData < 0 ? stack.getMetadata() : p.clientData);
        NBTTagCompound copy = (NBTTagCompound) tag.copy();
        if (copy.hasKey("CustomModelData", 99) && copy.getInteger("CustomModelData") == p.model) {
            copy.removeTag("CustomModelData");
        }
        if (copy.hasKey("display", 10)) {
            NBTTagCompound display = copy.getCompoundTag("display");
            if (hasAddedName(tag, display)) {
                display.removeTag("Name");
            }
            stripViaKeys(display);
            dropStandinColor(display, item);
            if (display.hasNoTags()) {
                copy.removeTag("display");
            }
        }
        stripViaKeys(copy);
        if (!copy.hasNoTags()) {
            restored.setTagCompound(copy);
        }
        return restored;
    }

    public static ItemStack toServer(ItemStack stack, boolean onServer) {
        if (!onServer || stack == null) {
            return stack;
        }
        Item held = stack.getItem();
        if (held == null) {
            return stack;
        }
        for (Port p : ports) {
            if (p.client != held) {
                continue;
            }
            NBTTagCompound template = learned.get(held);
            if (template == null) {
                template = seeds.get(held);
            }
            if (template == null || p.standin == null) {
                return stack;
            }
            ItemStack out = new ItemStack(p.standin, stack.stackSize,
                    p.standinData < 0 ? stack.getMetadata() : p.standinData);
            NBTTagCompound merged = (NBTTagCompound) template.copy();
            if (stack.hasTagCompound()) {
                NBTTagCompound own = stack.getTagCompound();
                for (String key : new ArrayList<String>(own.getKeySet())) {
                    if (isTagKey(key) || key.equals("CustomModelData")) {
                        continue;
                    }
                    if (key.equals("display") && own.hasKey(key, 10)) {
                        mergeDisplay(merged, own.getCompoundTag(key));
                    } else {
                        merged.setTag(key, own.getTag(key).copy());
                    }
                }
            }
            out.setTagCompound(merged);
            return out;
        }
        return stack;
    }

    public static ItemStack restore(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) {
            return stack;
        }
        NBTTagCompound tag = stack.getTagCompound();
        String base = findBase(tag);
        if (base != null) {
            Item item = Item.getItemById(tag.getInteger(base + ID_SUFFIX));
            if (item != null) {
                int damage = tag.hasKey(base + DATA_SUFFIX, 99) ? tag.getInteger(base + DATA_SUFFIX)
                        : stack.getMetadata();
                ItemStack restored = new ItemStack(item, stack.stackSize, damage);
                NBTTagCompound copy = (NBTTagCompound) tag.copy();
                copy.removeTag(base + ID_SUFFIX);
                copy.removeTag(base + DATA_SUFFIX);
                if (copy.hasKey("display", 10)) {
                    NBTTagCompound display = copy.getCompoundTag("display");
                    boolean viaName = display.hasKey(base + NAME_SUFFIX, 99)
                            && display.getInteger(base + NAME_SUFFIX) == 0;
                    display.removeTag(base + NAME_SUFFIX);
                    if (viaName) {
                        display.removeTag("Name");
                    }
                    stripViaKeys(display);
                    dropStandinColor(display, item);
                    if (display.hasNoTags()) {
                        copy.removeTag("display");
                    }
                }
                stripViaKeys(copy);
                if (!copy.hasNoTags()) {
                    restored.setTagCompound(copy);
                }
                return restored;
            }
        }
        Port p = findPort(stack, tag);
        return p == null ? stack : restorePort(stack, tag, p);
    }
}