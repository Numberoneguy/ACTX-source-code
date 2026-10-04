package net.lax1dude.eaglercraft.v1_8.profile;

import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.minecraft.EnumInputEvent;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

public class GuiScreenAdvancedProfile extends GuiScreen {

    private final GuiScreen parent;

    private GuiTextField advancedNameField;
    private GuiTextField symbolSearchField;
    private String previewName  = "";
    private String lastRawText  = "";
    private String symbolTooltip = "";
    private int    tooltipX = 0, tooltipY = 0;

    private int activeTab  = 0;
    private int symbolPage = 0;
    private static final int SYMBOLS_PER_PAGE = 48;
    
    private static final char[] COLOR_CHARS = {
        '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'
    };
    
    private static final String[] COLOR_KEYS = {
        "black", "darkBlue", "darkGreen", "darkAqua",
        "darkRed", "darkPurple", "gold", "gray",
        "darkGray", "blue", "green", "aqua",
        "red", "lightPurple", "yellow", "white"
    };

    private static final char[]   FORMAT_CHARS  = {'l','o','n','m','k','r'};
    
    private static final String[] FORMAT_KEYS = {
        "bold", "italic", "underline", "strikethrough", "obfuscated", "reset"
    };
    
    private static final String[] FORMAT_INSERT = {"&l","&o","&n","&m","&k","&r"};
    private static final String[][] ALL_SYMBOLS = {
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

    private String[][] filteredSymbols = ALL_SYMBOLS;
    private String     lastSearchQuery  = "";

    private static final int BTN_TAB_COLORS  = 40;
    private static final int BTN_TAB_FORMAT  = 41;
    private static final int BTN_TAB_SYMBOLS = 42;
    private static final int BTN_SYM_PREV    = 43;
    private static final int BTN_SYM_NEXT    = 44;

    private int leftCenterX, fieldX, fieldY, tabY, contentY;
    private int symStartX, gridTop, symNavY;

    public GuiScreenAdvancedProfile(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();

        leftCenterX = width / 4;

        fieldY     = 46;
        int fieldW = 200;
        fieldX     = leftCenterX - fieldW / 2;

        String savedText = advancedNameField != null ? advancedNameField.getText()
                : EaglerProfile.getName().replace("\u00a7", "&");
        advancedNameField = new GuiTextField(0, fontRendererObj, fieldX, fieldY, fieldW, 20);
        advancedNameField.setFocused(activeTab != 2);
        advancedNameField.setMaxStringLength(64);
        advancedNameField.setText(savedText);

        buttonList.add(new GuiButton(1, fieldX + fieldW + 5, fieldY, 40, 20, I18n.format("advancedProfile.clear")));

        tabY = fieldY + 28;
        int tabW = 64, tabGap = 6;
        int totalTabW = 3 * tabW + 2 * tabGap;
        int tabStartX = leftCenterX - totalTabW / 2;
        buttonList.add(new GuiButton(BTN_TAB_COLORS,  tabStartX,                   tabY, tabW, 18, I18n.format("advancedProfile.tab.colors")));
        buttonList.add(new GuiButton(BTN_TAB_FORMAT,  tabStartX + tabW + tabGap,   tabY, tabW, 18, I18n.format("advancedProfile.tab.format")));
        buttonList.add(new GuiButton(BTN_TAB_SYMBOLS, tabStartX + 2*(tabW+tabGap), tabY, tabW, 18, I18n.format("advancedProfile.tab.symbols")));

        contentY = tabY + 26;

        buildTabContent();
        buttonList.add(new GuiButton(2, width / 2 - 102, height - 44, 100, 20, I18n.format("advancedProfile.applySave")));
        buttonList.add(new GuiButton(0, width / 2 + 2,   height - 44, 100, 20, I18n.format("gui.done")));

        updatePreview();
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
            int btnW = 64, btnH = 20, colGap = 4, rowGap = 6;
            int gridW = 4 * btnW + 3 * colGap;
            int startX = leftCenterX - gridW / 2;
            int startY = contentY + 10;
            for (int i = 0; i < 16; i++) {
                int col = i % 4, row = i / 4;
                int bx = startX + col * (btnW + colGap);
                int by = startY + row * (btnH + rowGap);
                buttonList.add(new GuiButton(10 + i, bx, by, btnW, btnH, I18n.format("gui.act.color." + COLOR_KEYS[i])));
            }

        } else if (activeTab == 1) {
            int btnW = 180, btnH = 18;
            int btnX = leftCenterX - btnW / 2;
            int startY = contentY + 8;
            for (int i = 0; i < FORMAT_KEYS.length; i++) {
                buttonList.add(new GuiButton(30 + i, btnX, startY + i * (btnH + 6), btnW, btnH, I18n.format("advancedProfile.format." + FORMAT_KEYS[i])));
            }

        } else {
            String savedSearch = symbolSearchField != null ? symbolSearchField.getText() : "";
            int sfW = 180, sfX = leftCenterX - sfW / 2;
            symbolSearchField = new GuiTextField(99, fontRendererObj, sfX, contentY, sfW, 16);
            symbolSearchField.setMaxStringLength(40);
            symbolSearchField.setText(savedSearch);
            symbolSearchField.setFocused(true);
            applySymbolFilter();

            int symBtnW = 24, symBtnH = 18, symColGap = 2, symRowGap = 4;
            int symCols = 8;
            int symGridW = symCols * symBtnW + (symCols - 1) * symColGap;
            symStartX = leftCenterX - symGridW / 2;
            gridTop = contentY + 24;

            int start = symbolPage * SYMBOLS_PER_PAGE;
            int end   = Math.min(start + SYMBOLS_PER_PAGE, filteredSymbols.length);
            for (int i = start; i < end; i++) {
                int idx = i - start;
                int col = idx % symCols, row = idx / symCols;
                int bx = symStartX + col * (symBtnW + symColGap);
                int by = gridTop   + row * (symBtnH + symRowGap);
                String glyph = glyphFor(filteredSymbols[i][0]);
                buttonList.add(new GuiButton(50 + idx, bx, by, symBtnW, symBtnH, glyph));
            }

            symNavY = gridTop + 6 * (symBtnH + symRowGap) + 6;
            buttonList.add(new GuiButton(BTN_SYM_PREV, leftCenterX - 58, symNavY, 54, 16, I18n.format("advancedProfile.prev")));
            buttonList.add(new GuiButton(BTN_SYM_NEXT, leftCenterX +  4, symNavY, 54, 16, I18n.format("advancedProfile.next")));
        }
    }

