package net.minecraft.actx.guis;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.EditorState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import java.util.ArrayList;
import java.util.List;
public abstract class listmodifier extends GuiScreen {
    protected final GuiScreen parent;
    protected final List<ListElement> elements = new ArrayList<>();
    protected int page = 0;
    protected int maxpage = 0;
    private GuiButton btndone;
    private GuiButton btncancel;
    private GuiButton btnpreviouspage;
    private GuiButton btnnextpage;
    public listmodifier(GuiScreen parent) {
        this.parent = parent;
    }
    public static abstract class ListElement {
        public final List<GuiButton> buttonList = new ArrayList<>();
        private final int height;
        public ListElement(int height) {
            this.height = height;
        }
        public int getSizeY() {
            return this.height;
        }
        public abstract void draw(int x, int y, int width, int mousex, int mousey, float partialticks);
        public abstract void mouseClicked(int mousex, int mousey, int mousebutton, int x, int y, int width);
        public void keyTyped(char typedchar, int keycode) {}
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        int centerx = this.width / 2;
        this.btndone = new GuiButton(200, centerx - 100, this.height - 26, 99, 20, I18n.format("gui.done"));
        this.btncancel = new GuiButton(201, centerx + 1, this.height - 26, 99, 20, I18n.format("gui.cancel"));
        this.btnpreviouspage = new GuiButton(202, centerx - 130, this.height - 26, 20, 20, "<");
        this.btnnextpage = new GuiButton(203, centerx + 110, this.height - 26, 20, 20, ">");
        this.buttonList.add(this.btndone);
        this.buttonList.add(this.btncancel);
        this.buttonList.add(this.btnpreviouspage);
        this.buttonList.add(this.btnnextpage);
        updatePagination();
    }
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    protected int getItemsPerPage() {
        return 3;
    }
    protected int getContentStartY() {
        return 40;
    }
    protected void updatePagination() {
        int itemsperpage = getItemsPerPage();
        this.maxpage = Math.max(0, (this.elements.size() - 1) / itemsperpage);
        this.page = MathHelper.clamp_int(this.page, 0, this.maxpage);
        if (this.btnpreviouspage != null) {
            this.btnpreviouspage.visible = this.page > 0;
        }
        if (this.btnnextpage != null) {
            this.btnnextpage.visible = this.page < this.maxpage;
        }
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 200) {
            onSave();
            this.mc.displayGuiScreen(this.parent);
        } else if (button.id == 201) {
            this.mc.displayGuiScreen(this.parent);
        } else if (button.id == 202) {
            this.page--;
            updatePagination();
        } else if (button.id == 203) {
            this.page++;
            updatePagination();
        }
    }
    protected abstract void onSave();
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        this.drawDefaultBackground();
        int starty = getContentStartY();
        int elementwidth = 240;
        int startx = (this.width / 2) - (elementwidth / 2);
        int itemsperpage = getItemsPerPage();
        int startindex = this.page * itemsperpage;
        int endindex = Math.min(startindex + itemsperpage, this.elements.size());
        int currenty = starty;
        for (int i = startindex; i < endindex; i++) {
            ListElement el = this.elements.get(i);
            el.draw(startx, currenty, elementwidth, mousex, mousey, partialticks);
            currenty += el.getSizeY() + 6;
        }
        super.drawScreen(mousex, mousey, partialticks);
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        int starty = getContentStartY();
        int elementwidth = 240;
        int startx = (this.width / 2) - (elementwidth / 2);
        int itemsperpage = getItemsPerPage();
        int startindex = this.page * itemsperpage;
        int endindex = Math.min(startindex + itemsperpage, this.elements.size());
        int currenty = starty;
        for (int i = startindex; i < endindex; i++) {
            ListElement el = this.elements.get(i);
            el.mouseClicked(mousex, mousey, mousebutton, startx, currenty, elementwidth);
            currenty += el.getSizeY() + 6;
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (keycode == 1) {
            this.mc.displayGuiScreen(this.parent);
            return;
        }
        int itemsperpage = getItemsPerPage();
        int startindex = this.page * itemsperpage;
        int endindex = Math.min(startindex + itemsperpage, this.elements.size());
        for (int i = startindex; i < endindex; i++) {
            this.elements.get(i).keyTyped(typedchar, keycode);
        }
        super.keyTyped(typedchar, keycode);
    }
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
