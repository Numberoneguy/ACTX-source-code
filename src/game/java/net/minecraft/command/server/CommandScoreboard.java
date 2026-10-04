package net.minecraft.command.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.actx.data_o_algo.actxmiscdata;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class CommandScoreboard extends CommandBase {

	// Global storage wrapper for score entry flags accessible by the GUI renderer
	public static class ScoreboardFlags {
		private static final Map<String, Boolean> visibilityFlags = new HashMap<>();

		public static void setVisibility(String entryName, boolean visible) {
			visibilityFlags.put(entryName, visible);
		}

		public static boolean isVisible(String entryName) {
			Boolean vis = visibilityFlags.get(entryName);
			return vis == null || vis;
		}
	}

	// Generic named on/off flags for /scoreboard flags <name> <true|false>,
	// e.g. hideNumbers -- distinct from ScoreboardFlags above, which is
	// per-entry visibility rather than a board-wide toggle.
	public static class GlobalFlags {
		private static final Map<String, Boolean> flags = new HashMap<>();

		public static void set(String flagName, boolean value) {
			flags.put(flagName.toLowerCase(), value);
		}

		public static boolean get(String flagName, boolean defaultValue) {
			Boolean v = flags.get(flagName.toLowerCase());
			return v == null ? defaultValue : v;
		}
	}

	public String getCommandName() {
		return "scoreboard";
	}

	public int getRequiredPermissionLevel() {
		return 2;
	}

	public String getCommandUsage(ICommandSender var1) {
		return "commands.scoreboard.usage";
	}

	public void processCommand(ICommandSender parICommandSender, String[] parArrayOfString) throws CommandException {
		if (!this.func_175780_b(parICommandSender, parArrayOfString)) {
			if (parArrayOfString.length < 1) {
				throw new WrongUsageException("commands.scoreboard.usage", new Object[0]);
			} else {
				if (parArrayOfString[0].equalsIgnoreCase("objectives")) {
					if (parArrayOfString.length == 1) {
						throw new WrongUsageException("commands.scoreboard.objectives.usage", new Object[0]);
					}

					if (parArrayOfString[1].equalsIgnoreCase("list")) {
						this.listObjectives(parICommandSender);
					} else if (parArrayOfString[1].equalsIgnoreCase("add")) {
						if (parArrayOfString.length < 4) {
							throw new WrongUsageException("commands.scoreboard.objectives.add.usage", new Object[0]);
						}

						this.addObjective(parICommandSender, parArrayOfString, 2);
					} else if (parArrayOfString[1].equalsIgnoreCase("remove")) {
						if (parArrayOfString.length != 3) {
							throw new WrongUsageException("commands.scoreboard.objectives.remove.usage", new Object[0]);
						}

						this.removeObjective(parICommandSender, parArrayOfString[2]);
					} else if (parArrayOfString[1].equalsIgnoreCase("save")) {
						if (parArrayOfString.length < 3) {
							throw new WrongUsageException("commands.scoreboard.objectives.save.usage", new Object[0]);
						}

						this.saveObjective(parICommandSender, parArrayOfString);
					} else if (parArrayOfString[1].equalsIgnoreCase("load")) {
						if (parArrayOfString.length < 3) {
							throw new WrongUsageException("commands.scoreboard.objectives.load.usage", new Object[0]);
						}

						this.loadObjective(parICommandSender, parArrayOfString);
					} else {
						if (!parArrayOfString[1].equalsIgnoreCase("setdisplay")) {
							throw new WrongUsageException("commands.scoreboard.objectives.usage", new Object[0]);
						}

						if (parArrayOfString.length != 3 && parArrayOfString.length != 4) {
							throw new WrongUsageException("commands.scoreboard.objectives.setdisplay.usage",
									new Object[0]);
						}

						this.setObjectiveDisplay(parICommandSender, parArrayOfString, 2);
					}
				} else if (parArrayOfString[0].equalsIgnoreCase("players")) {
					if (parArrayOfString.length == 1) {
						throw new WrongUsageException("commands.scoreboard.players.usage", new Object[0]);
					}

					parArrayOfString = mergeQuotedArgs(parICommandSender, parArrayOfString, 2);
					if (parArrayOfString == null) {
						return;
					}

					if (parArrayOfString[1].equalsIgnoreCase("list")) {
						if (parArrayOfString.length > 3) {
							throw new WrongUsageException("commands.scoreboard.players.list.usage", new Object[0]);
						}

						this.listPlayers(parICommandSender, parArrayOfString, 2);
					} else if (parArrayOfString[1].equalsIgnoreCase("add")) {
						if (parArrayOfString.length < 5) {
							throw new WrongUsageException("commands.scoreboard.players.add.usage", new Object[0]);
						}

						this.setPlayer(parICommandSender, parArrayOfString, 2);
					} else if (parArrayOfString[1].equalsIgnoreCase("remove")) {
						if (parArrayOfString.length < 5) {
							throw new WrongUsageException("commands.scoreboard.players.remove.usage", new Object[0]);
						}

						this.setPlayer(parICommandSender, parArrayOfString, 2);
					} else if (parArrayOfString[1].equalsIgnoreCase("set")) {
						if (parArrayOfString.length < 5) {
							throw new WrongUsageException("commands.scoreboard.players.set.usage", new Object[0]);
						}

						this.setPlayer(parICommandSender, parArrayOfString, 2);
					} else if (parArrayOfString[1].equalsIgnoreCase("reset")) {
						if (parArrayOfString.length != 3 && parArrayOfString.length != 4) {
							throw new WrongUsageException("commands.scoreboard.players.reset.usage", new Object[0]);
						}

						this.resetPlayers(parICommandSender, parArrayOfString, 2);
					} else if (parArrayOfString[1].equalsIgnoreCase("enable")) {
						if (parArrayOfString.length != 4) {
							throw new WrongUsageException("commands.scoreboard.players.enable.usage", new Object[0]);
						}

						this.func_175779_n(parICommandSender, parArrayOfString, 2);
					} else if (parArrayOfString[1].equalsIgnoreCase("test")) {
						if (parArrayOfString.length != 5 && parArrayOfString.length != 6) {
							throw new WrongUsageException("commands.scoreboard.players.test.usage", new Object[0]);
						}

						this.func_175781_o(parICommandSender, parArrayOfString, 2);
					} else if (parArrayOfString[1].equalsIgnoreCase("flag")) {
						// Custom Flag handling: /scoreboard players flag set "<name>" visibility <show/hide>
						if (parArrayOfString.length < 6 || !parArrayOfString[2].equalsIgnoreCase("set") || !parArrayOfString[4].equalsIgnoreCase("visibility")) {
							throw new WrongUsageException("commands.scoreboard.players.flag.usage", new Object[0]);
						}
						String targetName = parArrayOfString[3];
						String value = parArrayOfString[5];
						boolean visible = !value.equalsIgnoreCase("hide");
						ScoreboardFlags.setVisibility(targetName, visible);
						notifyOperators(parICommandSender, this, "commands.scoreboard.players.flag.success",
								new Object[] { targetName, visible ? "show" : "hide" });
					} else {
						if (!parArrayOfString[1].equalsIgnoreCase("operation")) {
							throw new WrongUsageException("commands.scoreboard.players.usage", new Object[0]);
						}

						if (parArrayOfString.length != 7) {
							throw new WrongUsageException("commands.scoreboard.players.operation.usage", new Object[0]);
						}

						this.func_175778_p(parICommandSender, parArrayOfString, 2);
					}
				} else if (parArrayOfString[0].equalsIgnoreCase("flags")) {
					if (parArrayOfString.length != 3
							|| (!parArrayOfString[2].equalsIgnoreCase("true") && !parArrayOfString[2].equalsIgnoreCase("false"))) {
						throw new WrongUsageException("commands.scoreboard.flags.usage", new Object[0]);
					}

					String flagName = parArrayOfString[1];
					boolean value = parArrayOfString[2].equalsIgnoreCase("true");
					GlobalFlags.set(flagName, value);
					notifyOperators(parICommandSender, this, "commands.scoreboard.flags.success",
							new Object[] { flagName, value });
				} else {
					if (!parArrayOfString[0].equalsIgnoreCase("teams")) {
						throw new WrongUsageException("commands.scoreboard.usage", new Object[0]);
					}

					if (parArrayOfString.length == 1) {
						throw new WrongUsageException("commands.scoreboard.teams.usage", new Object[0]);
					}

					if (parArrayOfString[1].equalsIgnoreCase("list")) {
						if (parArrayOfString.length > 3) {
							throw new WrongUsageException("commands.scoreboard.teams.list.usage", new Object[0]);
						}

						this.listTeams(parICommandSender, parArrayOfString, 2);
					} else if (parArrayOfString[1].equalsIgnoreCase("add")) {
						if (parArrayOfString.length < 3) {
							throw new WrongUsageException("commands.scoreboard.teams.add.usage", new Object[0]);
						}

						this.addTeam(parICommandSender, parArrayOfString, 2);
					} else if (parArrayOfString[1].equalsIgnoreCase("remove")) {
						if (parArrayOfString.length != 3) {
							throw new WrongUsageException("commands.scoreboard.teams.remove.usage", new Object[0]);
						}

						this.removeTeam(parICommandSender, parArrayOfString, 2);
					} else if (parArrayOfString[1].equalsIgnoreCase("empty")) {
						if (parArrayOfString.length != 3) {
							throw new WrongUsageException("commands.scoreboard.teams.empty.usage", new Object[0]);
						}

						this.emptyTeam(parICommandSender, parArrayOfString, 2);
					} else if (parArrayOfString[1].equalsIgnoreCase("join")) {
						if (parArrayOfString.length < 4
								&& (parArrayOfString.length != 3 || !(parICommandSender instanceof EntityPlayer))) {
							throw new WrongUsageException("commands.scoreboard.teams.join.usage", new Object[0]);
						}

						this.joinTeam(parICommandSender, parArrayOfString, 2);
					} else if (parArrayOfString[1].equalsIgnoreCase("leave")) {
						if (parArrayOfString.length < 3 && !(parICommandSender instanceof EntityPlayer)) {
							throw new WrongUsageException("commands.scoreboard.teams.leave.usage", new Object[0]);
						}

						this.leaveTeam(parICommandSender, parArrayOfString, 2);
					} else {
						if (!parArrayOfString[1].equalsIgnoreCase("option")) {
							throw new WrongUsageException("commands.scoreboard.teams.usage", new Object[0]);
						}

						if (parArrayOfString.length < 4) {
							throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
						}

						this.setTeamOption(parICommandSender, parArrayOfString, 2);
					}
				}

			}
		}
	}

	private boolean func_175780_b(ICommandSender parICommandSender, String[] parArrayOfString) throws CommandException {
		int i = -1;

		for (int j = 0; j < parArrayOfString.length; ++j) {
			if (this.isUsernameIndex(parArrayOfString, j) && "*".equals(parArrayOfString[j])) {
				if (i >= 0) {
					throw new CommandException("commands.scoreboard.noMultiWildcard", new Object[0]);
				}

				i = j;
			}
		}

		if (i < 0) {
			return false;
		} else {
			ArrayList arraylist1 = Lists.newArrayList(this.getScoreboard().getObjectiveNames());
			String s = parArrayOfString[i];
			ArrayList arraylist = Lists.newArrayList();

			for (int j = 0, l = arraylist1.size(); j < l; ++j) {
				String s1 = (String) arraylist1.get(j);
				parArrayOfString[i] = s1;

				try {
					this.processCommand(parICommandSender, parArrayOfString);
					arraylist.add(s1);
				} catch (CommandException commandexception) {
					ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation(
							commandexception.getMessage(), commandexception.getErrorObjects());
					chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
					parICommandSender.addChatMessage(chatcomponenttranslation);
				}
			}

			parArrayOfString[i] = s;
			parICommandSender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, arraylist.size());
			if (arraylist.size() == 0) {
				throw new WrongUsageException("commands.scoreboard.allMatchesFailed", new Object[0]);
			} else {
				return true;
			}
		}
	}

	private static String[] mergeQuotedArgs(ICommandSender sender, String[] args, int fromIndex) {
		if (fromIndex >= args.length) {
			return args;
		}

		ArrayList<String> merged = Lists.newArrayList();
		for (int k = 0; k < fromIndex; ++k) {
			merged.add(args[k]);
		}

		int i = fromIndex;
		while (i < args.length) {
			String token = args[i];
			if (token.length() > 0 && token.charAt(0) == '"') {
				String remainder = token.substring(1);
				if (remainder.length() > 0 && remainder.charAt(remainder.length() - 1) == '"') {
					merged.add(remainder.substring(0, remainder.length() - 1).replace('&', '\u00a7'));
					++i;
					continue;
				}

				StringBuilder sb = new StringBuilder(remainder);
				int j = i + 1;
				boolean closed = false;
				while (j < args.length) {
					String next = args[j];
					sb.append(' ');
					if (next.length() > 0 && next.charAt(next.length() - 1) == '"') {
						sb.append(next, 0, next.length() - 1);
						closed = true;
						++j;
						break;
					}

					sb.append(next);
					++j;
				}

				if (!closed) {
					IChatComponent message = new ChatComponentText(
							"Unclosed quote in scoreboard player name, make sure it has a closing \"");
					message.getChatStyle().setColor(EnumChatFormatting.RED);
					sender.addChatMessage(message);
					return null;
				}

				merged.add(sb.toString().replace('&', '\u00a7'));
				i = j;
			} else {
				merged.add(token);
				++i;
			}
		}

		return merged.toArray(new String[merged.size()]);
	}

	protected Scoreboard getScoreboard() {
		return MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
	}

	protected ScoreObjective getObjective(String name, boolean edit) throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		ScoreObjective scoreobjective = scoreboard.getObjective(name);
		if (scoreobjective == null) {
			throw new CommandException("commands.scoreboard.objectiveNotFound", new Object[] { name });
		} else if (edit && scoreobjective.getCriteria().isReadOnly()) {
			throw new CommandException("commands.scoreboard.objectiveReadOnly", new Object[] { name });
		} else {
			return scoreobjective;
		}
	}

	protected ScorePlayerTeam getTeam(String name) throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		ScorePlayerTeam scoreplayerteam = scoreboard.getTeam(name);
		if (scoreplayerteam == null) {
			throw new CommandException("commands.scoreboard.teamNotFound", new Object[] { name });
		} else {
			return scoreplayerteam;
		}
	}

	protected void addObjective(ICommandSender sender, String[] args, int index) throws CommandException {
		String s = args[index++];
		String s1 = args[index++];
		Scoreboard scoreboard = this.getScoreboard();
		IScoreObjectiveCriteria iscoreobjectivecriteria = (IScoreObjectiveCriteria) IScoreObjectiveCriteria.INSTANCES
				.get(s1);
		if (iscoreobjectivecriteria == null) {
			throw new WrongUsageException("commands.scoreboard.objectives.add.wrongType", new Object[] { s1 });
		} else if (scoreboard.getObjective(s) != null) {
			throw new CommandException("commands.scoreboard.objectives.add.alreadyExists", new Object[] { s });
		} else if (s.length() > 16) {
			throw new SyntaxErrorException("commands.scoreboard.objectives.add.tooLong",
					new Object[] { s, Integer.valueOf(16) });
		} else if (s.length() == 0) {
			throw new WrongUsageException("commands.scoreboard.objectives.add.usage", new Object[0]);
		} else {
			if (args.length > index) {
				String s2 = getChatComponentFromNthArg(sender, args, index).getUnformattedText();
				if (s2.length() > 32) {
					throw new SyntaxErrorException("commands.scoreboard.objectives.add.displayTooLong",
							new Object[] { s2, Integer.valueOf(32) });
				}

				if (s2.length() > 0) {
					scoreboard.addScoreObjective(s, iscoreobjectivecriteria).setDisplayName(s2);
				} else {
					scoreboard.addScoreObjective(s, iscoreobjectivecriteria);
				}
			} else {
				scoreboard.addScoreObjective(s, iscoreobjectivecriteria);
			}

			notifyOperators(sender, this, "commands.scoreboard.objectives.add.success", new Object[] { s });
		}
	}

	protected void addTeam(ICommandSender sender, String[] args, int index) throws CommandException {
		String s = args[index++];
		Scoreboard scoreboard = this.getScoreboard();
		if (scoreboard.getTeam(s) != null) {
			throw new CommandException("commands.scoreboard.teams.add.alreadyExists", new Object[] { s });
		} else if (s.length() > 16) {
			throw new SyntaxErrorException("commands.scoreboard.teams.add.tooLong",
					new Object[] { s, Integer.valueOf(16) });
		} else if (s.length() == 0) {
			throw new WrongUsageException("commands.scoreboard.teams.add.usage", new Object[0]);
		} else {
			if (args.length > index) {
				String s1 = getChatComponentFromNthArg(sender, args, index).getUnformattedText();
				if (s1.length() > 32) {
					throw new SyntaxErrorException("commands.scoreboard.teams.add.displayTooLong",
							new Object[] { s1, Integer.valueOf(32) });
				}

				if (s1.length() > 0) {
					scoreboard.createTeam(s).setTeamName(s1);
				} else {
					scoreboard.createTeam(s);
				}
			} else {
				scoreboard.createTeam(s);
			}

			notifyOperators(sender, this, "commands.scoreboard.teams.add.success", new Object[] { s });
		}
	}

	protected void setTeamOption(ICommandSender sender, String[] args, int index) throws CommandException {
		ScorePlayerTeam scoreplayerteam = this.getTeam(args[index++]);
		if (scoreplayerteam != null) {
			String s = args[index++].toLowerCase();
			if (!s.equalsIgnoreCase("color") && !s.equalsIgnoreCase("friendlyfire")
					&& !s.equalsIgnoreCase("seeFriendlyInvisibles") && !s.equalsIgnoreCase("nametagVisibility")
					&& !s.equalsIgnoreCase("deathMessageVisibility") && !s.equalsIgnoreCase("prefix")
					&& !s.equalsIgnoreCase("suffix")) {
				throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
			} else if (args.length == 4) {
				if (s.equalsIgnoreCase("color")) {
					throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] { s,
							joinNiceStringFromCollection(EnumChatFormatting.getValidValues(true, false)) });
				} else if (s.equalsIgnoreCase("prefix") || s.equalsIgnoreCase("suffix")) {
					throw new WrongUsageException("commands.scoreboard.teams.option.noValue",
							new Object[] { s, "<text ...>" });
				} else if (!s.equalsIgnoreCase("friendlyfire") && !s.equalsIgnoreCase("seeFriendlyInvisibles")) {
					if (!s.equalsIgnoreCase("nametagVisibility") && !s.equalsIgnoreCase("deathMessageVisibility")) {
						throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
					} else {
						throw new WrongUsageException("commands.scoreboard.teams.option.noValue",
								new Object[] { s, joinNiceString(Team.EnumVisible.func_178825_a()) });
					}
				} else {
					throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] { s,
							joinNiceStringFromCollection(Arrays.asList(new String[] { "true", "false" })) });
				}
			} else {
				String s1 = args[index];
				if (s.equalsIgnoreCase("color")) {
					EnumChatFormatting enumchatformatting = EnumChatFormatting.getValueByName(s1);
					if (enumchatformatting == null || enumchatformatting.isFancyStyling()) {
						throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] { s,
								joinNiceStringFromCollection(EnumChatFormatting.getValidValues(true, false)) });
					}

					scoreplayerteam.setChatFormat(enumchatformatting);
					scoreplayerteam.setNamePrefix(enumchatformatting.toString());
					scoreplayerteam.setNameSuffix(EnumChatFormatting.RESET.toString());
				} else if (s.equalsIgnoreCase("friendlyfire")) {
					if (!s1.equalsIgnoreCase("true") && !s1.equalsIgnoreCase("false")) {
						throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] { s,
								joinNiceStringFromCollection(Arrays.asList(new String[] { "true", "false" })) });
					}

					scoreplayerteam.setAllowFriendlyFire(s1.equalsIgnoreCase("true"));
				} else if (s.equalsIgnoreCase("seeFriendlyInvisibles")) {
					if (!s1.equalsIgnoreCase("true") && !s1.equalsIgnoreCase("false")) {
						throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] { s,
								joinNiceStringFromCollection(Arrays.asList(new String[] { "true", "false" })) });
					}

					scoreplayerteam.setSeeFriendlyInvisiblesEnabled(s1.equalsIgnoreCase("true"));
				} else if (s.equalsIgnoreCase("nametagVisibility")) {
					Team.EnumVisible team$enumvisible = Team.EnumVisible.func_178824_a(s1);
					if (team$enumvisible == null) {
						throw new WrongUsageException("commands.scoreboard.teams.option.noValue",
								new Object[] { s, joinNiceString(Team.EnumVisible.func_178825_a()) });
					}

					scoreplayerteam.setNameTagVisibility(team$enumvisible);
				} else if (s.equalsIgnoreCase("deathMessageVisibility")) {
					Team.EnumVisible team$enumvisible1 = Team.EnumVisible.func_178824_a(s1);
					if (team$enumvisible1 == null) {
						throw new WrongUsageException("commands.scoreboard.teams.option.noValue",
								new Object[] { s, joinNiceString(Team.EnumVisible.func_178825_a()) });
					}
				scoreplayerteam.setDeathMessageVisibility(team$enumvisible1);
                } else if (s.equalsIgnoreCase("prefix")) {
                    String rawPrefix = buildString(args, index).replace('&', '\u00a7');
                    if (rawPrefix.length() <= 16) {
                        scoreplayerteam.setNamePrefix(rawPrefix);
                    } else {
                        IChatComponent message = new ChatComponentText("The prefix can be no longer than 16 characters");
                        message.getChatStyle().setColor(EnumChatFormatting.RED);
                        sender.addChatMessage(message);
                        return;
                    }
                } else if (s.equalsIgnoreCase("suffix")) {
                    String rawSuffix = buildString(args, index).replace('&', '\u00a7');
                    if (rawSuffix.length() <= 16) {
                        scoreplayerteam.setNameSuffix(rawSuffix);
                    } else {
                        IChatComponent message = new ChatComponentText("The suffix can be no longer than 16 characters");
                        message.getChatStyle().setColor(EnumChatFormatting.RED);
                        sender.addChatMessage(message);
                        return;
                    }
                }

				notifyOperators(sender, this, "commands.scoreboard.teams.option.success",
						new Object[] { s, scoreplayerteam.getRegisteredName(), s1 });
			}
		}
	}

	protected void removeTeam(ICommandSender parICommandSender, String[] parArrayOfString, int parInt1)
			throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		ScorePlayerTeam scoreplayerteam = this.getTeam(parArrayOfString[parInt1]);
		if (scoreplayerteam != null) {
			scoreboard.removeTeam(scoreplayerteam);
			notifyOperators(parICommandSender, this, "commands.scoreboard.teams.remove.success",
					new Object[] { scoreplayerteam.getRegisteredName() });
		}
	}

	protected void listTeams(ICommandSender parICommandSender, String[] parArrayOfString, int parInt1)
			throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		if (parArrayOfString.length > parInt1) {
			ScorePlayerTeam scoreplayerteam = this.getTeam(parArrayOfString[parInt1]);
			if (scoreplayerteam == null) {
				return;
			}

			Collection collection = scoreplayerteam.getMembershipCollection();
			parICommandSender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, collection.size());
			if (collection.size() <= 0) {
				throw new CommandException("commands.scoreboard.teams.list.player.empty",
						new Object[] { scoreplayerteam.getRegisteredName() });
			}

			ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation(
					"commands.scoreboard.teams.list.player.count",
					new Object[] { Integer.valueOf(collection.size()), scoreplayerteam.getRegisteredName() });
			chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
			parICommandSender.addChatMessage(chatcomponenttranslation);
			parICommandSender.addChatMessage(new ChatComponentText(joinNiceString(collection.toArray())));
		} else {
			Collection collection1 = scoreboard.getTeams();
			parICommandSender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, collection1.size());
			if (collection1.size() <= 0) {
				throw new CommandException("commands.scoreboard.teams.list.empty", new Object[0]);
			}

			ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation(
					"commands.scoreboard.teams.list.count", new Object[] { Integer.valueOf(collection1.size()) });
			chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
			parICommandSender.addChatMessage(chatcomponenttranslation1);

			for (ScorePlayerTeam scoreplayerteam1 : (Collection<ScorePlayerTeam>) collection1) {
				parICommandSender.addChatMessage(new ChatComponentTranslation("commands.scoreboard.teams.list.entry",
						new Object[] { scoreplayerteam1.getRegisteredName(), scoreplayerteam1.getTeamName(),
								Integer.valueOf(scoreplayerteam1.getMembershipCollection().size()) }));
			}
		}

	}

	protected void joinTeam(ICommandSender parICommandSender, String[] parArrayOfString, int parInt1)
			throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		String s = parArrayOfString[parInt1++];
		HashSet hashset = Sets.newHashSet();
		HashSet hashset1 = Sets.newHashSet();
		if (parICommandSender instanceof EntityPlayer && parInt1 == parArrayOfString.length) {
			String s4 = getCommandSenderAsPlayer(parICommandSender).getName();
			if (scoreboard.addPlayerToTeam(s4, s)) {
				hashset.add(s4);
			} else {
				hashset1.add(s4);
			}
		} else {
			while (parInt1 < parArrayOfString.length) {
				String s1 = parArrayOfString[parInt1++];
				if (s1.startsWith("@")) {
					List<Entity> lst = func_175763_c(parICommandSender, s1);
					for (int i = 0, l = lst.size(); i < l; ++i) {
						Entity entity = lst.get(i);
						String s3 = getEntityName(parICommandSender, entity.getUniqueID().toString());
						if (scoreboard.addPlayerToTeam(s3, s)) {
							hashset.add(s3);
						} else {
							hashset1.add(s3);
						}
					}
				} else {
					String s2 = getEntityName(parICommandSender, s1);
					if (scoreboard.addPlayerToTeam(s2, s)) {
						hashset.add(s2);
					} else {
						hashset1.add(s2);
					}
				}
			}
		}

		if (!hashset.isEmpty()) {
			parICommandSender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, hashset.size());
			notifyOperators(parICommandSender, this, "commands.scoreboard.teams.join.success", new Object[] {
					Integer.valueOf(hashset.size()), s, joinNiceString(hashset.toArray(new String[hashset.size()])) });
		}

		if (!hashset1.isEmpty()) {
			throw new CommandException("commands.scoreboard.teams.join.failure",
					new Object[] { Integer.valueOf(hashset1.size()), s,
							joinNiceString(hashset1.toArray(new String[hashset1.size()])) });
		}
	}

	protected void leaveTeam(ICommandSender parICommandSender, String[] parArrayOfString, int parInt1)
			throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		HashSet hashset = Sets.newHashSet();
		HashSet hashset1 = Sets.newHashSet();
		if (parICommandSender instanceof EntityPlayer && parInt1 == parArrayOfString.length) {
			String s3 = getCommandSenderAsPlayer(parICommandSender).getName();
			if (scoreboard.removePlayerFromTeams(s3)) {
				hashset.add(s3);
			} else {
				hashset1.add(s3);
			}
		} else {
			while (parInt1 < parArrayOfString.length) {
				String s = parArrayOfString[parInt1++];
				if (s.startsWith("@")) {
					List<Entity> lst = func_175763_c(parICommandSender, s);
					for (int i = 0, l = lst.size(); i < l; ++i) {
						Entity entity = lst.get(i);
						String s2 = getEntityName(parICommandSender, entity.getUniqueID().toString());
						if (scoreboard.removePlayerFromTeams(s2)) {
							hashset.add(s2);
						} else {
							hashset1.add(s2);
						}
					}
				} else {
					String s1 = getEntityName(parICommandSender, s);
					if (scoreboard.removePlayerFromTeams(s1)) {
						hashset.add(s1);
					} else {
						hashset1.add(s1);
					}
				}
			}
		}

		if (!hashset.isEmpty()) {
			parICommandSender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, hashset.size());
			notifyOperators(parICommandSender, this, "commands.scoreboard.teams.leave.success", new Object[] {
					Integer.valueOf(hashset.size()), joinNiceString(hashset.toArray(new String[hashset.size()])) });
		}

		if (!hashset1.isEmpty()) {
			throw new CommandException("commands.scoreboard.teams.leave.failure", new Object[] {
					Integer.valueOf(hashset1.size()), joinNiceString(hashset1.toArray(new String[hashset1.size()])) });
		}
	}

	protected void emptyTeam(ICommandSender parICommandSender, String[] parArrayOfString, int parInt1)
			throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		ScorePlayerTeam scoreplayerteam = this.getTeam(parArrayOfString[parInt1]);
		if (scoreplayerteam != null) {
			ArrayList<String> arraylist = Lists.newArrayList(scoreplayerteam.getMembershipCollection());
			parICommandSender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, arraylist.size());
			if (arraylist.isEmpty()) {
				throw new CommandException("commands.scoreboard.teams.empty.alreadyEmpty",
						new Object[] { scoreplayerteam.getRegisteredName() });
			} else {
				for (int i = 0, l = arraylist.size(); i < l; ++i) {
					scoreboard.removePlayerFromTeam(arraylist.get(i), scoreplayerteam);
				}

				notifyOperators(parICommandSender, this, "commands.scoreboard.teams.empty.success",
						new Object[] { Integer.valueOf(arraylist.size()), scoreplayerteam.getRegisteredName() });
			}
		}
	}

	protected void removeObjective(ICommandSender parICommandSender, String parString1) throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		ScoreObjective scoreobjective = this.getObjective(parString1, false);
		scoreboard.removeObjective(scoreobjective);
		notifyOperators(parICommandSender, this, "commands.scoreboard.objectives.remove.success",
				new Object[] { parString1 });
	}

	/** Joins args[from..] with single spaces (callers normalise the result, which turns those spaces into '-'). */
	private static String joinFrom(String[] args, int from) {
		StringBuilder sb = new StringBuilder();
		for (int i = from; i < args.length; ++i) {
			if (i > from) sb.append(' ');
			sb.append(args[i]);
		}
		return sb.toString();
	}

	/**
	 * /scoreboard objectives save <objective_name> [saved name...]
	 * Stores the objective's criteria, display name, render type, display slots, every score and hidden entries in
	 * actx_misc.yml. The saved name defaults to the objective name; any spaces in it become '-'.
	 */
	protected void saveObjective(ICommandSender sender, String[] args) throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		ScoreObjective objective = this.getObjective(args[2], false);
		String key = actxmiscdata.normalizeSavedKey(args.length > 3 ? joinFrom(args, 3) : objective.getName());
		if (key.isEmpty()) {
			throw new WrongUsageException("commands.scoreboard.objectives.save.usage", new Object[0]);
		}

		List<String> slots = new ArrayList<String>();
		for (int i = 0; i < 19; ++i) {
			if (scoreboard.getObjectiveInDisplaySlot(i) == objective) {
				String slotName = Scoreboard.getObjectiveDisplaySlot(i);
				if (slotName != null) slots.add(slotName);
			}
		}

		Map<String, Integer> scores = new LinkedHashMap<String, Integer>();
		List<String> hidden = new ArrayList<String>();
		for (Score score : (Collection<Score>) scoreboard.getSortedScores(objective)) {
			scores.put(score.getPlayerName(), Integer.valueOf(score.getScorePoints()));
			if (!ScoreboardFlags.isVisible(score.getPlayerName())) {
				hidden.add(score.getPlayerName());
			}
		}

		String renderType = objective.getRenderType() == null ? "INTEGER" : objective.getRenderType().name();
		actxmiscdata.saveObjective(key, new actxmiscdata.SavedObjective(objective.getName(),
				objective.getDisplayName(), objective.getCriteria().getName(), renderType, slots, scores, hidden));

		notifyOperators(sender, this, "commands.scoreboard.objectives.save.success",
				new Object[] { objective.getName(), key, Integer.valueOf(scores.size()) });
	}

	/**
	 * /scoreboard objectives load <saved_objective>
	 * Recreates a saved objective (settings, display slots, scores) in this world. Fails if nothing is saved under that
	 * name, if the saved data is invalid, or if an objective with that name already exists here.
	 */
	protected void loadObjective(ICommandSender sender, String[] args) throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		String key = actxmiscdata.normalizeSavedKey(joinFrom(args, 2));
		actxmiscdata.SavedObjective saved = actxmiscdata.getSavedObjective(key);
		if (saved == null) {
			throw new CommandException("commands.scoreboard.objectives.load.notSaved", new Object[] { key });
		}

		IScoreObjectiveCriteria criteria = (IScoreObjectiveCriteria) IScoreObjectiveCriteria.INSTANCES
				.get(saved.criteria);
		if (criteria == null) {
			throw new CommandException("commands.scoreboard.objectives.load.invalidCriteria",
					new Object[] { key, saved.criteria });
		}
		if (saved.name.length() == 0 || saved.name.length() > 16 || saved.name.indexOf(' ') >= 0
				|| saved.displayname.length() > 32) {
			throw new CommandException("commands.scoreboard.objectives.load.invalid", new Object[] { key });
		}
		if (scoreboard.getObjective(saved.name) != null) {
			throw new CommandException("commands.scoreboard.objectives.add.alreadyExists",
					new Object[] { saved.name });
		}

		ScoreObjective objective = scoreboard.addScoreObjective(saved.name, criteria);
		if (saved.displayname.length() > 0) {
			objective.setDisplayName(saved.displayname);
		}
		try {
			objective.setRenderType(IScoreObjectiveCriteria.EnumRenderType.valueOf(saved.rendertype));
		} catch (IllegalArgumentException ignored) {
			// unknown render type in the file -- keep the default
		}

		for (String slot : saved.slots) {
			int slotIndex = Scoreboard.getObjectiveDisplaySlotNumber(slot);
			if (slotIndex >= 0) {
				scoreboard.setObjectiveInDisplaySlot(slotIndex, objective);
			}
		}

		for (Map.Entry<String, Integer> entry : saved.scores.entrySet()) {
			if (entry.getKey().length() <= 40) {
				scoreboard.getValueFromObjective(entry.getKey(), objective).setScorePoints(entry.getValue().intValue());
			}
		}
		for (String entryName : saved.hidden) {
			ScoreboardFlags.setVisibility(entryName, false);
		}

		notifyOperators(sender, this, "commands.scoreboard.objectives.load.success",
				new Object[] { saved.name, key, Integer.valueOf(saved.scores.size()) });
	}

	protected void listObjectives(ICommandSender parICommandSender) throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		Collection collection = scoreboard.getScoreObjectives();
		if (collection.size() <= 0) {
			throw new CommandException("commands.scoreboard.objectives.list.empty", new Object[0]);
		} else {
			ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation(
					"commands.scoreboard.objectives.list.count", new Object[] { Integer.valueOf(collection.size()) });
			chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
			parICommandSender.addChatMessage(chatcomponenttranslation);

			for (ScoreObjective scoreobjective : (Collection<ScoreObjective>) collection) {
				parICommandSender.addChatMessage(new ChatComponentTranslation(
						"commands.scoreboard.objectives.list.entry", new Object[] { scoreobjective.getName(),
								scoreobjective.getDisplayName(), scoreobjective.getCriteria().getName() }));
			}

		}
	}

	protected void setObjectiveDisplay(ICommandSender parICommandSender, String[] parArrayOfString, int parInt1)
			throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		String s = parArrayOfString[parInt1++];
		int i = Scoreboard.getObjectiveDisplaySlotNumber(s);
		ScoreObjective scoreobjective = null;
		if (parArrayOfString.length == 4) {
			scoreobjective = this.getObjective(parArrayOfString[parInt1], false);
		}

		if (i < 0) {
			throw new CommandException("commands.scoreboard.objectives.setdisplay.invalidSlot", new Object[] { s });
		} else {
			scoreboard.setObjectiveInDisplaySlot(i, scoreobjective);
			if (scoreobjective != null) {
				notifyOperators(parICommandSender, this, "commands.scoreboard.objectives.setdisplay.successSet",
						new Object[] { Scoreboard.getObjectiveDisplaySlot(i), scoreobjective.getName() });
			} else {
				notifyOperators(parICommandSender, this, "commands.scoreboard.objectives.setdisplay.successCleared",
						new Object[] { Scoreboard.getObjectiveDisplaySlot(i) });
			}

		}
	}

	protected void listPlayers(ICommandSender parICommandSender, String[] parArrayOfString, int parInt1)
			throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		if (parArrayOfString.length > parInt1) {
			String s = getEntityName(parICommandSender, parArrayOfString[parInt1]);
			Map map = scoreboard.getObjectivesForEntity(s);
			parICommandSender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, map.size());
			if (map.size() <= 0) {
				throw new CommandException("commands.scoreboard.players.list.player.empty", new Object[] { s });
			}

			ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation(
					"commands.scoreboard.players.list.player.count", new Object[] { Integer.valueOf(map.size()), s });
			chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
			parICommandSender.addChatMessage(chatcomponenttranslation);

			for (Score score : (Collection<Score>) map.values()) {
				parICommandSender
						.addChatMessage(new ChatComponentTranslation("commands.scoreboard.teams.list.player.entry",
								new Object[] { Integer.valueOf(score.getScorePoints()),
										score.getObjective().getDisplayName(), score.getObjective().getName() }));
			}
		} else {
			Collection collection = scoreboard.getObjectiveNames();
			parICommandSender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, collection.size());
			if (collection.size() <= 0) {
				throw new CommandException("commands.scoreboard.players.list.empty", new Object[0]);
			}

			ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation(
					"commands.scoreboard.players.list.count", new Object[] { Integer.valueOf(collection.size()) });
			chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
			parICommandSender.addChatMessage(chatcomponenttranslation1);
			parICommandSender.addChatMessage(new ChatComponentText(joinNiceString(collection.toArray())));
		}

	}

	protected void setPlayer(ICommandSender parICommandSender, String[] parArrayOfString, int parInt1)
			throws CommandException {
		String s = parArrayOfString[parInt1 - 1];
		int i = parInt1;
		String s1 = getEntityName(parICommandSender, parArrayOfString[parInt1++]);
		if (s1.length() > 40) {
			throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong",
					new Object[] { s1, Integer.valueOf(40) });
		} else {
			ScoreObjective scoreobjective = this.getObjective(parArrayOfString[parInt1++], true);
			int j = s.equalsIgnoreCase("set") ? parseInt(parArrayOfString[parInt1++])
					: parseInt(parArrayOfString[parInt1++], 0);
			if (parArrayOfString.length > parInt1) {
				Entity entity = func_175768_b(parICommandSender, parArrayOfString[i]);

				try {
					NBTTagCompound nbttagcompound = JsonToNBT.getTagFromJson(buildString(parArrayOfString, parInt1));
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					entity.writeToNBT(nbttagcompound1);
					if (!NBTUtil.func_181123_a(nbttagcompound, nbttagcompound1, true)) {
						throw new CommandException("commands.scoreboard.players.set.tagMismatch", new Object[] { s1 });
					}
				} catch (NBTException nbtexception) {
					throw new CommandException("commands.scoreboard.players.set.tagError",
							new Object[] { nbtexception.getMessage() });
				}
			}

			Scoreboard scoreboard = this.getScoreboard();
			Score score = scoreboard.getValueFromObjective(s1, scoreobjective);
			if (s.equalsIgnoreCase("set")) {
				score.setScorePoints(j);
			} else if (s.equalsIgnoreCase("add")) {
				score.increseScore(j);
			} else {
				score.decreaseScore(j);
			}

			notifyOperators(parICommandSender, this, "commands.scoreboard.players.set.success",
					new Object[] { scoreobjective.getName(), s1, Integer.valueOf(score.getScorePoints()) });
		}
	}

	protected void resetPlayers(ICommandSender parICommandSender, String[] parArrayOfString, int parInt1)
			throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		String s = getEntityName(parICommandSender, parArrayOfString[parInt1++]);
		if (parArrayOfString.length > parInt1) {
			ScoreObjective scoreobjective = this.getObjective(parArrayOfString[parInt1++], false);
			scoreboard.removeObjectiveFromEntity(s, scoreobjective);
			notifyOperators(parICommandSender, this, "commands.scoreboard.players.resetscore.success",
					new Object[] { scoreobjective.getName(), s });
		} else {
			scoreboard.removeObjectiveFromEntity(s, (ScoreObjective) null);
			notifyOperators(parICommandSender, this, "commands.scoreboard.players.reset.success", new Object[] { s });
		}

	}

	protected void func_175779_n(ICommandSender parICommandSender, String[] parArrayOfString, int parInt1)
			throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		String s = getPlayerName(parICommandSender, parArrayOfString[parInt1++]);
		if (s.length() > 40) {
			throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong",
					new Object[] { s, Integer.valueOf(40) });
		} else {
			ScoreObjective scoreobjective = this.getObjective(parArrayOfString[parInt1], false);
			if (scoreobjective.getCriteria() != IScoreObjectiveCriteria.TRIGGER) {
				throw new CommandException("commands.scoreboard.players.enable.noTrigger",
						new Object[] { scoreobjective.getName() });
			} else {
				Score score = scoreboard.getValueFromObjective(s, scoreobjective);
				score.setLocked(false);
				notifyOperators(parICommandSender, this, "commands.scoreboard.players.enable.success",
						new Object[] { scoreobjective.getName(), s });
			}
		}
	}

	protected void func_175781_o(ICommandSender parICommandSender, String[] parArrayOfString, int parInt1)
			throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		String s = getEntityName(parICommandSender, parArrayOfString[parInt1++]);
		if (s.length() > 40) {
			throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong",
					new Object[] { s, Integer.valueOf(40) });
		} else {
			ScoreObjective scoreobjective = this.getObjective(parArrayOfString[parInt1++], false);
			if (!scoreboard.entityHasObjective(s, scoreobjective)) {
				throw new CommandException("commands.scoreboard.players.test.notFound",
						new Object[] { scoreobjective.getName(), s });
			} else {
				int i = parArrayOfString[parInt1].equals("*") ? Integer.MIN_VALUE : parseInt(parArrayOfString[parInt1]);
				++parInt1;
				int j = parInt1 < parArrayOfString.length && !parArrayOfString[parInt1].equals("*")
						? parseInt(parArrayOfString[parInt1], i)
						: Integer.MAX_VALUE;
				Score score = scoreboard.getValueFromObjective(s, scoreobjective);
				if (score.getScorePoints() >= i && score.getScorePoints() <= j) {
					notifyOperators(parICommandSender, this, "commands.scoreboard.players.test.success", new Object[] {
							Integer.valueOf(score.getScorePoints()), Integer.valueOf(i), Integer.valueOf(j) });
				} else {
					throw new CommandException("commands.scoreboard.players.test.failed", new Object[] {
							Integer.valueOf(score.getScorePoints()), Integer.valueOf(i), Integer.valueOf(j) });
				}
			}
		}
	}

	protected void func_175778_p(ICommandSender parICommandSender, String[] parArrayOfString, int parInt1)
			throws CommandException {
		Scoreboard scoreboard = this.getScoreboard();
		String s = getEntityName(parICommandSender, parArrayOfString[parInt1++]);
		ScoreObjective scoreobjective = this.getObjective(parArrayOfString[parInt1++], true);
		String s1 = parArrayOfString[parInt1++];
		String s2 = getEntityName(parICommandSender, parArrayOfString[parInt1++]);
		ScoreObjective scoreobjective1 = this.getObjective(parArrayOfString[parInt1], false);
		if (s.length() > 40) {
			throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong",
					new Object[] { s, Integer.valueOf(40) });
		} else if (s2.length() > 40) {
			throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong",
					new Object[] { s2, Integer.valueOf(40) });
		} else {
			Score score = scoreboard.getValueFromObjective(s, scoreobjective);
			if (!scoreboard.entityHasObjective(s2, scoreobjective1)) {
				throw new CommandException("commands.scoreboard.players.test.notFound",
						new Object[] { scoreobjective1.getName(), s2 });
			} else {
				Score score1 = scoreboard.getValueFromObjective(s2, scoreobjective1);
				if (s1.equals("+=")) {
					score.setScorePoints(score.getScorePoints() + score1.getScorePoints());
				} else if (s1.equals("-=")) {
					score.setScorePoints(score.getScorePoints() - score1.getScorePoints());
				} else if (s1.equals("*=")) {
					score.setScorePoints(score.getScorePoints() * score1.getScorePoints());
				} else if (s1.equals("/=")) {
					if (score1.getScorePoints() != 0) {
						score.setScorePoints(score.getScorePoints() / score1.getScorePoints());
					}
				} else if (s1.equals("%=")) {
					if (score1.getScorePoints() != 0) {
						score.setScorePoints(score.getScorePoints() % score1.getScorePoints());
					}
				} else if (s1.equals("=")) {
					score.setScorePoints(score1.getScorePoints());
				} else if (s1.equals("<")) {
					score.setScorePoints(Math.min(score.getScorePoints(), score1.getScorePoints()));
				} else if (s1.equals(">")) {
					score.setScorePoints(Math.max(score.getScorePoints(), score1.getScorePoints()));
				} else {
					if (!s1.equals("><")) {
						throw new CommandException("commands.scoreboard.players.operation.invalidOperation",
								new Object[] { s1 });
					}

					int i = score.getScorePoints();
					score.setScorePoints(score1.getScorePoints());
					score1.setScorePoints(i);
				}

				notifyOperators(parICommandSender, this, "commands.scoreboard.players.operation.success",
						new Object[0]);
			}
		}
	}

	public List<String> addTabCompletionOptions(ICommandSender var1, String[] astring, BlockPos var3) {
		if (astring.length == 1) {
			return getListOfStringsMatchingLastWord(astring, new String[] { "objectives", "players", "teams", "flags" });
		} else {
			if (astring[0].equalsIgnoreCase("objectives")) {
				if (astring.length == 2) {
					return getListOfStringsMatchingLastWord(astring,
							new String[] { "list", "add", "remove", "setdisplay", "save", "load" });
				}

				if (astring[1].equalsIgnoreCase("add")) {
					if (astring.length == 4) {
						Set set = IScoreObjectiveCriteria.INSTANCES.keySet();
						return getListOfStringsMatchingLastWord(astring, set);
					}
				} else if (astring[1].equalsIgnoreCase("remove")) {
					if (astring.length == 3) {
						return getListOfStringsMatchingLastWord(astring, this.func_147184_a(false));
					}
				} else if (astring[1].equalsIgnoreCase("save")) {
					if (astring.length == 3) {
						return getListOfStringsMatchingLastWord(astring, this.func_147184_a(false));
					}
				} else if (astring[1].equalsIgnoreCase("load")) {
					if (astring.length == 3) {
						return getListOfStringsMatchingLastWord(astring, actxmiscdata.getSavedObjectiveKeys());
					}
				} else if (astring[1].equalsIgnoreCase("setdisplay")) {
					if (astring.length == 3) {
						return getListOfStringsMatchingLastWord(astring, Scoreboard.getDisplaySlotStrings());
					}

					if (astring.length == 4) {
						return getListOfStringsMatchingLastWord(astring, this.func_147184_a(false));
					}
				}
			} else if (astring[0].equalsIgnoreCase("players")) {
				if (astring.length == 2) {
					return getListOfStringsMatchingLastWord(astring,
							new String[] { "set", "add", "remove", "reset", "list", "enable", "test", "operation", "flag" });
				}

				if (astring[1].equalsIgnoreCase("flag")) {
					if (astring.length == 3) {
						return getListOfStringsMatchingLastWord(astring, new String[] { "set" });
					}
					if (astring.length == 5) {
						return getListOfStringsMatchingLastWord(astring, new String[] { "visibility" });
					}
					if (astring.length == 6) {
						return getListOfStringsMatchingLastWord(astring, new String[] { "show", "hide" });
					}
				}

				if (!astring[1].equalsIgnoreCase("set") && !astring[1].equalsIgnoreCase("add")
						&& !astring[1].equalsIgnoreCase("remove") && !astring[1].equalsIgnoreCase("reset")) {
					if (astring[1].equalsIgnoreCase("enable")) {
						if (astring.length == 3) {
							return getListOfStringsMatchingLastWord(astring,
									MinecraftServer.getServer().getAllUsernames());
						}

						if (astring.length == 4) {
							return getListOfStringsMatchingLastWord(astring, this.func_175782_e());
						}
					} else if (!astring[1].equalsIgnoreCase("list") && !astring[1].equalsIgnoreCase("test")) {
						if (astring[1].equalsIgnoreCase("operation")) {
							if (astring.length == 3) {
								return getListOfStringsMatchingLastWord(astring,
										this.getScoreboard().getObjectiveNames());
							}

							if (astring.length == 4) {
								return getListOfStringsMatchingLastWord(astring, this.func_147184_a(true));
							}

							if (astring.length == 5) {
								return getListOfStringsMatchingLastWord(astring,
										new String[] { "+=", "-=", "*=", "/=", "%=", "=", "<", ">", "><" });
							}

							if (astring.length == 6) {
								return getListOfStringsMatchingLastWord(astring,
										MinecraftServer.getServer().getAllUsernames());
							}

							if (astring.length == 7) {
								return getListOfStringsMatchingLastWord(astring, this.func_147184_a(false));
							}
						}
					} else {
						if (astring.length == 3) {
							return getListOfStringsMatchingLastWord(astring, this.getScoreboard().getObjectiveNames());
						}

						if (astring.length == 4 && astring[1].equalsIgnoreCase("test")) {
							return getListOfStringsMatchingLastWord(astring, this.func_147184_a(false));
						}
					}
				} else {
					if (astring.length == 3) {
						return getListOfStringsMatchingLastWord(astring, MinecraftServer.getServer().getAllUsernames());
					}

					if (astring.length == 4) {
						return getListOfStringsMatchingLastWord(astring, this.func_147184_a(true));
					}
				}
			} else if (astring[0].equalsIgnoreCase("teams")) {
				if (astring.length == 2) {
					return getListOfStringsMatchingLastWord(astring,
							new String[] { "add", "remove", "join", "leave", "empty", "list", "option" });
				}

				if (astring[1].equalsIgnoreCase("join")) {
					if (astring.length == 3) {
						return getListOfStringsMatchingLastWord(astring, this.getScoreboard().getTeamNames());
					}

					if (astring.length >= 4) {
						return getListOfStringsMatchingLastWord(astring, MinecraftServer.getServer().getAllUsernames());
					}
				} else {
					if (astring[1].equalsIgnoreCase("leave")) {
						return getListOfStringsMatchingLastWord(astring, MinecraftServer.getServer().getAllUsernames());
					}

					if (!astring[1].equalsIgnoreCase("empty") && !astring[1].equalsIgnoreCase("list")
							&& !astring[1].equalsIgnoreCase("remove")) {
						if (astring[1].equalsIgnoreCase("option")) {
							if (astring.length == 3) {
								return getListOfStringsMatchingLastWord(astring, this.getScoreboard().getTeamNames());
							}

							if (astring.length == 4) {
								return getListOfStringsMatchingLastWord(astring, new String[] { "color", "friendlyfire",
										"seeFriendlyInvisibles", "nametagVisibility", "deathMessageVisibility",
										"prefix", "suffix" });
							}

							if (astring.length == 5) {
								if (astring[3].equalsIgnoreCase("color")) {
									return getListOfStringsMatchingLastWord(astring,
											EnumChatFormatting.getValidValues(true, false));
								}

								if (astring[3].equalsIgnoreCase("nametagVisibility")
										|| astring[3].equalsIgnoreCase("deathMessageVisibility")) {
									return getListOfStringsMatchingLastWord(astring, Team.EnumVisible.func_178825_a());
								}

								if (astring[3].equalsIgnoreCase("friendlyfire")
										|| astring[3].equalsIgnoreCase("seeFriendlyInvisibles")) {
									return getListOfStringsMatchingLastWord(astring, new String[] { "true", "false" });
								}
							}
						}
					} else if (astring.length == 3) {
						return getListOfStringsMatchingLastWord(astring, this.getScoreboard().getTeamNames());
					}
				}
			} else if (astring[0].equalsIgnoreCase("flags")) {
				if (astring.length == 2) {
					return getListOfStringsMatchingLastWord(astring, new String[] { "hideNumbers" });
				}
				if (astring.length == 3) {
					return getListOfStringsMatchingLastWord(astring, new String[] { "true", "false" });
				}
			}

			return null;
		}
	}

	protected List<String> func_147184_a(boolean parFlag) {
		Collection collection = this.getScoreboard().getScoreObjectives();
		ArrayList arraylist = Lists.newArrayList();

		for (ScoreObjective scoreobjective : (Collection<ScoreObjective>) collection) {
			if (!parFlag || !scoreobjective.getCriteria().isReadOnly()) {
				arraylist.add(scoreobjective.getName());
			}
		}

		return arraylist;
	}

	protected List<String> func_175782_e() {
		Collection collection = this.getScoreboard().getScoreObjectives();
		ArrayList arraylist = Lists.newArrayList();

		for (ScoreObjective scoreobjective : (Collection<ScoreObjective>) collection) {
			if (scoreobjective.getCriteria() == IScoreObjectiveCriteria.TRIGGER) {
				arraylist.add(scoreobjective.getName());
			}
		}

		return arraylist;
	}

	public boolean isUsernameIndex(String[] astring, int i) {
		return !astring[0].equalsIgnoreCase("players") ? (astring[0].equalsIgnoreCase("teams") ? i == 2 : false)
				: (astring.length > 1 && astring[1].equalsIgnoreCase("operation") ? i == 2 || i == 5 : i == 2);
	}
}