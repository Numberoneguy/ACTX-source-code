package net.minecraft.client.gui;

import java.util.List;

import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;

public class GuiActAnimationOptions extends GuiScreen {

    private static final int previous = 10;
    private static final int next = 11;
    private static final int createNew = 12;
    private static final int delete = 13;
    private static final int save = 14;
    private static final int done = 200;
    private final GuiScreen parentScreen;
    private String screenTitle;
    private String statusMessage = "";
    private List<String> names;
    private String editingOriginalName;

    private GuiTextField nameField;
    private GuiTextField intervalField;
    private GuiTextField framesField;
    private int focusedField = 0; // 0 = name, 1 = interval, 2 = frames and \n = new frame

    public GuiActAnimationOptions(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }
    @Override
    public void initGui() {
        this.screenTitle = GuiActOptions.translate("act.animationOptions");
        this.buttonList.clear();
        int fieldWidth = 240;
        int fieldX = this.width / 2 - fieldWidth / 2;
        int nameY = this.height / 6 + 30;
        int intervalY = nameY + 30;
        int framesY = intervalY + 30;
        this.nameField = new GuiTextField(20, this.fontRendererObj, fieldX, nameY, fieldWidth, 16);
        this.nameField.setMaxStringLength(64);
        this.intervalField = new GuiTextField(21, this.fontRendererObj, fieldX, intervalY, fieldWidth, 16);
        this.intervalField.setMaxStringLength(8);
        this.framesField = new GuiTextField(22, this.fontRendererObj, fieldX, framesY, fieldWidth, 16);
        this.framesField.setMaxStringLength(32767);
        int rowY = framesY + 30;
        int btnW = 78, gap = 4;
        this.buttonList.add(new GuiButton(previous, fieldX, rowY, btnW, 20, GuiActOptions.translate("gui.ok.back")));
        this.buttonList.add(new GuiButton(createNew, fieldX + btnW + gap, rowY, btnW, 20, GuiActOptions.translate("act.animation.new")));
        this.buttonList.add(new GuiButton(next, fieldX + (btnW + gap) * 2, rowY, btnW, 20, GuiActOptions.translate("gui.ok.next")));
        this.buttonList.add(new GuiButton(delete, fieldX, rowY + 24, fieldWidth / 2 - 2, 20, GuiActOptions.translate("act.animation.delete")));
        this.buttonList.add(new GuiButton(save, fieldX + fieldWidth / 2 + 2, rowY + 24, fieldWidth / 2 - 2, 20, GuiActOptions.translate("act.animation.save")));

        this.buttonList.add(new GuiButton(done, this.width / 2 - 100, this.height - 27, 200, 20,
                GuiActOptions.translate("gui.done")));

        this.names = actxmiscdata.getAnimationNames();
        loadSlot(this.names.isEmpty() ? -1 : 0);
    }

    private void loadSlot(int idx) {
        if (idx < 0 || idx >= this.names.size()) {
            this.editingOriginalName = null;
            this.nameField.setText("");
            this.intervalField.setText("1000");
            this.framesField.setText("");
        } else {
            String name = this.names.get(idx);
            this.editingOriginalName = name;
            this.nameField.setText(name);
            this.intervalField.setText(Integer.toString(actxmiscdata.getAnimationInterval(name)));
            this.framesField.setText(GuiActTabOptions.denormalizeNewlines(
                    GuiActTabOptions.denormalizeAC(actxmiscdata.getAnimationFramesJoined(name))));
        }
        this.statusMessage = "";
    }

    private void prev() {
        if (this.names.isEmpty()) return;
        int idx = this.names.indexOf(this.editingOriginalName);
        loadSlot(idx <= 0 ? this.names.size() - 1 : idx - 1);
    }

    private void next() {
        if (this.names.isEmpty()) return;
        int idx = this.names.indexOf(this.editingOriginalName);
        loadSlot((idx < 0 || idx >= this.names.size() - 1) ? 0 : idx + 1);
    }

