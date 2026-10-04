package net.minecraft.actx;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
public final class actxcreativetabs {
    private actxcreativetabs() {
    }
    public static final String advanced_title_key = "itemGroup.act";
    public static final String saved_items_title_key = "key.actx.saveditems";
    public static final String custom_blocks_title_key = "itemGroup.actx.customblocks";
    public static ItemStack getAdvancedTabIcon() {
        return new ItemStack(Blocks.command_block, 1, 0);
    }
    public static ItemStack getCustomBlocksTabIcon() {
        ItemStack stack = new ItemStack(Blocks.command_block, 1, 2);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("ench", new NBTTagList());
        stack.setTagCompound(tag);
        return stack;
    }
    public static ItemStack getSavedItemsTabIcon() {
        return new ItemStack(Items.map, 1, 0);
    }
}
