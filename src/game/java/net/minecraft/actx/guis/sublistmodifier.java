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
public class sublistmodifier extends GuiScreen {
    static final int mode_name = 0;
    static final int mode_desc = 1;
    static final int mode_ench = 2;
    static final int mode_attr = 3;
    static final int mode_type = 4;
    static final int mode_meta = 5;
    private static final int btn_done = 0;
    private static final int btn_cancel = 1;
    private final EditorState state;
    private final int mode;
    private final GuiScreen backscreen;
    private GuiTextField primaryfield;
    private GuiTextField commandfield;
    private int panelx, panely, panelw, panelh;
    private int fieldw, fieldh;
    sublistmodifier(EditorState state, int mode, GuiScreen backscreen) {
        this.state = state;
        this.mode = mode;
        this.backscreen = backscreen;
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        panelw = 500;
        panelh = 80;
        panelx = (this.width - panelw) / 2;
        panely = (this.height - panelh) / 2;
        int labelw = 110;
        int fieldx = panelx + labelw + 4;
        fieldw = panelw - labelw - 8;
        fieldh = 16;
        int row1y = panely + 14;
        commandfield = null;
        primaryfield = new GuiTextField(0, this.fontRendererObj, fieldx, row1y, fieldw, fieldh);
        primaryfield.setMaxStringLength(32767);
        primaryfield.setEnableBackgroundDrawing(true);
        primaryfield.setFocused(true);
        primaryfield.setText(getCurrentValue());
        primaryfield.setCursorPositionEnd();
        int btnw = (panelw - 12) / 2;
        int btnh = 20;
        int btny = row1y + fieldh + 10;
        int col1 = panelx + 4;
        int col2 = col1 + btnw + 4;
        panelh = (btny + btnh + 4) - panely;
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(btn_done, col1, btny, btnw, btnh, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(btn_cancel, col2, btny, btnw, btnh, I18n.format("gui.cancel")));
    }
    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    @Override
    public void updateScreen() {
        primaryfield.updateCursorCounter();
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        int fieldh = 16;
        int row1y = panely + 14;
        int labelx = panelx + 4;
        this.fontRendererObj.drawStringWithShadow(getLabel() + ":", labelx, row1y + 4, 0xFFFFFF);
        primaryfield.drawTextBox();
        super.drawScreen(mousex, mousey, partialticks);
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == btn_done) {
            applyValue();
            this.mc.displayGuiScreen(backscreen);
        } else if (button.id == btn_cancel) {
            this.mc.displayGuiScreen(backscreen);
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (primaryfield.isFocused()) {
            primaryfield.textboxKeyTyped(typedchar, keycode);
        }
        if (keycode == 1) {
            this.mc.displayGuiScreen(backscreen);
        } else if (keycode == 28 || keycode == 156) {
            applyValue();
            this.mc.displayGuiScreen(backscreen);
        } else {
            super.keyTyped(typedchar, keycode);
        }
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        primaryfield.mouseClicked(mousex, mousey, mousebutton);
        if (isInsideTextField(primaryfield, mousex, mousey)) {
            primaryfield.setFocused(true);
        }
    }
    private boolean isInsideTextField(GuiTextField field, int mx, int my) {
        return mx >= field.xPosition && mx < field.xPosition + fieldw
            && my >= field.yPosition && my < field.yPosition + fieldh;
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
    private String getLabel() {
        switch (mode) {
            case mode_name: return "\u00a76" + I18n.format("gui.act.text");
            case mode_desc: return I18n.format("gui.act.modifier.lore");
            case mode_ench: return I18n.format("gui.act.modifier.ench");
            case mode_attr: return I18n.format("gui.act.modifier.attr");
            case mode_type: return I18n.format("gui.act.modifier.itemType");
            case mode_meta: return I18n.format("gui.act.modifier.meta");
            default: return I18n.format("gui.act.modifier.value");
        }
    }
    private String getCurrentValue() {
        switch (mode) {
            case mode_name: return state.customName;
            case mode_desc: return state.loreLines.isEmpty() ? "" : state.loreLines.get(0);
            case mode_ench: return state.enchantStr;
            case mode_attr: return state.attributeStr;
            case mode_type: return state.itemType;
            case mode_meta: return state.metaStr;
            default: return "";
        }
    }
    private void applyValue() {
        String val = primaryfield.getText();
        switch (mode) {
            case mode_name: state.customName = val; break;
            case mode_desc: if (state.loreLines.isEmpty()) state.loreLines.add(val); else state.loreLines.set(0, val); break;
            case mode_ench: state.enchantStr = val; break;
            case mode_attr: state.attributeStr = val; break;
            case mode_type: state.itemType = val; break;
            case mode_meta: state.metaStr = val; break;
        }
    }
}
