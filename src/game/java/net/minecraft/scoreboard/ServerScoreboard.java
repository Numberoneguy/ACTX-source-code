package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.server.MinecraftServer;

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
public class ServerScoreboard extends Scoreboard {

	private final MinecraftServer scoreboardMCServer;
	private final Set<ScoreObjective> field_96553_b = Sets.newHashSet();
	private ScoreboardSaveData scoreboardSaveData;

	/**
	 * Vanilla's scoreboard sidebar identifies each row by its literal text (the
	 * "fake player name"), and that text can now contain placeholders like
	 * %player% that resolve differently per viewer. This cache remembers, for
	 * each raw template string, what resolved text was last sent to each named
	 * player - so that when a row is removed we can send back the EXACT string
	 * that player's client currently has. Sending anything else on removal
	 * leaves a permanent ghost row, since the client can't match it.
	 *
	 * Outer key: the raw template (e.g. "Welcome %player%!").
	 * Inner key: viewer's player name.
	 * Value: the resolved string most recently sent to that viewer.
	 */
	private final Map<String, Map<String, String>> resolvedScoreNameCache = Maps.newHashMap();

	/** Hard cap matching S3CPacketUpdateScore's readStringFromBuffer(40) on the client. */
	private static final int MAX_SCORE_NAME_LENGTH = 40;

	public ServerScoreboard(MinecraftServer mcServer) {
		this.scoreboardMCServer = mcServer;
	}

	/**
	 * Resolves placeholders in {@code template} for {@code viewer}, clamps the
	 * result to the protocol's 40-character limit, remembers it in the cache
	 * keyed by (template, viewer name), and returns it. Call this whenever a
	 * CHANGE packet is about to be sent so removal can look the value back up
	 * later.
	 */
	private String resolveAndCacheScoreName(String template, EntityPlayerMP viewer) {
		String resolved = this.scoreboardMCServer.getConfigurationManager()
				.resolveTabPlaceholders(template, viewer);
		if (resolved == null) {
			resolved = template;
		}
		if (resolved.length() > MAX_SCORE_NAME_LENGTH) {
			resolved = resolved.substring(0, MAX_SCORE_NAME_LENGTH);
		}

		Map<String, String> perPlayer = this.resolvedScoreNameCache.get(template);
		if (perPlayer == null) {
			perPlayer = Maps.newHashMap();
			this.resolvedScoreNameCache.put(template, perPlayer);
		}
		perPlayer.put(viewer.getName(), resolved);
		return resolved;
	}

	/**
	 * Returns the resolved string previously sent to {@code viewer} for
	 * {@code template}, or resolves fresh if nothing was cached yet (e.g. the
	 * player joined after this entry was created and never received a CHANGE
	 * packet for it). Used right before sending a REMOVE packet, so the client
	 * can actually find and delete the row.
	 */
	private String getCachedOrResolveScoreName(String template, EntityPlayerMP viewer) {
		Map<String, String> perPlayer = this.resolvedScoreNameCache.get(template);
		String cached = perPlayer != null ? perPlayer.get(viewer.getName()) : null;
		return cached != null ? cached : this.resolveAndCacheScoreName(template, viewer);
	}

	/** Drops the cached resolved name for (template, playerName) once it's no longer displayed. */
	private void forgetCachedScoreName(String template, String playerName) {
		Map<String, String> perPlayer = this.resolvedScoreNameCache.get(template);
		if (perPlayer != null) {
			perPlayer.remove(playerName);
			if (perPlayer.isEmpty()) {
				this.resolvedScoreNameCache.remove(template);
			}
		}
	}

	/**
	 * Reads the cached resolved name for (template, playerName) WITHOUT
	 * resolving if missing - returns null in that case. Used below to tell
	 * "nothing cached yet" apart from "cached value is identical to what we
	 * just resolved", so callers only send packets when the text actually
	 * changed.
	 */
	private String peekCachedScoreName(String template, String playerName) {
		Map<String, String> perPlayer = this.resolvedScoreNameCache.get(template);
		return perPlayer != null ? perPlayer.get(playerName) : null;
	}

