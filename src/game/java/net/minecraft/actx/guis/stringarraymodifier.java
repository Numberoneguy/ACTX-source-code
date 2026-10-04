package net.minecraft.actx.guis;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
public class stringarraymodifier extends GuiScreen {
    private static final int btn_done = 0;
    private static final int btn_cancel = 1;
    private static final int btn_previous = 2;
    private static final int btn_next = 3;
    private static final int btn_add_bottom= 4;
    private static final int line_base = 100;
    private static final int line_stride = 3;
    private static final int off_remove = 0;
    private static final int off_insert = 1;
    private final EditorState state;
    private final GuiScreen backscreen;
    private java.util.List<String> lines = new java.util.ArrayList<String>();
    private java.util.List<GuiTextField> linefields = new java.util.ArrayList<GuiTextField>();
    private java.util.List<Integer> linefieldw = new java.util.ArrayList<Integer>();
    private static final int field_h = 16;
    private static final int rows_per_page = 8;
    private int page = 0;
    stringarraymodifier(EditorState state, GuiScreen backscreen) {
        this.state = state;
        this.backscreen = backscreen;
        lines.addAll(state.loreLines);
        if (lines.isEmpty()) lines.add("");
    }
    private int pageCount() {
        return Math.max(1, (lines.size() + rows_per_page - 1) / rows_per_page);
    }
    private int pageStart() { return page * rows_per_page; }
    private int pageEnd() { return Math.min(pageStart() + rows_per_page, lines.size()); }
    private void commitFields() {
        int start = pageStart();
        for (int i = 0; i < linefields.size(); i++) {
            int lineidx = start + i;
            if (lineidx < lines.size()) {
                lines.set(lineidx, linefields.get(i).getText());
            }
        }
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buildWidgets();
    }
    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    private void buildWidgets() {
        this.buttonList.clear();
        linefields.clear();
        linefieldw.clear();
        int sw = this.width;
        int minusbtnw = 26;
        int plusbtnw = 26;
        int gap = 4;
        int indexw = this.fontRendererObj.getStringWidth("00 : ") + 4;
        int fieldw = sw - indexw - minusbtnw - plusbtnw - gap * 2 - 20;
        int rowh = field_h + gap + 4;
        int starty = 20;
        int start = pageStart();
        int end = pageEnd();
        for (int i = 0; i < end - start; i++) {
            int lineidx = start + i;
            int y = starty + i * rowh;
            int fx = 10 + indexw;
            GuiTextField tf = new GuiTextField(i, this.fontRendererObj, fx, y, fieldw, field_h);
            tf.setMaxStringLength(32767);
            tf.setEnableBackgroundDrawing(true);
            tf.setText(lines.get(lineidx));
            tf.setCursorPositionEnd();
            linefields.add(tf);
            linefieldw.add(fieldw);
            int bx = fx + fieldw + gap;
            int base = line_base + i * line_stride;
            this.buttonList.add(new GuiButton(base + off_remove, bx, y, minusbtnw, field_h, "\u00a7c-"));
            this.buttonList.add(new GuiButton(base + off_insert, bx + minusbtnw + gap, y, plusbtnw, field_h, "\u00a7a+"));
        }
        int addy = starty + (end - start) * rowh + 6;
        int addbtnw = 200;
        int addbtnx = (sw - addbtnw) / 2;
        this.buttonList.add(new GuiButton(btn_add_bottom, addbtnx, addy, addbtnw, 20, "\u00a7a+"));
        int navy = this.height - 28;
        int navbtnw = 80;
        int totalnavw= navbtnw * 4 + gap * 3;
        int navx = (sw - totalnavw) / 2;
        this.buttonList.add(new GuiButton(btn_previous, navx, navy, navbtnw, 20, "<-"));
        this.buttonList.add(new GuiButton(btn_done, navx + (navbtnw + gap), navy, navbtnw, 20, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(btn_cancel, navx + (navbtnw + gap) * 2, navy, navbtnw, 20, I18n.format("gui.cancel")));
        this.buttonList.add(new GuiButton(btn_next, navx + (navbtnw + gap) * 3, navy, navbtnw, 20, "->"));
        for (GuiButton b : this.buttonList) {
            if (b.id == btn_previous) b.enabled = (page > 0);
            if (b.id == btn_next) b.enabled = (page < pageCount() - 1);
        }
    }
    @Override
    public void updateScreen() {
        for (GuiTextField tf : linefields) tf.updateCursorCounter();
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        int starty = 20;
        int rowh = field_h + 4 + 4;
        int indexw = this.fontRendererObj.getStringWidth("00 : ") + 4;
        int start = pageStart();
        for (int i = 0; i < linefields.size(); i++) {
            int lineidx = start + i;
            int y = starty + i * rowh;
            String label = lineidx + " :";
            this.fontRendererObj.drawStringWithShadow(label, 10, y + 4, 0xFFFFFF);
        }
        for (GuiTextField tf : linefields) tf.drawTextBox();
        if (pageCount() > 1) {
            String pg = (page + 1) + " / " + pageCount();
            int pw = this.fontRendererObj.getStringWidth(pg);
            this.fontRendererObj.drawStringWithShadow(pg, (this.width - pw) / 2, this.height - 36, 0xFFAAAAAA);
        }
        super.drawScreen(mousex, mousey, partialticks);
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        int id = button.id;
        if (id == btn_done) {
            commitFields();
            state.loreLines.clear();
            state.loreLines.addAll(lines);
            this.mc.displayGuiScreen(backscreen);
            return;
        }
        if (id == btn_cancel) { this.mc.displayGuiScreen(backscreen); return; }
        if (id == btn_previous) { commitFields(); page--; buildWidgets(); return; }
        if (id == btn_next) { commitFields(); page++; buildWidgets(); return; }
        if (id == btn_add_bottom) {
            commitFields();
            lines.add("");
            page = (lines.size() - 1) / rows_per_page;
            buildWidgets();
            if (!linefields.isEmpty()) linefields.get(linefields.size() - 1).setFocused(true);
            return;
        }
        if (id >= line_base) {
            int i = (id - line_base) / line_stride;
            int offset = (id - line_base) % line_stride;
            commitFields();
            int lineidx = pageStart() + i;
            if (lineidx >= lines.size()) return;
            if (offset == off_remove) {
                if (lines.size() > 1) {
                    lines.remove(lineidx);
                    if (page >= pageCount()) page = pageCount() - 1;
                } else {
                    lines.set(0, "");
                }
            } else if (offset == off_insert) {
                lines.add(lineidx + 1, "");
                if (lineidx + 1 >= pageEnd()) page++;
            }
            buildWidgets();
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (keycode == 1) { this.mc.displayGuiScreen(backscreen); return; }
        for (GuiTextField tf : linefields) {
            if (tf.isFocused()) { tf.textboxKeyTyped(typedchar, keycode); return; }
        }
        super.keyTyped(typedchar, keycode);
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        for (int i = 0; i < linefields.size(); i++) {
            GuiTextField tf = linefields.get(i);
            int fw = i < linefieldw.size() ? linefieldw.get(i) : 100;
            tf.mouseClicked(mousex, mousey, mousebutton);
            if (mousex >= tf.xPosition && mousex < tf.xPosition + fw
                && mousey >= tf.yPosition && mousey < tf.yPosition + field_h) {
                tf.setFocused(true);
                for (int j = 0; j < linefields.size(); j++) {
                    if (j != i) linefields.get(j).setFocused(false);
                }
            }
        }
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
}
