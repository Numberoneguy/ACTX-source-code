package net.minecraft.actx.guis;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.actx.selector.searchwidget;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.nbt.NBTTagCompound;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;
public class itemlistmodifier extends GuiScreen {
    private static final int btn_cancel = 0;
    private static final int btn_previous = 1;
    private static final int btn_next = 2;
    private static final int slot_size = 22;
    private int gridcols = 14;
    private int gridrows = 8;
    private int slotsperpage = 100;
    private final EditorState state;
    private final GuiScreen backscreen;
    private java.util.List<ItemStack> allitems = new java.util.ArrayList<>();
    private java.util.List<ItemStack> filtereditems = new java.util.ArrayList<>();
    private int page = 0;
    private int maxpage = 0;
    private searchwidget searchwidget;
    private int startx;
    private int starty;
    private static final int[] extra_potion_metas = {
        0, 16, 32, 64, 8193, 8194, 8195, 8196, 8197, 8198, 8200, 8201, 8202, 8204, 8205, 8206,
        8225, 8226, 8228, 8229, 8233, 8235, 8236, 8238, 8257, 8258, 8259, 8260, 8262, 8264, 8265, 8266, 8268, 8269, 8270,
        16385, 16386, 16387, 16388, 16389, 16390, 16392, 16393, 16394, 16396, 16397, 16398,
        16417, 16418, 16420, 16421, 16425, 16427, 16428, 16430, 16449, 16450, 16451, 16452, 16454, 16456, 16457, 16458, 16460, 16461, 16462,
        84223, 84224, 84231, 84239, 84247, 84255, 84263, 84271, 84279
    };
    public itemlistmodifier(EditorState state, GuiScreen backscreen) {
        this.state = state;
        this.backscreen = backscreen;
        populateItems();
    }
    private void populateItems() {
        allitems.clear();
        Set<Item> processeditems = new HashSet<>();
        for (CreativeTabs tab : CreativeTabs.creativeTabArray) {
            if (tab == null || tab == CreativeTabs.tabAllSearch) continue;
            for (ResourceLocation loc : Item.itemRegistry.getKeys()) {
                Item it = Item.itemRegistry.getObject(loc);
                if (it != null && it.getCreativeTab() == tab) {
                    java.util.List<ItemStack> sub = new java.util.ArrayList<>();
                    try {
                        it.getSubItems(it, tab, sub);
                    } catch (Exception ignored) {}
                    if (sub.isEmpty()) {
                        allitems.add(new ItemStack(it));
                    } else {
                        allitems.addAll(sub);
                    }
                    processeditems.add(it);
                }
            }
        }
        for (ResourceLocation loc : Item.itemRegistry.getKeys()) {
            Item it = Item.itemRegistry.getObject(loc);
            if (it != null && !processeditems.contains(it)) {
                java.util.List<ItemStack> sub = new java.util.ArrayList<>();
                if (it == Items.potionitem) {
                    for (int meta : extra_potion_metas) {
                        sub.add(new ItemStack(it, 1, meta));
                    }
                } else {
                    try {
                        it.getSubItems(it, CreativeTabs.tabAllSearch, sub);
                    } catch (Exception ignored) {}
                    if (sub.isEmpty()) {
                        try {
                            it.getSubItems(it, null, sub);
                        } catch (Exception ignored) {}
                    }
                }
                if (sub.isEmpty()) {
                    allitems.add(new ItemStack(it));
                } else {
                    allitems.addAll(sub);
                }
            }
        }
        filtereditems.addAll(allitems);
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        int availablew = this.width - 40;
        int availableh = this.height - 80;
        this.gridcols = Math.max(4, availablew / slot_size);
        this.gridrows = Math.max(2, availableh / slot_size);
        this.slotsperpage = this.gridcols * this.gridrows;
        this.maxpage = (filtereditems.size() - 1) / slotsperpage;
        if (this.page > maxpage) this.page = maxpage;
        if (this.page < 0) this.page = 0;
        int totalgridw = this.gridcols * slot_size;
        this.startx = (this.width - totalgridw) / 2;
        this.starty = 40;
        String oldsearch = searchwidget != null ? searchwidget.getText() : "";
        this.searchwidget = new searchwidget(99, this.fontRendererObj, this.startx, 12, totalgridw, 20);
        this.searchwidget.setText(oldsearch);
        int bottomy = this.height - 25;
        int center = this.width / 2;
        this.buttonList.add(new GuiButton(btn_previous, center - 75, bottomy, 24, 20, "<-"));
        this.buttonList.add(new GuiButton(btn_cancel, center - 47, bottomy, 94, 20, I18n.format("gui.cancel")));
        this.buttonList.add(new GuiButton(btn_next, center + 51, bottomy, 24, 20, "->"));
        updateNavButtons();
    }
    private void applySearch(String query) {
        filtereditems.clear();
        if (query == null || query.trim().isEmpty()) {
            filtereditems.addAll(allitems);
        } else {
            String q = query.toLowerCase().trim();
            for (ItemStack is : allitems) {
                String dname = is.getDisplayName().toLowerCase();
                ResourceLocation loc = Item.itemRegistry.getNameForObject(is.getItem());
                String rname = loc != null ? loc.toString().toLowerCase() : "";
                if (dname.contains(q) || rname.contains(q)) {
                    filtereditems.add(is);
                }
            }
        }
        this.page = 0;
        this.maxpage = (filtereditems.size() - 1) / slotsperpage;
        if (this.maxpage < 0) this.maxpage = 0;
    }
    private void updateNavButtons() {
        for (GuiButton btn : this.buttonList) {
            if (btn.id == btn_previous) btn.enabled = (page > 0);
            if (btn.id == btn_next) btn.enabled = (page < maxpage);
        }
    }
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    @Override
    public void updateScreen() {
        if (searchwidget != null) searchwidget.updateScreen();
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        super.drawScreen(mousex, mousey, partialticks);
        if (searchwidget != null) {
            searchwidget.drawWidget(mousex, mousey);
        }
        int startidx = page * slotsperpage;
        int endidx = Math.min(startidx + slotsperpage, filtereditems.size());
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepth();
        ItemStack hovered = null;
        int hoverx = 0, hovery = 0;
        for (int i = startidx; i < endidx; i++) {
            int local = i - startidx;
            int col = local % gridcols;
            int row = local / gridcols;
            int x = startx + col * slot_size;
            int y = starty + row * slot_size;
            ItemStack is = filtereditems.get(i);
            int itemx = x + (slot_size - 16) / 2;
            int itemy = y + (slot_size - 16) / 2;
            this.itemRender.renderItemIntoGUI(is, itemx, itemy);
            this.itemRender.renderItemOverlays(this.fontRendererObj, is, itemx, itemy);
            if (mousex >= x && mousex < x + slot_size && mousey >= y && mousey < y + slot_size) {
                hovered = is;
                hoverx = mousex;
                hovery = mousey;
            }
        }
        GlStateManager.disableDepth();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        if (hovered != null) {
            java.util.List<String> tip = new java.util.ArrayList<>();
            tip.add(hovered.getDisplayName());
            ResourceLocation loc = Item.itemRegistry.getNameForObject(hovered.getItem());
            if (loc != null) {
                tip.add("\u00a77" + loc.toString());
                tip.add("\u00a78" + I18n.format("gui.act.modifier.meta") + ": " + hovered.getItemDamage());
            }
            drawHoveringText(tip, hoverx, hovery);
        }
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == btn_cancel) {
            this.mc.displayGuiScreen(backscreen);
        } else if (button.id == btn_previous) {
            if (page > 0) { page--; updateNavButtons(); }
        } else if (button.id == btn_next) {
            if (page < maxpage) { page++; updateNavButtons(); }
        }
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        if (searchwidget != null) {
            searchwidget.mouseClicked(mousex, mousey, mousebutton);
        }
        if (mousebutton == 0) {
            int startidx = page * slotsperpage;
            int endidx = Math.min(startidx + slotsperpage, filtereditems.size());
            for (int i = startidx; i < endidx; i++) {
                int local = i - startidx;
                int col = local % gridcols;
                int row = local / gridcols;
                int x = startx + col * slot_size;
                int y = starty + row * slot_size;
                if (mousex >= x && mousex < x + slot_size && mousey >= y && mousey < y + slot_size) {
                    selectItem(filtereditems.get(i));
                    break;
                }
            }
        }
    }
    private void selectItem(ItemStack picked) {
        if (picked == null) return;
        ResourceLocation loc = Item.itemRegistry.getNameForObject(picked.getItem());
        if (loc != null) state.itemType = loc.toString();
        state.metaStr = String.valueOf(picked.getItemDamage());
        state.isCommandBlock = loc != null && loc.getResourcePath().contains("command_block");
        if (state.stack != null) {
            state.stack.setItem(picked.getItem());
            state.stack.setItemDamage(picked.getItemDamage());
            NBTTagCompound tag = picked.getTagCompound();
            if (tag != null) {
                state.stack.setTagCompound((NBTTagCompound) tag.copy());
            } else {
                state.stack.setTagCompound(null);
            }
        } else {
        }
        this.mc.displayGuiScreen(backscreen);
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (keycode == 1) { this.mc.displayGuiScreen(backscreen); return; }
        if (searchwidget != null && searchwidget.isFocused()) {
            boolean handled = searchwidget.keyTyped(typedchar, keycode, query -> {
                applySearch(query);
                updateNavButtons();
            });
            if (handled) return;
        }
        super.keyTyped(typedchar, keycode);
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
}
