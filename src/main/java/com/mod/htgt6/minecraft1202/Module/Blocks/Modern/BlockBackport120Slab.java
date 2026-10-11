package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/** Single/double slab pair retaining the 1.7.10 placement and merge contract. */
public class BlockBackport120Slab extends BlockSlab {
    private final String family;
    private BlockBackport120Slab single;

    public BlockBackport120Slab(boolean full, String family, String texture, Block source,
                         float modernResistance) {
        this(full, family, texture, source, modernResistance, Material.rock,
                Block.soundTypePiston, "pickaxe", 0);
    }

    public BlockBackport120Slab(boolean full, String family, String texture, Block source,
                         float modernResistance, Material material, Block.SoundType sound,
                         String harvestTool, int harvestLevel) {
        super(full, material);
        this.family = family;
        setBlockName("backport_1_20." + (full ? "double_" : "") + family + "_slab");
        setBlockTextureName("htgt6:1_20_backport/" + texture);
        setHardness(source.getBlockHardness(null, 0, 0, 0));
        setResistance(Backport120Content.legacyResistance(modernResistance));
        setStepSound(sound);
        if (harvestTool != null) setHarvestLevel(harvestTool, harvestLevel);
        if (!full) setCreativeTab(CreativeTabs.tabBlock);
        useNeighborBrightness = !full;
    }

    public void setSingle(BlockBackport120Slab value) { single = value; }

    @Override public Item getItemDropped(int metadata, java.util.Random random, int fortune) {
        return Item.getItemFromBlock(single == null ? this : single);
    }

    @Override protected net.minecraft.item.ItemStack createStackedBlock(int metadata) {
        return new net.minecraft.item.ItemStack(single == null ? this : single,
                field_150004_a ? 2 : 1, 0);
    }

    @Override public String func_150002_b(int metadata) {
        return "tile.backport_1_20." + family + "_slab";
    }

    @Override public int getRenderType() {
        return Backport120Content.getJsonRenderId();
    }

    @Override public Item getItem(World world, int x, int y, int z) {
        return Item.getItemFromBlock(single == null ? this : single);
    }
}
