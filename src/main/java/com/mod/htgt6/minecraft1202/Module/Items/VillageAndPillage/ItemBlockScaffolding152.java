package com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockScaffolding152;
import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.item.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.world.World;import net.minecraft.util.MathHelper;import net.minecraftforge.common.util.ForgeDirection;

/** Extend a tower upward or a bridge horizontally without replacing its clicked block. */
public final class ItemBlockScaffolding152 extends ItemBlock {
    public ItemBlockScaffolding152(Block b){super(b);}
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer p,World w,int x,int y,int z,int side,float a,float b,float c){
        if(stack.stackSize<=0)return false;BlockScaffolding152 block=(BlockScaffolding152)field_150939_a;boolean extend=w.getBlock(x,y,z)==block;
        ForgeDirection dir=ForgeDirection.getOrientation(side);
        if(extend){
            if(!p.isSneaking())dir=side==1?new ForgeDirection[]{ForgeDirection.SOUTH,ForgeDirection.WEST,ForgeDirection.NORTH,ForgeDirection.EAST}[MathHelper.floor_double(p.rotationYaw*4/360+.5)&3]:ForgeDirection.UP;
            int horizontal=0;
            do{x+=dir.offsetX;y+=dir.offsetY;z+=dir.offsetZ;if(y<0||y>=w.getHeight()||!w.blockExists(x,y,z))return false;if(w.getBlock(x,y,z)!=block)break;if(dir.offsetY==0&&++horizontal>=7)return false;}while(true);
        }else if(!w.getBlock(x,y,z).isReplaceable(w,x,y,z)){x+=dir.offsetX;y+=dir.offsetY;z+=dir.offsetZ;}
        if(y<0||y>=w.getHeight()||!w.blockExists(x,y,z)||!p.canPlayerEdit(x,y,z,side,stack)||!w.getBlock(x,y,z).isReplaceable(w,x,y,z))return false;
        int d=block.distance(w,x,y,z);if(d<0||(!extend&&d==7))return false;
        if(w.isRemote)return true;
        int wet=w.getBlock(x,y,z).getMaterial()==Material.water&&w.getBlockMetadata(x,y,z)==0?8:0;
        if(!w.setBlock(x,y,z,block,d|wet,3))return false;
        block.onBlockPlacedBy(w,x,y,z,p,stack);w.playSoundEffect(x+.5,y+.5,z+.5,block.stepSound.func_150496_b(),1,.8F);
        if(!p.capabilities.isCreativeMode)stack.stackSize--;return true;
    }
}
