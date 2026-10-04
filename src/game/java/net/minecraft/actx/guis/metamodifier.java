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
public class metamodifier extends GuiScreen {
    private static final int btn_unbreakable = 0;
    private static final int btn_tag_editor = 1;
    private static final int btn_done = 2;
    private final EditorState state;
    private final GuiScreen backscreen;
    private boolean unbreakable;
    private GuiButton unbreakablebtn;
    metamodifier(EditorState state, GuiScreen backscreen) {
        this.state = state;
        this.backscreen = backscreen;
        this.unbreakable = state.unbreakable;
    }
    @Override
    public void initGui() {
        int btnw = 300;
        int btnh = 20;
        int gap = 10;
        int donegap = 24;
        int totalh = btnh * 3 + gap + donegap;
        int startx = (this.width - btnw) / 2;
        int starty = (this.height - totalh) / 2;
        this.buttonList.clear();
        unbreakablebtn = new GuiButton(btn_unbreakable, startx, starty, btnw, btnh, unbreakableLabel());
        GuiButton tageditorbtn = new GuiButton(btn_tag_editor, startx, starty + btnh + gap, btnw, btnh, I18n.format("gui.act.modifier.tag.editor"));
        GuiButton donebtn = new GuiButton(btn_done, startx, starty + btnh * 2 + gap + donegap, btnw, btnh, I18n.format("gui.done"));
        tageditorbtn.enabled = true;
        this.buttonList.add(unbreakablebtn);
        this.buttonList.add(tageditorbtn);
        this.buttonList.add(donebtn);
    }
    private String unbreakableLabel() {
        return (unbreakable ? "\u00a7a" : "\u00a7c") + I18n.format("item.unbreakable");
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        super.drawScreen(mousex, mousey, partialticks);
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case btn_unbreakable:
                unbreakable = !unbreakable;
                unbreakablebtn.displayString = unbreakableLabel();
                break;
            case btn_tag_editor:
                break;
            case btn_done:
                state.unbreakable = this.unbreakable;
                this.mc.displayGuiScreen(backscreen);
                break;
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (keycode == 1) {
            this.mc.displayGuiScreen(backscreen);
        } else {
            super.keyTyped(typedchar, keycode);
        }
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
}