	/**
	 * Sends whatever packets are needed so {@code viewer} ends up seeing
	 * (template resolved for viewer) with {@code value} under {@code objective}.
	 *
	 * THE BUG THIS FIXES: vanilla's scoreboard client identifies each row by
	 * its literal displayed TEXT, not by any separate id. If a template like
	 * "ping %ping%" resolves to "ping 0" the first time and "ping 1223" the
	 * next, sending only a CHANGE packet for the new text makes the client
	 * add a SECOND row - the old "ping 0" row is never told to leave, so it
	 * sits there forever. Every subsequent refresh with a different value
	 * adds yet another permanent duplicate line, which is exactly what was
	 * seen in testing (a growing stack of "ping 0", "ping 1223", "ping 1588"...
	 * all still on screen at once).
	 *
	 * The fix: if the resolved text changed since we last sent something to
	 * this viewer, explicitly REMOVE the old text first, then CHANGE to the
	 * new one. If nothing was cached yet (brand new entry, or viewer just
	 * joined) there's nothing to remove - just send the CHANGE.
	 */
	private void sendResolvedScoreUpdate(String template, ScoreObjective objective, int value, EntityPlayerMP viewer) {
		String previous = this.peekCachedScoreName(template, viewer.getName());
		String resolved = this.resolveAndCacheScoreName(template, viewer);

		if (previous != null && !previous.equals(resolved)) {
			// Old row's text no longer matches - tell the client to drop it
			// before the new one arrives, or it becomes a permanent duplicate.
			viewer.playerNetServerHandler.sendPacket(new S3CPacketUpdateScore(previous, objective));
		}
		if (previous == null || !previous.equals(resolved)) {
			viewer.playerNetServerHandler.sendPacket(new S3CPacketUpdateScore(resolved, objective, value));
		}
		// previous != null && previous.equals(resolved): text unchanged, nothing to send.
	}

	/**
	 * Re-resolves entry text for every objective currently displayed to at
	 * least one player, and sends an updated S3CPacketUpdateScore to each
	 * viewer whose resolved text actually changed since the last call. Call
	 * this periodically (see MinecraftServer.tick(), same cadence as
	 * broadcastTabHeaderFooter) so dynamic placeholders like %tps% or %online%
	 * inside a scoreboard entry name stay live instead of only updating when
	 * the score's numeric value happens to change.
	 *
	 * Skips sending for entries whose resolved text is unchanged - the large
	 * majority of scoreboard lines have no placeholders at all, so this keeps
	 * the packet volume close to zero in the common case rather than
	 * resending every entry to every player on every call.
	 */
	public void refreshDynamicPlaceholders() {
		if (this.field_96553_b.isEmpty()) {
			return;
		}

		List<EntityPlayerMP> players = this.scoreboardMCServer.getConfigurationManager().func_181057_v();
		for (ScoreObjective objective : this.field_96553_b) {
			for (Score score : this.getSortedScores(objective)) {
				String template = score.getPlayerName();
				for (int i = 0, l = players.size(); i < l; ++i) {
					EntityPlayerMP viewer = players.get(i);
					this.sendResolvedScoreUpdate(template, objective, score.getScorePoints(), viewer);
				}
			}
		}
	}

	public void func_96536_a(Score score) {
		super.func_96536_a(score);
		if (this.field_96553_b.contains(score.getObjective())) {
			// The entry name can contain %placeholder%s (e.g. "Welcome %player%!"),
			// so each connected player needs their own resolved copy of this packet
			// rather than one packet broadcast to everyone. sendResolvedScoreUpdate
			// also removes any previously-shown text that no longer matches, so a
			// changing placeholder value doesn't leave duplicate rows behind.
			String template = score.getPlayerName();
			List<EntityPlayerMP> players = this.scoreboardMCServer.getConfigurationManager().func_181057_v();
			for (int i = 0, l = players.size(); i < l; ++i) {
				EntityPlayerMP viewer = players.get(i);
				this.sendResolvedScoreUpdate(template, score.getObjective(), score.getScorePoints(), viewer);
			}
		}

		this.func_96551_b();
	}

	public void func_96516_a(String s) {
		super.func_96516_a(s);
		// s is the raw template. Each viewer may have received a differently
		// resolved string (e.g. "Welcome Steve!" vs "Welcome Alex!"), and the
		// client can only find/remove a row by its exact displayed text - so we
		// must look up (or resolve) each viewer's specific copy before removing.
		List<EntityPlayerMP> players = this.scoreboardMCServer.getConfigurationManager().func_181057_v();
		for (int i = 0, l = players.size(); i < l; ++i) {
			EntityPlayerMP viewer = players.get(i);
			String resolvedName = this.getCachedOrResolveScoreName(s, viewer);
			viewer.playerNetServerHandler.sendPacket(new S3CPacketUpdateScore(resolvedName));
			this.forgetCachedScoreName(s, viewer.getName());
		}
		this.func_96551_b();
	}

