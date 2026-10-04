package net.minecraft.network.play.server;

import net.minecraft.actx.utils.streamedmodelresolver;
import net.minecraft.actx.utils.texturesisfind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.lax1dude.eaglercraft.v1_8.opengl.ImageData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class S50PacketStreamTextures implements Packet<INetHandlerPlayClient> {

    // streaming didnt work really well :(

    private String resourcePath;
    private byte[] fileData;
    private int resourceType; // 0 = JSON model, 1 = PNG texture (Block/Item), 2 = MCMETA, 3 = Blockstate JSON

    public S50PacketStreamTextures() {}

    public S50PacketStreamTextures(String resourcePath, byte[] fileData, int resourceType) {
        this.resourcePath = resourcePath;
        this.fileData = fileData;
        this.resourceType = resourceType;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.resourceType = buf.readVarIntFromBuffer();
        this.resourcePath = buf.readStringFromBuffer(256);
        int length = buf.readVarIntFromBuffer();
        this.fileData = buf.readByteArray(length);
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.resourceType);

        String pathToSend = (this.resourcePath != null) ? this.resourcePath : "";
        if (pathToSend.length() > 256) {
            pathToSend = pathToSend.substring(0, 256);
        }
        buf.writeString(pathToSend);

        if (this.fileData != null) {
            buf.writeVarIntToBuffer(this.fileData.length);
            buf.writeByteArray(this.fileData);
        } else {
            buf.writeVarIntToBuffer(0);
        }
    }

    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        // One-way push - the client is already showing the stone fallback for
        // anything it doesn't recognize. We only need to stop a bad payload
        // from replacing that fallback with something broken, and let the
        // resolver decide when a piece has everything it depends on.
        if (!texturesisfind.isValidPayload(this.resourcePath, this.resourceType, this.fileData)) {
            System.err.println("[S50Stream] Rejected invalid payload for: " + this.resourcePath
                    + " (type " + this.resourceType + ") - keeping fallback.");
            return;
        }

        try {
            ResourceLocation location = new ResourceLocation(this.resourcePath);

            switch (this.resourceType) {
                case 1: { // PNG texture (Blocks & Items)
                    ImageData image = TextureUtil.readBufferedImage(new ByteArrayInputStream(this.fileData));
                    if (image == null) {
                        System.err.println("[S50Stream] Failed to decode image stream for: " + this.resourcePath);
                        return;
                    }
                    DynamicTexture dynamicTexture = new DynamicTexture(image);
                    Minecraft.getMinecraft().getTextureManager().loadTexture(location, dynamicTexture);
                    streamedmodelresolver.onTextureReady(location);

                    // Localization lookup only makes sense for actual textures
                    String resPath = location.getResourcePath();
                    int lastSlash = resPath.lastIndexOf('/');
                    String textureName = (lastSlash != -1 ? resPath.substring(lastSlash + 1) : resPath).replace(".png", "");
                    String prefix = resPath.contains("items/") ? "item." : "tile.";
                    String unlocalizedKey = prefix + textureName + ".name";
                    if (StatCollector.canTranslate(unlocalizedKey)) {
                        String localizedName = StatCollector.translateToLocal(unlocalizedKey);
                        System.out.println("[S50Stream] Applied local translation: " + localizedName);
                    }
                    break;
                }
                case 0: { // JSON model
                    String jsonContent = new String(this.fileData, StandardCharsets.UTF_8);
                    streamedmodelresolver.onModelReceived(location, jsonContent);
                    break;
                }
                case 3: { // Blockstate JSON
                    String blockstateContent = new String(this.fileData, StandardCharsets.UTF_8);
                    streamedmodelresolver.onBlockstateReceived(location, blockstateContent);
                    break;
                }
                case 2: { // MCMETA
                    String mcmetaContent = new String(this.fileData, StandardCharsets.UTF_8);
                    break;
                }
                default:
                    return;
            }

        } catch (Exception e) {
            System.err.println("[S50packet o algo] Error processing texture stream packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getResourcePath() { return this.resourcePath; }
    public byte[] getFileData() { return this.fileData; }
    public int getResourceType() { return this.resourceType; }
}