package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

/** The Ancient City portal-frame block. It intentionally has no obtainable item drop. */
public final class BlockBackport120ReinforcedDeepslate extends Block {
    private IIcon top;
    private IIcon bottom;

    public BlockBackport120ReinforcedDeepslate() {
        super(Material.rock);
        setBlockName("backport_1_20.reinforced_deepslate");
        setHardness(55.0F);
        setResistance(Backport120Content.legacyResistance(1200.0F));
        setStepSound(soundTypeStone);
        setHarvestLevel("pickaxe", 3);
    }

    @Override public int quantityDropped(java.util.Random random) { return 0; }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon("htgt6:1_20_backport/reinforced_deepslate_side");
        top = register.registerIcon("htgt6:1_20_backport/reinforced_deepslate_top");
        bottom = register.registerIcon("htgt6:1_20_backport/reinforced_deepslate_bottom");
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) { return side == 1 ? top : side == 0 ? bottom : blockIcon; }
}
