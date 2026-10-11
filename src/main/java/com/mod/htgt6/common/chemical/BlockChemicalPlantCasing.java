package com.mod.htgt6.common.chemical;

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

/** Two Chemical Plant structural casings: shell and thermochemical process deck. */
public final class BlockChemicalPlantCasing extends Block {
    public static final int STANDARD = 0;
    public static final int THERMOCHEMICAL = 1;

    @SideOnly(Side.CLIENT) private IIcon thermochemical;

    public BlockChemicalPlantCasing() {
        super(Material.iron);
        setBlockName("chemical_plant_casing");
        setHardness(7.0F);
        setResistance(35.0F);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon("htgt6:multiblock_v2_chemical_casing");
        thermochemical = register.registerIcon("htgt6:multiblock_v2_chemical_inner");
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        return metadata == THERMOCHEMICAL ? thermochemical : blockIcon;
    }

    @Override public int damageDropped(int metadata) { return metadata == THERMOCHEMICAL ? 1 : 0; }

    @Override @SideOnly(Side.CLIENT) @SuppressWarnings("unchecked")
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, STANDARD));
        list.add(new ItemStack(item, 1, THERMOCHEMICAL));
    }
}
