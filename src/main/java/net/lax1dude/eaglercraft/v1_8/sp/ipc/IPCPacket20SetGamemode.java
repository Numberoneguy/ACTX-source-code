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
 * Sent client → server to change a player's game mode.
 *
 * gameModeId values (matching WorldSettings.GameType):
 *   0 = Survival
 *   1 = Creative
 *   2 = Adventure
 *   3 = Spectator
 *
 * Handled in EaglerIntegratedServerWorker case IPCPacket20SetGamemode.ID.
 */
public class IPCPacket20SetGamemode implements IPCPacketBase {

    public static final int ID = 0x20;

    /** The username whose game mode to change. */
    public String playerName;

    /**
     * Target game mode ID.
     * 0=Survival, 1=Creative, 2=Adventure, 3=Spectator
     */
    public byte gameModeId;

    public IPCPacket20SetGamemode() {
    }

    public IPCPacket20SetGamemode(String playerName, int gameModeId) {
        this.playerName = playerName;
        this.gameModeId = (byte) gameModeId;
    }

    @Override
    public void deserialize(DataInput bin) throws IOException {
        playerName = bin.readUTF();
        gameModeId = bin.readByte();
    }

    @Override
    public void serialize(DataOutput bin) throws IOException {
        bin.writeUTF(playerName);
        bin.writeByte(gameModeId);
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
