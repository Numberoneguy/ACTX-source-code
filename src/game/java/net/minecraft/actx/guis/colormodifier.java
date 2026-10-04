package net.minecraft.actx.guis;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.client.renderer.RenderHelper;
import java.util.function.Consumer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
public class colormodifier extends GuiScreen {
    private final ResourceLocation picker = new ResourceLocation("textures/gui/picker.png");
    private final GuiScreen parent;
    private final Consumer<Integer> consumer;
    private int color;
    private boolean drag = false;
    private boolean dragr = false;
    private boolean dragg = false;
    private boolean dragb = false;
    private GuiTextField tfr;
    private GuiTextField tfg;
    private GuiTextField tfb;
    private GuiTextField intcolor;
    private GuiTextField hexcolor;
    private GuiButton btnadvanced;
    private static final int btn_done = 0;
    private static final int btn_cancel = 1;
    private static final int btn_advanced = 2;
    private boolean advancedmode = false;
    private int guileft;
    private int guitop;
    private final int guiwidth = 200;
    private final int guiheight = 190;
    private final int canvaswidth = this.guiwidth - 40;
    private final int canvasheight = 150;
    private static final int[] left_metas = {0, 2, 4, 6, 8, 10, 12, 14};
    private static final int[] left_colors = {0x191919, 0x667F33, 0x334CB2, 0x4C7F99, 0x999999, 0x7FCC19, 0x6699D8, 0xD87F33};
    private static final int[] right_metas = {1, 3, 5, 7, 9, 11, 13, 15};
    private static final int[] right_colors = {0x993333, 0x664C33, 0x7F3FB2, 0x4C4C4C, 0xF27FA5, 0xE5E533, 0xB24CD8, 0xFFFFFF};
    public colormodifier(GuiScreen parent, Consumer<Integer> consumer, int initialcolor, int defaultcolor) {
        this.parent = parent;
        this.consumer = consumer;
        this.color = initialcolor;
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.guileft = (this.width - this.guiwidth) / 2;
        this.guitop = (this.height - this.guiheight) / 2;
        int centerleft = this.guileft + 20;
        tfr = new GuiTextField(10, this.fontRendererObj, centerleft + 105, this.guitop + 30, 35, 14);
        tfg = new GuiTextField(11, this.fontRendererObj, centerleft + 105, this.guitop + 60, 35, 14);
        tfb = new GuiTextField(12, this.fontRendererObj, centerleft + 105, this.guitop + 90, 35, 14);
        intcolor = new GuiTextField(13, this.fontRendererObj, centerleft, this.guitop + 120, 140, 14);
        hexcolor = new GuiTextField(14, this.fontRendererObj, centerleft, this.guitop + 150, 140, 14);
        tfr.setMaxStringLength(3);
        tfg.setMaxStringLength(3);
        tfb.setMaxStringLength(3);
        intcolor.setMaxStringLength(10);
        hexcolor.setMaxStringLength(7);
        int btnwidth = 55;
        int spacing = (this.guiwidth - (3 * btnwidth)) / 4;
        this.buttonList.add(new GuiButton(btn_done, this.guileft + spacing, this.guitop + 170, btnwidth, 20, I18n.format("gui.done")));
        this.btnadvanced = new GuiButton(btn_advanced, this.guileft + spacing * 2 + btnwidth, this.guitop + 170, btnwidth, 20, getAdvancedLabel());
        this.buttonList.add(this.btnadvanced);
        this.buttonList.add(new GuiButton(btn_cancel, this.guileft + spacing * 3 + btnwidth * 2, this.guitop + 170, btnwidth, 20, I18n.format("gui.cancel")));
        updateTextFields();
    }
    private String getAdvancedLabel() {
        return (this.advancedmode ? "\u00A7f" : "\u00A77") + I18n.format("Advanced");
    }
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    private void updateTextFields() {
        int r = (this.color >> 16) & 255;
        int g = (this.color >> 8) & 255;
        int b = this.color & 255;
        if (!tfr.isFocused()) tfr.setText(String.valueOf(r));
        if (!tfg.isFocused()) tfg.setText(String.valueOf(g));
        if (!tfb.isFocused()) tfb.setText(String.valueOf(b));
        if (!intcolor.isFocused()) intcolor.setText(String.valueOf(this.color));
        if (!hexcolor.isFocused()) {
            String hex = Integer.toHexString(this.color).toUpperCase();
            while (hex.length() < 6) hex = "0" + hex;
            hexcolor.setText("#" + hex);
        }
    }
    private void updateColor(int newcolor) {
        this.color = newcolor & 0xFFFFFF;
        updateTextFields();
    }
    @Override
    public void updateScreen() {
        if (this.advancedmode) {
            tfr.updateCursorCounter();
            tfg.updateCursorCounter();
            tfb.updateCursorCounter();
            intcolor.updateCursorCounter();
            hexcolor.updateCursorCounter();
        }
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        drawRect(this.guileft - 1, this.guitop - 1, this.guileft + this.guiwidth + 1, this.guitop + this.guiheight + 1, 0xFF000000);
        drawRect(this.guileft, this.guitop + 15, this.guileft + this.guiwidth, this.guitop + this.guiheight, 0xFF222222);
        drawRect(this.guileft, this.guitop, this.guileft + this.guiwidth, this.guitop + 15, 0xFF000000 | this.color);
        for (int i = 0; i < 8; i++) {
            int dyey = this.guitop + 20 + i * 18;
            int leftx = this.guileft + 2;
            int rightx = this.guileft + this.guiwidth - 18;
            drawRect(leftx, dyey, leftx + 16, dyey + 16, 0xFF000000 | left_colors[i]);
            drawRect(rightx, dyey, rightx + 16, dyey + 16, 0xFF000000 | right_colors[i]);
        }
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < 8; i++) {
            int dyey = this.guitop + 20 + i * 18;
            this.mc.getRenderItem().renderItemIntoGUI(new ItemStack(Items.dye, 1, left_metas[i]), this.guileft + 2, dyey);
            this.mc.getRenderItem().renderItemIntoGUI(new ItemStack(Items.dye, 1, right_metas[i]), this.guileft + this.guiwidth - 18, dyey);
        }
        RenderHelper.disableStandardItemLighting();
        if (this.advancedmode) {
            this.fontRendererObj.drawStringWithShadow(I18n.format("gui.act.red") + ":", this.guileft + 20, this.guitop + 18, 0xFFFFFF);
            this.fontRendererObj.drawStringWithShadow(I18n.format("gui.act.green") + ":", this.guileft + 20, this.guitop + 48, 0xFFFFFF);
            this.fontRendererObj.drawStringWithShadow(I18n.format("gui.act.blue") + ":", this.guileft + 20, this.guitop + 78, 0xFFFFFF);
            this.fontRendererObj.drawStringWithShadow(I18n.format("gui.act.modifier.meta.setColor.intColor") + ":", this.guileft + 20, this.guitop + 108, 0xFFFFFF);
            this.fontRendererObj.drawStringWithShadow(I18n.format("gui.act.modifier.meta.setColor.hexColor") + ":", this.guileft + 20, this.guitop + 138, 0xFFFFFF);
            drawSliderTrack(this.guileft + 20, this.guitop + 30, (this.color >> 16) & 255);
            drawSliderTrack(this.guileft + 20, this.guitop + 60, (this.color >> 8) & 255);
            drawSliderTrack(this.guileft + 20, this.guitop + 90, this.color & 255);
            tfr.drawTextBox();
            tfg.drawTextBox();
            tfb.drawTextBox();
            intcolor.drawTextBox();
            hexcolor.drawTextBox();
        } else {
            this.mc.getTextureManager().bindTexture(picker);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GuiScreen.drawModalRectWithCustomSizedTexture(this.guileft + 20, this.guitop + 15, 0, 0, this.canvaswidth, this.canvasheight, this.canvaswidth, this.canvasheight);
        }
        super.drawScreen(mousex, mousey, partialticks);
    }
    private void drawSliderTrack(int x, int y, int value) {
        drawRect(x, y, x + 100, y + 14, 0xFF555555);
        drawRect(x + 1, y + 1, x + 99, y + 13, 0xFF000000);
        int handlex = x + (int)((value / 255.0F) * 92);
        drawRect(handlex, y, handlex + 8, y + 14, 0xFFAAAAAA);
    }
    private void handleCanvasSelection(int mousex, int mousey) {
        int localx = MathHelper.clamp_int(mousex - (this.guileft + 20), 0, this.canvaswidth);
        int localy = MathHelper.clamp_int(mousey - (this.guitop + 15), 0, this.canvasheight);
        float hue = localx / (float) this.canvaswidth;
        float yratio = localy / (float) this.canvasheight;
        float brightness = yratio <= 0.5F ? yratio * 2.0F : 1.0F;
        float saturation = yratio > 0.5F ? 1.0F - ((yratio - 0.5F) * 2.0F) : 1.0F;
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0F + 0.5F);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0F;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0F - saturation);
            float q = brightness * (1.0F - saturation * f);
            float t = brightness * (1.0F - saturation * (1.0F - f));
            switch ((int) h) {
                case 0: r = (int) (brightness * 255.0F + 0.5F); g = (int) (t * 255.0F + 0.5F); b = (int) (p * 255.0F + 0.5F); break;
                case 1: r = (int) (q * 255.0F + 0.5F); g = (int) (brightness * 255.0F + 0.5F); b = (int) (p * 255.0F + 0.5F); break;
                case 2: r = (int) (p * 255.0F + 0.5F); g = (int) (brightness * 255.0F + 0.5F); b = (int) (t * 255.0F + 0.5F); break;
                case 3: r = (int) (p * 255.0F + 0.5F); g = (int) (q * 255.0F + 0.5F); b = (int) (brightness * 255.0F + 0.5F); break;
                case 4: r = (int) (t * 255.0F + 0.5F); g = (int) (p * 255.0F + 0.5F); b = (int) (brightness * 255.0F + 0.5F); break;
                case 5: r = (int) (brightness * 255.0F + 0.5F); g = (int) (p * 255.0F + 0.5F); b = (int) (q * 255.0F + 0.5F); break;
            }
        }
        updateColor((r << 16) | (g << 8) | b);
    }
    private void updateSliderColor(int mousex, int type) {
        float ratio = MathHelper.clamp_float((float)(mousex - (this.guileft + 20)) / 100.0F, 0.0F, 1.0F);
        int val = (int)(ratio * 255.0F);
        int r = (this.color >> 16) & 255;
        int g = (this.color >> 8) & 255;
        int b = this.color & 255;
        if (type == 0) r = val;
        else if (type == 1) g = val;
        else if (type == 2) b = val;
        updateColor((r << 16) | (g << 8) | b);
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == btn_done) {
            this.consumer.accept(this.color);
            this.mc.displayGuiScreen(parent);
        } else if (button.id == btn_cancel) {
            this.mc.displayGuiScreen(parent);
        } else if (button.id == btn_advanced) {
            this.advancedmode = !this.advancedmode;
            this.btnadvanced.displayString = getAdvancedLabel();
            updateTextFields();
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (keycode == 1) {
            this.mc.displayGuiScreen(parent);
            return;
        }
        if (this.advancedmode) {
            boolean textchanged = false;
            if (tfr.isFocused()) { tfr.textboxKeyTyped(typedchar, keycode); textchanged = true; }
            else if (tfg.isFocused()) { tfg.textboxKeyTyped(typedchar, keycode); textchanged = true; }
            else if (tfb.isFocused()) { tfb.textboxKeyTyped(typedchar, keycode); textchanged = true; }
            else if (intcolor.isFocused()) {
                intcolor.textboxKeyTyped(typedchar, keycode);
                try {
                    this.color = Integer.parseInt(intcolor.getText().trim()) & 0xFFFFFF;
                } catch (NumberFormatException ignored) {}
                updateTextFields();
                return;
            } else if (hexcolor.isFocused()) {
                hexcolor.textboxKeyTyped(typedchar, keycode);
                String text = hexcolor.getText().replace("#", "").trim();
                try {
                    this.color = Integer.parseInt(text, 16) & 0xFFFFFF;
                } catch (NumberFormatException ignored) {}
                updateTextFields();
                return;
            }
            if (textchanged) {
                try {
                    int r = MathHelper.clamp_int(tfr.getText().isEmpty() ? 0 : Integer.parseInt(tfr.getText().trim()), 0, 255);
                    int g = MathHelper.clamp_int(tfg.getText().isEmpty() ? 0 : Integer.parseInt(tfg.getText().trim()), 0, 255);
                    int b = MathHelper.clamp_int(tfb.getText().isEmpty() ? 0 : Integer.parseInt(tfb.getText().trim()), 0, 255);
                    this.color = (r << 16) | (g << 8) | b;
                } catch (NumberFormatException ignored) {}
                updateTextFields();
            }
        }
        super.keyTyped(typedchar, keycode);
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        if (mousebutton == 0) {
            for (int i = 0; i < 8; i++) {
                int dyey = this.guitop + 20 + i * 18;
                if (mousex >= this.guileft + 2 && mousex <= this.guileft + 18 && mousey >= dyey && mousey <= dyey + 16) {
                    updateColor(left_colors[i]);
                    return;
                }
                if (mousex >= this.guileft + this.guiwidth - 18 && mousex <= this.guileft + this.guiwidth - 2 && mousey >= dyey && mousey <= dyey + 16) {
                    updateColor(right_colors[i]);
                    return;
                }
            }
        }
        if (this.advancedmode) {
            tfr.mouseClicked(mousex, mousey, mousebutton);
            tfg.mouseClicked(mousex, mousey, mousebutton);
            tfb.mouseClicked(mousex, mousey, mousebutton);
            intcolor.mouseClicked(mousex, mousey, mousebutton);
            hexcolor.mouseClicked(mousex, mousey, mousebutton);
            if (mousebutton == 0 && mousex >= this.guileft + 20 && mousex <= this.guileft + 120) {
                if (mousey >= this.guitop + 30 && mousey <= this.guitop + 44) {
                    this.dragr = true;
                    updateSliderColor(mousex, 0);
                } else if (mousey >= this.guitop + 60 && mousey <= this.guitop + 74) {
                    this.dragg = true;
                    updateSliderColor(mousex, 1);
                } else if (mousey >= this.guitop + 90 && mousey <= this.guitop + 104) {
                    this.dragb = true;
                    updateSliderColor(mousex, 2);
                }
            }
        } else {
            if (mousebutton == 0 && mousex >= this.guileft + 20 && mousex <= this.guileft + 20 + this.canvaswidth && mousey >= this.guitop + 15 && mousey <= this.guitop + 15 + this.canvasheight) {
                this.drag = true;
                handleCanvasSelection(mousex, mousey);
            }
        }
    }
    @Override
    protected void mouseReleased(int mousex, int mousey, int state) {
        super.mouseReleased(mousex, mousey, state);
        if (state == 0) {
            this.drag = false;
            this.dragr = false;
            this.dragg = false;
            this.dragb = false;
        }
    }
    @Override
    protected void mouseClickMove(int mousex, int mousey, int clickedmousebutton, long timesincelastclick) {
        super.mouseClickMove(mousex, mousey, clickedmousebutton, timesincelastclick);
        if (clickedmousebutton == 0) {
            if (this.advancedmode) {
                if (this.dragr) updateSliderColor(mousex, 0);
                if (this.dragg) updateSliderColor(mousex, 1);
                if (this.dragb) updateSliderColor(mousex, 2);
            } else {
                if (this.drag) handleCanvasSelection(mousex, mousey);
            }
        }
    }
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
