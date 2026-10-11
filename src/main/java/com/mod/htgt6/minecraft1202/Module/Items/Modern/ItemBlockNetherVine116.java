package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockNetherVine116;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.world.World;

/** Clicking any segment with its own item extends the end instead of replacing the body. */
public final class ItemBlockNetherVine116 extends ItemBlock {
    public ItemBlockNetherVine116(Block block){super(block);}
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World w,int x,int y,int z,int side,float hx,float hy,float hz){
        BlockNetherVine116 vine=(BlockNetherVine116)field_150939_a;
        if(!vine.same(w.getBlock(x,y,z)))return super.onItemUse(stack,player,w,x,y,z,side,hx,hy,hz);
        if(stack.stackSize<=0)return false;
        int direction=vine.direction,height=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.height(w),steps=0;
        while(y>=0&&y<height&&w.blockExists(x,y,z)&&vine.same(w.getBlock(x,y,z))&&steps++<height)y+=direction;
        side=direction>0?1:0;
        if(y<0||y>=height||!w.blockExists(x,y,z)||!w.isAirBlock(x,y,z)||!player.canPlayerEdit(x,y,z,side,stack)||!w.canPlaceEntityOnSide(vine,x,y,z,false,side,player,stack))return false;
        if(w.isRemote)return true;
        if(!placeBlockAt(stack,player,w,x,y,z,side,hx,hy,hz,0))return false;
        w.playSoundEffect(x+.5,y+.5,z+.5,vine.stepSound.func_150496_b(),(vine.stepSound.getVolume()+1)/2,vine.stepSound.getPitch()*.8F);
        if(!player.capabilities.isCreativeMode)stack.stackSize--;
        return true;
    }
}
