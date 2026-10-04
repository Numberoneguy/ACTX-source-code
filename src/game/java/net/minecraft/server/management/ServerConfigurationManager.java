package net.minecraft.server.management;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.lax1dude.eaglercraft.v1_8.mojang.authlib.GameProfile;
import net.lax1dude.eaglercraft.v1_8.netty.Unpooled;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.EaglercraftUUID;
import net.lax1dude.eaglercraft.v1_8.internal.vfs2.VFile2;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S41PacketServerDifficulty;
import net.minecraft.network.play.server.S44PacketWorldBorder;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.util.StatCollector;
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter;
import net.minecraft.network.play.server.S50PacketStreamTextures;
import net.minecraft.actx.utils.customblockresource;
import net.minecraft.actx.utils.customresourceregistry;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsFile;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.demo.DemoWorldManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.WorldInfo;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.GamePluginMessageProtocol;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.pkt.server.SPacketUpdateCertEAG;
import net.lax1dude.eaglercraft.v1_8.sp.server.EaglerMinecraftServer;
import net.lax1dude.eaglercraft.v1_8.sp.server.WorldsDB;
import net.lax1dude.eaglercraft.v1_8.sp.server.skins.PlayerTextureData;
import net.lax1dude.eaglercraft.v1_8.sp.server.socket.IntegratedServerPlayerNetworkManager;
import net.lax1dude.eaglercraft.v1_8.sp.server.voice.IntegratedVoiceService;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;

/**+
 * This portion of EaglercraftX contains deobfuscated Minecraft 1.8 source code.
 * * Minecraft 1.8.8 bytecode is (c) 2015 Mojang AB. "Do not distribute!"
 * Mod Coder Pack v9.18 deobfuscation configs are (c) Copyright by the MCP Team
 * * EaglercraftX 1.8 patch files (c) 2022-2025 lax1dude, ayunami2000. All Rights Reserved.
 * * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * */
public abstract class ServerConfigurationManager {
	private static final Logger logger = LogManager.getLogger();
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd \'at\' HH:mm:ss z");
	private final MinecraftServer mcServer;
	/**+
	 * A list of player entities that exist on this server.
	 */
	public final List<EntityPlayerMP> playerEntityList = Lists.newArrayList();
	private final Map<EaglercraftUUID, EntityPlayerMP> uuidToPlayerMap = Maps.newHashMap();
	private final Map<String, StatisticsFile> playerStatFiles;
	private IPlayerFileData playerNBTManagerObj;
	private boolean whiteListEnforced;
	protected int maxPlayers;
	protected int viewDistance;
	private WorldSettings.GameType gameType;
	private boolean commandsAllowedForAll;
	private int playerPingIndex;

	private WorldSettings.GameType lanGamemode = WorldSettings.GameType.SURVIVAL;
	private boolean lanCheats = false;
	private String lanRelayUri = "disconnected";
	private String lanCode = "undefined";

	/** Players opped this session via GuiOpPlayers. Cleared when the server stops. */
	private final HashSet<String> sessionOppedPlayers = new HashSet<>();

	public ServerConfigurationManager(MinecraftServer server) {
		this.playerStatFiles = Maps.newHashMap();
		this.mcServer = server;
		this.maxPlayers = 100;
	}

	public void initializeConnectionToPlayer(IntegratedServerPlayerNetworkManager netManager, EntityPlayerMP playerIn,
			GamePluginMessageProtocol protocolVersion, PlayerTextureData textureData, EaglercraftUUID clientBrandUUID) {
		playerIn.textureData = textureData;
		playerIn.clientBrandUUID = clientBrandUUID;
		NBTTagCompound nbttagcompound = this.readPlayerDataFromFile(playerIn);
		playerIn.setWorld(this.mcServer.worldServerForDimension(playerIn.dimension));
		playerIn.theItemInWorldManager.setWorld((WorldServer) playerIn.worldObj);
		String s1 = "channel:" + netManager.playerChannel;

		logger.info(playerIn.getName() + "[" + s1 + "] logged in with entity id " + playerIn.getEntityId() + " at ("
				+ playerIn.posX + ", " + playerIn.posY + ", " + playerIn.posZ + ")");
		WorldServer worldserver = this.mcServer.worldServerForDimension(playerIn.dimension);
		WorldInfo worldinfo = worldserver.getWorldInfo();
		BlockPos blockpos = worldserver.getSpawnPoint();
		this.setPlayerGameTypeBasedOnOther(playerIn, (EntityPlayerMP) null, worldserver);
		NetHandlerPlayServer nethandlerplayserver = new NetHandlerPlayServer(this.mcServer, netManager, playerIn,
				protocolVersion);
		nethandlerplayserver.sendPacket(new S01PacketJoinGame(playerIn.getEntityId(),
				playerIn.theItemInWorldManager.getGameType(), worldinfo.isHardcoreModeEnabled(),
				worldserver.provider.getDimensionId(), worldserver.getDifficulty(), this.getMaxPlayers(),
				worldinfo.getTerrainType(), worldserver.getGameRules().getBoolean("reducedDebugInfo")));
		nethandlerplayserver
				.sendPacket(new S3FPacketCustomPayload("MC|Brand", (PacketBuffer) (new PacketBuffer(Unpooled.buffer()))
						.writeString(this.getServerInstance().getServerModName())));
		nethandlerplayserver
				.sendPacket(new S41PacketServerDifficulty(worldinfo.getDifficulty(), worldinfo.isDifficultyLocked()));
		nethandlerplayserver.sendPacket(new S05PacketSpawnPosition(blockpos));
		nethandlerplayserver.sendPacket(new S39PacketPlayerAbilities(playerIn.capabilities));
		nethandlerplayserver.sendPacket(new S09PacketHeldItemChange(playerIn.inventory.currentItem));
		playerIn.getStatFile().func_150877_d();
		playerIn.getStatFile().sendAchievements(playerIn);
		this.sendScoreboard((ServerScoreboard) worldserver.getScoreboard(), playerIn);
		this.mcServer.refreshStatusNextTick();
		
		actxmiscdata.ensureLoaded();
		actxmiscdata.markPlayerJoined(playerIn);
		if (playerIn.worldObj.getGameRules().getBoolean("announceJoin")) {
			this.sendChatMsg(new ChatComponentText(
					actxmiscdata.formatJoin(playerIn)));
		}

		// Check if authentication feature is enabled via gamerule before running auth
		if (playerIn.worldObj.getGameRules().getBoolean("doAuth")) {
			boolean isHost = playerIn.mcServer.isSinglePlayer()
					&& playerIn.getName().equals(playerIn.mcServer.getServerOwner());

			if (!isHost && !actxmiscdata.isRegistered(playerIn.getName())) {
				ChatComponentText actxMsg = new ChatComponentText("§4[§7ActX§4] ");
				ChatComponentText actxMsgText = new ChatComponentText(StatCollector.translateToLocal("actx.register.tip"));
				actxMsgText.getChatStyle().setColor(EnumChatFormatting.RED);
				actxMsg.appendSibling(actxMsgText);
				playerIn.addChatMessage(actxMsg);

				// Quietly let online staff know an unregistered player just joined (will be a gamerule soon)
				for (int i = 0, l = this.playerEntityList.size(); i < l; ++i) {
					EntityPlayerMP staffMember = (EntityPlayerMP) this.playerEntityList.get(i);
					if (staffMember != playerIn && this.isOpped(staffMember.getName())) {
						ChatComponentText staffNotice = new ChatComponentText(
								"§4[§7ActX§4] §7" + playerIn.getName() + " joined unregistered.");
						staffMember.addChatMessage(staffNotice);
					}
				}
			}
		}

		// EaglercraftX tip message
		if (playerIn.canCommandSenderUseCommand(2, "give")) {
			ChatComponentText shaderF4Msg = new ChatComponentText("[EaglercraftX] ");
			shaderF4Msg.getChatStyle().setColor(EnumChatFormatting.GOLD);
			ChatComponentTranslation shaderF4Msg2 = new ChatComponentTranslation(StatCollector.translateToLocal("command.skull.tip"));
			shaderF4Msg2.getChatStyle().setColor(EnumChatFormatting.AQUA);
			shaderF4Msg.appendSibling(shaderF4Msg2);
			playerIn.addChatMessage(shaderF4Msg);
		}

		this.playerLoggedIn(playerIn);
		nethandlerplayserver.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
		this.updateTimeAndWeatherForPlayer(playerIn, worldserver);
		String rpUrl = this.mcServer.getResourcePackUrl();
		String rpHash = this.mcServer.getResourcePackHash();
		logger.info("Resource pack on join: player={} url='{}' hash='{}'", playerIn.getName(), rpUrl, rpHash);
		if (rpUrl.length() > 0) {
				try {
						playerIn.loadResourcePack(rpUrl, rpHash == null ? "" : rpHash);
				} catch (Throwable t) {
						logger.error("Failed to send resource pack to {}", playerIn.getName());
						logger.error(t);
				}
		}
		this.sendCustomStreamedResources(nethandlerplayserver);
		for (ObjectCursor<PotionEffect> potioneffect : playerIn.getActivePotionEffects()) {
			nethandlerplayserver.sendPacket(new S1DPacketEntityEffect(playerIn.getEntityId(), potioneffect.value));
		}
		playerIn.addSelfToInternalCraftingInventory();
		if (nbttagcompound != null && nbttagcompound.hasKey("Riding", 10)) {
			Entity entity = EntityList.createEntityFromNBT(nbttagcompound.getCompoundTag("Riding"), worldserver);
			if (entity != null) {
				entity.forceSpawn = true;
				worldserver.spawnEntityInWorld(entity);
				playerIn.mountEntity(entity);
				entity.forceSpawn = false;
			}
		}
	}

