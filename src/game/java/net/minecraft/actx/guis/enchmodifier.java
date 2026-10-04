package net.minecraft.actx.guis;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.minecraft.actx.selector.searchwidget;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.resources.I18n;
public class enchmodifier extends GuiScreen {
    static final int[][] enchants = {
        { 0, 4, 0}, { 1, 4, 1}, { 2, 4, 2}, { 3, 4, 3}, { 4, 4, 4},
        { 5, 3, 5}, { 6, 3, 6}, { 7, 10,7}, { 8, 3, 8}, { 9, 3, 9},
        { 16, 5,10}, { 17, 5,11}, { 18, 1,12}, { 19, 5,13}, { 20, 2,14},
        { 21, 3,15}, { 32, 5,16}, { 33, 3,17}, { 34, 3,18}, { 35, 1,19},
        { 48, 5,20}, { 49, 3,21}, { 50, 1,22}, { 51, 1,23},
        { 61, 3,24}, { 62, 5,25}, { 70, 1,26}, { 71, 3,27}
    };
    static final String[] names = {
        "enchantment.protect.all","enchantment.protect.fire","enchantment.protect.fall","enchantment.protect.explosion",
        "enchantment.protect.projectile","enchantment.oxygen","enchantment.waterWorker","enchantment.thorns",
        "enchantment.waterWalker","enchantment.frostWalker",
        "enchantment.damage.all","enchantment.damage.undead","enchantment.damage.arthropods","enchantment.knockback","enchantment.fire",
        "enchantment.lootBonus","enchantment.digging","enchantment.untouching","enchantment.durability","enchantment.lootBonusDigger",
        "enchantment.arrowDamage","enchantment.arrowKnockback","enchantment.arrowFire","enchantment.arrowInfinite",
        "enchantment.lootBonusFishing","enchantment.fishingSpeed","enchantment.mending","enchantment.curse.vanishing"
    };
    private static final int btn_done = 0;
    private static final int btn_cancel = 1;
    private static final int btn_previous = 2;
    private static final int btn_next = 3;
    private static final int btn_max_all = 4;
    private static final int btn_max_base = 100;
    private final EditorState state;
    private final GuiScreen backscreen;
    private java.util.List<Integer> visible = new java.util.ArrayList<Integer>();
    private java.util.List<GuiTextField> levelfields = new java.util.ArrayList<GuiTextField>();
    private java.util.List<Integer> levelfieldw = new java.util.ArrayList<Integer>();
    private java.util.List<Integer> levelfieldh = new java.util.ArrayList<Integer>();
    private java.util.List<Boolean> fielderrors = new java.util.ArrayList<Boolean>();
    private int[] levels = new int[enchants.length];
    private searchwidget searchwidget;
    private static final int rows_per_page = 9;
    private int page = 0;
    enchmodifier(EditorState state, GuiScreen backscreen) {
        this.state = state;
        this.backscreen = backscreen;
        parseExistingEnchants();
    }
    private void parseExistingEnchants() {
        java.util.Arrays.fill(levels, 0);
        if (state.enchantStr == null || state.enchantStr.trim().isEmpty()) return;
        for (String part : state.enchantStr.split(",")) {
            part = part.trim();
            if (!part.contains(":")) continue;
            try {
                String[] kv = part.split(":");
                int id = Integer.parseInt(kv[0].trim());
                int lvl = Integer.parseInt(kv[1].trim());
                for (int i = 0; i < enchants.length; i++) {
                    if (enchants[i][0] == id) { levels[i] = lvl; break; }
                }
            } catch (NumberFormatException ignored) {}
        }
    }
    private void commitToState() {
        commitFields();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < enchants.length; i++) {
            if (levels[i] > 0) {
                if (sb.length() > 0) sb.append(',');
                sb.append(enchants[i][0]).append(':').append(levels[i]);
            }
        }
        state.enchantStr = sb.toString();
    }
    private void commitFields() {
        java.util.List<Integer> pageitems = pageItems();
        for (int r = 0; r < levelfields.size() && r < pageitems.size(); r++) {
            int enchidx = pageitems.get(r);
            String txt = levelfields.get(r).getText().trim();
            try {
                if (txt.isEmpty()) {
                    levels[enchidx] = 0;
                    fielderrors.set(r, false);
                } else {
                    levels[enchidx] = Integer.parseInt(txt);
                    fielderrors.set(r, false);
                }
            }
            catch (NumberFormatException e) {
                fielderrors.set(r, true);
            }
        }
    }
    private void applySearch(String query) {
        visible.clear();
        query = query == null ? "" : query.toLowerCase().trim();
        for (int i = 0; i < enchants.length; i++) {
            if (query.isEmpty() || I18n.format(names[enchants[i][2]]).toLowerCase().contains(query)) {
                visible.add(i);
            }
        }
        if (page * rows_per_page * 2 >= visible.size()) page = 0;
    }
    private int pageCount() {
        int perpage = rows_per_page * 2;
        return Math.max(1, (visible.size() + perpage - 1) / perpage);
    }
    private java.util.List<Integer> pageItems() {
        int perpage = rows_per_page * 2;
        int from = page * perpage;
        int to = Math.min(from + perpage, visible.size());
        if (from >= visible.size()) return new java.util.ArrayList<Integer>();
        return visible.subList(from, to);
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        String q = searchwidget != null ? searchwidget.getText() : "";
        applySearch(q);
        buildWidgets(q);
    }
    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    private void buildWidgets(String searchquery) {
        this.buttonList.clear();
        levelfields.clear();
        levelfieldw.clear();
        levelfieldh.clear();
        fielderrors.clear();
        int sw = this.width;
        int sh = this.height;
        String searchlabel = I18n.format("gui.act.search") + ": ";
        int sflabelw = this.fontRendererObj.getStringWidth(searchlabel) + 4;
        int sfx = sflabelw + 10;
        int sfw = sw - sfx - 10;
        searchwidget = new searchwidget(9999, this.fontRendererObj, sfx, 12, sfw, 14);
        searchwidget.setText(searchquery);
        int colw = sw / 2 - 10;
        int labelw = 130;
        int fldw = 50;
        int maxbtnw = 40;
        int rowh = 22;
        int starty = 35;
        int col1x = 10;
        int col2x = sw / 2 + 5;
        java.util.List<Integer> items = pageItems();
        for (int r = 0; r < items.size(); r++) {
            int enchidx = items.get(r);
            int col = r % 2;
            int row = r / 2;
            int x = col == 0 ? col1x : col2x;
            int y = starty + row * rowh;
            int fldx = x + labelw + 4;
            int fldh = 14;
            GuiTextField tf = new GuiTextField(r, this.fontRendererObj, fldx, y + 3, fldw, fldh);
            tf.setMaxStringLength(5);
            tf.setEnableBackgroundDrawing(true);
            String lvlstr = levels[enchidx] > 0 ? String.valueOf(levels[enchidx]) : "";
            tf.setText(lvlstr);
            tf.setCursorPositionEnd();
            levelfields.add(tf);
            levelfieldw.add(fldw);
            levelfieldh.add(fldh);
            fielderrors.add(false);
            int maxx = fldx + fldw + 4;
            this.buttonList.add(new GuiButton(btn_max_base + r, maxx, y + 1, maxbtnw, 18, I18n.format("gui.act.modifier.ench.max")));
        }
        int navy = sh - 28;
        int navbtnw = 80;
        int gap = 4;
        int totalnavw = navbtnw * 5 + gap * 4;
        int navx = (sw - totalnavw) / 2;
        this.buttonList.add(new GuiButton(btn_previous, navx, navy, navbtnw, 20, "<-"));
        this.buttonList.add(new GuiButton(btn_done, navx + (navbtnw + gap), navy, navbtnw, 20, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(btn_max_all, navx + (navbtnw + gap) * 2, navy, navbtnw, 20, I18n.format("gui.act.modifier.ench.max")));
        this.buttonList.add(new GuiButton(btn_cancel, navx + (navbtnw + gap) * 3, navy, navbtnw, 20, I18n.format("gui.cancel")));
        this.buttonList.add(new GuiButton(btn_next, navx + (navbtnw + gap) * 4, navy, navbtnw, 20, "->"));
        for (GuiButton b : this.buttonList) {
            if (b.id == btn_previous) b.enabled = (page > 0);
            if (b.id == btn_next) b.enabled = (page < pageCount() - 1);
        }
    }
    @Override
    public void updateScreen() {
        if (searchwidget != null) searchwidget.updateScreen();
        for (GuiTextField tf : levelfields) tf.updateCursorCounter();
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        commitFields();
        int sw = this.width;
        int col1x = 10;
        int col2x = sw / 2 + 5;
        int labelw = 130;
        int rowh = 22;
        int starty = 35;
        String searchprefix = I18n.format("gui.act.search") + ":";
        this.fontRendererObj.drawStringWithShadow(searchprefix, 10, 16, 0xFFE0A000);
        if (searchwidget != null) {
            searchwidget.drawWidget(mousex, mousey);
        }
        java.util.List<Integer> items = pageItems();
        for (int r = 0; r < items.size(); r++) {
            int enchidx = items.get(r);
            String name = I18n.format(names[enchants[enchidx][2]]);
            int col = r % 2;
            int row = r / 2;
            int x = col == 0 ? col1x : col2x;
            int y = starty + row * rowh;
            int color = 0xFFFFFF;
            if (r < fielderrors.size() && fielderrors.get(r)) {
                color = 0xFF5555;
            } else if (levels[enchidx] == 0) {
                color = 0x777777;
            }
            int namew = this.fontRendererObj.getStringWidth(name + " :");
            this.fontRendererObj.drawStringWithShadow(name + " :", x + labelw - namew, y + 7, color);
        }
        for (GuiTextField tf : levelfields) tf.drawTextBox();
        String pagelabel = (page + 1) + " / " + pageCount();
        int pw = this.fontRendererObj.getStringWidth(pagelabel);
        this.fontRendererObj.drawStringWithShadow(pagelabel, (sw - pw) / 2, this.height - 36, 0xFFAAAAAA);
        super.drawScreen(mousex, mousey, partialticks);
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        int id = button.id;
        if (id == btn_done) { commitToState(); this.mc.displayGuiScreen(backscreen); return; }
        if (id == btn_cancel) { this.mc.displayGuiScreen(backscreen); return; }
        if (id == btn_previous) { commitFields(); page--; buildWidgets(searchwidget.getText()); return; }
        if (id == btn_next) { commitFields(); page++; buildWidgets(searchwidget.getText()); return; }
        if (id == btn_max_all) {
            java.util.List<Integer> items = pageItems();
            for (int enchidx : items) {
                levels[enchidx] = enchants[enchidx][1];
            }
            buildWidgets(searchwidget.getText());
            return;
        }
        if (id >= btn_max_base) {
            int r = id - btn_max_base;
            java.util.List<Integer> items = pageItems();
            if (r < items.size()) {
                int enchidx = items.get(r);
                levels[enchidx] = enchants[enchidx][1];
                if (r < levelfields.size()) {
                    levelfields.get(r).setText(String.valueOf(levels[enchidx]));
                }
            }
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (keycode == 1) { this.mc.displayGuiScreen(backscreen); return; }
        if (searchwidget != null && searchwidget.isFocused()) {
            boolean handled = searchwidget.keyTyped(typedchar, keycode, query -> {
                commitFields();
                applySearch(query);
                buildWidgets(query);
            });
            if (handled) return;
        }
        for (GuiTextField tf : levelfields) {
            if (tf.isFocused()) { tf.textboxKeyTyped(typedchar, keycode); return; }
        }
        super.keyTyped(typedchar, keycode);
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        if (mousebutton == 1) {
            for (GuiButton btn : this.buttonList) {
                if (btn.id == btn_max_all && mousex >= btn.xPosition && mousex < btn.xPosition + btn.width
                    && mousey >= btn.yPosition && mousey < btn.yPosition + btn.height) {
                    java.util.List<Integer> items = pageItems();
                    for (int enchidx : items) {
                        levels[enchidx] = 0;
                    }
                    buildWidgets(searchwidget.getText());
                    return;
                }
            }
        }
        super.mouseClicked(mousex, mousey, mousebutton);
        if (searchwidget != null) {
            searchwidget.mouseClicked(mousex, mousey, mousebutton);
            if (searchwidget.isFocused()) {
                for (GuiTextField tf : levelfields) tf.setFocused(false);
                return;
            }
        }
        for (int i = 0; i < levelfields.size(); i++) {
            GuiTextField tf = levelfields.get(i);
            int fw = i < levelfieldw.size() ? levelfieldw.get(i) : 50;
            int fh = i < levelfieldh.size() ? levelfieldh.get(i) : 14;
            if (isIn(tf, fw, fh, mousex, mousey)) {
                if (mousebutton == 1) {
                    tf.setText("");
                    java.util.List<Integer> items = pageItems();
                    if (i < items.size()) levels[items.get(i)] = 0;
                } else {
                    tf.setFocused(true);
                    if (searchwidget != null) searchwidget.setFocused(false);
                    for (int j = 0; j < levelfields.size(); j++) {
                        if (j != i) levelfields.get(j).setFocused(false);
                    }
                }
                return;
            }
            tf.mouseClicked(mousex, mousey, mousebutton);
        }
    }
    private boolean isIn(GuiTextField f, int w, int h, int mx, int my) {
        return mx >= f.xPosition && mx < f.xPosition + w
            && my >= f.yPosition && my < f.yPosition + h;
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
}
