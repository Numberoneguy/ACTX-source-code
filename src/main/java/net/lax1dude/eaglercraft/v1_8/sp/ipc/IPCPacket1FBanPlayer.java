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
 * Sent client → server to session-ban (or unban) a player.
 * ban == true  → add to server-side session ban list and kick immediately.
 * ban == false → remove from session ban list (unban).
 *
 * The server-side ban set lives in EaglerIntegratedServerWorker so it persists
 * for the life of the running world without touching the global ban list files.
 *
 * Handled in EaglerIntegratedServerWorker case IPCPacket1FBanPlayer.ID.
 * The login gate in NetHandlerLoginServer.tryAcceptPlayer() checks the same set.
 */
public class IPCPacket1FBanPlayer implements IPCPacketBase {

    public static final int ID = 0x1F;

    /** The username to ban or unban. */
    public String playerName;

    /** true = ban + kick, false = unban. */
    public boolean ban;

    public IPCPacket1FBanPlayer() {
    }

    public IPCPacket1FBanPlayer(String playerName, boolean ban) {
        this.playerName = playerName;
        this.ban = ban;
    }

    @Override
    public void deserialize(DataInput bin) throws IOException {
        playerName = bin.readUTF();
        ban        = bin.readBoolean();
    }

    @Override
    public void serialize(DataOutput bin) throws IOException {
        bin.writeUTF(playerName);
        bin.writeBoolean(ban);
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

