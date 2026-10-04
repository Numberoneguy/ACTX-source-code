package net.minecraft.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

/**
 * Lets the player customize how ChatTranslator displays auto-translated
 * chat: whether it's on at all, the prefix text ("Translation: "), its
 * color, and whether it's bold.
 */
public class GuiActTranslationSettings extends GuiScreen {

    public static final EnumChatFormatting[] selectable_colors = java.util.Arrays.stream(EnumChatFormatting.values())
        .filter(EnumChatFormatting::isColor)
        .toArray(EnumChatFormatting[]::new);
    private static final int on = 0x66FF00;
    private static final int off = 0xFF0000;

    private final GuiScreen parentScreen;
    protected String screenTitle = "Translation Settings";

    private GuiTextField prefixField;
    private GuiButton colorButton;
    private GuiButton boldButton;
    private GuiButton enabledButton;

    private int colorIndex;
    private boolean boldEnabled;
    private boolean translateEnabled;

    public GuiActTranslationSettings(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    public void initGui() {
        this.screenTitle = I18n.format("act.translationSettings");
        this.buttonList.clear();

        this.colorIndex = indexOfColor(ChatTranslator.getPrefixColor());
        this.boldEnabled = ChatTranslator.isPrefixBold();
        this.translateEnabled = ChatTranslator.isEnabled();

        int left = this.width / 2 - 100;
        int fieldWidth = 200;

        this.prefixField = new GuiTextField(0, this.fontRendererObj, left, this.height / 6 + 6, fieldWidth, 20);
        this.prefixField.setMaxStringLength(48);
        this.prefixField.setText(ChatTranslator.getPrefixText());

        String colorDisplay = selectable_colors[this.colorIndex].toString() + selectable_colors[this.colorIndex].name();

        this.buttonList.add(this.colorButton = new GuiButton(301, left, this.height / 6 + 48, fieldWidth, 20,
                I18n.format("gui.act.translate.prefixColor", colorDisplay)));
        this.buttonList.add(this.boldButton = new GuiButton(302, left, this.height / 6 + 72, fieldWidth, 20, ""));
        this.buttonList.add(this.enabledButton = new GuiButton(303, left, this.height / 6 + 96, fieldWidth, 20, ""));

        this.buttonList.add(new GuiButton(200, left, this.height / 6 + 168, fieldWidth, 20,
                I18n.format("gui.done", new Object[0])));
    }

    private static int indexOfColor(EnumChatFormatting color) {
        for (int i = 0; i < selectable_colors.length; ++i) {
            if (selectable_colors[i] == color) {
                return i;
            }
        }
        return 0;
    }

    protected void actionPerformed(GuiButton parGuiButton) {
        if (!parGuiButton.enabled) {
            return;
        }

        if (parGuiButton.id == 301) {
            this.colorIndex = (this.colorIndex + 1) % selectable_colors.length;
            String colorDisplay = selectable_colors[this.colorIndex].toString() + selectable_colors[this.colorIndex].name();
            this.colorButton.displayString = I18n.format("gui.act.translate.prefixColor", colorDisplay);
        }

        if (parGuiButton.id == 302) {
            this.boldEnabled = !this.boldEnabled;
        }

        if (parGuiButton.id == 303) {
            this.translateEnabled = !this.translateEnabled;
        }

        if (parGuiButton.id == 200) {
            String text = this.prefixField.getText();

            // 1. Update runtime ChatTranslator state
            ChatTranslator.setPrefixText(text);
            ChatTranslator.setPrefixColor(selectable_colors[this.colorIndex]);
            ChatTranslator.setPrefixBold(this.boldEnabled);
            ChatTranslator.setEnabled(this.translateEnabled);

            // 2. Persist to GameSettings and save options to browser storage (_eaglercraftX.g)
            if (this.mc != null && this.mc.gameSettings != null) {
                this.mc.gameSettings.chatTranslatePrefix = text;
                this.mc.gameSettings.chatTranslateColor = this.colorIndex;
                this.mc.gameSettings.chatTranslateBold = this.boldEnabled;
                this.mc.gameSettings.chatTranslateEnabled = this.translateEnabled;
                
                this.mc.gameSettings.saveOptions();
            }

            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    protected void keyTyped(char parChar1, int parInt1) {
        this.prefixField.textboxKeyTyped(parChar1, parInt1);
        if (parInt1 == 1) {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    protected void mouseClicked(int mx, int my, int button) {
        super.mouseClicked(mx, my, button);
        this.prefixField.mouseClicked(mx, my, button);
    }

    public void updateScreen() {
        this.prefixField.updateCursorCounter();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 15, 16777215);

        this.drawString(this.fontRendererObj, I18n.format("gui.act.translate.outputText"), this.width / 2 - 100,
                this.height / 6 - 6, 10526880);
        this.prefixField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);

        int boldColor = this.boldEnabled ? on : off;
        this.drawCenteredString(this.fontRendererObj, I18n.format("gui.act.translate.bold"),
                this.boldButton.xPosition + this.boldButton.width / 2,
                this.boldButton.yPosition + (this.boldButton.height - 8) / 2,
                boldColor);

        int toggleColor = this.translateEnabled ? on : off;
        this.drawCenteredString(this.fontRendererObj, I18n.format("gui.act.translate.autoTranslate"),
                this.enabledButton.xPosition + this.enabledButton.width / 2,
                this.enabledButton.yPosition + (this.enabledButton.height - 8) / 2,
                toggleColor);
    }
}