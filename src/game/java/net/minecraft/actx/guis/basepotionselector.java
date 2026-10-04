package net.minecraft.actx.guis;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.minecraft.actx.selector.searchwidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
public class basepotionselector extends GuiScreen {
    private final GuiScreen parent;
    private final Consumer<Potion> callback;
    private final List<PotionEntry> alleffects = new ArrayList<>();
    private List<PotionEntry> filteredeffects = new ArrayList<>();
    private searchwidget searchwidget;
    private int scrolloffset = 0;
    private static final int items_per_page = 24;
    private static class PotionEntry {
        final Potion potion;
        final String displayname;
        PotionEntry(Potion potion, String displayname) {
            this.potion = potion;
            this.displayname = displayname;
        }
    }
    public basepotionselector(GuiScreen parent, Consumer<Potion> callback) {
        this.parent = parent;
        this.callback = callback;
        setupeffectslist();
    }
    private void setupeffectslist() {
        for (Potion pot : Potion.potionTypes) {
            if (pot != null && pot.getName() != null) {
                String localizedname = I18n.format(pot.getName());
                alleffects.add(new PotionEntry(pot, localizedname));
            }
        }
        filteredeffects.addAll(alleffects);
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        int searchw = 300;
        int startx = (this.width - searchw) / 2;
        String oldsearch = searchwidget != null ? searchwidget.getText() : "";
        this.searchwidget = new searchwidget(0, this.fontRendererObj, startx, 15, searchw, 20);
        this.searchwidget.setText(oldsearch);
        this.searchwidget.setFocused(true);
        this.buttonList.add(new GuiButton(100, this.width / 2 - 80, this.height - 28, 160, 20, I18n.format("gui.cancel")));
        this.buttonList.add(new GuiButton(101, this.width / 2 - 105, this.height - 28, 20, 20, "<-"));
        this.buttonList.add(new GuiButton(102, this.width / 2 + 85, this.height - 28, 20, 20, "->"));
        updatelistbuttons();
    }
    private void updatelistbuttons() {
        this.buttonList.removeIf(btn -> btn.id < 100);
        int starty = 40;
        int btnwidth = 150;
        int btnheight = 20;
        for (int i = 0; i < items_per_page; i++) {
            int index = scrolloffset + i;
            if (index < filteredeffects.size()) {
                PotionEntry entry = filteredeffects.get(index);
                int col = i % 2;
                int row = i / 2;
                int x = (col == 0) ? (this.width / 2 - 155) : (this.width / 2 + 5);
                int y = starty + (row * btnheight);
                this.buttonList.add(new GuiButton(i, x, y, btnwidth, btnheight, entry.displayname));
            }
        }
    }
    private void filterlist(String query) {
        String text = query == null ? "" : query.toLowerCase();
        filteredeffects.clear();
        for (PotionEntry entry : alleffects) {
            if (entry.displayname.toLowerCase().contains(text)) {
                filteredeffects.add(entry);
            }
        }
        scrolloffset = 0;
        updatelistbuttons();
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 100) {
            this.mc.displayGuiScreen(this.parent);
        } else if (button.id == 101) {
            if (scrolloffset - items_per_page >= 0) {
                scrolloffset -= items_per_page;
                updatelistbuttons();
            }
        } else if (button.id == 102) {
            if (scrolloffset + items_per_page < filteredeffects.size()) {
                scrolloffset += items_per_page;
                updatelistbuttons();
            }
        } else if (button.id >= 0 && button.id < items_per_page) {
            int index = scrolloffset + button.id;
            if (index < filteredeffects.size()) {
                callback.accept(filteredeffects.get(index).potion);
                this.mc.displayGuiScreen(this.parent);
            }
        }
    }
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        this.drawDefaultBackground();
        if (this.searchwidget != null) {
            this.searchwidget.drawWidget(mousex, mousey);
        }
        super.drawScreen(mousex, mousey, partialticks);
    }
    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.searchwidget != null) {
            this.searchwidget.updateScreen();
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (keycode == 1) {
            this.mc.displayGuiScreen(this.parent);
            return;
        }
        if (this.searchwidget != null && this.searchwidget.isFocused()) {
            boolean handled = this.searchwidget.keyTyped(typedchar, keycode, this::filterlist);
            if (handled) return;
        }
        super.keyTyped(typedchar, keycode);
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        if (this.searchwidget != null) {
            this.searchwidget.mouseClicked(mousex, mousey, mousebutton);
        }
    }
    @Override
    public boolean doesGuiPauseGame() {return true;}
}
