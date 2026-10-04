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
 * Sent client → server to kick a player by name with an optional reason.
 * Handled in EaglerIntegratedServerWorker case IPCPacket1EKickPlayer.ID.
 */
public class IPCPacket1EKickPlayer implements IPCPacketBase {

    public static final int ID = 0x1E;

    /** The username to kick. */
    public String playerName;

    /** Kick reason shown on the disconnect screen. May be empty. */
    public String reason;

    public IPCPacket1EKickPlayer() {
    }

    public IPCPacket1EKickPlayer(String playerName, String reason) {
        this.playerName = playerName;
        this.reason = reason == null ? "" : reason;
    }

    @Override
    public void deserialize(DataInput bin) throws IOException {
        playerName = bin.readUTF();
        reason     = bin.readUTF();
    }

    @Override
    public void serialize(DataOutput bin) throws IOException {
        bin.writeUTF(playerName);
        bin.writeUTF(reason);
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public int size() {
        return IPCPacketBase.strLen(playerName) + IPCPacketBase.strLen(reason);
    }
}
