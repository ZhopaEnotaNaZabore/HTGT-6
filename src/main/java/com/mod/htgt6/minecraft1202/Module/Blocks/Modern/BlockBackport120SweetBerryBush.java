package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.Random;

/** Four growth ages with harvesting and thorn slowdown/damage. */
public final class BlockBackport120SweetBerryBush extends BlockBush implements IGrowable {
    private static final DamageSource THORNS = new DamageSource("sweetBerryBush");
    public BlockBackport120SweetBerryBush(){setBlockName("backport_1_20.sweet_berry_bush");
        setBlockTextureName("htgt6:1_20_backport/sweet_berry_bush_stage0");setHardness(0);
        setStepSound(Block.soundTypeGrass);setTickRandomly(true);setBlockBounds(.1F,0,.1F,.9F,.8F,.9F);}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override protected boolean canPlaceBlockOn(Block b){return b==Blocks.grass||b==Blocks.dirt||b==Blocks.farmland
            ||b==Backport120Content.mossBlock||b==Backport120Content.mud;}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){return null;}
    @Override public void updateTick(World w,int x,int y,int z,Random r){int age=w.getBlockMetadata(x,y,z)&3;
        if(!w.isRemote&&age<3&&w.getBlockLightValue(x,y+1,z)>=9&&r.nextInt(5)==0)w.setBlockMetadataWithNotify(x,y,z,age+1,3);}
    @Override public void onEntityCollidedWithBlock(World w,int x,int y,int z,Entity e){
        if(!com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.BerryMovement147.affected(e))return;
        com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.BerryMovement147.touch(e);
        if(!w.isRemote&&(w.getBlockMetadata(x,y,z)&3)>0
                &&(Math.abs(e.posX-e.lastTickPosX)>=(double).003F||Math.abs(e.posZ-e.lastTickPosZ)>=(double).003F))e.attackEntityFrom(THORNS,1);
    }
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float hx,float hy,float hz){
        int age=w.getBlockMetadata(x,y,z)&3;
        ItemStack held=p.getHeldItem();
        if(age<3&&held!=null&&held.getItem()==net.minecraft.init.Items.dye&&held.getItemDamage()==15)return false;
        if(age<2)return false;
        if(!w.isRemote){
            if(!w.setBlockMetadataWithNotify(x,y,z,1,3))return false;
            int count=1+w.rand.nextInt(2)+(age==3?1:0);
            dropBlockAsItem(w,x,y,z,new ItemStack(Backport120Content.sweetBerries,count));
            w.playSoundEffect(x+.5,y+.5,z+.5,"htgt6:block.sweet_berry_bush.pick_berries",1F,.8F+w.rand.nextFloat()*.4F);
        }
        return true;
    }
    @Override public int damageDropped(int metadata){return 0;}
    @Override public Item getItemDropped(int metadata,Random random,int fortune){return Backport120Content.sweetBerries;}
    @Override public int quantityDropped(int metadata,int fortune,Random random){int age=metadata&3;return age<2?0:(age==2?1+random.nextInt(2):2+random.nextInt(2))+random.nextInt(Math.max(0,fortune)+1);}
    @Override public boolean func_149851_a(World w,int x,int y,int z,boolean client){return(w.getBlockMetadata(x,y,z)&3)<3;}
    @Override public boolean func_149852_a(World w,Random r,int x,int y,int z){return true;}
    @Override public void func_149853_b(World w,Random r,int x,int y,int z){int a=w.getBlockMetadata(x,y,z)&3;if(!w.isRemote&&a<3)w.setBlockMetadataWithNotify(x,y,z,a+1,3);}
}
