package net.minecraft.actx.selector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
public class buttoncolored extends GuiButton {
    private final int textcolor;
    public buttoncolored(int id, int x, int y, int width, int height, String displayString, int textcolor) {
        super(id, x, y, width, height, displayString);
        this.textcolor = textcolor;
    }
    @Override
    public void drawButton(Minecraft mc, int mousex, int mousey) {
        if (!this.visible) {
            return;
        }
        String text = this.displayString;
        this.displayString = "";
        super.drawButton(mc, mousex, mousey);
        this.displayString = text;
        int color = !this.enabled ? 0x707070 : this.textcolor;
        this.drawCenteredString(mc.fontRendererObj, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, color);
    }
}
