package net.minecraft.actx.guis;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemDye;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.actx.selector.buttonlistselector;
import net.minecraft.actx.selector.buttoncolored;
import net.minecraft.actx.selector.searchwidget;
import net.minecraft.actx.colorlist;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
public class fireworksmodifier extends listmodifier {
    private final EditorState state;
    private final NBTTagCompound fireworkscompound;
    private GuiTextField flighttimefield;
    private searchwidget searchwidget;
    private GuiButton btnaddexplosion;
    private static final Random rand = new Random();
    private int itemsperpage = 1;
    static final int row_height = 23;
    static final int label_w = 100;
    static final int arrow_w = 20;
    static final int copy_w = 60;
    static final int gap = 1;
    static final int row_width = label_w + gap + arrow_w + gap + arrow_w + gap + copy_w;
    static final int add_button_w = 200;
    static final int search_bar_y = 18;
    static final int search_bar_h = 18;
    static final int search_text_y = 23;
    static final int search_label_color = 0xFFC800;
    static final int bottom_bar_offset = 21;
    static final float tooltip_dim = 0.4f;
    private GuiButton btnpreviouspage;
    private GuiButton btnnextpage;
    private NBTTagCompound hoveredtag;
    private int hoveredmousex;
    private int hoveredmousey;
    private String searchlabel;
    private int searchlabelx;
    private int searchlabely;
    private String flightlabel;
    private int flightlabelx;
    private int flightlabely;
    private final List<ExplosionElement> allexplosionelements = new ArrayList<>();
    static int searchBarX(int screenwidth) {
        return (int) (screenwidth * 0.2f) + 40;
    }
    static int searchBarWidth(int screenwidth) {
        return Math.max(100, screenwidth - 2 * (int) (screenwidth * 0.2f) - 40);
    }
    static int dim(int rgb) {
        int r = Math.round(((rgb >> 16) & 0xFF) * tooltip_dim);
        int g = Math.round(((rgb >> 8) & 0xFF) * tooltip_dim);
        int b = Math.round((rgb & 0xFF) * tooltip_dim);
        return (r << 16) | (g << 8) | b;
    }
    void setHoveredExplosion(NBTTagCompound tag, int mousex, int mousey) {
        this.hoveredtag = tag;
        this.hoveredmousex = mousex;
        this.hoveredmousey = mousey;
    }
    public fireworksmodifier(EditorState state, GuiScreen parent) {
        super(parent);
        this.state = state;
        NBTTagCompound basetag = state.stack.getTagCompound();
        if (basetag == null) {
            basetag = new NBTTagCompound();
            state.stack.setTagCompound(basetag);
        }
        if (!basetag.hasKey("Fireworks", 10)) {
            basetag.setTag("Fireworks", new NBTTagCompound());
        }
        this.fireworkscompound = basetag.getCompoundTag("Fireworks");
        loadExplosions();
    }
    private void loadExplosions() {
        this.allexplosionelements.clear();
        if (this.fireworkscompound.hasKey("Explosions", 9)) {
            NBTTagList list = this.fireworkscompound.getTagList("Explosions", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                this.allexplosionelements.add(new ExplosionElement(this, list.getCompoundTagAt(i)));
            }
        }
        updateSearch();
    }
    private void updateSearch() {
        this.elements.clear();
        String query = (this.searchwidget != null) ? this.searchwidget.getText().toLowerCase() : "";
        for (ExplosionElement el : this.allexplosionelements) {
            if (query.isEmpty() || matchesQuery(el.saveTag(), query)) {
                this.elements.add(el);
            }
        }
        updatePagination();
    }
    private boolean matchesQuery(NBTTagCompound tag, String query) {
        int shapeidx = tag.getByte("Type");
        String shapename = (shapeidx >= 0 && shapeidx < 5) ? I18n.format("item.fireworksCharge.type." + shapeidx).toLowerCase() : "?";
        return shapename.contains(query);
    }
    @Override
    protected int getItemsPerPage() {
        return this.itemsperpage;
    }
    @Override
    public void initGui() {
        super.initGui();
        int centerx = this.width / 2;
        this.searchlabel = I18n.format("gui.act.search") + " :";
        int labelw = this.fontRendererObj.getStringWidth(this.searchlabel);
        int barx = searchBarX(this.width);
        int barw = searchBarWidth(this.width);
        this.searchlabelx = Math.max(2, barx - 4 - labelw);
        this.searchlabely = search_text_y;
        String oldsearch = (this.searchwidget != null) ? this.searchwidget.getText() : "";
        this.searchwidget = new searchwidget(98, this.fontRendererObj, barx, search_bar_y, barw, search_bar_h);
        this.searchwidget.setText(oldsearch);
        String rawflight = I18n.format("item.fireworks.flight").trim();
        if (rawflight.endsWith(":")) {
            rawflight = rawflight.substring(0, rawflight.length() - 1).trim();
        }
        this.flightlabel = rawflight + " :";
        this.flightlabelx = centerx - 100;
        this.flightlabely = 46;
        this.flighttimefield = new GuiTextField(99, this.fontRendererObj, centerx - 2, 44, 98, 16);
        int currentflight = this.fireworkscompound.hasKey("Flight", 1) ? this.fireworkscompound.getByte("Flight") : 1;
        this.flighttimefield.setText(String.valueOf(currentflight));
        int starty = getContentStartY();
        int bottomspace = 40;
        int availableheight = this.height - starty - bottomspace - 26;
        this.itemsperpage = Math.max(1, availableheight / (row_height + 6));
        this.btnaddexplosion = new GuiButton(300, (this.width - add_button_w) / 2, 0, add_button_w, 20, "\u00A7a+");
        this.buttonList.add(this.btnaddexplosion);
        layoutBottomBar(centerx);
        updateSearch();
    }
    private void layoutBottomBar(int centerx) {
        int bottomy = this.height - bottom_bar_offset;
        this.buttonList.removeIf(b -> b.id == 202 || b.id == 203 || b == this.btnpreviouspage || b == this.btnnextpage);
        GuiButton done = findButtonByText(I18n.format("gui.done"));
        if (done != null) {
            done.xPosition = centerx - 100;
            done.yPosition = bottomy;
            done.width = 98;
            done.height = 20;
        }
        GuiButton cancel = findButtonByText(I18n.format("gui.cancel"));
        if (cancel != null) {
            cancel.xPosition = centerx;
            cancel.yPosition = bottomy;
            cancel.width = 98;
            cancel.height = 20;
        }
        this.btnpreviouspage = new GuiButton(310, centerx - 121, bottomy, 20, 20, "<-");
        this.btnnextpage = new GuiButton(311, centerx + 100, bottomy, 20, 20, "->");
        this.buttonList.add(this.btnpreviouspage);
        this.buttonList.add(this.btnnextpage);
    }
    private GuiButton findButtonByText(String text) {
        for (int i = 0; i < this.buttonList.size(); i++) {
            GuiButton b = this.buttonList.get(i);
            if (text.equals(b.displayString)) {
                return b;
            }
        }
        return null;
    }
    private int lastPageIndex() {
        return Math.max(0, (this.elements.size() - 1) / Math.max(1, this.itemsperpage));
    }
    private void updateArrowStates() {
        if (this.btnpreviouspage == null || this.btnnextpage == null) return;
        this.btnpreviouspage.enabled = this.page > 0;
        this.btnnextpage.enabled = this.page < lastPageIndex();
    }
    @Override
    protected int getContentStartY() {
        return 71;
    }
    public void updatePagination() {
        super.updatePagination();
        updateAddButtonPosition();
        updateArrowStates();
    }
    private void updateAddButtonPosition() {
        if (this.btnaddexplosion != null) {
            int p = 0;
            try { p = this.page; } catch (Exception e) {}
            int displayeditems = this.elements.size() - (p * this.itemsperpage);
            if (displayeditems > this.itemsperpage) displayeditems = this.itemsperpage;
            if (displayeditems < 0) displayeditems = 0;
            this.btnaddexplosion.yPosition = getContentStartY() + displayeditems * (row_height + 6);
            this.btnaddexplosion.xPosition = (this.width - add_button_w) / 2;
            this.btnaddexplosion.width = add_button_w;
        }
    }
    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.searchwidget != null) {
            this.searchwidget.updateScreen();
        }
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 300) {
            NBTTagCompound newexplosion = new NBTTagCompound();
            newexplosion.setByte("Type", (byte) 0);
            int randomdyecolor = ItemDye.dyeColors[rand.nextInt(ItemDye.dyeColors.length)];
            newexplosion.setIntArray("Colors", new int[]{ randomdyecolor });
            newexplosion.setIntArray("FadeColors", new int[]{});
            this.allexplosionelements.add(new ExplosionElement(this, newexplosion));
            updateSearch();
            return;
        }
        if (button.id == 310) {
            if (this.page > 0) {
                this.page--;
                updatePagination();
            }
            return;
        }
        if (button.id == 311) {
            if (this.page < lastPageIndex()) {
                this.page++;
                updatePagination();
            }
            return;
        }
        super.actionPerformed(button);
    }
    @Override
    protected void onSave() {
        try {
            int flightval = Integer.parseInt(this.flighttimefield.getText().trim());
            if (flightval < 1) flightval = 1;
            if (flightval > 3) flightval = 3;
            this.fireworkscompound.setByte("Flight", (byte) flightval);
        } catch (NumberFormatException e) {
            this.fireworkscompound.setByte("Flight", (byte) 1);
        }
        NBTTagList taglist = new NBTTagList();
        for (ExplosionElement el : this.allexplosionelements) {
            taglist.appendTag(el.saveTag());
        }
        this.fireworkscompound.setTag("Explosions", taglist);
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        this.hoveredtag = null;
        super.drawScreen(mousex, mousey, partialticks);
        if (this.searchwidget != null) {
            this.fontRendererObj.drawString(this.searchlabel, this.searchlabelx, this.searchlabely, search_label_color);
            this.searchwidget.drawWidget(mousex, mousey);
        }
        this.fontRendererObj.drawString(this.flightlabel, this.flightlabelx, this.flightlabely, 0xFFFFFF);
        this.flighttimefield.drawTextBox();
        updateAddButtonPosition();
        if (this.hoveredtag != null) {
            drawExplosionTooltip(this.hoveredmousex, this.hoveredmousey, this.hoveredtag);
        }
    }
    private void drawExplosionTooltip(int mousex, int mousey, NBTTagCompound tag) {
        int shapeidx = tag.getByte("Type");
        String shapename = (shapeidx >= 0 && shapeidx < 5) ? I18n.format("item.fireworksCharge.type." + shapeidx) : "?";
        boolean trail = tag.getBoolean("Trail");
        boolean twinkle = tag.getBoolean("Flicker");
        int[] colors = tag.getIntArray("Colors");
        int[] fadecolors = tag.getIntArray("FadeColors");
        String typelabel = I18n.format("gui.act.modifier.type") + " : ";
        String trailtext = I18n.format("item.fireworksCharge.trail");
        String twinkletext = I18n.format("item.fireworksCharge.flicker");
        String colorsline = I18n.format("options.chat.color") + " :";
        String fadeline = I18n.format("gui.act.modifier.meta.explosion.fadeColor") + " :";
        final int textcolor = dim(0xFFFFFF);
        final int shapecolor = dim(0xFFFF55);
        final int lineh = 10;
        final int swatch = 9;
        final int swatchstride = 10;
        final int padx = 5;
        final int pady = 6;
        int linecount = 3 + (trail ? 1 : 0) + (twinkle ? 1 : 0);
        int contentw = this.fontRendererObj.getStringWidth(typelabel) + this.fontRendererObj.getStringWidth(shapename);
        if (trail) contentw = Math.max(contentw, this.fontRendererObj.getStringWidth(trailtext));
        if (twinkle) contentw = Math.max(contentw, this.fontRendererObj.getStringWidth(twinkletext));
        contentw = Math.max(contentw, this.fontRendererObj.getStringWidth(colorsline) + 4 + Math.max(colors.length, 1) * swatchstride);
        contentw = Math.max(contentw, this.fontRendererObj.getStringWidth(fadeline) + 4 + Math.max(fadecolors.length, 1) * swatchstride);
        int contenth = (linecount - 1) * lineh + 8;
        int textx = mousex + 12;
        int texty = mousey + 12;
        if (textx + contentw + padx > this.width) textx = mousex - 12 - contentw;
        if (texty + contenth + pady > this.height) texty = this.height - contenth - pady;
        textx = Math.max(textx, padx);
        texty = Math.max(texty, pady);
        int ox0 = textx - padx;
        int ox1 = textx + contentw + padx;
        int oy0 = texty - pady;
        int oy1 = texty + contenth + pady;
        int bg = 0xF0100010;
        int border1 = 0x505000FF;
        int border2 = (border1 & 0xFEFEFE) >> 1 | (border1 & 0xFF000000);
        GlStateManager.disableDepth();
        this.drawGradientRect(ox0 + 1, oy0, ox1 - 1, oy0 + 1, bg, bg);
        this.drawGradientRect(ox0 + 1, oy1 - 1, ox1 - 1, oy1, bg, bg);
        this.drawGradientRect(ox0 + 1, oy0 + 1, ox1 - 1, oy1 - 1, bg, bg);
        this.drawGradientRect(ox0, oy0 + 1, ox0 + 1, oy1 - 1, bg, bg);
        this.drawGradientRect(ox1 - 1, oy0 + 1, ox1, oy1 - 1, bg, bg);
        this.drawGradientRect(ox0 + 1, oy0 + 2, ox0 + 2, oy1 - 2, border1, border2);
        this.drawGradientRect(ox1 - 2, oy0 + 2, ox1 - 1, oy1 - 2, border1, border2);
        this.drawGradientRect(ox0 + 1, oy0 + 1, ox1 - 1, oy0 + 2, border1, border1);
        this.drawGradientRect(ox0 + 1, oy1 - 2, ox1 - 1, oy1 - 1, border2, border2);
        int cury = texty;
        this.fontRendererObj.drawString(typelabel, textx, cury, textcolor);
        this.fontRendererObj.drawString(shapename, textx + this.fontRendererObj.getStringWidth(typelabel), cury, shapecolor);
        cury += lineh;
        if (trail) {
            this.fontRendererObj.drawString(trailtext, textx, cury, textcolor);
            cury += lineh;
        }
        if (twinkle) {
            this.fontRendererObj.drawString(twinkletext, textx, cury, textcolor);
            cury += lineh;
        }
        this.fontRendererObj.drawString(colorsline, textx, cury, textcolor);
        drawDimSwatches(textx + this.fontRendererObj.getStringWidth(colorsline) + 4, cury, colors, swatch, swatchstride);
        cury += lineh;
        this.fontRendererObj.drawString(fadeline, textx, cury, textcolor);
        drawDimSwatches(textx + this.fontRendererObj.getStringWidth(fadeline) + 4, cury, fadecolors, swatch, swatchstride);
        GlStateManager.enableDepth();
    }
    private void drawDimSwatches(int x, int y, int colors[], int size, int stride) {
        int cx = x;
        for (int c : colors) {
            GuiScreen.drawRect(cx, y, cx + size, y + size, 0xFF000000 | dim(c & 0xFFFFFF));
            cx += stride;
        }
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        this.flighttimefield.mouseClicked(mousex, mousey, mousebutton);
        if (this.searchwidget != null) {
            this.searchwidget.mouseClicked(mousex, mousey, mousebutton);
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (this.searchwidget != null && this.searchwidget.isFocused()) {
            boolean handled = this.searchwidget.keyTyped(typedchar, keycode, query -> {
                this.page = 0;
                updateSearch();
            });
            if (handled) return;
        }
        if (this.flighttimefield.isFocused() && (Character.isDigit(typedchar) || keycode == 14 || keycode == 211)) {
            this.flighttimefield.textboxKeyTyped(typedchar, keycode);
            return;
        }
        super.keyTyped(typedchar, keycode);
    }
    private static class ExplosionElement extends ListElement {
        private final GuiScreen parent;
        private final NBTTagCompound tag;
        private final GuiButton btnedit;
        private final GuiButton btnmoveup;
        private final GuiButton btnmovedown;
        private final GuiButton btncopy;
        public ExplosionElement(GuiScreen parent, NBTTagCompound tag) {
            super(row_height);
            this.parent = parent;
            this.tag = tag;
            this.btnedit = new GuiButton(0, 0, 0, label_w, 20, I18n.format("gui.act.modifier.meta.explosion"));
            this.btnmoveup = new GuiButton(1, 0, 0, arrow_w, 20, "\u00A7c-");
            this.btnmovedown = new GuiButton(2, 0, 0, arrow_w, 20, "\u00A7a+");
            this.btncopy = new GuiButton(3, 0, 0, copy_w, 20, I18n.format("gui.act.give.copy"));
            this.buttonList.add(btnedit);
            this.buttonList.add(btnmoveup);
            this.buttonList.add(btnmovedown);
            this.buttonList.add(btncopy);
        }
        public NBTTagCompound saveTag() {
            return this.tag;
        }
        @Override
        public void draw(int x, int y, int width, int mousex, int mousey, float partialticks) {
            int rowleft = (this.parent.width - row_width) / 2;
            int bx = rowleft;
            this.btnedit.xPosition = bx; this.btnedit.yPosition = y;
            bx += label_w + gap;
            this.btnmoveup.xPosition = bx; this.btnmoveup.yPosition = y;
            bx += arrow_w + gap;
            this.btnmovedown.xPosition = bx; this.btnmovedown.yPosition = y;
            bx += arrow_w + gap;
            this.btncopy.xPosition = bx; this.btncopy.yPosition = y;
            int rowright = bx + copy_w;
            this.btnedit.drawButton(Minecraft.getMinecraft(), mousex, mousey);
            this.btnmoveup.drawButton(Minecraft.getMinecraft(), mousex, mousey);
            this.btnmovedown.drawButton(Minecraft.getMinecraft(), mousex, mousey);
            this.btncopy.drawButton(Minecraft.getMinecraft(), mousex, mousey);
            boolean rowhovered = mousex >= rowleft && mousex < rowright && mousey >= y && mousey < y + 20;
            if (rowhovered && this.parent instanceof fireworksmodifier) {
                ((fireworksmodifier) this.parent).setHoveredExplosion(this.tag, mousex, mousey);
            }
        }
        @Override
        public void mouseClicked(int mousex, int mousey, int mousebutton, int x, int y, int width) {
            if (!(this.parent instanceof fireworksmodifier)) return;
            fireworksmodifier gm = (fireworksmodifier) this.parent;
            if (mousebutton == 0) {
                if (this.btnedit.mousePressed(Minecraft.getMinecraft(), mousex, mousey)) {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiExplosionEditor(this.parent, this.tag));
                } else if (this.btnmoveup.mousePressed(Minecraft.getMinecraft(), mousex, mousey)) {
                    moveBy(gm, -1);
                } else if (this.btnmovedown.mousePressed(Minecraft.getMinecraft(), mousex, mousey)) {
                    moveBy(gm, 1);
                } else if (this.btncopy.mousePressed(Minecraft.getMinecraft(), mousex, mousey)) {
                    int idx = gm.allexplosionelements.indexOf(this);
                    if (idx >= 0) {
                        NBTTagCompound cloned = (NBTTagCompound) this.tag.copy();
                        gm.allexplosionelements.add(idx + 1, new ExplosionElement(this.parent, cloned));
                        gm.updateSearch();
                    }
                }
            } else if (mousebutton == 1) {
                if (this.btnedit.mousePressed(Minecraft.getMinecraft(), mousex, mousey)) {
                    gm.allexplosionelements.remove(this);
                    gm.updateSearch();
                }
            }
        }
        private void moveBy(fireworksmodifier gm, int delta) {
            if (gm.searchwidget != null && !gm.searchwidget.getText().isEmpty()) return;
            int idx = gm.allexplosionelements.indexOf(this);
            int newidx = idx + delta;
            if (idx < 0 || newidx < 0 || newidx >= gm.allexplosionelements.size()) return;
            java.util.Collections.swap(gm.allexplosionelements, idx, newidx);
            gm.updateSearch();
        }
    }
    private static class GuiExplosionEditor extends GuiScreen {
        private final GuiScreen parentscreen;
        private final NBTTagCompound explosiontag;
        private int type;
        private boolean trail;
        private boolean twinkle;
        private GuiButton btntype;
        private GuiButton btntrail;
        private GuiButton btntwinkle;
        private GuiButton btndone;
        private GuiButton btncancel;
        private colorlist colorgrid;
        private final List<Integer> colorslist = new ArrayList<>();
        private colorlist fadegrid;
        private final List<Integer> fadecolorslist = new ArrayList<>();
        private static final int shape_count = 5;
        public GuiExplosionEditor(GuiScreen parentscreen, NBTTagCompound explosiontag) {
            this.parentscreen = parentscreen;
            this.explosiontag = explosiontag;
            this.type = explosiontag.getByte("Type");
            this.trail = explosiontag.getBoolean("Trail");
            this.twinkle = explosiontag.getBoolean("Flicker");
            for (int c : explosiontag.getIntArray("Colors")) colorslist.add(c);
            for (int f : explosiontag.getIntArray("FadeColors")) fadecolorslist.add(f);
        }
        @Override
        public void initGui() {
            int centerx = this.width / 2;
            int centery = this.height / 2;
            int gutter = 2;
            int panelwidth = Math.min(198, centerx - 22);
            int panelx = centerx - gutter - panelwidth;
            int rowstep = 21;
            int rowy0 = centery - 42;
            int rowy1 = rowy0 + rowstep;
            int rowy2 = rowy1 + rowstep;
            int rowy3 = rowy2 + rowstep;
            this.btntype = new GuiButton(0, panelx, rowy0, panelwidth, 20, getShapeName());
            this.btntrail = createToggleButton(1, panelx, rowy1, panelwidth, "item.fireworksCharge.trail", this.trail);
            this.btntwinkle = createToggleButton(2, panelx, rowy2, panelwidth, "item.fireworksCharge.flicker", this.twinkle);
            int canceldonew = (panelwidth - 2) / 2;
            this.btncancel = new GuiButton(3, panelx, rowy3, canceldonew, 20, I18n.format("gui.cancel"));
            this.btndone = new GuiButton(4, panelx + canceldonew + 2, rowy3, canceldonew, 20, I18n.format("gui.done"));
            this.buttonList.add(btntype);
            this.buttonList.add(btntrail);
            this.buttonList.add(btntwinkle);
            this.buttonList.add(btncancel);
            this.buttonList.add(btndone);
            int colorgridx = panelx + panelwidth + gutter;
            int gridy = rowy0;
            if (this.colorgrid == null) {
                int[] carr = new int[colorslist.size()];
                for (int i = 0; i < colorslist.size(); i++) carr[i] = colorslist.get(i);
                this.colorgrid = new colorlist(this, colorgridx, gridy, 6, carr, "options.chat.color", 8);
            } else {
                this.colorgrid.x = colorgridx;
                this.colorgrid.y = gridy;
            }
            int fadegridx = colorgridx + this.colorgrid.getGridWidth() + 4;
            if (this.fadegrid == null) {
                int[] farr = new int[fadecolorslist.size()];
                for (int i = 0; i < fadecolorslist.size(); i++) farr[i] = fadecolorslist.get(i);
                this.fadegrid = new colorlist(this, fadegridx, gridy, 6, farr, "gui.act.modifier.meta.explosion.fadeColor", 8);
            } else {
                this.fadegrid.x = fadegridx;
                this.fadegrid.y = gridy;
            }
        }
        private String getShapeName() {
            return (this.type >= 0 && this.type < shape_count)
                ? I18n.format("item.fireworksCharge.type." + this.type)
                : "?";
        }
        private GuiButton createToggleButton(int id, int x, int y, int w, String labelkey, boolean on) {
            return new buttoncolored(id, x, y, w, 20, I18n.format(labelkey), on ? 0x00FF00 : 0xFF0000);
        }
        private GuiButton swapButton(GuiButton oldbutton, GuiButton newbutton) {
            int idx = this.buttonList.indexOf(oldbutton);
            if (idx >= 0) {
                this.buttonList.set(idx, newbutton);
            } else {
                this.buttonList.add(newbutton);
            }
            return newbutton;
        }
        @Override
        protected void actionPerformed(GuiButton button) {
            if (button.id == 0) {
                buttonlistselector selector = new buttonlistselector(this) {
                    private int selectedidx = GuiExplosionEditor.this.type;
                    private searchwidget innersearchwidget;
                    private String innersearchlabel;
                    private int innersearchlabelx;
                    private int innersearchlabely;
                    private GuiButton leftarrow;
                    private GuiButton rightarrow;
                    private int itemsperpage = 1;
                    private int elementwidth = 200;
                    private final int rowpitch = 21;
                    private int liststarty = 44;
                    @Override
                    protected int getItemsPerPage() {
                        return this.itemsperpage;
                    }
                    @Override
                    public void initGui() {
                        int reservedtop = 44;
                        int reservedbottom = 30;
                        int availableheight = this.height - reservedtop - reservedbottom;
                        this.itemsperpage = Math.max(1, (availableheight + 1) / this.rowpitch);
                        int fulllistheight = shape_count * this.rowpitch - 1;
                        this.liststarty = (fulllistheight <= availableheight)
                                ? Math.max(reservedtop, (this.height - fulllistheight) / 2)
                                : reservedtop;
                        this.elementwidth = Math.min(200, this.width - 20);
                        final int btnwidth = this.elementwidth;
                        this.elements.clear();
                        String query = (innersearchwidget != null) ? innersearchwidget.getText().toLowerCase() : "";
                        for (int i = 0; i < shape_count; i++) {
                            String shapename = I18n.format("item.fireworksCharge.type." + i);
                            if (!query.isEmpty() && !shapename.toLowerCase().contains(query)) {
                                continue;
                            }
                            final int idx = i;
                            this.elements.add(new buttonlistselector.ListElement(20) {
                                private final GuiButton btn = new GuiButton(0, 0, 0, btnwidth, 20, shapename);
                                @Override
                                public void draw(int x, int y, int width, int mousex, int mousey, float partialticks) {
                                    this.btn.xPosition = x;
                                    this.btn.yPosition = y;
                                    this.btn.drawButton(Minecraft.getMinecraft(), mousex, mousey);
                                }
                                @Override
                                public void mouseClicked(int mousex, int mousey, int mousebutton, int x, int y, int width) {
                                    if (mousebutton == 0 && this.btn.mousePressed(Minecraft.getMinecraft(), mousex, mousey)) {
                                        selectedidx = idx;
                                        onSave();
                                        Minecraft.getMinecraft().displayGuiScreen(parent);
                                    }
                                }
                            });
                        }
                        super.initGui();
                        int centerx = this.width / 2;
                        int barx = fireworksmodifier.searchBarX(this.width);
                        int barw = fireworksmodifier.searchBarWidth(this.width);
                        this.innersearchlabel = I18n.format("gui.act.search") + " :";
                        int labelw = this.fontRendererObj.getStringWidth(this.innersearchlabel);
                        this.innersearchlabelx = Math.max(2, barx - 4 - labelw);
                        this.innersearchlabely = search_text_y;
                        String oldsearch = (innersearchwidget != null) ? innersearchwidget.getText() : "";
                        this.innersearchwidget = new searchwidget(95, this.fontRendererObj, barx, search_bar_y, barw, search_bar_h);
                        this.innersearchwidget.setText(oldsearch);
                        int bottomy = this.height - bottom_bar_offset;
                        final String donetext = I18n.format("gui.done");
                        final String canceltext = I18n.format("gui.cancel");
                        this.buttonList.removeIf(btn -> btn.id == 202 || btn.id == 203
                                || btn == this.leftarrow || btn == this.rightarrow
                                || donetext.equals(btn.displayString));
                        for (int i = 0; i < this.buttonList.size(); i++) {
                            GuiButton b = this.buttonList.get(i);
                            if (canceltext.equals(b.displayString)) {
                                b.xPosition = centerx - 50;
                                b.yPosition = bottomy;
                                b.width = 98;
                                b.height = 20;
                            }
                        }
                        this.leftarrow = new GuiButton(3, centerx - 71, bottomy, 20, 20, "<-");
                        this.rightarrow = new GuiButton(4, centerx + 50, bottomy, 20, 20, "->");
                        this.leftarrow.enabled = this.page > 0;
                        this.rightarrow.enabled = this.page < this.maxpage;
                        this.buttonList.add(leftarrow);
                        this.buttonList.add(rightarrow);
                    }
                    @Override
                    public void updateScreen() {
                        super.updateScreen();
                        if (this.innersearchwidget != null) {
                            this.innersearchwidget.updateScreen();
                        }
                    }
                    @Override
                    protected void actionPerformed(GuiButton button) {
                        if (button.id == 3) {
                            if (this.page > 0) {
                                this.page--;
                                this.initGui();
                            }
                        } else if (button.id == 4) {
                            if (this.page < this.maxpage) {
                                this.page++;
                                this.initGui();
                            }
                        } else {
                            super.actionPerformed(button);
                        }
                    }
                    @Override
                    public void drawScreen(int mousex, int mousey, float partialticks) {
                        this.drawDefaultBackground();
                        int startx = (this.width / 2) - (this.elementwidth / 2);
                        int startindex = this.page * this.itemsperpage;
                        int endindex = Math.min(startindex + this.itemsperpage, this.elements.size());
                        int currenty = this.liststarty;
                        for (int i = startindex; i < endindex; i++) {
                            this.elements.get(i).draw(startx, currenty, this.elementwidth, mousex, mousey, partialticks);
                            currenty += this.rowpitch;
                        }
                        if (this.innersearchwidget != null) {
                            this.fontRendererObj.drawString(this.innersearchlabel, this.innersearchlabelx, this.innersearchlabely, search_label_color);
                            this.innersearchwidget.drawWidget(mousex, mousey);
                        }
                        for (int i = 0; i < this.buttonList.size(); i++) {
                            this.buttonList.get(i).drawButton(this.mc, mousex, mousey);
                        }
                    }
                    @Override
                    protected void keyTyped(char typedchar, int keycode) {
                        if (this.innersearchwidget != null && this.innersearchwidget.isFocused()) {
                            boolean handled = this.innersearchwidget.keyTyped(typedchar, keycode, q -> {
                                this.page = 0;
                                this.initGui();
                            });
                            if (handled) return;
                        }
                        super.keyTyped(typedchar, keycode);
                    }
                    @Override
                    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
                        if (this.innersearchwidget != null) {
                            this.innersearchwidget.mouseClicked(mousex, mousey, mousebutton);
                        }
                        int startx = (this.width / 2) - (this.elementwidth / 2);
                        int startindex = this.page * this.itemsperpage;
                        int endindex = Math.min(startindex + this.itemsperpage, this.elements.size());
                        int currenty = this.liststarty;
                        for (int i = startindex; i < endindex; i++) {
                            this.elements.get(i).mouseClicked(mousex, mousey, mousebutton, startx, currenty, this.elementwidth);
                            currenty += this.rowpitch;
                        }
                        for (int i = 0; i < this.buttonList.size(); i++) {
                            GuiButton guibutton = this.buttonList.get(i);
                            if (guibutton.mousePressed(this.mc, mousex, mousey)) {
                                this.actionPerformed(guibutton);
                            }
                        }
                    }
                    @Override
                    protected void onSave() {
                        GuiExplosionEditor.this.type = this.selectedidx;
                        GuiExplosionEditor.this.btntype.displayString = GuiExplosionEditor.this.getShapeName();
                    }
                };
                Minecraft.getMinecraft().displayGuiScreen(selector);
            } else if (button.id == 1) {
                this.trail = !this.trail;
                this.btntrail = swapButton(button, createToggleButton(1, button.xPosition, button.yPosition, button.width, "item.fireworksCharge.trail", this.trail));
            } else if (button.id == 2) {
                this.twinkle = !this.twinkle;
                this.btntwinkle = swapButton(button, createToggleButton(2, button.xPosition, button.yPosition, button.width, "item.fireworksCharge.flicker", this.twinkle));
            } else if (button.id == 3) {
                Minecraft.getMinecraft().displayGuiScreen(this.parentscreen);
            } else if (button.id == 4) {
                this.explosiontag.setByte("Type", (byte) this.type);
                this.explosiontag.setBoolean("Trail", this.trail);
                this.explosiontag.setBoolean("Flicker", this.twinkle);
                List<Integer> clist = colorgrid.getColors();
                int[] carr = new int[clist.size()];
                for (int i = 0; i < clist.size(); i++) carr[i] = clist.get(i);
                this.explosiontag.setIntArray("Colors", carr);
                List<Integer> flist = fadegrid.getColors();
                int[] farr = new int[flist.size()];
                for (int i = 0; i < flist.size(); i++) farr[i] = flist.get(i);
                this.explosiontag.setIntArray("FadeColors", farr);
                Minecraft.getMinecraft().displayGuiScreen(this.parentscreen);
            }
        }
        @Override
        public void drawScreen(int mousex, int mousey, float partialticks) {
            this.drawDefaultBackground();
            super.drawScreen(mousex, mousey, partialticks);
            this.colorgrid.draw(mousex, mousey, partialticks);
            this.fadegrid.draw(mousex, mousey, partialticks);
        }
        @Override
        protected void mouseClicked(int mousex, int mousey, int mousebutton) {
            super.mouseClicked(mousex, mousey, mousebutton);
            this.colorgrid.mouseClick(mousex, mousey, mousebutton);
            this.fadegrid.mouseClick(mousex, mousey, mousebutton);
        }
    }
}
