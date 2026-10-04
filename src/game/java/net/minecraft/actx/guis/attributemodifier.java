package net.minecraft.actx.guis;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.actx.selector.searchwidget;
import net.minecraft.actx.selector.buttoncolored;
import net.minecraft.actx.selector.buttonfilehighlight;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.Minecraft;
import java.util.List;
import java.util.ArrayList;
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
public class attributemodifier extends GuiScreen {
    private static final String[][] attr_types = {
        { "attribute.name.generic.followRange", "generic.followRange" },
        { "attribute.name.generic.maxHealth", "generic.maxHealth" },
        { "potion.moveSpeed", "generic.movementSpeed" },
        { "attribute.name.generic.attackDamage", "generic.attackDamage" },
        { "attribute.name.generic.knockbackResistance", "generic.knockbackResistance" },
    };
    private static final double[] attr_min = { 0.0, 0.0, 0.0, 0.0, 0.0 };
    private static final double[] attr_max = { 2048.0, 1024.0, 1024.0, 2048.0, 1.0 };
    private static double clampattr(int typeidx, double v) {
        return Math.max(attr_min[typeidx], Math.min(attr_max[typeidx], v));
    }
    private static String clampamount(int typeidx, String text) {
        try {
            double v = Double.parseDouble(text.trim());
            double c = clampattr(typeidx, v);
            return c == v ? text : String.valueOf(c);
        } catch (NumberFormatException e) {
            return text;
        }
    }
    private static final String[][] operations = {
        { "gui.act.modifier.attr.operation.0", "0" },
        { "gui.act.modifier.attr.operation.1", "1" },
        { "gui.act.modifier.attr.operation.2", "2" },
    };
    private static String attrtypelabel(int idx) {
        return I18n.format(attr_types[idx][0]);
    }
    private static String operationlabel(int idx) {
        return I18n.format(operations[idx][0]) + " (" + operations[idx][1] + ")";
    }
    private static class AttrRow {
        int typeindex = 0;
        String amount = "0.0";
        int opindex = 0;
    }
    private final EditorState state;
    private final GuiScreen backscreen;
    private java.util.List<AttrRow> rows = new java.util.ArrayList<AttrRow>();
    private java.util.List<GuiTextField> amountfields = new java.util.ArrayList<GuiTextField>();
    private java.util.List<Integer> pagerows = new java.util.ArrayList<Integer>();
    private java.util.List<Integer> filtered = new java.util.ArrayList<Integer>();
    private searchwidget searchwidget;
    private String searchquery = "";
    private int page = 0;
    private int currowspercol = 1;
    private int curbasex = 0;
    private int searchx = 0;
    private static final int btn_done = 0;
    private static final int btn_cancel = 1;
    private static final int btn_add = 2;
    private static final int btn_previous = 3;
    private static final int btn_next = 4;
    private static final int row_btn_base = 10;
    private static final int row_btn_count = 5;
    private static final int off_type = 0;
    private static final int off_minus = 1;
    private static final int off_plus = 2;
    private static final int off_op = 3;
    private static final int off_copy = 4;
    private static final int row_h = 56;
    private static final int top_y = 48;
    private static final int btn_h = 20;
    private static final int gap = 4;
    private static final int type_w = 220;
    private static final int amt_lbl = 54;
    private static final int amt_w = 118;
    private static final int pm_w = 21;
    private static final int copy_w = 46;
    private static final int col_w = type_w + gap + amt_lbl + amt_w + gap + pm_w + gap + pm_w;
    private static final int col_gap = 4;
    private static final int search_w = 601;
    private static final int search_y = 18;
    private static final int bar_w = 108;
    private static final int arrow_w = 21;
    private static final int op_w = amt_lbl + amt_w + gap + pm_w + gap + pm_w - gap - copy_w;
    attributemodifier(EditorState state, GuiScreen backscreen) {
        this.state = state;
        this.backscreen = backscreen;
        parsefromstate();
    }
    private void parsefromstate() {
        rows.clear();
        String s = state.attributeStr;
        if (s == null || s.trim().isEmpty()) return;
        for (String part : s.split(",")) {
            String[] kv = part.split(":");
            if (kv.length < 3) continue;
            AttrRow r = new AttrRow();
            r.typeindex = findtypeindex(kv[0]);
            r.amount = kv[1];
            try { r.opindex = Integer.parseInt(kv[2]); } catch (NumberFormatException ignored) {}
            rows.add(r);
        }
    }
    private int findtypeindex(String nbtname) {
        for (int i = 0; i < attr_types.length; i++) {
            if (attr_types[i][1].equalsIgnoreCase(nbtname)) return i;
        }
        return 0;
    }
    private void committostate() {
        syncfieldstorows();
        StringBuilder sb = new StringBuilder();
        for (AttrRow r : rows) {
            if (sb.length() > 0) sb.append(',');
            sb.append(attr_types[r.typeindex][1])
              .append(':').append(r.amount)
              .append(':').append(r.opindex);
        }
        state.attributeStr = sb.toString();
    }
    private void syncfieldstorows() {
        for (int i = 0; i < pagerows.size() && i < amountfields.size(); i++) {
            int ri = pagerows.get(i);
            if (ri < 0 || ri >= rows.size()) continue;
            AttrRow r = rows.get(ri);
            r.amount = clampamount(r.typeindex, amountfields.get(i).getText().trim());
        }
    }
    private boolean rowmatches(AttrRow r) {
        String q = searchquery == null ? "" : searchquery.toLowerCase().trim();
        if (q.isEmpty()) return true;
        String hay = (attrtypelabel(r.typeindex) + " " + operationlabel(r.opindex) + " " + r.amount).toLowerCase();
        return hay.contains(q);
    }
    private void applyfilter() {
        filtered.clear();
        for (int i = 0; i < rows.size(); i++) {
            if (rowmatches(rows.get(i))) filtered.add(i);
        }
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        if (!amountfields.isEmpty()) syncfieldstorows();
        searchx = (this.width - search_w) / 2 + 21;
        String oldsearch = searchwidget != null ? searchwidget.getText() : "";
        boolean focused = searchwidget == null || searchwidget.isFocused();
        searchwidget = new searchwidget(0, this.fontRendererObj, searchx, search_y, search_w, 20);
        searchwidget.setText(oldsearch);
        searchwidget.setFocused(focused);
        searchquery = oldsearch;
        rebuildwidgets();
    }
    private void rebuildwidgets() {
        this.buttonList.clear();
        amountfields.clear();
        pagerows.clear();
        int cx = this.width / 2;
        int bary = this.height - 28;
        int rowspercol = Math.max(1, (bary - gap - btn_h - gap - top_y) / row_h);
        boolean twocols = this.width >= col_w * 2 + col_gap + 20;
        int perpage = rowspercol * (twocols ? 2 : 1);
        applyfilter();
        int pages = Math.max(1, (filtered.size() + perpage - 1) / perpage);
        if (page >= pages) page = pages - 1;
        if (page < 0) page = 0;
        int first = page * perpage;
        int count = Math.max(0, Math.min(perpage, filtered.size() - first));
        int cols = (twocols && count > rowspercol) ? 2 : 1;
        int totalw = cols == 2 ? col_w * 2 + col_gap : col_w;
        int basex = cx - totalw / 2;
        currowspercol = rowspercol;
        curbasex = basex;
        for (int slot = 0; slot < count; slot++) {
            int rowidx = filtered.get(first + slot);
            AttrRow r = rows.get(rowidx);
            pagerows.add(rowidx);
            int btnbase = row_btn_base + slot * row_btn_count;
            int startx = slotx(slot);
            int rowy = sloty(slot);
            this.buttonList.add(new GuiButton(btnbase + off_type,
                    startx, rowy, type_w, btn_h,
                    I18n.format("gui.act.modifier.type") + " \u2013 " + attrtypelabel(r.typeindex)));
            int fieldx = startx + type_w + gap + amt_lbl;
            GuiTextField field = new GuiTextField(slot, this.fontRendererObj,
                    fieldx, rowy, amt_w, btn_h);
            field.setMaxStringLength(67);
            field.setEnableBackgroundDrawing(true);
            field.setText(r.amount);
            amountfields.add(field);
            int pmx = fieldx + amt_w + gap;
            this.buttonList.add(new buttoncolored(btnbase + off_minus,
                    pmx, rowy, pm_w, btn_h, "-", 0xFF0000));
            this.buttonList.add(new buttoncolored(btnbase + off_plus,
                    pmx + pm_w + gap, rowy, pm_w, btn_h, "+", 0x66FF00));
            int line2y = rowy + btn_h + gap;
            int opx = startx + type_w + gap;
            this.buttonList.add(new GuiButton(btnbase + off_op,
                    opx, line2y, op_w, btn_h,
                    I18n.format("gui.act.modifier.attr.operation") + " \u2013 " + operationlabel(r.opindex)));
            this.buttonList.add(new GuiButton(btnbase + off_copy,
                    opx + op_w + gap, line2y, copy_w, btn_h, I18n.format("gui.act.give.copy")));
        }
        int lastcol = count == 0 ? 0 : (count - 1) / rowspercol;
        int lastlines = count == 0 ? 0 : count - lastcol * rowspercol;
        int addw = 110;
        int addx = basex + lastcol * (col_w + col_gap) + col_w / 2 - addw / 2;
        int addy = top_y + lastlines * row_h;
        this.buttonList.add(new buttoncolored(btn_add, addx, addy, addw, btn_h, "+", 0x66FF00));
        int barw = bar_w;
        GuiButton previousbutton = new GuiButton(btn_previous, cx - barw - 2 - gap - arrow_w, bary, arrow_w, btn_h, "<-");
        GuiButton next = new GuiButton(btn_next, cx + 2 + barw + gap, bary, arrow_w, btn_h, "->");
        previousbutton.enabled = page > 0;
        next.enabled = page < pages - 1;
        this.buttonList.add(previousbutton);
        this.buttonList.add(next);
        this.buttonList.add(new GuiButton(btn_done, cx - barw - 2, bary, barw, btn_h, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(btn_cancel, cx + 2, bary, barw, btn_h, I18n.format("gui.cancel")));
    }
    private int slotx(int slot) {
        return curbasex + (slot / currowspercol) * (col_w + col_gap);
    }
    private int sloty(int slot) {
        return top_y + (slot % currowspercol) * row_h;
    }
    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    @Override
    public void updateScreen() {
        for (GuiTextField f : amountfields) f.updateCursorCounter();
        if (searchwidget != null) searchwidget.updateScreen();
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        if (searchwidget != null) searchwidget.drawWidget(mousex, mousey);
        String searchlbl = "\u00a76Search :";
        this.fontRendererObj.drawStringWithShadow(searchlbl,
                searchx - this.fontRendererObj.getStringWidth(searchlbl) - 4, search_y + 6, 0xFFFFFF);
        for (int i = 0; i < pagerows.size(); i++) {
            int labelx = slotx(i) + type_w + 8;
            int rowy = sloty(i);
            this.fontRendererObj.drawStringWithShadow(I18n.format("gui.act.modifier.attr.amount") + " :", labelx, rowy + 6, 0xFFFFFF);
            if (i < amountfields.size()) amountfields.get(i).drawTextBox();
        }
        super.drawScreen(mousex, mousey, partialticks);
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == btn_done) { committostate(); this.mc.displayGuiScreen(backscreen); return; }
        if (button.id == btn_cancel) { this.mc.displayGuiScreen(backscreen); return; }
        if (button.id == btn_add) {
            syncfieldstorows();
            rows.add(new AttrRow());
            searchquery = "";
            if (searchwidget != null) searchwidget.setText("");
            page = Integer.MAX_VALUE;
            rebuildwidgets();
            return;
        }
        if (button.id == btn_previous) { syncfieldstorows(); page--; rebuildwidgets(); return; }
        if (button.id == btn_next) { syncfieldstorows(); page++; rebuildwidgets(); return; }
        int rel = button.id - row_btn_base;
        if (rel < 0) return;
        int slot = rel / row_btn_count;
        int offset = rel % row_btn_count;
        if (slot < 0 || slot >= pagerows.size()) return;
        int rowidx = pagerows.get(slot);
        if (rowidx < 0 || rowidx >= rows.size()) return;
        AttrRow r = rows.get(rowidx);
        switch (offset) {
            case off_type:
                syncfieldstorows();
                this.mc.displayGuiScreen(new AttrTypePicker(this, rowidx));
                break;
            case off_minus:
                syncfieldstorows();
                rows.remove(rowidx);
                rebuildwidgets();
                break;
            case off_plus: {
                syncfieldstorows();
                AttrRow nr = new AttrRow();
                if (!rowmatches(nr)) {
                    searchquery = "";
                    if (searchwidget != null) searchwidget.setText("");
                    page = 0;
                }
                rows.add(rowidx + 1, nr);
                rebuildwidgets();
                break;
            }
            case off_op:
                syncfieldstorows();
                this.mc.displayGuiScreen(new AttrOpPicker(this, rowidx));
                break;
            case off_copy:
                syncfieldstorows();
                AttrRow dup = new AttrRow();
                dup.typeindex = r.typeindex;
                dup.amount = r.amount;
                dup.opindex = r.opindex;
                rows.add(rowidx + 1, dup);
                rebuildwidgets();
                break;
        }
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        boolean anyfield = false;
        for (int i = 0; i < amountfields.size(); i++) {
            GuiTextField f = amountfields.get(i);
            f.mouseClicked(mousex, mousey, mousebutton);
            boolean inside = mousex >= f.xPosition && mousex < f.xPosition + amt_w
                          && mousey >= f.yPosition && mousey < f.yPosition + btn_h;
            f.setFocused(inside);
            if (inside) anyfield = true;
        }
        if (searchwidget != null) {
            searchwidget.mouseClicked(mousex, mousey, mousebutton);
            if (anyfield) searchwidget.setFocused(false);
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        boolean consumed = false;
        for (GuiTextField f : amountfields) {
            if (f.isFocused()) { f.textboxKeyTyped(typedchar, keycode); consumed = true; }
        }
        if (consumed) return;
        if (keycode == 1) { this.mc.displayGuiScreen(backscreen); return; }
        if (searchwidget != null && searchwidget.isFocused()) {
            boolean handled = searchwidget.keyTyped(typedchar, keycode, q -> {
                syncfieldstorows();
                searchquery = q;
                page = 0;
                rebuildwidgets();
            });
            if (handled) return;
        }
        super.keyTyped(typedchar, keycode);
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
    void ontypepicked(int rowidx, int typeidx) {
        if (rowidx >= 0 && rowidx < rows.size()) {
            AttrRow r = rows.get(rowidx);
            r.typeindex = typeidx;
            r.amount = clampamount(typeidx, r.amount);
        }
        rebuildwidgets();
    }
    void onoppicked(int rowidx, int opidx) {
        if (rowidx >= 0 && rowidx < rows.size()) rows.get(rowidx).opindex = opidx;
        rebuildwidgets();
    }
    private abstract class AttrListPicker extends GuiScreen {
        protected final attributemodifier parent;
        protected final int rowidx;
        private searchwidget searchwidget;
        private final java.util.List<Integer> filtered = new java.util.ArrayList<Integer>();
        private String query = "";
        private int page = 0;
        private int searchx = 0;
        private static final int cancel = 0;
        private static final int previous = 1;
        private static final int next = 2;
        private static final int base = 10;
        private static final int item1 = 24;
        AttrListPicker(attributemodifier parent, int rowidx) {
            this.parent = parent; this.rowidx = rowidx;
        }
        abstract int itemcount();
        abstract String itemlabel(int idx);
        abstract void onpicked(int idx);
        private int perpage() { return Math.max(1, (height - 80) / item1); }
        private void applysearch(String q) {
            filtered.clear();
            String lq = q == null ? "" : q.toLowerCase().trim();
            for (int i = 0; i < itemcount(); i++) {
                if (lq.isEmpty() || itemlabel(i).toLowerCase().contains(lq)) filtered.add(i);
            }
        }
        @Override public void initGui() {
            Keyboard.enableRepeatEvents(true);
            searchx = (width - search_w) / 2 + 21;
            String oldsearch = searchwidget != null ? searchwidget.getText() : query;
            searchwidget = new searchwidget(0, fontRendererObj, searchx, search_y, search_w, 20);
            searchwidget.setText(oldsearch);
            searchwidget.setFocused(true);
            query = oldsearch;
            applysearch(query);
            rebuildbuttons();
        }
        private void rebuildbuttons() {
            buttonList.clear();
            int cx = width / 2;
            int btnw = 220;
            int perpage = perpage();
            int pages = Math.max(1, (filtered.size() + perpage - 1) / perpage);
            if (page >= pages) page = pages - 1;
            if (page < 0) page = 0;
            int first = page * perpage;
            int n = Math.max(0, Math.min(perpage, filtered.size() - first));
            int starty = (height - n * item1) / 2;
            for (int i = 0; i < n; i++) {
                int idx = filtered.get(first + i);
                buttonList.add(new GuiButton(base + i,
                        cx - btnw / 2, starty + i * item1, btnw, 20, itemlabel(idx)));
            }
            int bary = height - 28;
            int barw = bar_w;
            GuiButton previousbutton = new GuiButton(previous, cx - barw / 2 - gap - arrow_w, bary, arrow_w, 20, "<-");
            GuiButton nextbutton = new GuiButton(next, cx + barw / 2 + gap, bary, arrow_w, 20, "->");
            previousbutton.enabled = page > 0;
            nextbutton.enabled = page < pages - 1;
            buttonList.add(previousbutton);
            buttonList.add(nextbutton);
            buttonList.add(new GuiButton(cancel, cx - barw / 2, bary, barw, 20, I18n.format("gui.cancel")));
        }
        @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
        @Override public void updateScreen() {
            if (searchwidget != null) searchwidget.updateScreen();
        }
        @Override public void drawScreen(int mousex, int mousey, float partialticks) {
            drawDefaultBackground();
            if (searchwidget != null) searchwidget.drawWidget(mousex, mousey);
            String searchlbl = "\u00a76Search :";
            fontRendererObj.drawStringWithShadow(searchlbl,
                    searchx - fontRendererObj.getStringWidth(searchlbl) - 4, search_y + 6, 0xFFFFFF);
            super.drawScreen(mousex, mousey, partialticks);
        }
        @Override protected void actionPerformed(GuiButton button) {
            if (button.id == cancel) { mc.displayGuiScreen(parent); return; }
            if (button.id == previous) { page--; rebuildbuttons(); return; }
            if (button.id == next) { page++; rebuildbuttons(); return; }
            int fi = page * perpage() + (button.id - base);
            if (button.id >= base && fi >= 0 && fi < filtered.size()) {
                onpicked(filtered.get(fi));
                mc.displayGuiScreen(parent);
            }
        }
        @Override protected void mouseClicked(int mx, int my, int mb) {
            super.mouseClicked(mx, my, mb);
            if (searchwidget != null) searchwidget.mouseClicked(mx, my, mb);
        }
        @Override protected void keyTyped(char c, int key) {
            if (key == 1) { mc.displayGuiScreen(parent); return; }
            if (searchwidget != null && searchwidget.isFocused()) {
                boolean handled = searchwidget.keyTyped(c, key, q -> {
                    query = q;
                    page = 0;
                    applysearch(q);
                    rebuildbuttons();
                });
                if (handled) return;
            }
            super.keyTyped(c, key);
        }
        @Override public boolean doesGuiPauseGame() { return true; }
    }
    private class AttrTypePicker extends AttrListPicker {
        AttrTypePicker(attributemodifier parent, int rowidx) { super(parent, rowidx); }
        @Override int itemcount() { return attr_types.length; }
        @Override String itemlabel(int idx) { return attrtypelabel(idx); }
        @Override void onpicked(int idx) { parent.ontypepicked(rowidx, idx); }
    }
    private class AttrOpPicker extends AttrListPicker {
        AttrOpPicker(attributemodifier parent, int rowidx) { super(parent, rowidx); }
        @Override int itemcount() { return operations.length; }
        @Override String itemlabel(int idx) { return operationlabel(idx); }
        @Override void onpicked(int idx) { parent.onoppicked(rowidx, idx); }
    }
}
