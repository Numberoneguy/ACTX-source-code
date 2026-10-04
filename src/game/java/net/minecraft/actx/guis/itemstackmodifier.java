package net.minecraft.actx.guis;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.Minecraft;
import net.minecraft.actx.giver;
import net.minecraft.nbt.NBTTagCompound;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.client.resources.I18n;
public class itemstackmodifier extends GuiScreen {
    private static final int btn_name = 0;
    private static final int btn_description = 1;
    private static final int btn_enchant = 2;
    private static final int btn_attributes = 3;
    private static final int btn_type = 4;
    private static final int btn_meta = 5;
    private static final int btn_done = 6;
    private static final int btn_cancel = 7;
    private static final int btn_cmd_block_editor = 8;
    private static final int btn_set_entity = 9;
    private static final int btn_fireworks = 10;
    private static final int btn_potion_type = 11;
    private static final int btn_potion_editor = 12;
    private static final int btn_set_color = 13;
    private static final int btn_head = 14;
    private static final int default_leather_color = 0xA06540;
    private final EditorState state;
    public itemstackmodifier(GuiScreen returnto, ItemStack stack) {
        this.state = new EditorState(returnto, stack);
    }
    private itemstackmodifier(EditorState state) {
        this.state = state;
    }
    @Override
    public void initGui() {
        boolean iscmdblock = state.isCommandBlock;
        boolean isspawnegg = state.isSpawnEgg;
        boolean isfireworks = state.stack != null && state.stack.getItem() == Items.fireworks;
        boolean ispotion = state.stack != null && state.stack.getItem() == Items.potionitem;
        boolean isleatherarmor = state.stack != null && EditorState.isLeatherArmor(state.stack);
        boolean ishead = state.isHead;
        int btnw = 100;
        int btnw2 = 99;
        int btnh = 20;
        int col1 = this.width / 2 - 100;
        int col2 = this.width / 2 + 1;
        int row1 = this.height / 2 - 53;
        int row2 = this.height / 2 - 32;
        int row3 = this.height / 2 - 11;
        int row4 = this.height / 2 + 10;
        int row5 = this.height / 2 + 31;
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(btn_name, col1, row1, btnw, btnh, I18n.format("cmd.act.ui.name")));
        this.buttonList.add(new GuiButton(btn_description, col2, row1, btnw2, btnh, I18n.format("gui.act.modifier.lore")));
        this.buttonList.add(new GuiButton(btn_enchant, col1, row2, btnw, btnh, I18n.format("gui.act.modifier.ench")));
        this.buttonList.add(new GuiButton(btn_attributes, col2, row2, btnw2, btnh, I18n.format("gui.act.modifier.attr")));
        this.buttonList.add(new GuiButton(btn_type, col1, row3, btnw, btnh, I18n.format("gui.act.modifier.type")));
        this.buttonList.add(new GuiButton(btn_meta, col2, row3, btnw2, btnh, I18n.format("gui.act.modifier.meta")));
        int extrarows = 0;
        if (ispotion) {
            this.buttonList.add(new GuiButton(btn_potion_type, col1, row4, btnw, btnh, I18n.format("gui.act.modifier.meta.potion")));
            this.buttonList.add(new GuiButton(btn_potion_editor, col2, row4, btnw2, btnh, I18n.format("gui.act.modifier.meta.potionType")));
            extrarows = 1;
        }
        int conditionalrow = ispotion ? row5 : row4;
        if (iscmdblock) {
            this.buttonList.add(new GuiButton(btn_cmd_block_editor, col1, conditionalrow, btnw + 1 + btnw2, btnh, I18n.format("gui.act.modifier.meta.command")));
            extrarows++;
        } else if (isspawnegg) {
            this.buttonList.add(new GuiButton(btn_set_entity, col1, conditionalrow, btnw + 1 + btnw2, btnh, I18n.format("gui.act.modifier.meta.setEntity")));
            extrarows++;
        } else if (isfireworks) {
            this.buttonList.add(new GuiButton(btn_fireworks, col1, conditionalrow, btnw + 1 + btnw2, btnh, I18n.format("gui.act.modifier.meta.fireworks")));
            extrarows++;
        } else if (isleatherarmor) {
            this.buttonList.add(new GuiButton(btn_set_color, col1, conditionalrow, btnw + 1 + btnw2, btnh, I18n.format("gui.act.modifier.meta.setColor")));
            extrarows++;
        } else if (ishead) {
            this.buttonList.add(new GuiButton(btn_head, col1, conditionalrow, btnw + 1 + btnw2, btnh, I18n.format("gui.act.modifier.head")));
            extrarows++;
        }
        int donerow = this.height / 2 + 15 + (21 * extrarows);
        this.buttonList.add(new GuiButton(btn_done, col1, donerow, btnw, btnh, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(btn_cancel, col2, donerow, btnw2, btnh, I18n.format("gui.cancel")));
        this.previewx = this.width / 2 - 10;
        this.previewy = (this.height / 2 - 74) - (10 * extrarows);
    }
    private int previewx, previewy;
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        super.drawScreen(mousex, mousey, partialticks);
        if (state.stack != null) {
            ItemStack preview = state.buildPreviewStack();
            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableDepth();
            float oldz = this.itemRender.zLevel;
            this.itemRender.zLevel = 100.0F;
            this.itemRender.renderItemAndEffectIntoGUI(preview, previewx, previewy);
            this.itemRender.renderItemOverlays(this.fontRendererObj, preview, previewx, previewy);
            this.itemRender.zLevel = oldz;
            GlStateManager.disableDepth();
            GlStateManager.disableRescaleNormal();
            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
            if (mousex >= previewx && mousex < previewx + 20 && mousey >= previewy && mousey < previewy + 20) {
                this.renderToolTip(preview, mousex, mousey);
            }
        }
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case btn_name:
                this.mc.displayGuiScreen(new sublistmodifier(state, sublistmodifier.mode_name, this));
                break;
            case btn_description:
                this.mc.displayGuiScreen(new stringarraymodifier(state, this));
                break;
            case btn_enchant:
                this.mc.displayGuiScreen(new enchmodifier(state, this));
                break;
            case btn_attributes:
                this.mc.displayGuiScreen(new attributemodifier(state, this));
                break;
            case btn_type:
                this.mc.displayGuiScreen(new itemlistmodifier(state, this));
                break;
            case btn_meta:
                this.mc.displayGuiScreen(new metamodifier(state, this));
                break;
            case btn_cmd_block_editor:
                this.mc.displayGuiScreen(new commandblockmodifier(state, this));
                break;
            case btn_set_entity:
                this.mc.displayGuiScreen(new entitylistmodifier(state, this));
                break;
            case btn_fireworks:
                this.mc.displayGuiScreen(new fireworksmodifier(state, this));
                break;
            case btn_set_color:
                this.mc.displayGuiScreen(new colormodifier(this,
                        newcolor -> state.leatherColor = newcolor,
                        state.leatherColor >= 0 ? state.leatherColor : default_leather_color,
                        default_leather_color));
                break;
            case btn_head:
                this.mc.displayGuiScreen(new headmodifier(state, this));
                break;
            case btn_potion_type:
                this.mc.displayGuiScreen(new potionmodifier(state, this));
                break;
            case btn_potion_editor:
                this.mc.displayGuiScreen(new potionlistmodifier(state, this));
                break;
            case btn_done:
                sendAndClose();
                break;
            case btn_cancel:
                this.mc.displayGuiScreen(state.returnTo);
                break;
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (keycode == 1) this.mc.displayGuiScreen(state.returnTo);
        else super.keyTyped(typedchar, keycode);
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
    private void sendAndClose() {
        String cmd = state.buildCommand();
        GuiScreen ultimateparent = (state.returnTo instanceof giver)
                ? ((giver) state.returnTo).getParentScreen()
                : state.returnTo;
        giver returngui = new giver(ultimateparent, state.stack, cmd);
        this.mc.displayGuiScreen(returngui);
    }
}
