package net.lax1dude.eaglercraft.v1_8.internal;

import net.lax1dude.eaglercraft.v1_8.internal.vfs2.VFile2;
import net.minecraft.actx.data.actxmiscdata;

public class PlatformActxMisc {
    // old file breh i focused on WASM to much including platform-api
    public static void exportMiscData() {
        actxmiscdata.ensureLoaded();

        VFile2 file = new VFile2("actx_misc.dat");
        if (!file.exists()) {
            return;
        }

        byte[] bytes = file.getAllBytes();
        if (bytes == null || bytes.length == 0) {
            return;
        }

        PlatformApplication.downloadFileWithName("actx_misc.dat", bytes);
    }

    public static void importMiscData() {
        PlatformApplication.displayFileChooser("application/octet-stream", "dat");
    }
}