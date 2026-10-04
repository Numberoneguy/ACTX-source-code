package net.minecraft.client.gui.inventory;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerDispenser;
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
public class GuiDispenser extends GuiContainer {
	private static final ResourceLocation dispenserGuiTextures = new ResourceLocation(
			"textures/gui/container/dispenser.png");
	private final InventoryPlayer playerInventory;
	public IInventory dispenserInventory;

	public GuiDispenser(InventoryPlayer playerInv, IInventory dispenserInv) {
		super(new ContainerDispenser(playerInv, dispenserInv));
		this.playerInventory = playerInv;
		this.dispenserInventory = dispenserInv;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		char c = Character.toLowerCase(typedChar);
		if (c == 'g') {
			storeAllIntoDispenser(); // G now stores items
			return;
		}
		if (c == 'm') {
			grabAllFromDispenser();  // M now grabs items
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}


	private void grabAllFromDispenser() {
		if (this.mc.thePlayer == null) return;

		// Replaced with dispenserInventory (Dispenser has exactly 9 slots)
		int dispenserSize = this.dispenserInventory.getSizeInventory();
		List<Slot> slots = this.inventorySlots.inventorySlots;

		for (int i = 0; i < dispenserSize && i < slots.size(); ++i) {
			Slot slot = slots.get(i);
			if (slot.getHasStack()) {
				this.handleMouseClick(slot, slot.slotNumber, 0, 1);
			}
		}
	}

	private void storeAllIntoDispenser() {
		if (this.mc.thePlayer == null) return;

		// Starts cycling immediately after the 9 dispenser slots
		int dispenserSize = this.dispenserInventory.getSizeInventory();
		List<Slot> slots = this.inventorySlots.inventorySlots;

		for (int i = dispenserSize; i < slots.size(); ++i) {
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
		String s = this.dispenserInventory.getDisplayName().getUnformattedText();
		this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);
		this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8,
				this.ySize - 96 + 2, 4210752);
	}

	/**+
	 * Args : renderPartialTicks, mouseX, mouseY
	 */
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(dispenserGuiTextures);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
	}
}
