package com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn;

import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;

/** Axis-aware bone block using the 1.7 rotated-pillar metadata contract. */
public final class BlockFrostburnBone extends BlockRotatedPillar {
    private IIcon side;

    public BlockFrostburnBone() {
        super(Material.rock);
        setBlockName("bone_block");
        setHardness(2.0F);
        setResistance(Backport120Content.legacyResistance(2.0F));
        setStepSound(new FrostburnSound120("bone_block"));
        setHarvestLevel("pickaxe", 0);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        side = register.registerIcon("htgt6:frostburn/bone_block_side");
        field_150164_N = register.registerIcon("htgt6:frostburn/bone_block_top");
        blockIcon = side;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected IIcon getSideIcon(int metadata) {
        return side;
    }

    @Override public int getRenderType() { return FrostburnContent.getJsonRenderId(); }
}
