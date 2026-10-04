package net.lax1dude.eaglercraft.v1_8.sp.ipc;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
public class IPCPacket29MiscData implements IPCPacketBase {
    public static final int ID = 0x29;
    public static final int OP_EXPORT_REQUEST = 0;
    public static final int OP_EXPORT_REPLY = 1;
    public static final int OP_IMPORT = 2;
    public static final int OP_WIPE = 3;
    public int op;
    public byte[] data = new byte[0];
    public IPCPacket29MiscData() {
    }
    public IPCPacket29MiscData(int op, byte[] data) {
        this.op = op;
        this.data = data == null ? new byte[0] : data;
    }
    @Override
    public void deserialize(DataInput bin) throws IOException {
        op = bin.readUnsignedByte();
        int len = bin.readInt();
        data = new byte[len];
        bin.readFully(data);
    }
    @Override
    public void serialize(DataOutput bin) throws IOException {
        bin.writeByte(op);
        bin.writeInt(data.length);
        bin.write(data);
    }
    @Override
    public int id() {
        return ID;
    }
    @Override
    public int size() {
        return 1 + 4 + data.length;
    }
}