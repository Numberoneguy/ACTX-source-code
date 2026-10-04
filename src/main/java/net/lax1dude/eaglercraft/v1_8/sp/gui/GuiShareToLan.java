/*
 * Copyright (c) 2022-2024 lax1dude, ayunami2000. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.sp.gui;

import net.lax1dude.eaglercraft.v1_8.internal.PlatformWebRTC;
import net.lax1dude.eaglercraft.v1_8.minecraft.EnumInputEvent;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.lax1dude.eaglercraft.v1_8.sp.lan.LANServerController;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonToggleWrap;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldSettings;

public class GuiShareToLan extends GuiScreen {
	/**
	 * A reference to the screen object that created this. Used for navigating
	 * between screens.
	 */
	/** Set by GuiMiscLan so the name survives the initGui re-call on screen switch. */
	static String pendingLanName = null;

	private final GuiScreen parentScreen;
	private GuiButton buttonAllowCommandsToggle;
	private GuiButton buttonGameMode;
	private GuiButton buttonHiddenToggle;

	/**
	 * The currently selected game mode. One of 'survival', 'creative', or
	 * 'adventure'
	 */
	private String gameMode;

	/**
	 * True if 'Allow Cheats' is currently enabled
	 */
	private boolean allowCommands = false;

	private final GuiNetworkSettingsButton relaysButton;

	private boolean hiddenToggle = false;

	private GuiTextField codeTextField;

	/** Toggle button for "require players to accept the resource pack before joining". */
	private GuiButtonToggleWrap buttonRequirePack;

	/** True if a custom resource pack is currently set for this world - drives the toggle's grey-out state. */
	private boolean customPackAvailable = true;

	/** Off by default, per spec. */
	private boolean requirePackAccept = false;

	/** Shown on double-click of buttonRequirePack; holds the resource pack URL. */
	private GuiTextField packUrlField;
	private boolean packUrlFieldVisible = false;
	private static final int COLOR_URL_VALID = 14737632;
	private static final int COLOR_URL_INVALID = 0xFF5555;

	public GuiShareToLan(GuiScreen par1GuiScreen, String gameMode) {
		this.parentScreen = par1GuiScreen;
		this.relaysButton = new GuiNetworkSettingsButton(this);
		this.gameMode = gameMode;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	public void initGui() {
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(101, this.width / 2 - 155, this.height - 28, 140, 20,
				I18n.format("lanServer.start")));
		this.buttonList.add(new GuiButton(102, this.width / 2 + 5, this.height - 28, 140, 20,
				I18n.format("gui.cancel")));
		this.buttonList.add(this.buttonGameMode = new GuiButton(104, this.width / 2 - 155, 135, 140, 20,
				I18n.format("selectWorld.gameMode")));
		this.buttonList.add(this.buttonAllowCommandsToggle = new GuiButton(103, this.width / 2 + 5, 135, 140, 20,
				I18n.format("selectWorld.allowCommands")));
		this.buttonGameMode.enabled = this.buttonAllowCommandsToggle.enabled = !mc.isDemo();
		this.buttonList.add(new GuiButton(106, 4, this.height - 28, 80, 20, "Miscellaneous"));
		this.buttonList.add(this.buttonHiddenToggle = new GuiButton(105, this.width / 2 - 155, 165, 140, 20,
				I18n.format("lanServer.hidden")));

		// True when the host currently has at least one resource pack enabled under
		// Options > Resource Packs (mirrors what GuiScreenResourcePacks treats as "selected").
		this.customPackAvailable = !this.mc.getResourcePackRepository().getRepositoryEntries().isEmpty();
		this.buttonList.add(this.buttonRequirePack = new GuiButtonToggleWrap(107, this.width / 2 + 5, 165, 140, 20,
				"actx.lan.requirePack"));
		this.buttonRequirePack.enabled = this.customPackAvailable;

		this.packUrlField = new GuiTextField(1, this.fontRendererObj, this.width / 2 - 100, 189, 200, 16);
		this.packUrlField.setMaxStringLength(256);
		this.packUrlField.setTextColor(COLOR_URL_VALID);

		this.codeTextField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100, 80, 200, 20);
		String defaultWorldName = (pendingLanName != null) ? pendingLanName : mc.thePlayer.getName() + "'s World";
		pendingLanName = null; // consume so it only fires once
		this.codeTextField.setText(defaultWorldName);
		this.codeTextField.setFocused(true);
		this.codeTextField.setMaxStringLength(32767);
		this.func_74088_g();
	}

	private void func_74088_g() {
		this.buttonGameMode.displayString = I18n.format("selectWorld.gameMode") + ": "
				+ I18n.format("selectWorld.gameMode." + this.gameMode);
		this.buttonAllowCommandsToggle.displayString = I18n.format("selectWorld.allowCommands")
				+ " ";
		this.buttonHiddenToggle.displayString = I18n.format("lanServer.hidden")
				+ " ";

		if (this.allowCommands) {
			this.buttonAllowCommandsToggle.displayString = this.buttonAllowCommandsToggle.displayString
					+ I18n.format("options.on");
		} else {
			this.buttonAllowCommandsToggle.displayString = this.buttonAllowCommandsToggle.displayString
					+ I18n.format("options.off");
		}

		if (this.hiddenToggle) {
			this.buttonHiddenToggle.displayString = this.buttonHiddenToggle.displayString
					+ I18n.format("options.on");
		} else {
			this.buttonHiddenToggle.displayString = this.buttonHiddenToggle.displayString
					+ I18n.format("options.off");
		}

		this.buttonRequirePack.displayString = I18n.format("actx.lan.requirePack") + " "
				+ I18n.format(this.requirePackAccept ? "options.on" : "options.off");
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.id == 102) {
			this.mc.displayGuiScreen(this.parentScreen);
		} else if (par1GuiButton.id == 104) {
			if(!mc.isDemo()) {
				if (this.gameMode.equals("survival")) {
					this.gameMode = "creative";
				} else if (this.gameMode.equals("creative")) {
					this.gameMode = "adventure";
				} else if (this.gameMode.equals("adventure")) {
					this.gameMode = "spectator";
				} else {
					this.gameMode = "survival";
				}
	
				this.func_74088_g();
			}
		} else if (par1GuiButton.id == 103) {
			if(!mc.isDemo()) {
				this.allowCommands = !this.allowCommands;
				this.func_74088_g();
			}
		} else if (par1GuiButton.id == 105) {
			this.hiddenToggle = !this.hiddenToggle;
			this.func_74088_g();
		} else if (par1GuiButton.id == 107) {
			if (this.buttonRequirePack.isDoubleClick()) {
				this.packUrlFieldVisible = !this.packUrlFieldVisible;
				this.packUrlField.setFocused(this.packUrlFieldVisible);
			} else {
				this.requirePackAccept = !this.requirePackAccept;
			}
			this.func_74088_g();
		} else if (par1GuiButton.id == 106) {
			this.mc.displayGuiScreen(new GuiMiscLan(this, this.codeTextField.getText()));
		} else if (par1GuiButton.id == 101) {
			if (LANServerController.isLANOpen()) {
				return;
			}
			PlatformWebRTC.startRTCLANServer();
			String worldName = this.codeTextField.getText().trim();
			if (worldName.isEmpty()) {
				worldName = mc.thePlayer.getName() + "'s World";
			}
			if (worldName.length() >= 252) {
				worldName = worldName.substring(0, 252);
			}
			String packUrl = this.packUrlField.getText().trim();
			String packPath = packUrl;
			int cut = packPath.indexOf('?');
			if (cut >= 0) packPath = packPath.substring(0, cut);
			cut = packPath.indexOf('#');
			if (cut >= 0) packPath = packPath.substring(0, cut);
			boolean packUrlValid = !packUrl.isEmpty() && packPath.toLowerCase().endsWith(".zip");
			if (!packUrl.isEmpty() && !packUrlValid) {
					this.mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(
							"§cResource pack URL ignored: it must end in .zip (a ?query is fine)."));
			}
			boolean requirePack = packUrlValid && this.requirePackAccept;
			this.mc.displayGuiScreen(null);
			LoadingScreenRenderer ls = mc.loadingScreen;
			String code = LANServerController.shareToLAN((msg) -> ls.eaglerShow(msg, null), worldName, hiddenToggle);
			if (code != null) {
				SingleplayerServerController.configureLAN(WorldSettings.GameType.getByName(this.gameMode), this.allowCommands,
						packUrlValid ? packUrl : "", "", requirePack);
				this.mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(I18n.format("lanServer.opened")
						.replace("$relay$", LANServerController.getCurrentURI()).replace("$code$", code)));
			} else {
				this.mc.displayGuiScreen(new GuiScreenNoRelays(this, "noRelay.titleFail"));
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, I18n.format("lanServer.title"), this.width / 2,
				35, 16777215);
		this.drawCenteredString(this.fontRendererObj, I18n.format("lanServer.worldName"), this.width / 2,
				62, 16777215);
		this.drawCenteredString(this.fontRendererObj, I18n.format("lanServer.otherPlayers"),
				this.width / 2, 112, 16777215);
		this.drawCenteredString(this.fontRendererObj, I18n.format("lanServer.ipGrabNote"),
				this.width / 2, 212, 16777215);
		super.drawScreen(par1, par2, par3);
		this.relaysButton.drawScreen(par1, par2);
		this.codeTextField.drawTextBox();
		if (this.packUrlFieldVisible) {
			this.packUrlField.drawTextBox();
		}
	}

	public void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		this.relaysButton.mouseClicked(par1, par2, par3);
		this.codeTextField.mouseClicked(par1, par2, par3);
		if (this.packUrlFieldVisible) {
			this.packUrlField.mouseClicked(par1, par2, par3);
		}
	}

	protected void keyTyped(char c, int k) {
		super.keyTyped(c, k);
		this.codeTextField.textboxKeyTyped(c, k);
		if (this.packUrlFieldVisible && this.packUrlField.isFocused()) {
			this.packUrlField.textboxKeyTyped(c, k);
			String url = this.packUrlField.getText();
			boolean valid = url.toLowerCase().endsWith(".zip");
			this.packUrlField.setTextColor(valid ? COLOR_URL_VALID : COLOR_URL_INVALID);
		}
	}

	public void updateScreen() {
		super.updateScreen();
		this.codeTextField.updateCursorCounter();
		this.packUrlField.updateCursorCounter();
	}

	public boolean blockPTTKey() {
		return this.codeTextField.isFocused() || (this.packUrlFieldVisible && this.packUrlField.isFocused());
	}

	@Override
	public boolean showCopyPasteButtons() {
		return this.codeTextField.isFocused() || (this.packUrlFieldVisible && this.packUrlField.isFocused());
	}

	@Override
	public void fireInputEvent(EnumInputEvent event, String param) {
		this.codeTextField.fireInputEvent(event, param);
		if (this.packUrlFieldVisible && this.packUrlField.isFocused()) {
			this.packUrlField.fireInputEvent(event, param);
		}
	}

}