package net.minecraft.actx.guis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.actx.selector.searchwidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
public class potionmodifier extends GuiScreen {
    protected GuiScreen parentscreen;
    protected EditorState state;
    private List<EffectData> activeeffects = new ArrayList<>();
    private List<EffectData> filteredeffects = new ArrayList<>();
    private int page = 0;
    private int elementsperpage = 2;
    private searchwidget searchwidget;
    private GuiButton btnbasetype;
    private GuiButton btnpreviouspage;
    private GuiButton btnnextpage;
    private class ElementUI {
        int effectindex;
        int ypos;
        GuiTextField durationfield;
        GuiTextField amplifierfield;
        GuiButton ambientbtn;
        GuiButton particlesbtn;
    }
    private List<ElementUI> uielements = new ArrayList<>();
    private class EffectData {
        int duration = 0;
        int amplifier = 0;
        int potionid = 1;
        boolean ambient = false;
        boolean showparticles = true;
    }
    public potionmodifier(EditorState state, GuiScreen parentscreen) {
        this.state = state;
        this.parentscreen = parentscreen;
        loadEffectsFromNBT();
    }
    private void loadEffectsFromNBT() {
        activeeffects.clear();
        if (state.stack != null && state.stack.hasTagCompound()) {
            NBTTagCompound tags = state.stack.getTagCompound();
            if (tags.hasKey("CustomPotionEffects", 9)) {
                NBTTagList list = tags.getTagList("CustomPotionEffects", 10);
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound comp = list.getCompoundTagAt(i);
                    EffectData data = new EffectData();
                    data.potionid = comp.getByte("Id");
                    data.amplifier = comp.getByte("Amplifier");
                    data.duration = comp.getInteger("Duration");
                    data.ambient = comp.getBoolean("Ambient");
                    data.showparticles = comp.getBoolean("ShowParticles");
                    activeeffects.add(data);
                }
            }
        }
        applySearch("");
    }
    private void applySearch(String query) {
        filteredeffects.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredeffects.addAll(activeeffects);
        } else {
            String q = query.toLowerCase().trim();
            for (EffectData data : activeeffects) {
                Potion p = Potion.potionTypes[data.potionid];
                String name = p != null ? I18n.format(p.getName()).toLowerCase() : "";
                if (name.contains(q) || String.valueOf(data.potionid).contains(q)) {
                    filteredeffects.add(data);
                }
            }
        }
        this.page = 0;
    }
    private void saveFields() {
        for (ElementUI ui : uielements) {
            if (ui.effectindex < activeeffects.size()) {
                EffectData data = activeeffects.get(ui.effectindex);
                try { data.duration = Integer.parseInt(ui.durationfield.getText()); } catch (NumberFormatException ex) {}
                try { data.amplifier = Integer.parseInt(ui.amplifierfield.getText()); } catch (NumberFormatException ex) {}
            }
        }
    }
    private void saveEffectsToNBT() {
        saveFields();
        if (state.stack == null) return;
        if (!state.stack.hasTagCompound()) {
            state.stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound tags = state.stack.getTagCompound();
        if (activeeffects.isEmpty()) {
            tags.removeTag("CustomPotionEffects");
            if (tags.hasNoTags()) state.stack.setTagCompound(null);
        } else {
            NBTTagList list = new NBTTagList();
            for (EffectData data : activeeffects) {
                NBTTagCompound comp = new NBTTagCompound();
                comp.setByte("Id", (byte) data.potionid);
                comp.setByte("Amplifier", (byte) data.amplifier);
                comp.setInteger("Duration", data.duration);
                comp.setBoolean("Ambient", data.ambient);
                comp.setBoolean("ShowParticles", data.showparticles);
                list.appendTag(comp);
            }
            tags.setTag("CustomPotionEffects", list);
        }
    }
    public void setEffectPotionID(int index, int potionid) {
        saveFields();
        if (index >= 0 && index < activeeffects.size()) {
            activeeffects.get(index).potionid = potionid;
            if (searchwidget != null) {
                applySearch(searchwidget.getText());
            }
        }
    }
    @Override
    public void initGui() {
        this.buttonList.clear();
        this.uielements.clear();
        int topoffset = 70;
        int elementheight = 55;
        int availablespace = this.height - topoffset - 40;
        this.elementsperpage = Math.max(1, availablespace / elementheight);
        String oldsearch = "";
        if (searchwidget != null) oldsearch = searchwidget.getText();
        this.searchwidget = new searchwidget(0, this.fontRendererObj, this.width / 2 - 150, 15, 300, 16);
        this.searchwidget.setText(oldsearch);
        applySearch(oldsearch);
        String basename = state.stack != null ? state.stack.getDisplayName() : I18n.format("item.potion.name");
        this.btnbasetype = new GuiButton(10, this.width / 2 - 100, 45, 200, 20, I18n.format("gui.act.modifier.type") + " (" + basename + ")");
        this.buttonList.add(btnbasetype);
        int startindex = page * elementsperpage;
        int endindex = Math.min(startindex + elementsperpage, filteredeffects.size());
        for (int i = startindex; i < endindex; i++) {
            int displayindex = i - startindex;
            int y = topoffset + (displayindex * elementheight);
            EffectData data = filteredeffects.get(i);
            int globalindex = activeeffects.indexOf(data);
            ElementUI ui = new ElementUI();
            ui.effectindex = globalindex;
            ui.ypos = y;
            ui.durationfield = new GuiTextField(globalindex * 2 + 1000, this.fontRendererObj, this.width / 2 - 150, y, 90, 20);
            ui.durationfield.setMaxStringLength(8);
            ui.durationfield.setText(String.valueOf(data.duration));
            ui.amplifierfield = new GuiTextField(globalindex * 2 + 1001, this.fontRendererObj, this.width / 2 - 150, y + 25, 90, 20);
            ui.amplifierfield.setMaxStringLength(3);
            ui.amplifierfield.setText(String.valueOf(data.amplifier));
            this.uielements.add(ui);
            int baseid = 100 + (globalindex * 10);
            Potion p = Potion.potionTypes[data.potionid];
            String pname = p != null ? I18n.format(p.getName()) : I18n.format("gui.act.modifier.tag.editor.unknown");
            this.buttonList.add(new GuiButton(baseid, this.width / 2 - 45, y, 145, 20, I18n.format("gui.act.modifier.type") + " (" + pname + ")"));
            this.buttonList.add(new GuiButton(baseid + 1, this.width / 2 + 105, y, 20, 20, "\u00a7c-"));
            this.buttonList.add(new GuiButton(baseid + 2, this.width / 2 + 130, y, 20, 20, "\u00a7a+"));
            ui.ambientbtn = new GuiButton(baseid + 3, this.width / 2 - 45, y + 25, 70, 20, "");
            ui.particlesbtn = new GuiButton(baseid + 4, this.width / 2 + 30, y + 25, 80, 20, "");
            this.buttonList.add(ui.ambientbtn);
            this.buttonList.add(ui.particlesbtn);
            this.buttonList.add(new GuiButton(baseid + 5, this.width / 2 + 115, y + 25, 35, 20, I18n.format("gui.act.give.copy")));
        }
        if (filteredeffects.size() >= startindex && filteredeffects.size() < startindex + elementsperpage) {
            int displayindex = filteredeffects.size() - startindex;
            int ypos = topoffset + (displayindex * elementheight);
            this.buttonList.add(new GuiButton(0, this.width / 2 - 150, ypos, 300, 20, "\u00a7a+"));
        }
        int maxpages = filteredeffects.isEmpty() ? 0 : (filteredeffects.size() - 1) / elementsperpage;
        this.btnpreviouspage = new GuiButton(1, this.width / 2 - 105, this.height - 28, 20, 20, "<-");
        this.btnpreviouspage.enabled = (page > 0);
        this.buttonList.add(this.btnpreviouspage);
        this.buttonList.add(new GuiButton(2, this.width / 2 - 80, this.height - 28, 75, 20, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(3, this.width / 2 + 5, this.height - 28, 75, 20, I18n.format("gui.cancel")));
        this.btnnextpage = new GuiButton(4, this.width / 2 + 85, this.height - 28, 20, 20, "->");
        this.btnnextpage.enabled = (page < maxpages);
        this.buttonList.add(this.btnnextpage);
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        saveFields();
        if (button.id == 2) {
            saveEffectsToNBT();
            this.mc.displayGuiScreen(this.parentscreen);
        } else if (button.id == 3) {
            this.mc.displayGuiScreen(this.parentscreen);
        } else if (button.id == 1) {
            if (page > 0) { page--; initGui(); }
        } else if (button.id == 4) {
            int maxpages = (filteredeffects.size() - 1) / elementsperpage;
            if (page < maxpages) { page++; initGui(); }
        } else if (button.id == 0) {
            activeeffects.add(new EffectData());
            applySearch(searchwidget != null ? searchwidget.getText() : "");
            page = (filteredeffects.size() - 1) / elementsperpage;
            initGui();
        } else if (button.id == 10) {
            this.mc.displayGuiScreen(new potionlistmodifier(state, this));
        } else if (button.id >= 100) {
            int index = (button.id - 100) / 10;
            int action = (button.id - 100) % 10;
            if (index >= 0 && index < activeeffects.size()) {
                EffectData data = activeeffects.get(index);
                if (action == 0) {
                    this.mc.displayGuiScreen(new potioneffectlistmodifier(state, this, index));
                } else if (action == 1) {
                    activeeffects.remove(index);
                    applySearch(searchwidget != null ? searchwidget.getText() : "");
                    int maxpages = filteredeffects.isEmpty() ? 0 : (filteredeffects.size() - 1) / elementsperpage;
                    if (page > maxpages) page = Math.max(0, maxpages);
                    initGui();
                } else if (action == 2) {
                    activeeffects.add(index + 1, new EffectData());
                    applySearch(searchwidget != null ? searchwidget.getText() : "");
                    initGui();
                } else if (action == 3) {
                    data.ambient = !data.ambient;
                    initGui();
                } else if (action == 4) {
                    data.showparticles = !data.showparticles;
                    initGui();
                } else if (action == 5) {
                    EffectData copy = new EffectData();
                    copy.duration = data.duration;
                    copy.amplifier = data.amplifier;
                    copy.potionid = data.potionid;
                    copy.ambient = data.ambient;
                    copy.showparticles = data.showparticles;
                    activeeffects.add(index + 1, copy);
                    applySearch(searchwidget != null ? searchwidget.getText() : "");
                    initGui();
                }
            }
        }
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        this.drawDefaultBackground();
        this.drawString(this.fontRendererObj, I18n.format("gui.act.search") + " :", this.width / 2 - 200, 19, 0xFFDAA520);
        if (searchwidget != null) {
            searchwidget.drawWidget(mousex, mousey);
        }
        for (ElementUI ui : uielements) {
            this.drawString(this.fontRendererObj, I18n.format("gui.act.modifier.meta.potion.duration") + " :", this.width / 2 - 210, ui.ypos + 6, 0xFFFFFF);
            this.drawString(this.fontRendererObj, I18n.format("gui.act.modifier.meta.potion.amplifier") + " :", this.width / 2 - 210, ui.ypos + 31, 0xFFFFFF);
            ui.durationfield.drawTextBox();
            ui.amplifierfield.drawTextBox();
        }
        super.drawScreen(mousex, mousey, partialticks);
        for (ElementUI ui : uielements) {
            if (ui.effectindex >= activeeffects.size()) continue;
            EffectData data = activeeffects.get(ui.effectindex);
            int ambientcolor = data.ambient ? 0x66FF00 : 0xFF0000;
            this.drawCenteredString(this.fontRendererObj, I18n.format("gui.act.modifier.meta.potion.ambient"),
                    ui.ambientbtn.xPosition + ui.ambientbtn.width / 2,
                    ui.ambientbtn.yPosition + (ui.ambientbtn.height - 8) / 2,
                    ambientcolor);
            int particlescolor = data.showparticles ? 0x66FF00 : 0xFF0000;
            this.drawCenteredString(this.fontRendererObj, I18n.format("gui.act.modifier.meta.potion.showParticles"),
                    ui.particlesbtn.xPosition + ui.particlesbtn.width / 2,
                    ui.particlesbtn.yPosition + (ui.particlesbtn.height - 8) / 2,
                    particlescolor);
        }
    }
    @Override
    public void updateScreen() {
        super.updateScreen();
        if (searchwidget != null) searchwidget.updateScreen();
        for (ElementUI ui : uielements) {
            ui.durationfield.updateCursorCounter();
            ui.amplifierfield.updateCursorCounter();
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (keycode == 1) {
            this.mc.displayGuiScreen(this.parentscreen);
            return;
        }
        if (searchwidget != null && searchwidget.isFocused()) {
            boolean handled = searchwidget.keyTyped(typedchar, keycode, query -> {
                saveFields();
                applySearch(query);
                initGui();
            });
            if (handled) return;
        }
        for (ElementUI ui : uielements) {
            if (ui.durationfield.textboxKeyTyped(typedchar, keycode)) return;
            if (ui.amplifierfield.textboxKeyTyped(typedchar, keycode)) return;
        }
        super.keyTyped(typedchar, keycode);
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        if (searchwidget != null) {
            searchwidget.mouseClicked(mousex, mousey, mousebutton);
        }
        for (ElementUI ui : uielements) {
            ui.durationfield.mouseClicked(mousex, mousey, mousebutton);
            ui.amplifierfield.mouseClicked(mousex, mousey, mousebutton);
        }
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
}
