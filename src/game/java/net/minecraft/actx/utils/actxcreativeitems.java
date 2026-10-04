package net.minecraft.actx.utils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
public final class actxcreativeitems {
    private actxcreativeitems() {
    }
    private static final int[] unobtainable_block_ids = {
            8, 9, 10, 11,
            26, 43, 52, 55, 59, 60, 62, 63, 64, 68, 71, 74, 75,
            90, 93, 94, 99, 100, 104, 105, 115, 117, 118, 119, 127, 132,
            137, 140, 141, 142, 144, 149, 150, 166, 176, 177, 178,
    };
    private static final int[] unobtainable_item_ids = {
            383,
            422,
            387,
            403,
            358,
    };
    public static List<ItemStack> getAdvancedCreativeItems() {
        List<ItemStack> items = new ArrayList<ItemStack>();
        for (int id : unobtainable_block_ids) {
            Block block = Block.getBlockById(id);
            if (block != null) {
                Item blockitem = Item.getItemFromBlock(block);
                if (blockitem != null) {
                    items.add(new ItemStack(blockitem, 1, 0));
                }
            }
        }
        for (int id : unobtainable_item_ids) {
            Item item = Item.getItemById(id);
            if (item != null) {
                items.add(new ItemStack(item, 1, 0));
            }
        }
        Block block122 = Block.getBlockById(122);
        if (block122 != null) {
            Item item122 = Item.getItemFromBlock(block122);
            if (item122 != null) {
                items.add(new ItemStack(item122, 1, 0));
            }
        }
        Block stoneslabblock = Block.getBlockById(44);
        if (stoneslabblock != null) {
            Item slabitem = Item.getItemFromBlock(stoneslabblock);
            if (slabitem != null) {
                items.add(new ItemStack(slabitem, 1, 2));
            }
        }
        Item item401 = Item.getItemById(401);
        if (item401 != null) {
            items.add(new ItemStack(item401, 1, 0));
        }
        Item item373 = Item.getItemById(373);
        if (item373 != null) {
            items.add(new ItemStack(item373, 1, 7));
            items.add(new ItemStack(item373, 1, 15));
            items.add(new ItemStack(item373, 1, 31));
            items.add(new ItemStack(item373, 1, 39));
            items.add(new ItemStack(item373, 1, 47));
            items.add(new ItemStack(item373, 1, 55));
            items.add(new ItemStack(item373, 1, 63));
            items.add(new ItemStack(item373, 1, 87));
            items.add(new ItemStack(item373, 1, 84223));
            items.add(new ItemStack(item373, 1, 84224));
            items.add(new ItemStack(item373, 1, 84231));
            items.add(new ItemStack(item373, 1, 84239));
            items.add(new ItemStack(item373, 1, 84247));
            items.add(new ItemStack(item373, 1, 84255));
            items.add(new ItemStack(item373, 1, 84263));
            items.add(new ItemStack(item373, 1, 84271));
            items.add(new ItemStack(item373, 1, 84279));
        }
        return items;
    }
    public static List<ItemStack> getCustomBlockItems() {
        List<ItemStack> items = new ArrayList<ItemStack>();
        Block customblock198 = Block.getBlockById(198);
        if (customblock198 != null) {
            Item customblockitem = Item.getItemFromBlock(customblock198);
            if (customblockitem != null) {
                items.add(new ItemStack(customblockitem, 1, 0));
            }
        }
        Block commandblock = Block.getBlockById(137);
        if (commandblock != null) {
            Item commandblockitem = Item.getItemFromBlock(commandblock);
            if (commandblockitem != null) {
                items.add(new ItemStack(commandblockitem, 1, 1));
                items.add(new ItemStack(commandblockitem, 1, 2));
            }
        }
        return items;
    }
}
