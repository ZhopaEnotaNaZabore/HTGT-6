package com.mod.htgt6.common.mining;

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

/** Structural shell. Metadata 1 is the diagnostic/service variant allowed in the base. */
public final class BlockMiningUnitCasing extends Block {
    @SideOnly(Side.CLIENT) private IIcon diagnostic;

    public BlockMiningUnitCasing() {
        super(Material.iron);
        setBlockName("mining_unit_casing");
        setHardness(6.0F);
        setResistance(30.0F);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon("htgt6:multiblock_v2_mining_casing");
        diagnostic = register.registerIcon("htgt6:mining_unit_diagnostic");
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) { return metadata == 1 ? diagnostic : blockIcon; }
    @Override public int damageDropped(int metadata) { return metadata & 1; }

    @Override @SideOnly(Side.CLIENT) @SuppressWarnings("unchecked")
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
    }
}
