package net.minecraft.client.gui;

import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.selector.buttoncolored;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

/**
 * Boss bar settings. Layout mirrors GuiActTabOptions: option buttons in a left column, a single labelled
 * text field above the Done/Cancel row. The bar is drawn here as a preview only: it is painted straight onto this
 * screen and never touches BossStatus. The in-game bar comes from the server (ACTX|BossBar), sent when Done is pressed.
 */
public class GuiActBossBarOptions extends GuiScreen {

    private static final ResourceLocation GUI_ICONS = new ResourceLocation("textures/gui/icons.png");

    private static final int toggle_bossbar = 1;
    private static final int preview = 2;
    private static final int reset          = 3;
    private static final int done           = 4;
    private static final int cancel         = 5;

    private static final int on    = 0x66FF00;
    private static final int off   = 0xFF0000;
    private static final int title_color = 0x7F00FF;
    private final GuiScreen parentScreen;
    private String screenTitle;
    private boolean enableBossBar;
    private boolean showPreview;
    private GuiTextField titleTextField;
    private boolean titleLoaded = false;

    public GuiActBossBarOptions(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.screenTitle = I18n.format("act.bossBarOptions.title");
        this.buttonList.clear();
        actxmiscdata.ensureLoaded();
        this.enableBossBar = actxmiscdata.isBossBarEnabled();
        this.showPreview = actxmiscdata.isBossBarPreview();
        this.buttonList.add(new buttoncolored(toggle_bossbar, 6, this.height / 2 - 34, 94, 20,
                getBossBarToggleLabel(), this.enableBossBar ? on : off));
        this.buttonList.add(new buttoncolored(preview, 6, this.height / 2 - 10, 94, 20,
                getPreviewToggleLabel(), this.showPreview ? on : off));
        this.buttonList.add(new GuiButton(reset, 6, this.height / 2 + 14, 94, 20,
                I18n.format("controls.reset")));

        this.buttonList.add(new GuiButton(done, this.width / 2 - 105, this.height - 26, 100, 20,
                I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(cancel, this.width / 2 + 5, this.height - 26, 100, 20,
                I18n.format("gui.cancel")));
        int fieldWidth = 280;
        int fieldHeight = 18;
        int fieldX = this.width / 2 - fieldWidth / 2;
        int fieldY = this.height - 34 - fieldHeight;
        String existingText = this.titleLoaded
                ? this.titleTextField.getText()
                : GuiActTabOptions.denormalizeAC(actxmiscdata.getBossBarTitle());
        this.titleLoaded = true;
        this.titleTextField = new GuiTextField(10, this.fontRendererObj, fieldX, fieldY, fieldWidth, fieldHeight);
        this.titleTextField.setMaxStringLength(128);
        this.titleTextField.setFocused(true);
        this.titleTextField.setText(existingText);
        this.titleTextField.setTextColor(0xFFFFFF);
        this.titleTextField.setDisabledTextColour(0x707070);
    }
    private String getBossBarToggleLabel() {
        return I18n.format("act.bossBarOptions.toggle") + ": "
                + (this.enableBossBar ? I18n.format("act.tabOptions.on") : I18n.format("act.tabOptions.off"));
    }
    private String getPreviewToggleLabel() {
        return I18n.format("act.bossBarOptions.preview") + ": "
                + (this.showPreview ? I18n.format("act.tabOptions.on") : I18n.format("act.tabOptions.off"));
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }
        if (button.id == toggle_bossbar) {
            this.enableBossBar = !this.enableBossBar;
            this.buttonList.set(0, new buttoncolored(toggle_bossbar, button.xPosition, button.yPosition,
                    button.width, button.height, getBossBarToggleLabel(), this.enableBossBar ? on : off));
        } else if (button.id == preview) {
            this.showPreview = !this.showPreview;
            this.buttonList.set(1, new buttoncolored(preview, button.xPosition, button.yPosition,
                    button.width, button.height, getPreviewToggleLabel(), this.showPreview ? on : off));
        } else if (button.id == reset) {
            actxmiscdata.resetBossBarOptions();
            this.titleTextField.setText("");
            initGui();
        } else if (button.id == done) {
            actxmiscdata.setBossBarEnabled(this.enableBossBar);
            actxmiscdata.setBossBarPreview(this.showPreview);
            actxmiscdata.setBossBarTitle(GuiActTabOptions.convertAC(this.titleTextField.getText()));
            try { // push to the integrated server so everyone sees it (no-op if no world is running)
                SingleplayerServerController.setBossBar(this.enableBossBar, actxmiscdata.getBossBarTitle());
            } catch (Throwable ignored) { }
            this.mc.displayGuiScreen(this.parentScreen);
        } else if (button.id == cancel) {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.titleTextField.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) { // ESC -- back to the parent screen, not the game
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }
        if (keyCode == 28 || keyCode == 156) { // ENTER / numpad ENTER -- the title is a single line
            return;
        }
        if (this.titleTextField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.titleTextField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 15, title_color);

        renderBossBarPreview();

        this.drawString(this.fontRendererObj, I18n.format("act.bossBarOptions.titleField") + ":",
                this.titleTextField.xPosition, this.titleTextField.yPosition - 10, 0xFFFFFF);
        this.titleTextField.drawTextBox();

        if (this.titleTextField.getText().isEmpty() && !this.titleTextField.isFocused()) {
            this.drawString(this.fontRendererObj, I18n.format("act.bossBarOptions.titleHint"),
                    this.titleTextField.xPosition + 4, this.titleTextField.yPosition + 5, 0x707070);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    private void renderBossBarPreview() {
        int barWidth = 182;
        int barHeight = 5;
        int x = this.width / 2 - barWidth / 2;
        int y = 50;

        String raw = this.titleTextField.getText();
        if (raw.isEmpty()) {
            raw = I18n.format("entity.EnderDragon.name");
        }
        String displayTitle = this.showPreview ? GuiActTabOptions.convertAC(raw) : raw;

        this.mc.getTextureManager().bindTexture(GUI_ICONS);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawTexturedModalRect(x, y, 0, 74, barWidth, barHeight); // empty bar
        int fillWidth = (int) (0.75F * (float) (barWidth + 1));
        this.drawTexturedModalRect(x, y, 0, 79, fillWidth, barHeight); // filled part (75%)

        this.fontRendererObj.drawStringWithShadow(displayTitle,
                (float) (this.width / 2 - this.fontRendererObj.getStringWidth(displayTitle) / 2),
                (float) (y - 10), 0xFFFFFF);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() { return true; }
}