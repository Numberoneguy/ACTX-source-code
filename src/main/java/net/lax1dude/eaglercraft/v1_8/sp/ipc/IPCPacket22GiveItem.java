/*
 * Copyright (c) 2023-2024 lax1dude. All Rights Reserved.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.lax1dude.eaglercraft.v1_8.sp.ipc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Sent client → server to give a player an item stack.
 *
 * The server side executes:
 *   /give playerName itemId count damage
 *
 * itemId   — numeric item ID (e.g. 276 = diamond sword)
 * count    — stack size 1–64
 * damage   — item damage / metadata value (0 for most items)
 *
 * Handled in EaglerIntegratedServerWorker case IPCPacket22GiveItem.ID.
 */
public class IPCPacket22GiveItem implements IPCPacketBase {

    public static final int ID = 0x22;

    /** The username to give the item to. */
    public String playerName;

    /** Numeric item ID. */
    public int itemId;

    /** Stack count (1–64). */
    public byte count;

    /** Item damage / metadata. */
    public short damage;

    public IPCPacket22GiveItem() {
    }

    public IPCPacket22GiveItem(String playerName, int itemId, int count, int damage) {
        this.playerName = playerName;
        this.itemId     = itemId;
        this.count      = (byte) count;
        this.damage     = (short) damage;
    }

    @Override
    public void deserialize(DataInput bin) throws IOException {
        playerName = bin.readUTF();
        itemId     = bin.readInt();
        count      = bin.readByte();
        damage     = bin.readShort();
    }

    @Override
    public void serialize(DataOutput bin) throws IOException {
        bin.writeUTF(playerName);
        bin.writeInt(itemId);
        bin.writeByte(count);
        bin.writeShort(damage);
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public int size() {
        // strLen + int(4) + byte(1) + short(2)
        return IPCPacketBase.strLen(playerName) + 7;
    }
}