    private void save() {
        String newName = this.nameField.getText().trim();
        int interval;
        try {
            interval = Integer.parseInt(this.intervalField.getText().trim());
        } catch (NumberFormatException e) {
            interval = 1000;
        }
        String framesRaw = GuiActTabOptions.convertAC(GuiActTabOptions.processEscapes(this.framesField.getText()));
        boolean ok = actxmiscdata.setAnimation(newName, interval, framesRaw);
        if (ok) {
            if (this.editingOriginalName != null && !this.editingOriginalName.equals(newName)) {
                actxmiscdata.removeAnimation(this.editingOriginalName);
                SingleplayerServerController.removeAnimation(this.editingOriginalName);
            }
            SingleplayerServerController.setAnimation(newName, interval, framesRaw);
            this.names = actxmiscdata.getAnimationNames();
            this.editingOriginalName = newName;
            this.statusMessage = GuiActOptions.translate("act.animation.saved");
        } else {
            this.statusMessage = GuiActOptions.translate("act.animation.invalid");
        }
    }

    private void delete() {
        if (this.editingOriginalName == null) return;
        actxmiscdata.removeAnimation(this.editingOriginalName);
        SingleplayerServerController.removeAnimation(this.editingOriginalName);
        this.names = actxmiscdata.getAnimationNames();
        loadSlot(this.names.isEmpty() ? -1 : 0);
        this.statusMessage = GuiActOptions.translate("act.animation.deleted");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        switch (button.id) {
            case previous: prev(); break;
            case next: next(); break;
            case createNew: loadSlot(-1); break;
            case delete: delete(); break;
            case save: save(); break;
            case done: this.mc.displayGuiScreen(this.parentScreen); break;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.nameField.updateCursorCounter();
        this.intervalField.updateCursorCounter();
        this.framesField.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) { // ESC
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }
        if (keyCode == 200) { // UP -- previous field
            this.focusedField = (this.focusedField + 2) % 3;
            refocus();
            return;
        }
        if (keyCode == 208) { // DOWN -- next field
            this.focusedField = (this.focusedField + 1) % 3;
            refocus();
            return;
        }

        GuiTextField active = this.focusedField == 0 ? this.nameField
                : this.focusedField == 1 ? this.intervalField : this.framesField;
        if (active.textboxKeyTyped(typedChar, keyCode)) return;

        super.keyTyped(typedChar, keyCode);
    }

    private void refocus() {
        this.nameField.setFocused(this.focusedField == 0);
        this.intervalField.setFocused(this.focusedField == 1);
        this.framesField.setFocused(this.focusedField == 2);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
        this.intervalField.mouseClicked(mouseX, mouseY, mouseButton);
        this.framesField.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.nameField.isFocused()) this.focusedField = 0;
        else if (this.intervalField.isFocused()) this.focusedField = 1;
        else if (this.framesField.isFocused()) this.focusedField = 2;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 15, 16777215);

        this.drawString(this.fontRendererObj, GuiActOptions.translate("act.animation.name"),
                this.nameField.xPosition, this.nameField.yPosition - 10, 0xAAAAAA);
        this.drawString(this.fontRendererObj, GuiActOptions.translate("act.animation.interval"),
                this.intervalField.xPosition, this.intervalField.yPosition - 10, 0xAAAAAA);
        this.drawString(this.fontRendererObj, GuiActOptions.translate("act.animation.frames"),
                this.framesField.xPosition, this.framesField.yPosition - 10, 0xAAAAAA);

        this.nameField.drawTextBox();
        this.intervalField.drawTextBox();
        this.framesField.drawTextBox();

        if (!this.statusMessage.isEmpty()) {
            this.drawCenteredString(this.fontRendererObj, this.statusMessage,
                    this.width / 2, this.framesField.yPosition + 40, 0xFFFF55);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}