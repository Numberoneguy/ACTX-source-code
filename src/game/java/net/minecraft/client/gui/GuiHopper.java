package net.minecraft.client.gui;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import java.util.List;

/**+
 * This portion of EaglercraftX contains deobfuscated Minecraft 1.8 source code.
 * 
 * Minecraft 1.8.8 bytecode is (c) 2015 Mojang AB. "Do not distribute!"
 * Mod Coder Pack v9.18 deobfuscation configs are (c) Copyright by the MCP Team
 * 
 * EaglercraftX 1.8 patch files (c) 2022-2025 lax1dude, ayunami2000. All Rights Reserved.
 */
public class GuiHopper extends GuiContainer {
	/**+
	 * The ResourceLocation containing the gui texture for the
	 * hopper
	 */
	private static final ResourceLocation HOPPER_GUI_TEXTURE = new ResourceLocation(
			"textures/gui/container/hopper.png");
	private IInventory playerInventory;
	private IInventory hopperInventory;

	public GuiHopper(InventoryPlayer playerInv, IInventory hopperInv) {
		super(new ContainerHopper(playerInv, hopperInv, Minecraft.getMinecraft().thePlayer));
		this.playerInventory = playerInv;
		this.hopperInventory = hopperInv;
		this.allowUserInput = false;
		this.ySize = 133;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		char c = Character.toLowerCase(typedChar);
		if (c == 'g') {
			storeAllIntoHopper(); // G now stores items
			return;
		}
		if (c == 'm') {
			grabAllFromHopper();  // M now grabs items
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}


	private void grabAllFromHopper() {
		if (this.mc.thePlayer == null) return;

		// Replaced upperChestInventory with hopperInventory (Hopper has exactly 5 slots)
		int hopperSize = this.hopperInventory.getSizeInventory();
		List<Slot> slots = this.inventorySlots.inventorySlots;

		for (int i = 0; i < hopperSize && i < slots.size(); ++i) {
			Slot slot = slots.get(i);
			if (slot.getHasStack()) {
				this.handleMouseClick(slot, slot.slotNumber, 0, 1);
			}
		}
	}

	private void storeAllIntoHopper() {
		if (this.mc.thePlayer == null) return;

		// Starts cycling immediately after the 5 hopper slots
		int hopperSize = this.hopperInventory.getSizeInventory();
		List<Slot> slots = this.inventorySlots.inventorySlots;

		for (int i = hopperSize; i < slots.size(); ++i) {
			Slot slot = slots.get(i);
			if (slot.getHasStack()) {
				this.handleMouseClick(slot, slot.slotNumber, 0, 1);
			}
		}
	}

	/**+
	 * Draw the foreground layer for the GuiContainer (everything in
	 * front of the items). Args : mouseX, mouseY
	 */
	protected void drawGuiContainerForegroundLayer(int var1, int var2) {
		this.fontRendererObj.drawString(this.hopperInventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
		this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8,
				this.ySize - 96 + 2, 4210752);
	}

	/**+
	 * Args : renderPartialTicks, mouseX, mouseY
	 */
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(HOPPER_GUI_TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
	}
}
