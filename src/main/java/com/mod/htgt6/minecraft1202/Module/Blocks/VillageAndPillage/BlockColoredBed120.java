package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;

import java.util.Random;
import net.minecraft.block.BlockBed;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/** One block identity per 1.20 dye color while retaining the proven 1.7 sleep/respawn logic. */
public final class BlockColoredBed120 extends BlockBed {
    private final int color;

    public BlockColoredBed120(String name, int color) {
        super();
        this.color = color;
        setBlockName("village_pillage_" + name + "_bed");
        setBlockTextureName("htgt6:village_pillage/bed/" + name);
    }

    @Override public boolean hasTileEntity(int meta){return true;}
    @Override public net.minecraft.tileentity.TileEntity createTileEntity(World world,int meta){return new TileEntityColoredBed120();}
    @Override public int getRenderType(){return -1;}
    public int getColor() { return color; }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) {
        return isBlockHeadOfBed(metadata) ? null : Item.getItemFromBlock(this);
    }
    @Override public Item getItem(World world, int x, int y, int z) { return Item.getItemFromBlock(this); }
    @Override public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        VillagePoiData120.registerIfPoi(world, x, y, z);
    }
}
