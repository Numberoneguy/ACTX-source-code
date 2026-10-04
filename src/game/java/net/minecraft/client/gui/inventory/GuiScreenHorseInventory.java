package net.minecraft.client.gui.inventory;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.inventory.ContainerHorseInventory;
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
public class GuiScreenHorseInventory extends GuiContainer {
	private static final ResourceLocation horseGuiTextures = new ResourceLocation("textures/gui/container/horse.png");
	private IInventory playerInventory;
	private IInventory horseInventory;
	private EntityHorse horseEntity;
	private float mousePosx;
	private float mousePosY;

	public GuiScreenHorseInventory(IInventory playerInv, IInventory horseInv, EntityHorse horse) {
		super(new ContainerHorseInventory(playerInv, horseInv, horse, Minecraft.getMinecraft().thePlayer));
		this.playerInventory = playerInv;
		this.horseInventory = horseInv;
		this.horseEntity = horse;
		this.allowUserInput = false;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		char c = Character.toLowerCase(typedChar);
		if (c == 'g') {
			storeAllIntoHorse(); // G now stores items
			return;
		}
		if (c == 'm') {
			grabAllFromHorse();  // M now grabs items
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}


	private void grabAllFromHorse() {
		if (this.mc.thePlayer == null) return;

		int horseInvSize = this.horseInventory.getSizeInventory();
		List<Slot> slots = this.inventorySlots.inventorySlots;

		// We start loop at index 2 to explicitly skip the Saddle and Armor slots,
		// vacuuming only the actual items inside the donkey/mule chest bag.
		for (int i = 2; i < horseInvSize && i < slots.size(); ++i) {
			Slot slot = slots.get(i);
			if (slot.getHasStack()) {
				this.handleMouseClick(slot, slot.slotNumber, 0, 1);
			}
		}
	}

	private void storeAllIntoHorse() {
		if (this.mc.thePlayer == null) return;

		int horseInvSize = this.horseInventory.getSizeInventory();
		List<Slot> slots = this.inventorySlots.inventorySlots;

		// Cycles your player inventory slots, completely skipping the horse's equipment metrics
		for (int i = horseInvSize; i < slots.size(); ++i) {
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
		this.fontRendererObj.drawString(this.horseInventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
		this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8,
				this.ySize - 96 + 2, 4210752);
	}

	/**+
	 * Args : renderPartialTicks, mouseX, mouseY
	 */
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(horseGuiTextures);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
		if (this.horseEntity.isChested()) {
			this.drawTexturedModalRect(i + 79, j + 17, 0, this.ySize, 90, 54);
		}

		if (this.horseEntity.canWearArmor()) {
			this.drawTexturedModalRect(i + 7, j + 35, 0, this.ySize + 54, 18, 18);
		}

		GuiInventory.drawEntityOnScreen(i + 51, j + 60, 17, (float) (i + 51) - this.mousePosx,
				(float) (j + 75 - 50) - this.mousePosY, this.horseEntity);
	}

	/**+
	 * Draws the screen and all the components in it. Args : mouseX,
	 * mouseY, renderPartialTicks
	 */
	public void drawScreen(int i, int j, float f) {
		this.mousePosx = (float) i;
		this.mousePosY = (float) j;
		super.drawScreen(i, j, f);
	}
}
