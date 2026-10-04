package net.minecraft.actx.utils;
public class resourcenamespaces {
    public static boolean isVanilla(String resourcepath) {
        if (resourcepath == null) {
            return true;
        }
        int colon = resourcepath.indexOf(':');
        String domain = (colon >= 0) ? resourcepath.substring(0, colon) : "minecraft";
        return domain.equals("minecraft");
    }
}
