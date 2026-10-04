package net.minecraft.client.gui;

import net.minecraft.actx.selector.buttoncolored;
import net.minecraft.actx.selector.buttonfilehighlight;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;

public class GuiActOptions extends GuiScreen {
    // welcome to act options brother.
    // ronaldo grin
    private final GuiScreen parentScreen;
    protected String screenTitle;
    public GuiActOptions(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }
    @Override
    public void initGui() {
        this.screenTitle = I18n.format("options.act");
        this.buttonList.clear();
        this.buttonList.add(new buttonfilehighlight(301, this.width / 2 - 100, this.height / 6 + 24, 200, 20,
                translate("act.miscDataOptions"), "actx_misc.yml", 0xFF0000));
        this.buttonList.add(new buttoncolored(200, this.width / 2 - 100, this.height / 6 + 48, 200, 20,
                I18n.format("act.bossBar"), 0x7F00FF));
        this.buttonList.add(new buttoncolored(302, this.width / 2 - 100, this.height / 6 + 72, 200, 20,
                I18n.format("act.tabOptions"), 0x7F00FF));
        this.buttonList.add(new buttoncolored(303, this.width / 2 - 100, this.height / 6 + 96, 200, 20,
                I18n.format("act.animationOptions"), 0x7F00FF));
        this.buttonList.add(new buttoncolored(304, this.width / 2 - 100, this.height / 6 + 120, 200, 20,
                I18n.format("act.translationSettings"), 0x4FC3F7));
        this.buttonList.add(new GuiButton(300, this.width / 2 - 100, this.height - 27, 200, 20,
                I18n.format("gui.done")));
    }

    @Override
    protected void actionPerformed(GuiButton parGuiButton) {
        if (!parGuiButton.enabled) {
            return;
        }

        if (parGuiButton.id == 301) {
            this.mc.displayGuiScreen(new GuiActMiscDataOptions(this));
        }

        if (parGuiButton.id == 200) {
            this.mc.displayGuiScreen(new GuiActBossBarOptions(this));
        }

        if (parGuiButton.id == 302) {
            this.mc.displayGuiScreen(new GuiActTabOptions(this));
        }

        if (parGuiButton.id == 303) {
            this.mc.displayGuiScreen(new GuiActAnimationOptions(this));
        }

        if (parGuiButton.id == 304) {
            this.mc.displayGuiScreen(new GuiActTranslationSettings(this));
        }

        if (parGuiButton.id == 300) {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 15, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    static String translate(String key, Object... args) {
        return new ChatComponentTranslation(key, args).getFormattedText();
    }
}