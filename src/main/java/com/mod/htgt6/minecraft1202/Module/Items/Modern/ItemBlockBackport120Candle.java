package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Candle;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ItemBlockBackport120Candle extends ItemBlock {
    public ItemBlockBackport120Candle(Block block){super(block);}
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hitX,float hitY,float hitZ){
        if(world.getBlock(x,y,z)==field_150939_a){int m=world.getBlockMetadata(x,y,z),count=BlockBackport120Candle.count(m);
            if(count<4){if(!world.isRemote){world.setBlockMetadataWithNotify(x,y,z,BlockBackport120Candle.metadata(count+1,(m&BlockBackport120Candle.LIT)!=0),3);if(!player.capabilities.isCreativeMode)--stack.stackSize;}return true;}}
        return super.onItemUse(stack,player,world,x,y,z,side,hitX,hitY,hitZ);
    }
}
