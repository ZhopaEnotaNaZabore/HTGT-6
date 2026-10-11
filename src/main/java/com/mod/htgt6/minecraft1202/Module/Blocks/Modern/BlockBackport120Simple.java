package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/** Simple full-cube block whose physical values mirror its Minecraft 1.20.2 counterpart. */
public class BlockBackport120Simple extends Block {
    public BlockBackport120Simple(Backport120BlockStat stat,String name){
        super(stat.material);
        setBlockName("backport_1_20."+name);
        setBlockTextureName("htgt6:1_20_backport/"+name);
        stat.apply(this);
    }
    public BlockBackport120Simple(Material material, String name, float hardness, float resistance,
                           SoundType sound, String harvestTool, int harvestLevel) {
        super(material);
        setBlockName("backport_1_20." + name);
        setBlockTextureName("htgt6:1_20_backport/" + name);
        setHardness(hardness);
        setResistance(Backport120Content.legacyResistance(resistance));
        setStepSound(sound);
        setHarvestLevel(harvestTool, harvestLevel);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public int getRenderType() {
        return Backport120Content.getJsonRenderId();
    }
}
