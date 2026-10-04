package net.minecraft.actx.data_o_algo;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.lax1dude.eaglercraft.v1_8.Base64;
import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.crypto.SHA256Digest;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.lax1dude.eaglercraft.v1_8.internal.vfs2.VFile2;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
public final class actxmiscdata {
    public static final String default_join = "\u00a7e%player%\u00a7e joined the game.";
    public static final String default_leave = "\u00a7e%player%\u00a7e left the game.";
    public static final String default_chat = "\u00a7r<%player%\u00a7r> %message%";
    public static final Set<String> session_bans = new LinkedHashSet<String>();
    public static final String file_name = "actx_misc.yml";
    private static boolean loaded = false;
    private static String joinmessage = default_join;
    private static String leavemessage = default_leave;
    private static String chatformat = default_chat;
    private static String authtitle = null;
    private static String authsubtitle = null;
    private static String tabheader = null;
    private static String tabfooter = null;
    private static boolean bossbarenabled = true;
    private static boolean bossbarpreview = true;
    private static String bossbartitle = "";
    private static final Set<String> ops = new LinkedHashSet<String>();
    private static final Map<String, String> opuuids = new LinkedHashMap<String, String>();
    private static final Map<String, String[]> passwords = new LinkedHashMap<String, String[]>();
    private static final Set<String> loggedinusers = new HashSet<String>();
    private static final List<String> saveditems = new ArrayList<String>();
    private static final List<String> savedlannames = new ArrayList<String>();
    private static final Set<String> blockedcommands = new LinkedHashSet<String>();
    public static final class AnimationDef {
        public final int changeintervalms;
        public final List<String> frames;
        AnimationDef(int changeintervalms, List<String> frames) {
            this.changeintervalms = Math.max(1, changeintervalms);
            this.frames = frames;
        }
    }
    private static final Map<String, AnimationDef> animations = new HashMap<String, AnimationDef>();
    private actxmiscdata() {}
    public static boolean isBanned(String name) {
        String lower = name.toLowerCase();
        if (session_bans.contains(lower)) {
            return true;
        }
        for (String entry : session_bans) {
            if ((entry.indexOf('*') >= 0 || entry.indexOf('?') >= 0)
                    && net.minecraft.command.CommandBan.globglob(entry, lower)) {
                return true;
            }
        }
        return false;
    }
    public static void ensureLoaded() {
        MinecraftServer server = MinecraftServer.getServer();
        ensureLoaded(server != null ? server.getConfigurationManager() : null);
    }
    public static synchronized void ensureLoaded(ServerConfigurationManager scm) {
        if (!loaded) {
            load(scm);
            loaded = true;
        } else if (scm != null) {
            syncOpsToScm(scm);
        }
    }
    private static void load(ServerConfigurationManager scm) {
        byte[] raw = readBytes();
        if (raw != null && raw.length > 0) parseYamlInto(raw);
        if (scm != null) syncOpsToScm(scm);
    }
    private static void parseYamlInto(byte[] raw) {
        try {
            Object root = MiniYaml.parse(new String(raw, StandardCharsets.UTF_8));
            Map<String, Object> map = asMap(root);
            if (map != null) applyRoot(map);
        } catch (Exception e) {
        }
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : null;
    }
    private static String asString(Object o) {
        return o instanceof String ? (String) o : null;
    }
    private static List<String> asStringList(Object o) {
        if (!(o instanceof List)) return null;
        List<String> out = new ArrayList<String>();
        for (Object x : (List<?>) o) {
            if (x instanceof String) out.add((String) x);
        }
        return out;
    }
    private static String linesToString(Object o) {
        if (o instanceof String) return (String) o;
        List<String> lines = asStringList(o);
        if (lines == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines.get(i));
        }
        return sb.toString();
    }
    private static String unescapePct(String s) {
        return s.replace("%/", "%");
    }
    private static void applyRoot(Map<String, Object> root) {
        String v;
        Map<String, Object> messages = asMap(root.get("messages"));
        if (messages != null) {
            if ((v = asString(messages.get("join"))) != null && !v.isEmpty()) joinmessage = unescapePct(v);
            if ((v = asString(messages.get("leave"))) != null && !v.isEmpty()) leavemessage = unescapePct(v);
            if ((v = asString(messages.get("chat"))) != null && !v.isEmpty()) chatformat = unescapePct(v);
        }
        Map<String, Object> bossbar = asMap(root.get("bossbar"));
        if (bossbar != null) {
            if ((v = asString(bossbar.get("enabled"))) != null) bossbarenabled = Boolean.parseBoolean(v.trim());
            if ((v = asString(bossbar.get("preview"))) != null) bossbarpreview = Boolean.parseBoolean(v.trim());
            if ((v = asString(bossbar.get("title"))) != null) bossbartitle = unescapePct(v);
        }
        Map<String, Object> auth = asMap(root.get("auth"));
        if (auth != null) {
            if ((v = asString(auth.get("title"))) != null) authtitle = unescapePct(v);
            if ((v = asString(auth.get("subtitle"))) != null) authsubtitle = unescapePct(v);
        }
        Map<String, Object> tablist = asMap(root.get("tablist"));
        if (tablist != null) {
            if ((v = linesToString(tablist.get("header"))) != null) tabheader = unescapePct(v);
            if ((v = linesToString(tablist.get("footer"))) != null) tabfooter = unescapePct(v);
        }
        Map<String, Object> permissions = asMap(root.get("permissions"));
        if (permissions != null) {
            readOps(permissions.get("op"));
            List<String> bannedlist = asStringList(permissions.get("banned"));
            if (bannedlist != null) {
                session_bans.clear();
                for (String b : bannedlist) session_bans.add(b.toLowerCase());
            }
        }
        Map<String, Object> pw = asMap(root.get("passwords"));
        if (pw != null) {
            passwords.clear();
            for (Map.Entry<String, Object> e : pw.entrySet()) {
                Map<String, Object> cred = asMap(e.getValue());
                if (cred == null) continue;
                String salt = asString(cred.get("salt"));
                String hash = asString(cred.get("hash"));
                if (salt != null && hash != null) {
                    String uuid = asString(cred.get("uuid"));
                    passwords.put(e.getKey().toLowerCase(), new String[] { salt, hash.trim().toLowerCase(), uuid == null ? "" : uuid });
                }
            }
        }
        List<String> items = asStringList(root.get("custom_items"));
        if (items != null) {
            saveditems.clear();
            saveditems.addAll(items);
        }
        Map<String, Object> lan = asMap(root.get("lan"));
        if (lan != null) {
            List<String> names = asStringList(lan.get("names"));
            if (names != null) {
                savedlannames.clear();
                savedlannames.addAll(names);
            }
        }
        Map<String, Object> restrictions = asMap(root.get("restrictions"));
        if (restrictions != null) {
            List<String> blocked = asStringList(restrictions.get("blocked_commands"));
            if (blocked != null) {
                blockedcommands.clear();
                for (String b : blocked) blockedcommands.add(b.toLowerCase());
            }
        }
        readSavedObjectives(root.get("scoreboards"));
        Map<String, Object> anims = asMap(root.get("animations"));
        if (anims != null) {
            animations.clear();
            for (Map.Entry<String, Object> e : anims.entrySet()) {
                Map<String, Object> a = asMap(e.getValue());
                if (a == null) continue;
                List<String> framesin = asStringList(a.get("frames"));
                if (framesin == null || framesin.isEmpty()) continue;
                int interval = 1000;
                String iv = asString(a.get("change-interval"));
                if (iv != null) {
                    try { interval = Integer.parseInt(iv.trim()); } catch (NumberFormatException ex) { }
                }
                List<String> frames = new ArrayList<String>(framesin.size());
                for (String f : framesin) frames.add(unescapePct(f));
                animations.put(e.getKey(), new AnimationDef(interval, frames));
            }
        }
    }
    private static void syncOpsToScm(ServerConfigurationManager scm) {
        for (String opname : ops) {
            if (!opname.isEmpty()) scm.addOp(opname);
        }
    }
    private static String buildFileContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(file_name).append('\n');
        sb.append("messages:\n");
        putScalar(sb, 1, "join", joinmessage);
        putScalar(sb, 1, "leave", leavemessage);
        putScalar(sb, 1, "chat", chatformat);
        sb.append("\nbossbar:\n");
        sb.append("  enabled: ").append(bossbarenabled).append('\n');
        sb.append("  preview: ").append(bossbarpreview).append('\n');
        putScalar(sb, 1, "title", bossbartitle);
        sb.append("\nauth:\n");
        putScalar(sb, 1, "title", getAuthTitle());
        putScalar(sb, 1, "subtitle", getAuthSubtitle());
        sb.append("\ntablist:\n");
        putList(sb, 1, "header", splitLines(getTabHeader()));
        putList(sb, 1, "footer", splitLines(getTabFooter()));
        sb.append("\npermissions:\n");
        if (ops.isEmpty()) {
            sb.append("  op: []\n");
        } else {
            sb.append("  op:\n");
            for (String opname : ops) {
                String u = opuuids.get(opname);
                sb.append("    ").append(yamlKey(opname)).append(": ").append(yamlQuote(u == null ? "" : u)).append('\n');
            }
        }
        putList(sb, 1, "banned", session_bans);
        sb.append("\npasswords:");
        if (passwords.isEmpty()) {
            sb.append(" {}\n");
        } else {
            sb.append('\n');
            for (Map.Entry<String, String[]> e : passwords.entrySet()) {
                sb.append("  ").append(yamlKey(e.getKey())).append(":\n");
                if (e.getValue().length > 2 && !e.getValue()[2].isEmpty()) putScalar(sb, 2, "uuid", e.getValue()[2]);
                putScalar(sb, 2, "salt", e.getValue()[0]);
                putScalar(sb, 2, "hash", e.getValue()[1]);
            }
        }
        sb.append('\n');
        putList(sb, 0, "custom_items", saveditems);
        sb.append("\nlan:\n");
        putList(sb, 1, "names", savedlannames);
        sb.append("\nrestrictions:\n");
        putList(sb, 1, "blocked_commands", blockedcommands);
        sb.append('\n');
        writeSavedObjectives(sb);
        sb.append("\nanimations:");
        if (animations.isEmpty()) {
            sb.append(" {}\n");
        } else {
            sb.append('\n');
            List<String> animnames = new ArrayList<String>(animations.keySet());
            Collections.sort(animnames);
            for (String name : animnames) {
                AnimationDef def = animations.get(name);
                sb.append("  ").append(yamlKey(name)).append(":\n");
                sb.append("    change-interval: ").append(def.changeintervalms).append('\n');
                putList(sb, 2, "frames", def.frames);
            }
        }
        return sb.toString();
    }
    private static void indent(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) sb.append("  ");
    }
    private static void putScalar(StringBuilder sb, int level, String key, String value) {
        indent(sb, level);
        sb.append(key).append(": ").append(yamlQuote(value)).append('\n');
    }
    private static void putList(StringBuilder sb, int level, String key, java.util.Collection<String> values) {
        indent(sb, level);
        if (values.isEmpty()) {
            sb.append(key).append(": []\n");
            return;
        }
        sb.append(key).append(":\n");
        for (String v : values) {
            indent(sb, level + 1);
            sb.append("- ").append(yamlQuote(v)).append('\n');
        }
    }
    private static String yamlKey(String key) {
        return key.matches("[A-Za-z0-9_\\-]+") ? key : yamlQuote(key);
    }
    private static String yamlQuote(String s) {
        if (s == null) s = "";
        StringBuilder sb = new StringBuilder(s.length() + 2);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u");
                        for (int p = hex.length(); p < 4; p++) sb.append('0');
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }
    private static List<String> splitLines(String s) {
        return Arrays.asList((s == null ? "" : s).split("\n", -1));
    }
    public static synchronized String getAnimationFrame(String name, long nowmillis) {
        AnimationDef def = animations.get(name);
        if (def == null || def.frames.isEmpty()) return null;
        int index = (int) ((nowmillis / def.changeintervalms) % def.frames.size());
        if (index < 0) index += def.frames.size();
        return def.frames.get(index);
    }
    public static synchronized boolean hasAnimation(String name) {
        return animations.containsKey(name);
    }
    public static synchronized List<String> getAnimationNames() {
        List<String> names = new ArrayList<String>(animations.keySet());
        Collections.sort(names);
        return names;
    }
    public static synchronized int getAnimationInterval(String name) {
        AnimationDef def = animations.get(name);
        return def != null ? def.changeintervalms : -1;
    }
    public static synchronized String getAnimationFramesJoined(String name) {
        AnimationDef def = animations.get(name);
        if (def == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < def.frames.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(def.frames.get(i));
        }
        return sb.toString();
    }
    public static synchronized boolean setAnimation(String name, int intervalms, String framesjoined) {
        if (name == null || !name.matches("[A-Za-z0-9_\\-]+")) return false;
        List<String> frames = new ArrayList<String>();
        for (String line : (framesjoined == null ? "" : framesjoined).split("\n", -1)) {
            if (!line.isEmpty()) frames.add(line.replace("%/", "%"));
        }
        if (frames.isEmpty()) return false;
        animations.put(name, new AnimationDef(intervalms, frames));
        saveSafe();
        return true;
    }
    public static synchronized boolean removeAnimation(String name) {
        boolean existed = animations.remove(name) != null;
        if (existed) saveSafe();
        return existed;
    }
    private static void readOps(Object o) {
        if (o instanceof List) {
            List<String> names = asStringList(o);
            ops.clear();
            opuuids.clear();
            for (String n : names) {
                if (n.isEmpty()) continue;
                ops.add(n);
                opuuids.put(n, "");
            }
        } else if (o instanceof Map) {
            ops.clear();
            opuuids.clear();
            for (Map.Entry<String, Object> e : asMap(o).entrySet()) {
                if (e.getKey().isEmpty()) continue;
                String u = asString(e.getValue());
                ops.add(e.getKey());
                opuuids.put(e.getKey(), u == null ? "" : u);
            }
        }
    }
    public static synchronized void addOpRecord(String name, String uuid) {
        if (name == null || name.isEmpty()) return;
        ops.add(name);
        opuuids.put(name, uuid == null ? "" : uuid);
    }
    public static synchronized void save(ServerConfigurationManager scm) {
        if (scm != null) {
            Set<String> candidates = new LinkedHashSet<String>(ops);
            for (String username : scm.getAllUsernames()) candidates.add(username);
            Map<String, String> olduuids = new LinkedHashMap<String, String>(opuuids);
            ops.clear();
            opuuids.clear();
            for (String name : candidates) {
                if (!scm.isOpped(name)) continue;
                ops.add(name);
                String uuid = olduuids.get(name);
                net.minecraft.entity.player.EntityPlayerMP online = scm.getPlayerByUsername(name);
                if (online != null) uuid = online.getUniqueID().toString();
                opuuids.put(name, uuid == null ? "" : uuid);
            }
        }
        else {
            refreshServerOwnedFromDisk();
        }
        writeBytes(buildFileContent().getBytes(StandardCharsets.UTF_8));
    }
    private static void refreshServerOwnedFromDisk() {
        byte[] raw = readBytes();
        if (raw == null || raw.length == 0) return;
        try {
            Map<String, Object> root = asMap(MiniYaml.parse(new String(raw, StandardCharsets.UTF_8)));
            if (root == null) return;
            Map<String, Object> pw = asMap(root.get("passwords"));
            if (pw != null) {
                passwords.clear();
                for (Map.Entry<String, Object> e : pw.entrySet()) {
                    Map<String, Object> cred = asMap(e.getValue());
                    if (cred == null) continue;
                    String salt = asString(cred.get("salt"));
                    String hash = asString(cred.get("hash"));
                    if (salt != null && hash != null) {
                        String uuid = asString(cred.get("uuid"));
                    passwords.put(e.getKey().toLowerCase(), new String[] { salt, hash.trim().toLowerCase(), uuid == null ? "" : uuid });
                    }
                }
            }
            readSavedObjectives(root.get("scoreboards"));
            Map<String, Object> permissions = asMap(root.get("permissions"));
            if (permissions != null) {
                readOps(permissions.get("op"));
                List<String> bannedlist = asStringList(permissions.get("banned"));
                if (bannedlist != null) {
                    session_bans.clear();
                    for (String b : bannedlist) session_bans.add(b.toLowerCase());
                }
            }
        } catch (Exception e) {
        }
    }
    private static void saveSafe() {
        MinecraftServer server = MinecraftServer.getServer();
        save(server != null ? server.getConfigurationManager() : null);
    }
    private static final char[] hex = "0123456789abcdef".toCharArray();
    private static long saltcounter = 0L;
    private static String toHex(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            out[i * 2] = hex[(bytes[i] >> 4) & 0xF];
            out[i * 2 + 1] = hex[bytes[i] & 0xF];
        }
        return new String(out);
    }
    private static byte[] newSalt() {
        SHA256Digest digest = new SHA256Digest();
        long[] parts = {
            Double.doubleToLongBits(Math.random()),
            Double.doubleToLongBits(Math.random()),
            Double.doubleToLongBits(Math.random()),
            Double.doubleToLongBits(Math.random()),
            EagRuntime.nanoTime(),
            EagRuntime.steadyTimeMillis(),
            ++saltcounter
        };
        byte[] buf = new byte[parts.length * 8];
        for (int i = 0; i < parts.length; i++) {
            for (int j = 0; j < 8; j++) buf[i * 8 + j] = (byte) (parts[i] >>> (56 - j * 8));
        }
        digest.update(buf, 0, buf.length);
        byte[] full = new byte[32];
        digest.doFinal(full, 0);
        byte[] salt = new byte[16];
        System.arraycopy(full, 0, salt, 0, 16);
        return salt;
    }
    private static String hashPassword(byte[] salt, String password) {
        byte[] pw = password.getBytes(StandardCharsets.UTF_8);
        byte[] data = new byte[salt.length + pw.length];
        System.arraycopy(salt, 0, data, 0, salt.length);
        System.arraycopy(pw, 0, data, salt.length, pw.length);
        SHA256Digest digest = new SHA256Digest();
        digest.update(data, 0, data.length);
        byte[] out = new byte[32];
        digest.doFinal(out, 0);
        return toHex(out);
    }
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) diff |= a.charAt(i) ^ b.charAt(i);
        return diff == 0;
    }
    public static synchronized boolean isRegistered(String username) {
        return username != null && passwords.containsKey(username.toLowerCase());
    }
    public static synchronized boolean checkPassword(String username, String password) {
        if (username == null || password == null) return false;
        String[] stored = passwords.get(username.toLowerCase());
        if (stored == null) return false;
        try {
            byte[] salt = Base64.decodeBase64(stored[0]);
            return constantTimeEquals(hashPassword(salt, password), stored[1]);
        } catch (Throwable t) {
            return false;
        }
    }
    public static synchronized void registerUser(String username, String password) {
        registerUser(username, null, password);
    }
    public static synchronized void registerUser(String username, String uuid, String password) {
        if (username == null || password == null) return;
        byte[] salt = newSalt();
        passwords.put(username.toLowerCase(), new String[] {
            Base64.encodeBase64String(salt), hashPassword(salt, password), uuid == null ? "" : uuid });
        saveSafe();
    }
    public static synchronized void unregisterUser(String username) {
        if (username == null) return;
        passwords.remove(username.toLowerCase());
        loggedinusers.remove(username.toLowerCase());
        saveSafe();
    }
    public static synchronized boolean isLoggedIn(String username) {
        return username != null && loggedinusers.contains(username.toLowerCase());
    }
    public static synchronized void setLoggedIn(String username, boolean loggedin) {
        if (username == null) return;
        if (loggedin) {
            loggedinusers.add(username.toLowerCase());
        } else {
            loggedinusers.remove(username.toLowerCase());
        }
    }
    public static synchronized String getAuthTitle() {
        if (authtitle == null) {
            authtitle = StatCollector.translateToLocal("actx.auth.title").replace("%/", "%");
        }
        return authtitle;
    }
    public static synchronized String getAuthSubtitle() {
        if (authsubtitle == null) {
            authsubtitle = StatCollector.translateToLocal("actx.auth.subtitle").replace("%/", "%");
        }
        return authsubtitle;
    }
    public static synchronized void setAuthTitle(String titlejson) {
        if (titlejson == null) return;
        authtitle = titlejson.replace("%/", "%");
        saveSafe();
    }
    public static synchronized void setAuthSubtitle(String subtitlejson) {
        if (subtitlejson == null) return;
        authsubtitle = subtitlejson.replace("%/", "%");
        saveSafe();
    }
    public static synchronized void resetAuthTitles() {
        authtitle = StatCollector.translateToLocal("actx.auth.title").replace("%/", "%");
        authsubtitle = StatCollector.translateToLocal("actx.auth.subtitle").replace("%/", "%");
        saveSafe();
    }
    public static synchronized String getTabHeader() {
        if (tabheader == null) {
            tabheader = normalizeNewlines(StatCollector.translateToLocal("actx.tab.header"));
        }
        return tabheader;
    }
    public static synchronized String getTabFooter() {
        if (tabfooter == null) {
            tabfooter = normalizeNewlines(StatCollector.translateToLocal("actx.tab.footer"));
        }
        return tabfooter;
    }
    public static synchronized void setTabHeader(String header) {
        if (header == null) return;
        tabheader = normalizeNewlines(header);
        saveSafe();
    }
    public static synchronized void setTabFooter(String footer) {
        if (footer == null) return;
        tabfooter = normalizeNewlines(footer);
        saveSafe();
    }
    public static synchronized void resetTabHeaderFooter() {
        tabheader = normalizeNewlines(StatCollector.translateToLocal("actx.tab.header"));
        tabfooter = normalizeNewlines(StatCollector.translateToLocal("actx.tab.footer"));
        saveSafe();
    }
    private static String normalizeNewlines(String s) {
        if (s == null) return null;
        return s.replace("\\n", "\n").replace("%/", "%");
    }
    public static final class SavedObjective {
        public final String name;
        public final String displayname;
        public final String criteria;
        public final String rendertype;
        public final List<String> slots;
        public final Map<String, Integer> scores;
        public final List<String> hidden;
        public SavedObjective(String name, String displayname, String criteria, String rendertype,
                List<String> slots, Map<String, Integer> scores, List<String> hidden) {
            this.name = name == null ? "" : name;
            this.displayname = displayname == null ? "" : displayname;
            this.criteria = criteria == null ? "" : criteria;
            this.rendertype = rendertype == null || rendertype.isEmpty() ? "INTEGER" : rendertype;
            this.slots = slots == null ? new ArrayList<String>() : slots;
            this.scores = scores == null ? new LinkedHashMap<String, Integer>() : scores;
            this.hidden = hidden == null ? new ArrayList<String>() : hidden;
        }
    }
    private static final Map<String, SavedObjective> savedobjectives = new LinkedHashMap<String, SavedObjective>();
    public static String normalizeSavedKey(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s", "-");
    }
    public static synchronized void saveObjective(String key, SavedObjective objective) {
        ensureLoaded();
        savedobjectives.put(normalizeSavedKey(key), objective);
        saveSafe();
    }
    public static synchronized SavedObjective getSavedObjective(String key) {
        ensureLoaded();
        String k = normalizeSavedKey(key);
        SavedObjective exact = savedobjectives.get(k);
        if (exact != null) return exact;
        for (Map.Entry<String, SavedObjective> e : savedobjectives.entrySet()) {
            if (e.getKey().equalsIgnoreCase(k)) return e.getValue();
        }
        return null;
    }
    public static synchronized List<String> getSavedObjectiveKeys() {
        ensureLoaded();
        return new ArrayList<String>(savedobjectives.keySet());
    }
    private static void readSavedObjectives(Object o) {
        Map<String, Object> map = asMap(o);
        if (map == null) return;
        savedobjectives.clear();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            Map<String, Object> m = asMap(e.getValue());
            if (m == null) continue;
            String name = asString(m.get("name"));
            Map<String, Integer> scores = new LinkedHashMap<String, Integer>();
            Map<String, Object> rawscores = asMap(m.get("scores"));
            if (rawscores != null) {
                for (Map.Entry<String, Object> se : rawscores.entrySet()) {
                    String val = asString(se.getValue());
                    if (val == null) continue;
                    try { scores.put(se.getKey(), Integer.valueOf(val.trim())); } catch (NumberFormatException ignored) { }
                }
            }
            List<String> slots = asStringList(m.get("slots"));
            List<String> hidden = asStringList(m.get("hidden"));
            savedobjectives.put(normalizeSavedKey(e.getKey()), new SavedObjective(
                    name == null || name.isEmpty() ? e.getKey() : name,
                    asString(m.get("display_name")), asString(m.get("criteria")), asString(m.get("render_type")),
                    slots, scores, hidden));
        }
    }
    private static void writeSavedObjectives(StringBuilder sb) {
        sb.append("scoreboards:");
        if (savedobjectives.isEmpty()) {
            sb.append(" {}\n");
            return;
        }
        sb.append('\n');
        for (Map.Entry<String, SavedObjective> e : savedobjectives.entrySet()) {
            SavedObjective o = e.getValue();
            sb.append("  ").append(yamlKey(e.getKey())).append(":\n");
            putScalar(sb, 2, "name", o.name);
            putScalar(sb, 2, "display_name", o.displayname);
            putScalar(sb, 2, "criteria", o.criteria);
            putScalar(sb, 2, "render_type", o.rendertype);
            putList(sb, 2, "slots", o.slots);
            if (o.scores.isEmpty()) {
                sb.append("    scores: {}\n");
            } else {
                sb.append("    scores:\n");
                for (Map.Entry<String, Integer> se : o.scores.entrySet()) {
                    sb.append("      ").append(yamlKey(se.getKey())).append(": ").append(se.getValue().intValue()).append('\n');
                }
            }
            putList(sb, 2, "hidden", o.hidden);
        }
    }
    public static synchronized boolean isBossBarEnabled() {
        return bossbarenabled;
    }
    public static synchronized void setBossBarEnabled(boolean enabled) {
        bossbarenabled = enabled;
        saveSafe();
    }
    public static synchronized boolean isBossBarPreview() {
        return bossbarpreview;
    }
    public static synchronized void setBossBarPreview(boolean preview) {
        bossbarpreview = preview;
        saveSafe();
    }
    public static synchronized String getBossBarTitle() {
        return bossbartitle == null ? "" : bossbartitle;
    }
    public static synchronized void setBossBarTitle(String title) {
        bossbartitle = title == null ? "" : title.replace("\r", " ").replace("\n", " ");
        saveSafe();
    }
    public static synchronized void resetBossBarOptions() {
        bossbarenabled = true;
        bossbarpreview = true;
        bossbartitle = "";
        saveSafe();
    }
    public static synchronized String getJoinMessage() { return joinmessage; }
    public static synchronized String getLeaveMessage() { return leavemessage; }
    public static synchronized String getChatFormat() { return chatformat; }
    public static synchronized void setJoinMessage(String s) {
        joinmessage = s != null ? s.replace("%/", "%") : null;
        saveSafe();
    }
    public static synchronized void setLeaveMessage(String s) {
        leavemessage = s != null ? s.replace("%/", "%") : null;
        saveSafe();
    }
    public static synchronized void setChatFormat(String s) {
        chatformat = s != null ? s.replace("%/", "%") : null;
        saveSafe();
    }
    private static String replaceIgnoreCase(String source, String placeholder, String replacement) {
        return Pattern.compile(Pattern.quote(placeholder), Pattern.CASE_INSENSITIVE)
                .matcher(source)
                .replaceAll(Matcher.quoteReplacement(replacement));
    }
    public static synchronized String formatJoin(String playername) {
        return replaceIgnoreCase(joinmessage.replace("%/", "%"), "%player%", playername);
    }
    public static synchronized String formatLeave(String playername) {
        return replaceIgnoreCase(leavemessage.replace("%/", "%"), "%player%", playername);
    }
    public static synchronized String formatChat(String playername, String message) {
        String result = replaceIgnoreCase(chatformat.replace("%/", "%"), "%player%", playername);
        return replaceIgnoreCase(result, "%message%", message);
    }
    private static ScorePlayerTeam resolvePlayerTeam(EntityPlayerMP player) {
        try {
            return player.worldObj.getScoreboard().getPlayersTeam(player.getName());
        } catch (Exception e) {
            return null;
        }
    }
    private static final Map<String, Long> jointimestamps = new HashMap<String, Long>();
    public static synchronized void markPlayerJoined(EntityPlayerMP player) {
        jointimestamps.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
    }
    public static synchronized void clearPlayerSession(EntityPlayerMP player) {
        jointimestamps.remove(player.getName());
    }
    private static String formatSessionDuration(EntityPlayerMP player) {
        Long joinedat = jointimestamps.get(player.getName());
        if (joinedat == null) {
            return "0s";
        }
        long elapsedms = System.currentTimeMillis() - joinedat.longValue();
        long totalseconds = elapsedms / 1000L;
        long hours = totalseconds / 3600L;
        long minutes = (totalseconds % 3600L) / 60L;
        long seconds = totalseconds % 60L;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return seconds + "s";
        }
    }
    private static String applyPlayerPlaceholders(String template, EntityPlayerMP player) {
        String playername = player.getName();
        ScorePlayerTeam team = resolvePlayerTeam(player);
        String prefix = team != null ? team.getColorPrefix() : "";
        String suffix = team != null ? team.getColorSuffix() : "";
        String displayname = ScorePlayerTeam.formatPlayerName(team, playername);
        String biome;
        try {
            biome = player.worldObj.getBiomeGenForCoords(player.getPosition()).biomeName;
        } catch (Exception e) {
            biome = "Unknown";
        }
        String world;
        try {
            world = player.worldObj.provider.getDimensionName();
        } catch (Exception e) {
            world = "Unknown";
        }
        String gamemode;
        try {
            gamemode = player.theItemInWorldManager.getGameType().getName();
        } catch (Exception e) {
            gamemode = "Unknown";
        }
        String time = formatSessionDuration(player);
        String result = template.replace("%/", "%");
        result = replaceIgnoreCase(result, "%player%", playername);
        result = replaceIgnoreCase(result, "%prefix%", prefix);
        result = replaceIgnoreCase(result, "%suffix%", suffix);
        result = replaceIgnoreCase(result, "%displayname%", displayname);
        result = replaceIgnoreCase(result, "%display_name%", displayname);
        result = replaceIgnoreCase(result, "%biome%", biome);
        result = replaceIgnoreCase(result, "%world%", world);
        result = replaceIgnoreCase(result, "%gamemode%", gamemode);
        result = replaceIgnoreCase(result, "%time%", time);
        return result;
    }
    public static synchronized String formatJoin(EntityPlayerMP player) {
        return applyPlayerPlaceholders(joinmessage, player);
    }
    public static synchronized String formatLeave(EntityPlayerMP player) {
        return applyPlayerPlaceholders(leavemessage, player);
    }
    public static synchronized String formatChat(EntityPlayerMP player, String message) {
        String result = applyPlayerPlaceholders(chatformat, player);
        return replaceIgnoreCase(result, "%message%", message);
    }
    public static IChatComponent formatChatComponent(String playername, String message) {
        return parseLegacyTextToComponent(formatChat(playername, message));
    }
    public static IChatComponent formatChatComponent(EntityPlayerMP player, String message) {
        return parseLegacyTextToComponent(formatChat(player, message));
    }
    public static IChatComponent parseLegacyTextToComponent(String text) {
        IChatComponent root = new ChatComponentText("");
        if (text == null || text.isEmpty()) {
            return root;
        }
        String[] parts = text.split("\u00a7");
        ChatStyle currentstyle = new ChatStyle();
        if (!parts[0].isEmpty()) {
            IChatComponent first = new ChatComponentText(parts[0]);
            first.setChatStyle(currentstyle.createDeepCopy());
            root.appendSibling(first);
        }
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            char code = part.charAt(0);
            String content = part.substring(1);
            EnumChatFormatting formatting = getFormattingByCode(code);
            if (formatting != null) {
                if (formatting == EnumChatFormatting.RESET) {
                    currentstyle = new ChatStyle();
                } else if (formatting.isColor()) {
                    currentstyle = new ChatStyle();
                    currentstyle.setColor(formatting);
                } else if (formatting.isFancyStyling()) {
                    switch (formatting) {
                        case BOLD: currentstyle.setBold(Boolean.TRUE); break;
                        case ITALIC: currentstyle.setItalic(Boolean.TRUE); break;
                        case UNDERLINE: currentstyle.setUnderlined(Boolean.TRUE); break;
                        case STRIKETHROUGH: currentstyle.setStrikethrough(Boolean.TRUE); break;
                        case OBFUSCATED: currentstyle.setObfuscated(Boolean.TRUE); break;
                        default: break;
                    }
                }
            }
            if (!content.isEmpty()) {
                IChatComponent child = new ChatComponentText(content);
                child.setChatStyle(currentstyle.createDeepCopy());
                root.appendSibling(child);
            }
        }
        return root;
    }
    private static EnumChatFormatting getFormattingByCode(char code) {
        char lowercode = Character.toLowerCase(code);
        for (EnumChatFormatting formatting : EnumChatFormatting.values()) {
            String str = formatting.toString();
            if (str != null && str.length() >= 2 && Character.toLowerCase(str.charAt(1)) == lowercode) {
                return formatting;
            }
        }
        return null;
    }
    public static synchronized List<String> getSavedItems() {
        return new ArrayList<String>(saveditems);
    }
    public static synchronized boolean addSavedItemIfAbsent(String command) {
        if (command == null || command.trim().isEmpty()) return false;
        String trimmed = command.trim();
        if (saveditems.contains(trimmed)) return false;
        saveditems.add(trimmed);
        saveSafe();
        return true;
    }
    public static synchronized int addSavedItemsIfAbsent(List<String> commands) {
        if (commands == null) return 0;
        int added = 0;
        for (String command : commands) {
            if (command == null || command.trim().isEmpty()) continue;
            String trimmed = command.trim();
            if (!saveditems.contains(trimmed)) {
                saveditems.add(trimmed);
                added++;
            }
        }
        if (added > 0) saveSafe();
        return added;
    }
    public static synchronized void insertSavedItem(int index, String command) {
        if (command == null) return;
        int clamped = Math.max(0, Math.min(index, saveditems.size()));
        saveditems.add(clamped, command);
        saveSafe();
    }
    public static synchronized void removeSavedItem(int index) {
        if (index < 0 || index >= saveditems.size()) return;
        saveditems.remove(index);
        saveSafe();
    }
    public static synchronized void migrateLegacySavedItems(List<String> legacyitems) {
        if (legacyitems == null || legacyitems.isEmpty()) return;
        addSavedItemsIfAbsent(legacyitems);
    }
    public static synchronized List<String> getSavedLanNames() {
        return new ArrayList<String>(savedlannames);
    }
    public static synchronized boolean addSavedLanNameIfAbsent(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String trimmed = name.trim();
        if (savedlannames.contains(trimmed)) return false;
        savedlannames.add(trimmed);
        saveSafe();
        return true;
    }
    public static synchronized void removeSavedLanName(int index) {
        if (index < 0 || index >= savedlannames.size()) return;
        savedlannames.remove(index);
        saveSafe();
    }
    public static synchronized void migrateLegacySavedLanNames(List<String> legacynames) {
        if (legacynames == null || legacynames.isEmpty()) return;
        boolean changed = false;
        for (String name : legacynames) {
            if (name == null) continue;
            String trimmed = name.trim();
            if (!trimmed.isEmpty() && !savedlannames.contains(trimmed)) {
                savedlannames.add(trimmed);
                changed = true;
            }
        }
        if (changed) saveSafe();
    }
    public static synchronized Set<String> getBlockedCommands() {
        return new LinkedHashSet<String>(blockedcommands);
    }
    public static synchronized boolean isCommandBlocked(String commandname) {
        if (commandname == null) return false;
        return blockedcommands.contains(commandname.trim().toLowerCase());
    }
    public static synchronized void addBlockedCommand(String commandname, ServerConfigurationManager scm) {
        if (commandname == null) return;
        String clean = commandname.trim().toLowerCase();
        if (clean.isEmpty()) return;
        if (blockedcommands.add(clean)) {
            save(scm);
        }
    }
    public static synchronized void removeBlockedCommand(String commandname, ServerConfigurationManager scm) {
        if (commandname == null) return;
        String clean = commandname.trim().toLowerCase();
        if (blockedcommands.remove(clean)) {
            save(scm);
        }
    }
    public static synchronized byte[] getExportBytes() {
        byte[] ondisk = readBytes();
        if (ondisk != null && ondisk.length > 0) return ondisk;
        ensureLoaded();
        return buildFileContent().getBytes(StandardCharsets.UTF_8);
    }
    public static synchronized void replaceFileWith(byte[] raw, ServerConfigurationManager scm) {
        if (raw == null || raw.length == 0) return;
        writeBytes(raw);
        reloadFrom(raw, scm);
    }
    public static synchronized void wipeAll(ServerConfigurationManager scm) {
        new VFile2(file_name).delete();
        reloadFrom(null, scm);
    }
    private static long exportrequestedat = 0L;
    public static synchronized void requestExport() {
        try {
            SingleplayerServerController.requestMiscDataExport();
            exportrequestedat = EagRuntime.steadyTimeMillis();
        } catch (Throwable t) {
            exportrequestedat = 0L;
            exportLocal();
        }
    }
    public static synchronized void onExportReply(byte[] data) {
        if (exportrequestedat == 0L) return;
        exportrequestedat = 0L;
        if (data != null && data.length > 0) EagRuntime.downloadFileWithName(file_name, data);
        else exportLocal();
    }
    public static synchronized void tickExport() {
        if (exportrequestedat != 0L && EagRuntime.steadyTimeMillis() - exportrequestedat > 1500L) {
            exportrequestedat = 0L;
            exportLocal();
        }
    }
    private static void exportLocal() {
        byte[] bytes = getExportBytes();
        if (bytes != null && bytes.length > 0) EagRuntime.downloadFileWithName(file_name, bytes);
    }
    public static synchronized void reloadFrom(byte[] raw, ServerConfigurationManager scm) {
        resetToDefaults();
        if (raw != null && raw.length > 0) parseYamlInto(raw);
        if (scm != null) syncOpsToScm(scm);
        loaded = true;
    }
    private static void resetToDefaults() {
        joinmessage = default_join;
        leavemessage = default_leave;
        chatformat = default_chat;
        authtitle = null;
        authsubtitle = null;
        tabheader = null;
        tabfooter = null;
        bossbarenabled = true;
        bossbarpreview = true;
        bossbartitle = "";
        ops.clear();
        opuuids.clear();
        savedobjectives.clear();
        session_bans.clear();
        passwords.clear();
        saveditems.clear();
        savedlannames.clear();
        blockedcommands.clear();
        animations.clear();
    }
    private static byte[] readBytes() {
        VFile2 file = new VFile2(file_name);
        return file.exists() ? file.getAllBytes() : null;
    }
    private static void writeBytes(byte[] data) {
        VFile2 file = new VFile2(file_name);
        file.setAllBytes(data);
    }
    private static final class MiniYaml {
        private static final class Line {
            final int indent;
            final String text;
            Line(int indent, String text) { this.indent = indent; this.text = text; }
        }
        static Object parse(String text) {
            text = text.replace("\r\n", "\n").replace('\r', '\n');
            if (text.startsWith("\uFEFF")) text = text.substring(1);
            List<Line> lines = new ArrayList<Line>();
            for (String raw : text.split("\n", -1)) {
                String stripped = stripComment(raw);
                String trimmed = stripped.trim();
                if (trimmed.isEmpty() || trimmed.equals("---")) continue;
                int indent = 0;
                while (indent < stripped.length() && stripped.charAt(indent) == ' ') indent++;
                lines.add(new Line(indent, trimmed));
            }
            if (lines.isEmpty()) return null;
            int[] pos = { 0 };
            return parseNode(lines, pos, lines.get(0).indent);
        }
        private static boolean tokenStart(String s, int i) {
            if (i == 0) return true;
            char p = s.charAt(i - 1);
            return p == ' ' || p == '\t' || p == '[' || p == ',' || p == '{';
        }
        private static String stripComment(String s) {
            boolean dq = false, sq = false;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (dq) {
                    if (c == '\\') i++;
                    else if (c == '"') dq = false;
                    continue;
                }
                if (sq) {
                    if (c == '\'') {
                        if (i + 1 < s.length() && s.charAt(i + 1) == '\'') i++;
                        else sq = false;
                    }
                    continue;
                }
                if (c == '"' && tokenStart(s, i)) dq = true;
                else if (c == '\'' && tokenStart(s, i)) sq = true;
                else if (c == '#' && (i == 0 || Character.isWhitespace(s.charAt(i - 1)))) return s.substring(0, i);
            }
            return s;
        }
        private static boolean isListItem(String t) {
            return t.equals("-") || t.startsWith("- ");
        }
        private static Object parseNode(List<Line> lines, int[] pos, int indent) {
            if (isListItem(lines.get(pos[0]).text)) return parseList(lines, pos, indent);
            return parseMap(lines, pos, indent);
        }
        private static List<Object> parseList(List<Line> lines, int[] pos, int indent) {
            List<Object> list = new ArrayList<Object>();
            while (pos[0] < lines.size()) {
                Line l = lines.get(pos[0]);
                if (l.indent != indent || !isListItem(l.text)) break;
                String rest = l.text.length() > 1 ? l.text.substring(1).trim() : "";
                pos[0]++;
                if (rest.isEmpty()) {
                    if (pos[0] < lines.size() && lines.get(pos[0]).indent > indent) {
                        list.add(parseNode(lines, pos, lines.get(pos[0]).indent));
                    } else {
                        list.add("");
                    }
                } else {
                    list.add(parseInline(rest));
                }
            }
            return list;
        }
        private static Map<String, Object> parseMap(List<Line> lines, int[] pos, int indent) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            while (pos[0] < lines.size()) {
                Line l = lines.get(pos[0]);
                if (l.indent < indent) break;
                if (l.indent > indent || isListItem(l.text)) { pos[0]++; continue; }
                int colon = findKeyColon(l.text);
                if (colon < 0) { pos[0]++; continue; }
                Object keyobj = parseInline(l.text.substring(0, colon).trim());
                String key = keyobj instanceof String ? (String) keyobj : l.text.substring(0, colon).trim();
                String rest = l.text.substring(colon + 1).trim();
                pos[0]++;
                if (rest.isEmpty()) {
                    Object child = null;
                    if (pos[0] < lines.size()) {
                        Line n = lines.get(pos[0]);
                        if (n.indent > indent) child = parseNode(lines, pos, n.indent);
                        else if (n.indent == indent && isListItem(n.text)) child = parseList(lines, pos, indent);
                    }
                    map.put(key, child);
                } else {
                    map.put(key, parseInline(rest));
                }
            }
            return map;
        }
        private static int findKeyColon(String t) {
            int i = 0;
            char first = t.charAt(0);
            if (first == '"' || first == '\'') {
                i = 1;
                while (i < t.length()) {
                    char c = t.charAt(i);
                    if (first == '"' && c == '\\') { i += 2; continue; }
                    if (c == first) {
                        if (first == '\'' && i + 1 < t.length() && t.charAt(i + 1) == '\'') { i += 2; continue; }
                        break;
                    }
                    i++;
                }
                i++;
                while (i < t.length() && t.charAt(i) == ' ') i++;
                return (i < t.length() && t.charAt(i) == ':') ? i : -1;
            }
            for (; i < t.length(); i++) {
                if (t.charAt(i) == ':' && (i + 1 == t.length() || t.charAt(i + 1) == ' ')) return i;
            }
            return -1;
        }
        private static Object parseInline(String s) {
            if (s.startsWith("\"")) return parseDoubleQuoted(s);
            if (s.startsWith("'")) return parseSingleQuoted(s);
            if (s.equals("[]")) return new ArrayList<Object>();
            if (s.equals("{}")) return new LinkedHashMap<String, Object>();
            if (s.startsWith("[") && s.endsWith("]")) return parseFlowList(s.substring(1, s.length() - 1));
            return s;
        }
        private static String parseDoubleQuoted(String s) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '"') break;
                if (c == '\\' && i + 1 < s.length()) {
                    char n = s.charAt(++i);
                    switch (n) {
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
                        case 'r': sb.append('\r'); break;
                        case '0': sb.append('\0'); break;
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'u':
                            if (i + 4 < s.length()) {
                                try {
                                    sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                                    i += 4;
                                } catch (NumberFormatException e) {
                                    sb.append("\\u");
                                }
                            } else {
                                sb.append("\\u");
                            }
                            break;
                        default: sb.append('\\').append(n); break;
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
        private static String parseSingleQuoted(String s) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '\'') {
                    if (i + 1 < s.length() && s.charAt(i + 1) == '\'') { sb.append('\''); i++; }
                    else break;
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
        private static List<Object> parseFlowList(String inner) {
            List<Object> out = new ArrayList<Object>();
            int n = inner.length();
            int i = 0;
            while (i < n) {
                while (i < n && (inner.charAt(i) == ' ' || inner.charAt(i) == ',')) i++;
                if (i >= n) break;
                char c = inner.charAt(i);
                int end;
                if (c == '"') {
                    end = i + 1;
                    while (end < n && inner.charAt(end) != '"') { if (inner.charAt(end) == '\\') end++; end++; }
                    end++;
                } else if (c == '\'') {
                    end = i + 1;
                    while (end < n) {
                        if (inner.charAt(end) == '\'') {
                            if (end + 1 < n && inner.charAt(end + 1) == '\'') { end += 2; continue; }
                            break;
                        }
                        end++;
                    }
                    end++;
                } else {
                    end = inner.indexOf(',', i);
                    if (end < 0) end = n;
                }
                end = Math.min(end, n);
                out.add(parseInline(inner.substring(i, end).trim()));
                i = end;
            }
            return out;
        }
    }
}