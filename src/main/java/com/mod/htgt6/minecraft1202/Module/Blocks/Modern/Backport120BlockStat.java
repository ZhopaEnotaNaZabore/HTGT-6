package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/** Immutable physical/stat profile applied to reusable 1.7 block shells. */
public final class Backport120BlockStat {
    public static final Backport120BlockStat WOOD=new Backport120BlockStat(
            Material.wood,2.0F,3.0F,Block.soundTypeWood,"axe",0,CreativeTabs.tabBlock);
    public final Material material;
    public final float hardness,resistance;
    public final Block.SoundType sound;
    public final String harvestTool;
    public final int harvestLevel;
    public final CreativeTabs creativeTab;
    public Backport120BlockStat(Material material,float hardness,float resistance,Block.SoundType sound,
                                String harvestTool,int harvestLevel,CreativeTabs creativeTab){
        this.material=material;this.hardness=hardness;this.resistance=resistance;this.sound=sound;
        this.harvestTool=harvestTool;this.harvestLevel=harvestLevel;this.creativeTab=creativeTab;
    }
    public <T extends Block>T apply(T block){
        block.setHardness(hardness);block.setResistance(Backport120Content.legacyResistance(resistance));
        block.setStepSound(sound);if(harvestTool!=null)block.setHarvestLevel(harvestTool,harvestLevel);
        block.setCreativeTab(creativeTab);return block;
    }
}
