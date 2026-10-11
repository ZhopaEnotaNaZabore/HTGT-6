package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

/** Shared leaves implementation; species data selects texture and sapling/propagule drop. */
public final class BlockBackport120Leaves extends BlockLeaves {
    private static final int UNTINTED = 0xFFFFFF;
    private final String species;
    private final Block plantingItem;
    @SideOnly(Side.CLIENT) private IIcon icon;

    public BlockBackport120Leaves(String species, Block plantingItem) {
        this.species = species;
        this.plantingItem = plantingItem;
        setBlockName("backport_1_20." + species + "_leaves");
        setHardness(0.2F);
        setResistance(Backport120Content.legacyResistance(0.2F));
        setStepSound(soundTypeGrass);
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) {
        return Item.getItemFromBlock(plantingItem);
    }
    @Override protected int func_150123_b(int metadata) { return 20; }
    @Override public int damageDropped(int metadata) { return 0; }
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister register) {
        icon = register.registerIcon("htgt6:1_20_backport/" + species + "_leaves");
    }
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int side, int metadata) { return icon; }
    @SideOnly(Side.CLIENT) @Override public int getBlockColor() {
        return isCherry() ? UNTINTED : super.getBlockColor();
    }
    @SideOnly(Side.CLIENT) @Override public int getRenderColor(int metadata) {
        return isCherry() ? UNTINTED : super.getRenderColor(metadata);
    }
    @SideOnly(Side.CLIENT) @Override public int colorMultiplier(IBlockAccess world,int x,int y,int z) {
        return isCherry() ? UNTINTED : super.colorMultiplier(world,x,y,z);
    }
    @Override public String[] func_150125_e() { return new String[]{species}; }
    private boolean isCherry(){return "cherry".equals(species);}
}
