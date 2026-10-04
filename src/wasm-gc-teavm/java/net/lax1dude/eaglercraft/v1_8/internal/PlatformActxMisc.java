package net.lax1dude.eaglercraft.v1_8.internal;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.minecraft.actx.data_o_algo.actxmiscdata;

public class PlatformActxMisc {
    public static void exportMiscData() {
        actxmiscdata.requestExport();
    }

    public static void importMiscData() {
        EagRuntime.displayFileChooser("application/x-yaml", "yml");
    }
}