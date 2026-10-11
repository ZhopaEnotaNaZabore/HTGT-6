package com.mod.htgt6.common.mining;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

import java.util.List;

/** Open framebox; metadata 1 is the quarter-footprint cap used on layer 12. */
public final class BlockMiningUnitFrame extends Block {
    public BlockMiningUnitFrame() {
        super(Material.iron);
        setBlockName("mining_unit_frame");
        setHardness(5.0F);
        setResistance(20.0F);
        setStepSound(soundTypeMetal);
        setLightOpacity(0);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        // Native HTGT6 artwork; no textures are borrowed from GT5/GT6.
        blockIcon = register.registerIcon("htgt6:mining_unit_frame");
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return true; }
    @Override public int getRenderBlockPass() { return 1; }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        if (world.getBlockMetadata(x, y, z) == 1) setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F);
        else setBlockBounds(0, 0, 0, 1, 1, 1);
    }

    @Override public int damageDropped(int metadata) { return metadata & 1; }
    @Override @SideOnly(Side.CLIENT) @SuppressWarnings("unchecked")
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
    }
}