	protected void sendScoreboard(ServerScoreboard scoreboardIn, EntityPlayerMP playerIn) {
		java.util.Set<ScoreObjective> set = com.google.common.collect.Sets.newHashSet();
	
		for (ScorePlayerTeam scoreplayerteam : scoreboardIn.getTeams()) {
			playerIn.playerNetServerHandler.sendPacket(new S3EPacketTeams(scoreplayerteam, 0));
		}
	
		for (int i = 0; i < 19; ++i) {
			ScoreObjective scoreobjective = scoreboardIn.getObjectiveInDisplaySlot(i);
	
			if (scoreobjective != null && !set.contains(scoreobjective)) {
				java.util.List<Packet> list = scoreboardIn.func_96550_d(scoreobjective, playerIn);
	
				for (Packet packet : list) {
					playerIn.playerNetServerHandler.sendPacket(packet);
				}
	
				set.add(scoreobjective);
			}
		}
	}

	private void sendCustomStreamedResources(NetHandlerPlayServer nethandlerplayserver) {
		for (customblockresource resource : customresourceregistry.getAll()) {
			nethandlerplayserver.sendPacket(new S50PacketStreamTextures(
					resource.blockstatepath, resource.blockstatejson, 3));
			nethandlerplayserver.sendPacket(new S50PacketStreamTextures(
					resource.modelpath, resource.modeljson, 0));
			nethandlerplayserver.sendPacket(new S50PacketStreamTextures(
					resource.texturepath, resource.texturebytes, 1));
			if (resource.mcmetabytes != null) {
				nethandlerplayserver.sendPacket(new S50PacketStreamTextures(
						resource.mcmetapath, resource.mcmetabytes, 2));
			}
		}
	}

	/**+
	 * Sets the NBT manager to the one for the WorldServer given.
	 */
	public void setPlayerManager(WorldServer[] worldServers) {
		this.playerNBTManagerObj = worldServers[0].getSaveHandler().getPlayerNBTManager();
		worldServers[0].getWorldBorder().addListener(new IBorderListener() {
			public void onSizeChanged(WorldBorder worldborder, double var2) {
				ServerConfigurationManager.this.sendPacketToAllPlayers(
						new S44PacketWorldBorder(worldborder, S44PacketWorldBorder.Action.SET_SIZE));
			}

			public void onTransitionStarted(WorldBorder worldborder, double var2, double var4, long var6) {
				ServerConfigurationManager.this.sendPacketToAllPlayers(
						new S44PacketWorldBorder(worldborder, S44PacketWorldBorder.Action.LERP_SIZE));
			}

			public void onCenterChanged(WorldBorder worldborder, double var2, double var4) {
				ServerConfigurationManager.this.sendPacketToAllPlayers(
						new S44PacketWorldBorder(worldborder, S44PacketWorldBorder.Action.SET_CENTER));
			}

			public void onWarningTimeChanged(WorldBorder worldborder, int var2) {
				ServerConfigurationManager.this.sendPacketToAllPlayers(
						new S44PacketWorldBorder(worldborder, S44PacketWorldBorder.Action.SET_WARNING_TIME));
			}

			public void onWarningDistanceChanged(WorldBorder worldborder, int var2) {
				ServerConfigurationManager.this.sendPacketToAllPlayers(
						new S44PacketWorldBorder(worldborder, S44PacketWorldBorder.Action.SET_WARNING_BLOCKS));
			}

			public void onDamageAmountChanged(WorldBorder var1, double var2) {
			}

			public void onDamageBufferChanged(WorldBorder var1, double var2) {
			}
		});
	}

	public void preparePlayer(EntityPlayerMP playerIn, WorldServer worldIn) {
		WorldServer worldserver = playerIn.getServerForPlayer();
		if (worldIn != null) {
			worldIn.getPlayerManager().removePlayer(playerIn);
		}

		worldserver.getPlayerManager().addPlayer(playerIn);
		worldserver.theChunkProviderServer.loadChunk((int) playerIn.posX >> 4, (int) playerIn.posZ >> 4);
	}

	public int getEntityViewDistance() {
		return PlayerManager.getFurthestViewableBlock(this.getViewDistance());
	}

	/**+
	 * called during player login. reads the player information from
	 * disk.
	 */
	public NBTTagCompound readPlayerDataFromFile(EntityPlayerMP playerIn) {
		NBTTagCompound nbttagcompound = this.mcServer.worldServers[0].getWorldInfo().getPlayerNBTTagCompound();
		NBTTagCompound nbttagcompound1;
		if (playerIn.getName().equals(this.mcServer.getServerOwner()) && nbttagcompound != null) {
			playerIn.readFromNBT(nbttagcompound);
			nbttagcompound1 = nbttagcompound;
			logger.debug("loading single player");
		} else {
			nbttagcompound1 = this.playerNBTManagerObj.readPlayerData(playerIn);
		}

		return nbttagcompound1;
	}

	/**+
	 * also stores the NBTTags if this is an intergratedPlayerList
	 */
	protected void writePlayerData(EntityPlayerMP entityplayermp) {
		this.playerNBTManagerObj.writePlayerData(entityplayermp);
		StatisticsFile statisticsfile = (StatisticsFile) this.playerStatFiles.get(entityplayermp.getName());
		if (statisticsfile != null) {
			statisticsfile.saveStatFile();
		}

	}

	/**+
	 * Called when a player successfully logs in. Reads player data
	 * from disk and inserts the player into the world.
	 */
	public void playerLoggedIn(EntityPlayerMP playerIn) {
		this.playerEntityList.add(playerIn);
		this.uuidToPlayerMap.put(playerIn.getUniqueID(), playerIn);
		this.sendPacketToAllPlayers(new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.ADD_PLAYER,
				new EntityPlayerMP[] { playerIn }));
		WorldServer worldserver = this.mcServer.worldServerForDimension(playerIn.dimension);
		worldserver.spawnEntityInWorld(playerIn);
		this.preparePlayer(playerIn, (WorldServer) null);

