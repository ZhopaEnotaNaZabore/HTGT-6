package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.util.List;

/** Sixteen metadata colours of hardened concrete. */
public final class BlockBackport120ColoredConcrete extends Block {
    @SideOnly(Side.CLIENT) private IIcon[] icons;

    public BlockBackport120ColoredConcrete() {
        super(Material.rock);
        setBlockName("backport_1_20.concrete");
        setHardness(1.8F);
        setResistance(Backport120Content.legacyResistance(1.8F));
        setStepSound(soundTypeStone);
        setHarvestLevel("pickaxe", 0);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister register) {
        icons = new IIcon[Backport120Content.TERRACOTTA_COLORS.length];
        for (int i = 0; i < icons.length; i++) icons[i] = register.registerIcon(
                "htgt6:1_20_backport/" + Backport120Content.TERRACOTTA_COLORS[i] + "_concrete");
        blockIcon = icons[0];
    }

    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int side, int metadata) {
        return icons[metadata & 15];
    }
    @Override public int damageDropped(int metadata) { return metadata & 15; }
    @SuppressWarnings({"rawtypes", "unchecked"})
    @SideOnly(Side.CLIENT) @Override public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < 16; i++) list.add(new ItemStack(item, 1, i));
    }
}
