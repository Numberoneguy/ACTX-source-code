package net.minecraft.actx.utils;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
public class actxchatutils {
    private static String substituteArgs(String pattern, Object[] args) {
        if (args == null || args.length == 0 || pattern.indexOf("%s") < 0) {
            return pattern;
        }
        StringBuilder result = new StringBuilder(pattern.length() + 16);
        int argindex = 0;
        int i = 0;
        int len = pattern.length();
        while (i < len) {
            if (argindex < args.length && pattern.charAt(i) == '%' && i + 1 < len && pattern.charAt(i + 1) == 's') {
                result.append(String.valueOf(args[argindex]));
                argindex++;
                i += 2;
            } else {
                result.append(pattern.charAt(i));
                i++;
            }
        }
        return result.toString();
    }
    public static IChatComponent makeComponent(String key, EnumChatFormatting color, Object... args) {
        String raw = StatCollector.translateToLocal(key);
        String text;
        if (args != null && args.length > 0) {
            Object[] resolvedargs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    resolvedargs[i] = "";
                } else if (args[i] instanceof IChatComponent) {
                    resolvedargs[i] = ((IChatComponent) args[i]).getFormattedText();
                } else {
                    resolvedargs[i] = args[i];
                }
            }
            text = substituteArgs(raw, resolvedargs);
        } else {
            text = raw;
        }
        ChatComponentText component = new ChatComponentText(text);
        if (color != null) {
            component.getChatStyle().setColor(color);
        }
        return component;
    }
    public static void sendTranslated(ICommandSender sender, EnumChatFormatting color, String key, Object... args) {
        sender.addChatMessage(makeComponent(key, color, args));
    }
    public static void sendSuccess(ICommandSender sender, String key, Object... args) {
        sendTranslated(sender, EnumChatFormatting.GREEN, key, args);
    }
    public static void sendError(ICommandSender sender, String key, Object... args) {
        sendTranslated(sender, EnumChatFormatting.RED, key, args);
    }
    public static void sendWarning(ICommandSender sender, String key, Object... args) {
        sendTranslated(sender, EnumChatFormatting.YELLOW, key, args);
    }
    public static void sendClickableCommand(ICommandSender sender, String textkey, String hoverkey, String commandtorun, EnumChatFormatting color) {
        IChatComponent component = makeComponent(textkey, color);
        component.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandtorun));
        String hovertext = StatCollector.translateToLocal(hoverkey);
        component.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(hovertext)));
        sender.addChatMessage(component);
    }
}
