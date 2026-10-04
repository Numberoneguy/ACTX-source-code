package net.lax1dude.eaglercraft.v1_8.sp.gui;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.minecraft.EnumInputEvent;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.actx.data_o_algo.actxmiscdata;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GuiMiscLan extends GuiScreen {
    private static final String LEGACY_STORAGE_KEY = "actx_saved_lan_names";
    private static boolean legacyMigrated = false;

    static void saveName(String name) {
        if (name == null || name.trim().isEmpty()) return;
        actxmiscdata.ensureLoaded();
        migrateLegacyStorageIfNeeded();
        actxmiscdata.addSavedLanNameIfAbsent(name.trim());
    }

    private static void migrateLegacyStorageIfNeeded() {
        if (legacyMigrated) return;
        legacyMigrated = true;
        try {
            byte[] raw = EagRuntime.getStorage(LEGACY_STORAGE_KEY);
            if (raw == null || raw.length == 0) return;
            String s = new String(raw, StandardCharsets.UTF_8).trim();
            if (s.isEmpty()) return;

            JSONObject root = new JSONObject(s);
            JSONArray arr = root.optJSONArray("names");
            if (arr == null) return;

            List<String> legacyNames = new ArrayList<String>();
            for (int i = 0; i < arr.length(); i++) {
                String n = arr.optString(i, null);
                if (n != null && !n.trim().isEmpty()) legacyNames.add(n.trim());
            }

            if (!legacyNames.isEmpty()) {
                actxmiscdata.migrateLegacySavedLanNames(legacyNames);
            }

            EagRuntime.setStorage(LEGACY_STORAGE_KEY, new byte[0]);
        } catch (Exception e) {
            legacyMigrated = false;
        }
    }

    private static final char[] COLOR_CHARS = {
        '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'
    };
    private static final String[] COLOR_KEYS = {
        "black", "darkBlue", "darkGreen", "darkAqua",
        "darkRed", "darkPurple", "gold", "gray",
        "darkGray", "blue", "green", "aqua",
        "red", "lightPurple", "yellow", "white"
    };

    private static final String[] FORMAT_KEYS = {
        "bold", "italic", "underline", "strikethrough", "obfuscated", "reset"
    };
    private static final String[] FORMAT_INSERT = { "&l","&o","&n","&m","&k","&r" };

    private static final String[] SYMBOLS = {
        "2605", "2606", "2764", "2665", "2666", "2663", "2660", "2734", "2733",
        "2729", "272A", "2737", "2714", "2716", "271A", "2718", "2713", "2715",
        "2611", "2612", "2610", "25CF", "25CB", "25CE", "25A0", "25A1", "25AA",
        "25AB", "25C6", "25C7", "25B2", "25BC", "25B6", "25C0", "25B3", "25BD",
        "2192", "2190", "2191", "2193", "21D2", "21D4", "21BA", "21BB", "21A9",
        "2B05", "2B06", "2B07", "266A", "266B", "266C", "2600", "2601", "2602",
        "2744", "2603", "2604", "26A1", "1F319", "263A", "2639", "270A", "270B",
        "270C", "270D", "261E", "261F", "261C", "261D", "270E", "2709", "2702",
        "2692", "2694", "26A0", "2620", "262E", "2622", "2623", "26D4", "26B0",
        "269B", "269C", "2721", "271D", "262F", "00B7", "2022", "2026", "00AB",
        "00BB", "2039", "203A", "2014", "2013", "00A6", "00B6", "2020", "2021",
        "203B", "00A7", "221E", "00B1", "00D7", "00F7", "221A", "2211", "220F",
        "2206", "2207", "222B", "2248", "2260", "2264", "2265", "2202", "00BC",
        "00BD", "00BE", "00B9", "00B2", "00B3", "03B1", "03B2", "03B3", "03B4",
        "03B5", "03B8", "03BB", "03BC", "03C0", "03C3", "03C4", "03A9", "00A2",
        "00A3", "00A5", "20AC", "00A4", "20B9", "2588", "2587", "2593", "2592",
        "2591", "2584", "2580", "258C", "2590", "2500", "2502", "250C", "2510",
        "2514", "2518", "251C", "2524", "252C", "2534", "253C", "2550", "2551",
        "2554", "2557", "255A", "255D", "25A4", "00AE", "2122", "00A9", "2B55",
        "29EB", "2B22", "2741", "2726", "2727", "00B0", "2103", "2109", "231A",
        "231B", "2328", "26A2", "26A3"
    };

    private static final int SYMBOLS_PER_PAGE = 48;
    private static final int BTN_BACK         = 0;
    private static final int BTN_APPLY_BACK   = 1;
    private static final int BTN_SAVE_NAME    = 2;
    private static final int BTN_SAVED_NAMES  = 3;
    private static final int BTN_TAB_COLORS   = 40;
    private static final int BTN_TAB_FORMATS  = 41;
    private static final int BTN_TAB_SYMBOLS  = 42;
    private static final int BTN_SYM_PREV     = 43;
    private static final int BTN_SYM_NEXT     = 44;
    private final GuiScreen parent;
    private final String    initialName;
    GuiTextField nameField;

    private GuiTextField   symSearchField;
    private int            activeTab   = 0;
    private int            symbolPage  = 0;
    private String[]       filteredSymbols = SYMBOLS;
    private String         lastSymSearch   = "";
    private int centerX, fieldX, fieldY, tabY, contentY;
    private int symStartX, gridTop;

    private String symTooltip = "";
    private int    tipX, tipY;
    
    public GuiMiscLan(GuiScreen parent, String initialName) {
        this.parent      = parent;
        this.initialName = initialName != null ? initialName : "";
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        actxmiscdata.ensureLoaded();
        migrateLegacyStorageIfNeeded();
        buttonList.clear();

        centerX = width / 2;

        fieldY = 50;
        int fieldW = Math.min(300, width - 40);
        fieldX = centerX - fieldW / 2;
        String savedText = (nameField != null) ? nameField.getText() : initialName;
        nameField = new GuiTextField(0, fontRendererObj, fieldX, fieldY, fieldW, 20);
        nameField.setMaxStringLength(Integer.MAX_VALUE);
        nameField.setText(savedText);
        nameField.setFocused(activeTab != 2);
        tabY = fieldY + 28;
        int tabW = 64, tabGap = 6;
        int totalTabW = 3 * tabW + 2 * tabGap;
        int tabStartX = centerX - totalTabW / 2;
        buttonList.add(new GuiButton(BTN_TAB_COLORS,
            tabStartX, tabY, tabW, 18, I18n.format("gui.act.misc.colors")));
        buttonList.add(new GuiButton(BTN_TAB_FORMATS,
            tabStartX + tabW + tabGap, tabY, tabW, 18, I18n.format("gui.act.misc.formats")));
        buttonList.add(new GuiButton(BTN_TAB_SYMBOLS,
            tabStartX + 2 * (tabW + tabGap), tabY, tabW, 18, I18n.format("gui.act.misc.symbols")));

        contentY = tabY + 26;

        buildTabContent();
        buttonList.add(new GuiButton(BTN_BACK, 4, 4, 60, 18, I18n.format("gui.ok.back1")));

        int botY = height - 28;
        buttonList.add(new GuiButton(BTN_APPLY_BACK, 4, botY, 90, 20, I18n.format("gui.act.misc.applyBack")));
        buttonList.add(new GuiButton(BTN_SAVE_NAME, centerX - 45, botY, 90, 20, I18n.format("gui.act.misc.saveName")));
        GuiButton savedBtn = new GuiButton(BTN_SAVED_NAMES,
                width - 132, botY, 128, 20, I18n.format("gui.act.misc.savedLanNames"));
        savedBtn.enabled = !actxmiscdata.getSavedLanNames().isEmpty();
        buttonList.add(savedBtn);
    }

    private void buildTabContent() {
        java.util.Iterator<GuiButton> it = buttonList.iterator();
        while (it.hasNext()) {
            int id = it.next().id;
            if ((id >= 10 && id < 36) || (id >= 50 && id < 98)
                    || id == BTN_SYM_PREV || id == BTN_SYM_NEXT) {
                it.remove();
            }
        }

        if (activeTab == 0) {
            int btnW = 68, btnH = 20, colGap = 4, rowGap = 6;
            int gridW = 4 * btnW + 3 * colGap;
            int startX = centerX - gridW / 2;
            int startY = contentY + 10;
            for (int i = 0; i < 16; i++) {
                int col = i % 4, row = i / 4;
                String localizedLabel = "\u00a7" + COLOR_CHARS[i] + I18n.format("gui.act.color." + COLOR_KEYS[i]);
                buttonList.add(new GuiButton(10 + i,
                    startX + col * (btnW + colGap),
                    startY + row * (btnH + rowGap),
                    btnW, btnH, localizedLabel));
            }

        } else if (activeTab == 1) {
            int btnW = 180, btnH = 18;
            int btnX = centerX - btnW / 2;
            int startY = contentY + 8;
            for (int i = 0; i < FORMAT_KEYS.length; i++) {
                buttonList.add(new GuiButton(30 + i,
                    btnX, startY + i * (btnH + 6), btnW, btnH, I18n.format("gui.act.format." + FORMAT_KEYS[i])));
            }

        } else {
            String savedSearch = (symSearchField != null) ? symSearchField.getText() : "";
            int sfW = 180, sfX = centerX - sfW / 2;
            symSearchField = new GuiTextField(99, fontRendererObj, sfX, contentY, sfW, 16);
            symSearchField.setMaxStringLength(Integer.MAX_VALUE);
            symSearchField.setText(savedSearch);
            symSearchField.setFocused(true);
            applySymFilter();

            int symBtnW = 24, symBtnH = 18, symColGap = 2, symRowGap = 4, symCols = 8;
            int symGridW = symCols * symBtnW + (symCols - 1) * symColGap;
            symStartX = centerX - symGridW / 2;
            gridTop   = contentY + 24;

            int start = symbolPage * SYMBOLS_PER_PAGE;
            int end   = Math.min(start + SYMBOLS_PER_PAGE, filteredSymbols.length);
            for (int i = start; i < end; i++) {
                int idx = i - start;
                int col = idx % symCols, row = idx / symCols;
                buttonList.add(new GuiButton(50 + idx,
                    symStartX + col * (symBtnW + symColGap),
                    gridTop   + row * (symBtnH + symRowGap),
                    symBtnW, symBtnH, glyphFor(filteredSymbols[i])));
            }

            int navY = gridTop + 6 * (symBtnH + symRowGap) + 6;
            buttonList.add(new GuiButton(BTN_SYM_PREV, centerX - 60, navY, 56, 16, "<-"));
            buttonList.add(new GuiButton(BTN_SYM_NEXT, centerX +  4, navY, 56, 16, "->"));
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        nameField.updateCursorCounter();
        if (symSearchField != null) symSearchField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        drawDefaultBackground();

        drawCenteredString(fontRendererObj, "\u00a76" + I18n.format("itemGroup.misc"), width / 2, 12, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
            I18n.format("gui.act.misc.lanName") + "  \u00a77(" + I18n.format("gui.act.misc.lanNameHint") + ")", width / 2, 38, 0xAAAAAA);

        int tabW = 64, tabGap = 6;
        int totalTabW = 3 * tabW + 2 * tabGap;
        int tabStartX = centerX - totalTabW / 2;
        int[] tabXs = {
            tabStartX,
            tabStartX + tabW + tabGap,
            tabStartX + 2 * (tabW + tabGap)
        };
        drawRect(tabXs[activeTab], tabY + 17, tabXs[activeTab] + tabW, tabY + 19, 0xFFFFFFAA);
        if (activeTab == 0) {
            drawCenteredString(fontRendererObj, I18n.format("gui.act.misc.insertColor") + ":", centerX, contentY, 0xAAAAAA);
        } else if (activeTab == 1) {
            drawCenteredString(fontRendererObj, I18n.format("gui.act.misc.insertFormat") + ":", centerX, contentY, 0xAAAAAA);
        } else {
            if (symSearchField != null) symSearchField.drawTextBox();
            int totalPages = Math.max(1,
                (filteredSymbols.length + SYMBOLS_PER_PAGE - 1) / SYMBOLS_PER_PAGE);
            drawCenteredString(fontRendererObj,
                I18n.format("gui.act.misc.page", symbolPage + 1, totalPages, filteredSymbols.length),
                centerX, height - 62, 0x888888);
        }

        nameField.drawTextBox();

        String raw     = nameField.getText();
        StringBuffer motdBuf = new StringBuffer();
        java.util.regex.Matcher motdM =
            java.util.regex.Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(raw);
        while (motdM.find()) {
            try {
                int cp = Integer.parseInt(motdM.group(1), 16);
                motdM.appendReplacement(motdBuf,
                    java.util.regex.Matcher.quoteReplacement(
                        new String(Character.toChars(cp))));
            } catch (Exception e) {
                motdM.appendReplacement(motdBuf, motdM.group(0));
            }
        }
        motdM.appendTail(motdBuf);
        String preview = motdBuf.toString()
            .replaceAll("&([0-9a-fA-FlLoOnNkKmMrR])", "\u00a7$1");
        drawCenteredString(fontRendererObj, "\u00a77" + I18n.format("gui.act.misc.preview") + ": \u00a7r" + preview,
            width / 2, height - 48, 0xFFFFFF);

        super.drawScreen(mx, my, partialTicks);
        symTooltip = "";
        if (activeTab == 2) computeSymTip(mx, my);
        if (!symTooltip.isEmpty()) {
            int tipW = fontRendererObj.getStringWidth(symTooltip) + 8;
            int tipX2 = Math.min(tipX + 10, width - tipW - 2);
            int tipY2 = Math.max(tipY - 16, 2);
            drawRect(tipX2 - 2, tipY2 - 2, tipX2 + tipW + 2, tipY2 + 11, 0xFF111111);
            drawRect(tipX2 - 1, tipY2 - 1, tipX2 + tipW + 1, tipY2 + 10, 0xFF333333);
            drawString(fontRendererObj, symTooltip, tipX2 + 2, tipY2 + 1, 0xFFFFFF);
        }
    }

    @Override
    public void handleMouseInput() throws java.io.IOException {
        super.handleMouseInput();
    }

    private void computeSymTip(int mx, int my) {
        symTooltip = "";
        int symBtnW = 24, symBtnH = 18, symColGap = 2, symRowGap = 4, symCols = 8;
        int start = symbolPage * SYMBOLS_PER_PAGE;
        int end   = Math.min(start + SYMBOLS_PER_PAGE, filteredSymbols.length);
        for (int i = start; i < end; i++) {
            int idx = i - start;
            int col = idx % symCols, row = idx / symCols;
            int bx = symStartX + col * (symBtnW + symColGap);
            int by = gridTop   + row * (symBtnH + symRowGap);
            if (mx >= bx && mx < bx + symBtnW && my >= by && my < by + symBtnH) {
                symTooltip = "U+" + filteredSymbols[i] + "  " + I18n.format("gui.act.sym." + filteredSymbols[i]);
                tipX = mx; tipY = my;
                return;
            }
        }
    }

    @Override
    protected void keyTyped(char c, int k) {
        if (activeTab == 2 && symSearchField != null && symSearchField.isFocused()) {
            symSearchField.textboxKeyTyped(c, k);
            applySymFilter();
            buildTabContent();
        } else {
            nameField.textboxKeyTyped(c, k);
        }
        if (k == 1) mc.displayGuiScreen(parent);
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        nameField.mouseClicked(mx, my, button);
        if (activeTab == 2 && symSearchField != null) {
            symSearchField.mouseClicked(mx, my, button);
        }
        super.mouseClicked(mx, my, button);
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        switch (btn.id) {

            case BTN_BACK:
                mc.displayGuiScreen(parent);
                break;

            case BTN_APPLY_BACK:
                String raw = nameField.getText().trim();
                StringBuffer applyBuf = new StringBuffer();
                java.util.regex.Matcher applyM =
                    java.util.regex.Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(raw);
                while (applyM.find()) {
                    try {
                        int cp = Integer.parseInt(applyM.group(1), 16);
                        applyM.appendReplacement(applyBuf,
                            java.util.regex.Matcher.quoteReplacement(
                                new String(Character.toChars(cp))));
                    } catch (Exception e) {
                        applyM.appendReplacement(applyBuf, applyM.group(0));
                    }
                }
                applyM.appendTail(applyBuf);
                String formatted = applyBuf.toString()
                    .replaceAll("&([0-9a-fA-FlLoOnNkKmMrR])", "\u00a7$1");
                GuiShareToLan.pendingLanName = formatted;
                mc.displayGuiScreen(parent);
                break;

            case BTN_SAVE_NAME: {
                String name = nameField.getText().trim();
                if (!name.isEmpty()) {
                    saveName(name);
                    for (GuiButton b : buttonList) {
                        if (b.id == BTN_SAVED_NAMES) { b.enabled = true; break; }
                    }
                }
                break;
            }

            case BTN_SAVED_NAMES:
                mc.displayGuiScreen(new GuiSavedLanNames(this));
                break;

            case BTN_TAB_COLORS:  activeTab = 0; symbolPage = 0; initGui(); break;
            case BTN_TAB_FORMATS: activeTab = 1; symbolPage = 0; initGui(); break;
            case BTN_TAB_SYMBOLS: activeTab = 2; symbolPage = 0; initGui(); break;

            case BTN_SYM_PREV:
                if (symbolPage > 0) { symbolPage--; buildTabContent(); }
                break;

            case BTN_SYM_NEXT: {
                int total = (filteredSymbols.length + SYMBOLS_PER_PAGE - 1) / SYMBOLS_PER_PAGE;
                if (symbolPage < total - 1) { symbolPage++; buildTabContent(); }
                break;
            }

            default:
                if (btn.id >= 10 && btn.id < 26) {
                    insertAtCursor("&" + COLOR_CHARS[btn.id - 10]);
                } else if (btn.id >= 30 && btn.id < 36) {
                    insertAtCursor(FORMAT_INSERT[btn.id - 30]);
                } else if (btn.id >= 50 && btn.id < 98) {
                    int idx = symbolPage * SYMBOLS_PER_PAGE + (btn.id - 50);
                    if (idx < filteredSymbols.length) {
                        insertAtCursor(glyphFor(filteredSymbols[idx]));
                    }
                }
                break;
        }
    }

    private void insertAtCursor(String s) {
        String cur = nameField.getText();
        int pos    = nameField.getCursorPosition();
        nameField.setText(cur.substring(0, pos) + s + cur.substring(pos));
        nameField.setCursorPosition(pos + s.length());
    }

    private void applySymFilter() {
        String q = (symSearchField != null)
                ? symSearchField.getText().trim().toLowerCase() : "";
        if (q.equals(lastSymSearch)) return;
        lastSymSearch = q;
        if (q.isEmpty()) {
            filteredSymbols = SYMBOLS;
        } else {
            List<String> res = new ArrayList<>();
            for (String hex : SYMBOLS) {
                String translated = I18n.format("gui.act.sym." + hex).toLowerCase();
                if (hex.toLowerCase().contains(q) || translated.contains(q)) {
                    res.add(hex);
                }
            }
            filteredSymbols = res.toArray(new String[0]);
        }
        symbolPage = 0;
    }

    private String glyphFor(String hex) {
        try {
            return new String(Character.toChars(Integer.parseInt(hex, 16)));
        } catch (Exception e) {
            return "?";
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return true; }

    @Override
    public boolean showCopyPasteButtons() { return nameField.isFocused(); }

    @Override
    public void fireInputEvent(EnumInputEvent event, String param) {
        nameField.fireInputEvent(event, param);
    }
}