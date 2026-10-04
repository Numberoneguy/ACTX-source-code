package net.minecraft.actx.guis;
import java.util.List;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.resources.I18n;
public class commandblockmodifier extends GuiScreen {
    private static final int btn_type = 0;
    private static final int btn_done = 1;
    private static final int btn_cancel = 2;
    private static final int btn_auto = 3;
    private final EditorState state;
    private final GuiScreen backscreen;
    private GuiTextField namefield;
    private GuiTextField commandfield;
    private GuiButton autobutton;
    private int panelx, panely, panelw, panelh;
    private int fieldw, fieldh;
    private int iconx, icony;
    private static final String[] cb_mode_keys = {
        "gui.act.modifier.meta.command.mode.impulse",
        "gui.act.modifier.meta.command.mode.chain",
        "gui.act.modifier.meta.command.mode.repeat"
    };
    private static final String[] cb_trigger_keys = {
        "gui.act.modifier.meta.command.trigger.needsRedstone",
        "gui.act.modifier.meta.command.trigger.alwaysActive"
    };
    private int modeindex = 0;
    private boolean autovalue = false;
    public commandblockmodifier(EditorState state, GuiScreen backscreen) {
        this.state = state;
        this.backscreen = backscreen;
    }
    private NBTTagCompound getOrCreateBlockEntityTag() {
        NBTTagCompound tag = state.stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            state.stack.setTagCompound(tag);
        }
        NBTTagCompound bet = tag.hasKey("BlockEntityTag")
                ? tag.getCompoundTag("BlockEntityTag") : new NBTTagCompound();
        tag.setTag("BlockEntityTag", bet);
        return bet;
    }
    private void loadValues() {
        NBTTagCompound bet = getOrCreateBlockEntityTag();
        String storedname;
        if (bet.hasKey("CustomName", 8)) {
            storedname = bet.getString("CustomName");
        } else if (state.customName != null && !state.customName.isEmpty()) {
            storedname = state.customName;
        } else {
            storedname = "@";
        }
        namefield.setText(storedname.replaceAll("§", "&"));
        String storedcmd = bet.hasKey("Command", 8) ? bet.getString("Command") : "";
        commandfield.setText(storedcmd.replaceAll("§", "&"));
        this.autovalue = bet.hasKey("auto") && bet.getByte("auto") == 1;
        this.modeindex = state.stack.getMetadata();
        if (this.modeindex < 0 || this.modeindex >= cb_mode_keys.length) {
            this.modeindex = 0;
        }
    }
    private void saveValues() {
        NBTTagCompound bet = getOrCreateBlockEntityTag();
        String typedname = namefield.getText();
        String finalname = typedname.isEmpty() ? "@" : typedname.replaceAll("&", "§");
        bet.setString("CustomName", finalname);
        state.customName = finalname;
        String finalcmd = commandfield.getText().replaceAll("&", "§");
        bet.setString("Command", finalcmd);
        bet.setByte("auto", (byte) (autovalue ? 1 : 0));
        bet.setByte("CBMode", (byte) modeindex);
        state.stack.setItemDamage(modeindex);
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        panelw = 500;
        panelh = 164;
        panelx = (this.width - panelw) / 2;
        panely = (this.height - panelh) / 2;
        iconx = panelx + panelw / 2 - 8;
        icony = panely + 6;
        int labelw = 110;
        fieldw = panelw - labelw - 8;
        fieldh = 16;
        int fieldx = panelx + labelw + 4;
        int row1y = icony + 24;
        int row2y = row1y + fieldh + 10;
        int row3y = row2y + fieldh + 10;
        int row4y = row3y + 20 + 8;
        int btnw = (panelw - 12) / 2;
        int btnh = 20;
        int col1 = panelx + 4;
        int col2 = col1 + btnw + 4;
        namefield = new GuiTextField(0, this.fontRendererObj, fieldx, row1y, fieldw, fieldh);
        namefield.setMaxStringLength(32767);
        namefield.setEnableBackgroundDrawing(true);
        namefield.setCursorPositionEnd();
        namefield.setFocused(true);
        commandfield = new GuiTextField(1, this.fontRendererObj, fieldx, row2y, fieldw, fieldh);
        commandfield.setMaxStringLength(32767);
        commandfield.setEnableBackgroundDrawing(true);
        commandfield.setCursorPositionEnd();
        loadValues();
        panelh = (row4y + btnh + 4) - panely;
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(btn_type, col1, row3y, btnw, btnh,
                I18n.format("gui.act.modifier.type") + ": " + I18n.format(cb_mode_keys[modeindex])));
        this.autobutton = new GuiButton(btn_auto, col2, row3y, btnw, btnh, "");
        this.buttonList.add(this.autobutton);
        this.buttonList.add(new GuiButton(btn_done, col1, row4y, btnw, btnh, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(btn_cancel, col2, row4y, btnw, btnh, I18n.format("gui.cancel")));
    }
    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    @Override
    public void updateScreen() {
        namefield.updateCursorCounter();
        commandfield.updateCursorCounter();
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        int row1y = icony + 24;
        int row2y = row1y + fieldh + 10;
        int labelx = panelx + 4;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getRenderItem().renderItemAndEffectIntoGUI(state.stack, iconx, icony);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.fontRendererObj.drawStringWithShadow(I18n.format("gui.act.modifier.meta.command.name") + " :", labelx, row1y + 4, 0xFFFFFF);
        this.fontRendererObj.drawStringWithShadow(I18n.format("gui.act.modifier.meta.command.cmd") + " :", labelx, row2y + 4, 0xFFFFFF);
        namefield.drawTextBox();
        commandfield.drawTextBox();
        super.drawScreen(mousex, mousey, partialticks);
        String autolabel = I18n.format(autovalue ? cb_trigger_keys[1] : cb_trigger_keys[0]);
        int autocolor = autovalue ? 0x00FF00 : 0xFF0000;
        int autotextw = this.fontRendererObj.getStringWidth(autolabel);
        int autotextx = autobutton.xPosition + (autobutton.width - autotextw) / 2;
        int autotexty = autobutton.yPosition + (autobutton.height - 8) / 2;
        this.fontRendererObj.drawStringWithShadow(autolabel, autotextx, autotexty, autocolor);
        boolean hoveringicon = mousex >= iconx && mousex < iconx + 16
                            && mousey >= icony && mousey < icony + 16;
        if (hoveringicon && this.mc.thePlayer != null) {
            List<String> tooltip = state.stack.getTooltip(this.mc.thePlayer, false);
            this.drawHoveringText(tooltip, mousex, mousey);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
        }
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == btn_type) {
            modeindex = (modeindex + 1) % cb_mode_keys.length;
            button.displayString = I18n.format("gui.act.modifier.type") + ": " + I18n.format(cb_mode_keys[modeindex]);
            state.stack.setItemDamage(modeindex);
        } else if (button.id == btn_auto) {
            autovalue = !autovalue;
        } else if (button.id == btn_done) {
            saveValues();
            this.mc.displayGuiScreen(backscreen);
        } else if (button.id == btn_cancel) {
            this.mc.displayGuiScreen(backscreen);
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (namefield.isFocused()) {
            namefield.textboxKeyTyped(typedchar, keycode);
        } else if (commandfield.isFocused()) {
            commandfield.textboxKeyTyped(typedchar, keycode);
        }
        if (keycode == 1) {
            this.mc.displayGuiScreen(backscreen);
        } else if (keycode == 28 || keycode == 156) {
            saveValues();
            this.mc.displayGuiScreen(backscreen);
        } else if (keycode == 15) {
            boolean nf = namefield.isFocused();
            namefield.setFocused(!nf);
            commandfield.setFocused(nf);
        } else {
            super.keyTyped(typedchar, keycode);
        }
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        namefield.mouseClicked(mousex, mousey, mousebutton);
        commandfield.mouseClicked(mousex, mousey, mousebutton);
        boolean inname = mousex >= namefield.xPosition && mousex < namefield.xPosition + fieldw
                      && mousey >= namefield.yPosition && mousey < namefield.yPosition + fieldh;
        boolean incmd = mousex >= commandfield.xPosition && mousex < commandfield.xPosition + fieldw
                      && mousey >= commandfield.yPosition && mousey < commandfield.yPosition + fieldh;
        if (mousebutton == 1) {
            if (inname) namefield.setText("");
            if (incmd) commandfield.setText("");
        }
        if (inname) {
            namefield.setFocused(true);
            commandfield.setFocused(false);
        } else if (incmd) {
            commandfield.setFocused(true);
            namefield.setFocused(false);
        }
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
}
