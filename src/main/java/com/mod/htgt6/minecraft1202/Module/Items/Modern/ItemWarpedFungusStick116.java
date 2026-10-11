package com.mod.htgt6.minecraft1202.Module.Items.Modern;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityStrider116;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.init.Items;
import net.minecraft.world.World;
public final class ItemWarpedFungusStick116 extends Item {
    public ItemWarpedFungusStick116(){setUnlocalizedName("backport_1_20.warped_fungus_on_a_stick");setTextureName("htgt6:1_20_backport/warped_fungus_on_a_stick");setMaxStackSize(1);setMaxDamage(100);setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabTransport);}
    @Override public boolean isFull3D(){return true;}
    @Override public ItemStack onItemRightClick(ItemStack stack,World w,EntityPlayer player){
        if(!w.isRemote&&player.ridingEntity instanceof EntityStrider116&&((EntityStrider116)player.ridingEntity).isSaddled()&&((EntityStrider116)player.ridingEntity).boost()){
            if(!player.capabilities.isCreativeMode){int before=stack.getItemDamage();stack.damageItem(1,player);if(stack.stackSize<=0||stack.getItemDamage()!=before)com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.complete(player,"ride_strider");}
            if(stack.stackSize<=0){ItemStack rod=new ItemStack(Items.fishing_rod);if(stack.hasTagCompound())rod.setTagCompound((net.minecraft.nbt.NBTTagCompound)stack.getTagCompound().copy());return rod;}
        }return stack;
    }
}
