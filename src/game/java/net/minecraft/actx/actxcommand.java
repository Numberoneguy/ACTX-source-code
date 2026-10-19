package net.minecraft.actx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.actx.guis.colormodifier;
import net.minecraft.actx.guis.itemstackmodifier;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.gui.GuiActOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ChatTranslator;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.lax1dude.eaglercraft.v1_8.sp.lan.LANServerController;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.lax1dude.eaglercraft.v1_8.EagRuntime;
public final class actxcommand {
    private actxcommand() {}
    private static volatile GuiScreen pendingscreen = null;
    private static void queueScreen(GuiScreen screen) {
        pendingscreen = screen;
    }
    public static GuiScreen pollPendingScreen() {
        GuiScreen s = pendingscreen;
        pendingscreen = null;
        return s;
    }
    private static final class Entry {
        final String usage;
        final String desckey;
        final boolean runnable;
        Entry(String usage, String desckey, boolean runnable) {
            this.usage = usage; this.desckey = desckey; this.runnable = runnable;
        }
    }
    private static final Entry[] entries = {
        new Entry("/act help", "cmd.act.help.cmd", true),
        new Entry("/act menu", "cmd.act.menu", true),
        new Entry("/act give (item) [amount]", "cmd.act.give", false),
        new Entry("/act opengiver", "cmd.act.opengiver", true),
        new Entry("/act head (player)", "cmd.act.head", false),
        new Entry("/act rfw", "cmd.act.rfw", true),
        new Entry("/act edit", "cmd.act.edit", true),
        new Entry("/act rename (name)", "cmd.act.rename", false),
        new Entry("/act enchant [enchant] [level]", "cmd.act.enchant", false),
        new Entry("/act format [format]", "cmd.act.format", false),
        new Entry("/act info", "cmd.act.info", true),
        new Entry("/act unbreakable [true|false]", "cmd.act.unbreakable", false),
        new Entry("/act translate [true|false]", "cmd.act.translate", false),
        new Entry("/act color", "cmd.act.color", true),
        new Entry("/act color picker", "cmd.act.color.picker", true),
        new Entry("/act color remove", "cmd.act.color.remove", true),
        new Entry("/act color set", "cmd.act.color.set", false),
        new Entry("/act color set rgb (r) (g) (b)", "cmd.act.color.set.rgb", false),
        new Entry("/act color set hsl (h) (s) (l)", "cmd.act.color.set.hsl", false),
        new Entry("/act color set hex (hex)", "cmd.act.color.set.hex", false),
        new Entry("/act drop", "cmd.act.drop", true),
        new Entry("/act fov [val]", "cmd.act.fov", false),
        new Entry("/act copy [opt]", "cmd.act.copy", false),
        new Entry("/act view [opt]", "cmd.act.view", false),
        new Entry("/act clear", "cmd.act.clear", true),
    };
    private static final String[] subcommands = {
        "menu", "give", "opengiver", "head", "rfw", "edit", "rename", "enchant", "format", "info",
        "unbreakable", "translate", "color", "drop", "fov", "copy", "view", "clear", "help"
    };
    public static String[] complete(EntityPlayerSP player, String text) {
        if (!text.startsWith("/act ")) return new String[0];
        String after = text.substring(4);
        boolean trailing = after.endsWith(" ");
        String trimmed = after.trim();
        String[] parts = trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
        int index = trailing ? parts.length : parts.length - 1;
        String current = trailing || parts.length == 0 ? "" : parts[parts.length - 1];
        List<String> options = new ArrayList<String>();
        collectOptions(player, parts, index, options);
        List<String> matches = new ArrayList<String>();
        for (String option : options) {
            if (option.regionMatches(true, 0, current, 0, current.length())) matches.add(option);
        }
        Collections.sort(matches, String.CASE_INSENSITIVE_ORDER);
        return matches.toArray(new String[matches.size()]);
    }
    private static void collectOptions(EntityPlayerSP player, String[] parts, int index, List<String> out) {
        if (index == 0) {
            for (String s : subcommands) out.add(s);
            return;
        }
        String sub = parts[0].toLowerCase();
        switch (sub) {
            case "give":
                if (index == 1) {
                    for (ResourceLocation loc : Item.itemRegistry.getKeys()) {
                        out.add(loc.getResourceDomain().equals("minecraft") ? loc.getResourcePath() : loc.toString());
                    }
                } else if (index == 2) {
                    out.add("1"); out.add("16"); out.add("32"); out.add("64");
                }
                return;
            case "head":
                if (index == 1) {
                    for (NetworkPlayerInfo info : player.sendQueue.getPlayerInfoMap()) {
                        out.add(info.getGameProfile().getName());
                    }
                }
                return;
            case "enchant":
                if (index == 1) {
                    for (ResourceLocation loc : Enchantment.func_181077_c()) out.add(loc.getResourcePath());
                } else if (index == 2) {
                    Integer id = resolveEnchantId(parts[1]);
                    Enchantment ench = id == null ? null : Enchantment.getEnchantmentById(id);
                    int max = ench == null ? 5 : Math.max(1, ench.getMaxLevel());
                    for (int i = 1; i <= max; ++i) out.add(String.valueOf(i));
                }
                return;
            case "unbreakable":
            case "translate":
                if (index == 1) { out.add("true"); out.add("false"); }
                return;
            case "color":
                if (index == 1) {
                    out.add("picker"); out.add("remove"); out.add("set");
                } else if (index == 2 && parts[1].equalsIgnoreCase("set")) {
                    out.add("rgb"); out.add("hsl"); out.add("hex");
                }
                return;
            case "copy":
            case "view":
                if (index == 1) {
                    out.add("coords"); out.add("nbt"); out.add("item");
                    if (sub.equals("copy")) out.add("relay");
                }
                return;
            case "help":
                if (index == 1) {
                    int pages = Math.max(1, (sortedentries.length + helppagesize - 1) / helppagesize);
                    for (int i = 1; i <= pages; ++i) out.add(String.valueOf(i));
                }
                return;
            default:
        }
    }
    public static boolean handle(EntityPlayerSP player, String message) {
        String trimmed = message.trim();
        if (!trimmed.equals("/act") && !trimmed.startsWith("/act ")) return false;
        String remainder = trimmed.length() > 4 ? trimmed.substring(4).trim() : "";
        String[] args = remainder.isEmpty() ? new String[0] : remainder.split("\\s+");
        String sub = args.length > 0 ? args[0].toLowerCase() : "";
        if (sub.isEmpty() || sub.equals("help")) {
            sendHelp(player, args.length > 1 ? args[1] : "1");
            return true;
        }
        switch (sub) {
            case "menu": openMenu(player); return true;
            case "give": handleGive(player, args); return true;
            case "opengiver": openGiver(player); return true;
            case "head": handleHead(player, args); return true;
            case "rfw": actxrfwhandler.handleRandomFireworks(player); return true;
            case "edit": openEditor(player); return true;
            case "rename": handleRename(player, restOf(args, 1)); return true;
            case "enchant": handleEnchant(player, args); return true;
            case "format": handleFormat(player, restOf(args, 1)); return true;
            case "info": sendInfo(player); return true;
            case "unbreakable": handleUnbreakable(player, args); return true;
            case "translate": handleTranslate(player, args); return true;
            case "color": handleColor(player, args); return true;
            case "drop": handleDrop(player); return true;
            case "fov": handleFov(player, args); return true;
            case "copy": handleInspect(player, args, false); return true;
            case "view": handleInspect(player, args, true); return true;
            case "clear": handleClear(player); return true;
            default:
                player.addChatMessage(createStyledMessage("cmd.act.mc.invalid", EnumChatFormatting.DARK_RED, "/act help"));
                return true;
        }
    }
    private static final String helpversion = "v2.1";
    private static final int helppagesize = 8;
    private static final Entry[] sortedentries = buildSorted();
    private static Entry[] buildSorted() {
        Entry[] copy = new Entry[entries.length];
        System.arraycopy(entries, 0, copy, 0, entries.length);
        java.util.Arrays.sort(copy, (a, b) -> a.usage.compareToIgnoreCase(b.usage));
        return copy;
    }
    private static void sendHelp(EntityPlayerSP player, String pagearg) {
        int pages = Math.max(1, (sortedentries.length + helppagesize - 1) / helppagesize);
        int page;
        try {
            page = Integer.parseInt(pagearg);
        } catch (NumberFormatException ex) {
            sendNaN(player, pagearg);
            return;
        }
        page = Math.max(1, Math.min(pages, page));
        ChatComponentText header = new ChatComponentText(
                "-- Help for ACT-Mod " + helpversion + " (" + page + " / " + pages + ") --");
        header.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED));
        player.addChatMessage(header);
        int start = (page - 1) * helppagesize;
        int end = Math.min(sortedentries.length, start + helppagesize);
        for (int i = start; i < end; i++) printEntry(player, sortedentries[i]);
    }
    private static void sendSubHelp(EntityPlayerSP player, String prefix) {
        boolean any = false;
        for (Entry e : entries) {
            if (e.usage.equals(prefix) || e.usage.startsWith(prefix + " ")) {
                printEntry(player, e);
                any = true;
            }
        }
        if (!any) player.addChatMessage(createStyledMessage("cmd.act.usage", EnumChatFormatting.DARK_RED, prefix));
    }
    private static void printEntry(EntityPlayerSP player, Entry e) {
        ChatComponentText line = new ChatComponentText(e.usage);
        ChatStyle style = new ChatStyle();
        style.setColor(EnumChatFormatting.GOLD);
        String literal = e.usage.replaceAll("\\s*[\\[(].*", "");
        style.setChatClickEvent(new ClickEvent(
                e.runnable ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND, literal));
        style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ChatComponentText(I18n.format(e.runnable ? "cmd.act.help.do" : "cmd.act.help.click"))));
        line.setChatStyle(style);
        ChatComponentText sep = new ChatComponentText(" : ");
        sep.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));
        line.appendSibling(sep);
        ChatComponentText desc = new ChatComponentText(EnumChatFormatting.getTextWithoutFormattingCodes(I18n.format(e.desckey)));
        desc.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE));
        line.appendSibling(desc);
        player.addChatMessage(line);
    }
    private static void sendInfo(EntityPlayerSP player) {
        player.addChatMessage(createStyledMessage("cmd.act.info.text", EnumChatFormatting.DARK_RED));
    }
    private static void openEditor(EntityPlayerSP player) {
        ItemStack held = player.inventory.getCurrentItem();
        if (held == null) { sendError(player, "cmd.act.noitem"); return; }
        queueScreen(new itemstackmodifier(null, held));
    }
    private static void openMenu(EntityPlayerSP player) {
        queueScreen(new GuiActOptions(null));
    }
    private static void openGiver(EntityPlayerSP player) {
        ItemStack held = player.inventory.getCurrentItem();
        queueScreen(giver.open(null, held));
    }
    private static void openColorPicker(EntityPlayerSP player) {
        ItemStack held = player.inventory.getCurrentItem();
        if (held == null || !EditorState.isLeatherArmor(held)) {
            sendError(player, "cmd.act.notleather");
            return;
        }
        if (!requireCreative(player)) return;
        EditorState state = new EditorState(null, held);
        int initial = state.leatherColor >= 0 ? state.leatherColor : 0xA06540;
        queueScreen(new colormodifier(null,
                newcolor -> applyLeatherColor(player, newcolor),
                initial, 0xA06540));
    }
    private static void handleRename(EntityPlayerSP player, String name) {
        if (name.isEmpty()) { sendSubHelp(player, "/act rename"); return; }
        withHeldItem(player, state -> state.customName = name);
    }
    private static void handleEnchant(EntityPlayerSP player, String[] args) {
        if (args.length < 3) { sendSubHelp(player, "/act enchant"); return; }
        Integer id = resolveEnchantId(args[1]);
        if (id == null) {
            player.addChatMessage(createStyledMessage("cmd.act.noenchant", EnumChatFormatting.DARK_RED, args[1]));
            return;
        }
        int level;
        try { level = Integer.parseInt(args[2]); }
        catch (NumberFormatException e) { sendNaN(player, args[2]); return; }
        int enchid = id;
        withHeldItem(player, state -> {
            String existing = state.enchantStr.trim();
            state.enchantStr = existing.isEmpty() ? (enchid + ":" + level) : (existing + "," + enchid + ":" + level);
        });
    }
    private static Integer resolveEnchantId(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ignored) {}
        Enchantment ench = Enchantment.getEnchantmentByLocation(token.toLowerCase());
        return ench != null ? ench.effectId : null;
    }
    private static void handleUnbreakable(EntityPlayerSP player, String[] args) {
        boolean value = args.length < 2 || Boolean.parseBoolean(args[1]);
        withHeldItem(player, state -> state.unbreakable = value);
    }
    private static void handleTranslate(EntityPlayerSP player, String[] args) {
        boolean value = args.length < 2 ? !ChatTranslator.isEnabled() : Boolean.parseBoolean(args[1]);
        ChatTranslator.setEnabled(value);
        player.addChatMessage(createStyledMessage(value ? "cmd.act.translate.on" : "cmd.act.translate.off", EnumChatFormatting.YELLOW));
    }
    private static void handleFormat(EntityPlayerSP player, String sample) {
        if (!sample.isEmpty()) {
            player.addChatMessage(new ChatComponentText(EditorState.convertColors(sample)));
            return;
        }
        player.addChatMessage(new ChatComponentTranslation("cmd.act.format"));
        StringBuilder colors = new StringBuilder();
        for (char c : "0123456789abcdef".toCharArray()) colors.append('&').append(c).append(c).append(' ');
        player.addChatMessage(new ChatComponentText(EditorState.convertColors(colors.toString().trim())));
        StringBuilder styles = new StringBuilder();
        for (char c : "klmno".toCharArray()) styles.append('&').append(c).append(c).append("&r ");
        player.addChatMessage(new ChatComponentText(EditorState.convertColors(styles.toString().trim())));
    }
    private static void handleColor(EntityPlayerSP player, String[] args) {
        if (args.length < 2) { sendSubHelp(player, "/act color"); return; }
        switch (args[1].toLowerCase()) {
            case "picker": openColorPicker(player); return;
            case "remove": withHeldItem(player, state -> state.leatherColor = -1); return;
            case "set": handleColorSet(player, args); return;
            default: sendSubHelp(player, "/act color");
        }
    }
    private static void handleColorSet(EntityPlayerSP player, String[] args) {
        if (args.length < 3) { sendSubHelp(player, "/act color set"); return; }
        switch (args[2].toLowerCase()) {
            case "rgb": {
                if (args.length < 6) { sendSubHelp(player, "/act color set rgb"); return; }
                Integer r = parseClamped(player, args[3], 0, 255);
                Integer g = parseClamped(player, args[4], 0, 255);
                Integer b = parseClamped(player, args[5], 0, 255);
                if (r == null || g == null || b == null) return;
                int color = (r << 16) | (g << 8) | b;
                withHeldItem(player, state -> state.leatherColor = color);
                return;
            }
            case "hsl": {
                if (args.length < 6) { sendSubHelp(player, "/act color set hsl"); return; }
                Integer h = parseClamped(player, args[3], 0, 360);
                Integer s = parseClamped(player, args[4], 0, 100);
                Integer l = parseClamped(player, args[5], 0, 100);
                if (h == null || s == null || l == null) return;
                int color = hslToRgb(h / 360.0F, s / 100.0F, l / 100.0F);
                withHeldItem(player, state -> state.leatherColor = color);
                return;
            }
            case "hex": {
                if (args.length < 4) { sendSubHelp(player, "/act color set hex"); return; }
                String hex = args[3].startsWith("#") ? args[3].substring(1) : args[3];
                int color;
                try { color = (int) Long.parseLong(hex, 16) & 0xFFFFFF; }
                catch (NumberFormatException e) { sendNaN(player, hex); return; }
                withHeldItem(player, state -> state.leatherColor = color);
                return;
            }
            default:
                sendSubHelp(player, "/act color set");
        }
    }
    private static void handleDrop(EntityPlayerSP player) {
        boolean droppedany = false;
        if (player.capabilities.isCreativeMode) {
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (stack == null) continue;
                int slotid = containerSlotFor(i);
                clearInventorySlotDirect(player, i);
                player.sendQueue.addToSendQueue(
                        new C10PacketCreativeInventoryAction(slotid, null));
                droppedany = true;
            }
        } else {
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (stack == null) continue;
                int slotid = containerSlotFor(i);
                short actionnum = 0;
                player.sendQueue.addToSendQueue(
                        new C0EPacketClickWindow(0, slotid, 1, 4, stack, actionnum));
                clearInventorySlotDirect(player, i);
                droppedany = true;
            }
        }
        if (droppedany) {
            player.addChatMessage(createStyledMessage("cmd.act.drop.success", EnumChatFormatting.GREEN));
        } else {
            sendError(player, "cmd.act.drop.empty");
        }
    }
    private static void clearInventorySlotDirect(EntityPlayerSP player, int i) {
        if (i < player.inventory.mainInventory.length) {
            player.inventory.mainInventory[i] = null;
        } else {
            player.inventory.armorInventory[i - player.inventory.mainInventory.length] = null;
        }
    }
    private static int containerSlotFor(int invindex) {
        if (invindex < 9) {
            return 36 + invindex;
        } else if (invindex < 36) {
            return invindex;
        } else {
            int armorindex = invindex - 36;
            return 8 - armorindex;
        }
    }
    private static void handleGive(EntityPlayerSP player, String[] args) {
        if (args.length < 2) { sendSubHelp(player, "/act give"); return; }
        if (!requireCreative(player)) return;
        Item item = resolveItem(args[1]);
        if (item == null) {
            player.addChatMessage(createStyledMessage("cmd.act.give.invalid", EnumChatFormatting.DARK_RED, args[1]));
            return;
        }
        int amount = 1;
        if (args.length >= 3) {
            Integer parsed = parseClamped(player, args[2], 1, 64);
            if (parsed == null) return;
            amount = parsed;
        }
        ItemStack stack = new ItemStack(item, amount);
        if (giveItem(player, stack)) {
            player.addChatMessage(createStyledMessage("cmd.act.give.success", EnumChatFormatting.GREEN, stack.getDisplayName()));
        } else {
            player.addChatMessage(createStyledMessage("cmd.act.give.full", EnumChatFormatting.DARK_RED));
        }
    }
    private static Item resolveItem(String token) {
        try {
            return Item.getItemById(Integer.parseInt(token));
        } catch (NumberFormatException ignored) {}
        String name = token.contains(":") ? token : "minecraft:" + token;
        ResourceLocation loc = new ResourceLocation(name);
        return Item.itemRegistry.containsKey(loc) ? (Item) Item.itemRegistry.getObject(loc) : null;
    }
    private static void handleHead(EntityPlayerSP player, String[] args) {
        if (args.length < 2) { sendSubHelp(player, "/act head"); return; }
        if (!requireCreative(player)) return;
        String name = args[1];
        ItemStack stack = new ItemStack(Item.getItemById(397), 1, 3);
        String idstr;
        EntityPlayer target = player.worldObj.getPlayerEntityByName(name);
        if (target != null && target.getGameProfile().getId() != null) {
            idstr = target.getGameProfile().getId().toString();
        } else {
            idstr = java.util.UUID.nameUUIDFromBytes(
                    ("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
        }
        net.minecraft.nbt.NBTTagCompound tag = new net.minecraft.nbt.NBTTagCompound();
        net.minecraft.nbt.NBTTagCompound skullowner = new net.minecraft.nbt.NBTTagCompound();
        skullowner.setString("Name", name);
        skullowner.setString("Id", idstr);
        tag.setTag("SkullOwner", skullowner);
        stack.setTagCompound(tag);
        if (giveItem(player, stack)) {
            player.addChatMessage(createStyledMessage("cmd.act.head.success", EnumChatFormatting.GREEN, name));
        } else {
            player.addChatMessage(createStyledMessage("cmd.act.give.full", EnumChatFormatting.DARK_RED));
        }
    }
    static boolean giveItem(EntityPlayerSP player, ItemStack stack) {
        for (int i = 0; i < player.inventory.mainInventory.length; ++i) {
            if (player.inventory.mainInventory[i] == null) {
                player.inventory.setInventorySlotContents(i, stack);
                int slotid = (i < 9) ? (36 + i) : i;
                player.sendQueue.addToSendQueue(new C10PacketCreativeInventoryAction(slotid, stack));
                return true;
            }
        }
        return false;
    }
    private static void handleFov(EntityPlayerSP player, String[] args) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (args.length < 2) {
            float currentfov = mc.gameSettings.fovSetting;
            player.addChatMessage(createStyledMessage("cmd.act.fov.get", EnumChatFormatting.AQUA, String.valueOf((int) currentfov)));
            return;
        }
        try {
            float fovval = Float.parseFloat(args[1]);
            mc.gameSettings.fovSetting = fovval;
            player.addChatMessage(createStyledMessage("cmd.act.fov.set", EnumChatFormatting.GREEN, String.valueOf((int) fovval)));
        } catch (NumberFormatException e) {
            sendNaN(player, args[1]);
        }
    }
    private static void handleInspect(EntityPlayerSP player, String[] args, boolean viewonly) {
        if (args.length < 2) {
            sendSubHelp(player, viewonly ? "/act view" : "/act copy");
            return;
        }
        String target = args[1].toLowerCase();
        String result = null;
        switch (target) {
            case "cords":
            case "coords":
            case "pos":
                result = String.format("%.1f, %.1f, %.1f", player.posX, player.posY, player.posZ);
                break;
            case "nbt":
                ItemStack heldnbt = player.inventory.getCurrentItem();
                if (heldnbt != null && heldnbt.hasTagCompound()) {
                    result = heldnbt.getTagCompound().toString();
                } else if (heldnbt != null) {
                    result = "{}";
                } else {
                    sendError(player, "cmd.act.noitem");
                    return;
                }
                break;
            case "item":
            case "item_name":
                ItemStack helditem = player.inventory.getCurrentItem();
                if (helditem != null) {
                    ResourceLocation loc = Item.itemRegistry.getNameForObject(helditem.getItem());
                    result = loc != null ? loc.toString() : "minecraft:air";
                } else {
                    result = "minecraft:air";
                }
                break;
            case "relay":
            case "relay_code":
            case "code":
                if (viewonly) {
                    sendSubHelp(player, "/act view");
                    return;
                }
                if (LANServerController.isHostingLAN()) {
                    result = LANServerController.getCurrentCode();
                } else {
                    sendError(player, "cmd.act.not_hosting");
                    return;
                }
                break;
            default:
                sendSubHelp(player, viewonly ? "/act view" : "/act copy");
                return;
        }
        if (viewonly) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + result));
        } else {
            EagRuntime.setClipboard(result);
            player.addChatMessage(createStyledMessage("cmd.act.copy.success", EnumChatFormatting.GREEN, result));
        }
    }
    private static void handleClear(EntityPlayerSP player) {
        if (!requireCreative(player)) return;
        for (int i = 0; i < player.inventory.mainInventory.length; ++i) {
            if (player.inventory.mainInventory[i] != null) {
                player.inventory.mainInventory[i] = null;
                int slotid = (i < 9) ? (36 + i) : i;
                player.sendQueue.addToSendQueue(new C10PacketCreativeInventoryAction(slotid, null));
            }
        }
        player.addChatMessage(createStyledMessage("cmd.act.clear.success", EnumChatFormatting.GREEN));
    }
    private static Integer parseClamped(EntityPlayerSP player, String token, int min, int max) {
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(token)));
        } catch (NumberFormatException e) {
            sendNaN(player, token);
            return null;
        }
    }
    private static int hslToRgb(float h, float s, float l) {
        float r, g, b;
        if (s == 0f) {
            r = g = b = l;
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1f / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f / 3f);
        }
        return (Math.round(r * 255) << 16) | (Math.round(g * 255) << 8) | Math.round(b * 255);
    }
    private static float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1f / 6f) return p + (q - p) * 6 * t;
        if (t < 1f / 2f) return q;
        if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6;
        return p;
    }
    private static void withHeldItem(EntityPlayerSP player, Consumer<EditorState> edit) {
        ItemStack held = player.inventory.getCurrentItem();
        if (held == null) { sendError(player, "cmd.act.noitem"); return; }
        if (!requireCreative(player)) return;
        EditorState state = new EditorState(null, held);
        edit.accept(state);
        applyStateToHeldItem(player, state);
    }
    private static void applyLeatherColor(EntityPlayerSP player, int newcolor) {
        ItemStack held = player.inventory.getCurrentItem();
        if (held == null) return;
        EditorState state = new EditorState(null, held);
        state.leatherColor = newcolor;
        applyStateToHeldItem(player, state);
    }
    private static void applyStateToHeldItem(EntityPlayerSP player, EditorState state) {
        ItemStack updated = state.buildPreviewStack();
        int slotid = 36 + player.inventory.currentItem;
        player.sendQueue.addToSendQueue(new C10PacketCreativeInventoryAction(slotid, updated));
    }
    static boolean requireCreative(EntityPlayerSP player) {
        if (player.capabilities.isCreativeMode) return true;
        player.addChatMessage(createStyledMessage("gui.act.nocreative", EnumChatFormatting.DARK_RED));
        return false;
    }
    private static void sendError(EntityPlayerSP player, String key) {
        player.addChatMessage(createStyledMessage(key, EnumChatFormatting.DARK_RED));
    }
    private static void sendNaN(EntityPlayerSP player, String value) {
        player.addChatMessage(createStyledMessage("cmd.act.NaN", EnumChatFormatting.DARK_RED, value));
    }
    private static String restOf(String[] args, int from) {
        if (args.length <= from) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }
    static ChatComponentTranslation createStyledMessage(String langkey, EnumChatFormatting color, Object... args) {
        ChatComponentTranslation msg = new ChatComponentTranslation(langkey, args);
        ChatStyle style = new ChatStyle();
        style.setColor(color);
        msg.setChatStyle(style);
        return msg;
    }
}