    private String glyphFor(String hex) {
        try {
            return new String(Character.toChars(Integer.parseInt(hex, 16)));
        } catch (Exception e) {
            return "?";
        }
    }

    private void applySymbolFilter() {
        String query = symbolSearchField != null ? symbolSearchField.getText().trim().toLowerCase() : "";
        if (query.equals(lastSearchQuery)) return;
        lastSearchQuery = query;
        if (query.isEmpty()) {
            filteredSymbols = ALL_SYMBOLS;
        } else {
            java.util.List<String[]> result = new java.util.ArrayList<String[]>();
            for (String[] sym : ALL_SYMBOLS) {
                if (sym[0].toLowerCase().contains(query) || sym[1].toLowerCase().contains(query)) {
                    result.add(sym);
                }
            }
            filteredSymbols = result.toArray(new String[0][]);
        }
        symbolPage = 0;
    }

    private void updatePreview() {
        String raw = advancedNameField.getText();
        String converted = raw.replaceAll("&([0-9a-fA-FlLoOnNkKmMrR])", "\u00a7$1");
        previewName = trimToVisibleLength(converted, 16);
        lastRawText = raw;
    }

    private String trimToVisibleLength(String s, int maxVisible) {
        if (s.length() <= maxVisible) return s;
        return s.substring(0, maxVisible);
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        drawDefaultBackground();

        int rightCenterX = width / 4 * 3;

        drawCenteredString(fontRendererObj, I18n.format("advancedProfile.title"), width / 2, 14, 0xFFFFFF);

        drawCenteredString(fontRendererObj,
                I18n.format("advancedProfile.usernameDesc"),
                leftCenterX, 32, 0xAAAAAA);

        int tabW = 64, tabGap = 6;
        int totalTabW = 3 * tabW + 2 * tabGap;
        int tabStartX = leftCenterX - totalTabW / 2;
        int[] tabXs = {tabStartX, tabStartX + tabW + tabGap, tabStartX + 2*(tabW+tabGap)};
        drawRect(tabXs[activeTab], tabY + 17, tabXs[activeTab] + tabW, tabY + 19, 0xFFFFFFAA);
        
        if (activeTab == 0) {
            drawCenteredString(fontRendererObj, I18n.format("advancedProfile.insertColor"), leftCenterX, contentY, 0xAAAAAA);
        } else if (activeTab == 1) {
            drawCenteredString(fontRendererObj, I18n.format("advancedProfile.insertFormat"), leftCenterX, contentY, 0xAAAAAA);
        } else {
            int sfX = leftCenterX - 90;
            String searchStr = I18n.format("advancedProfile.search");
            drawString(fontRendererObj, searchStr, sfX - fontRendererObj.getStringWidth(searchStr) - 3, contentY + 4, 0xAAAAAA);
            if (symbolSearchField != null) symbolSearchField.drawTextBox();
            int totalPages = Math.max(1, (filteredSymbols.length + SYMBOLS_PER_PAGE - 1) / SYMBOLS_PER_PAGE);
            String pageStr = I18n.format("advancedProfile.page", symbolPage + 1, totalPages, filteredSymbols.length);
            drawCenteredString(fontRendererObj, pageStr, leftCenterX, symNavY + 20, 0x888888);
        }

        advancedNameField.drawTextBox();
        super.drawScreen(mx, my, partialTicks);

        drawRect(width / 2 - 1, 28, width / 2, height - 52, 0x44FFFFFF);

        drawCenteredString(fontRendererObj, I18n.format("advancedProfile.preview"), rightCenterX, 34, 0xAAAAAA);

        int previewY = 52;
        if (!previewName.isEmpty()) {
            int pw = fontRendererObj.getStringWidth(previewName);
            fontRendererObj.drawStringWithShadow(previewName, rightCenterX - pw / 2, previewY, 0xFFFFFF);
        } else {
            drawCenteredString(fontRendererObj, I18n.format("advancedProfile.empty"), rightCenterX, previewY, 0x555555);
        }

        int vis = countVisibleChars(previewName);
        drawCenteredString(fontRendererObj,
                I18n.format("advancedProfile.totalChars", vis), rightCenterX, previewY + 18,
                vis >= 16 ? 0xFF5555 : 0x55FF55);

        String rawDisp = advancedNameField.getText();
        if (rawDisp.length() > 22) rawDisp = rawDisp.substring(0, 22) + "...";
        drawCenteredString(fontRendererObj, I18n.format("advancedProfile.raw", rawDisp), rightCenterX, previewY + 34, 0x777777);

        symbolTooltip = "";
        if (activeTab == 2) computeSymbolTooltip(mx, my);
        if (!symbolTooltip.isEmpty()) {
            int tipW = fontRendererObj.getStringWidth(symbolTooltip) + 8;
            int tipX = Math.min(tooltipX + 10, width - tipW - 2);
            int tipY = Math.max(tooltipY - 16, 2);
            drawRect(tipX - 2, tipY - 2, tipX + tipW + 2, tipY + 11, 0xFF111111);
            drawRect(tipX - 1, tipY - 1, tipX + tipW + 1, tipY + 10, 0xFF333333);
            drawString(fontRendererObj, symbolTooltip, tipX + 2, tipY + 1, 0xFFFFFF);
        }
    }

