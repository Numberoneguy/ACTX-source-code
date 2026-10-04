package net.minecraft.actx.selector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
public class buttonfilehighlight extends GuiButton {
    private final String highlightsubstring;
    private final int highlightcolor;
    public buttonfilehighlight(int id, int x, int y, int width, int height, String displayString,
            String highlightsubstring, int highlightcolor) {
        super(id, x, y, width, height, displayString);
        this.highlightsubstring = highlightsubstring;
        this.highlightcolor = highlightcolor;
    }
    @Override
    public void drawButton(Minecraft mc, int mousex, int mousey) {
        if (!this.visible) {
            return;
        }
        String fulltext = this.displayString;
        this.displayString = "";
        super.drawButton(mc, mousex, mousey);
        this.displayString = fulltext;
        if (fulltext == null || fulltext.isEmpty()) {
            return;
        }
        int basecolor = !this.enabled ? 10526880 : (this.hovered ? 16777120 : 14737632);
        drawSplitCenteredString(mc.fontRendererObj, fulltext, this.highlightsubstring, this.highlightcolor,
                basecolor, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2);
    }
    private static void drawSplitCenteredString(FontRenderer fr, String text, String highlight,
            int highlightcolor, int basecolor, int centerx, int y) {
        int idx = highlight != null ? text.indexOf(highlight) : -1;
        if (idx < 0) {
            fr.drawStringWithShadow(text, centerx - fr.getStringWidth(text) / 2, y, basecolor);
            return;
        }
        String prefix = text.substring(0, idx);
        String match = text.substring(idx, idx + highlight.length());
        String suffix = text.substring(idx + highlight.length());
        int x = centerx - fr.getStringWidth(text) / 2;
        if (!prefix.isEmpty()) {
            fr.drawStringWithShadow(prefix, x, y, basecolor);
            x += fr.getStringWidth(prefix);
        }
        fr.drawStringWithShadow(match, x, y, highlightcolor);
        x += fr.getStringWidth(match);
        if (!suffix.isEmpty()) {
            fr.drawStringWithShadow(suffix, x, y, basecolor);
        }
    }
}
