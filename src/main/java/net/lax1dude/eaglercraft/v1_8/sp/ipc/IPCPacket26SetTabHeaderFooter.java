package net.lax1dude.eaglercraft.v1_8.sp.ipc;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
public class IPCPacket26SetTabHeaderFooter implements IPCPacketBase {
    public static final int ID = 0x26;
    public String header;
    public String footer;
    public IPCPacket26SetTabHeaderFooter() {
    }
    public IPCPacket26SetTabHeaderFooter(String header, String footer) {
        this.header = header;
        this.footer = footer;
    }
    @Override
    public void deserialize(DataInput bin) throws IOException {
        header = bin.readUTF();
        footer = bin.readUTF();
    }
    @Override
    public void serialize(DataOutput bin) throws IOException {
        bin.writeUTF(header);
        bin.writeUTF(footer);
    }
    @Override
    public int id() {
        return ID;
    }
    @Override
    public int size() {
        return IPCPacketBase.strLen(header) + IPCPacketBase.strLen(footer);
    }
}