    private void computeSymbolTooltip(int mx, int my) {
        symbolTooltip = "";
        int symBtnW = 24, symBtnH = 18, symColGap = 2, symRowGap = 4, symCols = 8;
        int start = symbolPage * SYMBOLS_PER_PAGE;
        int end   = Math.min(start + SYMBOLS_PER_PAGE, filteredSymbols.length);
        for (int i = start; i < end; i++) {
            int idx = i - start;
            int col = idx % symCols, row = idx / symCols;
            int bx = symStartX + col * (symBtnW + symColGap);
            int by = gridTop   + row * (symBtnH + symRowGap);
            if (mx >= bx && mx < bx + symBtnW && my >= by && my < by + symBtnH) {
                symbolTooltip = "U+" + filteredSymbols[i][0] + "  " + filteredSymbols[i][1];
                tooltipX = mx;
                tooltipY = my;
                return;
            }
        }
    }

    private int countVisibleChars(String s) {
        return s.length();
    }

    @Override
    protected void keyTyped(char c, int k) {
        if (activeTab == 2 && symbolSearchField != null && symbolSearchField.isFocused()) {
            symbolSearchField.textboxKeyTyped(c, k);
            applySymbolFilter();
            buildTabContent();
        } else {
            advancedNameField.textboxKeyTyped(c, k);
            updatePreview();
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        advancedNameField.mouseClicked(mx, my, button);
        if (activeTab == 2 && symbolSearchField != null) {
            symbolSearchField.mouseClicked(mx, my, button);
        }
        super.mouseClicked(mx, my, button);
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == 0) {
            mc.displayGuiScreen(parent);
        } else if (btn.id == 1) {
            advancedNameField.setText(""); updatePreview();
        } else if (btn.id == 2) {
            applyName(); mc.displayGuiScreen(parent);

        } else if (btn.id == BTN_TAB_COLORS)  { activeTab = 0; symbolPage = 0; initGui();
        } else if (btn.id == BTN_TAB_FORMAT)   { activeTab = 1; symbolPage = 0; initGui();
        } else if (btn.id == BTN_TAB_SYMBOLS)  { activeTab = 2; symbolPage = 0; initGui();

        } else if (btn.id == BTN_SYM_PREV) {
            if (symbolPage > 0) { symbolPage--; buildTabContent(); }
        } else if (btn.id == BTN_SYM_NEXT) {
            int total = (filteredSymbols.length + SYMBOLS_PER_PAGE - 1) / SYMBOLS_PER_PAGE;
            if (symbolPage < total - 1) { symbolPage++; buildTabContent(); }

        } else if (btn.id >= 10 && btn.id < 26) {
            insertAtCursor("&" + COLOR_CHARS[btn.id - 10]);
        } else if (btn.id >= 30 && btn.id < 36) {
            insertAtCursor(FORMAT_INSERT[btn.id - 30]);
        } else if (btn.id >= 50 && btn.id < 98) {
            int symIdx = symbolPage * SYMBOLS_PER_PAGE + (btn.id - 50);
            if (symIdx < filteredSymbols.length) {
                insertAtCursor(glyphFor(filteredSymbols[symIdx][0]));
            }
        }
    }

    private void insertAtCursor(String toInsert) {
        String cur = advancedNameField.getText();
        int pos    = advancedNameField.getCursorPosition();
        advancedNameField.setText(cur.substring(0, pos) + toInsert + cur.substring(pos));
        advancedNameField.setCursorPosition(pos + toInsert.length());
        updatePreview();
    }

    private void applyName() {
        updatePreview();
        String name = previewName;
        int vis = countVisibleChars(name);
        while (vis < 3) { name += "_"; vis++; }
        EaglerProfile.setName(name);
        EaglerProfile.save();
    }

    @Override
    public void updateScreen() {
        advancedNameField.updateCursorCounter();
        if (symbolSearchField != null) symbolSearchField.updateCursorCounter();
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override
    public boolean showCopyPasteButtons() { return advancedNameField.isFocused(); }

    @Override
    public void fireInputEvent(EnumInputEvent event, String param) {
        advancedNameField.fireInputEvent(event, param);
    }

    @Override
    public boolean doesGuiPauseGame() { return true; }
}