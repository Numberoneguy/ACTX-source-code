package net.minecraft.actx;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.actx.guis.colormodifier;
import net.minecraft.client.resources.I18n;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class colorlist {
    private static final Random random = new Random();
    private final GuiScreen parent;
    private final Minecraft mc;
    private final FontRenderer fontrenderer;
    private final List<Integer> list;
    private final String title;
    private final int maxelement;
    private final int sizex;
    public int x;
    public int y;
    public colorlist(GuiScreen parent, int x, int y, int sizex, int[] initialcolors, String title, int maxelement) {
        this.parent = parent;
        this.mc = Minecraft.getMinecraft();
        this.fontrenderer = this.mc.fontRendererObj;
        this.x = x;
        this.y = y;
        this.sizex = sizex;
        this.title = title;
        this.maxelement = maxelement;
        this.list = new ArrayList<Integer>(initialcolors.length);
        for (int c : initialcolors) {
            this.list.add(c);
        }
    }
    public List<Integer> getColors() {
        return this.list;
    }
    public int getGridWidth() {
        return this.sizex * 16;
    }
    public void draw(int mousex, int mousey, float partialticks) {
        this.fontrenderer.drawStringWithShadow(I18n.format(this.title), this.x, this.y, 0xFFFFFF);
        int i = 0;
        boolean hoveringswatch = false;
        boolean hoveringadd = false;
        while (i < this.list.size()) {
            int blockx = this.x + (16 * (i % this.sizex));
            int blocky = this.y + this.fontrenderer.FONT_HEIGHT + 2 + (16 * (i / this.sizex));
            int color = this.list.get(i);
            boolean hovered = mousex >= blockx && mousex < blockx + 15 && mousey >= blocky && mousey < blocky + 15;
            if (hovered) hoveringswatch = true;
            GuiScreen.drawRect(blockx, blocky, blockx + 15, blocky + 15, 0xFF000000 | color);
            if (hovered) {
                GuiScreen.drawRect(blockx, blocky, blockx + 15, blocky + 15, 0x55FFFFFF);
            }
            i++;
        }
        if (i < this.maxelement) {
            int addx = this.x + (16 * (i % this.sizex));
            int addy = this.y + this.fontrenderer.FONT_HEIGHT + 2 + (16 * (i / this.sizex));
            boolean hovered = mousex >= addx && mousex < addx + 15 && mousey >= addy && mousey < addy + 15;
            if (hovered) hoveringadd = true;
            int crosscolor = hovered ? 0xFF00FF00 : 0xFF00AA00;
            GuiScreen.drawRect(addx, addy, addx + 15, addy + 15, 0xFF333333);
            String plus = "+";
            int plusw = this.fontrenderer.getStringWidth(plus);
            this.fontrenderer.drawStringWithShadow(plus, addx + 8 - (plusw / 2), addy + 4, crosscolor);
        }
        if (hoveringswatch) {
            drawTooltip(mousex, mousey, "§6[§eLeft-Click§6] to edit color.", "§6[§eRight-click§6] delete box.");
        } else if (hoveringadd) {
            drawTooltip(mousex, mousey, "§6[§eLeft-Click§6] to add a random color.");
        }
    }
    private void drawTooltip(int mousex, int mousey, String... lines) {
        int lineheight = this.fontrenderer.FONT_HEIGHT + 2;
        int textwidth = 0;
        for (String line : lines) {
            textwidth = Math.max(textwidth, this.fontrenderer.getStringWidth(line));
        }
        int boxx = mousex + 12;
        int boxy = mousey - 12;
        int boxwidth = textwidth + 6;
        int boxheight = lines.length * lineheight + 4;
        GlStateManager.disableDepth();
        GuiScreen.drawRect(boxx - 3, boxy - 4, boxx + boxwidth, boxy + boxheight - 4, 0xF0100010);
        for (int j = 0; j < lines.length; j++) {
            this.fontrenderer.drawStringWithShadow(lines[j], boxx, boxy + j * lineheight, 0xFFFFFF);
        }
        GlStateManager.enableDepth();
    }
    public void mouseClick(int mousex, int mousey, int mousebutton) {
        int i = 0;
        while (i < this.list.size()) {
            int blockx = this.x + (16 * (i % this.sizex));
            int blocky = this.y + this.fontrenderer.FONT_HEIGHT + 2 + (16 * (i / this.sizex));
            if (mousex >= blockx && mousex < blockx + 15 && mousey >= blocky && mousey < blocky + 15) {
                if (mousebutton == 0) {
                    final int activeindex = i;
                    int currentcolor = this.list.get(i);
                    this.mc.displayGuiScreen(new colormodifier(this.parent, new java.util.function.Consumer<Integer>() {
                        @Override
                        public void accept(Integer newcolor) {
                            colorlist.this.list.set(activeindex, newcolor);
                        }
                    }, currentcolor, 0xFFFFFF));
                } else if (mousebutton == 1) {
                    this.list.remove(i);
                }
                return;
            }
            i++;
        }
        if (i < this.maxelement) {
            int addx = this.x + (16 * (i % this.sizex));
            int addy = this.y + this.fontrenderer.FONT_HEIGHT + 2 + (16 * (i / this.sizex));
            if (mousebutton == 0 && mousex >= addx && mousex < addx + 15 && mousey >= addy && mousey < addy + 15) {
                this.list.add(random.nextInt(0x1000000));
            }
        }
    }
}
