package com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/** 1.20.2 sea-lantern hardness, light and crystal drop contract. */
public final class BlockBountifulSeaLantern extends Block {
    public BlockBountifulSeaLantern(){super(Material.glass);setBlockName("sea_lantern");
        setBlockTextureName("htgt6:bountiful/sea_lantern");setHardness(.3F);
        setResistance(Backport120Content.legacyResistance(.3F));setLightLevel(1.0F);
        setStepSound(soundTypeGlass);setCreativeTab(CreativeTabs.tabBlock);}
    @Override public Item getItemDropped(int metadata,Random random,int fortune){return BountifulContent.prismarineCrystals;}
    @Override public int quantityDropped(Random random){return 2+random.nextInt(2);}
    @Override public int quantityDroppedWithBonus(int fortune,Random random){
        return Math.min(5,quantityDropped(random)+random.nextInt(Math.max(1,fortune+1)));}
    @Override protected boolean canSilkHarvest(){return true;}
}
