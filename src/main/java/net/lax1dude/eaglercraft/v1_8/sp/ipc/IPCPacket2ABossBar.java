package net.lax1dude.eaglercraft.v1_8.sp.ipc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Client -> integrated server: boss bar settings from GuiActBossBarOptions.
 * The worker calls serverConfigurationManager.updateBossBar(pkt.enabled, pkt.title).
 */
public class IPCPacket2ABossBar implements IPCPacketBase {

    public static final int ID = 0x2A;

    public boolean enabled;
    public String title = "";

    public IPCPacket2ABossBar() {
    }

    public IPCPacket2ABossBar(boolean enabled, String title) {
        this.enabled = enabled;
        this.title = title == null ? "" : title;
    }

    @Override
    public void deserialize(DataInput bin) throws IOException {
        enabled = bin.readBoolean();
        title = bin.readUTF();
    }

    @Override
    public void serialize(DataOutput bin) throws IOException {
        bin.writeBoolean(enabled);
        bin.writeUTF(title);
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public int size() {
        return 1 + IPCPacketBase.strLen(title);
    }
}
