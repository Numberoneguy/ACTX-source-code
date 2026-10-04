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
 * Sent client → server to teleport one player to another.
 *
 * Two modes driven by the single boolean {@code toTarget}:
 *
 *   toTarget == true  → tp subjectName to targetName
 *                        ("teleport player to me"  : subject=target player, target=local host)
 *                        ("teleport me to player"  : subject=local host,    target=target player)
 *
 *   toTarget == false → tp targetName to subjectName  (pull: move target to subject)
 *
 * In practice GuiOpPlayers always sets:
 *   subjectName = mc.thePlayer (the host)
 *   targetName  = the selected remote player
 *   toTarget    = true  → host TP to remote player  ("Teleport to player")
 *   toTarget    = false → remote player TP to host  ("Teleport player to you")
 *
 * The server side just does:
 *   if (toTarget)  /tp subjectName targetName
 *   else           /tp targetName subjectName
 *
 * Handled in EaglerIntegratedServerWorker case IPCPacket21TeleportPlayer.ID.
 */
public class IPCPacket21TeleportPlayer implements IPCPacketBase {

    public static final int ID = 0x21;

    /** Player who moves (or whose position is used as destination). */
    public String subjectName;

    /** Player who is the destination (or who moves). */
    public String targetName;

    /**
     * true  → subject teleports TO target.
     * false → target teleports TO subject.
     */
    public boolean toTarget;

    public IPCPacket21TeleportPlayer() {
    }

    public IPCPacket21TeleportPlayer(String subjectName, String targetName, boolean toTarget) {
        this.subjectName = subjectName;
        this.targetName  = targetName;
        this.toTarget    = toTarget;
    }

    @Override
    public void deserialize(DataInput bin) throws IOException {
        subjectName = bin.readUTF();
        targetName  = bin.readUTF();
        toTarget    = bin.readBoolean();
    }

    @Override
    public void serialize(DataOutput bin) throws IOException {
        bin.writeUTF(subjectName);
        bin.writeUTF(targetName);
        bin.writeBoolean(toTarget);
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public int size() {
        return IPCPacketBase.strLen(subjectName) + IPCPacketBase.strLen(targetName) + 1;
    }
}
