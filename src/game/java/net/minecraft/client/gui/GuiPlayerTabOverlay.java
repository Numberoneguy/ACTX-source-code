package net.minecraft.client.gui;

import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import net.lax1dude.eaglercraft.v1_8.mojang.authlib.GameProfile;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings;

/**+
 * This portion of EaglercraftX contains deobfuscated Minecraft 1.8 source code.
 * * Minecraft 1.8.8 bytecode is (c) 2015 Mojang AB. "Do not distribute!"
 * Mod Coder Pack v9.18 deobfuscation configs are (c) Copyright by the MCP Team
 * * EaglercraftX 1.8 patch files (c) 2022-2025 lax1dude, ayunami2000. All Rights Reserved.
 * */
public class GuiPlayerTabOverlay extends Gui {
	private static final Ordering<NetworkPlayerInfo> field_175252_a = Ordering
			.from(new GuiPlayerTabOverlay.PlayerComparator());

	private static final int maxrows_per_column = 10;

	private final Minecraft mc;
	private final GuiIngame guiIngame;
	private IChatComponent footer;
	private IChatComponent header;
	private long lastTimeOpened;
	private boolean isBeingRendered;

	private final List<RemovableRow> removablePreviewRows = new ArrayList<RemovableRow>();

	private static class RemovableRow {
		final int x0, y0, x1, y1;
		final Object tag;

		RemovableRow(int x0, int y0, int x1, int y1, Object tag) {
			this.x0 = x0;
			this.y0 = y0;
			this.x1 = x1;
			this.y1 = y1;
			this.tag = tag;
		}
	}

	/** One row in a renderPreviewList() call. Not tied to any live network state. */
	public static class PreviewEntry {
		public final String displayName;
		public final ResourceLocation skinLocation;
		public final boolean removable;
		/** Opaque caller-supplied tag echoed back by getRemovablePreviewRowAt() when this
		 *  row is clicked, so the caller can identify which entry (out of possibly several
		 *  removable ones) it needs to remove. Unused when removable is false. */
		public final Object userData;
		/** Ping in ms to show as a ping-bars icon on this row (negative = "disconnected"
		 *  bars, same convention as NetworkPlayerInfo.getResponseTime()), or null to
		 *  draw no ping icon at all. */
		public final Integer pingMs;

		public PreviewEntry(String displayName, ResourceLocation skinLocation, boolean removable) {
			this(displayName, skinLocation, removable, null, null);
		}

		public PreviewEntry(String displayName, ResourceLocation skinLocation, boolean removable, Object userData) {
			this(displayName, skinLocation, removable, userData, null);
		}

		public PreviewEntry(String displayName, ResourceLocation skinLocation, boolean removable, Object userData,
				Integer pingMs) {
			this.displayName = displayName;
			this.skinLocation = skinLocation;
			this.removable = removable;
			this.userData = userData;
			this.pingMs = pingMs;
		}
	}

	public GuiPlayerTabOverlay(Minecraft mcIn, GuiIngame guiIngameIn) {
		this.mc = mcIn;
		this.guiIngame = guiIngameIn;
	}

