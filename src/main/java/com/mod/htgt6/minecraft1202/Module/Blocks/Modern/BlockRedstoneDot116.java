package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.RedstoneWire116;
public final class BlockRedstoneDot116 extends BlockRedstoneWire {
    public BlockRedstoneDot116(){setBlockName("backport_1_20.redstone_dot");}
    private boolean reconnect(World w,int x,int y,int z){if(w.isRemote)return false;for(int side:RedstoneWire116.raw(w,x,y,z))if(side!=0){w.setBlock(x,y,z,Blocks.redstone_wire,w.getBlockMetadata(x,y,z),3);return true;}return false;}
    @Override public void onBlockAdded(World w,int x,int y,int z){if(!reconnect(w,x,y,z))super.onBlockAdded(w,x,y,z);}
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block block){if(!reconnect(w,x,y,z))super.onNeighborBlockChange(w,x,y,z,block);}
    @Override public int isProvidingWeakPower(IBlockAccess w,int x,int y,int z,int side){return RedstoneWire116.power(this,w,x,y,z,side);}
    @Override public int getRenderType(){return RedstoneWire116.render(this);}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float hx,float hy,float hz){return RedstoneWire116.toggle(this,w,x,y,z,p,side,hx,hy,hz);}
}
