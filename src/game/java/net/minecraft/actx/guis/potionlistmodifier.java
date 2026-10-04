package net.minecraft.actx.guis;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.actx.selector.searchwidget;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.resources.I18n;
public class potionlistmodifier extends GuiScreen {
    private static final int btn_cancel = 0;
    private static final int btn_previous = 1;
    private static final int btn_next = 2;
    private static final int slot_size = 22;
    private int gridcols = 5;
    private int gridrows = 5;
    private int slotsperpage = 25;
    private final EditorState state;
    private final GuiScreen backscreen;
    private java.util.List<ItemStack> allpotions = new java.util.ArrayList<>();
    private java.util.List<ItemStack> filteredpotions = new java.util.ArrayList<>();
    private int page = 0;
    private int maxpage = 0;
    private searchwidget searchwidget;
    private int startx;
    private int starty;
    public potionlistmodifier(EditorState state, GuiScreen backscreen) {
        this.state = state;
        this.backscreen = backscreen;
        populatePotions();
    }
    private static final int[] extra_potion_metas = {
        7, 15, 31, 39, 47, 55, 63, 87,
        84223, 84224, 84231, 84239, 84247, 84255, 84263, 84271, 84279,
    };
    private void populatePotions() {
        allpotions.clear();
        if (Items.potionitem != null) {
            java.util.List<ItemStack> sub = new java.util.ArrayList<>();
            try {
                Items.potionitem.getSubItems(Items.potionitem, null, sub);
            } catch (Exception e) {
            }
            if (sub.isEmpty()) {
                allpotions.add(new ItemStack(Items.potionitem, 1, 0));
            } else {
                allpotions.addAll(sub);
            }
            java.util.Set<Integer> seenmetas = new java.util.HashSet<>();
            for (ItemStack existing : allpotions) {
                seenmetas.add(existing.getItemDamage());
            }
            for (int meta : extra_potion_metas) {
                if (seenmetas.add(meta)) {
                    allpotions.add(new ItemStack(Items.potionitem, 1, meta));
                }
            }
        }
        filteredpotions.addAll(allpotions);
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
        this.maxpage = (filteredpotions.size() - 1) / slotsperpage;
        if (this.page > maxpage) this.page = maxpage;
        if (this.page < 0) this.page = 0;
        int totalgridw = this.gridcols * slot_size;
        this.startx = (this.width - totalgridw) / 2;
        this.starty = 40;
        String oldsearch = "";
        if (searchwidget != null) oldsearch = searchwidget.getText();
        int labelw = this.fontRendererObj.getStringWidth(I18n.format("gui.act.search") + " : ");
        int searchx = this.startx + labelw + 6;
        int searchw = totalgridw - labelw - 6;
        this.searchwidget = new searchwidget(99, this.fontRendererObj, searchx, 12, searchw, 20);
        this.searchwidget.setText(oldsearch);
        int bottomy = this.height - 25;
        int center = this.width / 2;
        this.buttonList.add(new GuiButton(btn_previous, center - 75, bottomy, 24, 20, "<-"));
        this.buttonList.add(new GuiButton(btn_cancel, center - 47, bottomy, 94, 20, I18n.format("gui.cancel")));
        this.buttonList.add(new GuiButton(btn_next, center + 51, bottomy, 24, 20, "->"));
        updateNavButtons();
    }
    private void applySearch(String query) {
        filteredpotions.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredpotions.addAll(allpotions);
        } else {
            String q = query.toLowerCase().trim();
            for (ItemStack is : allpotions) {
                if (is.getDisplayName().toLowerCase().contains(q)) {
                    filteredpotions.add(is);
                }
            }
        }
        this.page = 0;
        this.maxpage = (filteredpotions.size() - 1) / slotsperpage;
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
        super.updateScreen();
        if (searchwidget != null) searchwidget.updateScreen();
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        super.drawScreen(mousex, mousey, partialticks);
        if (searchwidget != null) {
            String label = "\u00a76" + I18n.format("gui.act.search") + " : ";
            this.fontRendererObj.drawStringWithShadow(label, this.startx, 18, 0xFFFFFF);
            searchwidget.drawWidget(mousex, mousey);
        }
        int startidx = page * slotsperpage;
        int endidx = Math.min(startidx + slotsperpage, filteredpotions.size());
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
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
            ItemStack is = filteredpotions.get(i);
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
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        if (hovered != null) {
            java.util.List<String> tip = new java.util.ArrayList<>();
            tip.add(hovered.getDisplayName());
            tip.add("\u00a78" + I18n.format("gui.act.modifier.meta") + ": " + hovered.getItemDamage());
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
            int endidx = Math.min(startidx + slotsperpage, filteredpotions.size());
            for (int i = startidx; i < endidx; i++) {
                int local = i - startidx;
                int col = local % gridcols;
                int row = local / gridcols;
                int x = startx + col * slot_size;
                int y = starty + row * slot_size;
                if (mousex >= x && mousex < x + slot_size && mousey >= y && mousey < y + slot_size) {
                    selectPotion(filteredpotions.get(i));
                    break;
                }
            }
        }
    }
    private void selectPotion(ItemStack picked) {
        if (picked == null) return;
        state.stack.setItemDamage(picked.getItemDamage());
        state.metaStr = String.valueOf(picked.getItemDamage());
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
