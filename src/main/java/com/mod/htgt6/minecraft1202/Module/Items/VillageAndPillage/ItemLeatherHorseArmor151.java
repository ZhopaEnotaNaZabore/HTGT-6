package com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage;

import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/** Dyeable, non-damageable horse armor; protection is supplied by the horse hook. */
public final class ItemLeatherHorseArmor151 extends Item {
    public static final int DEFAULT_COLOR=0xA06540;
    public ItemLeatherHorseArmor151(){setMaxStackSize(1);setUnlocalizedName("village_pillage_leather_horse_armor");setTextureName("htgt6:village_pillage/leather_horse_armor");setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabCombat);}
    public static boolean hasColor(ItemStack stack){return stack!=null&&stack.hasTagCompound()&&stack.getTagCompound().hasKey("display",10)&&stack.getTagCompound().getCompoundTag("display").hasKey("color",99);}
    public static int color(ItemStack stack){return hasColor(stack)?stack.getTagCompound().getCompoundTag("display").getInteger("color")&0xFFFFFF:DEFAULT_COLOR;}
    public static void setColor(ItemStack stack,int rgb){if(!stack.hasTagCompound())stack.setTagCompound(new NBTTagCompound());NBTTagCompound root=stack.getTagCompound();if(!root.hasKey("display",10))root.setTag("display",new NBTTagCompound());root.getCompoundTag("display").setInteger("color",rgb&0xFFFFFF);}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack,int pass){return color(stack);}
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hitX,float hitY,float hitZ){
        if(world.getBlock(x,y,z)!=Blocks.cauldron||world.getBlockMetadata(x,y,z)<=0||!hasColor(stack)||!player.canPlayerEdit(x,y,z,side,stack))return false;
        if(!world.isRemote&&world.setBlockMetadataWithNotify(x,y,z,world.getBlockMetadata(x,y,z)-1,3))stack.getTagCompound().getCompoundTag("display").removeTag("color");
        return true;
    }
}
