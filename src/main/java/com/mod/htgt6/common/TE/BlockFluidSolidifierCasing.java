package com.mod.htgt6.common.TE;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;

/** Solid metallic shell used by both casting sections and their structural ribs. */
public final class BlockFluidSolidifierCasing extends Block {
    public BlockFluidSolidifierCasing() {
        super(Material.iron);
        setBlockName("fluid_solidifier_casing");
        setHardness(7F);
        setResistance(35F);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }
    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon("htgt6:multiblock_v2_foundry_casing");
    }
}
