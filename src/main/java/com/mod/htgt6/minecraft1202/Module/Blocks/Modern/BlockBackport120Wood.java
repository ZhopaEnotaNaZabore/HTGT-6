package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

/** Data-driven modern log/wood implementation shared by cherry and mangrove species. */
public final class BlockBackport120Wood extends BlockRotatedPillar {
    private final String texture;
    private final boolean allBark;
    @SideOnly(Side.CLIENT) private IIcon sideIcon, topIcon;

    public BlockBackport120Wood(String name, String texture, boolean allBark) {
        super(com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherWoodMaterial116.forName(name));
        this.texture = texture;
        this.allBark = allBark;
        setBlockName("backport_1_20." + name);
        setHardness(2.0F);
        setResistance(Backport120Content.legacyResistance(2.0F));
        setStepSound(soundTypeWood);
        setHarvestLevel("axe", 0);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public int damageDropped(int metadata) { return 0; }
    @Override public boolean isWood(IBlockAccess world, int x, int y, int z) { return true; }
    @Override public boolean canSustainLeaves(IBlockAccess world, int x, int y, int z) { return true; }
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister register) {
        sideIcon = register.registerIcon("htgt6:1_20_backport/" + texture);
        topIcon = register.registerIcon("htgt6:1_20_backport/" + texture + "_top");
        blockIcon = sideIcon;
    }
    @SideOnly(Side.CLIENT) @Override protected IIcon getSideIcon(int metadata) { return sideIcon; }
    @SideOnly(Side.CLIENT) @Override protected IIcon getTopIcon(int metadata) {
        return allBark ? sideIcon : topIcon;
    }
}
