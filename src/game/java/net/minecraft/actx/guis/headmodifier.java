package net.minecraft.actx.guis;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.Base64;
import net.lax1dude.eaglercraft.v1_8.internal.PlatformRuntime;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import org.json.JSONArray;
import org.json.JSONObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.EditorState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.I18n;
import net.lax1dude.eaglercraft.v1_8.mojang.authlib.GameProfile;
import net.lax1dude.eaglercraft.v1_8.mojang.authlib.Property;
import com.google.common.collect.Multimap;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;
import java.util.regex.Pattern;
public class headmodifier extends GuiScreen {
    private static final int btn_my_skin = 0;
    private static final int btn_download_skin = 1;
    private static final int btn_load_by_name = 2;
    private static final int btn_load_by_link = 3;
    private static final int btn_cancel = 4;
    private static final int btn_done = 5;
    private static final Pattern uuid_pattern = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private final EditorState state;
    private final GuiScreen parent;
    private GuiTextField namefield;
    private GuiTextField uuidfield;
    private GuiTextField linkfield;
    private int labelx;
    private int namey, uuidy, linky;
    private int previewx, previewy;
    private String statusmessage = "";
    private boolean statusiserror = false;
    private long statusexpireat = 0L;
    private static final long lookup_debounce_ms = 500L;
    private boolean namedirty, uuiddirty;
    private long namedirtyat, uuiddirtyat;
    private String lastlookedupname;
    private String lastlookedupuuid;
    private volatile String pendinguuidresult;
    private volatile String pendingskinlinkresult;
    private volatile boolean lookupfailed;
    public headmodifier(EditorState state, GuiScreen parent) {
        this.state = state;
        this.parent = parent;
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        int col1 = this.width / 2 - 130;
        int col2 = this.width / 2 + 2;
        labelx = col1;
        int fieldx = col1 + 44;
        int fieldw = 210;
        int fieldh = 16;
        namey = this.height / 2 - 74;
        uuidy = namey + 22;
        linky = uuidy + 22;
        previewx = fieldx + fieldw + 6;
        previewy = uuidy;
        namefield = new GuiTextField(0, this.fontRendererObj, fieldx, namey, fieldw, fieldh);
        uuidfield = new GuiTextField(1, this.fontRendererObj, fieldx, uuidy, fieldw, fieldh);
        linkfield = new GuiTextField(2, this.fontRendererObj, fieldx, linky, fieldw, fieldh);
        namefield.setMaxStringLength(64);
        uuidfield.setMaxStringLength(64);
        linkfield.setMaxStringLength(512);
        prefillFromState();
        lastlookedupname = state.headName.isEmpty() ? null : state.headName;
        lastlookedupuuid = state.headUuid.isEmpty() ? null : state.headUuid;
        namefield.setFocused(true);
        int btnw = 130;
        int btnw2 = 129;
        int btnh = 20;
        int gridrow1 = linky + 30;
        int gridrow2 = gridrow1 + 21;
        int donerow = gridrow2 + 21;
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(btn_my_skin, col1, gridrow1, btnw, btnh, I18n.format("gui.act.modifier.head.me")));
        this.buttonList.add(new GuiButton(btn_download_skin, col2, gridrow1, btnw2, btnh, I18n.format("gui.act.modifier.head.saveSkin")));
        this.buttonList.add(new GuiButton(btn_load_by_name, col1, gridrow2, btnw, btnh, I18n.format("gui.act.modifier.head.load.name")));
        this.buttonList.add(new GuiButton(btn_load_by_link, col2, gridrow2, btnw2, btnh, I18n.format("gui.act.modifier.head.load.link")));
        this.buttonList.add(new GuiButton(btn_cancel, col1, donerow, btnw, btnh, I18n.format("gui.cancel")));
        this.buttonList.add(new GuiButton(btn_done, col2, donerow, btnw2, btnh, I18n.format("gui.done")));
    }
    private void prefillFromState() {
        if (!state.headName.isEmpty()) namefield.setText(state.headName);
        if (!state.headUuid.isEmpty()) uuidfield.setText(state.headUuid);
        if (!state.headLink.isEmpty()) linkfield.setText(state.headLink);
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        this.drawDefaultBackground();
        if (state.stack != null) {
            ItemStack preview = state.buildPreviewStack();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableDepth();
            float oldz = this.itemRender.zLevel;
            this.itemRender.zLevel = 100.0F;
            this.itemRender.renderItemAndEffectIntoGUI(preview, previewx, previewy);
            this.itemRender.renderItemOverlays(this.fontRendererObj, preview, previewx, previewy);
            this.itemRender.zLevel = oldz;
            GlStateManager.disableDepth();
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            if (mousex >= previewx && mousex < previewx + 20 && mousey >= previewy && mousey < previewy + 20) {
                this.renderToolTip(preview, mousex, mousey);
            }
        }
        this.drawString(this.fontRendererObj, I18n.format("gui.act.name"), labelx, namey + 4, 0xFFFFFF);
        this.drawString(this.fontRendererObj, I18n.format("gui.act.uuid"), labelx, uuidy + 4, 0xFFFFFF);
        this.drawString(this.fontRendererObj, I18n.format("gui.act.link"), labelx, linky + 4, 0xFFFFFF);
        namefield.drawTextBox();
        uuidfield.drawTextBox();
        linkfield.drawTextBox();
        if (!statusmessage.isEmpty()) {
            if (Minecraft.getSystemTime() < statusexpireat) {
                int color = statusiserror ? 0xFF0000 : 0x55FF55;
                this.drawCenteredString(this.fontRendererObj, statusmessage, this.width / 2, namey - 14, color);
            } else {
                statusmessage = "";
            }
        }
        super.drawScreen(mousex, mousey, partialticks);
    }
    @Override
    public void updateScreen() {
        namefield.updateCursorCounter();
        uuidfield.updateCursorCounter();
        linkfield.updateCursorCounter();
        long now = Minecraft.getSystemTime();
        if (namedirty && now - namedirtyat >= lookup_debounce_ms) {
            namedirty = false;
            String query = namefield.getText().trim();
            if (!query.isEmpty() && !query.equals(lastlookedupname)) {
                lastlookedupname = query;
                lookupOfficialUuidForName(query);
            }
        }
        if (uuiddirty && now - uuiddirtyat >= lookup_debounce_ms) {
            uuiddirty = false;
            String query = uuidfield.getText().trim();
            if (uuid_pattern.matcher(query).matches() && !query.equals(lastlookedupuuid)) {
                lastlookedupuuid = query;
                lookupSkinLinkForUuid(query);
            }
        }
        if (pendinguuidresult != null) {
            uuidfield.setText(pendinguuidresult);
            pendinguuidresult = null;
        }
        if (pendingskinlinkresult != null) {
            linkfield.setText(pendingskinlinkresult);
            pendingskinlinkresult = null;
        }
        if (lookupfailed) {
            lookupfailed = false;
            showStatus(I18n.format("gui.act.modifier.head.fileNotFound"), true);
        }
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        namefield.mouseClicked(mousex, mousey, mousebutton);
        uuidfield.mouseClicked(mousex, mousey, mousebutton);
        linkfield.mouseClicked(mousex, mousey, mousebutton);
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (keycode == 1) {
            this.mc.displayGuiScreen(parent);
            return;
        }
        if (namefield.isFocused()) {
            namefield.textboxKeyTyped(typedchar, keycode);
            namedirty = true;
            namedirtyat = Minecraft.getSystemTime();
            return;
        }
        if (uuidfield.isFocused()) {
            uuidfield.textboxKeyTyped(typedchar, keycode);
            uuiddirty = true;
            uuiddirtyat = Minecraft.getSystemTime();
            return;
        }
        if (linkfield.isFocused()) { linkfield.textboxKeyTyped(typedchar, keycode); return; }
        super.keyTyped(typedchar, keycode);
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case btn_my_skin:
                fillFromLocalPlayer();
                break;
            case btn_download_skin:
                downloadSkin();
                break;
            case btn_load_by_name:
                loadByName();
                break;
            case btn_load_by_link:
                loadByLink();
                break;
            case btn_done:
                applyAndClose();
                break;
            case btn_cancel:
                this.mc.displayGuiScreen(parent);
                break;
        }
    }
    private void fillFromLocalPlayer() {
        if (this.mc.thePlayer == null || this.mc.getNetHandler() == null) {
            showStatus(I18n.format("gui.act.modifier.head.fileNotFound"), true);
            return;
        }
        NetworkPlayerInfo info = this.mc.getNetHandler().getPlayerInfo(this.mc.thePlayer.getUniqueID());
        if (info == null || info.getGameProfile() == null) {
            showStatus(I18n.format("gui.act.modifier.head.fileNotFound"), true);
            return;
        }
        applyProfile(info.getGameProfile());
    }
    private void loadByName() {
        String query = namefield.getText().trim();
        if (query.isEmpty()) {
            showStatus(I18n.format("gui.act.modifier.head.name.warning"), true);
            return;
        }
        NetworkPlayerInfo match = findPlayerInfoByName(query);
        if (match != null && match.getGameProfile() != null) {
            applyProfile(match.getGameProfile());
        } else {
            UUID offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + query).getBytes(StandardCharsets.UTF_8));
            uuidfield.setText(offline.toString());
        }
    }
    private NetworkPlayerInfo findPlayerInfoByName(String name) {
        if (this.mc.getNetHandler() == null) return null;
        for (NetworkPlayerInfo info : this.mc.getNetHandler().getPlayerInfoMap()) {
            GameProfile p = info.getGameProfile();
            if (p != null && p.getName() != null && p.getName().equalsIgnoreCase(name)) return info;
        }
        return null;
    }
    private void applyProfile(GameProfile profile) {
        if (profile == null) return;
        if (profile.getName() != null) {
            namefield.setText(profile.getName());
        }
        if (profile.getId() != null) {
            uuidfield.setText(profile.getId().toString());
        }
        Multimap<String, Property> props = profile.getProperties();
        if (props != null && props.containsKey("textures")) {
            Collection<Property> textures = props.get("textures");
            if (textures != null && !textures.isEmpty()) {
                Property tex = textures.iterator().next();
                if (tex != null && tex.getValue() != null) {
                    String decodedurl = EditorState.extractSkinUrlFromValue(tex.getValue());
                    linkfield.setText(decodedurl != null ? decodedurl : tex.getValue());
                }
            }
        }
    }
    private void lookupOfficialUuidForName(String username) {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
        PlatformRuntime.downloadRemoteURIByteArray(url, false, arr -> {
            if (arr == null) {
                lookupfailed = true;
                return;
            }
            try {
                JSONObject obj = new JSONObject(new String(arr, StandardCharsets.UTF_8));
                String id = obj.optString("id", null);
                if (id != null && id.length() == 32) {
                    String dashed = id.substring(0, 8) + "-" + id.substring(8, 12) + "-"
                            + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20);
                    pendinguuidresult = dashed;
                } else {
                    lookupfailed = true;
                }
            } catch (Throwable t) {
                lookupfailed = true;
            }
        });
    }
    private void lookupSkinLinkForUuid(String uuid) {
        String undashed = uuid.replace("-", "");
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + undashed;
        PlatformRuntime.downloadRemoteURIByteArray(url, false, arr -> {
            if (arr == null) {
                lookupfailed = true;
                return;
            }
            try {
                JSONObject obj = new JSONObject(new String(arr, StandardCharsets.UTF_8));
                JSONArray props = obj.optJSONArray("properties");
                if (props == null) {
                    lookupfailed = true;
                    return;
                }
                for (int i = 0; i < props.length(); i++) {
                    JSONObject prop = props.getJSONObject(i);
                    if ("textures".equals(prop.optString("name", null))) {
                        String value = prop.optString("value", null);
                        if (value != null) {
                            String decodedurl = EditorState.extractSkinUrlFromValue(value);
                            pendingskinlinkresult = decodedurl != null ? decodedurl : value;
                            return;
                        }
                    }
                }
                lookupfailed = true;
            } catch (Throwable t) {
                lookupfailed = true;
            }
        });
    }
    private void loadByLink() {
        String raw = linkfield.getText().trim();
        if (raw.isEmpty()) {
            showStatus(I18n.format("gui.act.modifier.head.link.warning"), true);
            return;
        }
        String decodedurl = EditorState.extractSkinUrlFromValue(raw);
        if (decodedurl != null) {
            linkfield.setText(decodedurl);
            return;
        }
        if (!isValidSkinLink(raw)) {
            showStatus(I18n.format("gui.act.modifier.head.link.warning"), true);
            return;
        }
        linkfield.setText(raw);
    }
    private void downloadSkin() {
        String link = linkfield.getText().trim();
        if (link.isEmpty() || !isValidSkinLink(link)) {
            showStatus(I18n.format("gui.act.modifier.head.link.warning"), true);
            return;
        }
        this.setClipboardString(link);
        boolean saved = false;
        try {
            if (link.startsWith("data:") || link.startsWith("http")) {
                String filename = guessFileName(link);
                byte[] linkbytes = link.getBytes(StandardCharsets.UTF_8);
                EagRuntime.downloadFileWithName(filename, linkbytes);
                saved = true;
            }
        } catch (Throwable t) {
            saved = false;
        }
        showStatus(I18n.format(saved ? "gui.act.modifier.head.fileSaved" : "gui.act.modifier.head.fileNotFound"), !saved);
    }
    private void applyAndClose() {
        String name = namefield.getText().trim();
        String uuid = uuidfield.getText().trim();
        String link = linkfield.getText().trim();
        if (!uuid.isEmpty() && !uuid_pattern.matcher(uuid).matches()) {
            showStatus(I18n.format("gui.act.modifier.head.uuid.warning"), true);
            return;
        }
        if (!link.isEmpty() && !isValidSkinLink(link)) {
            showStatus(I18n.format("gui.act.modifier.head.link.warning"), true);
            return;
        }
        if (name.isEmpty() && uuid.isEmpty() && link.isEmpty()) {
            showStatus(I18n.format("gui.act.modifier.head.name.warning"), true);
            return;
        }
        state.headName = name;
        state.headUuid = uuid;
        state.headLink = link;
        state.headSet = true;
        this.mc.displayGuiScreen(parent);
    }
    private static boolean isValidSkinLink(String link) {
        return link.startsWith("http://") || link.startsWith("https://") || link.startsWith("eagler://") || link.startsWith("data:");
    }
    private static String guessFileName(String url) {
        String name = url;
        int slash = name.lastIndexOf('/');
        if (slash >= 0 && slash < name.length() - 1) name = name.substring(slash + 1);
        int query = name.indexOf('?');
        if (query >= 0) name = name.substring(0, query);
        if (name.isEmpty() || !name.endsWith(".png")) name = "skin.png";
        return name;
    }
    private void showStatus(String msg, boolean iserror) {
        this.statusmessage = msg;
        this.statusiserror = iserror;
        this.statusexpireat = Minecraft.getSystemTime() + 3000L;
    }
}
