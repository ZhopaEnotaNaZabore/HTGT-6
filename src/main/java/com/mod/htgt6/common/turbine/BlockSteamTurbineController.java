package com.mod.htgt6.common.turbine;

import com.cleanroommc.modularui.manager.GuiInfos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public final class BlockSteamTurbineController extends BlockContainer {
    public BlockSteamTurbineController(){super(Material.iron);setBlockName("steam_turbine");setBlockTextureName("htgt6:hullUlv");setHardness(7);setResistance(35);setCreativeTab(CreativeTabs.tabRedstone);}
    @Override public TileEntity createNewTileEntity(World world,int meta){return new MultiTileSteamTurbine();}
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase entity,ItemStack stack){
        int dir=MathHelper.floor_double(entity.rotationYaw*4/360F+.5F)&3;
        world.setBlockMetadataWithNotify(x,y,z,new int[]{2,5,3,4}[dir],2);
    }
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){
        if(player.isSneaking())return false;
        if(!world.isRemote)GuiInfos.TILE_ENTITY.open(player,world,x,y,z);
        return true;
    }
    @Override public void breakBlock(World world,int x,int y,int z,Block block,int meta){
        TileEntity tile=world.getTileEntity(x,y,z);
        if(!world.isRemote && tile instanceof MultiTileSteamTurbine){
            MultiTileSteamTurbine turbine=(MultiTileSteamTurbine)tile;
            turbine.detachGateways();
            for(ItemStack stack:turbine.inventory) if(stack!=null)
                world.spawnEntityInWorld(new net.minecraft.entity.item.EntityItem(world,x+.5,y+.5,z+.5,stack.copy()));
        }
        super.breakBlock(world,x,y,z,block,meta);
    }
}
