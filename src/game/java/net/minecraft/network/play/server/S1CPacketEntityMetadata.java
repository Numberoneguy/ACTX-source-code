package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.actx.server.actxcompat;
import net.minecraft.entity.DataWatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

/**+
 * This portion of EaglercraftX contains deobfuscated Minecraft 1.8 source code.
 * 
 * Minecraft 1.8.8 bytecode is (c) 2015 Mojang AB. "Do not distribute!"
 * Mod Coder Pack v9.18 deobfuscation configs are (c) Copyright by the MCP Team
 * 
 * EaglercraftX 1.8 patch files (c) 2022-2025 lax1dude, ayunami2000. All Rights Reserved.
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
public class S1CPacketEntityMetadata implements Packet<INetHandlerPlayClient> {
	private int entityId;
	private List<DataWatcher.WatchableObject> field_149378_b;

	public S1CPacketEntityMetadata() {
	}

	public S1CPacketEntityMetadata(int entityIdIn, DataWatcher parDataWatcher, boolean parFlag) {
		this.entityId = entityIdIn;
		if (parFlag) {
			this.field_149378_b = parDataWatcher.getAllWatched();
		} else {
			this.field_149378_b = parDataWatcher.getChanged();
		}

	}

	/**+
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer parPacketBuffer) throws IOException {
		this.entityId = parPacketBuffer.readVarIntFromBuffer();
		this.field_149378_b = DataWatcher.readWatchedListFromPacketBuffer(parPacketBuffer);
	}

	/**+
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer parPacketBuffer) throws IOException {
		parPacketBuffer.writeVarIntToBuffer(this.entityId);
		DataWatcher.writeWatchedListToPacketBuffer(this.field_149378_b, parPacketBuffer);
	}

	/**+
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient inethandlerplayclient) {
		inethandlerplayclient.handleEntityMetadata(this);
	}

	public S1CPacketEntityMetadata actxVanillaCopy() {
		if (this.field_149378_b == null) {
			return this;
		}
		boolean changed = false;
		for (int i = 0; i < this.field_149378_b.size(); ++i) {
			DataWatcher.WatchableObject object = this.field_149378_b.get(i);
			if (object.getObjectType() == 5 && object.getObject() != null
					&& actxcompat.isCustomItem(((ItemStack) object.getObject()).getItem())) {
				changed = true;
				break;
			}
		}
		if (!changed) {
			return this;
		}
		S1CPacketEntityMetadata copy = new S1CPacketEntityMetadata();
		copy.entityId = this.entityId;
		copy.field_149378_b = new ArrayList<DataWatcher.WatchableObject>(this.field_149378_b.size());
		for (int i = 0; i < this.field_149378_b.size(); ++i) {
			DataWatcher.WatchableObject object = this.field_149378_b.get(i);
			if (object.getObjectType() == 5 && object.getObject() != null) {
				ItemStack stack = actxcompat.stackForClient((ItemStack) object.getObject(), false);
				copy.field_149378_b.add(new DataWatcher.WatchableObject(5, object.getDataValueId(), stack));
			} else {
				copy.field_149378_b.add(object);
			}
		}
		return copy;
	}

	public List<DataWatcher.WatchableObject> func_149376_c() {
		return this.field_149378_b;
	}

	public int getEntityId() {
		return this.entityId;
	}
}