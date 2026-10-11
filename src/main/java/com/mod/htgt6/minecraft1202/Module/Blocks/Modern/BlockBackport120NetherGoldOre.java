package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import java.util.Random;

/** Nether gold ore using the modern gold-nugget drop. */
public final class BlockBackport120NetherGoldOre extends BlockBackport120EtFGapSimple {
    public BlockBackport120NetherGoldOre(){super(Material.rock,"nether_gold_ore","nether_gold_ore",null,
            3.0F,3.0F,Block.soundTypePiston,"pickaxe",0);}
    @Override public Item getItemDropped(int metadata,Random random,int fortune){return Items.gold_nugget;}
    @Override public int quantityDropped(Random random){return 2+random.nextInt(5);}
    @Override public int quantityDroppedWithBonus(int fortune,Random random){
        int multiplier=fortune>0?Math.max(0,random.nextInt(fortune+2)-1)+1:1;
        return quantityDropped(random)*multiplier;
    }
    @Override public int getExpDrop(net.minecraft.world.IBlockAccess world,int metadata,int fortune){return world instanceof net.minecraft.world.World?((net.minecraft.world.World)world).rand.nextInt(2):0;}
}
