package net.lax1dude.eaglercraft.v1_8.internal;

import org.teavm.jso.JSBody;

public class PlatformTranslator {

    @JSBody(params = { "text", "targetLang" },
            script = "if(window.eaglerTranslate) window.eaglerTranslate(text, targetLang);")
    public static native void fireTranslate(String text, String targetLang);

    @JSBody(params = {},
            script = "return (window.eaglerTranslateQueue && window.eaglerTranslateQueue.length > 0)"
                   + " ? window.eaglerTranslateQueue.shift() : null;")
    public static native String popResult();

    @JSBody(params = { "text" },
            script = "if(window.eaglerTranslateChinese) window.eaglerTranslateChinese(text);")
    public static native void fireTranslateChinese(String text);

    @JSBody(params = {},
            script = "return (window.eaglerTranslateChineseQueue && window.eaglerTranslateChineseQueue.length > 0)"
                   + " ? window.eaglerTranslateChineseQueue.shift() : null;")
    public static native String popResultChinese();
}