	/**+
	 * Returns the name that should be rendered for the player supplied
	 */
	public String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn) {
		IChatComponent dname = networkPlayerInfoIn.getDisplayNameProfanityFilter();
		return dname != null ? dname.getFormattedText()
				: ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(),
						networkPlayerInfoIn.getGameProfileNameProfanityFilter());
	}

	/**+
	 * Called by GuiIngame to update the information stored in the
	 * playerlist, does not actually render the list, however.
	 */
	public void updatePlayerList(boolean willBeRendered) {
		if (willBeRendered && !this.isBeingRendered) {
			this.lastTimeOpened = Minecraft.getSystemTime();
		}

		this.isBeingRendered = willBeRendered;
	}

	/**+
	 * Renders the playerlist, its background, headers and footers.
	 */
	public void renderPlayerlist(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn) {
		NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
		List list = field_175252_a.sortedCopy(nethandlerplayclient.getPlayerInfoMap());
		int i = 0;
		int j = 0;

		for (int m = 0, n = list.size(); m < n; ++m) {
			NetworkPlayerInfo networkplayerinfo = (NetworkPlayerInfo) list.get(m);
			int k = this.mc.fontRendererObj.getStringWidth(this.getPlayerName(networkplayerinfo));
			i = Math.max(i, k);
			if (scoreObjectiveIn != null
					&& scoreObjectiveIn.getRenderType() != IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
				k = this.mc.fontRendererObj.getStringWidth(" " + scoreboardIn
						.getValueFromObjective(networkplayerinfo.getGameProfile().getName(), scoreObjectiveIn)
						.getScorePoints());
				j = Math.max(j, k);
			}
		}

		list = list.subList(0, Math.min(list.size(), 80));
		int l3 = list.size();
		int i4 = l3;

		int j4;
		for (j4 = 1; i4 > 20; i4 = (l3 + j4 - 1) / j4) {
			++j4;
		}

		boolean flag = true;
		int l;
		if (scoreObjectiveIn != null) {
			if (scoreObjectiveIn.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
				l = 90;
			} else {
				l = j;
			}
		} else {
			l = 0;
		}

		int i1 = Math.min(j4 * ((flag ? 9 : 0) + i + l + 13), width - 50) / j4;
		int j1 = width / 2 - (i1 * j4 + (j4 - 1) * 5) / 2;
		int k1 = 10;
		int l1 = i1 * j4 + (j4 - 1) * 5;
		List<String> list1 = null;
		List<String> list2 = null;
		if (this.header != null) {
			list1 = this.mc.fontRendererObj.listFormattedStringToWidth(this.header.getFormattedText(), width - 50);

			for (int m = 0, n = list1.size(); m < n; ++m) {
				l1 = Math.max(l1, this.mc.fontRendererObj.getStringWidth(list1.get(m)));
			}
		}

		if (this.footer != null) {
			list2 = this.mc.fontRendererObj.listFormattedStringToWidth(this.footer.getFormattedText(), width - 50);

			for (int m = 0, n = list2.size(); m < n; ++m) {
				l1 = Math.max(l1, this.mc.fontRendererObj.getStringWidth(list2.get(m)));
			}
		}

		if (list1 != null) {
			drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1,
					k1 + list1.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

			for (int m = 0, n = list1.size(); m < n; ++m) {
				String s3 = list1.get(m);
				int i2 = this.mc.fontRendererObj.getStringWidth(s3);
				this.mc.fontRendererObj.drawStringWithShadow(s3, (float) (width / 2 - i2 / 2), (float) k1, -1);
				k1 += this.mc.fontRendererObj.FONT_HEIGHT;
			}

			++k1;
		}

		drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + i4 * 9, Integer.MIN_VALUE);

		for (int k4 = 0; k4 < l3; ++k4) {
			int l4 = k4 / i4;
			int i5 = k4 % i4;
			int j2 = j1 + l4 * i1 + l4 * 5;
			int k2 = k1 + i5 * 9;
			drawRect(j2, k2, j2 + i1, k2 + 8, 553648127);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			if (k4 < list.size()) {
				NetworkPlayerInfo networkplayerinfo1 = (NetworkPlayerInfo) list.get(k4);
				String s1 = this.getPlayerName(networkplayerinfo1);
				GameProfile gameprofile = networkplayerinfo1.getGameProfile();
				EntityPlayer trackedPlayer = this.mc.theWorld.getPlayerEntityByUUID(gameprofile.getId());
				if (flag) {
					EntityPlayer entityplayer = trackedPlayer;
					boolean flag1 = entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.CAPE)
							&& (gameprofile.getName().equals("Dinnerbone") || gameprofile.getName().equals("Grumm"));
					this.mc.getTextureManager().bindTexture(networkplayerinfo1.getLocationSkin());
					int l2 = 8 + (flag1 ? 8 : 0);
					int i3 = 8 * (flag1 ? -1 : 1);
					Gui.drawScaledCustomSizeModalRect(j2, k2, 8.0F, (float) l2, 8, i3, 8, 8, 64.0F, 64.0F);
					if (entityplayer == null || entityplayer.isWearing(EnumPlayerModelParts.HAT)) {
						int j3 = 8 + (flag1 ? 8 : 0);
						int k3 = 8 * (flag1 ? -1 : 1);
						Gui.drawScaledCustomSizeModalRect(j2, k2, 40.0F, (float) j3, 8, k3, 8, 8, 64.0F, 64.0F);
					}

					j2 += 9;
				}


				if (networkplayerinfo1.getGameType() == WorldSettings.GameType.SPECTATOR) {
					s1 = EnumChatFormatting.ITALIC + s1;
					this.mc.fontRendererObj.drawStringWithShadow(s1, (float) j2, (float) k2, -1862270977);
				} else {
					this.mc.fontRendererObj.drawStringWithShadow(s1, (float) j2, (float) k2, -1);
				}

				if (scoreObjectiveIn != null && networkplayerinfo1.getGameType() != WorldSettings.GameType.SPECTATOR) {
					int k5 = j2 + i + 1;
					int l5 = k5 + l;
					if (l5 - k5 > 5) {
						this.drawScoreboardValues(scoreObjectiveIn, k2, gameprofile.getName(), k5, l5,
								networkplayerinfo1);
					}
				}

				this.drawPing(i1, j2 - (flag ? 9 : 0), k2, networkplayerinfo1);
			}
		}

		if (list2 != null) {
			k1 = k1 + i4 * 9 + 1;
			drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1,
					k1 + list2.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

			for (int m = 0, n = list2.size(); m < n; ++m) {
				String s4 = list2.get(m);
				int j5 = this.mc.fontRendererObj.getStringWidth(s4);
				this.mc.fontRendererObj.drawStringWithShadow(s4, (float) (width / 2 - j5 / 2), (float) k1, -1);
				k1 += this.mc.fontRendererObj.FONT_HEIGHT;
			}
		}
	}

	public void renderPreviewList(int width, int height, List<PreviewEntry> entries) {
		this.removablePreviewRows.clear();

		int nameColumnWidth = 0;
		for (int i = 0, n = entries.size(); i < n; ++i) {
			nameColumnWidth = Math.max(nameColumnWidth,
					this.mc.fontRendererObj.getStringWidth(entries.get(i).displayName));
		}

		int rowCount = entries.size();
		int columns = 1;
		int rowsPerColumn = rowCount;
		while (rowsPerColumn > maxrows_per_column) {
			++columns;
			rowsPerColumn = (rowCount + columns - 1) / columns;
		}

		int columnWidth = Math.min(columns * (9 + nameColumnWidth + 13), width - 50) / columns;
		int boxWidth = columnWidth * columns + (columns - 1) * 5;
		int contentWidth = boxWidth;

		List<String> headerLines = null;
		List<String> footerLines = null;
		if (this.header != null) {
			headerLines = this.mc.fontRendererObj.listFormattedStringToWidth(this.header.getFormattedText(), width - 50);
			for (int m = 0, n = headerLines.size(); m < n; ++m) {
				contentWidth = Math.max(contentWidth, this.mc.fontRendererObj.getStringWidth(headerLines.get(m)));
			}
		}
		if (this.footer != null) {
			footerLines = this.mc.fontRendererObj.listFormattedStringToWidth(this.footer.getFormattedText(), width - 50);
			for (int m = 0, n = footerLines.size(); m < n; ++m) {
				contentWidth = Math.max(contentWidth, this.mc.fontRendererObj.getStringWidth(footerLines.get(m)));
			}
		}

		int headerHeight = headerLines != null ? headerLines.size() * this.mc.fontRendererObj.FONT_HEIGHT + 1 : 0;
		int footerHeight = footerLines != null ? footerLines.size() * this.mc.fontRendererObj.FONT_HEIGHT + 1 : 0;
		int listHeight = rowsPerColumn * 9;
		int totalHeight = headerHeight + listHeight + footerHeight;

		int y = 30;

		if (headerLines != null) {
			drawRect(width / 2 - contentWidth / 2 - 1, y - 1, width / 2 + contentWidth / 2 + 1,
					y + headerLines.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

			for (int m = 0, n = headerLines.size(); m < n; ++m) {
				String s = headerLines.get(m);
				int w = this.mc.fontRendererObj.getStringWidth(s);
				this.mc.fontRendererObj.drawStringWithShadow(s, (float) (width / 2 - w / 2), (float) y, -1);
				y += this.mc.fontRendererObj.FONT_HEIGHT;
			}

			++y;
		}

		int listTop = y;
		int listLeft = width / 2 - boxWidth / 2;
		drawRect(width / 2 - contentWidth / 2 - 1, listTop - 1, width / 2 + contentWidth / 2 + 1,
				listTop + rowsPerColumn * 9, Integer.MIN_VALUE);

		for (int idx = 0, n = entries.size(); idx < n; ++idx) {
			PreviewEntry entry = entries.get(idx);
			int col = idx / rowsPerColumn;
			int row = idx % rowsPerColumn;
			int rowX = listLeft + col * columnWidth + col * 5;
			int rowY = listTop + row * 9;

			drawRect(rowX, rowY, rowX + columnWidth, rowY + 8, 553648127);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);

			int textX = rowX;
			if (entry.skinLocation != null) {
				this.mc.getTextureManager().bindTexture(entry.skinLocation);
				Gui.drawScaledCustomSizeModalRect(rowX, rowY, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
				Gui.drawScaledCustomSizeModalRect(rowX, rowY, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
				textX += 9;
			}

			this.mc.fontRendererObj.drawStringWithShadow(entry.displayName, (float) textX, (float) rowY, -1);

			if (entry.pingMs != null) {
				this.drawPingBars(rowX + columnWidth, rowY, entry.pingMs.intValue());
			}

			if (entry.removable) {
				this.removablePreviewRows.add(new RemovableRow(rowX, rowY, rowX + columnWidth, rowY + 8, entry.userData));
			}
		}

		y = listTop + rowsPerColumn * 9;

		if (footerLines != null) {
			++y;
			drawRect(width / 2 - contentWidth / 2 - 1, y - 1, width / 2 + contentWidth / 2 + 1,
					y + footerLines.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

			for (int m = 0, n = footerLines.size(); m < n; ++m) {
				String s = footerLines.get(m);
				int w = this.mc.fontRendererObj.getStringWidth(s);
				this.mc.fontRendererObj.drawStringWithShadow(s, (float) (width / 2 - w / 2), (float) y, -1);
				y += this.mc.fontRendererObj.FONT_HEIGHT;
			}
		}
	}

	public Object getRemovablePreviewRowAt(int mouseX, int mouseY) {
		for (int i = 0, n = this.removablePreviewRows.size(); i < n; ++i) {
			RemovableRow row = this.removablePreviewRows.get(i);
			if (mouseX >= row.x0 && mouseX < row.x1 && mouseY >= row.y0 && mouseY < row.y1) {
				return row.tag;
			}
		}
		return null;
	}

	protected void drawPing(int networkPlayerInfoIn, int parInt2, int parInt3, NetworkPlayerInfo parNetworkPlayerInfo) {
		this.drawPingBars(parInt2 + networkPlayerInfoIn, parInt3, parNetworkPlayerInfo.getResponseTime());
	}

	private void drawPingBars(int x, int y, int pingMs) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(icons);
		byte b1;
		if (pingMs < 0) {
			b1 = 5;
		} else if (pingMs < 150) {
			b1 = 0;
		} else if (pingMs < 300) {
			b1 = 1;
		} else if (pingMs < 600) {
			b1 = 2;
		} else if (pingMs < 1000) {
			b1 = 3;
		} else {
			b1 = 4;
		}

		this.zLevel += 100.0F;
		this.drawTexturedModalRect(x - 11, y, 0, 176 + b1 * 8, 10, 8);
		this.zLevel -= 100.0F;
	}

	private void drawScoreboardValues(ScoreObjective parScoreObjective, int parInt1, String parString1, int parInt2,
			int parInt3, NetworkPlayerInfo parNetworkPlayerInfo) {
		int i = parScoreObjective.getScoreboard().getValueFromObjective(parString1, parScoreObjective).getScorePoints();
		if (parScoreObjective.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
			this.mc.getTextureManager().bindTexture(icons);
			if (this.lastTimeOpened == parNetworkPlayerInfo.func_178855_p()) {
				if (i < parNetworkPlayerInfo.func_178835_l()) {
					parNetworkPlayerInfo.func_178846_a(Minecraft.getSystemTime());
					parNetworkPlayerInfo.func_178844_b((long) (this.guiIngame.getUpdateCounter() + 20));
				} else if (i > parNetworkPlayerInfo.func_178835_l()) {
					parNetworkPlayerInfo.func_178846_a(Minecraft.getSystemTime());
					parNetworkPlayerInfo.func_178844_b((long) (this.guiIngame.getUpdateCounter() + 10));
				}
			}

			if (Minecraft.getSystemTime() - parNetworkPlayerInfo.func_178847_n() > 1000L
					|| this.lastTimeOpened != parNetworkPlayerInfo.func_178855_p()) {
				parNetworkPlayerInfo.func_178836_b(i);
				parNetworkPlayerInfo.func_178857_c(i);
				parNetworkPlayerInfo.func_178846_a(Minecraft.getSystemTime());
			}

			parNetworkPlayerInfo.func_178843_c(this.lastTimeOpened);
			parNetworkPlayerInfo.func_178836_b(i);
			int j = MathHelper.ceiling_float_int((float) Math.max(i, parNetworkPlayerInfo.func_178860_m()) / 2.0F);
			int k = Math.max(MathHelper.ceiling_float_int((float) (i / 2)),
					Math.max(MathHelper.ceiling_float_int((float) (parNetworkPlayerInfo.func_178860_m() / 2)), 10));
			boolean flag = parNetworkPlayerInfo.func_178858_o() > (long) this.guiIngame.getUpdateCounter()
					&& (parNetworkPlayerInfo.func_178858_o() - (long) this.guiIngame.getUpdateCounter()) / 3L
							% 2L == 1L;
			if (j > 0) {
				float f = Math.min((float) (parInt3 - parInt2 - 4) / (float) k, 9.0F);
				if (f > 3.0F) {
					for (int l = j; l < k; ++l) {
						this.drawTexturedModalRect((float) parInt2 + (float) l * f, (float) parInt1, flag ? 25 : 16, 0,
								9, 9);
					}

					for (int j1 = 0; j1 < j; ++j1) {
						this.drawTexturedModalRect((float) parInt2 + (float) j1 * f, (float) parInt1, flag ? 25 : 16, 0,
								9, 9);
						if (flag) {
							if (j1 * 2 + 1 < parNetworkPlayerInfo.func_178860_m()) {
								this.drawTexturedModalRect((float) parInt2 + (float) j1 * f, (float) parInt1, 70, 0, 9,
										9);
							}

							if (j1 * 2 + 1 == parNetworkPlayerInfo.func_178860_m()) {
								this.drawTexturedModalRect((float) parInt2 + (float) j1 * f, (float) parInt1, 79, 0, 9,
										9);
							}
						}

						if (j1 * 2 + 1 < i) {
							this.drawTexturedModalRect((float) parInt2 + (float) j1 * f, (float) parInt1,
									j1 >= 10 ? 160 : 52, 0, 9, 9);
						}

						if (j1 * 2 + 1 == i) {
							this.drawTexturedModalRect((float) parInt2 + (float) j1 * f, (float) parInt1,
									j1 >= 10 ? 169 : 61, 0, 9, 9);
						}
					}
				} else {
					float f1 = MathHelper.clamp_float((float) i / 20.0F, 0.0F, 1.0F);
					int i1 = (int) ((1.0F - f1) * 255.0F) << 16 | (int) (f1 * 255.0F) << 8;
					String s = "" + (float) i / 2.0F;
					if (parInt3 - this.mc.fontRendererObj.getStringWidth(s + "hp") >= parInt2) {
						s = s + "hp";
					}

					this.mc.fontRendererObj.drawStringWithShadow(s,
							(float) ((parInt3 + parInt2) / 2 - this.mc.fontRendererObj.getStringWidth(s) / 2),
							(float) parInt1, i1);
				}
			}
		} else {
			String s1 = EnumChatFormatting.YELLOW + "" + i;
			this.mc.fontRendererObj.drawStringWithShadow(s1,
					(float) (parInt3 - this.mc.fontRendererObj.getStringWidth(s1)), (float) parInt1, 16777215);
		}

	}

	public void setFooter(IChatComponent footerIn) {
		this.footer = footerIn;
	}

	public void setHeader(IChatComponent headerIn) {
		this.header = headerIn;
	}

	public void func_181030_a() {
		this.header = null;
		this.footer = null;
	}

	static class PlayerComparator implements Comparator<NetworkPlayerInfo> {
		private PlayerComparator() {
		}

		public int compare(NetworkPlayerInfo networkplayerinfo, NetworkPlayerInfo networkplayerinfo1) {
			ScorePlayerTeam scoreplayerteam = networkplayerinfo.getPlayerTeam();
			ScorePlayerTeam scoreplayerteam1 = networkplayerinfo1.getPlayerTeam();
			return ComparisonChain.start()
					.compareTrueFirst(networkplayerinfo.getGameType() != WorldSettings.GameType.SPECTATOR,
							networkplayerinfo1.getGameType() != WorldSettings.GameType.SPECTATOR)
					.compare(scoreplayerteam != null ? scoreplayerteam.getRegisteredName() : "",
							scoreplayerteam1 != null ? scoreplayerteam1.getRegisteredName() : "")
					.compare(networkplayerinfo.getGameProfile().getName(),
							networkplayerinfo1.getGameProfile().getName())
					.result();
		}
	}
}