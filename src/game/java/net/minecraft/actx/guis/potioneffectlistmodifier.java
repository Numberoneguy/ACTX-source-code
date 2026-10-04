package net.minecraft.actx.guis;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.minecraft.actx.selector.searchwidget;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import java.util.ArrayList;
import java.util.List;
public class potioneffectlistmodifier extends GuiScreen {
    private static final int btn_cancel = 0;
    private static final int btn_previous = 1;
    private static final int btn_next = 2;
    private static final int btn_list_start = 10;
    private final EditorState state;
    private final potionmodifier backscreen;
    private final int effectindex;
    private List<Integer> alleffectids = new ArrayList<Integer>();
    private List<Integer> filteredeffectids = new ArrayList<Integer>();
    private int page = 0;
    private int maxpage = 0;
    private int entriesperpage = 8;
    private final int entryheight = 20;
    private final int listwidth = 200;
    private searchwidget searchwidget;
    private int liststarty;
    private int listx;
    public potioneffectlistmodifier(EditorState state, potionmodifier backscreen, int effectindex) {
        this.state = state;
        this.backscreen = backscreen;
        this.effectindex = effectindex;
        populateEffects();
    }
    private void populateEffects() {
        alleffectids.clear();
        for (int i = 0; i < Potion.potionTypes.length; i++) {
            if (Potion.potionTypes[i] != null) {
                alleffectids.add(i);
            }
        }
        filteredeffectids.clear();
        filteredeffectids.addAll(alleffectids);
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.liststarty = 40;
        int bottomreserve = 30;
        int availableh = this.height - liststarty - bottomreserve;
        this.entriesperpage = Math.max(1, availableh / entryheight);
        this.maxpage = filteredeffectids.isEmpty() ? 0 : (filteredeffectids.size() - 1) / entriesperpage;
        if (this.page > maxpage) this.page = maxpage;
        if (this.page < 0) this.page = 0;
        this.listx = this.width / 2 - listwidth / 2;
        String oldsearch = "";
        if (searchwidget != null) oldsearch = searchwidget.getText();
        this.searchwidget = new searchwidget(99, this.fontRendererObj, listx, 15, listwidth, 16);
        this.searchwidget.setText(oldsearch);
        int startidx = page * entriesperpage;
        int endidx = Math.min(startidx + entriesperpage, filteredeffectids.size());
        for (int i = startidx; i < endidx; i++) {
            int local = i - startidx;
            int y = liststarty + local * entryheight;
            int potionid = filteredeffectids.get(i);
            Potion p = Potion.potionTypes[potionid];
            String name = p != null ? I18n.format(p.getName()) : "Unknown";
            this.buttonList.add(new GuiButton(btn_list_start + i, listx, y, listwidth, entryheight - 2, name));
        }
        int bottomy = this.height - 25;
        int center = this.width / 2;
        this.buttonList.add(new GuiButton(btn_previous, center - 75, bottomy, 24, 20, "<-"));
        this.buttonList.add(new GuiButton(btn_cancel, center - 47, bottomy, 94, 20, I18n.format("gui.cancel")));
        this.buttonList.add(new GuiButton(btn_next, center + 51, bottomy, 24, 20, "->"));
        updateNavButtons();
    }
    private void applySearch(String query) {
        filteredeffectids.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredeffectids.addAll(alleffectids);
        } else {
            String q = query.toLowerCase().trim();
            for (int id : alleffectids) {
                Potion p = Potion.potionTypes[id];
                if (p != null && I18n.format(p.getName()).toLowerCase().contains(q)) {
                    filteredeffectids.add(id);
                }
            }
        }
        this.page = 0;
        this.maxpage = filteredeffectids.isEmpty() ? 0 : (filteredeffectids.size() - 1) / entriesperpage;
    }
    private void updateNavButtons() {
        for (GuiButton btn : this.buttonList) {
            if (btn.id == btn_previous) btn.enabled = (page > 0);
            if (btn.id == btn_next) btn.enabled = (page < maxpage);
        }
    }
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    @Override
    public void updateScreen() {
        super.updateScreen();
        if (searchwidget != null) searchwidget.updateScreen();
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        super.drawScreen(mousex, mousey, partialticks);
        if (searchwidget != null) {
            searchwidget.drawWidget(mousex, mousey);
        }
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == btn_cancel) {
            this.mc.displayGuiScreen(backscreen);
        } else if (button.id == btn_previous) {
            if (page > 0) { page--; initGui(); }
        } else if (button.id == btn_next) {
            if (page < maxpage) { page++; initGui(); }
        } else if (button.id >= btn_list_start) {
            int idx = button.id - btn_list_start;
            if (idx >= 0 && idx < filteredeffectids.size()) {
                selectEffect(filteredeffectids.get(idx));
            }
        }
    }
    private void selectEffect(int potionid) {
        backscreen.setEffectPotionID(effectindex, potionid);
        this.mc.displayGuiScreen(backscreen);
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        if (searchwidget != null) {
            searchwidget.mouseClicked(mousex, mousey, mousebutton);
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (keycode == 1) {
            this.mc.displayGuiScreen(backscreen);
            return;
        }
        if (searchwidget != null && searchwidget.isFocused()) {
            boolean handled = searchwidget.keyTyped(typedchar, keycode, query -> {
                applySearch(query);
                initGui();
            });
            if (handled) return;
        }
        super.keyTyped(typedchar, keycode);
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
}
