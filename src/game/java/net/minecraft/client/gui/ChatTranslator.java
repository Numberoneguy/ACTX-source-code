package net.minecraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.lax1dude.eaglercraft.v1_8.internal.PlatformTranslator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class ChatTranslator {

    private static int pendingCount = 0;
    private static volatile boolean enabled = true;
    private static final int POLL_INTERVAL = 1;
    private static final int MAX_PENDING = 15;

    private static volatile String lastRequestedRaw = null;
    private static volatile long lastRequestedAtMillis = 0L;
    private static final long DEDUPE_WINDOW_MILLIS = 1500L;
    // added § just in case.
    private static volatile String prefixText = "§f§lTranslation: §f§l";
    private static volatile EnumChatFormatting prefixColor = EnumChatFormatting.WHITE;
    private static volatile boolean prefixBold = true;
    private static volatile String targetLangOverride = null;

    public static String getPrefixText() {
        return prefixText;
    }

    public static void setPrefixText(String value) {
        prefixText = (value == null || value.isEmpty()) ? "" : value;
    }

    public static EnumChatFormatting getPrefixColor() {
        return prefixColor;
    }

    public static void setPrefixColor(EnumChatFormatting value) {
        prefixColor = (value == null) ? EnumChatFormatting.WHITE : value;
    }

    public static boolean isPrefixBold() {
        return prefixBold;
    }

    public static void setPrefixBold(boolean value) {
        prefixBold = value;
    }

    public static String getTargetLangOverride() {
        return targetLangOverride;
    }

    public static void setTargetLangOverride(String value) {
        targetLangOverride = (value == null || value.trim().isEmpty()) ? null : value.trim().toLowerCase();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!value) flush();
    }

    public static void translateIfNeeded(String rawMessage) {
        if (!enabled) return;
        if (rawMessage == null || rawMessage.trim().isEmpty()) return;
        String stripped = stripFormatting(rawMessage.trim());
        if (!needsTranslation(stripped)) return;

        long now = System.currentTimeMillis();
        if (stripped.equals(lastRequestedRaw) && (now - lastRequestedAtMillis) < DEDUPE_WINDOW_MILLIS) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) return;

        if (pendingCount >= MAX_PENDING) {
            flush();
        }

        lastRequestedRaw = stripped;
        lastRequestedAtMillis = now;

        String targetLang = (targetLangOverride != null) ? targetLangOverride : playerLang(mc.gameSettings.language);
        pendingCount++;
        PlatformTranslator.fireTranslate(stripped, targetLang);
    }

    public static void tick() {
        if (!enabled) {
            flush();
            return;
        }

        int drained = 0;
        String result;
        
        while ((result = PlatformTranslator.popResult()) != null) {
            if (result.isEmpty()) continue;

            if (pendingCount > 0) pendingCount--;
            drained++;

            String display = result.replaceAll("[\r\n]+", " ").trim();
            if (display.isEmpty()) continue;

            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                String formatCodes = prefixColor.toString() + (prefixBold ? EnumChatFormatting.BOLD.toString() : "");
                int maxWidth = (mc.ingameGUI != null) ? mc.ingameGUI.getChatGUI().getChatWidth() : 320;
                for (String line : wrapWithPrefix(formatCodes, prefixText, display, mc.fontRendererObj, maxWidth)) {
                    mc.thePlayer.addChatMessage(new ChatComponentText(line));
                }
            }

            if (drained >= MAX_PENDING) break;
        }

        if (pendingCount > MAX_PENDING * 2) {
            pendingCount = 0;
        }
    }

    public static void flush() {
        pendingCount = 0;
        int safety = 1000;
        while (PlatformTranslator.popResult() != null && --safety > 0) {
        }
    }

    private static List<String> wrapWithPrefix(String formatCodes, String label, String text, FontRenderer font,
            int maxWidth) {
        List<String> lines = new ArrayList<>();
        String firstLineStart = formatCodes + label;
        String continuationStart = formatCodes;

        StringBuilder current = new StringBuilder(firstLineStart);
        int startLen = firstLineStart.length();

        for (String word : text.split(" ")) {
            String withWord = current.length() == startLen ? current + word : current + " " + word;
            if (font.getStringWidth(withWord) <= maxWidth) {
                current = new StringBuilder(withWord);
                continue;
            }

            if (current.length() > startLen) {
                lines.add(current.toString());
                current = new StringBuilder(continuationStart);
                startLen = continuationStart.length();
            }

            String solo = current + word;
            if (font.getStringWidth(solo) <= maxWidth) {
                current = new StringBuilder(solo);
            } else {
                for (char c : word.toCharArray()) {
                    String withChar = current + String.valueOf(c);
                    if (font.getStringWidth(withChar) <= maxWidth) {
                        current.append(c);
                    } else {
                        lines.add(current.toString());
                        current = new StringBuilder(continuationStart).append(c);
                        startLen = continuationStart.length();
                    }
                }
            }
        }

        if (current.length() > startLen || lines.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private static boolean needsTranslation(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > 0x024F) return true;
        }
        return false;
    }

    public static String stripFormatting(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)\u00a7[0-9a-fk-or]", "");
    }

    private static String playerLang(String mcLang) {
        if (mcLang == null || mcLang.isEmpty()) return "en";
        int under = mcLang.indexOf('_');
        return (under > 0 ? mcLang.substring(0, under) : mcLang).toLowerCase();
    }
}