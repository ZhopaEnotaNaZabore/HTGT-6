package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

/** age bit 0, leaves bits 1..2 (none/small/large), terminal stage bit 3. */
public final class BlockBackport120Bamboo extends Block implements IGrowable {
    public BlockBackport120Bamboo(){super(Material.wood);setBlockName("backport_1_20.bamboo");setHardness(1);setResistance(Backport120Content.legacyResistance(1.0F));
        setBlockTextureName("htgt6:1_20_backport/bamboo");setStepSound(soundTypeGrass);setTickRandomly(true);setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(6.5F/16,0,6.5F/16,9.5F/16,1,9.5F/16);}
    static int state(int age,int leaves,int stage){return(age&1)|((leaves&3)<<1)|((stage&1)<<3);}
    static int age(int m){return m&1;}static int leaves(int m){return(m>>>1)&3;}static int stage(int m){return(m>>>3)&1;}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override public boolean isOpaqueCube(){return false;}@Override public boolean renderAsNormalBlock(){return false;}
    @Override public Item getItemDropped(int m,Random r,int f){return Item.getItemFromBlock(Backport120Content.bamboo);}
    @Override public boolean canPlaceBlockAt(World w,int x,int y,int z){Block b=w.getBlock(x,y-1,z);return b==this||b==Backport120Content.bambooSapling
            ||b==Blocks.grass||b==Blocks.dirt||b==Blocks.sand||b==Blocks.gravel||b==Backport120Content.mossBlock;}
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block n){if(!canPlaceBlockAt(w,x,y,z)){dropBlockAsItem(w,x,y,z,0,0);w.setBlockToAir(x,y,z);}}
    @Override public void updateTick(World w,int x,int y,int z,Random r){
        if(!w.isRemote&&w.getBlockLightValue(x,y+1,z)>=9&&r.nextInt(3)==0)grow(w,r,x,y,z);
    }
    private int top(World w,int x,int y,int z){int limit=Math.min(w.getHeight()-1,y+16);while(y<limit&&w.getBlock(x,y+1,z)==this)y++;return y;}
    private int height(World w,int x,int y,int z){int count=1;while(count<16&&y-count>=0&&w.getBlock(x,y-count,z)==this)count++;return count;}
    private boolean grow(World w,Random r,int x,int y,int z){
        int old=w.getBlockMetadata(x,y,z),height=height(w,x,y,z);
        if(w.isRemote||w.getBlock(x,y,z)!=this||stage(old)!=0||height>=16||y+1>=w.getHeight()||!w.isAirBlock(x,y+1,z))return false;
        boolean below=w.getBlock(x,y-1,z)==this,twoBelow=w.getBlock(x,y-2,z)==this;
        int belowMeta=w.getBlockMetadata(x,y-1,z),twoMeta=w.getBlockMetadata(x,y-2,z);
        int newLeaves=below&&leaves(belowMeta)!=0?2:1;
        int newAge=age(old)==1||twoBelow?1:0;
        int terminal=(height>=11&&r.nextFloat()<.25F)||height==15?1:0;
        if(!w.setBlock(x,y+1,z,this,state(newAge,newLeaves,terminal),3))return false;
        // Do not strip existing foliage when another mod rejects the new block.
        if(newLeaves==2&&twoBelow){
            w.setBlockMetadataWithNotify(x,y-1,z,state(age(belowMeta),1,stage(belowMeta)),3);
            w.setBlockMetadataWithNotify(x,y-2,z,state(age(twoMeta),0,stage(twoMeta)),3);
        }
        return true;
    }
    @Override public boolean func_149851_a(World w,int x,int y,int z,boolean c){
        int top=top(w,x,y,z);return w.getBlock(x,y,z)==this&&height(w,x,top,z)<16&&stage(w.getBlockMetadata(x,top,z))==0;
    }
    @Override public boolean func_149852_a(World w,Random r,int x,int y,int z){return true;}
    @Override public void func_149853_b(World w,Random r,int x,int y,int z){
        if(w.isRemote)return;int top=top(w,x,y,z),count=1+r.nextInt(2);
        for(int i=0;i<count;i++){if(!grow(w,r,x,top,z))break;top++;}
    }
    @Override public int colorMultiplier(IBlockAccess w,int x,int y,int z){return w.getBiomeGenForCoords(x,z).getBiomeGrassColor(x,y,z);}
}
