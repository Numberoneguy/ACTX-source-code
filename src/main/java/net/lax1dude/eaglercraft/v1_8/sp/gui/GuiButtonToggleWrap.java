package net.minecraft.client.gui;

import java.util.List;

import com.google.common.collect.Lists;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**+
 * Toggle-style GuiButton whose label comes from a lang key and wraps to
 * multiple lines instead of overflowing a single-line button. Also tracks
 * single- vs double-click so a screen can use one click to flip a boolean
 * and a double-click to open something else (e.g. a text field).
 *
 * Greying out is just the normal GuiButton#enabled flag - set it from the
 * screen based on whatever "is this a custom resource pack" check applies
 * (e.g. mcServer.getResourcePackUrl().length() > 0 on the server side).
 *
 * Usage in a GuiScreen subclass:
 *
 *   this.buttonList.add(new GuiButtonToggleWrap(ID, x, y, w, h, "actx.lan.requirePack"));
 *   ...
 *   protected void actionPerformed(GuiButton button) {
 *       if (button.id == ID && button.enabled) {
 *           GuiButtonToggleWrap b = (GuiButtonToggleWrap) button;
 *           if (b.isDoubleClick()) {
 *               openUrlField(); // your text field logic
 *           } else {
 *               requirePack = !requirePack;
 *           }
 *       }
 *   }
 */
public class GuiButtonToggleWrap extends GuiButton {

	private static final long DOUBLE_CLICK_MS = 300L;

	private final String langKey;
	private final List<String> wrappedLines = Lists.newArrayList();
	private long lastClickTime = -1L;

	public GuiButtonToggleWrap(int buttonId, int x, int y, int widthIn, int heightIn, String langKey) {
		super(buttonId, x, y, widthIn, heightIn, I18n.format(langKey, new Object[0]));
		this.langKey = langKey;
	}

	/**+
	 * Re-resolves the label from the lang key. Call if the language can
	 * change while this screen is open.
	 */
	public void refreshText() {
		this.displayString = I18n.format(this.langKey, new Object[0]);
	}

	/**+
	 * Call once, from actionPerformed, the moment this button is clicked.
	 * Returns true if this click is the second half of a double-click
	 * (and resets the tracker), false if it's a fresh single click.
	 */
	public boolean isDoubleClick() {
		long now = System.currentTimeMillis();
		boolean isDouble = this.lastClickTime >= 0L && now - this.lastClickTime <= DOUBLE_CLICK_MS;
		this.lastClickTime = isDouble ? -1L : now;
		return isDouble;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (!this.visible) {
			return;
		}

		FontRenderer fontrenderer = mc.fontRendererObj;
		mc.getTextureManager().bindTexture(buttonTextures);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition
				&& mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
		int hoverState = this.getHoverState(this.hovered);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.blendFunc(770, 771);
		this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + hoverState * 20, this.width / 2, this.height);
		this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2,
				46 + hoverState * 20, this.width / 2, this.height);
		this.mouseDragged(mc, mouseX, mouseY);

		int color = 14737632;
		if (!this.enabled) {
			color = 10526880;
		} else if (this.hovered) {
			color = 16777120;
		}

		this.wrappedLines.clear();
		this.wrappedLines.addAll(fontrenderer.listFormattedStringToWidth(this.displayString, this.width - 6));
		int totalHeight = this.wrappedLines.size() * fontrenderer.FONT_HEIGHT;
		int startY = this.yPosition + (this.height - totalHeight) / 2;
		for (int i = 0; i < this.wrappedLines.size(); ++i) {
			this.drawCenteredString(fontrenderer, this.wrappedLines.get(i), this.xPosition + this.width / 2,
					startY + i * fontrenderer.FONT_HEIGHT, color);
		}
	}
} 