	public void func_178820_a(String s, ScoreObjective scoreobjective) {
		super.func_178820_a(s, scoreobjective);
		// Same reasoning as func_96516_a above, scoped to one objective instead
		// of removing the entry everywhere.
		List<EntityPlayerMP> players = this.scoreboardMCServer.getConfigurationManager().func_181057_v();
		for (int i = 0, l = players.size(); i < l; ++i) {
			EntityPlayerMP viewer = players.get(i);
			String resolvedName = this.getCachedOrResolveScoreName(s, viewer);
			viewer.playerNetServerHandler.sendPacket(new S3CPacketUpdateScore(resolvedName, scoreobjective));
			this.forgetCachedScoreName(s, viewer.getName());
		}
		this.func_96551_b();
	}

	/**+
	 * 0 is tab menu, 1 is sidebar, 2 is below name
	 */
	public void setObjectiveInDisplaySlot(int i, ScoreObjective scoreobjective) {
		ScoreObjective scoreobjective1 = this.getObjectiveInDisplaySlot(i);
		super.setObjectiveInDisplaySlot(i, scoreobjective);
		if (scoreobjective1 != scoreobjective && scoreobjective1 != null) {
			if (this.func_96552_h(scoreobjective1) > 0) {
				this.scoreboardMCServer.getConfigurationManager()
						.sendPacketToAllPlayers(new S3DPacketDisplayScoreboard(i, scoreobjective));
			} else {
				this.getPlayerIterator(scoreobjective1);
			}
		}

		if (scoreobjective != null) {
			if (this.field_96553_b.contains(scoreobjective)) {
				this.scoreboardMCServer.getConfigurationManager()
						.sendPacketToAllPlayers(new S3DPacketDisplayScoreboard(i, scoreobjective));
			} else {
				this.func_96549_e(scoreobjective);
			}
		}

		this.func_96551_b();
	}

	/**+
	 * Adds a player to the given team
	 */
	public boolean addPlayerToTeam(String s, String s1) {
		if (super.addPlayerToTeam(s, s1)) {
			ScorePlayerTeam scoreplayerteam = this.getTeam(s1);
			this.scoreboardMCServer.getConfigurationManager()
					.sendPacketToAllPlayers(new S3EPacketTeams(scoreplayerteam, Arrays.asList(new String[] { s }), 3));
			this.func_96551_b();
			return true;
		} else {
			return false;
		}
	}

	/**+
	 * Removes the given username from the given ScorePlayerTeam. If
	 * the player is not on the team then an IllegalStateException
	 * is thrown.
	 */
	public void removePlayerFromTeam(String s, ScorePlayerTeam scoreplayerteam) {
		super.removePlayerFromTeam(s, scoreplayerteam);
		this.scoreboardMCServer.getConfigurationManager()
				.sendPacketToAllPlayers(new S3EPacketTeams(scoreplayerteam, Arrays.asList(new String[] { s }), 4));
		this.func_96551_b();
	}

	/**+
	 * Called when a score objective is added
	 */
	public void onScoreObjectiveAdded(ScoreObjective scoreobjective) {
		super.onScoreObjectiveAdded(scoreobjective);
		this.func_96551_b();
	}

	public void func_96532_b(ScoreObjective scoreobjective) {
		super.func_96532_b(scoreobjective);
		if (this.field_96553_b.contains(scoreobjective)) {
			this.scoreboardMCServer.getConfigurationManager()
					.sendPacketToAllPlayers(new S3BPacketScoreboardObjective(scoreobjective, 2));
		}

		this.func_96551_b();
	}

	public void func_96533_c(ScoreObjective scoreobjective) {
		super.func_96533_c(scoreobjective);
		if (this.field_96553_b.contains(scoreobjective)) {
			this.getPlayerIterator(scoreobjective);
		}

		this.func_96551_b();
	}

	/**+
	 * This packet will notify the players that this team is
	 * created, and that will register it on the client
	 */
	public void broadcastTeamCreated(ScorePlayerTeam scoreplayerteam) {
		super.broadcastTeamCreated(scoreplayerteam);
		this.scoreboardMCServer.getConfigurationManager()
				.sendPacketToAllPlayers(new S3EPacketTeams(scoreplayerteam, 0));
		this.func_96551_b();
	}

	/**+
	 * This packet will notify the players that this team is updated
	 */
	public void sendTeamUpdate(ScorePlayerTeam scoreplayerteam) {
		super.sendTeamUpdate(scoreplayerteam);
		this.scoreboardMCServer.getConfigurationManager()
				.sendPacketToAllPlayers(new S3EPacketTeams(scoreplayerteam, 2));
		this.func_96551_b();
	}

	public void func_96513_c(ScorePlayerTeam scoreplayerteam) {
		super.func_96513_c(scoreplayerteam);
		this.scoreboardMCServer.getConfigurationManager()
				.sendPacketToAllPlayers(new S3EPacketTeams(scoreplayerteam, 1));
		this.func_96551_b();
	}

