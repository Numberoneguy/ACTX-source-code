package net.lax1dude.eaglercraft.v1_8.sp.ipc;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
public class IPCPacket24HealFeedPlayer implements IPCPacketBase {
    public static final int ID = 0x24;
    public static final byte FLAG_HEAL = 0x01;
    public static final byte FLAG_FEED = 0x02;
    public String playerName;
    public byte flags;
    public IPCPacket24HealFeedPlayer() {
    }
    public IPCPacket24HealFeedPlayer(String playerName, boolean heal, boolean feed) {
        this.playerName = playerName;
        this.flags      = (byte) ((heal ? FLAG_HEAL : 0) | (feed ? FLAG_FEED : 0));
    }
    public boolean isHeal() { return (flags & FLAG_HEAL) != 0; }
    public boolean isFeed() { return (flags & FLAG_FEED) != 0; }
    @Override
    public void deserialize(DataInput bin) throws IOException {
        playerName = bin.readUTF();
        flags      = bin.readByte();
    }
    @Override
    public void serialize(DataOutput bin) throws IOException {
        bin.writeUTF(playerName);
        bin.writeByte(flags);
    }
    @Override
    public int id() {
        return ID;
    }
    @Override
    public int size() {
        return IPCPacketBase.strLen(playerName) + 1;
    }
}
