package net.lax1dude.eaglercraft.v1_8.sp.gui;

import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.minecraft.EnumInputEvent;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.actx.data_o_algo.actxmiscdata;

import java.util.ArrayList;
import java.util.List;

public class GuiMiscLan extends GuiScreen {
    static void saveName(String name) {
        if (name == null || name.trim().isEmpty()) return;
        actxmiscdata.ensureLoaded();
        actxmiscdata.addSavedLanNameIfAbsent(name.trim());
    }

    private static final char[] color_num = {
        '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'
    };
    private static final String[] color_keys = {
        "black", "darkBlue", "darkGreen", "darkAqua",
        "darkRed", "darkPurple", "gold", "gray",
        "darkGray", "blue", "green", "aqua",
        "red", "lightPurple", "yellow", "white"
    };

    private static final String[] format_keyname = {
        "bold", "italic", "underline", "strikethrough", "obfuscated", "reset"
    };
    private static final String[] format_ins = { "&l","&o","&n","&m","&k","&r" };

    private static final String[][] all_symbols = {
        // pulled from my old linux machine for this also.
        {"2605","star filled black"},{"2606","star outline white"},
        {"2736","star six pointed"},{"2734","star eight pointed"},
        {"2726","star four pointed black diamond"},{"272A","star circled"},
        {"2729","star stress outlined"},{"2737","star eight pointed rectilinear"},
        {"2738","star heavy"},{"2739","star twelve pointed"},
        {"274B","asterisk heavy"},{"2733","star eight pinwheel"},
        {"2764","heart heavy red"},{"2665","heart suit black"},
        {"2661","heart suit white outline"},{"2666","diamond suit black"},
        {"2662","diamond suit white outline"},{"2663","club suit black"},
        {"2667","club suit white outline"},{"2660","spade suit black"},
        {"2664","spade suit white outline"},
        {"2623","biohazard hazard"},{"2622","radioactive hazard"},
        {"2620","skull crossbones death"},{"26A1","lightning bolt voltage"},
        {"26A0","warning caution"},{"2625","ankh cross"},
        {"2626","orthodox cross"},{"262D","hammer sickle"},
        {"262E","peace"},{"262F","yin yang"},
        {"26D4","no entry"},{"26B0","coffin"},{"26A2","double female"},
        {"26A3","double male"},
        {"2600","sun sunshine"},{"2601","cloud"},
        {"2602","umbrella rain"},{"2603","snowman winter"},
        {"2604","comet"},{"2614","umbrella drops rain"},
        {"2744","snowflake winter cold"},{"2745","snowflake tight"},
        {"26C4","snowman no snow"},
        {"266A","music note eighth"},{"266B","music notes beamed"},
        {"266C","music notes sixteenth"},{"266D","music flat"},
        {"266E","music natural"},{"266F","music sharp"},
        {"27A4","arrow right heavy"},{"2192","arrow right"},
        {"2190","arrow left"},{"2191","arrow up"},
        {"2193","arrow down"},{"21D2","double arrow right implies"},
        {"21E8","white arrow right"},{"2BC8","arrow right medium"},
        {"21A6","arrow right mapsto"},{"21C4","arrows right left"},
        {"21C6","arrows left right"},{"21D4","left right double"},
        {"2194","left right arrow"},{"2195","up down arrow"},
        {"21BA","counterclockwise"},{"21BB","clockwise"},
        {"21A9","return arrow"},{"2B05","arrow left black"},
        {"2B06","arrow up black"},{"2B07","arrow down black"},
        {"25CF","circle black"},{"25CB","circle white"},
        {"25C9","circle bullseye"},{"25CE","bullseye"},
        {"25A0","square black"},{"25A1","square white"},
        {"25AA","small square black"},{"25AB","small square white"},
        {"25B2","triangle up"},{"25BC","triangle down"},
        {"25B6","triangle right play"},{"25C0","triangle left"},
        {"25B3","triangle up outline"},{"25BD","triangle down outline"},
        {"29EB","diamond lozenge black"},{"25C6","diamond black"},
        {"25C7","diamond white"},{"2B22","hexagon black"},
        {"2B21","hexagon white"},{"2B1B","square large black"},
        {"2B1C","square large white"},{"2B25","diamond medium black"},
        {"2B26","diamond medium white"},
        {"2714","check mark heavy"},{"2716","x cross heavy"},
        {"271A","cross heavy plus"},{"2718","x ballot cross"},
        {"2713","check mark light"},{"2717","x ballot light"},
        {"2715","multiplication x"},
        {"2020","dagger cross"},{"2021","double dagger"},
        {"271D","latin cross"},{"2720","maltese cross"},
        {"2721","star of david"},{"2741","flower"},
        {"2611","checkbox checked"},{"2612","checkbox x"},
        {"2610","checkbox empty"},{"2B55","circle heavy large"},
        {"00B7","middle dot interpunct"},{"2022","bullet point"},
        {"2023","triangular bullet"},{"2024","one dot leader"},
        {"2025","two dot leader"},{"2026","ellipsis three dots"},
        {"00AB","left guillemet quote"},{"00BB","right guillemet quote"},
        {"2039","single left angle quote"},{"203A","single right angle quote"},
        {"2014","em dash"},{"2013","en dash"},
        {"00A6","broken bar"},{"203B","reference mark"},
        {"221E","infinity"},{"2248","almost equal approximately"},
        {"2260","not equal"},{"2264","less than equal"},
        {"2265","greater than equal"},{"00B1","plus minus"},
        {"00D7","multiplication times"},{"00F7","division"},
        {"221A","square root radical"},{"2211","sigma sum"},
        {"220F","pi product"},{"2206","increment delta"},
        {"2207","nabla gradient"},{"222B","integral"},
        {"2202","partial differential"},
        {"03C0","pi math"},{"00B2","squared superscript two"},
        {"00B3","cubed superscript three"},{"00B9","superscript one"},
        {"00BC","one quarter fraction"},{"00BD","one half fraction"},
        {"00BE","three quarters fraction"},
        {"00AE","registered trademark"},{"2122","trademark"},
        {"00A9","copyright"},{"00A7","section sign"},
        {"00B6","pilcrow paragraph"},{"2030","per mille"},
        {"20AC","euro currency"},{"00A3","pound sterling"},
        {"00A5","yen currency"},{"00A2","cent currency"},
        {"20B9","rupee currency"},{"00A4","generic currency"},
        {"03B1","alpha greek"},{"03B2","beta greek"},
        {"03B3","gamma greek"},{"03B4","delta greek"},
        {"03B5","epsilon greek"},{"03B8","theta greek"},
        {"03BB","lambda greek"},{"03BC","mu greek"},
        {"03C3","sigma greek"},{"03C4","tau greek"},
        {"03A9","omega greek capital"},{"03A3","sigma greek capital"},
        {"03A6","phi greek capital"},{"03A8","psi greek capital"},
        {"03A0","pi greek capital"},{"0394","delta greek capital"},
        {"039B","lambda greek capital"},{"0398","theta greek capital"},
        {"2588","solid block full"},{"2587","seven eighths block"},
        {"2593","dark shade block"},{"2592","medium shade block"},
        {"2591","light shade block"},{"2584","lower half block"},
        {"2580","upper half block"},{"258C","left half block"},
        {"2590","right half block"},{"25A4","square with fill"},
        {"2500","box drawing horizontal"},{"2502","box drawing vertical"},
        {"250C","box top left corner"},{"2510","box top right corner"},
        {"2514","box bottom left corner"},{"2518","box bottom right corner"},
        {"251C","box left tee"},{"2524","box right tee"},
        {"252C","box top tee"},{"2534","box bottom tee"},
        {"253C","box cross junction"},
        {"2550","double horizontal"},{"2551","double vertical"},
        {"2554","double top left"},{"2557","double top right"},
        {"255A","double bottom left"},{"255D","double bottom right"},
        {"263A","smiley face white"},{"263B","smiley face black"},
        {"2639","sad frown face"},{"261E","pointing right hand"},
        {"261C","pointing left hand"},{"261D","pointing up hand"},
        {"261F","pointing down hand"},{"270A","fist raised hand"},
        {"270B","hand raised"},{"270C","victory hand"},
        {"270D","writing hand"},{"270E","pencil"},
        {"2702","scissors"},{"2709","envelope"},
        {"2692","hammer pick"},{"2694","crossed swords"},
        {"269B","atom"},{"269C","fleur-de-lis"},
        {"00A0","no-break space invisible nbsp"},
        {"2009","thin space invisible"},
        {"200A","hair space invisible"},
        {"2002","en space invisible"},
        {"2003","em space invisible"},
        {"200B","zero width space invisible"},
    };

