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
import java.util.ArrayList;
import java.util.List;

public class IPCPacket17ConfigureLAN implements IPCPacketBase {
	
	public static final int ID = 0x17;
	
	public int gamemode;
	public boolean cheats;
	public final List<String> iceServers;
	public String resourcePackUrl;
	public String resourcePackHash;
	public boolean requirePack;
	public String relayUri;
	public String code;
	
	public IPCPacket17ConfigureLAN() {
		iceServers = new ArrayList<>();
		resourcePackUrl = "";
		resourcePackHash = "";
		relayUri = "";
		code = "";
	}
	
	public IPCPacket17ConfigureLAN(int gamemode, boolean cheats, List<String> iceServers) {
		this(gamemode, cheats, iceServers, "", "", false, "", "");
	}
	
	public IPCPacket17ConfigureLAN(int gamemode, boolean cheats, List<String> iceServers, String resourcePackUrl,
			String resourcePackHash, boolean requirePack) {
		this(gamemode, cheats, iceServers, resourcePackUrl, resourcePackHash, requirePack, "", "");
	}

	public IPCPacket17ConfigureLAN(int gamemode, boolean cheats, List<String> iceServers, String resourcePackUrl,
			String resourcePackHash, boolean requirePack, String relayUri, String code) {
		this.gamemode = gamemode;
		this.cheats = cheats;
		this.iceServers = iceServers;
		this.resourcePackUrl = resourcePackUrl == null ? "" : resourcePackUrl;
		this.resourcePackHash = resourcePackHash == null ? "" : resourcePackHash;
		this.requirePack = requirePack;
		this.relayUri = relayUri == null ? "" : relayUri;
		this.code = code == null ? "" : code;
	}

	@Override
	public void deserialize(DataInput bin) throws IOException {
		gamemode = bin.readUnsignedByte();
		cheats = bin.readBoolean();
		iceServers.clear();
		int iceCount = bin.readUnsignedByte();
		for(int i = 0; i < iceCount; ++i) {
			iceServers.add(bin.readUTF());
		}
		resourcePackUrl = bin.readUTF();
		resourcePackHash = bin.readUTF();
		requirePack = bin.readBoolean();
		relayUri = bin.readUTF();
		code = bin.readUTF();
	}

	@Override
	public void serialize(DataOutput bin) throws IOException {
		bin.writeByte(gamemode);
		bin.writeBoolean(cheats);
		bin.writeByte(iceServers.size());
		for(int i = 0, l = iceServers.size(); i < l; ++i) {
			bin.writeUTF(iceServers.get(i));
		}
		bin.writeUTF(resourcePackUrl);
		bin.writeUTF(resourcePackHash);
		bin.writeBoolean(requirePack);
		bin.writeUTF(relayUri);
		bin.writeUTF(code);
	}

	@Override
	public int id() {
		return ID;
	}

	@Override
	public int size() {
		int s = 0;
		for(int i = 0, l = iceServers.size(); i < l; ++i) {
			s += 2;
			s += iceServers.get(i).length();
		}
		s += 2 + resourcePackUrl.length();
		s += 2 + resourcePackHash.length();
		s += 2 + relayUri.length();
		s += 2 + code.length();
		return 2 + 1 + 1 + s;
	}

}