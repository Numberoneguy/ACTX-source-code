package net.lax1dude.eaglercraft.v1_8.sp.ipc;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
public class IPCPacket28RemoveAnimation implements IPCPacketBase {
    public static final int ID = 0x28;
    public String name;
    public IPCPacket28RemoveAnimation() {
    }
    public IPCPacket28RemoveAnimation(String name) {
        this.name = name;
    }
    @Override
    public void deserialize(DataInput bin) throws IOException {
        name = bin.readUTF();
    }
    @Override
    public void serialize(DataOutput bin) throws IOException {
        bin.writeUTF(name);
    }
    @Override
    public int id() {
        return ID;
    }
    @Override
    public int size() {
        return IPCPacketBase.strLen(name);
    }
}
