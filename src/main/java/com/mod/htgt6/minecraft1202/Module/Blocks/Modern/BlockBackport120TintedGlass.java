package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/** Visually translucent but fully opaque to the legacy light engine. */
public final class BlockBackport120TintedGlass extends BlockBreakable {
    public BlockBackport120TintedGlass(){super("htgt6:1_20_backport/tinted_glass",Material.glass,false);
        setBlockName("backport_1_20.tinted_glass");setHardness(.3F);setResistance(Backport120Content.legacyResistance(.3F));
        setStepSound(soundTypeGlass);setLightOpacity(255);setCreativeTab(CreativeTabs.tabBlock);}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override public int getRenderBlockPass(){return 1;}@Override public boolean canRenderInPass(int pass){return pass==1;}
}
