package net.lax1dude.eaglercraft.v1_8.sp.ipc;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
public class IPCPacket25WandPos implements IPCPacketBase {
    public static final int ID = 0x25;
    public String playerName;
    public int x, y, z;
    public int clickType; // 1 = pos1 (left), 2 = pos2 (right)
    public IPCPacket25WandPos() {}
    public IPCPacket25WandPos(String playerName, int x, int y, int z, int clickType) {
        this.playerName = playerName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.clickType = clickType;
    }
    @Override
    public void serialize(DataOutput output) throws IOException {
        output.writeUTF(playerName);
        output.writeInt(x);
        output.writeInt(y);
        output.writeInt(z);
        output.writeByte(clickType);
    }
    @Override
    public void deserialize(DataInput input) throws IOException {
        playerName = input.readUTF();
        x = input.readInt();
        y = input.readInt();
        z = input.readInt();
        clickType = input.readByte();
    }
    @Override
    public int size() {
        return playerName.length() * 3 + 16;
    }
    @Override
    public int id() {
        return ID;
    }
}
