package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.IVillagePoiBlock120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** 1.7-compatible barrel inventory with village-loot and comparator support. */
public final class BlockVillageBarrel120 extends BlockContainer implements IVillagePoiBlock120 {
    public BlockVillageBarrel120() {
        super(Material.wood);
        setBlockName("village_pillage_barrel"); setHardness(2.5F);
        setResistance(Backport120Content.legacyResistance(2.5F));
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);
        setBlockTextureName("htgt6:village_pillage/barrel_side");
    }
    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TileEntityVillageBarrel120(); }
    @Override public String getPoiType(){return "fisherman";}
    @Override public int getRenderType() { return VillagePillageContent.getJsonRenderId(); }
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase placer,ItemStack stack){
        world.setBlockMetadataWithNotify(x,y,z,placer.rotationPitch>45?4:placer.rotationPitch< -45?5:MathHelper.floor_double(placer.rotationYaw*4F/360F+.5D)&3,2);
        VillagePoiData120.register(world,x,y,z,"fisherman");
    }
    @Override public void onBlockAdded(World world,int x,int y,int z){super.onBlockAdded(world,x,y,z);VillagePoiData120.register(world,x,y,z,"fisherman");}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hitX,float hitY,float hitZ){
        if(!world.isRemote&&world.getTileEntity(x,y,z) instanceof TileEntityChest)
            player.displayGUIChest((TileEntityChest)world.getTileEntity(x,y,z));
        return true;
    }
    @Override public void breakBlock(World world,int x,int y,int z,Block block,int metadata){
        VillagePoiData120.remove(world,x,y,z);net.minecraft.init.Blocks.chest.breakBlock(world,x,y,z,block,metadata);
    }
    @Override public boolean hasComparatorInputOverride(){return true;}
    @Override public int getComparatorInputOverride(World world,int x,int y,int z,int side){
        return Container.calcRedstoneFromInventory((TileEntityChest)world.getTileEntity(x,y,z));
    }
}
