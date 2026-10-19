package net.minecraft.actx.creative;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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
        return new ItemStack(Blocks.what_the_fuck, 1, 0);
    }
    public static ItemStack getSavedItemsTabIcon() {
        return new ItemStack(Items.map, 1, 0);
    }
}
