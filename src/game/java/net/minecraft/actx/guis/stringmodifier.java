package net.minecraft.actx.guis;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.actx.guis.sublistmodifier;
import net.minecraft.actx.selector.searchwidget;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import java.util.List;
import java.util.ArrayList;
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
public class stringmodifier extends GuiScreen {
    private static final int btn_done = 0;
    private static final int btn_cancel = 1;
    private static final int btn_previous = 2;
    private static final int btn_next = 3;
    private static final int tag_base = 100;
    private static final int tag_stride = 10;
    private static final int off_minus = 0;
    private static final int off_plus = 1;
    private static final int off_copy = 2;
    private static final int off_sub0 = 3;
    private static final int off_sub1 = 4;
    private static final int off_sub2 = 5;
    private final EditorState state;
    private final GuiScreen backscreen;
    private static class TagEntry {
        String key;
        NBTBase tag;
        String typename;
        boolean iscompound;
        boolean islist;
        TagEntry(String key, NBTBase tag) {
            this.key = key;
            this.tag = tag;
            this.typename = typeName(tag);
            this.iscompound = tag instanceof NBTTagCompound;
            this.islist = tag instanceof NBTTagList;
        }
        static String typeName(NBTBase tag) {
            if (tag instanceof NBTTagCompound) return "gui.act.modifier.tag.editor.compound";
            if (tag instanceof NBTTagList) return "gui.act.modifier.tag.editor.list";
            if (tag instanceof NBTTagString) return "item.string.name";
            if (tag instanceof NBTTagByte) return "gui.act.modifier.tag.editor.byte";
            if (tag instanceof NBTTagShort) return "options.renderDistance.short";
            if (tag instanceof NBTTagInt) return "gui.act.modifier.tag.editor.int";
            if (tag instanceof NBTTagLong) return "gui.act.modifier.tag.editor.long";
            if (tag instanceof NBTTagFloat) return "gui.act.modifier.tag.editor.float";
            if (tag instanceof NBTTagDouble) return "gui.act.modifier.tag.editor.double";
            return "gui.act.modifier.tag.editor.unknown";
        }
    }
    private java.util.List<TagEntry> allentries = new java.util.ArrayList<TagEntry>();
    private java.util.List<TagEntry> visible = new java.util.ArrayList<TagEntry>();
    private java.util.List<GuiTextField> valuefields = new java.util.ArrayList<GuiTextField>();
    private java.util.List<Integer> fieldwidths = new java.util.ArrayList<Integer>();
    private java.util.List<Integer> fieldheights = new java.util.ArrayList<Integer>();
    private int searchfieldw, searchfieldh;
    private searchwidget searchfield;
    private static final int page_size = 3;
    private int page = 0;
    stringmodifier(EditorState state, GuiScreen backscreen) {
        this.state = state;
        this.backscreen = backscreen;
    }
    private void rebuildEntries() {
        allentries.clear();
        NBTTagCompound root = state.stack.getTagCompound();
        if (root == null) root = new NBTTagCompound();
        java.util.List<String> keys = new java.util.ArrayList<String>(root.getKeySet());
        java.util.Collections.sort(keys);
        for (String k : keys) {
            allentries.add(new TagEntry(k, root.getTag(k)));
        }
    }
    private void applySearch(String query) {
        visible.clear();
        query = query.toLowerCase().trim();
        for (TagEntry e : allentries) {
            if (query.isEmpty() || e.key.toLowerCase().contains(query)) {
                visible.add(e);
            }
        }
        if (page * page_size >= visible.size()) page = 0;
    }
    private int pageCount() {
        return Math.max(1, (visible.size() + page_size - 1) / page_size);
    }
    private java.util.List<TagEntry> pageEntries() {
        int from = page * page_size;
        int to = Math.min(from + page_size, visible.size());
        if (from >= visible.size()) return new java.util.ArrayList<TagEntry>();
        return visible.subList(from, to);
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        rebuildEntries();
        String searchquery = searchfield != null ? searchfield.getText() : "";
        applySearch(searchquery);
        buildWidgets(searchquery);
    }
    private void buildWidgets(String searchquery) {
        this.buttonList.clear();
        valuefields.clear();
        fieldwidths.clear();
        fieldheights.clear();
        int screenw = this.width;
        int contentw = Math.min(600, screenw - 20);
        int startx = (screenw - contentw) / 2;
        int cury = 12;
        String searchlabel = I18n.format("gui.act.search") + ": ";
        int sfw = contentw - this.fontRendererObj.getStringWidth(searchlabel) - 4;
        int sfx = startx + this.fontRendererObj.getStringWidth(searchlabel) + 4;
        searchfield = new searchwidget(9999, this.fontRendererObj, sfx, cury, sfw, 14);
        searchfield.setText(searchquery);
        searchfieldw = sfw;
        searchfieldh = 14;
        cury += 22;
        int dividery = cury;
        cury += 10;
        java.util.List<TagEntry> entries = pageEntries();
        int btnh = 18;
        int fieldh = 14;
        int gap = 4;
        int smallbtnw = 26;
        int copybtnw = 40;
        for (int i = 0; i < entries.size(); i++) {
            TagEntry e = entries.get(i);
            int base = tag_base + i * tag_stride;
            int fieldw = contentw - smallbtnw * 2 - copybtnw - gap * 3;
            GuiTextField vf = new GuiTextField(i, this.fontRendererObj,
                    startx, cury + btnh + gap, fieldw, fieldh);
            vf.setMaxStringLength(32767);
            vf.setEnableBackgroundDrawing(true);
            vf.setText(tagValueString(e.tag));
            vf.setCursorPositionEnd();
            valuefields.add(vf);
            fieldwidths.add(fieldw);
            fieldheights.add(fieldh);
            int bx = startx + fieldw + gap;
            this.buttonList.add(new GuiButton(base + off_minus, bx, cury + btnh + gap, smallbtnw, fieldh, "\u00a7c-"));
            this.buttonList.add(new GuiButton(base + off_plus, bx + smallbtnw + gap, cury + btnh + gap, smallbtnw, fieldh, "\u00a7a+"));
            this.buttonList.add(new GuiButton(base + off_copy, bx + smallbtnw * 2 + gap * 2, cury + btnh + gap, copybtnw, fieldh, I18n.format("gui.act.give.copy")));
            int suby = cury + btnh + gap + fieldh + gap;
            int subbtnw = (contentw - gap * 2) / 3;
            if (e.islist) {
                this.buttonList.add(new GuiButton(base + off_sub0, startx, suby, subbtnw, btnh, I18n.format("gui.act.modifier.tag.editor.list")));
                this.buttonList.add(new GuiButton(base + off_sub1, startx + subbtnw + gap, suby, subbtnw, btnh, I18n.format("cmd.act.ui.name")));
            } else if (e.iscompound) {
                this.buttonList.add(new GuiButton(base + off_sub0, startx, suby, subbtnw, btnh, I18n.format("gui.act.modifier.meta.setColor")));
                this.buttonList.add(new GuiButton(base + off_sub2, startx + subbtnw + gap, suby, subbtnw, btnh, I18n.format("gui.act.modifier.ench")));
                this.buttonList.add(new GuiButton(base + off_sub1, startx + subbtnw * 2 + gap * 2, suby, subbtnw, btnh, I18n.format("cmd.act.ui.name")));
            } else {
                this.buttonList.add(new GuiButton(base + off_sub0, startx, suby, subbtnw, btnh, I18n.format("gui.act.modifier.meta.setColor")));
                this.buttonList.add(new GuiButton(base + off_sub2, startx + subbtnw + gap, suby, subbtnw, btnh, I18n.format("gui.act.modifier.ench")));
                this.buttonList.add(new GuiButton(base + off_sub1, startx + subbtnw * 2 + gap * 2, suby, subbtnw, btnh, I18n.format("cmd.act.ui.name")));
            }
            cury += btnh + gap + fieldh + gap + btnh + 8;
        }
        int navy = this.height - 28;
        int navbtnw = 80;
        int navtotalw = navbtnw * 4 + gap * 3;
        int navx = (screenw - navtotalw) / 2;
        this.buttonList.add(new GuiButton(btn_previous, navx, navy, navbtnw, 20, "<-"));
        this.buttonList.add(new GuiButton(btn_done, navx + navbtnw + gap, navy, navbtnw, 20, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(btn_cancel, navx + (navbtnw + gap) * 2, navy, navbtnw, 20, I18n.format("gui.cancel")));
        this.buttonList.add(new GuiButton(btn_next, navx + (navbtnw + gap) * 3, navy, navbtnw, 20, "->"));
        for (GuiButton b : this.buttonList) {
            if (b.id == btn_previous) b.enabled = (page > 0);
            if (b.id == btn_next) b.enabled = (page < pageCount() - 1);
        }
    }
    private String tagValueString(NBTBase tag) {
        if (tag instanceof NBTTagString) return ((NBTTagString) tag).getString();
        if (tag instanceof NBTTagCompound || tag instanceof NBTTagList)
            return EditorState.serialiseNbt(tag);
        return tag.toString();
    }
    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    @Override
    public void updateScreen() {
        if (searchfield != null) searchfield.updateScreen();
        for (GuiTextField vf : valuefields) vf.updateCursorCounter();
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        int screenw = this.width;
        int contentw = Math.min(600, screenw - 20);
        int startx = (screenw - contentw) / 2;
        this.fontRendererObj.drawStringWithShadow(I18n.format("gui.act.search") + ":", startx, 16, 0xFFFFFF);
        if (searchfield != null) {
            searchfield.drawWidget(mousex, mousey);
        }
        drawRect(startx, 34, startx + contentw, 35, 0xFF888888);
        java.util.List<TagEntry> entries = pageEntries();
        int cury = 44;
        int btnh = 18;
        int fieldh = 14;
        int gap = 4;
        for (TagEntry e : entries) {
            String typelabel = I18n.format(e.typename);
            if (e.tag instanceof NBTTagList) {
                typelabel = typelabel + "[" + ((NBTTagList) e.tag).tagCount() + "]";
            }
            String header = e.key + " (" + typelabel + ")";
            drawRect(startx, cury, startx + contentw, cury + btnh, 0xCC303030);
            this.fontRendererObj.drawStringWithShadow(header, startx + 4, cury + 5, 0xFFFFFF);
            cury += btnh + gap + fieldh + gap + btnh + 8;
        }
        String pagelabel = (page + 1) + " / " + pageCount();
        int pw = this.fontRendererObj.getStringWidth(pagelabel);
        this.fontRendererObj.drawStringWithShadow(pagelabel,
                (screenw - pw) / 2, this.height - 34, 0xFFAAAAAA);
        for (GuiTextField vf : valuefields) vf.drawTextBox();
        super.drawScreen(mousex, mousey, partialticks);
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        int id = button.id;
        if (id == btn_done) { commitAllFields(); this.mc.displayGuiScreen(backscreen); return; }
        if (id == btn_cancel) { this.mc.displayGuiScreen(backscreen); return; }
        if (id == btn_previous) { commitAllFields(); page--; buildWidgets(searchfield.getText()); return; }
        if (id == btn_next) { commitAllFields(); page++; buildWidgets(searchfield.getText()); return; }
        if (id >= tag_base) {
            int rowindex = (id - tag_base) / tag_stride;
            int offset = (id - tag_base) % tag_stride;
            java.util.List<TagEntry> entries = pageEntries();
            if (rowindex >= entries.size()) return;
            TagEntry e = entries.get(rowindex);
            switch (offset) {
                case off_minus:
                    adjustNumeric(e, -1);
                    commitAllFields();
                    buildWidgets(searchfield.getText());
                    break;
                case off_plus:
                    adjustNumeric(e, +1);
                    commitAllFields();
                    buildWidgets(searchfield.getText());
                    break;
                case off_copy:
                    if (rowindex < valuefields.size()) {
                        setClipboardString(valuefields.get(rowindex).getText());
                    }
                    break;
                case off_sub0:
                    if (rowindex < valuefields.size()) {
                        setClipboardString(valuefields.get(rowindex).getText());
                    }
                    break;
                case off_sub1:
                    this.mc.displayGuiScreen(new sublistmodifier(state, sublistmodifier.mode_name, this));
                    break;
                case off_sub2:
                    this.mc.displayGuiScreen(new sublistmodifier(state, sublistmodifier.mode_ench, this));
                    break;
            }
        }
    }
    private void commitAllFields() {
        java.util.List<TagEntry> entries = pageEntries();
        NBTTagCompound root = state.stack.getTagCompound();
        if (root == null) { root = new NBTTagCompound(); state.stack.setTagCompound(root); }
        for (int i = 0; i < entries.size() && i < valuefields.size(); i++) {
            TagEntry e = entries.get(i);
            String rawval = valuefields.get(i).getText().trim();
            setNbtValue(root, e, rawval);
        }
    }
    private void setNbtValue(NBTTagCompound root, TagEntry e, String rawval) {
        try {
            if (e.tag instanceof NBTTagString) {
                root.setString(e.key, rawval);
            } else if (e.tag instanceof NBTTagByte) {
                root.setByte(e.key, Byte.parseByte(rawval.replace("b","").replace("B","")));
            } else if (e.tag instanceof NBTTagShort) {
                root.setShort(e.key, Short.parseShort(rawval.replace("s","").replace("S","")));
            } else if (e.tag instanceof NBTTagInt) {
                root.setInteger(e.key, Integer.parseInt(rawval));
            } else if (e.tag instanceof NBTTagLong) {
                root.setLong(e.key, Long.parseLong(rawval.replace("L","").replace("l","")));
            } else if (e.tag instanceof NBTTagFloat) {
                root.setFloat(e.key, Float.parseFloat(rawval.replace("f","").replace("F","")));
            } else if (e.tag instanceof NBTTagDouble) {
                root.setDouble(e.key, Double.parseDouble(rawval.replace("d","").replace("D","")));
            } else if (e.tag instanceof NBTTagCompound || e.tag instanceof NBTTagList) {
                NBTBase parsed = net.minecraft.nbt.JsonToNBT.getTagFromJson(rawval);
                root.setTag(e.key, parsed);
            }
        } catch (Exception ignored) {
        }
    }
    private void adjustNumeric(TagEntry e, int delta) {
        NBTTagCompound root = state.stack.getTagCompound();
        if (root == null) { root = new NBTTagCompound(); state.stack.setTagCompound(root); }
        try {
            if (e.tag instanceof NBTTagByte) root.setByte(e.key, (byte)(((NBTTagByte)e.tag).getByte() + delta));
            else if (e.tag instanceof NBTTagShort) root.setShort(e.key, (short)(((NBTTagShort)e.tag).getShort() + delta));
            else if (e.tag instanceof NBTTagInt) root.setInteger(e.key, ((NBTTagInt)e.tag).getInt() + delta);
            else if (e.tag instanceof NBTTagLong) root.setLong(e.key, ((NBTTagLong)e.tag).getLong() + delta);
            else if (e.tag instanceof NBTTagFloat) root.setFloat(e.key, ((NBTTagFloat)e.tag).getFloat() + delta);
            else if (e.tag instanceof NBTTagDouble) root.setDouble(e.key, ((NBTTagDouble)e.tag).getDouble() + delta);
            e.tag = root.getTag(e.key);
            e.typename = TagEntry.typeName(e.tag);
        } catch (Exception ignored) {}
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (keycode == 1) {
            commitAllFields();
            this.mc.displayGuiScreen(backscreen);
            return;
        }
        if (searchfield != null && searchfield.isFocused()) {
            boolean handled = searchfield.keyTyped(typedchar, keycode, query -> {
                commitAllFields();
                applySearch(query);
                buildWidgets(query);
            });
            if (handled) return;
        }
        for (GuiTextField vf : valuefields) {
            if (vf.isFocused()) { vf.textboxKeyTyped(typedchar, keycode); return; }
        }
        super.keyTyped(typedchar, keycode);
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        if (searchfield != null) {
            searchfield.mouseClicked(mousex, mousey, mousebutton);
            if (searchfield.isFocused()) {
                for (GuiTextField vf : valuefields) vf.setFocused(false);
                return;
            }
        }
        for (int i = 0; i < valuefields.size(); i++) {
            GuiTextField vf = valuefields.get(i);
            int fw = i < fieldwidths.size() ? fieldwidths.get(i) : 100;
            int fh = i < fieldheights.size() ? fieldheights.get(i) : 14;
            vf.mouseClicked(mousex, mousey, mousebutton);
            if (isInside(vf, fw, fh, mousex, mousey)) {
                vf.setFocused(true);
                for (int j = 0; j < valuefields.size(); j++) {
                    if (j != i) valuefields.get(j).setFocused(false);
                }
            }
        }
    }
    private boolean isInside(GuiTextField f, int w, int h, int mx, int my) {
        return mx >= f.xPosition && mx < f.xPosition + w
            && my >= f.yPosition && my < f.yPosition + h;
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
}
