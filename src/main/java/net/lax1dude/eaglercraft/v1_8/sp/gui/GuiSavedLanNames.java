package net.lax1dude.eaglercraft.v1_8.sp.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.actx.data_o_algo.actxmiscdata;

import java.util.List;

public class GuiSavedLanNames extends GuiScreen {

    private static final int BTN_CANCEL  = 0;
    private static final int BTN_DONE    = 1;
    private static final int BTN_DELETE  = 2;

    private static final int ENTRY_H = 24;

    private final GuiScreen parent;
    private int selectedIndex = -1;
    private int scrollOffset  = 0;
    private int listTop, listBottom;

    public GuiSavedLanNames(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        listTop    = 32;
        listBottom = height - 48;

        int btnY  = height - 26;
        int btnW  = 150;
        int gap   = 4;
        int totalW = btnW * 2 + gap;
        int startX = width / 2 - totalW / 2;

        buttonList.add(new GuiButton(BTN_DELETE, 4, btnY, 70, 20, "\u00a7c" + I18n.format("gui.act.delete")));
        buttonList.add(new GuiButton(BTN_CANCEL, startX,          btnY, btnW, 20, I18n.format("gui.cancel")));
        buttonList.add(new GuiButton(BTN_DONE,   startX + btnW + gap, btnY, btnW, 20, I18n.format("gui.done")));

        updateButtonStates();
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        switch (btn.id) {

            case BTN_CANCEL:
                mc.displayGuiScreen(parent);
                break;

            case BTN_DONE:
                List<String> savedNamesForDone = actxmiscdata.getSavedLanNames();
                if (selectedIndex >= 0 && selectedIndex < savedNamesForDone.size()) {
                    String stored = savedNamesForDone.get(selectedIndex);
                    String editable = stored.replace("\u00a7", "&");
                    if (parent instanceof GuiMiscLan) {
                        ((GuiMiscLan) parent).nameField.setText(editable);
                    }
                    mc.displayGuiScreen(parent);
                }
                break;

            case BTN_DELETE:
                List<String> savedNamesForDelete = actxmiscdata.getSavedLanNames();
                if (selectedIndex >= 0 && selectedIndex < savedNamesForDelete.size()) {
                    actxmiscdata.removeSavedLanName(selectedIndex);
                    selectedIndex = -1;
                    
                    int newSize = actxmiscdata.getSavedLanNames().size();
                    int visibleRows = Math.max(1, (listBottom - listTop) / ENTRY_H);
                    int maxScroll   = Math.max(0, newSize - visibleRows);
                    if (scrollOffset > maxScroll) scrollOffset = maxScroll;
                    updateButtonStates();
                }
                break;
        }
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        drawDefaultBackground();

        drawCenteredString(fontRendererObj,
            "\u00a7f" + I18n.format("gui.act.misc.savedLanNames"), width / 2, 10, 0xFFFFFF);

        drawRect(0, listTop - 2, width, listTop - 1, 0x55FFFFFF);

        List<String> savedNames = actxmiscdata.getSavedLanNames();

        if (savedNames.isEmpty()) {
            drawCenteredString(fontRendererObj,
                "\u00a77" + I18n.format("gui.act.misc.noSavedNames"),
                width / 2, (listTop + listBottom) / 2 - 4, 0xFFFFFF);
            drawBottomStrip(mx, my, partialTicks);
            return;
        }

        int visibleRows = Math.max(1, (listBottom - listTop) / ENTRY_H);
        int maxScroll   = Math.max(0, savedNames.size() - visibleRows);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        for (int i = scrollOffset; i < savedNames.size(); i++) {
            int rowY = listTop + (i - scrollOffset) * ENTRY_H;
            if (rowY + ENTRY_H > listBottom) break;

            boolean isSelected = (i == selectedIndex);
            boolean isHovered  = (my >= rowY && my < rowY + ENTRY_H
                                  && mx >= 0 && mx < width);

            if (isHovered && !isSelected) {
                drawRect(0, rowY, width, rowY + ENTRY_H, 0x33FFFFFF);
            }

            if (isSelected) {
                drawRect(0,         rowY,              width,     rowY + 1,         0xFFFFFFFF);
                drawRect(0,         rowY + ENTRY_H - 1, width,   rowY + ENTRY_H,   0xFFFFFFFF);
                drawRect(0,         rowY,              1,         rowY + ENTRY_H,   0xFFFFFFFF);
                drawRect(width - 1, rowY,              width,     rowY + ENTRY_H,   0xFFFFFFFF);
            }

            String raw     = savedNames.get(i);
            String display = raw.replaceAll("&([0-9a-fA-FlLoOnNkKmMrR])", "\u00a7$1");
            drawCenteredString(fontRendererObj,
                display, width / 2, rowY + (ENTRY_H - 8) / 2, 0xFFFFFF);
        }

        int listH      = listBottom - listTop;
        int totalItems = savedNames.size();
        if (totalItems > visibleRows && listH > 0) {
            int barH = Math.max(20, listH * listH / (totalItems * ENTRY_H));
            int barY = listTop + (listH - barH) * scrollOffset / Math.max(1, maxScroll);
            drawRect(width - 6, listTop,  width,     listBottom,  0xFF222222);
            drawRect(width - 6, barY,     width,     barY + barH, 0xFFAAAAAA);
            drawRect(width - 5, barY + 1, width - 1, barY + barH - 1, 0xFFCCCCCC);
        }

        drawBottomStrip(mx, my, partialTicks);
    }

