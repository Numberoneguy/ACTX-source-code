package net.lax1dude.eaglercraft.v1_8.internal.lwjgl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class MainClass {

    private static final List<String> LINUX_NATIVES = Arrays.asList(
        "libUnsafeMemcpy.so",
        "libopenal.so",
        "libwebrtc-java.so",
        "libjemalloc.so",
        "libglfw.so",
        "liblwjgl.so",
        "liblwjgl_opengles.so",
        "libGLESv2.so"
    );

    private static final List<String> WINDOWS_NATIVES = Arrays.asList(
        "UnsafeMemcpy.dll",
        "OpenAL.dll",
        "webrtc-java.dll",
        "jemalloc.dll",
        "glfw.dll",
        "lwjgl.dll",
        "lwjgl_opengles.dll",
        "libEGL.dll",
        "libGLESv2.dll",
        "d3dcompiler_47.dll",
        "vulkan-1.dll"
    );

    public static void main(String[] args) {
        // 1. Extract native binaries and force-load UnsafeMemcpy
        extractAndLoadNatives();

        // 2. Boot Eaglercraft desktop client
        LWJGLEntryPoint.main_(args);
    }

    private static void extractAndLoadNatives() {
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osName.contains("win");
        String nativeFolderInJar = isWindows ? "/natives/windows-x86_64/" : "/natives/linux-x86_64/";
        List<String> targetNatives = isWindows ? WINDOWS_NATIVES : LINUX_NATIVES;

        try {
            File tempDir = File.createTempFile("eagler_natives_", "");
            tempDir.delete();
            if (!tempDir.mkdirs()) {
                throw new IllegalStateException("Failed to create native temp directory: " + tempDir.getAbsolutePath());
            }
            tempDir.deleteOnExit();

            for (String libName : targetNatives) {
                // Try subfolder path first, then fallbacks
                InputStream in = MainClass.class.getResourceAsStream(nativeFolderInJar + libName);
                if (in == null) {
                    in = MainClass.class.getResourceAsStream("/natives/" + libName);
                }
                if (in == null) {
                    in = MainClass.class.getResourceAsStream("/" + libName);
                }

                if (in != null) {
                    File outFile = new File(tempDir, libName);
                    outFile.deleteOnExit();
                    try (FileOutputStream out = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                } else {
                    System.err.println("[MainClass] Warning: Missing embedded native " + libName);
                }
            }

            String absPath = tempDir.getAbsolutePath();

            // Set native library search paths
            System.setProperty("java.library.path", absPath);
            System.setProperty("org.lwjgl.librarypath", absPath);

            // Force JVM ClassLoader sys_paths cache reset
            try {
                Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
                fieldSysPath.setAccessible(true);
                fieldSysPath.set(null, null);
            } catch (Throwable ignored) {}

            // Direct System.load using exact absolute file path
            String unsafeFileName = isWindows ? "UnsafeMemcpy.dll" : "libUnsafeMemcpy.so";
            File unsafeLib = new File(tempDir, unsafeFileName);
            if (unsafeLib.exists()) {
                System.load(unsafeLib.getAbsolutePath());
                System.out.println("[MainClass] Successfully loaded " + unsafeFileName + " directly!");
            } else {
                System.err.println("[MainClass] Could not find " + unsafeFileName + " in extracted temp directory.");
            }

        } catch (Throwable t) {
            System.err.println("[MainClass] Critical failure unpacking native libraries:");
            t.printStackTrace();
        }
    }
}