		for (int i = 0; i < this.playerEntityList.size(); ++i) {
			EntityPlayerMP entityplayermp = (EntityPlayerMP) this.playerEntityList.get(i);
			playerIn.playerNetServerHandler.sendPacket(new S38PacketPlayerListItem(
					S38PacketPlayerListItem.Action.ADD_PLAYER, new EntityPlayerMP[] { entityplayermp }));
		}
		
		this.broadcastTabHeaderFooter();
	}

	private static final Pattern ANIMATION_PLACEHOLDER = Pattern.compile("%animation:([A-Za-z0-9_\\-]+)%");
	public String resolveTabPlaceholders(String template, EntityPlayerMP viewer) {
        if (template == null) return null;
        String result = template;

        // --- Server & Connection ---
        if (result.indexOf("%player%") >= 0) {
            result = result.replace("%player%", viewer.getName());
        }
		// animations that is saved (saved in animation stuffs ok)
		if (result.indexOf("%animation:") >= 0) {
			result = resolveAnimationPlaceholders(result);
		}
        if (result.indexOf("%display_name%") >= 0) {
            result = result.replace("%display_name%", viewer.getDisplayName().getFormattedText());
        }
        if (result.indexOf("%online%") >= 0) {
            result = result.replace("%online%", Integer.toString(this.playerEntityList.size()));
        }
        if (result.indexOf("%ping%") >= 0) {
            result = result.replace("%ping%", Integer.toString(viewer.ping));
        }
        if (result.indexOf("%max_players%") >= 0) {
            result = result.replace("%max_players%", Integer.toString(this.getMaxPlayers()));
        }
		if (result.indexOf("%relay%") >= 0) {
            result = result.replace("%relay%", this.lanRelayUri);
        }
        if (result.indexOf("%code%") >= 0) {
            result = result.replace("%code%", this.lanCode);
        }
		// you already know what this does
        if (result.indexOf("%online_ratio%") >= 0) {
            result = result.replace("%online_ratio%",
                    this.playerEntityList.size() + "/" + this.getMaxPlayers());
        }
		//slots_left jsut downgrades depending on how many players in the server.
        if (result.indexOf("%slots_left%") >= 0) {
            int left = this.getMaxPlayers() - this.playerEntityList.size();
            result = result.replace("%slots_left%", Integer.toString(Math.max(left, 0)));
        }
		if (result.indexOf("%staffonline%") >= 0) {
			int staffCount = 0;
			for (int i = 0, l = this.playerEntityList.size(); i < l; ++i) {
				EntityPlayerMP player = (EntityPlayerMP) this.playerEntityList.get(i);
				boolean op = this.isOpped(player.getName()) || 
						   (this.mcServer.isSinglePlayer() && player.getName().equals(this.mcServer.getServerOwner()));
				if (op) {
					staffCount++;
				}
			}
			result = result.replace("%staffonline%", Integer.toString(staffCount));
		}
		if (result.indexOf("%staff_list%") >= 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0, l = this.playerEntityList.size(); i < l; ++i) {
				EntityPlayerMP player = (EntityPlayerMP) this.playerEntityList.get(i);
				boolean op = this.isOpped(player.getName()) ||
						   (this.mcServer.isSinglePlayer() && player.getName().equals(this.mcServer.getServerOwner()));
				if (op) {
					if (sb.length() > 0) sb.append(", ");
					sb.append(player.getName());
				}
			}
			result = result.replace("%staff_list%", sb.length() > 0 ? sb.toString() : "None");
		}
        if (result.indexOf("%uptime%") >= 0) {
            result = result.replace("%uptime%", formatUptime(this.mcServer.getTickCounter()));
        }
        if (result.indexOf("%tps%") >= 0) {
            result = result.replace("%tps%", String.format("%.2f", this.mcServer.getTps()));
		}
        if (result.indexOf("%host%") >= 0) {
            String owner = this.mcServer.getServerOwner();
            result = result.replace("%host%", owner != null ? owner : "");
        }

        // player stats
        if (result.indexOf("%gamemode%") >= 0) {
            result = result.replace("%gamemode%", viewer.theItemInWorldManager.getGameType().getName());
        }
        if (result.indexOf("%health%") >= 0) {
            result = result.replace("%health%", Integer.toString(Math.round(viewer.getHealth())));
        }
		if (result.indexOf("%hp%") >= 0) {
            result = result.replace("%hp%", Integer.toString(Math.round(viewer.getHealth())));
        }
        if (result.indexOf("%max_health%") >= 0) {
            result = result.replace("%max_health%", Integer.toString(Math.round(viewer.getMaxHealth())));
        }
		if (result.indexOf("%max_hp%") >= 0) {
            result = result.replace("%max_hp%", Integer.toString(Math.round(viewer.getMaxHealth())));
        }
        if (result.indexOf("%absorption%") >= 0) {
            result = result.replace("%absorption%", Integer.toString(Math.round(viewer.getAbsorptionAmount())));
        }
        if (result.indexOf("%food%") >= 0) {
            result = result.replace("%food%", Integer.toString(viewer.getFoodStats().getFoodLevel()));
        }
		// alias
		if (result.indexOf("%hunger%") >= 0) {
            result = result.replace("%hunger%", Integer.toString(viewer.getFoodStats().getFoodLevel()));
        }
		if (result.indexOf("%saturation%") >= 0) {
			result = result.replace("%saturation%", String.format("%.1f", viewer.getFoodStats().getSaturationLevel()));
		}
        if (result.indexOf("%xp_level%") >= 0) {
            result = result.replace("%xp_level%", Integer.toString(viewer.experienceLevel));
        }
		// alias
		if (result.indexOf("%xp%") >= 0) {
			result = result.replace("%xp%", Integer.toString(viewer.experienceLevel));
		}
        if (result.indexOf("%xp_progress%") >= 0) {
            result = result.replace("%xp_progress%", Math.round(viewer.experience * 100.0F) + "%");
        }
        if (result.indexOf("%armor%") >= 0) {
            result = result.replace("%armor%", Integer.toString(viewer.getTotalArmorValue()));
        }
		// day and this will later use lang keys instead of hardcoded strings happily (hardcode strings dont matter)
		if (result.indexOf("%day%") >= 0) {
            long timeOfDay = viewer.worldObj.getWorldTime() % 24000L;
            String langKey;
            if (timeOfDay >= 0 && timeOfDay < 1000) {
                langKey = "actx.day.phase.sunrise";
            } else if (timeOfDay >= 1000 && timeOfDay < 12000) {
                langKey = "actx.day.phase.day";
            } else if (timeOfDay >= 12000 && timeOfDay < 13800) {
                langKey = "actx.day.phase.dusk";
            } else if (timeOfDay >= 13800 && timeOfDay < 22200) {
                langKey = "actx.day.phase.night";
            } else {
                langKey = "actx.day.phase.dawn";
            }
            result = result.replace("%day%", net.minecraft.util.StatCollector.translateToLocal(langKey));
        }
		if (result.indexOf("%time%") >= 0) {
			long t = viewer.worldObj.getWorldTime() % 24000L;
			int hours = (int) ((t / 1000L + 6L) % 24L);
			int minutes = (int) ((t % 1000L) * 60L / 1000L);
			result = result.replace("%time%", String.format("%02d:%02d", hours, minutes));
		}
		if (result.indexOf("%moon_phase%") >= 0) {
			int phase = (int) ((viewer.worldObj.getWorldTime() / 24000L % 8L + 8L) % 8L);
			String moonLangKey;
			switch (phase) {
				case 0:  moonLangKey = "actx.moon.phase.full"; break;
				case 1:  moonLangKey = "actx.moon.phase.waning_gibbous"; break;
				case 2:  moonLangKey = "actx.moon.phase.last_quarter"; break;
				case 3:  moonLangKey = "actx.moon.phase.waning_crescent"; break;
				case 4:  moonLangKey = "actx.moon.phase.new"; break;
				case 5:  moonLangKey = "actx.moon.phase.waxing_crescent"; break;
				case 6:  moonLangKey = "actx.moon.phase.first_quarter"; break;
				case 7:  moonLangKey = "actx.moon.phase.waxing_gibbous"; break;
				default: moonLangKey = "actx.moon.phase.full"; break;
			}
			result = result.replace("%moon_phase%", StatCollector.translateToLocal(moonLangKey));
		}
        // stats about player like potion effects and combat stats. but no combat states
		if (result.indexOf("%potions%") >= 0) {
			java.util.List<PotionEffect> effects = viewer.getActivePotionEffectsList();
			if (effects == null || effects.isEmpty()) {
				result = result.replace("%potions%", "None");
			} else {
				StringBuilder sb = new StringBuilder();
				for (PotionEffect effect : effects) {
					if (sb.length() > 0) sb.append(", ");
					sb.append(StatCollector.translateToLocal(effect.getEffectName()))
					.append(" ")
					.append(effect.getAmplifier() + 1);
				}
				result = result.replace("%potions%", sb.toString());
			}
		}
		// position and rotation
		if (result.indexOf("%x%") >= 0) {
			result = result.replace("%x%", Integer.toString(net.minecraft.util.MathHelper.floor_double(viewer.posX)));
		}
		if (result.indexOf("%y%") >= 0) {
			result = result.replace("%y%", Integer.toString(net.minecraft.util.MathHelper.floor_double(viewer.posY)));
		}
		if (result.indexOf("%z%") >= 0) {
			result = result.replace("%z%", Integer.toString(net.minecraft.util.MathHelper.floor_double(viewer.posZ)));
		}

		if (result.indexOf("%x1%") >= 0) {
			result = result.replace("%x1%", String.format("%.3f", viewer.posX));
		}
		if (result.indexOf("%y1%") >= 0) {
			result = result.replace("%y1%", String.format("%.3f", viewer.posY));
		}
		if (result.indexOf("%z1%") >= 0) {
			result = result.replace("%z1%", String.format("%.3f", viewer.posZ));
		}

		if (result.indexOf("%coords%") >= 0) {
			String coords = net.minecraft.util.MathHelper.floor_double(viewer.posX) + ", " +
							net.minecraft.util.MathHelper.floor_double(viewer.posY) + ", " +
							net.minecraft.util.MathHelper.floor_double(viewer.posZ);
			result = result.replace("%coords%", coords);
		}
		if (result.indexOf("%coords_exact%") >= 0) {
			String coordsExact = String.format("%.3f, %.3f, %.3f", viewer.posX, viewer.posY, viewer.posZ);
			result = result.replace("%coords_exact%", coordsExact);
		}

		if (result.indexOf("%facing%") >= 0) {
            float yaw = viewer.rotationYaw % 360.0F;
            if (yaw < 0) {
                yaw += 360.0F;
            }

            int index = net.minecraft.util.MathHelper.floor_double((double)(yaw * 16.0F / 360.0F) + 0.5D) & 15;

            String langKey;

            switch (index) {
                case 0:  langKey = "actx.direction.south"; break;
                case 1:  langKey = "actx.direction.south_south_west"; break;
                case 2:  langKey = "actx.direction.south_west"; break;
                case 3:  langKey = "actx.direction.west_south_west"; break;
                case 4:  langKey = "actx.direction.west"; break;
                case 5:  langKey = "actx.direction.west_north_west"; break;
                case 6:  langKey = "actx.direction.north_west"; break;
                case 7:  langKey = "actx.direction.north_north_west"; break;
                case 8:  langKey = "actx.direction.north"; break;
                case 9:  langKey = "actx.direction.north_north_east"; break;
                case 10: langKey = "actx.direction.north_east"; break;
                case 11: langKey = "actx.direction.east_north_east"; break;
                case 12: langKey = "actx.direction.east"; break;
                case 13: langKey = "actx.direction.east_south_east"; break;
                case 14: langKey = "actx.direction.south_east"; break;
                case 15: langKey = "actx.direction.south_south_east"; break;
                default: langKey = "actx.direction.south"; break;
            }

            result = result.replace("%facing%", StatCollector.translateToLocal(langKey));
        }
		// tells what biome you're in
		if (result.indexOf("%biome%") >= 0) {
            net.minecraft.util.BlockPos pos = new net.minecraft.util.BlockPos(viewer.posX, viewer.posY, viewer.posZ);
            String biomeName = viewer.worldObj.getBiomeGenForCoords(pos).biomeName;
            result = result.replace("%biome%", biomeName);
        }
		if (result.indexOf("%light%") >= 0) {
			net.minecraft.util.BlockPos lightPos = new net.minecraft.util.BlockPos(viewer.posX, viewer.posY, viewer.posZ);
			result = result.replace("%light%", Integer.toString(viewer.worldObj.getLight(lightPos)));
		}
        if (result.indexOf("%mob_kills%") >= 0) {
            int kills = viewer.getStatFile().readStat(StatList.mobKillsStat);
            result = result.replace("%mob_kills%", Integer.toString(kills));
        }
        if (result.indexOf("%player_kills%") >= 0) {
            int kills = viewer.getStatFile().readStat(StatList.playerKillsStat);
            result = result.replace("%player_kills%", Integer.toString(kills));
        }
        if (result.indexOf("%deaths%") >= 0) {
            int deaths = viewer.getStatFile().readStat(StatList.deathsStat);
            result = result.replace("%deaths%", Integer.toString(deaths));
        }

        // world and le ecosystem
        if (result.indexOf("%world%") >= 0) {
            result = result.replace("%world%", dimensionName(viewer.dimension));
        }
        if (result.indexOf("%weather%") >= 0) {
            String weather = viewer.worldObj.isThundering() ? "Storm" : viewer.worldObj.isRaining() ? "Rain" : "Clear";
            result = result.replace("%weather%", weather);
        }
        if (result.indexOf("%difficulty%") >= 0) {
            result = result.replace("%difficulty%", viewer.worldObj.getDifficulty().name());
        }
        if (result.indexOf("%player_count_dimension%") >= 0) {
            result = result.replace("%player_count_dimension%", Integer.toString(viewer.getServerForPlayer().playerEntities.size()));
        }

        // scoreboard team (team_name tells you the name fo the team but team formats everything basically)
        if (result.indexOf("%team%") >= 0) {
            ScorePlayerTeam team = viewer.getWorldScoreboard().getPlayersTeam(viewer.getName());
            result = result.replace("%team%", ScorePlayerTeam.formatPlayerName(team, viewer.getName()));
        }
        if (result.indexOf("%team_name%") >= 0) {
            ScorePlayerTeam team = viewer.getWorldScoreboard().getPlayersTeam(viewer.getName());
            result = result.replace("%team_name%", team != null ? team.getRegisteredName() : "???");
        }
        if (result.indexOf("%prefix%") >= 0) {
            ScorePlayerTeam team = viewer.getWorldScoreboard().getPlayersTeam(viewer.getName());
            result = result.replace("%prefix%", team != null ? team.getColorPrefix() : "");
        }
        if (result.indexOf("%suffix%") >= 0) {
            ScorePlayerTeam team = viewer.getWorldScoreboard().getPlayersTeam(viewer.getName());
            result = result.replace("%suffix%", team != null ? team.getColorSuffix() : "");
        }

		if (result.indexOf("%is_op%") >= 0) {
            boolean op = this.isOpped(viewer.getName()) || 
                       (this.mcServer.isSinglePlayer() && viewer.getName().equals(this.mcServer.getServerOwner()));
            result = result.replace("%is_op%", op ? "Operator" : "Visitor");
        }

		if (result.indexOf("%is_op1%") >= 0) {
            boolean op = this.isOpped(viewer.getName()) || 
                       (this.mcServer.isSinglePlayer() && viewer.getName().equals(this.mcServer.getServerOwner()));
            result = result.replace("%is_op1%", op ? "✦" : "✖");
        }

        return result;
    }

	private static String dimensionName(int dimension) {
        switch (dimension) {
            case -1: return net.minecraft.util.StatCollector.translateToLocal("actx.dimension.nether");
            case 0:  return net.minecraft.util.StatCollector.translateToLocal("actx.dimension.overworld");
            case 1:  return net.minecraft.util.StatCollector.translateToLocal("actx.dimension.the_end");
            default: return String.format(net.minecraft.util.StatCollector.translateToLocal("actx.dimension.unknown"), dimension);
        }
    }

	private static String formatUptime(int ticks) {
		int totalSeconds = ticks / 20;
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;
		return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
	}

	private String resolveAnimationPlaceholders(String text) {
		Matcher m = ANIMATION_PLACEHOLDER.matcher(text);
		StringBuffer sb = new StringBuffer();
		long now = EagRuntime.steadyTimeMillis();
		while (m.find()) {
			String frame = actxmiscdata.getAnimationFrame(m.group(1), now);
			m.appendReplacement(sb, Matcher.quoteReplacement(frame != null ? frame : m.group(0)));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public void broadcastTabHeaderFooter() {
		String headerTemplate = actxmiscdata.getTabHeader();
		String footerTemplate = actxmiscdata.getTabFooter();
		for (int i = 0; i < this.playerEntityList.size(); ++i) {
			EntityPlayerMP viewer = (EntityPlayerMP) this.playerEntityList.get(i);
			IChatComponent header = new ChatComponentText(resolveTabPlaceholders(headerTemplate, viewer));
			IChatComponent footer = new ChatComponentText(resolveTabPlaceholders(footerTemplate, viewer));
			viewer.playerNetServerHandler.sendPacket(new S47PacketPlayerListHeaderFooter(header, footer));
		}
		this.broadcastBossBar();
	}

	public static final String BOSSBAR_CHANNEL = "ACTX|BossBar";
	private boolean bossBarWasVisible = false;

	public void broadcastBossBar() {
		if (!actxmiscdata.isBossBarEnabled()) {
			if (bossBarWasVisible) {
				bossBarWasVisible = false;
				for (int i = 0; i < this.playerEntityList.size(); ++i) {
					sendBossBar((EntityPlayerMP) this.playerEntityList.get(i), false, "");
				}
			}
			return;
		}
		bossBarWasVisible = true;
		String template = actxmiscdata.getBossBarTitle();
		if (template.isEmpty()) {
			template = net.minecraft.util.StatCollector.translateToLocal("entity.EnderDragon.name");
		}
		for (int i = 0; i < this.playerEntityList.size(); ++i) {
			EntityPlayerMP viewer = (EntityPlayerMP) this.playerEntityList.get(i);
			String title = resolveTabPlaceholders(template, viewer);
			sendBossBar(viewer, true, title == null ? "" : title.replace('\n', ' ').replace('\r', ' '));
		}
	}

	private void sendBossBar(EntityPlayerMP viewer, boolean visible, String title) {
		PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
		buf.writeBoolean(visible);
		buf.writeString(title);
		buf.writeFloat(1.0F);
		viewer.playerNetServerHandler.sendPacket(new S3FPacketCustomPayload(BOSSBAR_CHANNEL, buf));
	}

	public void updateBossBar(boolean enabled, String rawTitle) {
		actxmiscdata.setBossBarEnabled(enabled);
		actxmiscdata.setBossBarTitle(rawTitle);
		this.broadcastBossBar();
	}

	public void updateTabHeaderFooter(String rawHeader, String rawFooter) {
		actxmiscdata.setTabHeader(rawHeader);
		actxmiscdata.setTabFooter(rawFooter);
		this.broadcastTabHeaderFooter();
	}

	/**+
	 * using player's dimension, update their movement when in a
	 * vehicle (e.g. cart, boat)
	 */
	public void serverUpdateMountedMovingPlayer(EntityPlayerMP playerIn) {
		playerIn.getServerForPlayer().getPlayerManager().updateMountedMovingPlayer(playerIn);
	}

	/**+
	 * Called when a player disconnects from the game. Writes player
	 * data to disk and removes them from the world.
	 */
public void playerLoggedOut(EntityPlayerMP playerIn) {
        playerIn.triggerAchievement(StatList.leaveGameStat);
        this.writePlayerData(playerIn);
        WorldServer worldserver = playerIn.getServerForPlayer();
        if (playerIn.ridingEntity != null) {
            worldserver.removePlayerEntityDangerously(playerIn.ridingEntity);
            logger.debug("removing player mount");
        }

        worldserver.removeEntity(playerIn);
        worldserver.getPlayerManager().removePlayer(playerIn);
        this.playerEntityList.remove(playerIn);
        EaglercraftUUID uuid = playerIn.getUniqueID();
        EntityPlayerMP entityplayermp = (EntityPlayerMP) this.uuidToPlayerMap.get(uuid);
        if (entityplayermp == playerIn) {
            this.uuidToPlayerMap.remove(uuid);
            this.playerStatFiles.remove(entityplayermp.getName());
        }

        IntegratedVoiceService vcs = ((EaglerMinecraftServer) mcServer).getVoiceService();
        if (vcs != null) {
            vcs.handlePlayerLoggedOut(playerIn);
        }

        this.sendPacketToAllPlayers(new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.REMOVE_PLAYER,
                new EntityPlayerMP[] { playerIn }));
        
        actxmiscdata.ensureLoaded();
        actxmiscdata.setLoggedIn(playerIn.getName(), false);
        if (playerIn.worldObj.getGameRules().getBoolean("announceLeave")) {
            this.sendChatMsg(new ChatComponentText(
                    actxmiscdata.formatLeave(playerIn)));
        }
        actxmiscdata.clearPlayerSession(playerIn); // after formatLeave, so %time% in the leave message reflects the FULL session, not 0s
    }

	/**+
	 * checks ban-lists, then white-lists, then space for the
	 * server. Returns null on success, or an error message
	 */
	public String allowUserToConnect(GameProfile gameprofile) {
		return doesPlayerAlreadyExist(gameprofile)
				? "\"" + gameprofile.getName() + "\" is already playing on this world!"
				: null;
	}

	private boolean doesPlayerAlreadyExist(GameProfile gameprofile) {
		for (int i = 0, l = playerEntityList.size(); i < l; ++i) {
			EntityPlayerMP player = playerEntityList.get(i);
			if (player.getName().equalsIgnoreCase(gameprofile.getName())
					|| player.getUniqueID().equals(gameprofile.getId())) {
				return true;
			}
		}
		return false;
	}

	/**+
	 * also checks for multiple logins across servers
	 */
	public EntityPlayerMP createPlayerForUser(GameProfile profile) {
		EaglercraftUUID uuid = EntityPlayer.getUUID(profile);
		ArrayList<EntityPlayerMP> arraylist = Lists.newArrayList();

		for (int i = 0, l = this.playerEntityList.size(); i < l; ++i) {
			EntityPlayerMP entityplayermp = (EntityPlayerMP) this.playerEntityList.get(i);
			if (entityplayermp.getUniqueID().equals(uuid)
					|| entityplayermp.getName().equalsIgnoreCase(profile.getName())) {
				arraylist.add(entityplayermp);
			}
		}

		EntityPlayerMP entityplayermp2 = (EntityPlayerMP) this.uuidToPlayerMap.get(profile.getId());
		if (entityplayermp2 != null && !arraylist.contains(entityplayermp2)) {
			arraylist.add(entityplayermp2);
		}

		for (int i = 0, l = arraylist.size(); i < l; ++i) {
			arraylist.get(i).playerNetServerHandler.kickPlayerFromServer("You logged in from another location");
		}

		Object object;
		if (this.mcServer.isDemo()) {
			object = new DemoWorldManager(this.mcServer.worldServerForDimension(0));
		} else {
			object = new ItemInWorldManager(this.mcServer.worldServerForDimension(0));
		}

		return new EntityPlayerMP(this.mcServer, this.mcServer.worldServerForDimension(0), profile,
				(ItemInWorldManager) object);
	}

	/**+
	 * Called on respawn
	 */
	public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP playerIn, int dimension, boolean conqueredEnd) {
		playerIn.getServerForPlayer().getEntityTracker().removePlayerFromTrackers(playerIn);
		playerIn.getServerForPlayer().getEntityTracker().untrackEntity(playerIn);
		playerIn.getServerForPlayer().getPlayerManager().removePlayer(playerIn);
		this.playerEntityList.remove(playerIn);
		this.mcServer.worldServerForDimension(playerIn.dimension).removePlayerEntityDangerously(playerIn);
		BlockPos blockpos = playerIn.getBedLocation();
		boolean flag = playerIn.isSpawnForced();
		playerIn.dimension = dimension;
		Object object;
		if (this.mcServer.isDemo()) {
			object = new DemoWorldManager(this.mcServer.worldServerForDimension(playerIn.dimension));
		} else {
			object = new ItemInWorldManager(this.mcServer.worldServerForDimension(playerIn.dimension));
		}

		EntityPlayerMP entityplayermp = new EntityPlayerMP(this.mcServer,
				this.mcServer.worldServerForDimension(playerIn.dimension), playerIn.getGameProfile(),
				(ItemInWorldManager) object);
		entityplayermp.updateCertificate = playerIn.updateCertificate;
		entityplayermp.clientBrandUUID = playerIn.clientBrandUUID;
		entityplayermp.playerNetServerHandler = playerIn.playerNetServerHandler;
		entityplayermp.clonePlayer(playerIn, conqueredEnd);
		entityplayermp.setEntityId(playerIn.getEntityId());
		entityplayermp.func_174817_o(playerIn);
		WorldServer worldserver = this.mcServer.worldServerForDimension(playerIn.dimension);
		this.setPlayerGameTypeBasedOnOther(entityplayermp, playerIn, worldserver);
		if (blockpos != null) {
			BlockPos blockpos1 = EntityPlayer
					.getBedSpawnLocation(this.mcServer.worldServerForDimension(playerIn.dimension), blockpos, flag);
			if (blockpos1 != null) {
				entityplayermp.setLocationAndAngles((double) ((float) blockpos1.getX() + 0.5F),
						(double) ((float) blockpos1.getY() + 0.1F), (double) ((float) blockpos1.getZ() + 0.5F), 0.0F,
						0.0F);
				entityplayermp.setSpawnPoint(blockpos, flag);
			} else {
				entityplayermp.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(0, 0.0F));
			}
		}

		worldserver.theChunkProviderServer.loadChunk((int) entityplayermp.posX >> 4, (int) entityplayermp.posZ >> 4);

		while (!worldserver.getCollidingBoundingBoxes(entityplayermp, entityplayermp.getEntityBoundingBox()).isEmpty()
				&& entityplayermp.posY < 256.0D) {
			entityplayermp.setPosition(entityplayermp.posX, entityplayermp.posY + 1.0D, entityplayermp.posZ);
		}

		entityplayermp.playerNetServerHandler.sendPacket(new S07PacketRespawn(entityplayermp.dimension,
				entityplayermp.worldObj.getDifficulty(), entityplayermp.worldObj.getWorldInfo().getTerrainType(),
				entityplayermp.theItemInWorldManager.getGameType()));
		BlockPos blockpos2 = worldserver.getSpawnPoint();
		entityplayermp.playerNetServerHandler.setPlayerLocation(entityplayermp.posX, entityplayermp.posY,
				entityplayermp.posZ, entityplayermp.rotationYaw, entityplayermp.rotationPitch);
		entityplayermp.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(blockpos2));
		entityplayermp.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(entityplayermp.experience,
				entityplayermp.experienceTotal, entityplayermp.experienceLevel));
		this.updateTimeAndWeatherForPlayer(entityplayermp, worldserver);
		worldserver.getPlayerManager().addPlayer(entityplayermp);
		worldserver.spawnEntityInWorld(entityplayermp);
		this.playerEntityList.add(entityplayermp);
		this.uuidToPlayerMap.put(entityplayermp.getUniqueID(), entityplayermp);
		entityplayermp.addSelfToInternalCraftingInventory();
		entityplayermp.setHealth(entityplayermp.getHealth());
		return entityplayermp;
	}

	/**+
	 * moves provided player from overworld to nether or vice versa
	 */
	public void transferPlayerToDimension(EntityPlayerMP playerIn, int dimension) {
		int i = playerIn.dimension;
		WorldServer worldserver = this.mcServer.worldServerForDimension(playerIn.dimension);
		playerIn.dimension = dimension;
		WorldServer worldserver1 = this.mcServer.worldServerForDimension(playerIn.dimension);
		playerIn.playerNetServerHandler.sendPacket(new S07PacketRespawn(playerIn.dimension,
				playerIn.worldObj.getDifficulty(), playerIn.worldObj.getWorldInfo().getTerrainType(),
				playerIn.theItemInWorldManager.getGameType()));
		worldserver.removePlayerEntityDangerously(playerIn);
		playerIn.isDead = false;
		this.transferEntityToWorld(playerIn, i, worldserver, worldserver1);
		this.preparePlayer(playerIn, worldserver);
		playerIn.playerNetServerHandler.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ,
				playerIn.rotationYaw, playerIn.rotationPitch);
		playerIn.theItemInWorldManager.setWorld(worldserver1);
		this.updateTimeAndWeatherForPlayer(playerIn, worldserver1);
		this.syncPlayerInventory(playerIn);

		for (ObjectCursor<PotionEffect> potioneffect : playerIn.getActivePotionEffects()) {
			playerIn.playerNetServerHandler
					.sendPacket(new S1DPacketEntityEffect(playerIn.getEntityId(), potioneffect.value));
		}

	}

	public void transferPlayerToDimensionAtPosition(EntityPlayerMP playerIn, int dimension, double x, double y,
			double z, float yaw, float pitch) {
		WorldServer worldserver = this.mcServer.worldServerForDimension(playerIn.dimension);
		playerIn.dimension = dimension;
		WorldServer worldserver1 = this.mcServer.worldServerForDimension(playerIn.dimension);
		playerIn.playerNetServerHandler.sendPacket(new S07PacketRespawn(playerIn.dimension,
				playerIn.worldObj.getDifficulty(), playerIn.worldObj.getWorldInfo().getTerrainType(),
				playerIn.theItemInWorldManager.getGameType()));
		worldserver.removePlayerEntityDangerously(playerIn);
		playerIn.isDead = false;

		playerIn.setLocationAndAngles(x, y, z, yaw, pitch);
		if (playerIn.isEntityAlive()) {
			worldserver1.updateEntityWithOptionalForce(playerIn, false);
		}
		playerIn.setWorld(worldserver1);
		worldserver1.spawnEntityInWorld(playerIn);

		this.preparePlayer(playerIn, worldserver);
		playerIn.playerNetServerHandler.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ,
				playerIn.rotationYaw, playerIn.rotationPitch);
		playerIn.theItemInWorldManager.setWorld(worldserver1);
		this.updateTimeAndWeatherForPlayer(playerIn, worldserver1);
		this.syncPlayerInventory(playerIn);

		for (ObjectCursor<PotionEffect> potioneffect : playerIn.getActivePotionEffects()) {
			playerIn.playerNetServerHandler
					.sendPacket(new S1DPacketEntityEffect(playerIn.getEntityId(), potioneffect.value));
		}
	}

	/**+
	 * Transfers an entity from a world to another world.
	 */
	public void transferEntityToWorld(Entity entityIn, int parInt1, WorldServer parWorldServer,
			WorldServer parWorldServer2) {
		double d0 = entityIn.posX;
		double d1 = entityIn.posZ;
		double d2 = 8.0D;
		float f = entityIn.rotationYaw;
		if (entityIn.dimension == -1) {
			d0 = MathHelper.clamp_double(d0 / d2, parWorldServer2.getWorldBorder().minX() + 16.0D,
					parWorldServer2.getWorldBorder().maxX() - 16.0D);
			d1 = MathHelper.clamp_double(d1 / d2, parWorldServer2.getWorldBorder().minZ() + 16.0D,
					parWorldServer2.getWorldBorder().maxZ() - 16.0D);
			entityIn.setLocationAndAngles(d0, entityIn.posY, d1, entityIn.rotationYaw, entityIn.rotationPitch);
			if (entityIn.isEntityAlive()) {
				parWorldServer.updateEntityWithOptionalForce(entityIn, false);
			}
		} else if (entityIn.dimension == 0) {
			d0 = MathHelper.clamp_double(d0 * d2, parWorldServer2.getWorldBorder().minX() + 16.0D,
					parWorldServer2.getWorldBorder().maxX() - 16.0D);
			d1 = MathHelper.clamp_double(d1 * d2, parWorldServer2.getWorldBorder().minZ() + 16.0D,
					parWorldServer2.getWorldBorder().maxZ() - 16.0D);
			entityIn.setLocationAndAngles(d0, entityIn.posY, d1, entityIn.rotationYaw, entityIn.rotationPitch);
			if (entityIn.isEntityAlive()) {
				parWorldServer.updateEntityWithOptionalForce(entityIn, false);
			}
		} else {
			BlockPos blockpos;
			if (parInt1 == 1) {
				blockpos = parWorldServer2.getSpawnPoint();
			} else {
				blockpos = parWorldServer2.getSpawnCoordinate();
			}

			d0 = (double) blockpos.getX();
			entityIn.posY = (double) blockpos.getY();
			d1 = (double) blockpos.getZ();
			entityIn.setLocationAndAngles(d0, entityIn.posY, d1, 90.0F, 0.0F);
			if (entityIn.isEntityAlive()) {
				parWorldServer.updateEntityWithOptionalForce(entityIn, false);
			}
		}

		if (parInt1 != 1) {
			d0 = (double) MathHelper.clamp_int((int) d0, -29999872, 29999872);
			d1 = (double) MathHelper.clamp_int((int) d1, -29999872, 29999872);
			if (entityIn.isEntityAlive()) {
				entityIn.setLocationAndAngles(d0, entityIn.posY, d1, entityIn.rotationYaw, entityIn.rotationPitch);
				parWorldServer2.getDefaultTeleporter().placeInPortal(entityIn, f);
				parWorldServer2.spawnEntityInWorld(entityIn);
				parWorldServer2.updateEntityWithOptionalForce(entityIn, false);
			}
		}

		entityIn.setWorld(parWorldServer2);
	}

	/**+
	 * self explanitory
	 */
	public void onTick() {
		if (++this.playerPingIndex > 600) {
			this.sendPacketToAllPlayers(
					new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.UPDATE_LATENCY, this.playerEntityList));
			this.playerPingIndex = 0;
		}
	}

	public void sendPacketToAllPlayers(Packet packetIn) {
		for (int i = 0; i < this.playerEntityList.size(); ++i) {
			((EntityPlayerMP) this.playerEntityList.get(i)).playerNetServerHandler.sendPacket(packetIn);
		}

	}

	public void sendPacketToAllPlayersInDimension(Packet packetIn, int dimension) {
		for (int i = 0; i < this.playerEntityList.size(); ++i) {
			EntityPlayerMP entityplayermp = (EntityPlayerMP) this.playerEntityList.get(i);
			if (entityplayermp.dimension == dimension) {
				entityplayermp.playerNetServerHandler.sendPacket(packetIn);
			}
		}

	}

	public void sendMessageToAllTeamMembers(EntityPlayer player, IChatComponent message) {
		Team team = player.getTeam();
		if (team != null) {
			for (String s : team.getMembershipCollection()) {
				EntityPlayerMP entityplayermp = this.getPlayerByUsername(s);
				if (entityplayermp != null && entityplayermp != player) {
					entityplayermp.addChatMessage(message);
				}
			}

		}
	}

	public void sendMessageToTeamOrEvryPlayer(EntityPlayer player, IChatComponent message) {
		Team team = player.getTeam();
		if (team == null) {
			this.sendChatMsg(message);
		} else {
			for (int i = 0, l = this.playerEntityList.size(); i < l; ++i) {
				EntityPlayerMP entityplayermp = (EntityPlayerMP) this.playerEntityList.get(i);
				if (entityplayermp.getTeam() != team) {
					entityplayermp.addChatMessage(message);
				}
			}

		}
	}

	public String func_181058_b(boolean parFlag) {
		String s = "";
		ArrayList arraylist = Lists.newArrayList(this.playerEntityList);

		for (int i = 0; i < arraylist.size(); ++i) {
			if (i > 0) {
				s = s + ", ";
			}

			s = s + ((EntityPlayerMP) arraylist.get(i)).getName();
			if (parFlag) {
				s = s + " (" + ((EntityPlayerMP) arraylist.get(i)).getUniqueID().toString() + ")";
			}
		}

		return s;
	}

	/**+
	 * Returns an array of the usernames of all the connected
	 * players.
	 */
	public String[] getAllUsernames() {
		String[] astring = new String[this.playerEntityList.size()];

		for (int i = 0; i < astring.length; ++i) {
			astring[i] = ((EntityPlayerMP) this.playerEntityList.get(i)).getName();
		}

		return astring;
	}

	public GameProfile[] getAllProfiles() {
		GameProfile[] agameprofile = new GameProfile[this.playerEntityList.size()];

		for (int i = 0; i < agameprofile.length; ++i) {
			agameprofile[i] = ((EntityPlayerMP) this.playerEntityList.get(i)).getGameProfile();
		}

		return agameprofile;
	}

	public boolean canJoin(GameProfile gameprofile) {
		return true;
	}

	public boolean canSendCommands(GameProfile profile) {
		return lanCheats
				|| this.mcServer.isSinglePlayer() && this.mcServer.worldServers[0].getWorldInfo().areCommandsAllowed()
						&& this.mcServer.getServerOwner().equalsIgnoreCase(profile.getName())
				|| this.commandsAllowedForAll
				|| this.sessionOppedPlayers.contains(profile.getName());
	}

	public EntityPlayerMP getPlayerByUsername(String username) {
		for (EntityPlayerMP entityplayermp : this.playerEntityList) {
			if (entityplayermp.getName().equalsIgnoreCase(username)) {
				return entityplayermp;
			}
		}

		return null;
	}

	/**+
	 * params: x,y,z,r,dimension. The packet is sent to all players
	 * within r radius of x,y,z (r^2>x^2+y^2+z^2)
	 */
	public void sendToAllNear(double x, double y, double z, double radius, int dimension, Packet packetIn) {
		this.sendToAllNearExcept((EntityPlayer) null, x, y, z, radius, dimension, packetIn);
	}

	/**+
	 * params: srcPlayer,x,y,z,r,dimension. The packet is not sent
	 * to the srcPlayer, but all other players within the search
	 * radius
	 */
	public void sendToAllNearExcept(EntityPlayer x, double y, double z, double radius, double dimension, int parInt1,
			Packet parPacket) {
		for (int i = 0, l = this.playerEntityList.size(); i < l; ++i) {
			EntityPlayerMP entityplayermp = (EntityPlayerMP) this.playerEntityList.get(i);
			if (entityplayermp != x && entityplayermp.dimension == parInt1) {
				double d0 = y - entityplayermp.posX;
				double d1 = z - entityplayermp.posY;
				double d2 = radius - entityplayermp.posZ;
				if (d0 * d0 + d1 * d1 + d2 * d2 < dimension * dimension) {
					entityplayermp.playerNetServerHandler.sendPacket(parPacket);
				}
			}
		}

	}

	/**+
	 * Saves all of the players' current states.
	 */
	public void saveAllPlayerData() {
		for (int i = 0, l = this.playerEntityList.size(); i < l; ++i) {
			this.writePlayerData((EntityPlayerMP) this.playerEntityList.get(i));
		}

	}

	/**+
	 * Updates the time and weather for the given player to those of
	 * the given world
	 */
	public void updateTimeAndWeatherForPlayer(EntityPlayerMP playerIn, WorldServer worldIn) {
		WorldBorder worldborder = this.mcServer.worldServers[0].getWorldBorder();
		playerIn.playerNetServerHandler
				.sendPacket(new S44PacketWorldBorder(worldborder, S44PacketWorldBorder.Action.INITIALIZE));
		playerIn.playerNetServerHandler.sendPacket(new S03PacketTimeUpdate(worldIn.getTotalWorldTime(),
				worldIn.getWorldTime(), worldIn.getGameRules().getBoolean("doDaylightCycle")));
		if (worldIn.isRaining()) {
			playerIn.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(1, 0.0F));
			playerIn.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(7, worldIn.getRainStrength(1.0F)));
			playerIn.playerNetServerHandler
					.sendPacket(new S2BPacketChangeGameState(8, worldIn.getThunderStrength(1.0F)));
		}

	}

	/**+
	 * sends the players inventory to himself
	 */
	public void syncPlayerInventory(EntityPlayerMP playerIn) {
		playerIn.sendContainerToPlayer(playerIn.inventoryContainer);
		playerIn.setPlayerHealthUpdated();
		playerIn.playerNetServerHandler.sendPacket(new S09PacketHeldItemChange(playerIn.inventory.currentItem));
	}

	/**+
	 * Returns the number of players currently on the server.
	 */
	public int getCurrentPlayerCount() {
		return this.playerEntityList.size();
	}

	/**+
	 * Returns the maximum number of players allowed on the server.
	 */
	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	/**+
	 * Returns an array of usernames for which player.dat exists
	 * for.
	 */
	public String[] getAvailablePlayerDat() {
		return this.mcServer.worldServers[0].getSaveHandler().getPlayerNBTManager().getAvailablePlayerDat();
	}

	public void setWhiteListEnabled(boolean flag) {
		this.whiteListEnforced = flag;
	}

	public List<EntityPlayerMP> getPlayersMatchingAddress(String address) {
		ArrayList arraylist = Lists.newArrayList();

		for (int i = 0, l = playerEntityList.size(); i < l; ++i) {
			EntityPlayerMP entityplayermp = playerEntityList.get(i);
			if (entityplayermp.getPlayerIP().equals(address)) {
				arraylist.add(entityplayermp);
			}
		}

		return arraylist;
	}

	/**+
	 * Gets the View Distance.
	 */
	public int getViewDistance() {
		return this.viewDistance;
	}

	public MinecraftServer getServerInstance() {
		return this.mcServer;
	}

	/**+
	 * On integrated servers, returns the host's player data to be
	 * written to level.dat.
	 */
	public NBTTagCompound getHostPlayerData() {
		return null;
	}

	public void setGameType(WorldSettings.GameType parGameType) {
		this.gameType = parGameType;
	}

	private void setPlayerGameTypeBasedOnOther(EntityPlayerMP worldIn, EntityPlayerMP parEntityPlayerMP2,
			World parWorld) {
		if (parEntityPlayerMP2 == null || parEntityPlayerMP2.getName().equals(mcServer.getServerOwner())) {
			if (parEntityPlayerMP2 != null) {
				worldIn.theItemInWorldManager.setGameType(parEntityPlayerMP2.theItemInWorldManager.getGameType());
			} else if (this.gameType != null) {
				worldIn.theItemInWorldManager.setGameType(this.gameType);
			}

			worldIn.theItemInWorldManager.initializeGameType(parWorld.getWorldInfo().getGameType());
		} else {
			worldIn.theItemInWorldManager.setGameType(lanGamemode);
		}
	}

	/**+
	 * Sets whether all players are allowed to use commands (cheats)
	 * on the server.
	 */
	public void setCommandsAllowedForAll(boolean parFlag) {
		this.commandsAllowedForAll = parFlag;
	}

	/**+
	 * Kicks everyone with "Server closed" as reason.
	 */
	public void removeAllPlayers() {
		for (int i = 0, l = this.playerEntityList.size(); i < l; ++i) {
			((EntityPlayerMP) this.playerEntityList.get(i)).playerNetServerHandler
					.kickPlayerFromServer("Server closed");
		}

	}

	public void sendChatMsgImpl(IChatComponent component, boolean isChat) {
		this.mcServer.addChatMessage(component);
		int i = isChat ? 1 : 0;
		this.sendPacketToAllPlayers(new S02PacketChat(component, (byte) i));
	}

	/**+
	 * Sends the given string to every player as chat message.
	 */
	public void sendChatMsg(IChatComponent component) {
		this.sendChatMsgImpl(component, true);
	}

	public StatisticsFile getPlayerStatsFile(EntityPlayer playerIn) {
		String name = playerIn.getName();
		StatisticsFile statisticsfile = (StatisticsFile) this.playerStatFiles.get(name);
		if (statisticsfile == null) {
			VFile2 file1 = WorldsDB
					.newVFile(this.mcServer.worldServerForDimension(0).getSaveHandler().getWorldDirectory(), "stats");
			VFile2 file2 = WorldsDB.newVFile(file1, name + ".json");
			statisticsfile = new StatisticsFile(this.mcServer, file2);
			statisticsfile.readStatFile();
			this.playerStatFiles.put(name, statisticsfile);
		}

		return statisticsfile;
	}

	public void setViewDistance(int distance) {
		this.viewDistance = distance;
		int entityViewDist = getEntityViewDistance();
		if (this.mcServer.worldServers != null) {
			WorldServer[] srv = this.mcServer.worldServers;
			for (int i = 0; i < srv.length; ++i) {
				WorldServer worldserver = srv[i];
				if (worldserver != null) {
					worldserver.getPlayerManager().setPlayerViewRadius(distance);
					worldserver.getEntityTracker().updateMaxTrackingThreshold(entityViewDist);
				}
			}
		}
	}

	public List<EntityPlayerMP> func_181057_v() {
		return this.playerEntityList;
	}

	/**+
	 * Get's the EntityPlayerMP object representing the player with
	 * the UUID.
	 */
	public EntityPlayerMP getPlayerByUUID(EaglercraftUUID playerUUID) {
		return (EntityPlayerMP) this.uuidToPlayerMap.get(playerUUID);
	}

	public boolean func_183023_f(GameProfile var1) {
		return false;
	}

	public void updatePlayerViewDistance(EntityPlayerMP entityPlayerMP, int viewDistance2) {
		if (entityPlayerMP.getName().equals(mcServer.getServerOwner())) {
			if (viewDistance != viewDistance2) {
				logger.info("Owner is setting view distance: {}", viewDistance2);
				setViewDistance(viewDistance2);
			}
		}
	}

	public void addOp(String playerName) {
		this.sessionOppedPlayers.add(playerName);
	}

	public void removeOp(String playerName) {
		this.sessionOppedPlayers.remove(playerName);
	}

	public boolean isOpped(String playerName) {
		return this.sessionOppedPlayers.contains(playerName);
	}

	public void configureLAN(int gamemode, boolean cheats) {
		lanGamemode = WorldSettings.GameType.getByID(gamemode);
		lanCheats = cheats;
		this.broadcastTabHeaderFooter(); // gamemode/cheats (and possibly relay/code) just changed - re-resolve for everyone
	}

	public void configureLAN(int gamemode, boolean cheats, String resourcePackUrl, String resourcePackHash,
			boolean requirePack) {
		this.configureLAN(gamemode, cheats);
		this.mcServer.setResourcePack(resourcePackUrl == null ? "" : resourcePackUrl,
				resourcePackHash == null ? "" : resourcePackHash);
		this.mcServer.setResourcePackRequired(requirePack);
	}

	public void configureLAN(int gamemode, boolean cheats, String resourcePackUrl, String resourcePackHash,
			boolean requirePack, String relayUri, String code) {
		this.lanRelayUri = (relayUri == null || relayUri.isEmpty()) ? "<disconnected>" : relayUri;
		this.lanCode = (code == null || code.isEmpty()) ? "<undefined>" : code;
		this.configureLAN(gamemode, cheats, resourcePackUrl, resourcePackHash, requirePack);
	}
}