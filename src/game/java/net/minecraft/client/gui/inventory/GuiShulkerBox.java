package net.minecraft.client.gui.inventory;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerShulkerBox;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class GuiShulkerBox extends GuiContainer {
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(
			"textures/gui/container/shulker_box.png");
	private final IInventory shulkerInventory;
	private final InventoryPlayer playerInventory;

	public GuiShulkerBox(InventoryPlayer playerInventory, IInventory shulkerInventory) {
		super(new ContainerShulkerBox(playerInventory, shulkerInventory, Minecraft.getMinecraft().thePlayer));
		this.playerInventory = playerInventory;
		this.shulkerInventory = shulkerInventory;
		this.allowUserInput = false;
		++this.ySize;
	}

	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRendererObj.drawString(this.shulkerInventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
		this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8,
				this.ySize - 96 + 2, 4210752);
	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
	}
}
