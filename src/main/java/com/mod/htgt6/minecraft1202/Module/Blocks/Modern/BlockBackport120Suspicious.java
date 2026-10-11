package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.creativetab.CreativeTabs;

import java.util.Random;

/** Brushable falling block; any fall destroys hidden loot and settles as its ordinary counterpart. */
public final class BlockBackport120Suspicious extends BlockContainer {
    private final Block settledBlock;
    public BlockBackport120Suspicious(String name,Block settledBlock){super(Material.sand);this.settledBlock=settledBlock;
        setBlockName("backport_1_20."+name);setBlockTextureName("htgt6:1_20_backport/"+name+"_0");setHardness(.25F);
        setResistance(Backport120Content.legacyResistance(.25F));setStepSound(settledBlock==net.minecraft.init.Blocks.sand?soundTypeSand:soundTypeGravel);
        setHarvestLevel("shovel",0);setCreativeTab(CreativeTabs.tabBlock);}
    public Block getSettledBlock(){return settledBlock;}
    @Override public TileEntity createNewTileEntity(World world,int metadata){return new TileEntityBackport120Brushable();}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override public Item getItemDropped(int metadata,Random random,int fortune){return null;}
    @Override public void onBlockAdded(World w,int x,int y,int z){w.scheduleBlockUpdate(x,y,z,this,2);}
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block n){w.scheduleBlockUpdate(x,y,z,this,2);}
    @Override public void updateTick(World w,int x,int y,int z,Random r){if(w.isRemote||y<=0||!fallThrough(w.getBlock(x,y-1,z)))return;
        int landing=y-1;while(landing>0&&fallThrough(w.getBlock(x,landing-1,z)))landing--;w.setBlockToAir(x,y,z);
        if(landing>0)w.setBlock(x,landing,z,settledBlock,0,3);}
    private static boolean fallThrough(Block b){return b==net.minecraft.init.Blocks.air||b.getMaterial().isLiquid()||b.getMaterial().isReplaceable();}
}
