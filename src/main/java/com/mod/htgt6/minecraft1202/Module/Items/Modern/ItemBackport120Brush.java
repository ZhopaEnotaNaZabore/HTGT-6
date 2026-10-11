package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Suspicious;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Brushable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;

/** Brush shell with the exact 64 durability; archaeology targets are attached in the next stage slice. */
public final class ItemBackport120Brush extends Item {
    public ItemBackport120Brush(){setUnlocalizedName("backport_1_20.brush");setTextureName("htgt6:1_20_backport/brush");
        setMaxStackSize(1);setMaxDamage(64);setFull3D();setCreativeTab(CreativeTabs.tabTools);}
    @Override public int getMaxItemUseDuration(ItemStack stack){return 200;}
    @Override public EnumAction getItemUseAction(ItemStack stack){return EnumAction.block;}
    @Override public ItemStack onItemRightClick(ItemStack stack,World world,EntityPlayer player){player.setItemInUse(stack,getMaxItemUseDuration(stack));return stack;}
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hx,float hy,float hz){
        if(!(world.getBlock(x,y,z) instanceof BlockBackport120Suspicious))return false;player.setItemInUse(stack,getMaxItemUseDuration(stack));return true;}
    @Override public void onUsingTick(ItemStack stack,EntityPlayer player,int count){int elapsed=getMaxItemUseDuration(stack)-count;
        if(elapsed<=0||elapsed%10!=0)return;MovingObjectPosition hit=player.rayTrace(5.0D,1.0F);
        if(hit==null||hit.typeOfHit!=MovingObjectPosition.MovingObjectType.BLOCK)return;World world=player.worldObj;
        if(!(world.getBlock(hit.blockX,hit.blockY,hit.blockZ) instanceof BlockBackport120Suspicious))return;
        net.minecraft.tileentity.TileEntity tile=world.getTileEntity(hit.blockX,hit.blockY,hit.blockZ);
        if(tile instanceof TileEntityBackport120Brushable&&!world.isRemote&&((TileEntityBackport120Brushable)tile)
                .brush(ForgeDirection.getOrientation(hit.sideHit)))stack.damageItem(1,player);}
}
