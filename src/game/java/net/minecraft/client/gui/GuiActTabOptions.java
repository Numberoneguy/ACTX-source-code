package net.minecraft.client.gui;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiActTabOptions extends GuiScreen {
    private static final String[] dummy_skins = {
        "01.default_steve.png", "02.default_alex.png", "03.tennis_steve.png", "04.tennis_alex.png",
        "05.tuxedo_steve.png", "06.tuxedo_alex.png", "07.athlete_steve.png", "08.athlete_alex.png",
        "09.cyclist_steve.png", "10.cyclist_alex.png", "11.boxer_steve.png", "12.boxer_alex.png",
        "13.prisoner_steve.png", "14.prisoner_alex.png", "15.scottish_steve.png", "16.scottish_alex.png",
        "17.developer_steve.png", "18.developer_alex.png", "19.herobrine.png", "20.notch.png"
    };
    // dernomalizeAC = dermoalizeAmpersandCodes
    // convertAC = convertAmpersandCodes
    private static final int preview = 1;
    private static final int add_dummy      = 2;
    private static final int reset          = 3;
    private static final int done           = 4;
    private static final int cancel         = 5;
    private final GuiScreen parentScreen;
    private String screenTitle;
    private GuiTextField headerField;
    private GuiTextField footerField;
    private boolean editingHeader = true;
    private GuiPlayerTabOverlay previewOverlay;
    private boolean previewConvertColors = true;
    private GuiButton previewToggleBtn;
    private static final class DummyRow {
        final int skinIndex;
        final int pingMs;
        DummyRow(int skinIndex, int pingMs) {
            this.skinIndex = skinIndex;
            this.pingMs = pingMs;
        }
    }
    private final List<DummyRow> dummyRows = new ArrayList<DummyRow>();
    private final Random rng = new Random();
    public GuiActTabOptions(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.screenTitle = I18n.format("act.tabOptions");
        this.buttonList.clear();
        actxmiscdata.ensureLoaded();
        this.previewOverlay = new GuiPlayerTabOverlay(this.mc, this.mc.ingameGUI);
        int fieldWidth = 280;
        int fieldX = this.width / 2 - fieldWidth / 2;
        int fieldHeight = 18;
        int footerFieldY = this.height - 34 - fieldHeight;
        int headerFieldY = footerFieldY - 10 - 8 - fieldHeight;
        this.headerField = new GuiTextField(10, this.fontRendererObj, fieldX, headerFieldY, fieldWidth, fieldHeight);
        this.headerField.setMaxStringLength(1024);
        this.headerField.setText(denormalizeNewlines(denormalizeAC(actxmiscdata.getTabHeader())));
        this.headerField.setFocused(this.editingHeader);
        this.footerField = new GuiTextField(11, this.fontRendererObj, fieldX, footerFieldY, fieldWidth, fieldHeight);
        this.footerField.setMaxStringLength(1024);
        this.footerField.setText(denormalizeNewlines(denormalizeAC(actxmiscdata.getTabFooter())));
        this.footerField.setFocused(!this.editingHeader);
        this.previewToggleBtn = new GuiButton(preview, 6, this.height / 2 - 34, 94, 20, "");
        this.buttonList.add(this.previewToggleBtn);
        this.buttonList.add(new GuiButton(add_dummy, 6, this.height / 2 - 10, 94, 20,
                I18n.format("act.tabOptions.addDummy")));
        this.buttonList.add(new GuiButton(reset, 6, this.height / 2 + 14, 94, 20,
                I18n.format("act.tabOptions.resetDefault")));

        this.buttonList.add(new GuiButton(done, this.width / 2 - 105, this.height - 26, 100, 20,
                I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(cancel, this.width / 2 + 5, this.height - 26, 100, 20,
                I18n.format("gui.cancel")));
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }
        if (button.id == preview) {
            this.previewConvertColors = !this.previewConvertColors;
        } else if (button.id == add_dummy) {
            int skinIndex = this.rng.nextInt(dummy_skins.length);
            int pingMs = this.rng.nextInt(1250) - 50;
            this.dummyRows.add(new DummyRow(skinIndex, pingMs));
        } else if (button.id == reset) {
            actxmiscdata.resetTabHeaderFooter();
            this.headerField.setText(denormalizeNewlines(denormalizeAC(actxmiscdata.getTabHeader())));
            this.footerField.setText(denormalizeNewlines(denormalizeAC(actxmiscdata.getTabFooter())));
        } else if (button.id == done) {
            String header = convertAC(processEscapes(this.headerField.getText()));
            String footer = convertAC(processEscapes(this.footerField.getText()));
            actxmiscdata.setTabHeader(header);
            actxmiscdata.setTabFooter(footer);
            net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController.setTabHeaderFooter(header, footer);
            this.mc.displayGuiScreen(this.parentScreen);
        } else if (button.id == cancel) {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 15, 16777215);
        String headerRaw = processEscapes(this.headerField.getText());
        String footerRaw = processEscapes(this.footerField.getText());
        String headerText = this.previewConvertColors ? convertAC(headerRaw) : headerRaw;
        String footerText = this.previewConvertColors ? convertAC(footerRaw) : footerRaw;
        this.previewOverlay.setHeader(actxmiscdata.parseLegacyTextToComponent(headerText));
        this.previewOverlay.setFooter(actxmiscdata.parseLegacyTextToComponent(footerText));
        List<GuiPlayerTabOverlay.PreviewEntry> previewEntries = new ArrayList<GuiPlayerTabOverlay.PreviewEntry>();
        int dummyCount = this.dummyRows.size();
        for (int i = 0; i < dummyCount; i++) {
            DummyRow dummy = this.dummyRows.get(i);
            // mmmmmmm
            ResourceLocation dummySkin = new ResourceLocation("eagler",
                    "skins/" + dummy_skins[dummy.skinIndex]);
            String label = dummyCount > 1 ? "Dummy " + (i + 1) : "Dummy";
            previewEntries.add(new GuiPlayerTabOverlay.PreviewEntry(label, dummySkin, true, Integer.valueOf(i),
                    Integer.valueOf(dummy.pingMs)));
        }
        this.previewOverlay.renderPreviewList(this.width, this.height, previewEntries);

        this.drawString(this.fontRendererObj, I18n.format("act.tabOptions.header") + ":",
                this.headerField.xPosition, this.headerField.yPosition - 10, 0xFFFFFF);
        this.headerField.drawTextBox();
        this.drawString(this.fontRendererObj, I18n.format("act.tabOptions.footer") + ":",
                this.footerField.xPosition, this.footerField.yPosition - 10, 0xFFFFFF);
        this.footerField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
        int previewColor = this.previewConvertColors ? 0x66FF00 : 0xFF0000;
        String previewLabel = I18n.format("act.tabOptions.preview") + ": "
                + (this.previewConvertColors ? I18n.format("act.tabOptions.on") : I18n.format("act.tabOptions.off"));
        this.drawCenteredString(this.fontRendererObj, previewLabel,
                this.previewToggleBtn.xPosition + this.previewToggleBtn.width / 2,
                this.previewToggleBtn.yPosition + (this.previewToggleBtn.height - 8) / 2,
                previewColor);
    }
    @Override
    public void updateScreen() {
        super.updateScreen();
        this.headerField.updateCursorCounter();
        this.footerField.updateCursorCounter();
    }
    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) { // ESC
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }
        if (keyCode == 200) { // UP arrow -- focus Header buffer
            this.editingHeader = true;
            this.headerField.setFocused(true);
            this.footerField.setFocused(false);
            return;
        }
        if (keyCode == 208) { // DOWN arrow -- focus Footer buffer
            this.editingHeader = false;
            this.headerField.setFocused(false);
            this.footerField.setFocused(true);
            return;
        }
        if (keyCode == 28 || keyCode == 156) { // ENTER / numpad ENTER -- insert a literal "\n" (newline escape) at the cursor
            (this.editingHeader ? this.headerField : this.footerField).writeText("\\n");
            return;
        }
        if (this.editingHeader) {
            if (this.headerField.textboxKeyTyped(typedChar, keyCode)) return;
        } else {
            if (this.footerField.textboxKeyTyped(typedChar, keyCode)) return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        Object clickedTag = this.previewOverlay.getRemovablePreviewRowAt(mouseX, mouseY);
        if (clickedTag instanceof Integer) {
            this.dummyRows.remove(((Integer) clickedTag).intValue()); // remove by position, not by value
            return;
        }
        this.headerField.mouseClicked(mouseX, mouseY, mouseButton);
        this.footerField.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.headerField.isFocused()) {
            this.editingHeader = true;
        } else if (this.footerField.isFocused()) {
            this.editingHeader = false;
        }
    }
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
    static String denormalizeNewlines(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\t') {
                sb.append("\\t");
            } else if (c < 0x20 || c == 0x7F) {
                sb.append('\\').append('u').append(String.format("%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    static String processEscapes(String text) {
        if (text == null) return null;
        StringBuilder sb = new StringBuilder(text.length());
        int i = 0;
        int len = text.length();
        while (i < len) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < len) {
                char next = text.charAt(i + 1);
                if (next == 'n') {
                    sb.append('\n');
                    i += 2;
                    continue;
                } else if (next == 't') {
                    sb.append('\t');
                    i += 2;
                    continue;
                } else if (next == '\\') {
                    sb.append('\\');
                    i += 2;
                    continue;
                } else if (next == 'u' && i + 5 < len && isHex4(text, i + 2)) {
                    sb.append((char) Integer.parseInt(text.substring(i + 2, i + 6), 16));
                    i += 6;
                    continue;
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private static boolean isHex4(String s, int start) {
        for (int i = start; i < start + 4; i++) {
            if (Character.digit(s.charAt(i), 16) < 0) return false;
        }
        return true;
    }
    static String convertAC(String text) {
        if (text == null) return null;
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&' && i + 1 < text.length()
                    && "0123456789abcdefklmnor".indexOf(Character.toLowerCase(text.charAt(i + 1))) >= 0) {
                sb.append('\u00a7');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    static String denormalizeAC(String text) {
        if (text == null) return null;
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00a7' && i + 1 < text.length()
                    && "0123456789abcdefklmnor".indexOf(Character.toLowerCase(text.charAt(i + 1))) >= 0) {
                sb.append('&');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}