    private void drawBottomStrip(int mx, int my, float partialTicks) {

        drawRect(0, listBottom, width, listBottom + 1, 0x55FFFFFF);

        List<String> savedNames = actxmiscdata.getSavedLanNames();
        String infoText;
        if (selectedIndex >= 0 && selectedIndex < savedNames.size()) {
            String stored   = savedNames.get(selectedIndex);
            String rawForm  = stored.replace("\u00a7", "&");
            int maxLen      = (width - 20) / 6;
            if (rawForm.length() > maxLen) rawForm = rawForm.substring(0, maxLen - 3) + "...";
            infoText = "\u00a7f" + I18n.format("gui.act.misc.raw") + ": \u00a7f" + rawForm;
        } else {
            infoText = "\u00a7f" + I18n.format("gui.act.misc.selectName");
        }
        drawCenteredString(fontRendererObj, infoText,
            width / 2, listBottom + 4, 0xFFFFFF);

        super.drawScreen(mx, my, partialTicks);
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        super.mouseClicked(mx, my, button);
        if (my >= listTop && my < listBottom) {
            int row = scrollOffset + (my - listTop) / ENTRY_H;
            if (row >= 0 && row < actxmiscdata.getSavedLanNames().size()) {
                selectedIndex = row;
                updateButtonStates();
            }
        }
    }

    @Override
    public void handleMouseInput() throws java.io.IOException {
        super.handleMouseInput();
        int wheel = net.lax1dude.eaglercraft.v1_8.Mouse.getDWheel();
        if (wheel != 0) {
            int visibleRows = Math.max(1, (listBottom - listTop) / ENTRY_H);
            int maxScroll   = Math.max(0, actxmiscdata.getSavedLanNames().size() - visibleRows);
            if (wheel > 0 && scrollOffset > 0)             scrollOffset--;
            else if (wheel < 0 && scrollOffset < maxScroll) scrollOffset++;
        }
    }

    @Override
    protected void keyTyped(char c, int k) {
        if (k == 1) mc.displayGuiScreen(parent);
        if (k == 200 && selectedIndex > 0) {
            selectedIndex--;
            if (selectedIndex < scrollOffset) scrollOffset = selectedIndex;
            updateButtonStates();
        }
        if (k == 208 && selectedIndex < actxmiscdata.getSavedLanNames().size() - 1) {
            selectedIndex++;
            int visibleRows = Math.max(1, (listBottom - listTop) / ENTRY_H);
            if (selectedIndex >= scrollOffset + visibleRows)
                scrollOffset = selectedIndex - visibleRows + 1;
            updateButtonStates();
        }
        if (k == 28 && selectedIndex >= 0) {
            for (GuiButton b : buttonList) {
                if (b.id == BTN_DONE && b.enabled) {
                    try { actionPerformed(b); } catch (Exception ignored) {}
                    break;
                }
            }
        }
        super.keyTyped(c, k);
    }

    private void updateButtonStates() {
        boolean hasSel = selectedIndex >= 0
                && selectedIndex < actxmiscdata.getSavedLanNames().size();
        for (GuiButton btn : buttonList) {
            if (btn.id == BTN_DONE || btn.id == BTN_DELETE) btn.enabled = hasSel;
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return true; }
}