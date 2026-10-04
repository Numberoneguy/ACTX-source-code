package net.minecraft.client.gui;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.internal.FileChooserResult;
import net.lax1dude.eaglercraft.v1_8.internal.PlatformActxMisc;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.actx.selector.buttonfilehighlight;

public class GuiActMiscDataOptions extends GuiScreen implements GuiYesNoCallback {
    private static final String filename = actxmiscdata.file_name;
    private static final int file_highlight_color = 0xFF0000;
    private static final int btn_width = 200;
    private static final int btn_height = 20;
    private static final int btn_import = 100;
    private static final int btn_export = 101;
    private static final int btn_wipe = 102;
    private static final int btn_done = 200;
    private static final int confirm_wipe = 300;
    private final GuiScreen parentScreen;
    protected String screenTitle = "";
    private boolean waitingForImport = false;
    public GuiActMiscDataOptions(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }
    @Override
    public void initGui() {
        this.screenTitle = GuiActOptions.translate("act.miscDataOptions");
        this.buttonList.clear();
        int gap = 6;
        int importY = this.height / 6 + 36;
        int exportY = importY + btn_height + gap;
        int wipeY = exportY + btn_height + gap;
        int x = this.width / 2 - btn_width / 2;
        this.buttonList.add(new buttonfilehighlight(btn_import, x, importY,
                btn_width, btn_height, GuiActOptions.translate("act.miscData.import"), filename, file_highlight_color));
        this.buttonList.add(new buttonfilehighlight(btn_export, x, exportY,
                btn_width, btn_height, GuiActOptions.translate("act.miscData.export"), filename, file_highlight_color));
        this.buttonList.add(new buttonfilehighlight(btn_wipe, x, wipeY,
                btn_width, btn_height, GuiActOptions.translate("act.miscData.wipe"), filename, file_highlight_color));
        this.buttonList.add(new GuiButton(btn_done, x, this.height - 27,
                btn_width, btn_height, GuiActOptions.translate("gui.done")));
    }
    @Override
    protected void actionPerformed(GuiButton parGuiButton) {
        if (!parGuiButton.enabled) {
            return;
        }

        if (parGuiButton.id == btn_import) {
            waitingForImport = true;
            PlatformActxMisc.importMiscData();
        }

        if (parGuiButton.id == btn_export) {
            PlatformActxMisc.exportMiscData();
        }

        if (parGuiButton.id == btn_wipe) {
            this.mc.displayGuiScreen(new GuiYesNo(this,
                    GuiActOptions.translate("act.miscData.wipe.title"),
                    GuiActOptions.translate("act.miscData.wipe.warning"),
                    confirm_wipe));
        }

        if (parGuiButton.id == btn_done) {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (id == confirm_wipe && result) {
            actxmiscdata.wipeAll(null); // client copy
            try { SingleplayerServerController.sendMiscDataWipe(); } catch (Throwable ignored) { } // server copy
        }
        this.mc.displayGuiScreen(this);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        actxmiscdata.tickExport();

        if (waitingForImport && EagRuntime.fileChooserHasResult()) {
            waitingForImport = false;
            FileChooserResult result = EagRuntime.getFileChooserResult();
            EagRuntime.clearFileChooserResult();

            if (result != null && result.fileData != null && result.fileData.length > 0) {
                actxmiscdata.replaceFileWith(result.fileData, null); // client copy
                try { SingleplayerServerController.sendMiscDataImport(result.fileData); } catch (Throwable ignored) { } // server copy + refresh
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 15, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}