    private static final int SYMBOLS_PER_PAGE = 48;
    private static final int back         = 0;
    private static final int apply_back   = 1;
    private static final int save_name    = 2;
    private static final int saved_names  = 3;
    private static final int tab_colors   = 40;
    private static final int tab_format  = 41;
    private static final int tab_symbols  = 42;
    private static final int symbols_previous     = 43;
    private static final int symbols_next     = 44;
    private final GuiScreen parent;
    private final String    initialName;
    GuiTextField nameField;

    private GuiTextField   symSearchField;
    private int            activeTab   = 0;
    private int            symbolPage  = 0;
    private String[][]     filteredSymbols = all_symbols;
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
        buttonList.add(new GuiButton(tab_colors,
            tabStartX, tabY, tabW, 18, I18n.format("gui.act.misc.colors")));
        buttonList.add(new GuiButton(tab_format,
            tabStartX + tabW + tabGap, tabY, tabW, 18, I18n.format("gui.act.misc.formats")));
        buttonList.add(new GuiButton(tab_symbols,
            tabStartX + 2 * (tabW + tabGap), tabY, tabW, 18, I18n.format("gui.act.misc.symbols")));
        contentY = tabY + 26;
        buildTabContent();
        buttonList.add(new GuiButton(back, 4, 4, 60, 18, I18n.format("gui.ok.back1")));
        int botY = height - 28;
        buttonList.add(new GuiButton(apply_back, 4, botY, 90, 20, I18n.format("gui.act.misc.applyBack")));
        buttonList.add(new GuiButton(save_name, centerX - 45, botY, 90, 20, I18n.format("gui.act.misc.saveName")));
        GuiButton savedBtn = new GuiButton(saved_names,
                width - 132, botY, 128, 20, I18n.format("gui.act.misc.savedLanNames"));
        savedBtn.enabled = !actxmiscdata.getSavedLanNames().isEmpty();
        buttonList.add(savedBtn);
    }

    private void buildTabContent() {
        java.util.Iterator<GuiButton> it = buttonList.iterator();
        while (it.hasNext()) {
            int id = it.next().id;
            if ((id >= 10 && id < 36) || (id >= 50 && id < 98)
                    || id == symbols_previous || id == symbols_next) {
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
                String localizedLabel = "§" + color_num[i] + I18n.format("gui.act.color." + color_keys[i]);
                buttonList.add(new GuiButton(10 + i,
                    startX + col * (btnW + colGap),
                    startY + row * (btnH + rowGap),
                    btnW, btnH, localizedLabel));
            }
        } else if (activeTab == 1) {
            int btnW = 180, btnH = 18;
            int btnX = centerX - btnW / 2;
            int startY = contentY + 8;
            for (int i = 0; i < format_keyname.length; i++) {
                buttonList.add(new GuiButton(30 + i,
                    btnX, startY + i * (btnH + 6), btnW, btnH, I18n.format("gui.act.format." + format_keyname[i])));
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
                    symBtnW, symBtnH, glyphFor(filteredSymbols[i][0])));
            }
            int navY = gridTop + 6 * (symBtnH + symRowGap) + 6;
            buttonList.add(new GuiButton(symbols_previous, centerX - 60, navY, 56, 16, "<-"));
            buttonList.add(new GuiButton(symbols_next, centerX +  4, navY, 56, 16, "->"));
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
        drawCenteredString(fontRendererObj, "§6" + I18n.format("itemGroup.misc"), width / 2, 12, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
            I18n.format("gui.act.misc.lanName") + "  §7(" + I18n.format("gui.act.misc.lanNameHint") + ")", width / 2, 38, 0xAAAAAA);
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
            .replaceAll("&([0-9a-fA-FlLoOnNkKmMrR])", "§$1");
        drawCenteredString(fontRendererObj, "§7" + I18n.format("gui.act.misc.preview") + ": §r" + preview,
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
                symTooltip = "U+" + filteredSymbols[i][0] + "  " + filteredSymbols[i][1];
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
            case back:
                mc.displayGuiScreen(parent);
                break;
            case apply_back:
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
                    .replaceAll("&([0-9a-fA-FlLoOnNkKmMrR])", "§$1");
                GuiShareToLan.pendingLanName = formatted;
                mc.displayGuiScreen(parent);
                break;
            case save_name: {
                String name = nameField.getText().trim();
                if (!name.isEmpty()) {
                    saveName(name);
                    for (GuiButton b : buttonList) {
                        if (b.id == saved_names) { b.enabled = true; break; }
                    }
                }
                break;
            }
            case saved_names:
                mc.displayGuiScreen(new GuiSavedLanNames(this));
                break;
            case tab_colors:  activeTab = 0; symbolPage = 0; initGui(); break;
            case tab_format: activeTab = 1; symbolPage = 0; initGui(); break;
            case tab_symbols: activeTab = 2; symbolPage = 0; initGui(); break;
            case symbols_previous:
                if (symbolPage > 0) { symbolPage--; buildTabContent(); }
                break;
            case symbols_next: {
                int total = (filteredSymbols.length + SYMBOLS_PER_PAGE - 1) / SYMBOLS_PER_PAGE;
                if (symbolPage < total - 1) { symbolPage++; buildTabContent(); }
                break;
            }
            default:
                if (btn.id >= 10 && btn.id < 26) {
                    insertAtCursor("&" + color_num[btn.id - 10]);
                } else if (btn.id >= 30 && btn.id < 36) {
                    insertAtCursor(format_ins[btn.id - 30]);
                } else if (btn.id >= 50 && btn.id < 98) {
                    int idx = symbolPage * SYMBOLS_PER_PAGE + (btn.id - 50);
                    if (idx < filteredSymbols.length) {
                        insertAtCursor(glyphFor(filteredSymbols[idx][0]));
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
            filteredSymbols = all_symbols;
        } else {
            List<String[]> res = new ArrayList<String[]>();
            for (String[] sym : all_symbols) {
                if (sym[0].toLowerCase().contains(q) || sym[1].toLowerCase().contains(q)) {
                    res.add(sym);
                }
            }
            filteredSymbols = res.toArray(new String[0][]);
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