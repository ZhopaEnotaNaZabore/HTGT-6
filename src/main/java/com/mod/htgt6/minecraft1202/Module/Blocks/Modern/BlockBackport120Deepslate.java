package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.Random;

/** Axis-aware deepslate. Normal harvesting drops cobbled deepslate; Silk Touch keeps deepslate. */
public final class BlockBackport120Deepslate extends BlockRotatedPillar {
    private IIcon side;

    public BlockBackport120Deepslate() {
        super(Material.rock);
        setBlockName("backport_1_20.deepslate");
        setHardness(3.0F);
        setResistance(Backport120Content.legacyResistance(6.0F));
        setStepSound(soundTypePiston);
        setHarvestLevel("pickaxe", 0);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public Item getItemDropped(int metadata, Random random, int fortune) {
        return Item.getItemFromBlock(Backport120Content.cobbledDeepslate);
    }

    @Override
    public int damageDropped(int metadata) {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        side = register.registerIcon("htgt6:1_20_backport/deepslate");
        field_150164_N = register.registerIcon("htgt6:1_20_backport/deepslate_top");
        blockIcon = side;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected IIcon getSideIcon(int metadata) {
        return side;
    }

    @Override
    public int getRenderType() {
        return Backport120Content.getJsonRenderId();
    }
}
