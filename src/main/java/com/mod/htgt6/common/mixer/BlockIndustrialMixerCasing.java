package com.mod.htgt6.common.mixer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

/** Smooth structural skin with a separate reinforced edge texture. */
public final class BlockIndustrialMixerCasing extends Block {
    @SideOnly(Side.CLIENT) private IIcon solid;
    @SideOnly(Side.CLIENT) private IIcon rib;

    public BlockIndustrialMixerCasing() {
        super(Material.iron);
        setBlockName("industrial_mixer_casing");
        setHardness(7.0F);
        setResistance(35.0F);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        solid = register.registerIcon("htgt6:multiblock_v2_mixer_casing");
        rib = register.registerIcon("htgt6:multiblock_v2_mixer_rib");
        blockIcon = solid;
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) { return solid; }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return solid;
    }

}