	public void func_96547_a(ScoreboardSaveData parScoreboardSaveData) {
		this.scoreboardSaveData = parScoreboardSaveData;
	}

	protected void func_96551_b() {
		if (this.scoreboardSaveData != null) {
			this.scoreboardSaveData.markDirty();
		}

	}

	/**
	 * Legacy variant with no specific viewer - sends each score's RAW literal
	 * name verbatim, unresolved. Kept only for source compatibility with any
	 * external caller that doesn't have a player to resolve placeholders
	 * against. Internal callers should use the (ScoreObjective, EntityPlayerMP)
	 * overload below instead so %placeholder%s actually resolve.
	 */
	public List<Packet> func_96550_d(ScoreObjective parScoreObjective) {
		ArrayList arraylist = Lists.newArrayList();
		arraylist.add(new S3BPacketScoreboardObjective(parScoreObjective, 0));

		for (int i = 0; i < 19; ++i) {
			if (this.getObjectiveInDisplaySlot(i) == parScoreObjective) {
				arraylist.add(new S3DPacketDisplayScoreboard(i, parScoreObjective));
			}
		}

		for (Score score : this.getSortedScores(parScoreObjective)) {
			arraylist.add(new S3CPacketUpdateScore(score));
		}

		return arraylist;
	}

	/**
	 * Builds the packet list for one specific viewer, resolving any
	 * %placeholder%s present in each score's entry name (e.g. %player%, %online%)
	 * against that viewer before building the S3CPacketUpdateScore. Used both
	 * for a single joining player (ServerConfigurationManager.sendScoreboard)
	 * and, looped per-player, for broadcasting to everyone already connected
	 * (func_96549_e below).
	 */
	public List<Packet> func_96550_d(ScoreObjective parScoreObjective, EntityPlayerMP viewer) {
		ArrayList arraylist = Lists.newArrayList();
		arraylist.add(new S3BPacketScoreboardObjective(parScoreObjective, 0));

		for (int i = 0; i < 19; ++i) {
			if (this.getObjectiveInDisplaySlot(i) == parScoreObjective) {
				arraylist.add(new S3DPacketDisplayScoreboard(i, parScoreObjective));
			}
		}

		for (Score score : this.getSortedScores(parScoreObjective)) {
			String resolvedName = this.resolveAndCacheScoreName(score.getPlayerName(), viewer);
			arraylist.add(new S3CPacketUpdateScore(resolvedName, parScoreObjective, score.getScorePoints()));
		}

		return arraylist;
	}

	public void func_96549_e(ScoreObjective parScoreObjective) {
		// Each viewer needs their own resolved copy of the score entries (a
		// shared list would show the same literal %player% text to everyone),
		// so this builds and sends a fresh list per player rather than reusing
		// one list like the pre-placeholder version did.
		List<EntityPlayerMP> players = this.scoreboardMCServer.getConfigurationManager().func_181057_v();
		for (int i = 0, l = players.size(); i < l; ++i) {
			EntityPlayerMP viewer = players.get(i);
			List<Packet> list = this.func_96550_d(parScoreObjective, viewer);
			for (int j = 0, m = list.size(); j < m; ++j) {
				viewer.playerNetServerHandler.sendPacket(list.get(j));
			}
		}

		this.field_96553_b.add(parScoreObjective);
	}

	public List<Packet> func_96548_f(ScoreObjective parScoreObjective) {
		ArrayList arraylist = Lists.newArrayList();
		arraylist.add(new S3BPacketScoreboardObjective(parScoreObjective, 1));

		for (int i = 0; i < 19; ++i) {
			if (this.getObjectiveInDisplaySlot(i) == parScoreObjective) {
				arraylist.add(new S3DPacketDisplayScoreboard(i, parScoreObjective));
			}
		}

		return arraylist;
	}

	public void getPlayerIterator(ScoreObjective parScoreObjective) {
		List<Packet> list = this.func_96548_f(parScoreObjective);

		List<EntityPlayerMP> players = this.scoreboardMCServer.getConfigurationManager().func_181057_v();
		for (int i = 0, l = players.size(); i < l; ++i) {
			EntityPlayerMP entityplayermp = players.get(i);
			for (int j = 0, m = list.size(); j < m; ++j) {
				entityplayermp.playerNetServerHandler.sendPacket(list.get(j));
			}
		}

		this.field_96553_b.remove(parScoreObjective);
	}

	public int func_96552_h(ScoreObjective parScoreObjective) {
		int i = 0;

		for (int j = 0; j < 19; ++j) {
			if (this.getObjectiveInDisplaySlot(j) == parScoreObjective) {
				++i;
			}
		}

		return i;
	}
}