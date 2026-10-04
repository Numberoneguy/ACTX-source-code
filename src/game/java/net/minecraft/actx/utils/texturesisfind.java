package net.minecraft.actx.utils;
import java.nio.charset.StandardCharsets;
import java.util.Map;
public class texturesisfind {
    public static boolean isValidPayload(String resourcepath, int resourcetype, byte[] filedata) {
        if (resourcepath == null || resourcepath.isEmpty()) {
            return false;
        }
        if (filedata == null || filedata.length == 0) {
            return false;
        }
        if (resourcenamespaces.isVanilla(resourcepath)) {
            return false;
        }
        switch (resourcetype) {
            case 0:
            case 2:
            case 3:
                return isValidJsonObject(filedata);
            case 1:
                return isLikelyPng(filedata);
            default:
                return false;
        }
    }
    private static boolean isLikelyPng(byte[] data) {
        if (data.length < 8) {
            return false;
        }
        return (data[0] & 0xFF) == 0x89
                && data[1] == 'P'
                && data[2] == 'N'
                && data[3] == 'G';
    }
    private static boolean isValidJsonObject(byte[] data) {
        try {
            Object parsed = minijson.parse(new String(data, StandardCharsets.UTF_8));
            return parsed instanceof Map;
        } catch (Exception e) {
            return false;
        }
    }
}
