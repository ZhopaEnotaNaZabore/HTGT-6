package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class BlockBackport120GildedBlackstone extends BlockBackport120EtFGapSimple {
    public BlockBackport120GildedBlackstone() {
        super(Material.rock, "gilded_blackstone", "gilded_blackstone", null, 1.5F, 6.0F,
                soundTypePiston, "pickaxe", 0);
    }
    /** Zero means the block itself; Fortune affects chance, never the nugget count. */
    public static int nuggetCount(Random random, int fortune) {
        float chance = new float[]{0.1F, 0.14285715F, 0.25F, 1.0F}[Math.max(0, Math.min(3, fortune))];
        return random.nextFloat() < chance ? 2 + random.nextInt(4) : 0;
    }
    @Override public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        int count = nuggetCount(world.rand, fortune);
        drops.add(count == 0 ? new ItemStack(this, 1, 0) : new ItemStack(Items.gold_nugget, count));
        return drops;
    }
    // Native Block.harvestBlock selects its Silk Touch branch before getDrops; retain that path.
}
