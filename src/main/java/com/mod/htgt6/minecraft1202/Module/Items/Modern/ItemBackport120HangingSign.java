package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Sign;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Places hanging signs only below or against a solid support, matching their 1.20 role. */
public final class ItemBackport120HangingSign extends Item {
    private final Block ceiling;
    private final Block wall;

    public ItemBackport120HangingSign(Block ceiling, Block wall) {
        this("cherry_hanging_sign", ceiling, wall);
    }

    public ItemBackport120HangingSign(String name, Block ceiling, Block wall) {
        this.ceiling=ceiling; this.wall=wall;
        setMaxStackSize(16);
        setCreativeTab(CreativeTabs.tabDecorations);
        setUnlocalizedName("backport_1_20." + name);
        setTextureName("htgt6:1_20_backport/" + name);
    }

    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,
                                       float hitX,float hitY,float hitZ){
        if(side==1||!world.getBlock(x,y,z).getMaterial().isSolid())return false;
        int px=x,py=y,pz=z;
        if(side==0)py--;else if(side==2)pz--;else if(side==3)pz++;else if(side==4)px--;else if(side==5)px++;
        if(!player.canPlayerEdit(px,py,pz,side,stack)||!world.isAirBlock(px,py,pz))return false;
        Block placed=side==0?ceiling:wall;
        int metadata=side==0?MathHelper.floor_double((player.rotationYaw+180.0F)*16.0F/360.0F+0.5D)&15:side;
        if(side==0&&!placed.canPlaceBlockAt(world,px,py,pz))return false;
        if(world.isRemote)return true;
        if(!world.setBlock(px,py,pz,placed,metadata,3))return false;
        stack.stackSize--;
        TileEntity tile=world.getTileEntity(px,py,pz);
        if(tile instanceof TileEntityBackport120Sign){
            TileEntityBackport120Sign sign=(TileEntityBackport120Sign)tile;
            if(side==0)sign.setHangingSignAttached(player.isSneaking());
            player.func_146100_a(sign);
        }
        return true;
    }
}
