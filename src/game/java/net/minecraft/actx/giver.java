package net.minecraft.actx;
import net.minecraft.actx.guis.itemstackmodifier;
import java.io.IOException;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
public class giver extends GuiScreen {
    protected GuiScreen parentscreen;
    private GuiButton givebutton;
    private GuiButton savebutton;
    private GuiButton donebutton;
    private GuiTextField iteminputfield;
    private ItemStack currentitemstack;
    private String initialtext;
    public GuiScreen getParentScreen() {
        return this.parentscreen;
    }
    public giver(GuiScreen parent, ItemStack helditem, String command) {
        this.parentscreen = parent;
        if (command != null && !command.isEmpty()) {
            this.initialtext = command;
        } else if (helditem != null && helditem.getItem() != null) {
            this.initialtext = buildGiveCommand(helditem);
        } else {
            this.initialtext = "";
        }
    }
    public giver(GuiScreen parent, ItemStack helditem) {
        this(parent, helditem, null);
    }
    public static giver open(GuiScreen parent, ItemStack helditem) {
        if (helditem == null || helditem.getItem() == null) {
            return new giver(parent, null, "");
        }
        return new giver(parent, helditem);
    }
    @Override
    public void initGui() {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        int fieldwidth = 356;
        int fieldheight = 16;
        int fieldx = this.width / 2 - fieldwidth / 2;
        int fieldy = this.height / 2 + 2;
        this.iteminputfield = new GuiTextField(0, this.fontRendererObj, fieldx, fieldy, fieldwidth, fieldheight);
        this.iteminputfield.setMaxStringLength(32767);
        this.iteminputfield.setFocused(true);
        this.iteminputfield.setText(convertSectionSignsToAmpersand(this.initialtext));
        this.iteminputfield.setCursorPositionEnd();
        int row1y = this.height / 2 + 21;
        this.givebutton = new GuiButton(1, this.width / 2 - 180, row1y, 180, 20, I18n.format("gui.act.give.give"));
        this.buttonList.add(this.givebutton);
        this.buttonList.add(new GuiButton(2, this.width / 2 + 2, row1y, 178, 20, I18n.format("gui.act.give.copy")));
        int row2y = this.height / 2 + 42;
        this.buttonList.add(new GuiButton(3, this.width / 2 - 180, row2y, 120, 20, I18n.format("gui.act.give.editor")));
        this.savebutton = new GuiButton(6, this.width / 2 - 58, row2y, 118, 20, I18n.format("gui.act.save"));
        this.buttonList.add(this.savebutton);
        int donex = this.width / 2 + 61;
        int copyrightedge = this.width / 2 + 180;
        this.donebutton = new GuiButton(0, donex, row2y, copyrightedge - donex, 20, I18n.format("gui.done"));
        this.buttonList.add(this.donebutton);
        this.func_73876_c();
    }
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    @Override
    public void updateScreen() {
        this.func_73876_c();
    }
    public void func_73876_c() {
        this.iteminputfield.updateCursorCounter();
        this.currentitemstack = parseStackFromField();
        boolean iscreative = this.mc.thePlayer != null && this.mc.thePlayer.capabilities.isCreativeMode;
        if (this.currentitemstack != null) {
            this.givebutton.enabled = iscreative;
            this.savebutton.enabled = true;
        } else {
            this.currentitemstack = null;
            this.givebutton.enabled = false;
            this.savebutton.enabled = false;
        }
    }
    @Override
    protected void keyTyped(char typedchar, int keycode) {
        if (this.iteminputfield.textboxKeyTyped(typedchar, keycode)) {
            this.func_73876_c();
            return;
        }
        if (keycode == 1) {
            this.mc.displayGuiScreen(this.parentscreen);
        }
    }
    @Override
    protected void mouseClicked(int mousex, int mousey, int mousebutton) {
        super.mouseClicked(mousex, mousey, mousebutton);
        this.iteminputfield.mouseClicked(mousex, mousey, mousebutton);
    }
    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }
        if (button.id == 0) {
            this.mc.displayGuiScreen(this.parentscreen);
        } else if (button.id == 1) {
            giveItem();
        } else if (button.id == 2) {
            GuiScreen.setClipboardString(this.iteminputfield.getText());
        } else if (button.id == 3) {
            ItemStack editorstack = this.currentitemstack != null ? this.currentitemstack : this.mc.thePlayer.getHeldItem();
            if (editorstack == null) {
                editorstack = new ItemStack(Block.getBlockById(3), 1, 0);
            }
            this.mc.displayGuiScreen(new itemstackmodifier(this, editorstack));
        } else if (button.id == 6) {
            String cmd = this.iteminputfield.getText().trim();
            if (!cmd.isEmpty()) {
                saveditems.saveCommand(cmd);
            }
            this.mc.displayGuiScreen(new saveditems(this.parentscreen));
        }
    }
    @Override
    public void drawScreen(int mousex, int mousey, float partialticks) {
        this.drawDefaultBackground();
        this.iteminputfield.drawTextBox();
        this.drawCenteredString(this.fontRendererObj, I18n.format("gui.act.give"), this.width / 2, this.iteminputfield.yPosition - 21, 16753920);
        int previewx = this.iteminputfield.xPosition + this.iteminputfield.getWidth() + 12;
        int previewy = this.iteminputfield.yPosition - 2;
        if (this.currentitemstack != null) {
            RenderHelper.disableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            this.itemRender.renderItemAndEffectIntoGUI(this.currentitemstack, previewx, previewy);
            this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.currentitemstack, previewx, previewy, null);
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (mousex >= previewx && mousex <= previewx + 16 && mousey >= previewy && mousey <= previewy + 16) {
                this.renderToolTip(this.currentitemstack, mousex, mousey);
            }
        } else {
            this.drawString(this.fontRendererObj, EnumChatFormatting.GRAY + "?", previewx + 3, previewy + 4, 16711680);
        }
        super.drawScreen(mousex, mousey, partialticks);
    }
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    private int findOpenSlot() {
        net.minecraft.entity.player.InventoryPlayer inv = this.mc.thePlayer.inventory;
        for (int i = 0; i < 9; i++) {
            if (inv.getStackInSlot(i) == null) return 36 + i;
        }
        for (int i = 9; i < 36; i++) {
            if (inv.getStackInSlot(i) == null) return i;
        }
        return -1;
    }
    private void giveItem() {
        if (this.currentitemstack == null || this.mc.thePlayer == null) {
            return;
        }
        int slot = findOpenSlot();
        if (slot == -1) {
            this.mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + I18n.format("gui.act.give.fail")));
            return;
        }
        try {
            this.mc.getNetHandler().addToSendQueue(new C10PacketCreativeInventoryAction(slot, this.currentitemstack));
            this.mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + I18n.format("gui.act.give.msg")));
        } catch (Exception e) {
            this.mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + I18n.format("gui.act.give.fail2")));
        }
    }
    private ItemStack parseStackFromField() {
        try {
            String raw = this.iteminputfield.getText().trim();
            if (raw.isEmpty()) return null;
            raw = convertColorCodesInNbt(raw);
            String[] parts = raw.split(" ", 4);
            if (parts.length < 1) return null;
            String itemarg = parts[0];
            int count = parts.length >= 2 ? Integer.parseInt(parts[1]) : 1;
            int meta = parts.length >= 3 ? Integer.parseInt(parts[2]) : 0;
            String nbtarg = parts.length >= 4 ? parts[3] : null;
            Item item = Item.itemRegistry.getObject(new ResourceLocation(itemarg));
            if (item == null) {
                try {
                    item = Item.getItemById(Integer.parseInt(itemarg));
                } catch (NumberFormatException ignored) {}
            }
            if (item == null) return null;
            ItemStack result = new ItemStack(item, Math.max(1, count), meta);
            if (nbtarg != null && !nbtarg.isEmpty()) {
                try {
                    NBTTagCompound tag = JsonToNBT.getTagFromJson(nbtarg);
                    result.setTagCompound(tag);
                } catch (NBTException e) {
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    private String buildGiveCommand(ItemStack stack) {
        ResourceLocation itemname = Item.itemRegistry.getNameForObject(stack.getItem());
        String namestr = itemname != null ? itemname.toString() : String.valueOf(Item.getIdFromItem(stack.getItem()));
        String base = namestr + " " + stack.stackSize + " " + stack.getItemDamage();
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || tag.getKeySet().isEmpty()) return base;
        return base + " " + buildNbtCompound(tag);
    }
    private String buildNbtCompound(NBTTagCompound compound) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (String key : compound.getKeySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(key).append(':');
            sb.append(serialiseNbt(compound.getTag(key)));
        }
        sb.append('}');
        return sb.toString();
    }
    private String serialiseNbt(net.minecraft.nbt.NBTBase tag) {
        if (tag instanceof NBTTagCompound) {
            return buildNbtCompound((NBTTagCompound) tag);
        } else if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) tag;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.tagCount(); i++) {
                if (i > 0) sb.append(',');
                sb.append(serialiseNbt(list.get(i)));
            }
            sb.append(']');
            return sb.toString();
        } else if (tag instanceof NBTTagString) {
            return "\"" + ((NBTTagString) tag).getString().replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        } else {
            return tag.toString();
        }
    }
    private String convertColorCodesInNbt(String input) {
        StringBuilder out = new StringBuilder(input.length());
        boolean inquotes = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                inquotes = !inquotes;
                out.append(c);
            } else if (inquotes && c == '&' && i + 1 < input.length()) {
                char next = input.charAt(i + 1);
                if ("0123456789abcdefklmnorABCDEFKLMNOR".indexOf(next) >= 0) {
                    out.append('\u00a7');
                } else {
                    out.append(c);
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
    private String convertSectionSignsToAmpersand(String input) {
        StringBuilder out = new StringBuilder(input.length());
        boolean inquotes = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                inquotes = !inquotes;
                out.append(c);
            } else if (inquotes && c == '\u00a7' && i + 1 < input.length()) {
                char next = input.charAt(i + 1);
                if ("0123456789abcdefklmnorABCDEFKLMNOR".indexOf(next) >= 0) {
                    out.append('&');
                } else {
                    out.append(c);
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
