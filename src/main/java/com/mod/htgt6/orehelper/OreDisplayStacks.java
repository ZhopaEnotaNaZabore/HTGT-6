package com.mod.htgt6.orehelper;

import gregapi.code.ItemStackContainer;
import gregapi.data.CS;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.Map;

/** GT6 ores are extended-metadata blocks; prefix unification can return no item. */
public final class OreDisplayStacks {
    private OreDisplayStacks() {}
    public static ItemStack resolve(int type, int material, ItemStack[] hosts) {
        if (material <= 0 || material >= 32768) return null;
        if (type == 3) return stack(CS.BlocksGT.oreBedrock, material);
        Map<ItemStackContainer, ?> registry = type == 2 ? CS.BlocksGT.stoneToSmallOres : CS.BlocksGT.stoneToNormalOres;
        // Layers have a specific host; generic replacement lists are not a preferred host.
        if (type == 1) for (ItemStack host : hosts) {
            if (host == null) continue;
            ItemStack ore = stack(registry.get(new ItemStackContainer(host)), material);
            if (ore != null) return ore;
        }
        ItemStack ore = stack(registry.get(new ItemStackContainer(Blocks.stone, 1, 0)), material);
        if (ore != null) return ore;
        ore = stack(type == 2 ? CS.BlocksGT.oreSmall : CS.BlocksGT.ore, material);
        if (ore != null) return ore;
        for (Object block : registry.values()) {
            ore = stack(block, material);
            if (ore != null) return ore;
        }
        return null;
    }
    private static ItemStack stack(Object value, int material) {
        if (!(value instanceof Block)) return null;
        Item item = Item.getItemFromBlock((Block) value);
        return item == null ? null : new ItemStack(item, 1, material);
    }
}
