package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;

import java.util.Random;

public final class BlockBackport120AzaleaLeaves extends BlockLeaves {
    private final boolean flowering;
    @SideOnly(Side.CLIENT) private IIcon icon;

    public BlockBackport120AzaleaLeaves(boolean flowering) {
        this.flowering = flowering;
        setBlockName("backport_1_20." + (flowering ? "flowering_azalea_leaves" : "azalea_leaves"));
        setHardness(0.2F);
        setResistance(Backport120Content.legacyResistance(0.2F));
        setStepSound(soundTypeGrass);
        setCreativeTab(CreativeTabs.tabDecorations);
    }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) {
        return Item.getItemFromBlock(flowering ? Backport120Content.floweringAzalea : Backport120Content.azalea);
    }
    @Override public int damageDropped(int metadata) { return 0; }
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister register) {
        icon = register.registerIcon("htgt6:1_20_backport/" + (flowering ? "flowering_azalea_leaves" : "azalea_leaves"));
    }
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int side, int metadata) { return icon; }
    @Override public String[] func_150125_e() { return new String[]{flowering ? "flowering_azalea" : "azalea"}; }
}
