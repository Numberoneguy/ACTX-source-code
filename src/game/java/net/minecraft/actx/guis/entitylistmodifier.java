package net.minecraft.actx.guis;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.minecraft.actx.selector.searchwidget;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.resources.I18n;
public class entitylistmodifier extends GuiScreen {
    private static final int btn_cancel = 0;
    private static final int btn_previous = 1;
    private static final int btn_next = 2;
    private static final int btn_entity_base = 10;
    private static final String[][] entities = {
        { "gui.none", "0" },
        { "entity.Item.name", "1" },
        { "entity.XPOrb.name", "2" },
        { "entity.ThrownEgg.name", "7" },
        { "entity.LeashKnot.name", "8" },
        { "item.painting.name", "9" },
        { "item.arrow.name", "10" },
        { "item.snowball.name", "11" },
        { "entity.Fireball.name", "12" },
        { "entity.SmallFireball.name", "13" },
        { "entity.ThrownEnderpearl.name", "14" },
        { "entity.EyeOfEnderSignal.name", "15" },
        { "entity.Potion.name", "16" },
        { "entity.ThrownExpBottle.name", "17" },
        { "entity.PrimedTnt.name", "18" },
        { "entity.WitherSkull.name", "19" },
        { "entity.PrimedTnt.name","20" },
        { "entity.FallingSand.name", "21" },
        { "entity.FireworksRocketEntity.name", "22" },
        { "item.armorStand.name", "30"},
        { "entity.MinecartCommandBlock.name", "40" },
        { "item.boat.name", "41" },
        { "entity.MinecartRideable.name", "42" },
        { "entity.MinecartChest.name", "43" },
        { "entity.MinecartFurnace.name", "44" },
        { "entity.MinecartTNT.name", "45" },
        { "entity.MinecartHopper.name","46" },
        { "entity.MinecartSpawner.name", "47"},
        { "entity.Mob.name", "48" },
        { "entity.Monster.name", "49" },
        { "entity.Creeper.name", "50" },
        { "entity.Skeleton.name", "51" },
        { "entity.Spider.name", "52" },
        { "entity.Giant.name", "53" },
        { "entity.Zombie.name", "54" },
        { "entity.Slime.name", "55" },
        { "entity.Ghast.name", "56" },
        { "entity.PigZombie.name", "57" },
        { "entity.Enderman.name", "58" },
        { "entity.CaveSpider.name", "59" },
        { "entity.Silverfish.name", "60" },
        { "entity.Blaze.name", "61" },
        { "entity.LavaSlime.name", "62" },
        { "entity.EnderDragon.name", "63" },
        { "potion.wither", "64" },
        { "entity.Bat.name", "65" },
        { "entity.Witch.name", "66" },
        { "entity.Endermite.name", "67" },
        { "entity.Guardian.name", "68" },
        { "entity.Pig.name", "90" },
        { "entity.Sheep.name", "91" },
        { "entity.Cow.name", "92" },
        { "entity.Chicken.name", "93" },
        { "entity.Squid.name", "94" },
        { "entity.Wolf.name", "95" },
        { "entity.MushroomCow.name", "96" },
        { "entity.SnowMan.name", "97" },
        { "entity.Ozelot.name", "98" },
        { "entity.VillagerGolem.name", "99" },
        { "entity.horse.name", "100" },
        { "entity.Rabbit.name", "101" },
        { "entity.Villager.name", "120" },
    };
    private static final int cols = 2;
    private static final int rows_per_page = 9;
    private static final int slots_per_page = cols * rows_per_page;
    private final EditorState state;
    private final GuiScreen backscreen;
    private searchwidget searchwidget;
    private java.util.List<String[]> displayed = new java.util.ArrayList<String[]>();
    private int page = 0;
    private int gridx, gridy, btnw, btnh, gap;
    private int searchfieldw;
    entitylistmodifier(EditorState state, GuiScreen backscreen) {
        this.state = state;
        this.backscreen = backscreen;
    }
    private int pageCount() {
        return Math.max(1, (displayed.size() + slots_per_page - 1) / slots_per_page);
    }
    private void applySearch(String query) {
        displayed.clear();
        String q = query == null ? "" : query.toLowerCase().trim();
        for (String[] e : entities) {
            if (q.isEmpty() || I18n.format(e[0]).toLowerCase().contains(q)) {
                displayed.add(e);
            }
        }
        page = 0;
    }
    private void updateNavButtons() {
        for (Object o : this.buttonList) {
            GuiButton b = (GuiButton) o;
            if (b.id == btn_previous) b.enabled = page > 0;
            if (b.id == btn_next) b.enabled = page < pageCount() - 1;
        }
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        btnh = 20;
        gap = 4;
        int totalw = 600;
        btnw = (totalw - gap) / cols;
        gridx = (this.width - totalw) / 2;
        gridy = 40;
        searchfieldw = totalw;
        String oldsearch = searchwidget != null ? searchwidget.getText() : "";
        applySearch(oldsearch);
        String searchlabel = I18n.format("gui.act.search") + " : ";
        int labelwidth = this.fontRendererObj.getStringWidth(searchlabel);
        searchwidget = new searchwidget(0, this.fontRendererObj,
                gridx + labelwidth, 12, searchfieldw - labelwidth, btnh);
        searchwidget.setText(oldsearch);
        searchwidget.setFocused(true);
        int navy = this.height - 28;
        int navbtnw = 60;
        int cancelw = 80;
        int navcenterx = this.width / 2;
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(btn_previous, navcenterx - navbtnw - cancelw / 2 - gap, navy, navbtnw, btnh, "<-"));
        this.buttonList.add(new GuiButton(btn_cancel, navcenterx - cancelw / 2, navy, cancelw, btnh, I18n.format("gui.cancel")));
        this.buttonList.add(new GuiButton(btn_next, navcenterx + cancelw / 2 + gap, navy, navbtnw, btnh, "->"));
        rebuildEntityButtons();
        updateNavButtons();
    }
    private void rebuildEntityButtons() {
        java.util.Iterator<GuiButton> it = this.buttonList.iterator();
        while (it.hasNext()) {
            GuiButton b = it.next();
            if (b.id >= btn_entity_base) it.remove();
        }
        int start = page * slots_per_page;
        for (int i = 0; i < slots_per_page; i++) {
            int idx = start + i;
            if (idx >= displayed.size()) break;
            int col = i % cols;
            int row = i / cols;
            int x = gridx + col * (btnw + gap);
            int y = gridy + row * (btnh + gap);
            this.buttonList.add(new GuiButton(btn_entity_base + i, x, y, btnw, btnh, I18n.format(displayed.get(idx)[0])));
        }
    }
    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    @Override
    public void updateScreen() {
        super.updateScreen();
        if (searchwidget != null) {
            searchwidget.updateScreen();
        }
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        drawDefaultBackground();
        this.fontRendererObj.drawStringWithShadow("\u00a76" + I18n.format("gui.act.search") + " :", gridx, 16, 0xFFFFFF);
        if (searchwidget != null) {
            searchwidget.drawWidget(mousex, mousey);
        }
        String pg = (page + 1) + " / " + pageCount();
        int pw = this.fontRendererObj.getStringWidth(pg);
        this.fontRendererObj.drawStringWithShadow(pg,
                (this.width - pw) / 2,
                this.height - 28 - 14,
                0xFFAAAAAA);
        super.drawScreen(mousex, mousey, partialticks);
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == btn_cancel) {
            this.mc.displayGuiScreen(backscreen);
        } else if (button.id == btn_previous && page > 0) {
            page--;
            rebuildEntityButtons();
            updateNavButtons();
        } else if (button.id == btn_next && page < pageCount() - 1) {
            page++;
            rebuildEntityButtons();
            updateNavButtons();
        } else if (button.id >= btn_entity_base) {
            int idx = page * slots_per_page + (button.id - btn_entity_base);
            if (idx < displayed.size()) {
                state.metaStr = displayed.get(idx)[1];
                this.mc.displayGuiScreen(backscreen);
            }
        }
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
        if (keycode == 1) { this.mc.displayGuiScreen(backscreen); return; }
        if (searchwidget != null && searchwidget.isFocused()) {
            boolean handled = searchwidget.keyTyped(typedchar, keycode, query -> {
                applySearch(query);
                rebuildEntityButtons();
                updateNavButtons();
            });
            if (handled) return;
        }
        super.keyTyped(typedchar, keycode);
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
}
