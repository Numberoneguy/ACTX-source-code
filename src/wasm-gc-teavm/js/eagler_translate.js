window.eaglerTranslateQueue = window.eaglerTranslateQueue || [];

window.eaglerTranslate = async function(text, targetLang) {
    if (!text || !text.trim()) return;
    var tl = targetLang || "en";

    var url = "https://translate.googleapis.com/translate_a/single"
        + "?client=gtx"
        + "&sl=auto"
        + "&tl=" + encodeURIComponent(tl)
        + "&dt=t"
        + "&q=" + encodeURIComponent(text);

    try {
        var response = await fetch(url);
        if (!response.ok) throw new Error("Network response was not ok not OK");

        var data = await response.json();
        var result = "";
        if (data && data[0]) {
            for (var i = 0; i < data[0].length; i++) {
                if (data[0][i] && data[0][i][0]) {
                    result += data[0][i][0];
                }
            }
        }
        if (result) {
            window.eaglerTranslateQueue.push(result);
        }
    } catch (e) {
        console.warn("[ActXTranslate] fetch failed:", e);
    }
};
