package net.minecraft.client.gui;

import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;
import java.nio.charset.StandardCharsets;

public class EditorState {
    public final GuiScreen   returnTo;
    public final ItemStack   stack;
    public boolean isCommandBlock;

    public String customName   = "";
    public java.util.List<String> loreLines = new java.util.ArrayList<String>();
    public String enchantStr   = "";
    public String attributeStr = "";
    public String itemType     = "";
    public String metaStr      = "";
    public boolean unbreakable = false;
    public boolean isSpawnEgg  = false;

    public int     hideFlags        = -1;
    public java.util.List<String> canBreak     = new java.util.ArrayList<String>();
    public java.util.List<String> canPlaceOn   = new java.util.ArrayList<String>();

    public int     repairCost       = -1;
    public boolean glintEnchant     = false;
    public int     leatherColor     = -1;
    public boolean lodestoneTracked = false;
    public boolean lodestoneTrackedSet = false;
    public String  bookTitle        = "";
    public String  bookAuthor       = "";
    public java.util.List<String> bookPages = new java.util.ArrayList<String>();
    public boolean bookTagsSet      = false;
    public String  storedEnchantStr = "";
    public boolean storedEnchantSet = false;

    public boolean isHead;

    public String  headName = "";
    public String  headUuid = "";
    public String  headLink = "";
    public boolean headSet  = false;

