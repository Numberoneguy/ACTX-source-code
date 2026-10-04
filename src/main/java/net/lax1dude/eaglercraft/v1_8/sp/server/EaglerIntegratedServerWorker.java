/*
 * Copyright (c) 2023-2025 lax1dude, ayunami2000. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.sp.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.EagUtils;
import net.lax1dude.eaglercraft.v1_8.internal.IPCPacketData;
import net.lax1dude.eaglercraft.v1_8.internal.vfs2.VFile2;
import net.lax1dude.eaglercraft.v1_8.log4j.ILogRedirector;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.lax1dude.eaglercraft.v1_8.sp.ipc.*;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ReportedException;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringTranslate;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.WorldType;
import java.util.HashSet;                                    
import java.util.Set;                                        
import net.minecraft.command.WorldEditCommand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;           
import net.lax1dude.eaglercraft.v1_8.sp.server.export.WorldConverterEPK;
import net.lax1dude.eaglercraft.v1_8.sp.server.export.WorldConverterMCA;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.lax1dude.eaglercraft.v1_8.sp.server.internal.ServerPlatformSingleplayer;
import net.lax1dude.eaglercraft.v1_8.sp.server.socket.IntegratedServerPlayerNetworkManager;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

public class EaglerIntegratedServerWorker {

	public static final Logger logger = LogManager.getLogger("EaglerIntegratedServer");

	private static EaglerMinecraftServer currentProcess = null;
	private static WorldSettings newWorldSettings = null;

	public static final EaglerSaveFormat saveFormat = new EaglerSaveFormat(EaglerSaveFormat.worldsFolder);

	private static final Map<String, IntegratedServerPlayerNetworkManager> openChannels = new HashMap<>();

	private static final IPCPacketManager packetManagerInstance = new IPCPacketManager();

	private static void processAsyncMessageQueue() {
		List<IPCPacketData> pktList = ServerPlatformSingleplayer.recieveAllPacket();
		if(pktList != null) {
			IPCPacketData packetData;
			for(int i = 0, l = pktList.size(); i < l; ++i) {
				packetData = pktList.get(i);
				if(packetData.channel.equals(SingleplayerServerController.IPC_CHANNEL)) {
					IPCPacketBase ipc;
					try {
						ipc = packetManagerInstance.IPCDeserialize(packetData.contents);
					}catch(IOException ex) {
						throw new RuntimeException("Failed to deserialize IPC packet", ex);
					}
					handleIPCPacket(ipc);
				}else {
					IntegratedServerPlayerNetworkManager netHandler = openChannels.get(packetData.channel);
					if(netHandler != null) {
						netHandler.addRecievedPacket(packetData.contents);
					}else {
						logger.error("Recieved packet on channel that does not exist: \"{}\"", packetData.channel);
					}
				}
			}
		}
		if (!ServerPlatformSingleplayer.isSingleThreadMode() && ServerPlatformSingleplayer.isTabAboutToCloseWASM()
				&& !isServerStopped()) {
			logger.info("Autosaving worlds because the tab is about to close!");
			currentProcess.getConfigurationManager().saveAllPlayerData();
			currentProcess.saveAllWorlds(false);
		}
	}

	public static void tick() {
		List<IntegratedServerPlayerNetworkManager> ocs = new ArrayList<>(openChannels.values());
		for(int i = 0, l = ocs.size(); i < l; ++i) {
			ocs.get(i).tick();
		}
	}

	public static EaglerMinecraftServer getServer() {
		return currentProcess;
	}

	public static boolean getChannelExists(String channel) {
		return openChannels.containsKey(channel);
	}

	public static void closeChannel(String channel) {
		IntegratedServerPlayerNetworkManager netmanager = openChannels.remove(channel);
		if(netmanager != null) {
			netmanager.closeChannel(new ChatComponentText("End of stream"));
			sendIPCPacket(new IPCPacket0CPlayerChannel(channel, false));
		}
	}

	private static void startPlayerConnnection(String channel) {
		if(openChannels.containsKey(channel)) {
			logger.error("Tried opening player channel that already exists: {}", channel);
			return;
		}
		if(currentProcess == null) {
			logger.error("Tried opening player channel while server is stopped: {}", channel);
			return;
		}
		IntegratedServerPlayerNetworkManager networkmanager = new IntegratedServerPlayerNetworkManager(channel);
		networkmanager.setConnectionState(EnumConnectionState.LOGIN);
		networkmanager.setNetHandler(new NetHandlerLoginServer(currentProcess, networkmanager));
		openChannels.put(channel, networkmanager);
	}

	private static void handleIPCPacket(IPCPacketBase ipc) {
		int id = ipc.id();
		try {
			switch(id) {
			case IPCPacket00StartServer.ID: {
				IPCPacket00StartServer pkt = (IPCPacket00StartServer)ipc;
				
				if(!isServerStopped()) {
					currentProcess.stopServer();
				}
				
				currentProcess = new EaglerMinecraftServer(pkt.worldName, pkt.ownerName, pkt.initialViewDistance, newWorldSettings, pkt.demoMode);
				currentProcess.setBaseServerProperties(EnumDifficulty.getDifficultyEnum(pkt.initialDifficulty), newWorldSettings == null ? GameType.SURVIVAL : newWorldSettings.getGameType());
				currentProcess.startServer();
				
				String[] worlds = EaglerSaveFormat.worldsList.getAllLines();
				if(worlds == null || (worlds.length == 1 && worlds[0].trim().length() <= 0)) {
					worlds = null;
				}
				if(worlds == null) {
					EaglerSaveFormat.worldsList.setAllChars(pkt.worldName);
				}else {
					boolean found = false;
					for(int i = 0; i < worlds.length; ++i) {
						if(worlds[i].equals(pkt.worldName)) {
							found = true;
							break;
						}
					}
					if(!found) {
						String[] s = new String[worlds.length + 1];
						s[0] = pkt.worldName;
						System.arraycopy(worlds, 0, s, 1, worlds.length);
						EaglerSaveFormat.worldsList.setAllChars(String.join("\n", s));
					}
				}
				
				sendIPCPacket(new IPCPacketFFProcessKeepAlive(IPCPacket00StartServer.ID));
				break;
			}
			case IPCPacket01StopServer.ID: {
				if(currentProcess != null) {
					currentProcess.stopServer();
					currentProcess = null;
				}
				sendIPCPacket(new IPCPacketFFProcessKeepAlive(IPCPacket01StopServer.ID));
				actxmiscdata.session_bans.clear();
				break;
			}
			case IPCPacket02InitWorld.ID: {
				tryStopServer();
				IPCPacket02InitWorld pkt = (IPCPacket02InitWorld)ipc;
				newWorldSettings = new WorldSettings(pkt.seed, GameType.getByID(pkt.gamemode), pkt.structures,
						pkt.hardcore, WorldType.worldTypes[pkt.worldType]);
				newWorldSettings.setWorldName(pkt.worldArgs); // "setWorldName" is actually for setting generator arguments, MCP fucked up
				if(pkt.bonusChest) {
					newWorldSettings.enableBonusChest();
				}
				if(pkt.cheats) {
					newWorldSettings.enableCommands();
				}
				break;
			}
			case IPCPacket03DeleteWorld.ID: {
				tryStopServer();
				IPCPacket03DeleteWorld pkt = (IPCPacket03DeleteWorld)ipc;
				if(!saveFormat.deleteWorldDirectory(pkt.worldName)) {
					sendTaskFailed();
					break;
				}
				String[] worldsTxt = EaglerSaveFormat.worldsList.getAllLines();
				if(worldsTxt != null) {
					List<String> newWorlds = new ArrayList<>();
					for(int i = 0; i < worldsTxt.length; ++i) {
						String str = worldsTxt[i];
						if(!str.equalsIgnoreCase(pkt.worldName)) {
							newWorlds.add(str);
						}
					}
					EaglerSaveFormat.worldsList.setAllChars(String.join("\n", newWorlds));
				}
				sendIPCPacket(new IPCPacketFFProcessKeepAlive(IPCPacket03DeleteWorld.ID));
				break;
			}
			case IPCPacket05RequestData.ID: {
				tryStopServer();
				IPCPacket05RequestData pkt = (IPCPacket05RequestData)ipc;
				if(pkt.request == IPCPacket05RequestData.REQUEST_LEVEL_EAG) {
					sendIPCPacket(new IPCPacket09RequestResponse(WorldConverterEPK.exportWorld(pkt.worldName)));
				}else if(pkt.request == IPCPacket05RequestData.REQUEST_LEVEL_MCA) {
					sendIPCPacket(new IPCPacket09RequestResponse(WorldConverterMCA.exportWorld(pkt.worldName)));
				}else {
					logger.error("Unknown IPCPacket05RequestData type {}", ((int)pkt.request & 0xFF));
					sendTaskFailed();
				}
				break;
			}
			case IPCPacket06RenameWorldNBT.ID: {
				tryStopServer();
				IPCPacket06RenameWorldNBT pkt = (IPCPacket06RenameWorldNBT)ipc;
				boolean b = false;
				if(pkt.duplicate) {
					b = saveFormat.duplicateWorld(pkt.worldName, pkt.displayName);
				}else {
					b = saveFormat.renameWorld(pkt.worldName, pkt.displayName);
				}
				if(!b) {
					sendTaskFailed();
					break;
				}
				sendIPCPacket(new IPCPacketFFProcessKeepAlive(IPCPacket06RenameWorldNBT.ID));
				break;
			}
			case IPCPacket07ImportWorld.ID: {
				tryStopServer();
				IPCPacket07ImportWorld pkt = (IPCPacket07ImportWorld)ipc;
				try {
					if(pkt.worldFormat == IPCPacket07ImportWorld.WORLD_FORMAT_EAG) {
						WorldConverterEPK.importWorld(pkt.worldData, pkt.worldName);
					}else if(pkt.worldFormat == IPCPacket07ImportWorld.WORLD_FORMAT_MCA) {
						WorldConverterMCA.importWorld(pkt.worldData, pkt.worldName, pkt.gameRules);
					}else {
						throw new IOException("Client requested an unsupported export format!");
					}
					sendIPCPacket(new IPCPacketFFProcessKeepAlive(IPCPacket07ImportWorld.ID));
				}catch(IOException ex) {
					sendIPCPacket(new IPCPacket15Crashed("COULD NOT IMPORT WORLD \"" + pkt.worldName + "\"!!!\n\n" + EagRuntime.getStackTrace(ex) + "\n\nFile is probably corrupt, try a different world"));
					sendTaskFailed();
				}
				break;
			}
			case IPCPacket0ASetWorldDifficulty.ID: {
				IPCPacket0ASetWorldDifficulty pkt = (IPCPacket0ASetWorldDifficulty)ipc;
				if(!isServerStopped()) {
					if(pkt.difficulty == (byte)-1) {
						currentProcess.setDifficultyLockedForAllWorlds(true);
					}else {
						currentProcess.setDifficultyForAllWorlds(EnumDifficulty.getDifficultyEnum(pkt.difficulty));
					}
				}else {
					logger.warn("Client tried to set difficulty while server was stopped");
				}
				break;
			}
			case IPCPacket0BPause.ID: {
				IPCPacket0BPause pkt = (IPCPacket0BPause)ipc;
				if(!isServerStopped()) {
					currentProcess.setPaused(pkt.pause);
					sendIPCPacket(new IPCPacketFFProcessKeepAlive(IPCPacket0BPause.ID));
				}else {
					logger.error("Client tried to {} while server was stopped", pkt.pause ? "pause" : "unpause");
					sendTaskFailed();
				}
				break;
			}
			case IPCPacket0CPlayerChannel.ID: {
				IPCPacket0CPlayerChannel pkt = (IPCPacket0CPlayerChannel)ipc;
				if(!isServerStopped()) {
					if(pkt.open) {
						startPlayerConnnection(pkt.channel);
					}else {
						closeChannel(pkt.channel);
					}
				}else {
					logger.error("Client tried to {} channel server was stopped", pkt.open ? "open" : "close");
				}
				break;
			}
			case IPCPacket0EListWorlds.ID: {
				IPCPacket0EListWorlds pkt = (IPCPacket0EListWorlds)ipc;
				if(!isServerStopped()) {
					logger.error("Client tried to list worlds while server was running");
					sendTaskFailed();
				}else {
					String[] worlds = EaglerSaveFormat.worldsList.getAllLines();
					if(worlds == null) {
						sendIPCPacket(new IPCPacket16NBTList(IPCPacket16NBTList.WORLD_LIST, new LinkedList<>()));
						break;
					}
					LinkedHashSet<String> updatedList = new LinkedHashSet<>();
					LinkedList<NBTTagCompound> sendListNBT = new LinkedList<>();
					boolean rewrite = false;
					for(int i = 0; i < worlds.length; ++i) {
						String w = worlds[i].trim();
						if(w.length() > 0) {
							VFile2 vf = WorldsDB.newVFile(EaglerSaveFormat.worldsFolder, w, "level.dat");
							if(!vf.exists()) {
								vf = WorldsDB.newVFile(EaglerSaveFormat.worldsFolder, w, "level.dat_old");
							}
							if(vf.exists()) {
								try(InputStream dat = vf.getInputStream()) {
									if(updatedList.add(w)) {
										NBTTagCompound worldDatNBT = CompressedStreamTools.readCompressed(dat);
										worldDatNBT.setString("folderNameEagler", w);
										sendListNBT.add(worldDatNBT);
									}else {
										rewrite = true;
									}
									continue;
								}catch(IOException e) {
									// shit fuck
								}
							}
							rewrite = true;
							logger.error("World level.dat for '{}' was not found, attempting to delete", w);
							if(!saveFormat.deleteWorldDirectory(w)) {
								logger.error("Failed to delete '{}'! It will be removed from the worlds list anyway", w);
							}
						}else {
							rewrite = true;
						}
					}
					if(rewrite) {
						EaglerSaveFormat.worldsList.setAllChars(String.join("\n", updatedList));
					}
					sendIPCPacket(new IPCPacket16NBTList(IPCPacket16NBTList.WORLD_LIST, sendListNBT));
				}
				break;
			}
			case IPCPacket14StringList.ID: {
				IPCPacket14StringList pkt = (IPCPacket14StringList)ipc;
				switch(pkt.opCode) {
				case IPCPacket14StringList.LOCALE:
					StringTranslate.initServer(pkt.stringList);
					break;
				//case IPCPacket14StringList.STAT_GUID:
				//	AchievementMap.init(pkt.stringList);
				//	AchievementList.init();
				//	break;
				default:
					logger.error("Strange string list 0x{} with length{} recieved", Integer.toHexString(pkt.opCode), pkt.stringList.size());
					break;
				}
				break;
			}
			case IPCPacket17ConfigureLAN.ID: {

				IPCPacket17ConfigureLAN pkt = (IPCPacket17ConfigureLAN)ipc;
				if(!pkt.iceServers.isEmpty() && ServerPlatformSingleplayer.getClientConfigAdapter().isAllowVoiceClient()) {
					currentProcess.enableVoice(pkt.iceServers.toArray(new String[pkt.iceServers.size()]));
				}
				currentProcess.getConfigurationManager().configureLAN(pkt.gamemode, pkt.cheats, pkt.resourcePackUrl,
						pkt.resourcePackHash, pkt.requirePack, pkt.relayUri, pkt.code); // don't use iceServers

				break;
			}
			case IPCPacket1EKickPlayer.ID: {
                IPCPacket1EKickPlayer pkt = (IPCPacket1EKickPlayer) ipc;
                if (isServerStopped()) {
                    logger.error("Client tried to kick player '{}' while server was stopped", pkt.playerName);
                    sendTaskFailed();
                } else {
                    EntityPlayerMP target = currentProcess.getConfigurationManager()
                            .getPlayerByUsername(pkt.playerName);
                    if (target != null) {
                        String reason = (pkt.reason == null || pkt.reason.isEmpty())
                                ? StatCollector.translateToLocal("commands.kick.default") : pkt.reason;
                        target.playerNetServerHandler.kickPlayerFromServer(reason);
                        logger.info("Host kicked player '{}'", pkt.playerName);
                    } else {
                        logger.warn("Kick: player '{}' not found online", pkt.playerName);
                    }
                }
                break;
            }
 
            case IPCPacket1FBanPlayer.ID: {
                IPCPacket1FBanPlayer pkt = (IPCPacket1FBanPlayer) ipc;
                if (pkt.ban) {
                    actxmiscdata.session_bans.add(pkt.playerName);
                    logger.info("Session-banned player '{}'", pkt.playerName);
                    // Also kick them immediately if they're online
                    if (!isServerStopped()) {
                        EntityPlayerMP target = currentProcess.getConfigurationManager()
                                .getPlayerByUsername(pkt.playerName);
                        if (target != null) {
                            target.playerNetServerHandler.kickPlayerFromServer("You have been banned from this session.");
                        }
                    }
                } else {
                    actxmiscdata.session_bans.remove(pkt.playerName);
                    logger.info("Session-unbanned player '{}'", pkt.playerName);
                }
                break;
            }
 
            case IPCPacket20SetGamemode.ID: {
                IPCPacket20SetGamemode pkt = (IPCPacket20SetGamemode) ipc;
                if (isServerStopped()) {
                    logger.error("Client tried to set gamemode for '{}' while server was stopped", pkt.playerName);
                    sendTaskFailed();
                } else {
                    EntityPlayerMP target = currentProcess.getConfigurationManager()
                            .getPlayerByUsername(pkt.playerName);
                    if (target != null) {
                        GameType gt = GameType.getByID(pkt.gameModeId);
                        target.setGameType(gt);
                        logger.info("Set gamemode of '{}' to {}", pkt.playerName, gt.getName());
                    } else {
                        logger.warn("SetGamemode: player '{}' not found online", pkt.playerName);
                    }
                }
                break;
            }
 
            case IPCPacket21TeleportPlayer.ID: {
                IPCPacket21TeleportPlayer pkt = (IPCPacket21TeleportPlayer) ipc;
                if (isServerStopped()) {
                    logger.error("Client tried to teleport while server was stopped");
                    sendTaskFailed();
                } else {
                    // Determine who moves and where
                    String moverName = pkt.toTarget ? pkt.subjectName : pkt.targetName;
                    String destName  = pkt.toTarget ? pkt.targetName  : pkt.subjectName;
 
                    EntityPlayerMP mover = currentProcess.getConfigurationManager()
                            .getPlayerByUsername(moverName);
                    EntityPlayerMP dest  = currentProcess.getConfigurationManager()
                            .getPlayerByUsername(destName);
 
                    if (mover != null && dest != null) {
                        mover.mountEntity(null); // dismount before teleporting
                        mover.playerNetServerHandler.setPlayerLocation(
                                dest.posX, dest.posY, dest.posZ,
                                mover.rotationYaw, mover.rotationPitch);
                        logger.info("Teleported '{}' to '{}'", moverName, destName);
                    } else {
                        logger.warn("Teleport: could not find '{}' or '{}'", moverName, destName);
                    }
                }
                break;
            }
            case IPCPacket22GiveItem.ID: {
                IPCPacket22GiveItem pkt = (IPCPacket22GiveItem) ipc;
                if (isServerStopped()) {
                    logger.error("Client tried to give item to '{}' while server was stopped", pkt.playerName);
                    sendTaskFailed();
                } else {
                    EntityPlayerMP target = currentProcess.getConfigurationManager()
                            .getPlayerByUsername(pkt.playerName);
                    if (target != null) {
                        net.minecraft.item.Item item = net.minecraft.item.Item.getItemById(pkt.itemId);
                        if (item != null) {
                            net.minecraft.item.ItemStack stack = new net.minecraft.item.ItemStack(
                                    item, pkt.count & 0xFF, pkt.damage & 0xFFFF);
                            boolean added = target.inventory.addItemStackToInventory(stack);
                            if (!added) {
                                // Drop at feet if inventory full
                                target.dropPlayerItemWithRandomChoice(stack, false);
                            }
                            target.inventoryContainer.detectAndSendChanges();
                            logger.info("Gave {}x item#{} to '{}'", pkt.count & 0xFF, pkt.itemId, pkt.playerName);
                        } else {
                            logger.warn("GiveItem: unknown item id {}", pkt.itemId);
                        }
                    } else {
                        logger.warn("GiveItem: player '{}' not found online", pkt.playerName);
                    }
                }
                break;
            }
            case IPCPacket23ClearInventory.ID: {
                IPCPacket23ClearInventory pkt = (IPCPacket23ClearInventory) ipc;
                if (isServerStopped()) {
                    logger.error("Client tried to clear inventory for '{}' while server was stopped", pkt.playerName);
                    sendTaskFailed();
                } else {
                    EntityPlayerMP target = currentProcess.getConfigurationManager()
                            .getPlayerByUsername(pkt.playerName);
                    if (target != null) {
                        // Clear all inventory slots (main + armor); setInventorySlotContents
                        // is universally available via IInventory interface.
                        for (int slotIdx = 0; slotIdx < target.inventory.getSizeInventory(); slotIdx++) {
                            target.inventory.setInventorySlotContents(slotIdx, null);
                        }
                        target.inventoryContainer.detectAndSendChanges();
                        logger.info("Cleared inventory of '{}'", pkt.playerName);
                    } else {
                        logger.warn("ClearInventory: player '{}' not found online", pkt.playerName);
                    }
                }
                break;
            }
 
            case IPCPacket24HealFeedPlayer.ID: {
                IPCPacket24HealFeedPlayer pkt = (IPCPacket24HealFeedPlayer) ipc;
                if (isServerStopped()) {
                    logger.error("Client tried to heal/feed '{}' while server was stopped", pkt.playerName);
                    sendTaskFailed();
                } else {
                    EntityPlayerMP target = currentProcess.getConfigurationManager()
                            .getPlayerByUsername(pkt.playerName);
                    if (target != null) {
                        if (pkt.isHeal()) {
                            target.setHealth(target.getMaxHealth());
                        }
                        if (pkt.isFeed()) {
                            target.getFoodStats().setFoodLevel(20);
                            target.getFoodStats().setFoodSaturationLevel(5.0f);
                        }
                        logger.info("Healed/fed '{}' (heal={}, feed={})",
                                pkt.playerName, pkt.isHeal(), pkt.isFeed());
                    } else {
                        logger.warn("HealFeed: player '{}' not found online", pkt.playerName);
                    }
                }
                break;
            }
			case IPCPacket18ClearPlayers.ID: {
				if(!isServerStopped()) {
					logger.error("Client tried to clear players while server was running");
					sendTaskFailed();
				}else {
					saveFormat.clearPlayers(((IPCPacket18ClearPlayers)ipc).worldName);
					sendIPCPacket(new IPCPacketFFProcessKeepAlive(IPCPacket18ClearPlayers.ID));
				}
				break;
			}
			case IPCPacket19Autosave.ID: {
				if(!isServerStopped()) {
					currentProcess.getConfigurationManager().saveAllPlayerData();
					currentProcess.saveAllWorlds(false);
					sendIPCPacket(new IPCPacketFFProcessKeepAlive(IPCPacket19Autosave.ID));
				}else {
					logger.error("Client tried to autosave while server was stopped");
					sendTaskFailed();
				}
				break;
			}
			case IPCPacket1BEnableLogging.ID: {
				enableLoggingRedirector(((IPCPacket1BEnableLogging)ipc).enable);
				break;
			}
			case IPCPacket25WandPos.ID: {
				IPCPacket25WandPos pkt = (IPCPacket25WandPos) ipc;
				if (!isServerStopped()) {
					EntityPlayerMP player = currentProcess.getConfigurationManager().getPlayerByUsername(pkt.playerName);
					if (player != null) {
						WorldEditCommand.handleWandClick(player, new BlockPos(pkt.x, pkt.y, pkt.z), pkt.clickType);
					}
				}
				break;
			}
			case IPCPacket26SetTabHeaderFooter.ID: {
				IPCPacket26SetTabHeaderFooter pkt = (IPCPacket26SetTabHeaderFooter) ipc;
				if (isServerStopped()) {
					logger.error("Client tried to set tab header/footer while server was stopped");
					sendTaskFailed();
				} else {
					currentProcess.getConfigurationManager().updateTabHeaderFooter(pkt.header, pkt.footer);
					logger.info("Updated tab header/footer");
				}
				break;
			}
			case IPCPacket27SetAnimation.ID: {
				IPCPacket27SetAnimation pkt = (IPCPacket27SetAnimation) ipc;
				if (isServerStopped()) {
					logger.error("Client tried to set animation '{}' while server was stopped", pkt.name);
					sendTaskFailed();
				} else {
					boolean ok = actxmiscdata.setAnimation(pkt.name, pkt.intervalMs, pkt.framesJoined);
					if (ok) {
						currentProcess.getConfigurationManager().broadcastTabHeaderFooter();
						logger.info("Set animation '{}' ({}ms interval)", pkt.name, pkt.intervalMs);
					} else {
						logger.warn("SetAnimation: rejected invalid name or empty frames for '{}'", pkt.name);
					}
				}
				break;
			}
			case IPCPacket28RemoveAnimation.ID: {
				IPCPacket28RemoveAnimation pkt = (IPCPacket28RemoveAnimation) ipc;
				if (isServerStopped()) {
					logger.error("Client tried to remove animation '{}' while server was stopped", pkt.name);
					sendTaskFailed();
				} else {
					boolean existed = actxmiscdata.removeAnimation(pkt.name);
					if (existed) {
						currentProcess.getConfigurationManager().broadcastTabHeaderFooter();
					}
					logger.info("Removed animation '{}': {}", pkt.name, existed ? "ok" : "not found");
				}
				break;
			}
			case IPCPacket29MiscData.ID: {
				IPCPacket29MiscData pkt = (IPCPacket29MiscData) ipc;
				net.minecraft.server.management.ServerConfigurationManager scm =
								isServerStopped() ? null : currentProcess.getConfigurationManager();
				switch (pkt.op) {
				case IPCPacket29MiscData.OP_EXPORT_REQUEST:
						sendIPCPacket(new IPCPacket29MiscData(IPCPacket29MiscData.OP_EXPORT_REPLY,
										actxmiscdata.getExportBytes()));
						break;
				case IPCPacket29MiscData.OP_IMPORT:
						actxmiscdata.replaceFileWith(pkt.data, scm);
						if (scm != null) scm.updateTabHeaderFooter(actxmiscdata.getTabHeader(), actxmiscdata.getTabFooter());
						logger.info("Imported actx_misc.yml ({} bytes)", pkt.data.length);
						break;
				case IPCPacket29MiscData.OP_WIPE:
						actxmiscdata.wipeAll(scm);
						if (scm != null) scm.updateTabHeaderFooter(actxmiscdata.getTabHeader(), actxmiscdata.getTabFooter());
						logger.info("Wiped actx_misc.yml");
						break;
				}
				break;
			}
			case IPCPacket2ABossBar.ID: {
				IPCPacket2ABossBar pkt = (IPCPacket2ABossBar) ipc;
				if (isServerStopped()) {
						logger.error("Client tried to set the boss bar while server was stopped");
						sendTaskFailed();
				} else {
						currentProcess.getConfigurationManager().updateBossBar(pkt.enabled, pkt.title);
						logger.info("Updated boss bar");
				}
				break;
			}
			default: 
				logger.error("IPC packet type 0x{} class \"{}\" was not handled", Integer.toHexString(id), ipc.getClass().getSimpleName());
				sendTaskFailed();
				break;
			}
		}catch(Throwable t) {
			logger.error("IPC packet type 0x{} class \"{}\" was not processed correctly", Integer.toHexString(id), ipc.getClass().getSimpleName());
			logger.error(t);
			sendIPCPacket(new IPCPacket15Crashed("IPC packet type 0x" + Integer.toHexString(id) + " class \"" + ipc.getClass().getSimpleName() + "\" was not processed correctly!\n\n" + EagRuntime.getStackTrace(t)));
			sendTaskFailed();
		}
	}

	public static void enableLoggingRedirector(boolean en) {
		LogManager.logRedirector = en ? new ILogRedirector() {
			@Override
			public void log(String txt, boolean err) {
				sendLogMessagePacket(txt, err);
			}
		} : null;
	}

	public static void sendLogMessagePacket(String txt, boolean err) {
		sendIPCPacket(new IPCPacket1ALoggerMessage(txt, err));
	}

	public static void sendIPCPacket(IPCPacketBase ipc) {
		byte[] pkt;
		try {
			pkt = packetManagerInstance.IPCSerialize(ipc);
		}catch (IOException ex) {
			throw new RuntimeException("Failed to serialize IPC packet", ex);
		}
		ServerPlatformSingleplayer.sendPacket(new IPCPacketData(SingleplayerServerController.IPC_CHANNEL, pkt));
	}

	public static void reportTPS(List<String> texts) {
		sendIPCPacket(new IPCPacket14StringList(IPCPacket14StringList.SERVER_TPS, texts));
	}

	public static void sendTaskFailed() {
		sendIPCPacket(new IPCPacketFFProcessKeepAlive(IPCPacketFFProcessKeepAlive.FAILURE));
	}

	public static void sendProgress(String updateMessage, float updateProgress) {
		sendIPCPacket(new IPCPacket0DProgressUpdate(updateMessage, updateProgress));
	}

	private static boolean isServerStopped() {
		return currentProcess == null || !currentProcess.isServerRunning();
	}

	private static void tryStopServer() {
		if(!isServerStopped()) {
			currentProcess.stopServer();
		}
		currentProcess = null;
	}

	private static void mainLoop(boolean singleThreadMode) {
		processAsyncMessageQueue();
		
		if(currentProcess != null) {
			if(currentProcess.isServerRunning()) {
				currentProcess.mainLoop(singleThreadMode);
			}
			if(!currentProcess.isServerRunning()) {
				currentProcess.stopServer();
				currentProcess = null;
				sendIPCPacket(new IPCPacketFFProcessKeepAlive(IPCPacket01StopServer.ID));
			}
		}else {
			if(!singleThreadMode) {
				EagUtils.sleep(50);
			}
		}
	}

	public static void serverMain() {
		try {
			currentProcess = null;
			logger.info("Starting EaglercraftX integrated server worker...");
			
			if(ServerPlatformSingleplayer.getWorldsDatabase().isRamdisk()) {
				sendIPCPacket(new IPCPacket1CIssueDetected(IPCPacket1CIssueDetected.ISSUE_RAMDISK_MODE));
			}
			
			// signal thread startup successful
			sendIPCPacket(new IPCPacketFFProcessKeepAlive(0xFF));
			
			ServerPlatformSingleplayer.setCrashCallbackWASM(EaglerIntegratedServerWorker::sendIntegratedServerCrashWASMCB);
			
			while(true) {
				mainLoop(false);
				ServerPlatformSingleplayer.immediateContinue();
			}
		}catch(Throwable tt) {
			if(tt instanceof ReportedException) {
				String fullReport = ((ReportedException)tt).getCrashReport().getCompleteReport();
				logger.error(fullReport);
				sendIPCPacket(new IPCPacket15Crashed(fullReport));
			}else {
				logger.error("Server process encountered a fatal error!");
				logger.error(tt);
				sendIPCPacket(new IPCPacket15Crashed("SERVER PROCESS EXITED!\n\n" + EagRuntime.getStackTrace(tt)));
			}
		}finally {
			if(!isServerStopped()) {
				try {
					currentProcess.stopServer();
				}catch(Throwable t) {
					logger.error("Encountered exception while stopping server!");
					logger.error(t);
				}
			}
			logger.error("Server process exited!");
			sendIPCPacket(new IPCPacketFFProcessKeepAlive(IPCPacketFFProcessKeepAlive.EXITED));
		}
	}

	public static void singleThreadMain() {
		logger.info("Starting EaglercraftX integrated server worker...");
		if(ServerPlatformSingleplayer.getWorldsDatabase().isRamdisk()) {
			sendIPCPacket(new IPCPacket1CIssueDetected(IPCPacket1CIssueDetected.ISSUE_RAMDISK_MODE));
		}
		sendIPCPacket(new IPCPacketFFProcessKeepAlive(0xFF));
	}

	public static void singleThreadUpdate() {
		mainLoop(true);
	}

	public static void sendIntegratedServerCrashWASMCB(String stringValue, boolean terminated) {
		sendIPCPacket(new IPCPacket15Crashed(stringValue));
		if(terminated) {
			sendIPCPacket(new IPCPacketFFProcessKeepAlive(IPCPacketFFProcessKeepAlive.EXITED));
		}
	}

}