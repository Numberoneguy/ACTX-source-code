package net.lax1dude.eaglercraft.v1_8.sp.ipc;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
public class IPCPacket27SetAnimation implements IPCPacketBase {
    public static final int ID = 0x27;
    public String name;
    public int intervalMs;
    public String framesJoined;
    public IPCPacket27SetAnimation() {
    }
    public IPCPacket27SetAnimation(String name, int intervalMs, String framesJoined) {
        this.name = name;
        this.intervalMs = intervalMs;
        this.framesJoined = framesJoined;
    }
    @Override
    public void deserialize(DataInput bin) throws IOException {
        name = bin.readUTF();
        intervalMs = bin.readInt();
        framesJoined = bin.readUTF();
    }
    @Override
    public void serialize(DataOutput bin) throws IOException {
        bin.writeUTF(name);
        bin.writeInt(intervalMs);
        bin.writeUTF(framesJoined);
    }
    @Override
    public int id() {
        return ID;
    }
    @Override
    public int size() {
        return IPCPacketBase.strLen(name) + 4 + IPCPacketBase.strLen(framesJoined);
    }
}
