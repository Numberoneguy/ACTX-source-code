package net.minecraft.actx;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.internal.FileChooserResult;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.guis.itemstackmodifier;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
public class saveditems extends GuiScreen {
    private static final String legacy_storage_key = "actx_saved_items";
    private static boolean legacymigrated = false;
    public static void saveCommand(String command) {
        if (command == null || command.trim().isEmpty()) return;
        actxmiscdata.ensureLoaded();
        migrateLegacyStorageIfNeeded();
        actxmiscdata.addSavedItemIfAbsent(command.trim());
    }
    private static byte[] buildItemsJsonBytes() {
        JSONArray arr = new JSONArray();
        for (String cmd : actxmiscdata.getSavedItems()) {
            arr.put(cmd);
        }
        JSONObject root = new JSONObject();
        root.put("items", arr);
        return root.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    private static void migrateLegacyStorageIfNeeded() {
        if (legacymigrated) return;
        legacymigrated = true;
        try {
            byte[] rawbytes = EagRuntime.getStorage(legacy_storage_key);
            if (rawbytes == null || rawbytes.length == 0) return;
            String raw = new String(rawbytes, java.nio.charset.StandardCharsets.UTF_8).trim();
            if (raw.isEmpty()) return;
            JSONObject root = new JSONObject(raw);
            JSONArray arr = root.optJSONArray("items");
            if (arr == null) return;
            List<String> legacyitems = new ArrayList<String>();
            for (int i = 0; i < arr.length(); i++) {
                String cmd = arr.optString(i, null);
                if (cmd != null && !cmd.trim().isEmpty()) {
                    legacyitems.add(cmd.trim());
                }
            }
            if (!legacyitems.isEmpty()) {
                actxmiscdata.migrateLegacySavedItems(legacyitems);
            }
            EagRuntime.setStorage(legacy_storage_key, new byte[0]);
        } catch (Exception e) {
            legacymigrated = false;
        }
    }
    public static List<ItemStack> getSavedStacks() {
        actxmiscdata.ensureLoaded();
        migrateLegacyStorageIfNeeded();
        List<ItemStack> result = new ArrayList<ItemStack>();
        for (String cmd : actxmiscdata.getSavedItems()) {
            ItemStack stack = parseSavedStack(cmd);
            if (stack != null) result.add(stack);
        }
        return result;
    }
    private static ItemStack parseSavedStack(String cmd) {
        try {
            String raw = cmd.trim();
            if (raw.startsWith("/give ")) {
                int a = raw.indexOf(' ');
                int b = raw.indexOf(' ', a + 1);
                if (b < 0) return null;
                raw = raw.substring(b + 1).trim();
            }
            String[] parts = raw.split(" ", 4);
            if (parts.length < 1) return null;
            net.minecraft.item.Item item = net.minecraft.item.Item.itemRegistry.getObject(new ResourceLocation(parts[0]));
            if (item == null) {
                try { item = net.minecraft.item.Item.getItemById(Integer.parseInt(parts[0])); }
                catch (NumberFormatException ignored) {}
            }
            if (item == null) return null;
            int count = parts.length >= 2 ? Integer.parseInt(parts[1]) : 1;
            int meta = parts.length >= 3 ? Integer.parseInt(parts[2]) : 0;
            ItemStack result = new ItemStack(item, Math.max(1, count), meta);
            if (parts.length >= 4 && !parts[3].isEmpty()) {
                try {
                    net.minecraft.nbt.NBTTagCompound tag = net.minecraft.nbt.JsonToNBT.getTagFromJson(parts[3]);
                    translateDisplayColors(tag);
                    result.setTagCompound(tag);
                } catch (net.minecraft.nbt.NBTException ignored) {}
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    private static void translateDisplayColors(net.minecraft.nbt.NBTTagCompound tag) {
        if (tag == null) return;
        if (tag.hasKey("display", 10)) {
            net.minecraft.nbt.NBTTagCompound display = tag.getCompoundTag("display");
            if (display.hasKey("Name", 8)) {
                display.setString("Name", GuiContainerCreative.translateColorCodes(display.getString("Name")));
            }
            if (display.hasKey("Lore", 9)) {
                net.minecraft.nbt.NBTTagList oldlore = display.getTagList("Lore", 8);
                net.minecraft.nbt.NBTTagList newlore = new net.minecraft.nbt.NBTTagList();
                for (int i = 0; i < oldlore.tagCount(); i++) {
                    newlore.appendTag(new net.minecraft.nbt.NBTTagString(
                            GuiContainerCreative.translateColorCodes(oldlore.getStringTagAt(i))));
                }
                display.setTag("Lore", newlore);
            }
        }
        translateContainerItems(tag);
        if (tag.hasKey("BlockEntityTag", 10)) {
            translateContainerItems(tag.getCompoundTag("BlockEntityTag"));
        }
    }
    private static void translateContainerItems(net.minecraft.nbt.NBTTagCompound holder) {
        if (holder == null || !holder.hasKey("Items", 9)) return;
        net.minecraft.nbt.NBTTagList items = holder.getTagList("Items", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            net.minecraft.nbt.NBTTagCompound itementry = items.getCompoundTagAt(i);
            if (itementry.hasKey("tag", 10)) {
                translateDisplayColors(itementry.getCompoundTag("tag"));
            }
        }
    }
    private void syncFromActxMiscData() {
        savedcommands.clear();
        savedcommands.addAll(actxmiscdata.getSavedItems());
    }
    private static final int slot_size = 18;
    private static final int cols = 3;
    private static final int bottom_h = 28;
    private static final int search_h = 24;
    private static final int top_pad = 4;
    private static final int btn_previous = 10;
    private static final int btn_done = 11;
    private static final int btn_edit = 12;
    private static final int btn_giver = 13;
    private static final int btn_next = 14;
    private static final int btn_delete = 15;
    private static final int btn_import = 16;
    private static final int btn_export = 17;
    private final GuiScreen parentscreen;
    private GuiTextField searchfield;
    private final List<String> savedcommands = new ArrayList<String>();
    private final List<ItemStack> displaystacks = new ArrayList<ItemStack>();
    private final List<Integer> filteredindices = new ArrayList<Integer>();
    private int scrolloffset = 0;
    private int visiblerows = 5;
    private int selectedindex = -1;
    private int gridx, gridy, gridw, gridh;
    private boolean waitingforimport = false;
    public saveditems(GuiScreen parent) {
        this.parentscreen = parent;
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        actxmiscdata.ensureLoaded();
        migrateLegacyStorageIfNeeded();
        syncFromActxMiscData();
        rebuildDisplayStacks();
        gridw = cols * slot_size;
        gridx = (this.width - gridw) / 2;
        int searchy = top_pad;
        int availh = this.height - (searchy + search_h + 2) - bottom_h - 8;
        visiblerows = Math.max(1, availh / slot_size);
        gridh = visiblerows * slot_size;
        int availtop = searchy + search_h + 2;
        int availbot = this.height - bottom_h - 8;
        gridy = availtop + (availbot - availtop - gridh) / 2;
        int sfx = this.fontRendererObj.getStringWidth("\u00a76" + I18n.format("gui.act.search") + ":\u00a7r") + 8;
        int sfw = this.width - sfx - 8;
        this.searchfield = new GuiTextField(99, this.fontRendererObj, sfx, searchy + 4, sfw, 16);
        this.searchfield.setMaxStringLength(256);
        this.searchfield.setEnableBackgroundDrawing(true);
        this.searchfield.setFocused(true);
        rebuildFilter();
        int bary = this.height - bottom_h + 4;
        int btnh = 20;
        int gap = 4;
        int arroww = 20;
        int toprowy = bary - 22;
        int innerw = this.width - gap * 2 - arroww * 2 - gap * 2;
        int cbtnw = (innerw - gap * 2) / 3;
        int x = gap;
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(btn_previous, x, bary, arroww, btnh, "<-"));
        x += arroww + gap;
        this.buttonList.add(new GuiButton(btn_done, x, bary, cbtnw, btnh, I18n.format("gui.done")));
        x += cbtnw + gap;
        this.buttonList.add(new GuiButton(btn_edit, x, bary, cbtnw, btnh, I18n.format("cmd.act.edit")));
        x += cbtnw + gap;
        this.buttonList.add(new GuiButton(btn_giver, x, bary, cbtnw, btnh, I18n.format("key.act.giver")));
        x += cbtnw + gap;
        this.buttonList.add(new GuiButton(btn_next, x, bary, arroww, btnh, "->"));
        int importexportw = 54;
        this.buttonList.add(new GuiButton(btn_import, gap, toprowy, importexportw, btnh, I18n.format("gui.act.saveditems.import")));
        this.buttonList.add(new GuiButton(btn_export, gap + importexportw + gap, toprowy, importexportw, btnh, I18n.format("gui.act.saveditems.export")));
        this.buttonList.add(new GuiButton(btn_delete, this.width - 60, toprowy, 56, btnh, "\u00a7c" + I18n.format("gui.act.delete")));
        updateButtonStates();
    }
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    @Override
    public void updateScreen() {
        this.searchfield.updateCursorCounter();
        if (waitingforimport && EagRuntime.fileChooserHasResult()) {
            waitingforimport = false;
            FileChooserResult result = EagRuntime.getFileChooserResult();
            EagRuntime.clearFileChooserResult();
            if (result != null && result.fileData != null) {
                handleImportResult(result.fileData);
            }
        }
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case btn_previous:
                if (scrolloffset > 0) { scrolloffset--; updateButtonStates(); }
                break;
            case btn_next:
                int maxscroll = Math.max(0, totalRows() - visiblerows);
                if (scrolloffset < maxscroll) { scrolloffset++; updateButtonStates(); }
                break;
            case btn_done:
                this.mc.displayGuiScreen(this.parentscreen);
                break;
            case btn_edit:
                ItemStack inhand = this.mc.thePlayer.getHeldItem();
                if (inhand != null) {
                    this.mc.displayGuiScreen(new itemstackmodifier(this.parentscreen, inhand));
                }
                break;
            case btn_giver:
                if (selectedindex >= 0 && selectedindex < filteredindices.size()) {
                    int origidx = filteredindices.get(selectedindex);
                    if (origidx < displaystacks.size() && displaystacks.get(origidx) != null) {
                        this.mc.displayGuiScreen(giver.open(this, displaystacks.get(origidx)));
                    }
                }
                break;
            case btn_delete:
                deleteSelected();
                break;
            case btn_import:
                waitingforimport = true;
                EagRuntime.displayFileChooser("application/json", ".json");
                break;
            case btn_export:
                EagRuntime.downloadFileWithName("Saved Items.json", buildItemsJsonBytes());
                break;
        }
    }
    private void handleImportResult(byte[] data) {
        try {
            String json = new String(data, java.nio.charset.StandardCharsets.UTF_8).trim();
            JSONArray arr = null;
            if (json.startsWith("[")) {
                arr = new JSONArray(json);
            } else {
                JSONObject root = new JSONObject(json);
                arr = root.optJSONArray("items");
            }
            if (arr == null) return;
            List<String> toimport = new ArrayList<String>();
            for (int i = 0; i < arr.length(); i++) {
                String cmd = arr.optString(i, null);
                if (cmd != null && !cmd.trim().isEmpty()) {
                    toimport.add(cmd.trim());
                }
            }
            int added = actxmiscdata.addSavedItemsIfAbsent(toimport);
            if (added > 0) {
                syncFromActxMiscData();
                rebuildDisplayStacks();
                scrolloffset = 0;
                selectedindex = -1;
                rebuildFilter();
                updateButtonStates();
            }
        } catch (Exception e) {
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (this.searchfield.textboxKeyTyped(typedchar, keycode)) {
            scrolloffset = 0;
            selectedindex = -1;
            rebuildFilter();
            updateButtonStates();
            return;
        }
        if (keycode == 1) {
            this.mc.displayGuiScreen(this.parentscreen);
            return;
        }
        super.keyTyped(typedchar, keycode);
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        this.searchfield.mouseClicked(mousex, mousey, mousebutton);
        int hoveredflat = getHoveredIndex(mousex, mousey);
        if (hoveredflat < 0 || hoveredflat >= filteredindices.size()) {
            return;
        }
        selectedindex = hoveredflat;
        updateButtonStates();
        int origidx = filteredindices.get(hoveredflat);
        ItemStack stack = origidx < displaystacks.size() ? displaystacks.get(origidx) : null;
        if (stack == null) {
            return;
        }
        boolean shiftdown = isShiftKeyDown();
        if (mousebutton == 0) {
            if (shiftdown) {
                copySelected();
            } else {
                this.mc.displayGuiScreen(new itemstackmodifier(this, stack));
            }
        } else if (mousebutton == 1) {
            if (shiftdown) {
                deleteSelected();
            } else {
                this.mc.displayGuiScreen(giver.open(this, stack));
            }
        }
    }
    @Override
    public void handleMouseInput() throws java.io.IOException {
        super.handleMouseInput();
        int wheel = net.lax1dude.eaglercraft.v1_8.Mouse.getDWheel();
        if (wheel != 0) {
            int maxscroll = Math.max(0, totalRows() - visiblerows);
            if (wheel > 0 && scrolloffset > 0) { scrolloffset--; updateButtonStates(); }
            else if (wheel < 0 && scrolloffset < maxscroll) { scrolloffset++; updateButtonStates(); }
        }
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        this.fontRendererObj.drawStringWithShadow("\u00a76" + I18n.format("gui.act.search") + ":\u00a7r", 4, top_pad + 8, 0xFFFFFF);
        this.searchfield.drawTextBox();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepth();
        for (int i = 0; i < visiblerows * cols; i++) {
            int flatindex = scrolloffset * cols + i;
            if (flatindex >= filteredindices.size()) break;
            int col = i % cols;
            int row = i / cols;
            int slotx = gridx + col * slot_size;
            int sloty = gridy + row * slot_size;
            if (flatindex == selectedindex) {
                drawRect(slotx, sloty, slotx + slot_size, sloty + 1, 0xFFFFAA00);
                drawRect(slotx, sloty + slot_size - 1, slotx + slot_size, sloty + slot_size, 0xFFFFAA00);
                drawRect(slotx, sloty, slotx + 1, sloty + slot_size, 0xFFFFAA00);
                drawRect(slotx + slot_size - 1, sloty, slotx + slot_size, sloty + slot_size, 0xFFFFAA00);
            }
            int origidx = filteredindices.get(flatindex);
            if (origidx < displaystacks.size()) {
                ItemStack stack = displaystacks.get(origidx);
                if (stack != null) {
                    float oldz = this.itemRender.zLevel;
                    this.itemRender.zLevel = 100.0F;
                    this.itemRender.renderItemIntoGUI(stack, slotx + 1, sloty + 1);
                    this.itemRender.renderItemOverlays(this.fontRendererObj, stack, slotx + 1, sloty + 1);
                    this.itemRender.zLevel = oldz;
                }
            }
        }
        GlStateManager.disableDepth();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        int hoveredflat = getHoveredIndex(mousex, mousey);
        if (hoveredflat >= 0 && hoveredflat < filteredindices.size()) {
            int origidx = filteredindices.get(hoveredflat);
            if (origidx < displaystacks.size() && displaystacks.get(origidx) != null) {
                drawHoveringText(buildHoverLines(displaystacks.get(origidx)), mousex, mousey);
            }
        }
        super.drawScreen(mousex, mousey, partialticks);
    }
    @Override
    public boolean doesGuiPauseGame() { return true; }
    private int totalRows() {
        return (int) Math.ceil(filteredindices.size() / (double) cols);
    }
    private int getHoveredIndex(int mousex, int mousey) {
        if (mousex < gridx || mousex >= gridx + gridw || mousey < gridy || mousey >= gridy + gridh) return -1;
        int col = (mousex - gridx) / slot_size;
        int row = (mousey - gridy) / slot_size;
        return (scrolloffset + row) * cols + col;
    }
    private List<String> buildHoverLines(ItemStack stack) {
        boolean shiftdown = isShiftKeyDown();
        if (!shiftdown) {
            List<String> lines = new ArrayList<String>(
                    stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips));
            lines.add("\u00a76[\u00a7e" + I18n.format("gui.act.leftClick") + "\u00a76] \u00a7e"
                    + I18n.format("gui.act.give.editor"));
            lines.add("\u00a76[\u00a7e" + I18n.format("gui.act.rightClick") + "\u00a76] \u00a7e"
                    + I18n.format("gui.act.give.give"));
            lines.add("\u00a7eLSHIFT " + I18n.format("gui.act.shift"));
            return lines;
        }
        List<String> lines = new ArrayList<String>();
        int numericid = net.minecraft.item.Item.getIdFromItem(stack.getItem());
        lines.add(stack.getRarity().rarityColor + stack.getDisplayName()
                + " (#" + numericid + "/" + stack.getItemDamage() + ")");
        lines.add("\u00a76[\u00a7e" + I18n.format("gui.act.leftClick") + "\u00a76] \u00a7e"
                + I18n.format("gui.act.give.copy"));
        lines.add("\u00a76[\u00a7e" + I18n.format("gui.act.rightClick") + "\u00a76] \u00a7e"
                + I18n.format("gui.act.delete"));
        ResourceLocation regname = Item.itemRegistry.getNameForObject(stack.getItem());
        String regnamestr = regname != null ? regname.toString() : String.valueOf(numericid);
        CreativeTabs tab = stack.getItem().getCreativeTab();
        String categorylabel = tab != null ? I18n.format(tab.getTranslatedTabLabel()) : I18n.format("gui.none");
        lines.add("\u00a77" + regnamestr + "\u00a7r (" + categorylabel + ")");
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : null;
        int tagcount = tag != null ? tag.getKeySet().size() : 0;
        StringBuilder tagsline = new StringBuilder(
                "\u00a76" + I18n.format("gui.act.tags") + "(\u00a7e" + tagcount + "\u00a76):");
        if (tagcount > 0) {
            tagsline.append(' ');
            boolean first = true;
            for (String key : tag.getKeySet()) {
                if (!first) tagsline.append("\u00a76, ");
                tagsline.append("\u00a7e").append(key);
                first = false;
            }
        }
        lines.add(tagsline.toString());
        return lines;
    }
    private void updateButtonStates() {
        int maxscroll = Math.max(0, totalRows() - visiblerows);
        for (GuiButton btn : this.buttonList) {
            if (btn.id == btn_previous) btn.enabled = scrolloffset > 0;
            if (btn.id == btn_next) btn.enabled = scrolloffset < maxscroll;
            if (btn.id == btn_delete) btn.enabled = selectedindex >= 0 && selectedindex < filteredindices.size();
            if (btn.id == btn_giver) btn.enabled = selectedindex >= 0 && selectedindex < filteredindices.size();
        }
    }
    private void rebuildFilter() {
        filteredindices.clear();
        String query = this.searchfield != null ? this.searchfield.getText().toLowerCase().trim() : "";
        for (int i = 0; i < savedcommands.size(); i++) {
            if (query.isEmpty() || savedcommands.get(i).toLowerCase().contains(query)
                || (i < displaystacks.size() && displaystacks.get(i) != null
                && displaystacks.get(i).getDisplayName().toLowerCase().contains(query))) {
                filteredindices.add(i);
            }
        }
    }
    private void rebuildDisplayStacks() {
        displaystacks.clear();
        for (String cmd : savedcommands) {
            displaystacks.add(parseStack(cmd));
        }
    }
    private void copySelected() {
        if (selectedindex < 0 || selectedindex >= filteredindices.size()) return;
        int origidx = filteredindices.get(selectedindex);
        if (origidx < 0 || origidx >= savedcommands.size()) return;
        String cmd = savedcommands.get(origidx);
        ItemStack stackcopy = origidx < displaystacks.size() ? displaystacks.get(origidx) : null;
        actxmiscdata.insertSavedItem(origidx + 1, cmd);
        syncFromActxMiscData();
        displaystacks.add(origidx + 1, stackcopy);
        rebuildFilter();
        updateButtonStates();
    }
    private void deleteSelected() {
        if (selectedindex < 0 || selectedindex >= filteredindices.size()) return;
        int origidx = filteredindices.get(selectedindex);
        actxmiscdata.removeSavedItem(origidx);
        syncFromActxMiscData();
        displaystacks.remove(origidx);
        selectedindex = -1;
        scrolloffset = 0;
        rebuildFilter();
        updateButtonStates();
    }
    private ItemStack parseStack(String cmd) {
        try {
            String raw = cmd.trim();
            if (raw.startsWith("/give ")) {
                int a = raw.indexOf(' ');
                int b = raw.indexOf(' ', a + 1);
                if (b < 0) return null;
                raw = raw.substring(b + 1).trim();
            }
            String[] parts = raw.split(" ", 4);
            if (parts.length < 1) return null;
            Item item = Item.itemRegistry.getObject(new ResourceLocation(parts[0]));
            if (item == null) {
                try { item = Item.getItemById(Integer.parseInt(parts[0])); }
                catch (NumberFormatException ignored) {}
            }
            if (item == null) return null;
            int count = parts.length >= 2 ? Integer.parseInt(parts[1]) : 1;
            int meta = parts.length >= 3 ? Integer.parseInt(parts[2]) : 0;
            ItemStack result = new ItemStack(item, Math.max(1, count), meta);
            if (parts.length >= 4 && !parts[3].isEmpty()) {
                try {
                    NBTTagCompound tag = JsonToNBT.getTagFromJson(parts[3]);
                    translateDisplayColors(tag);
                    result.setTagCompound(tag);
                } catch (NBTException ignored) {}
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
