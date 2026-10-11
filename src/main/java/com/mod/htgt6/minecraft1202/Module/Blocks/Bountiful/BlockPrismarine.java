package com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public final class BlockPrismarine extends Block {
    private static final String[] NAMES = {"prismarine", "prismarine_bricks", "dark_prismarine"};
    private IIcon[] icons;

    public BlockPrismarine() {
        super(Material.rock);
        setBlockName("prismarine"); setHardness(1.5F); setResistance(10.0F);
        setCreativeTab(CreativeTabs.tabBlock);
    }
    @Override public int damageDropped(int metadata) { return metadata; }
    @Override public IIcon getIcon(int side, int metadata) { return icons[Math.max(0, Math.min(2, metadata))]; }
    @Override public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int metadata = 0; metadata < 3; metadata++) list.add(new ItemStack(item, 1, metadata));
    }
    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister register) {
        icons = new IIcon[NAMES.length];
        for (int i = 0; i < NAMES.length; i++) icons[i] = register.registerIcon("htgt6:bountiful/" + NAMES[i]);
    }
}
