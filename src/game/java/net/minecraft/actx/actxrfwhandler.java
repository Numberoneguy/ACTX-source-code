package net.minecraft.actx;
import java.util.Random;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
final class actxrfwhandler {
    private actxrfwhandler() {}
    static void handleRandomFireworks(EntityPlayerSP player) {
        if (!actxcommand.requireCreative(player)) return;
        Random rand = new Random();
        ItemStack stack = new ItemStack(Item.getItemById(401), 1);
        NBTTagCompound fireworks = new NBTTagCompound();
        fireworks.setByte("Flight", (byte) (1 + rand.nextInt(3)));
        NBTTagList explosions = new NBTTagList();
        int explosioncount = 1 + rand.nextInt(3);
        for (int e = 0; e < explosioncount; ++e) {
            NBTTagCompound explosion = new NBTTagCompound();
            explosion.setByte("Type", (byte) rand.nextInt(5));
            explosion.setBoolean("Trail", rand.nextBoolean());
            explosion.setBoolean("Flicker", rand.nextBoolean());
            explosion.setIntArray("Colors", randomFireworkColors(rand));
            if (rand.nextBoolean()) {
                explosion.setIntArray("FadeColors", randomFireworkColors(rand));
            }
            explosions.appendTag(explosion);
        }
        fireworks.setTag("Explosions", explosions);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("Fireworks", fireworks);
        stack.setTagCompound(tag);
        if (actxcommand.giveItem(player, stack)) {
            player.addChatMessage(actxcommand.createStyledMessage("cmd.act.rfw.success", EnumChatFormatting.GREEN));
        } else {
            player.addChatMessage(actxcommand.createStyledMessage("cmd.act.give.full", EnumChatFormatting.DARK_RED));
        }
    }
    private static int[] randomFireworkColors(Random rand) {
        int count = 1 + rand.nextInt(3);
        int[] colors = new int[count];
        for (int i = 0; i < count; ++i) {
            colors[i] = rand.nextInt(0x1000000);
        }
        return colors;
    }
}
