package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Public 2-D chain item placing the technical axis-aware chain block. */
public final class ItemBackport120Chain extends Item {
    private final Block block;
    public ItemBackport120Chain(Block block){
        this.block=block;
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);
        setUnlocalizedName("backport_1_20.chain");
        setTextureName("htgt6:1_20_backport/chain_item");
    }
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,
                                       float hitX,float hitY,float hitZ){
        Block target=world.getBlock(x,y,z);
        if(!target.isReplaceable(world,x,y,z)){
            if(side==0)y--;else if(side==1)y++;else if(side==2)z--;else if(side==3)z++;else if(side==4)x--;else if(side==5)x++;
        }
        if(!player.canPlayerEdit(x,y,z,side,stack)||!block.canPlaceBlockAt(world,x,y,z))return false;
        int metadata=block.onBlockPlaced(world,x,y,z,side,hitX,hitY,hitZ,0);
        if(world.isRemote)return true;
        if(!world.setBlock(x,y,z,block,metadata,3))return false;
        if(!player.capabilities.isCreativeMode)stack.stackSize--;
        return true;
    }
}