    public static boolean isLeatherArmor(ItemStack stack) {
        int id = Item.getIdFromItem(stack.getItem());
        return id >= 298 && id <= 301;
    }
    public static boolean isCompass(ItemStack stack) {
        return Item.getIdFromItem(stack.getItem()) == 345;
    }
    public static boolean isBook(ItemStack stack) {
        int id = Item.getIdFromItem(stack.getItem());
        return id == 386 || id == 387;
    }
    public static boolean isEnchantedBook(ItemStack stack) {
        if (Item.getIdFromItem(stack.getItem()) == 403) return true;
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey("StoredEnchantments");
    }
    public EditorState(GuiScreen returnTo, ItemStack stack) {
        this.returnTo = returnTo;
        this.stack    = stack.copy();

        ResourceLocation loc = Item.itemRegistry.getNameForObject(stack.getItem());
        this.isCommandBlock = loc != null && loc.getResourcePath().contains("command_block");

        this.isSpawnEgg = (Item.getIdFromItem(stack.getItem()) == 383);
        this.isHead     = (Item.getIdFromItem(stack.getItem()) == 397 && stack.getItemDamage() == 3);

        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            if (tag.hasKey("display")) {
                NBTTagCompound display = tag.getCompoundTag("display");
                if (display.hasKey("Name")) customName = unconvertColors(display.getString("Name"));
                if (display.hasKey("Lore")) {
                    NBTTagList lore = display.getTagList("Lore", 8);
                    for (int i = 0; i < lore.tagCount(); i++) {
                        loreLines.add(unconvertColors(lore.getStringTagAt(i)));
                    }
                }
            }
            if (tag.hasKey("ench")) {
                NBTTagList ench = tag.getTagList("ench", 10);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < ench.tagCount(); i++) {
                    NBTTagCompound e = ench.getCompoundTagAt(i);
                    if (sb.length() > 0) sb.append(",");
                    sb.append(e.getShort("id")).append(":").append(e.getShort("lvl"));
                }
                enchantStr = sb.toString();
            }
            if (tag.hasKey("Unbreakable")) {
                unbreakable = tag.getBoolean("Unbreakable");
            }
            if (tag.hasKey("HideFlags")) {
                hideFlags = tag.getInteger("HideFlags");
            }
            if (tag.hasKey("CanBreak")) {
                NBTTagList cb = tag.getTagList("CanBreak", 8);
                for (int i = 0; i < cb.tagCount(); i++) canBreak.add(cb.getStringTagAt(i));
            }
            if (tag.hasKey("CanPlaceOn")) {
                NBTTagList cp = tag.getTagList("CanPlaceOn", 8);
                for (int i = 0; i < cp.tagCount(); i++) canPlaceOn.add(cp.getStringTagAt(i));
            }
            if (tag.hasKey("AttributeModifiers")) {
                NBTTagList attrs = tag.getTagList("AttributeModifiers", 10);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < attrs.tagCount(); i++) {
                    NBTTagCompound a = attrs.getCompoundTagAt(i);
                    String name   = a.hasKey("AttributeName") ? a.getString("AttributeName") : "";
                    String amount = a.hasKey("Amount") ? String.valueOf(a.getDouble("Amount")) : "0.0";
                    int    op     = a.hasKey("Operation") ? a.getInteger("Operation") : 0;
                    if (!name.isEmpty()) {
                        if (sb.length() > 0) sb.append(',');
                        sb.append(name).append(':').append(amount).append(':').append(op);
                    }
                }
                attributeStr = sb.toString();
            }
            if (tag.hasKey("RepairCost")) {
                repairCost = tag.getInteger("RepairCost");
            }
            if (tag.hasKey("display")) {
                NBTTagCompound disp = tag.getCompoundTag("display");
                if (disp.hasKey("color")) {
                    leatherColor = disp.getInteger("color");
                }
            }
            if (tag.hasKey("ench")) {
                NBTTagList enchList = tag.getTagList("ench", 10);
                for (int i = 0; i < enchList.tagCount(); i++) {
                    NBTTagCompound e = enchList.getCompoundTagAt(i);
                    if (e.getShort("id") == 0 && e.getShort("lvl") == 0) { glintEnchant = true; break; }
                }
            }
            if (tag.hasKey("LodestoneTracked")) {
                lodestoneTracked = tag.getBoolean("LodestoneTracked");
                lodestoneTrackedSet = true;
            }
            if (tag.hasKey("title")) { bookTitle = unconvertColors(tag.getString("title")); bookTagsSet = true; }
            if (tag.hasKey("author")) { bookAuthor = tag.getString("author"); bookTagsSet = true; }
            if (tag.hasKey("pages")) {
                NBTTagList pages = tag.getTagList("pages", 8);
                for (int i = 0; i < pages.tagCount(); i++) bookPages.add(unconvertColors(pages.getStringTagAt(i)));
                bookTagsSet = true;
            }
            if (tag.hasKey("StoredEnchantments")) {
                NBTTagList se = tag.getTagList("StoredEnchantments", 10);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < se.tagCount(); i++) {
                    NBTTagCompound e = se.getCompoundTagAt(i);
                    if (sb.length() > 0) sb.append(',');
                    sb.append(e.getShort("id")).append(":").append(e.getShort("lvl"));
                }
                storedEnchantStr = sb.toString();
                storedEnchantSet = true;
            }
            if (tag.hasKey("SkullOwner")) {
                NBTTagCompound owner = tag.getCompoundTag("SkullOwner");
                if (owner.hasKey("Name")) headName = owner.getString("Name");
                if (owner.hasKey("Id"))   headUuid = owner.getString("Id");
                if (owner.hasKey("Properties")) {
                    NBTTagCompound properties = owner.getCompoundTag("Properties");
                    if (properties.hasKey("textures")) {
                        NBTTagList textures = properties.getTagList("textures", 10);
                        if (textures.tagCount() > 0) {
                            NBTTagCompound texture = textures.getCompoundTagAt(0);
                            if (texture.hasKey("Value")) {
                                String url = extractSkinUrlFromValue(texture.getString("Value"));
                                if (url != null) headLink = url;
                            }
                        }
                    }
                }
                headSet = true;
            }
        }
        metaStr  = String.valueOf(stack.getItemDamage());
        ResourceLocation itemLoc = Item.itemRegistry.getNameForObject(stack.getItem());
        itemType = itemLoc != null ? itemLoc.toString() : "";
    }

    public ItemStack buildPreviewStack() {
        ItemStack preview = stack.copy();

        int meta;
        try { meta = Integer.parseInt(metaStr.trim()); }
        catch (NumberFormatException e) { meta = stack.getItemDamage(); }
        preview.setItemDamage(meta);

        NBTTagCompound existing = stack.getTagCompound();
        NBTTagCompound tag = existing != null ? (NBTTagCompound) existing.copy() : new NBTTagCompound();

        // display: Name / Lore / leather color
        String name = customName.trim();
        java.util.List<String> lore = new java.util.ArrayList<String>();
        for (String line : loreLines) lore.add(line);
        while (!lore.isEmpty() && lore.get(lore.size() - 1).trim().isEmpty()) lore.remove(lore.size() - 1);

        if (!name.isEmpty() || !lore.isEmpty() || leatherColor >= 0) {
            NBTTagCompound display = new NBTTagCompound();
            if (!name.isEmpty()) display.setString("Name", convertColors(name));
            if (!lore.isEmpty()) {
                NBTTagList loreList = new NBTTagList();
                for (String line : lore) loreList.appendTag(new NBTTagString(convertColors(line)));
                display.setTag("Lore", loreList);
            }
            if (leatherColor >= 0) display.setInteger("color", leatherColor);
            tag.setTag("display", display);
        } else {
            tag.removeTag("display");
        }

        NBTTagList enchList = new NBTTagList();
        if (glintEnchant) {
            NBTTagCompound e = new NBTTagCompound();
            e.setShort("id", (short) 0);
            e.setShort("lvl", (short) 0);
            enchList.appendTag(e);
        }
        String ench = enchantStr.trim();
        if (!ench.isEmpty()) {
            for (String part : ench.split(",")) {
                part = part.trim();
                if (part.contains(":")) {
                    try {
                        String[] kv = part.split(":");
                        short id  = Short.parseShort(kv[0].trim());
                        short lvl = Short.parseShort(kv[1].trim());
                        if (id == 0 && lvl == 0) continue;
                        NBTTagCompound e = new NBTTagCompound();
                        e.setShort("id", id);
                        e.setShort("lvl", lvl);
                        enchList.appendTag(e);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        if (enchList.tagCount() > 0) tag.setTag("ench", enchList); else tag.removeTag("ench");

        if (unbreakable) tag.setBoolean("Unbreakable", true); else tag.removeTag("Unbreakable");

        if (hideFlags >= 0) tag.setInteger("HideFlags", hideFlags); else tag.removeTag("HideFlags");

        if (!canBreak.isEmpty()) {
            NBTTagList cb = new NBTTagList();
            for (String s : canBreak) cb.appendTag(new NBTTagString(s));
            tag.setTag("CanBreak", cb);
        } else {
            tag.removeTag("CanBreak");
        }

        if (!canPlaceOn.isEmpty()) {
            NBTTagList cp = new NBTTagList();
            for (String s : canPlaceOn) cp.appendTag(new NBTTagString(s));
            tag.setTag("CanPlaceOn", cp);
        } else {
            tag.removeTag("CanPlaceOn");
        }

        if (repairCost >= 0) tag.setInteger("RepairCost", repairCost); else tag.removeTag("RepairCost");

        if (lodestoneTrackedSet) tag.setBoolean("LodestoneTracked", lodestoneTracked); else tag.removeTag("LodestoneTracked");

        if (bookTagsSet) {
            if (!bookTitle.isEmpty()) tag.setString("title", convertColors(bookTitle)); else tag.removeTag("title");
            if (!bookAuthor.isEmpty()) tag.setString("author", bookAuthor); else tag.removeTag("author");
            if (!bookPages.isEmpty()) {
                NBTTagList pages = new NBTTagList();
                for (String p : bookPages) pages.appendTag(new NBTTagString(convertColors(p)));
                tag.setTag("pages", pages);
            } else {
                tag.removeTag("pages");
            }
        }

        if (storedEnchantSet) {
            if (!storedEnchantStr.trim().isEmpty()) {
                NBTTagList se = new NBTTagList();
                for (String part : storedEnchantStr.split(",")) {
                    part = part.trim();
                    if (part.contains(":")) {
                        try {
                            String[] kv = part.split(":");
                            short id  = Short.parseShort(kv[0].trim());
                            short lvl = Short.parseShort(kv[1].trim());
                            NBTTagCompound e = new NBTTagCompound();
                            e.setShort("id", id);
                            e.setShort("lvl", lvl);
                            se.appendTag(e);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (se.tagCount() > 0) tag.setTag("StoredEnchantments", se); else tag.removeTag("StoredEnchantments");
            } else {
                tag.removeTag("StoredEnchantments");
            }
        }

        String attrStr = attributeStr.trim();
        if (!attrStr.isEmpty()) {
            NBTTagList attrs = new NBTTagList();
            int uuidCounter = 1;
            for (String part : attrStr.split(",")) {
                String[] kv = part.split(":");
                if (kv.length < 3) continue;
                try {
                    String attrName  = kv[0].trim();
                    double amount    = Double.parseDouble(kv[1].trim());
                    int    op        = Integer.parseInt(kv[2].trim());
                    String shortName = attrName.contains(".")
                            ? attrName.substring(attrName.lastIndexOf('.') + 1)
                            : attrName;
                    NBTTagCompound a = new NBTTagCompound();
                    a.setString("AttributeName", attrName);
                    a.setString("Name", shortName);
                    a.setDouble("Amount", amount);
                    a.setInteger("Operation", op);
                    a.setLong("UUIDLeast", uuidCounter);
                    a.setLong("UUIDMost", uuidCounter);
                    attrs.appendTag(a);
                    uuidCounter++;
                } catch (Exception ignored) {}
            }
            if (attrs.tagCount() > 0) tag.setTag("AttributeModifiers", attrs); else tag.removeTag("AttributeModifiers");
        } else {
            tag.removeTag("AttributeModifiers");
        }

        if (headSet) {
            String hName = headName.trim();
            String hUuid = headUuid.trim();
            String hLink = headLink.trim();
            if (!hName.isEmpty() || !hUuid.isEmpty() || !hLink.isEmpty()) {
                NBTTagCompound owner = new NBTTagCompound();
                if (!hLink.isEmpty()) {
                    NBTTagCompound properties = new NBTTagCompound();
                    NBTTagList textures = new NBTTagList();
                    NBTTagCompound texture = new NBTTagCompound();
                    texture.setString("Value", buildTexturesValue(hLink));
                    textures.appendTag(texture);
                    properties.setTag("textures", textures);
                    owner.setTag("Properties", properties);
                }
                if (!hUuid.isEmpty()) owner.setString("Id", hUuid);
                if (!hName.isEmpty()) owner.setString("Name", hName);
                tag.setTag("SkullOwner", owner);
            } else {
                tag.removeTag("SkullOwner");
            }
        }

        if (tag.hasNoTags()) {
            preview.setTagCompound(null);
        } else {
            preview.setTagCompound(tag);
        }

        return preview;
    }

    public String buildCommand() {
        String resolvedType = itemType.trim().isEmpty() ? resolveItemName() : itemType.trim();
        int meta;
        try { meta = Integer.parseInt(metaStr.trim()); }
        catch (NumberFormatException e) { meta = stack.getItemDamage(); }
        int count = stack.stackSize;

        String base = resolvedType + " " + count + " " + meta;

        StringBuilder nbt = new StringBuilder("{");
        boolean anyTag = false;

        NBTTagCompound existing = stack.getTagCompound();

        if (existing != null && existing.hasKey("BlockEntityTag")) {
            NBTTagCompound bet = existing.getCompoundTag("BlockEntityTag");
            StringBuilder betSb = new StringBuilder("{");
            boolean betFirst = true;
            if (bet.hasKey("Command")) {
                String cmd = convertColorsCommand(bet.getString("Command"));
                betSb.append("Command:\"").append(escapeNbtString(cmd)).append("\"");
                betFirst = false;
            }
            for (String k : bet.getKeySet()) {
                if (k.equals("Command")) continue;
                if (!betFirst) betSb.append(',');
                betFirst = false;
                betSb.append(k).append(':').append(serialiseNbt(bet.getTag(k)));
            }
            betSb.append('}');
            nbt.append("BlockEntityTag:").append(betSb);
            anyTag = true;
        }

        String name = customName.trim();
        java.util.List<String> lore = new java.util.ArrayList<String>();
        for (String line : loreLines) { lore.add(line); }
        while (!lore.isEmpty() && lore.get(lore.size()-1).trim().isEmpty()) lore.remove(lore.size()-1);

        if (!name.isEmpty() || !lore.isEmpty() || leatherColor >= 0) {
            if (anyTag) nbt.append(',');
            nbt.append("display:{");
            boolean dispFirst = true;
            if (!name.isEmpty()) {
                nbt.append("Name:\"")
                   .append(escapeNbtString(convertColors(name)))
                   .append("\"");
                dispFirst = false;
            }
            if (!lore.isEmpty()) {
                if (!dispFirst) nbt.append(',');
                nbt.append("Lore:[");
                for (int i = 0; i < lore.size(); i++) {
                    if (i > 0) nbt.append(',');
                    nbt.append("\"").append(escapeNbtString(convertColors(lore.get(i)))).append("\"");
                }
                nbt.append("]");
                dispFirst = false;
            }
            if (leatherColor >= 0) {
                if (!dispFirst) nbt.append(',');
                nbt.append("color:").append(leatherColor);
                dispFirst = false;
            }
            nbt.append('}');
            anyTag = true;
        }

        String ench = enchantStr.trim();
        {
            StringBuilder enchSb = new StringBuilder("[");
            boolean enchFirst = true;
            if (glintEnchant) {
                enchSb.append("{id:0s,lvl:0s}");
                enchFirst = false;
            }
            if (!ench.isEmpty()) {
                for (String part : ench.split(",")) {
                    part = part.trim();
                    if (part.contains(":")) {
                        try {
                            String[] kv = part.split(":");
                            short id  = Short.parseShort(kv[0].trim());
                            short lvl = Short.parseShort(kv[1].trim());
                            if (id == 0 && lvl == 0) continue;
                            if (!enchFirst) enchSb.append(',');
                            enchFirst = false;
                            enchSb.append("{id:").append(id).append("s,lvl:")
                                  .append(lvl).append("s}");
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            enchSb.append(']');
            if (!enchFirst) {
                if (anyTag) nbt.append(',');
                nbt.append("ench:").append(enchSb);
                anyTag = true;
            }
        }

        if (unbreakable) {
            if (anyTag) nbt.append(',');
            nbt.append("Unbreakable:1b");
            anyTag = true;
        }

        if (hideFlags >= 0) {
            if (anyTag) nbt.append(',');
            nbt.append("HideFlags:").append(hideFlags);
            anyTag = true;
        }

        if (!canBreak.isEmpty()) {
            if (anyTag) nbt.append(',');
            nbt.append("CanBreak:[");
            for (int i = 0; i < canBreak.size(); i++) {
                if (i > 0) nbt.append(',');
                nbt.append("\"").append(canBreak.get(i)).append("\"");
            }
            nbt.append(']');
            anyTag = true;
        }

        if (!canPlaceOn.isEmpty()) {
            if (anyTag) nbt.append(',');
            nbt.append("CanPlaceOn:[");
            for (int i = 0; i < canPlaceOn.size(); i++) {
                if (i > 0) nbt.append(',');
                nbt.append("\"").append(canPlaceOn.get(i)).append("\"");
            }
            nbt.append(']');
            anyTag = true;
        }

        if (repairCost >= 0) {
            if (anyTag) nbt.append(',');
            nbt.append("RepairCost:").append(repairCost);
            anyTag = true;
        }

        if (lodestoneTrackedSet) {
            if (anyTag) nbt.append(',');
            nbt.append("LodestoneTracked:").append(lodestoneTracked ? "1b" : "0b");
            anyTag = true;
        }

        if (bookTagsSet) {
            if (!bookTitle.isEmpty()) {
                if (anyTag) nbt.append(',');
                nbt.append("title:\"").append(escapeNbtString(convertColors(bookTitle))).append("\"");
                anyTag = true;
            }
            if (!bookAuthor.isEmpty()) {
                if (anyTag) nbt.append(',');
                nbt.append("author:\"").append(escapeNbtString(bookAuthor)).append("\"");
                anyTag = true;
            }
            if (!bookPages.isEmpty()) {
                if (anyTag) nbt.append(',');
                nbt.append("pages:[");
                for (int i = 0; i < bookPages.size(); i++) {
                    if (i > 0) nbt.append(',');
                    nbt.append("\"").append(escapeNbtString(convertColors(bookPages.get(i)))).append("\"");
                }
                nbt.append(']');
                anyTag = true;
            }
        }

        if (storedEnchantSet && !storedEnchantStr.trim().isEmpty()) {
            StringBuilder seSb = new StringBuilder("[");
            boolean seFirst = true;
            for (String part : storedEnchantStr.split(",")) {
                part = part.trim();
                if (part.contains(":")) {
                    try {
                        String[] kv = part.split(":");
                        short id  = Short.parseShort(kv[0].trim());
                        short lvl = Short.parseShort(kv[1].trim());
                        if (!seFirst) seSb.append(',');
                        seFirst = false;
                        seSb.append("{id:").append(id).append("s,lvl:").append(lvl).append("s}");
                    } catch (NumberFormatException ignored) {}
                }
            }
            seSb.append(']');
            if (!seFirst) {
                if (anyTag) nbt.append(',');
                nbt.append("StoredEnchantments:").append(seSb);
                anyTag = true;
            }
        }

        String attrStr = attributeStr.trim();
        if (!attrStr.isEmpty()) {
            StringBuilder attrSb = new StringBuilder("[");
            boolean attrFirst = true;
            int uuidCounter = 1;
            for (String part : attrStr.split(",")) {
                String[] kv = part.split(":");
                if (kv.length < 3) continue;
                try {
                    String attrName  = kv[0].trim();
                    String amount    = kv[1].trim();
                    int    op        = Integer.parseInt(kv[2].trim());
                    String shortName = attrName.contains(".")
                            ? attrName.substring(attrName.lastIndexOf('.') + 1)
                            : attrName;
                    if (!attrFirst) attrSb.append(',');
                    attrFirst = false;
                    attrSb.append("{AttributeName:\"").append(attrName).append("\",")
                          .append("Name:\"").append(shortName).append("\",")
                          .append("Amount:").append(amount).append("d,")
                          .append("Operation:").append(op).append(",")
                          .append("UUIDLeast:").append(uuidCounter).append("L,")
                          .append("UUIDMost:").append(uuidCounter).append("L}");
                    uuidCounter++;
                } catch (Exception ignored) {}
            }
            attrSb.append(']');
            if (!attrFirst) {
                if (anyTag) nbt.append(',');
                nbt.append("AttributeModifiers:").append(attrSb);
                anyTag = true;
            }
        }

        if (headSet) {
            String hName = headName.trim();
            String hUuid = headUuid.trim();
            String hLink = headLink.trim();
            if (!hName.isEmpty() || !hUuid.isEmpty() || !hLink.isEmpty()) {
                StringBuilder soSb = new StringBuilder("{");
                boolean soFirst = true;
                if (!hLink.isEmpty()) {
                    soSb.append("Properties:{textures:[{Value:\"")
                        .append(buildTexturesValue(hLink))
                        .append("\"}]}");
                    soFirst = false;
                }
                if (!hUuid.isEmpty()) {
                    if (!soFirst) soSb.append(',');
                    soSb.append("Id:\"").append(escapeNbtString(hUuid)).append("\"");
                    soFirst = false;
                }
                if (!hName.isEmpty()) {
                    if (!soFirst) soSb.append(',');
                    soSb.append("Name:\"").append(escapeNbtString(hName)).append("\"");
                    soFirst = false;
                }
                soSb.append('}');
                if (anyTag) nbt.append(',');
                nbt.append("SkullOwner:").append(soSb);
                anyTag = true;
            }
        }

        if (existing != null) {
            for (String key : existing.getKeySet()) {
                if (key.equals("display") || key.equals("ench")
                        || key.equals("BlockEntityTag") || key.equals("Unbreakable")
                        || key.equals("HideFlags") || key.equals("CanBreak") || key.equals("CanPlaceOn")
                        || key.equals("AttributeModifiers")
                        || key.equals("EntityTag")
                        || key.equals("RepairCost")
                        || key.equals("LodestoneTracked")
                        || key.equals("title") || key.equals("author") || key.equals("pages")
                        || key.equals("StoredEnchantments")
                        || key.equals("SkullOwner")) continue;
                if (anyTag) nbt.append(',');
                nbt.append(key).append(':').append(serialiseNbt(existing.getTag(key)));
                anyTag = true;
            }
        }

        nbt.append('}');
        return anyTag ? base + " " + nbt : base;
    }

    private static String escapeNbtString(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static String serialiseNbt(net.minecraft.nbt.NBTBase tag) {
        if (tag instanceof NBTTagCompound) {
            NBTTagCompound c = (NBTTagCompound) tag;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (String k : c.getKeySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append(k).append(':').append(serialiseNbt(c.getTag(k)));
            }
            return sb.append('}').toString();
        } else if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) tag;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.tagCount(); i++) {
                if (i > 0) sb.append(',');
                sb.append(serialiseNbt(list.get(i)));
            }
            return sb.append(']').toString();
        } else if (tag instanceof NBTTagString) {
            String val = ((NBTTagString) tag).getString();
            return "\"" + escapeNbtString(val) + "\"";
        } else {
            return tag.toString();
        }
    }

    private String resolveItemName() {
        ResourceLocation loc = Item.itemRegistry.getNameForObject(stack.getItem());
        return loc != null ? loc.toString() : String.valueOf(Item.getIdFromItem(stack.getItem()));
    }

    
    // Converts § section signs back to & color code characters.
    public static String unconvertColors(String s) {
        if (s == null) return "";
        return s.replace('\u00a7', '&');
    }
    // does the same thing as previous comment but opposite
    public static String convertColors(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                if ("0123456789abcdefklmnorABCDEFKLMNOR".indexOf(next) >= 0) {
                    out.append('\u00a7');
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    public static String buildTexturesValue(String skinUrl) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + jsonEscape(skinUrl)
                + "\",\"metadata\":{\"model\":\"default\"}}}}";
        return base64Encode(json.getBytes(StandardCharsets.UTF_8));
    }

    public static String extractSkinUrlFromValue(String base64Value) {
        if (base64Value == null || base64Value.trim().isEmpty()) return null;
        try {
            byte[] raw = base64Decode(base64Value.trim());
            String json = new String(raw, StandardCharsets.UTF_8);
            return extractJsonStringField(json, "url");
        } catch (Exception e) {
            return null;
        }
    }

    private static final char[] BASE64_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    public static String base64Encode(byte[] data) {
        StringBuilder out = new StringBuilder(((data.length + 2) / 3) * 4);
        int i = 0;
        int len = data.length;
        while (i < len) {
            int b0 = data[i++] & 0xFF;
            int b1 = i < len ? (data[i++] & 0xFF) : -1;
            int b2 = i < len ? (data[i++] & 0xFF) : -1;

            out.append(BASE64_CHARS[b0 >> 2]);
            out.append(BASE64_CHARS[((b0 & 0x03) << 4) | (b1 == -1 ? 0 : (b1 >> 4))]);
            out.append(b1 == -1 ? '=' : BASE64_CHARS[((b1 & 0x0F) << 2) | (b2 == -1 ? 0 : (b2 >> 6))]);
            out.append(b2 == -1 ? '=' : BASE64_CHARS[b2 & 0x3F]);
        }
        return out.toString();
    }
    // pulled from claude
    public static byte[] base64Decode(String data) {
        String cleaned = data.replaceAll("[^A-Za-z0-9+/=]", "");
        int outLen = (cleaned.length() / 4) * 3;
        if (cleaned.endsWith("==")) outLen -= 2;
        else if (cleaned.endsWith("=")) outLen -= 1;
        if (outLen < 0) outLen = 0;
        byte[] out = new byte[outLen];

        int outIdx = 0;
        int buffer = 0, bitsCollected = 0;
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (c == '=') break;
            int val = base64CharValue(c);
            if (val < 0) continue;
            buffer = (buffer << 6) | val;
            bitsCollected += 6;
            if (bitsCollected >= 8) {
                bitsCollected -= 8;
                if (outIdx < out.length) out[outIdx++] = (byte) ((buffer >> bitsCollected) & 0xFF);
            }
        }
        return out;
    }

    private static int base64CharValue(char c) {
        if (c >= 'A' && c <= 'Z') return c - 'A';
        if (c >= 'a' && c <= 'z') return c - 'a' + 26;
        if (c >= '0' && c <= '9') return c - '0' + 52;
        if (c == '+') return 62;
        if (c == '/') return 63;
        return -1;
    }

    private static String jsonEscape(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String extractJsonStringField(String json, String field) {
        String needle = "\"" + field + "\"";
        int keyIdx = json.indexOf(needle);
        if (keyIdx < 0) return null;
        int colon = json.indexOf(':', keyIdx + needle.length());
        if (colon < 0) return null;
        int quoteStart = json.indexOf('"', colon + 1);
        if (quoteStart < 0) return null;

        StringBuilder sb = new StringBuilder();
        for (int i = quoteStart + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case '"':  sb.append('"');  i++; break;
                    case '\\': sb.append('\\'); i++; break;
                    case 'n':  sb.append('\n'); i++; break;
                    case 'r':  sb.append('\r'); i++; break;
                    case 't':  sb.append('\t'); i++; break;
                    case 'u':
                        if (i + 5 < json.length()) {
                            String hex = json.substring(i + 2, i + 6);
                            try { sb.append((char) Integer.parseInt(hex, 16)); i += 5; }
                            catch (NumberFormatException ignored) { sb.append(next); i++; }
                        }
                        break;
                    default: sb.append(next); i++;
                }
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String convertColorsCommand(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                if ("0123456789abcdefklmnorABCDEFKLMNOR".indexOf(next) >= 0) {
                    if (i + 2 < s.length() && s.charAt(i + 2) == '@') {
                        i++;
                        continue;
                    }
                    out.append('\u00a7');
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }
}