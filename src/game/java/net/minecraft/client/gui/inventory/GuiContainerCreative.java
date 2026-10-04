package net.minecraft.client.gui.inventory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.carrotsearch.hppc.IntIntMap;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemTool;

import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.Mouse;
import net.lax1dude.eaglercraft.v1_8.PointerInputAbstraction;
import net.lax1dude.eaglercraft.v1_8.internal.EnumCursorType;
import net.lax1dude.eaglercraft.v1_8.minecraft.EnumInputEvent;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.actx.saveditems;
import net.minecraft.actx.actxcreativetabs;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

/**+
 * This portion of EaglercraftX contains deobfuscated Minecraft 1.8 source code.
 *
 * Minecraft 1.8.8 bytecode is (c) 2015 Mojang AB. "Do not distribute!"
 * Mod Coder Pack v9.18 deobfuscation configs are (c) Copyright by the MCP Team
 *
 * EaglercraftX 1.8 patch files (c) 2022-2025 lax1dude, ayunami2000. All Rights Reserved.
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
public class GuiContainerCreative extends InventoryEffectRenderer {
    /**+
     * The location of the creative inventory tabs texture
     */
    private static final ResourceLocation creativeInventoryTabs = new ResourceLocation(
            "textures/gui/container/creative_inventory/tabs.png");
    private static InventoryBasic field_147060_v = new InventoryBasic("tmp", true, 45);
    /**+
     * Currently selected creative inventory tab index.
     */
    private static int selectedTabIndex = CreativeTabs.tabBlock.getTabIndex();
    private float currentScroll;
    private boolean isScrolling;
    private boolean wasClicking;
    private GuiTextField searchField;
    private List<Slot> field_147063_B;
    private Slot field_147064_C;
    private boolean field_147057_D;
    private CreativeCrafting field_147059_E;
    private boolean showingUnobtainable = false;
    private int currentCategory = 1;
    private GuiButton buttonLeft;
    private GuiButton buttonRight;
    private static final int PAGE2_VIEW_ITEMS  = 0;
    private static final int PAGE2_VIEW_SEARCH = 1;
    private static final int PAGE2_VIEW_SAVED  = 2;
    private static final int PAGE2_VIEW_CUSTOM_BLOCKS = 3;
    private int page2View = PAGE2_VIEW_ITEMS;
    private int lastPage1TabIndex = selectedTabIndex;

    public GuiContainerCreative(EntityPlayer parEntityPlayer) {
        super(new GuiContainerCreative.ContainerCreative(parEntityPlayer));
        parEntityPlayer.openContainer = this.inventorySlots;
        this.allowUserInput = true;
        this.ySize = 136;
        this.xSize = 195;
    }

    private boolean isInventoryTabActive() {
        return !this.showingUnobtainable && selectedTabIndex == CreativeTabs.tabInventory.getTabIndex();
    }

    /**+
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
        if (!this.mc.playerController.isInCreativeMode()) {
            this.mc.displayGuiScreen(new GuiInventory(this.mc.thePlayer));
        }
        this.updateActivePotionEffects();
    }

    /**+
     * Called when the mouse is clicked over a slot or outside the gui.
     */
    protected void handleMouseClick(Slot slot, int i, int j, int k) {
    this.field_147057_D = true;
    boolean flag = k == 1;
    k = i == -999 && k == 0 ? 4 : k;
    IInventory slotInv = (slot instanceof CreativeSlot) 
            ? ((CreativeSlot) slot).slot.inventory 
            : (slot != null ? slot.inventory : null);
    if (slot == null && !this.isInventoryTabActive() && k != 5) {
        InventoryPlayer inventoryplayer1 = this.mc.thePlayer.inventory;
        if (inventoryplayer1.getItemStack() != null) {
            if (j == 0) {
                this.mc.thePlayer.dropPlayerItemWithRandomChoice(inventoryplayer1.getItemStack(), true);
                this.mc.playerController.sendPacketDropItem(inventoryplayer1.getItemStack());
                inventoryplayer1.setItemStack((ItemStack) null);
            }
            if (j == 1) {
                ItemStack itemstack5 = inventoryplayer1.getItemStack().splitStack(1);
                this.mc.thePlayer.dropPlayerItemWithRandomChoice(itemstack5, true);
                this.mc.playerController.sendPacketDropItem(itemstack5);
                if (inventoryplayer1.getItemStack().stackSize == 0) {
                    inventoryplayer1.setItemStack((ItemStack) null);
                }
            }
        }
    } else if (slot == this.field_147064_C && flag) {
        for (int i1 = 0; i1 < this.mc.thePlayer.inventoryContainer.getInventory().size(); ++i1) {
            this.mc.playerController.sendSlotPacket((ItemStack) null, i1);
        }
    } else if (this.isInventoryTabActive()) {
        if (slot == this.field_147064_C) {
            this.mc.thePlayer.inventory.setItemStack((ItemStack) null);
        } else if (k == 4 && slot != null && slot.getHasStack()) {
            ItemStack itemstack = slot.decrStackSize(j == 0 ? 1 : slot.getStack().getMaxStackSize());
            this.mc.thePlayer.dropPlayerItemWithRandomChoice(itemstack, true);
            this.mc.playerController.sendPacketDropItem(itemstack);
        } else if (k == 4 && this.mc.thePlayer.inventory.getItemStack() != null) {
            this.mc.thePlayer.dropPlayerItemWithRandomChoice(this.mc.thePlayer.inventory.getItemStack(), true);
            this.mc.playerController.sendPacketDropItem(this.mc.thePlayer.inventory.getItemStack());
            this.mc.thePlayer.inventory.setItemStack((ItemStack) null);
        } else {
            this.mc.thePlayer.inventoryContainer.slotClick(
                    slot == null ? i : ((GuiContainerCreative.CreativeSlot) slot).slot.slotNumber, j, k,
                    this.mc.thePlayer);
            this.mc.thePlayer.inventoryContainer.detectAndSendChanges();
        }
    } else if (k != 5 && slotInv == field_147060_v) {
        InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
        // ... rest of method
            ItemStack itemstack1 = inventoryplayer.getItemStack();
            ItemStack itemstack2 = slot.getStack();
            if (k == 2) {
                if (itemstack2 != null && j >= 0 && j < 9) {
                    ItemStack itemstack7 = itemstack2.copy();
                    itemstack7.stackSize = itemstack7.getMaxStackSize();
                    this.mc.thePlayer.inventory.setInventorySlotContents(j, itemstack7);
                    this.mc.thePlayer.inventoryContainer.detectAndSendChanges();
                }
                return;
            }
            if (k == 3) {
                if (inventoryplayer.getItemStack() == null && slot.getHasStack()) {
                    ItemStack itemstack6 = slot.getStack().copy();
                    itemstack6.stackSize = itemstack6.getMaxStackSize();
                    inventoryplayer.setItemStack(itemstack6);
                }
                return;
            }
            if (k == 4) {
                if (itemstack2 != null) {
                    ItemStack itemstack3 = itemstack2.copy();
                    itemstack3.stackSize = j == 0 ? 1 : itemstack3.getMaxStackSize();
                    this.mc.thePlayer.dropPlayerItemWithRandomChoice(itemstack3, true);
                    this.mc.playerController.sendPacketDropItem(itemstack3);
                }
                return;
            }
            if (itemstack1 != null && itemstack2 != null && itemstack1.isItemEqual(itemstack2)) {
                if (j == 0) {
                    if (flag) {
                        itemstack1.stackSize = itemstack1.getMaxStackSize();
                    } else if (itemstack1.stackSize < itemstack1.getMaxStackSize()) {
                        ++itemstack1.stackSize;
                    }
                } else if (itemstack1.stackSize <= 1) {
                    inventoryplayer.setItemStack((ItemStack) null);
                } else {
                    --itemstack1.stackSize;
                }
            } else if (itemstack2 != null && itemstack1 == null) {
                inventoryplayer.setItemStack(ItemStack.copyItemStack(itemstack2));
                itemstack1 = inventoryplayer.getItemStack();
                if (flag) {
                    itemstack1.stackSize = itemstack1.getMaxStackSize();
                }
            } else {
                inventoryplayer.setItemStack((ItemStack) null);
            }
        } else {
            this.inventorySlots.slotClick(slot == null ? i : slot.slotNumber, j, k, this.mc.thePlayer);
            if (Container.getDragEvent(j) == 2) {
                for (int l = 0; l < 9; ++l) {
                    this.mc.playerController.sendSlotPacket(this.inventorySlots.getSlot(45 + l).getStack(), 36 + l);
                }
            } else if (slot != null) {
                ItemStack itemstack4 = this.inventorySlots.getSlot(slot.slotNumber).getStack();
                this.mc.playerController.sendSlotPacket(itemstack4,
                        slot.slotNumber - this.inventorySlots.inventorySlots.size() + 9 + 36);
            }
        }
    }

    protected void updateActivePotionEffects() {
        int i = this.guiLeft;
        super.updateActivePotionEffects();
        if (this.searchField != null && this.guiLeft != i) {
            this.searchField.xPosition = this.guiLeft + 82;
        }
    }

    /**+
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui() {
        if (this.mc.playerController.isInCreativeMode()) {
            super.initGui();
            this.buttonList.clear();
            Keyboard.enableRepeatEvents(true);
            this.searchField = new GuiTextField(0, this.fontRendererObj, this.guiLeft + 82, this.guiTop + 6, 89,
                    this.fontRendererObj.FONT_HEIGHT);
            this.searchField.setMaxStringLength(15);
            this.searchField.setEnableBackgroundDrawing(false);
            this.searchField.setVisible(false);
            this.searchField.setTextColor(16777215);

            this.buttonList.add(this.buttonLeft  = new GuiButton(101, this.guiLeft - 24, this.guiTop - 26, 20, 20, "<"));
            this.buttonList.add(this.buttonRight = new GuiButton(102, this.guiLeft + this.xSize + 4, this.guiTop - 26, 20, 20, ">"));
            this.buttonLeft.enabled  = false;
            this.buttonRight.enabled = true;

            int i = selectedTabIndex;
            selectedTabIndex = -1;
            this.setCurrentCreativeTab(CreativeTabs.creativeTabArray[i]);
            this.field_147059_E = new CreativeCrafting(this.mc);
            this.mc.thePlayer.inventoryContainer.onCraftGuiOpened(this.field_147059_E);
        } else {
            this.mc.displayGuiScreen(new GuiInventory(this.mc.thePlayer));
        }
    }

    /**+
     * Called when the screen is unloaded.
     */
    public void onGuiClosed() {
        super.onGuiClosed();
        if (this.mc.thePlayer != null && this.mc.thePlayer.inventory != null) {
            this.mc.thePlayer.inventoryContainer.removeCraftingFromCrafters(this.field_147059_E);
        }
        Keyboard.enableRepeatEvents(false);
    }

    /**+
     * Fired when a key is typed.
     */
    protected void keyTyped(char parChar1, int parInt1) {
        if (parChar1 == '<' || parChar1 == ',') {
            if (this.currentCategory > 1) {
                this.currentCategory--;
                this.showingUnobtainable = false;
                this.page2View = PAGE2_VIEW_ITEMS;
                this.showingSavedItems = false;
                this.setCurrentCreativeTab(CreativeTabs.creativeTabArray[this.lastPage1TabIndex]);
                if (this.buttonLeft  != null) this.buttonLeft.enabled  = (this.currentCategory > 1);
                if (this.buttonRight != null) this.buttonRight.enabled = (this.currentCategory < 2);
            }
            return;
        }
        if (parChar1 == '>' || parChar1 == '.') {
            if (this.currentCategory < 2) {
                this.lastPage1TabIndex = selectedTabIndex;
                this.currentCategory++;
                this.restoreInventorySlotsIfNeeded();
                this.showingUnobtainable = true;
                this.page2View = PAGE2_VIEW_ITEMS;
                this.showingSavedItems = false;
                this.dragSplittingSlots.clear();
                this.loadUnobtainableItems();
                if (this.searchField != null) {
                    this.searchField.setVisible(false);
                    this.searchField.setCanLoseFocus(true);
                    this.searchField.setFocused(false);
                }
                if (this.buttonLeft  != null) this.buttonLeft.enabled  = (this.currentCategory > 1);
                if (this.buttonRight != null) this.buttonRight.enabled = (this.currentCategory < 2);
            }
            return;
        }

        if (!this.isSearchActive()) {
            if (GameSettings.isKeyDown(this.mc.gameSettings.keyBindChat)) {
                this.activateSearchTab();
            } else {
                super.keyTyped(parChar1, parInt1);
            }
        } else {
            if (this.field_147057_D) {
                this.field_147057_D = false;
                this.searchField.setText("");
            }
            if (parInt1 == getCloseKey() || (parInt1 == 1 && Keyboard.areKeysLocked())) {
                mc.displayGuiScreen(null);
            } else if (!this.checkHotbarKeys(parInt1)) {
                if (this.searchField.textboxKeyTyped(parChar1, parInt1)) {
                    this.updateCreativeSearch();
                } else {
                    super.keyTyped(parChar1, parInt1);
                }
            }
        }
    }
    // adding water to the ocean
    private boolean isSearchActive() {
        if (this.showingUnobtainable) {
            return this.page2View == PAGE2_VIEW_SEARCH;
        }
        return selectedTabIndex == CreativeTabs.tabAllSearch.getTabIndex();
    }

    private void activateSearchTab() {
        this.setCurrentCreativeTab(CreativeTabs.tabAllSearch);
        if (this.showingUnobtainable) {
            this.page2View = PAGE2_VIEW_SEARCH;
            this.showingSavedItems = false;
        }
    }

    private void activateSaveTab() {
        this.openSavedItemsTab();
        this.page2View = PAGE2_VIEW_SAVED;
    }

    private void activateCustomBlocksTab() {
        this.page2View = PAGE2_VIEW_CUSTOM_BLOCKS;
        this.showingSavedItems = false;
        this.dragSplittingSlots.clear();
        this.loadCustomBlockItems();
        if (this.searchField != null) {
            this.searchField.setVisible(false);
            this.searchField.setCanLoseFocus(true);
            this.searchField.setFocused(false);
        }
    }

    private void activateItemsTab() {
        this.page2View = PAGE2_VIEW_ITEMS;
        this.showingSavedItems = false;
        this.dragSplittingSlots.clear();
        this.loadUnobtainableItems();
        if (this.searchField != null) {
            this.searchField.setVisible(false);
            this.searchField.setCanLoseFocus(true);
            this.searchField.setFocused(false);
        }
    }

    protected int getCloseKey() {
        return !this.isSearchActive() ? super.getCloseKey()
                : mc.gameSettings.keyBindClose.getKeyCode();
    }

    private void updateCreativeSearch() {
        GuiContainerCreative.ContainerCreative guicontainercreative$containercreative =
                (GuiContainerCreative.ContainerCreative) this.inventorySlots;
        guicontainercreative$containercreative.itemList.clear();

        for (Item item : Item.itemRegistry) {
            if (item != null && item.getCreativeTab() != null) {
                item.getSubItems(item, (CreativeTabs) null, guicontainercreative$containercreative.itemList);
            }
        }

        for (int i = 0; i < Enchantment.enchantmentsBookList.length; ++i) {
            Enchantment enchantment = Enchantment.enchantmentsBookList[i];
            if (enchantment != null && enchantment.type != null) {
                Items.enchanted_book.getAll(enchantment, guicontainercreative$containercreative.itemList);
            }
        }

        Iterator iterator = guicontainercreative$containercreative.itemList.iterator();
        String s1 = this.searchField.getText().toLowerCase();

        while (iterator.hasNext()) {
            ItemStack itemstack = (ItemStack) iterator.next();
            boolean flag = false;
            List<String> lst = itemstack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
            for (int i = 0, l = lst.size(); i < l; ++i) {
                if (EnumChatFormatting.getTextWithoutFormattingCodes(lst.get(i)).toLowerCase().contains(s1)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                iterator.remove();
            }
        }

        this.currentScroll = 0.0F;
        guicontainercreative$containercreative.scrollTo(0.0F);
    }

    /**+
     * Draw the foreground layer for the GuiContainer.
     */
    protected void drawGuiContainerForegroundLayer(int var1, int var2) {
        GlStateManager.disableBlend();
        if (this.isSearchActive()) {
            // When search is active (even on page 2), show "Search" not "Advanced Creative Tab"
            this.fontRendererObj.drawString(I18n.format(CreativeTabs.tabAllSearch.getTranslatedTabLabel(), new Object[0]), 8, 6, 4210752);
        } else if (this.showingUnobtainable && this.page2View == PAGE2_VIEW_SAVED) {
            this.fontRendererObj.drawString(
                    "\u00a7r" + I18n.format("key.actx.saveditems", new Object[0]) + "\u00a7r", 8, 6, 4210752);
        } else if (this.showingUnobtainable && this.page2View == PAGE2_VIEW_CUSTOM_BLOCKS) {
            this.fontRendererObj.drawString(
                    "\u00a7r" + I18n.format(actxcreativetabs.custom_blocks_title_key, new Object[0]) + "\u00a7r", 8, 6, 4210752);
        } else if (this.showingUnobtainable) {
            this.fontRendererObj.drawString(
                    "\u00a7r" + I18n.format("itemGroup.act", new Object[0]) + "\u00a7r", 8, 6, 4210752);
        } else {
            CreativeTabs creativetabs = CreativeTabs.creativeTabArray[selectedTabIndex];
            if (creativetabs.drawInForegroundOfTab()) {
                this.fontRendererObj.drawString(I18n.format(creativetabs.getTranslatedTabLabel(), new Object[0]), 8, 6,
                        4210752);
            }
        }
    }

    /**+
     * Called when the mouse is clicked.
     *
     * mouseClicked only swallows the click (so slot logic below doesn't
     * fire) when it's on a tab; mouseReleased is the single authoritative
     * handler that actually performs the tab switch.
     */
    protected void mouseClicked(int parInt1, int parInt2, int parInt3) {
        if (parInt3 == 0 && !this.showingUnobtainable) {
            int i = parInt1 - this.guiLeft;
            int j = parInt2 - this.guiTop;

            for (int k = 0; k < CreativeTabs.creativeTabArray.length; ++k) {
                CreativeTabs tab = CreativeTabs.creativeTabArray[k];
                // Skip the tab-0 top-row slot on page 1 in mouseClicked --
                // this prevents the "Building Blocks" tab from being consumed
                // here and then re-consumed in mouseReleased.
                // mouseReleased is the single handler for ALL tab clicks.
                if (this.func_147049_a(tab, i, j)) {
                    return; // consume the click so the slot/item logic below is skipped
                }
            }
        }

        super.mouseClicked(parInt1, parInt2, parInt3);
    }

    /**+
     * Called when a mouse button is released.
     *
     * This is the SINGLE authoritative handler for all tab switching.
     * It handles two cases in order:
     *   1. Normal tab click on page 1 (any of the regular tabs, including
     *      Building Blocks at column 0 -- it is a real, selectable tab).
     *   2. Search/Save tab clicks on page 2.
     * Page 1 <-> page 2 switching is handled entirely by the buttonLeft/
     * buttonRight arrows and the '<'/'>' keys, not by clicking a tab.
     */
    protected void mouseReleased(int i, int j, int k) {
        if (k == 0) {
            int relX = i - this.guiLeft;
            int relY = j - this.guiTop;

            // NOTE: Page switching is handled exclusively by the buttonLeft/
            // buttonRight arrow buttons (actionPerformed ids 101/102) and the
            // '<'/'>' keyTyped shortcuts. There is deliberately no click-to-
            // toggle-page hit-area here anymore -- tab column 0, top row is
            // the real "Building Blocks" tab (creativeTabArray[0]) and must
            // be selectable like any other tab, not hijacked as a page arrow.

            // --- Normal tab clicks on page 1 ---
            if (!this.showingUnobtainable) {
                for (int m = 0; m < CreativeTabs.creativeTabArray.length; ++m) {
                    CreativeTabs creativetabs = CreativeTabs.creativeTabArray[m];
                    if (this.func_147049_a(creativetabs, relX, relY)) {
                        // Switching to a normal tab always puts us back on page 1
                        this.showingUnobtainable = false;
                        this.currentCategory = 1;
                        if (this.buttonLeft  != null) this.buttonLeft.enabled  = false;
                        if (this.buttonRight != null) this.buttonRight.enabled = true;
                        this.setCurrentCreativeTab(creativetabs);
                        return;
                    }
                }
            }

            // --- Tabs clickable on page 2 (search + save) ---
            if (this.showingUnobtainable) {
                // Search tab
                CreativeTabs searchTab = CreativeTabs.tabAllSearch;
                if (this.func_147049_a(searchTab, relX, relY)) {
                    this.activateSearchTab();
                    return;
                }
                // Advanced Creative Tab (items grid, col 0 top row)
                if (this.isOverAdvancedTab(relX, relY)) {
                    this.activateItemsTab();
                    return;
                }
                // Save tab (bottom row col 1)
                if (this.isOverSaveTab(relX, relY)) {
                    this.activateSaveTab();
                    return;
                }
                // ActX Custom Blocks tab (col 1, top row)
                if (this.isOverCustomBlocksTab(relX, relY)) {
                    this.activateCustomBlocksTab();
                    return;
                }
            }
        }

        super.mouseReleased(i, j, k);
    }

    @Override
    protected void touchTapped(int touchX, int touchY, int uid) {
        int relX = touchX - this.guiLeft;
        int relY = touchY - this.guiTop;

        // Page switching is handled by the arrow buttons/keys only -- see
        // the note in mouseReleased. Building Blocks (column 0) is a normal,
        // fully selectable tab here too.
        if (!this.showingUnobtainable) {
            for (int m = 0; m < CreativeTabs.creativeTabArray.length; ++m) {
                CreativeTabs creativetabs = CreativeTabs.creativeTabArray[m];
                if (this.func_147049_a(creativetabs, relX, relY)) {
                    this.showingUnobtainable = false;
                    this.currentCategory = 1;
                    this.setCurrentCreativeTab(creativetabs);
                    break;
                }
            }
        } else {
            // On page 2, allow tapping search, items grid, and save tabs
            CreativeTabs searchTab = CreativeTabs.tabAllSearch;
            if (this.func_147049_a(searchTab, relX, relY)) {
                this.activateSearchTab();
            } else if (this.isOverAdvancedTab(relX, relY)) {
                this.activateItemsTab();
            } else if (this.isOverSaveTab(relX, relY)) {
                this.activateSaveTab();
            } else if (this.isOverCustomBlocksTab(relX, relY)) {
                this.activateCustomBlocksTab();
            }
        }

        super.touchTapped(touchX, touchY, uid);
    }

    /**+
     * Returns (if you are not on the inventoryTab) and (the flag isn't set)
     * and (you have more than 1 page of items).
     */
    private boolean needsScrollBars() {
        return !this.showingUnobtainable
                && selectedTabIndex != CreativeTabs.tabInventory.getTabIndex()
                && CreativeTabs.creativeTabArray[selectedTabIndex].shouldHidePlayerInventory()
                && ((GuiContainerCreative.ContainerCreative) this.inventorySlots).func_148328_e();
    }

    private void setCurrentCreativeTab(CreativeTabs parCreativeTabs) {
        int i = selectedTabIndex;
        selectedTabIndex = parCreativeTabs.getTabIndex();
        GuiContainerCreative.ContainerCreative guicontainercreative$containercreative =
                (GuiContainerCreative.ContainerCreative) this.inventorySlots;
        this.dragSplittingSlots.clear();
        guicontainercreative$containercreative.itemList.clear();
        parCreativeTabs.displayAllReleventItems(guicontainercreative$containercreative.itemList);
        if (parCreativeTabs == CreativeTabs.tabInventory) {
            Container container = this.mc.thePlayer.inventoryContainer;
            if (this.field_147063_B == null) {
                this.field_147063_B = guicontainercreative$containercreative.inventorySlots;
            }

            guicontainercreative$containercreative.inventorySlots = Lists.newArrayList();

            for (int j = 0; j < container.inventorySlots.size(); ++j) {
                GuiContainerCreative.CreativeSlot guicontainercreative$creativeslot =
                        new GuiContainerCreative.CreativeSlot((Slot) container.inventorySlots.get(j), j);
                guicontainercreative$containercreative.inventorySlots.add(guicontainercreative$creativeslot);
                if (j >= 5 && j < 9) {
                    int j1 = j - 5;
                    int k1 = j1 / 2;
                    int l1 = j1 % 2;
                    guicontainercreative$creativeslot.xDisplayPosition = 9 + k1 * 54;
                    guicontainercreative$creativeslot.yDisplayPosition = 6 + l1 * 27;
                } else if (j >= 0 && j < 5) {
                    guicontainercreative$creativeslot.yDisplayPosition = -2000;
                    guicontainercreative$creativeslot.xDisplayPosition = -2000;
                } else if (j < container.inventorySlots.size()) {
                    int kk = j - 9;
                    int ll = kk % 9;
                    int i1 = kk / 9;
                    guicontainercreative$creativeslot.xDisplayPosition = 9 + ll * 18;
                    if (j >= 36) {
                        guicontainercreative$creativeslot.yDisplayPosition = 112;
                    } else {
                        guicontainercreative$creativeslot.yDisplayPosition = 54 + i1 * 18;
                    }
                }
            }

            this.field_147064_C = new Slot(field_147060_v, 0, 173, 112);
            guicontainercreative$containercreative.inventorySlots.add(this.field_147064_C);
            } else if (this.field_147063_B != null) {
                guicontainercreative$containercreative.inventorySlots = this.field_147063_B;
                this.field_147063_B = null;
            }

        if (this.searchField != null) {
            if (parCreativeTabs == CreativeTabs.tabAllSearch) {
                this.searchField.setVisible(true);
                this.searchField.setCanLoseFocus(false);
                this.searchField.setFocused(true);
                this.searchField.setText("");
                this.updateCreativeSearch();
            } else {
                this.searchField.setVisible(false);
                this.searchField.setCanLoseFocus(true);
                this.searchField.setFocused(false);
            }
        }

        this.currentScroll = 0.0F;
        guicontainercreative$containercreative.scrollTo(0.0F);

        if (!this.showingUnobtainable) {
            this.lastPage1TabIndex = selectedTabIndex;
        }
    }

    /**
     * If the Survival Inventory tab's special player-inventory slots (health,
     * armor, hotbar mirror) are currently active, swap them back out for the
     * normal creative item-grid slots. Must be called before switching to
     * page 2, otherwise those slots stay layered on top of the page-2 grid.
     */
    private void restoreInventorySlotsIfNeeded() {
        if (selectedTabIndex == CreativeTabs.tabInventory.getTabIndex() && this.field_147063_B != null) {
            ((GuiContainerCreative.ContainerCreative) this.inventorySlots).inventorySlots = this.field_147063_B;
            this.field_147063_B = null;
        }
    }

    /**+
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();
        if (i != 0 && this.needsScrollBars()) {
            int j = ((GuiContainerCreative.ContainerCreative) this.inventorySlots).itemList.size() / 9 - 5;
            if (i > 0) i = 1;
            if (i < 0) i = -1;
            this.currentScroll = (float) ((double) this.currentScroll - (double) i / (double) j);
            this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
            ((GuiContainerCreative.ContainerCreative) this.inventorySlots).scrollTo(this.currentScroll);
        }
    }

    /**+
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int i, int j, float f) {
        boolean flag = PointerInputAbstraction.getVCursorButtonDown(0);
        int k  = this.guiLeft;
        int l  = this.guiTop;
        int i1 = k + 175;
        int j1 = l + 18;
        int k1 = i1 + 14;
        int l1 = j1 + 112;
        if (!this.wasClicking && flag && i >= i1 && j >= j1 && i < k1 && j < l1) {
            this.isScrolling = this.needsScrollBars();
        }
        if (!flag) {
            this.isScrolling = false;
        }
        this.wasClicking = flag;
        if (this.isScrolling) {
            this.currentScroll = ((float) (j - j1) - 7.5F) / ((float) (l1 - j1) - 15.0F);
            this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
            ((GuiContainerCreative.ContainerCreative) this.inventorySlots).scrollTo(this.currentScroll);
        }

        super.drawScreen(i, j, f);

        // On page 1, show hover text for all tabs.
        // On page 2, only show hover text for the search tab (others are hidden).
        if (!this.showingUnobtainable) {
            for (int m = 0; m < CreativeTabs.creativeTabArray.length; ++m) {
                if (this.renderCreativeInventoryHoveringText(CreativeTabs.creativeTabArray[m], i, j)) {
                    Mouse.showCursor(EnumCursorType.HAND);
                    break;
                }
            }
        } else {
            // On page 2, show hover cursor/text for the search tab, items grid
            // tab, and Save tab
            if (this.renderCreativeInventoryHoveringText(CreativeTabs.tabAllSearch, i, j)) {
                Mouse.showCursor(EnumCursorType.HAND);
            }
            int relX2 = i - this.guiLeft;
            int relY2 = j - this.guiTop;
            if (this.isOverAdvancedTab(relX2, relY2)) {
                this.drawCreativeTabHoveringText(I18n.format("itemGroup.act", new Object[0]), i, j);
                Mouse.showCursor(EnumCursorType.HAND);
            }
            if (this.isOverSaveTab(relX2, relY2)) {
                this.drawCreativeTabHoveringText(I18n.format("key.actx.saveditems", new Object[0]), i, j);
                Mouse.showCursor(EnumCursorType.HAND);
            }
            if (this.isOverCustomBlocksTab(relX2, relY2)) {
                this.drawCreativeTabHoveringText(I18n.format(actxcreativetabs.custom_blocks_title_key, new Object[0]), i, j);
                Mouse.showCursor(EnumCursorType.HAND);
            }
        }

        if (this.field_147064_C != null && this.isInventoryTabActive()
                && this.isPointInRegion(this.field_147064_C.xDisplayPosition, this.field_147064_C.yDisplayPosition,
                        16, 16, i, j)) {
            this.drawCreativeTabHoveringText(I18n.format("inventory.binSlot", new Object[0]), i, j);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
    }

    private String getKeybindDisplay(String descKey) {
        KeyBinding kb = findKeyBinding(descKey);
        return kb != null ? GameSettings.getKeyDisplayString(kb.getKeyCode()) : "?";
    }

    /** Builds a comma-separated list of NBT tag key names. */
    private String buildTagSummary(java.util.Set<String> keys) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String key : keys) {
            if (!first) sb.append("\u00a77, ");
            sb.append("\u00a7f").append(key);
            first = false;
        }
        return sb.toString();
    }

    protected void renderToolTip(ItemStack itemstack, int i, int j) {
        if (itemstack == null) {
            super.renderToolTip(itemstack, i, j);
            return;
        }

        boolean lShiftHeld = Keyboard.isKeyDown(42); // 42 = L-Shift

        if (lShiftHeld) {
            List<String> lines = new ArrayList<String>();

            String displayName = itemstack.getDisplayName();
            int itemMeta = itemstack.getItemDamage();
            int itemId = Item.getIdFromItem(itemstack.getItem());
            lines.add(displayName + " (#" + itemId + "/" + itemMeta + ")");

            ResourceLocation regName = Item.itemRegistry.getNameForObject(itemstack.getItem());
            if (regName != null) {
                lines.add("\u00a77" + regName.toString());
            }

            NBTTagCompound nbt = itemstack.getTagCompound();
            if (itemstack.isItemStackDamageable()) {
                int maxDur = itemstack.getMaxDamage();
                int curDur = maxDur - itemstack.getItemDamage();
                // Color: green if full, yellow if damaged, red if critical (<10%)
                String durColor;
                if (itemstack.getItemDamage() == 0) {
                    durColor = "\u00a72"; // green
                } else if (curDur <= maxDur / 10) {
                    durColor = "\u00a74"; // red
                } else {
                    durColor = "\u00a7e"; // yellow
                }
                lines.add(I18n.format("durabilityinfo") + durColor + curDur + " / " + maxDur);
            }
            
            if (nbt != null && nbt.hasKey("display")) {
                NBTTagCompound display = nbt.getCompoundTag("display");
                if (display.hasKey("color")) {
                    int color = display.getInteger("color");
                    lines.add(I18n.format("colorinfo") + String.format("%06X", color));
                }
            }

            // Checking RepairCost using the existing 'nbt' variable
            if (nbt != null && nbt.hasKey("RepairCost")) {
                int repairCost = nbt.getInteger("RepairCost");
                String repairColor = repairCost == 0 ? "\u00a72" : "\u00a74";
                lines.add(I18n.format("repaircostboi") + repairColor + repairCost);
            }

            if (itemstack.getItem() instanceof ItemTool) {
                try {
                    ItemTool tool = (ItemTool) itemstack.getItem();
                    java.lang.reflect.Field matField = ItemTool.class.getDeclaredField("toolMaterial");
                    matField.setAccessible(true);
                    Item.ToolMaterial mat = (Item.ToolMaterial) matField.get(tool);
                    if (mat != null) {
                        lines.add(I18n.format("minespeed") + mat.getEfficiencyOnProperMaterial());
                    }
                } catch (Exception ignored) {
                    // Falls back cleanly if reflection fails
                }
            }

            // Tags: count + each key: value
            if (nbt != null) {
                java.util.Set<String> keys = nbt.getKeySet();
                lines.add(I18n.format("tagyes") + "(§e" + keys.size() + "§6): " + buildTagSummary(keys));
            } else {
                lines.add(I18n.format("tagnonada"));
            }

            // Action hints at the bottom
            String giverKeyShift = getKeybindDisplay("gui.act.give");
            String saverKeyShift = getKeybindDisplay("key.actx.saveditems");
            lines.add(I18n.format("gui.act.creative.giverHint", giverKeyShift));
            lines.add(I18n.format("gui.act.creative.saverHint", saverKeyShift));

            this.drawHoveringText(lines, i, j);
            return;
        }

        // --- Normal tooltip (unchanged existing behavior) ---
        List list = itemstack.getTooltipProfanityFilter(this.mc.thePlayer,
                this.mc.gameSettings.advancedItemTooltips);
        // Keybind hints in the tooltip instead of gray screen text
        list.add(I18n.format("moreoptions"));
        if (this.isSearchActive()) {
            CreativeTabs creativetabs = itemstack.getItem().getCreativeTab();
            if (creativetabs == null && itemstack.getItem() == Items.enchanted_book) {
                IntIntMap map = EnchantmentHelper.getEnchantments(itemstack);
                if (map.size() == 1) {
                    Enchantment enchantment = Enchantment.getEnchantmentById(map.keys().iterator().next().value);
                    for (int m = 0; m < CreativeTabs.creativeTabArray.length; ++m) {
                        CreativeTabs creativetabs1 = CreativeTabs.creativeTabArray[m];
                        if (creativetabs1.hasRelevantEnchantmentType(enchantment.type)) {
                            creativetabs = creativetabs1;
                            break;
                        }
                    }
                }
            }
            if (creativetabs != null) {
                list.add(1, "" + EnumChatFormatting.BOLD + EnumChatFormatting.BLUE
                        + I18n.format(creativetabs.getTranslatedTabLabel(), new Object[0]));
            }
            for (int kk = 0; kk < list.size(); ++kk) {
                if (kk == 0) {
                    list.set(kk, itemstack.getRarity().rarityColor + (String) list.get(kk));
                } else {
                    list.set(kk, EnumChatFormatting.GRAY + (String) list.get(kk));
                }
            }
        } else {
            if (!list.isEmpty()) {
                list.set(0, itemstack.getRarity().rarityColor + (String) list.get(0));
            }
        }
        this.drawHoveringText(list, i, j);
    }

    /**+
     * Args : renderPartialTicks, mouseX, mouseY
     */
    protected void drawGuiContainerBackgroundLayer(float var1, int i, int j) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        CreativeTabs creativetabs = CreativeTabs.creativeTabArray[selectedTabIndex];

        if (!this.showingUnobtainable) {
            for (int m = 0; m < CreativeTabs.creativeTabArray.length; ++m) {
                CreativeTabs creativetabs1 = CreativeTabs.creativeTabArray[m];
                this.mc.getTextureManager().bindTexture(creativeInventoryTabs);
                if (creativetabs1.getTabIndex() != selectedTabIndex) {
                    this.func_147051_a(creativetabs1);
                }
            }
        } else {
            if (this.page2View != PAGE2_VIEW_ITEMS) {
                this.mc.getTextureManager().bindTexture(creativeInventoryTabs);
                this.func_147051_a(CreativeTabs.creativeTabArray[0]);
            }
            if (this.page2View != PAGE2_VIEW_SEARCH) {
                this.mc.getTextureManager().bindTexture(creativeInventoryTabs);
                this.func_147051_a(CreativeTabs.tabAllSearch);
            }
            if (this.page2View != PAGE2_VIEW_SAVED) {
                this.mc.getTextureManager().bindTexture(creativeInventoryTabs);
                this.drawSaveTab(false);
            }
            if (this.page2View != PAGE2_VIEW_CUSTOM_BLOCKS) {
                this.mc.getTextureManager().bindTexture(creativeInventoryTabs);
                this.drawCustomBlocksTab(false);
            }
        }

        // On page 2, always use the items background; only use tab-specific bg on page 1
        String bgName;
            if (this.showingUnobtainable) {
            bgName = (this.page2View == PAGE2_VIEW_SEARCH) ? "item_search.png" : "items.png";
            } else {
                bgName = creativetabs.getBackgroundImageName();
        }
        this.mc.getTextureManager().bindTexture(new ResourceLocation(
                "textures/gui/container/creative_inventory/tab_" + bgName));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.searchField.drawTextBox();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int kk = this.guiLeft + 175;
        int ll = this.guiTop + 18;
        int i1 = ll + 112;
        this.mc.getTextureManager().bindTexture(creativeInventoryTabs);
        if (!this.showingUnobtainable && creativetabs.shouldHidePlayerInventory()) {
            this.drawTexturedModalRect(kk, ll + (int) ((float) (i1 - ll - 17) * this.currentScroll),
                    232 + (this.needsScrollBars() ? 0 : 12), 0, 12, 15);
        }

        if (this.showingUnobtainable) {
            // Page 2: draw ONLY the currently-active tab after the background
            // panel, so it (and only it) renders on top -- mirrors the
            // selected-tab-on-top behavior page 1 already has below.
            this.mc.getTextureManager().bindTexture(creativeInventoryTabs);
            if (this.page2View == PAGE2_VIEW_ITEMS) {
                this.func_147051_a(CreativeTabs.creativeTabArray[0]); // command block toggle slot
            } else if (this.page2View == PAGE2_VIEW_SEARCH) {
                this.func_147051_a(CreativeTabs.tabAllSearch);        // search tab
            } else if (this.page2View == PAGE2_VIEW_SAVED) {
                this.drawSaveTab(true);                                // save tab (col 0 bottom)
            } else if (this.page2View == PAGE2_VIEW_CUSTOM_BLOCKS) {
                this.drawCustomBlocksTab(true);                        // custom blocks tab (col 1 top)
            }
        } else {
            this.func_147051_a(creativetabs); // draw selected tab on top on page 1
        }
        if (this.isInventoryTabActive()) {
            GuiInventory.drawEntityOnScreen(this.guiLeft + 43, this.guiTop + 45, 20,
                    (float) (this.guiLeft + 43 - i), (float) (this.guiTop + 45 - 30 - j), this.mc.thePlayer);
        }

        // Page counter
        GlStateManager.disableLighting();
        String pageText = I18n.format("gui.act.creative.pageCounter", this.currentCategory, 2);
        int centerX = this.guiLeft + this.xSize / 2;
        int textW = this.fontRendererObj.getStringWidth(pageText);
        this.fontRendererObj.drawStringWithShadow(pageText, centerX - textW / 2, this.guiTop - 42, 0xFFFFFF);

        // Keybind hints are shown in tooltips, not as screen text.
    }

    protected boolean func_147049_a(CreativeTabs parCreativeTabs, int parInt1, int parInt2) {
        int i = parCreativeTabs.getTabColumn();
        int j = 28 * i;
        int k = 0;
        if (i == 5) {
            j = this.xSize - 28 + 2;
        } else if (i > 0) {
            j += i;
        }
        if (parCreativeTabs.isTabInFirstRow()) {
            k = k - 32;
        } else {
            k = k + this.ySize;
        }
        return parInt1 >= j && parInt1 <= j + 28 && parInt2 >= k && parInt2 <= k + 32;
    }

    /**+
     * Renders the creative inventory hovering text if mouse is over it.
     */
    protected boolean renderCreativeInventoryHoveringText(CreativeTabs parCreativeTabs, int parInt1, int parInt2) {
        int i = parCreativeTabs.getTabColumn();
        int j = 28 * i;
        int k = 0;
        if (i == 5) {
            j = this.xSize - 28 + 2;
        } else if (i > 0) {
            j += i;
        }
        if (parCreativeTabs.isTabInFirstRow()) {
            k = k - 32;
        } else {
            k = k + this.ySize;
        }
        if (this.isPointInRegion(j + 3, k + 3, 23, 27, parInt1, parInt2)) {
            String label = (parCreativeTabs.getTabColumn() == 0 && parCreativeTabs.isTabInFirstRow()
                    && this.showingUnobtainable)
                    ? I18n.format("itemGroup.act", new Object[0])
                    : I18n.format(parCreativeTabs.getTranslatedTabLabel(), new Object[0]);
            this.drawCreativeTabHoveringText(label, parInt1, parInt2);
            return true;
        }
        return false;
    }

    protected void func_147051_a(CreativeTabs parCreativeTabs) {
        boolean flag;
        if (this.showingUnobtainable) {
            boolean isItemsGridIcon = parCreativeTabs.getTabColumn() == 0 && parCreativeTabs.isTabInFirstRow();
            if (isItemsGridIcon) {
                flag = this.page2View == PAGE2_VIEW_ITEMS;
            } else if (parCreativeTabs == CreativeTabs.tabAllSearch) {
                flag = this.page2View == PAGE2_VIEW_SEARCH;
            } else {
                flag = false;
            }
        } else {
            flag = parCreativeTabs.getTabIndex() == selectedTabIndex;
        }
        boolean flag1 = parCreativeTabs.isTabInFirstRow();
        int i  = parCreativeTabs.getTabColumn();
        int j  = i * 28;
        int k  = 0;
        int l  = this.guiLeft + 28 * i;
        int i1 = this.guiTop;
        byte b0 = 32;
        if (flag) k += 32;
        if (i == 5) {
            l = this.guiLeft + this.xSize - 28;
        } else if (i > 0) {
            l += i;
        }
        if (flag1) {
            i1 = i1 - 28;
        } else {
            k += 64;
            i1 = i1 + (this.ySize - 4);
        }
        GlStateManager.disableLighting();
        this.drawTexturedModalRect(l, i1, j, k, 28, b0);
        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;
        l  = l  + 6;
        i1 = i1 + 8 + (flag1 ? 1 : -1);
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();
        // Command block tab col 0 top-row: show command block on page 2, normal icon on page 1
        ItemStack itemstack;
        if (parCreativeTabs.getTabColumn() == 0 && parCreativeTabs.isTabInFirstRow() && this.showingUnobtainable) {
            itemstack = new ItemStack(net.minecraft.init.Blocks.command_block, 1, 0);
        } else {
            itemstack = parCreativeTabs.getIconItemStack();
        }
        this.itemRender.renderItemAndEffectIntoGUI(itemstack, l, i1);
        this.itemRender.renderItemOverlays(this.fontRendererObj, itemstack, l, i1);
        GlStateManager.disableLighting();
        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    /**
     * Returns true if the given GUI-relative coords land on the
     * command-block "Advanced Creative" toggle slot (tab col 0, top row).
     */
    /**
     * Finds an ActX (or any) keybinding by its description/lang key.
     * Returns null if not found.
     */
    private KeyBinding findKeyBinding(String descriptionKey) {
        for (KeyBinding kb : this.mc.gameSettings.keyBindings) {
            if (descriptionKey.equals(kb.getKeyDescription())) {
                return kb;
            }
        }
        return null;
    }

    /**
     * Returns true if the given GUI-relative coords land on the Save tab.
     * The Save tab mirrors the Advanced Creative Tab but on the bottom row (col 0, bottom).
     */
    private boolean isOverSaveTab(int relX, int relY) {
        // col 0, bottom row: x in [0,28], y in [ySize, ySize+32]
        return relX >= 0 && relX <= 28 && relY >= this.ySize && relY <= this.ySize + 32;
    }

    /**
     * Draws the Save tab on page 2 at col 0 bottom row (mirrors Advanced Creative Tab position).
     * Uses map item as icon.
     */
    private void drawSaveTab(boolean selected) {
        int tabX = this.guiLeft; // Column 0
        int tabY = this.guiTop + (this.ySize - 4); // Bottom row
        int texX = 0; // Texture column 0
        int texY = 64 + (selected ? 32 : 0); // Bottom row: unselected=64, selected=96

    // 1. Force zLevel back to 0 so the tab texture doesn't render over the GUI border
        this.zLevel = 0.0F;
        this.itemRender.zLevel = 0.0F;

        this.mc.getTextureManager().bindTexture(creativeInventoryTabs);
        GlStateManager.disableLighting();
        this.drawTexturedModalRect(tabX, tabY, texX, texY, 28, 32);

    // 2. Bump zLevel ONLY for the map icon so it renders above the tab texture
        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();

        ItemStack mapStack = new ItemStack(Items.map, 1, 0);
        this.itemRender.renderItemAndEffectIntoGUI(mapStack, tabX + 6, tabY + 7);
        this.itemRender.renderItemOverlays(this.fontRendererObj, mapStack, tabX + 6, tabY + 7);

    // 3. Reset zLevel back to normal
        GlStateManager.disableLighting();
        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    /**
     * Returns true if the given GUI-relative coords land on the ActX Custom
     * Blocks tab (col 1, top row - directly right of the Advanced Creative
     * Tab). Same derivation as isOverSaveTab, using func_147049_a's column/
     * row formula for column 1, first row: j = 28*1 + 1 (the "i>0 -> j+=i"
     * gap), k = -32 (first-row offset).
     */
    private boolean isOverCustomBlocksTab(int relX, int relY) {
        return relX >= 29 && relX <= 57 && relY >= -32 && relY <= 0;
    }

    /**
     * Draws the ActX Custom Blocks tab on page 2 at col 1 top row (directly
     * right of the Advanced Creative Tab). Mirrors drawSaveTab's hand-drawn
     * approach - this isn't a real CreativeTabs entry either - but placed
     * per func_147051_a's top-row formula instead of the bottom-row one:
     * tabX = guiLeft + 28*1 + 1, tabY = guiTop - 28, icon offset tabY + 9
     * (top row uses "+8 +1"; drawSaveTab's bottom row uses "+8 -1" = +7).
     */
    private void drawCustomBlocksTab(boolean selected) {
        int tabX = this.guiLeft + 28 + 1; // Column 1
        int tabY = this.guiTop - 28;      // Top row
        int texX = 28;                     // Texture column 1
        int texY = selected ? 32 : 0;      // Top row: unselected=0, selected=32

        this.zLevel = 0.0F;
        this.itemRender.zLevel = 0.0F;

        this.mc.getTextureManager().bindTexture(creativeInventoryTabs);
        GlStateManager.disableLighting();
        this.drawTexturedModalRect(tabX, tabY, texX, texY, 28, 32);

        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();

        ItemStack iconStack = actxcreativetabs.getCustomBlocksTabIcon();
        this.itemRender.renderItemAndEffectIntoGUI(iconStack, tabX + 6, tabY + 9);
        this.itemRender.renderItemOverlays(this.fontRendererObj, iconStack, tabX + 6, tabY + 9);

        GlStateManager.disableLighting();
        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    /**
     * Loads the player's saved items (from saveditems) directly into the creative grid.
     * The save tab on page 2 (map icon) triggers this inline — no screen switch.
     */
    private boolean showingSavedItems = false;

    private void openSavedItemsTab() {
        showingSavedItems = true;
        GuiContainerCreative.ContainerCreative container =
                (GuiContainerCreative.ContainerCreative) this.inventorySlots;
        container.itemList.clear();
        // Pull saved stacks from saveditems' static list
        for (ItemStack stack : saveditems.getSavedStacks()) {
            if (stack != null) container.itemList.add(stack);
        }
        this.currentScroll = 0.0F;
        container.scrollTo(0.0F);
        if (this.searchField != null) {
            this.searchField.setVisible(false);
            this.searchField.setCanLoseFocus(true);
            this.searchField.setFocused(false);
        }
    }

    private boolean isOverAdvancedTab(int relX, int relY) {
        return this.func_147049_a(CreativeTabs.creativeTabArray[0], relX, relY);
    }

    private void loadUnobtainableItems() {
        GuiContainerCreative.ContainerCreative container =
                (GuiContainerCreative.ContainerCreative) this.inventorySlots;
        container.itemList.clear();
        container.itemList.addAll(net.minecraft.actx.utils.actxcreativeitems.getAdvancedCreativeItems());

        this.currentScroll = 0.0F;
        container.scrollTo(0.0F);
    }

    /**
     * Loads the ActX Custom Blocks tab's item list (block 198, plus command
     * block metadata 1/2). Mirrors loadUnobtainableItems().
     */
    private void loadCustomBlockItems() {
        GuiContainerCreative.ContainerCreative container =
                (GuiContainerCreative.ContainerCreative) this.inventorySlots;
        container.itemList.clear();
        container.itemList.addAll(net.minecraft.actx.utils.actxcreativeitems.getCustomBlockItems());

        this.currentScroll = 0.0F;
        container.scrollTo(0.0F);
    }

    /**+
     * Called by the controls from the buttonList when activated.
     */
    protected void actionPerformed(GuiButton parGuiButton) {
        if (parGuiButton.id == 0) {
            this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.getStatFileWriter()));
        }
        if (parGuiButton.id == 1) {
            this.mc.displayGuiScreen(new GuiStats(this, this.mc.thePlayer.getStatFileWriter()));
        }

        if (parGuiButton.id == 101) {
            // Left arrow: back to page 1
            if (this.currentCategory > 1) {
                this.currentCategory--;
                this.showingUnobtainable = false;
                this.page2View = PAGE2_VIEW_ITEMS;
                this.showingSavedItems = false;
                this.setCurrentCreativeTab(CreativeTabs.creativeTabArray[this.lastPage1TabIndex]);
            }
        } else if (parGuiButton.id == 102) {
            // Right arrow: page 2
            if (this.currentCategory < 2) {
                this.lastPage1TabIndex = selectedTabIndex;
                this.currentCategory++;
                this.restoreInventorySlotsIfNeeded();
                this.showingUnobtainable = true;
                this.page2View = PAGE2_VIEW_ITEMS;
                this.showingSavedItems = false;
                this.dragSplittingSlots.clear();
                this.loadUnobtainableItems();
                if (this.searchField != null) {
                    this.searchField.setVisible(false);
                    this.searchField.setCanLoseFocus(true);
                    this.searchField.setFocused(false);
                }
            }
        }
        if (this.buttonLeft  != null) this.buttonLeft.enabled  = (this.currentCategory > 1);
        if (this.buttonRight != null) this.buttonRight.enabled = (this.currentCategory < 2);
    }

    public int getSelectedTabIndex() {
        return selectedTabIndex;
    }

    /**
     * Translates '&' color codes (e.g. "&cRed &lBold") into real Minecraft
     * formatting codes so they render as colors/styles instead of literal
     * text. Only touches '&' when it's immediately followed by a valid
     * 0-9 / a-f / k-o / r code character, so ordinary ampersands (e.g.
     * "Fish & Chips") are left alone.
     *
     * Call this on the typed name right before you store/apply it, e.g.:
     *   itemStack.setStackDisplayName(GuiContainerCreative.translateColorCodes(typedName));
     */
    public static String translateColorCodes(String text) {
        if (text == null) return null;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(chars[i + 1]) > -1) {
                chars[i] = '\u00A7';
            }
        }
        return new String(chars);
    }

    static class ContainerCreative extends Container {
        public List<ItemStack> itemList = Lists.newArrayList();

        public ContainerCreative(EntityPlayer parEntityPlayer) {
            InventoryPlayer inventoryplayer = parEntityPlayer.inventory;
            for (int i = 0; i < 5; ++i) {
                for (int j = 0; j < 9; ++j) {
                    this.addSlotToContainer(
                            new Slot(GuiContainerCreative.field_147060_v, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(new Slot(inventoryplayer, k, 9 + k * 18, 112));
            }
            this.scrollTo(0.0F);
        }

        public boolean canInteractWith(EntityPlayer playerIn) { return true; }

        public void scrollTo(float parFloat1) {
            int i = (this.itemList.size() + 9 - 1) / 9 - 5;
            int j = (int) ((double) (parFloat1 * (float) i) + 0.5D);
            if (j < 0) j = 0;
            for (int k = 0; k < 5; ++k) {
                for (int l = 0; l < 9; ++l) {
                    int i1 = l + (k + j) * 9;
                    if (i1 >= 0 && i1 < this.itemList.size()) {
                        GuiContainerCreative.field_147060_v.setInventorySlotContents(l + k * 9,
                                (ItemStack) this.itemList.get(i1));
                    } else {
                        GuiContainerCreative.field_147060_v.setInventorySlotContents(l + k * 9, (ItemStack) null);
                    }
                }
            }
        }

        public boolean func_148328_e() { return this.itemList.size() > 45; }

        protected void retrySlotClick(int slotId, int clickedButton, boolean mode, EntityPlayer playerIn) {}

        public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
            if (index >= this.inventorySlots.size() - 9 && index < this.inventorySlots.size()) {
                Slot slot = (Slot) this.inventorySlots.get(index);
                if (slot != null && slot.getHasStack()) {
                    slot.putStack((ItemStack) null);
                }
            }
            return null;
        }

        public boolean canMergeSlot(ItemStack stack, Slot parSlot) {
            return parSlot.yDisplayPosition > 90;
        }

        public boolean canDragIntoSlot(Slot parSlot) {
            return parSlot.inventory instanceof InventoryPlayer
                    || parSlot.yDisplayPosition > 90 && parSlot.xDisplayPosition <= 162;
        }
    }

    class CreativeSlot extends Slot {
        private final Slot slot;

        public CreativeSlot(Slot parSlot, int parInt1) {
            super(parSlot.inventory, parInt1, 0, 0);
            this.slot = parSlot;
        }

        public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) { this.slot.onPickupFromSlot(playerIn, stack); }
        public boolean isItemValid(ItemStack stack) { return this.slot.isItemValid(stack); }
        public ItemStack getStack() { return this.slot.getStack(); }
        public boolean getHasStack() { return this.slot.getHasStack(); }
        public void putStack(ItemStack stack) { this.slot.putStack(stack); }
        public void onSlotChanged() { this.slot.onSlotChanged(); }
        public int getSlotStackLimit() { return this.slot.getSlotStackLimit(); }
        public int getItemStackLimit(ItemStack stack) { return this.slot.getItemStackLimit(stack); }
        public String getSlotTexture() { return this.slot.getSlotTexture(); }
        public ItemStack decrStackSize(int amount) { return this.slot.decrStackSize(amount); }
        public boolean isHere(IInventory inv, int slotIn) { return this.slot.isHere(inv, slotIn); }
    }

    public boolean blockPTTKey() { return searchField.isFocused(); }

    @Override
    public boolean showCopyPasteButtons() { return searchField.isFocused(); }

    @Override
    public void fireInputEvent(EnumInputEvent event, String param) { searchField.fireInputEvent(event, param); }
}