package net.minecraft.actx.server;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public final class actxcooldown {
    private actxcooldown() {
    }

    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");

    public static int mode = 1;
    public static int syncGrace = 8;

    private static int ticks = 1000;
    private static int grace = 0;
    private static ItemStack lastHeld = null;
    private static Object lastWorld = null;
    private static boolean viaTitleMode = false;

    public static boolean active() {
        Minecraft mc = Minecraft.getMinecraft();
        return mode != 0 && mc.theWorld != null && mc.thePlayer != null && !mc.isIntegratedServerRunning();
    }

    public static void reset() {
        ticks = 0;
        grace = syncGrace;
    }

    private static boolean sameItem(ItemStack a, ItemStack b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a.getItem() != b.getItem()) {
            return false;
        }
        return a.isItemStackDamageable() || a.getMetadata() == b.getMetadata();
    }

    public static void tick(EntityPlayer player) {
        if (player.worldObj != lastWorld) {
            lastWorld = player.worldObj;
            viaTitleMode = false;
        }
        if (ticks < 1000) {
            ticks++;
        }
        if (grace > 0) {
            grace--;
        }
        ItemStack held = player.getHeldItem();
        if (!sameItem(held, lastHeld)) {
            ticks = 0;
            grace = syncGrace;
        }
        lastHeld = held == null ? null : held.copy();
    }

    private static double defaultSpeed(ItemStack stack) {
        if (stack == null) {
            return 4.0D;
        }
        Item item = stack.getItem();
        if (item instanceof ItemSword) {
            return 1.6D;
        }
        if (item instanceof ItemPickaxe) {
            return 1.2D;
        }
        if (item instanceof ItemSpade) {
            return 1.0D;
        }
        if (item instanceof ItemAxe) {
            String material = ((ItemTool) item).getToolMaterialName();
            if ("IRON".equals(material)) {
                return 0.9D;
            }
            if ("EMERALD".equals(material) || "GOLD".equals(material)) {
                return 1.0D;
            }
            return 0.8D;
        }
        if (item instanceof ItemHoe) {
            String material = ((ItemHoe) item).getMaterialName();
            if ("STONE".equals(material)) {
                return 2.0D;
            }
            if ("IRON".equals(material)) {
                return 3.0D;
            }
            if ("EMERALD".equals(material)) {
                return 4.0D;
            }
            return 1.0D;
        }
        return 4.0D;
    }

    private static double attackSpeed(EntityPlayer player, ItemStack stack) {
        double add = 0.0D;
        double mul1 = 0.0D;
        double mul2 = 1.0D;
        boolean custom = false;
        if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("AttributeModifiers", 9)) {
            custom = true;
            NBTTagList list = stack.getTagCompound().getTagList("AttributeModifiers", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound modifier = list.getCompoundTagAt(i);
                if (!"generic.attackSpeed".equals(modifier.getString("AttributeName"))) {
                    continue;
                }
                if (modifier.hasKey("Slot", 8) && !"mainhand".equals(modifier.getString("Slot"))) {
                    continue;
                }
                double amount = modifier.getDouble("Amount");
                int operation = modifier.getInteger("Operation");
                if (operation == 0) {
                    add += amount;
                } else if (operation == 1) {
                    mul1 += amount;
                } else if (operation == 2) {
                    mul2 *= 1.0D + amount;
                }
            }
        }
        if (!custom) {
            add += defaultSpeed(stack) - 4.0D;
        }
        double base = 4.0D + add;
        double value = base + base * mul1;
        value *= mul2;
        if (player != null) {
            PotionEffect haste = player.getActivePotionEffect(Potion.digSpeed);
            if (haste != null) {
                value *= 1.0D + 0.10000000149011612D * (haste.getAmplifier() + 1);
            }
            PotionEffect fatigue = player.getActivePotionEffect(Potion.digSlowdown);
            if (fatigue != null) {
                value *= 1.0D - 0.10000000149011612D * (fatigue.getAmplifier() + 1);
            }
        }
        if (value > 1024.0D) {
            value = 1024.0D;
        }
        if (value < 0.0001D) {
            value = 0.0001D;
        }
        return value;
    }

    public static float getPeriod() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack held = player == null ? null : player.getHeldItem();
        return (float) (1.0D / attackSpeed(player, held) * 20.0D);
    }

    public static float getStrength(float adjustTicks) {
        return MathHelper.clamp_float(((float) ticks + adjustTicks) / getPeriod(), 0.0F, 1.0F);
    }

    public static void renderCrosshair(Gui gui, int width, int height) {
        if (mode != 1 || !active()) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.thirdPersonView != 0) {
            return;
        }
        float strength = getStrength(0.0F);
        boolean ready = false;
        if (mc.pointedEntity != null && mc.pointedEntity instanceof EntityLivingBase && strength >= 1.0F) {
            ready = getPeriod() > 5.0F && mc.pointedEntity.isEntityAlive();
        }
        int x = width / 2 - 8;
        int y = height / 2 - 7 + 16;
        mc.getTextureManager().bindTexture(ICONS);
        if (ready) {
            gui.drawTexturedModalRect(x, y, 68, 94, 16, 16);
        } else if (strength < 1.0F) {
            int barWidth = (int) (strength * 17.0F);
            gui.drawTexturedModalRect(x, y, 36, 94, 16, 4);
            gui.drawTexturedModalRect(x, y, 52, 94, barWidth, 4);
        }
    }

    public static void renderHotbar(Gui gui, ScaledResolution sr) {
        if (mode != 2 || !active()) {
            return;
        }
        float strength = getStrength(0.0F);
        if (strength >= 1.0F) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        int x = sr.getScaledWidth() / 2 - 91 - 22;
        int y = sr.getScaledHeight() - 20;
        int filled = (int) (strength * 19.0F);
        mc.getTextureManager().bindTexture(ICONS);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        gui.drawTexturedModalRect(x, y, 0, 94, 18, 18);
        gui.drawTexturedModalRect(x, y + 18 - filled, 18, 94 + 18 - filled, 18, filled);
        GlStateManager.disableBlend();
    }

    public static boolean isViaIndicator(String text) {
        if (text == null) {
            return false;
        }
        boolean any = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00a7') {
                i++;
                continue;
            }
            if (c == ' ') {
                continue;
            }
            if (c == '\u25A0' || c == '\u25A1' || c == '\u25AA' || c == '\u25AB' || c == '\u02D9') {
                any = true;
                continue;
            }
            return false;
        }
        return any;
    }

    private static void syncFromVia(String text) {
        int[] counts = new int[2];
        int segment = -1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00a7') {
                segment++;
                i++;
                continue;
            }
            if (c == ' ') {
                continue;
            }
            if (segment == 0 || segment == 1) {
                counts[segment]++;
            }
        }
        int total = counts[0] + counts[1];
        if (total <= 0 || grace > 0) {
            return;
        }
        float low = (float) counts[0] / (float) total;
        float high = counts[0] >= total ? 1.0F : low + 1.0F / (float) total - 0.001F;
        float current = getStrength(0.0F);
        float target = current < low ? low : (current > high ? high : current);
        if (target != current) {
            ticks = Math.round(target * getPeriod());
        }
    }

    public static boolean suppressVia(String text) {
        if (!active() || !isViaIndicator(text)) {
            return false;
        }
        syncFromVia(text);
        return true;
    }

    public static boolean suppressViaTitle(String type, String text, int fadeIn, int stay, int fadeOut) {
        if (!active()) {
            return false;
        }
        if ("SUBTITLE".equals(type) && isViaIndicator(text)) {
            viaTitleMode = true;
            syncFromVia(text);
            return true;
        }
        if (!viaTitleMode) {
            return false;
        }
        if ("TITLE".equals(type) && (text == null || text.trim().isEmpty())) {
            return true;
        }
        if ("TIMES".equals(type) && fadeIn == 0 && stay == 2 && fadeOut == 5) {
            return true;
        }
        return "CLEAR".equals(type);
    }
}