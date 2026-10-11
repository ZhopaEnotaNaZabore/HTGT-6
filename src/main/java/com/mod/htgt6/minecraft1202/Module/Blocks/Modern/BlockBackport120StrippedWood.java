package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

/**
 * Native 1.7 representation of the 1.13 stripped-log and stripped-wood families.
 * Species occupy the vanilla low metadata bits and the pillar axis remains in bits 2..3.
 */
public final class BlockBackport120StrippedWood extends BlockRotatedPillar {
    private final String[] species;
    private final boolean allBark;
    @SideOnly(Side.CLIENT) private IIcon[] sideIcons;
    @SideOnly(Side.CLIENT) private IIcon[] topIcons;

    public BlockBackport120StrippedWood(String name, String[] species, boolean allBark) {
        super(Material.wood);
        this.species = species;
        this.allBark = allBark;
        setBlockName("backport_1_20." + name);
        setHardness(2.0F);
        setResistance(Backport120Content.legacyResistance(2.0F));
        setStepSound(soundTypeWood);
        setHarvestLevel("axe", 0);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override public int getRenderType() { return 31; }
    @Override public int damageDropped(int metadata) { return metadata & 3; }

    @Override @SuppressWarnings({"rawtypes", "unchecked"})
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int speciesIndex = 0; speciesIndex < species.length; speciesIndex++)
            list.add(new ItemStack(item, 1, speciesIndex));
    }

    @SideOnly(Side.CLIENT) @Override
    public void registerBlockIcons(IIconRegister register) {
        sideIcons = new IIcon[species.length];
        topIcons = new IIcon[species.length];
        for (int i = 0; i < species.length; i++) {
            String base = "htgt6:1_20_backport/stripped_" + species[i] + "_log";
            sideIcons[i] = register.registerIcon(base);
            topIcons[i] = register.registerIcon(base + "_top");
        }
        blockIcon = sideIcons[0];
    }

    @SideOnly(Side.CLIENT) @Override protected IIcon getSideIcon(int metadata) {
        return sideIcons[species(metadata)];
    }

    @SideOnly(Side.CLIENT) @Override protected IIcon getTopIcon(int metadata) {
        int speciesIndex = species(metadata);
        return allBark ? sideIcons[speciesIndex] : topIcons[speciesIndex];
    }

    private int species(int metadata) {
        int value = metadata & 3;
        return value < species.length ? value : 0;
    }
}
