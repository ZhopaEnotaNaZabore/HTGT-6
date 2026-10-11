package com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

/** Flower effects from the official 1.20.2 block definitions, stored in the existing stew payload. */
public final class VillageFoodRecipes114 {
    public static ItemStack stewForFlower(ItemStack flower){
        if(flower==null||flower.getItem()==null)return null;
        net.minecraft.block.Block b=net.minecraft.block.Block.getBlockFromItem(flower.getItem());int id=-1,ticks=0,m=flower.getItemDamage();
        if(b==Blocks.yellow_flower&&m==0){id=23;ticks=7;}
        else if(b==Blocks.red_flower&&m>=0&&m<9){int[] ids={16,23,12,15,18,18,18,18,10},times={100,7,80,160,180,180,180,180,160};id=ids[m];ticks=times[m];}
        else if(b==Backport120Content.cornflower){id=8;ticks=120;}
        else if(b==Backport120Content.lilyOfTheValley){id=19;ticks=240;}
        else if(b==EtFVanillaGapContent120.witherRose){id=20;ticks=160;}
        if(id<0)return null;ItemStack stew=new ItemStack(OceanContent.suspiciousStew);NBTTagCompound tag=new NBTTagCompound();tag.setInteger("EffectId",id);tag.setInteger("EffectDuration",ticks);stew.setTagCompound(tag);return stew;
    }
    public static void register(){
        add(new ItemStack(Blocks.yellow_flower),23,7);
        int[] effects={Potion.nightVision.id,23,Potion.fireResistance.id,Potion.blindness.id,
                Potion.weakness.id,Potion.weakness.id,Potion.weakness.id,Potion.weakness.id,Potion.regeneration.id};
        int[] ticks={100,7,80,160,180,180,180,180,160};
        for(int meta=0;meta<effects.length;meta++)add(new ItemStack(Blocks.red_flower,1,meta),effects[meta],ticks[meta]);
        add(new ItemStack(Backport120Content.cornflower),Potion.jump.id,120);
        add(new ItemStack(Backport120Content.lilyOfTheValley),Potion.poison.id,240);
        add(new ItemStack(EtFVanillaGapContent120.witherRose),Potion.wither.id,160);
    }
    private static void add(ItemStack flower,int effect,int ticks){
        ItemStack stew=new ItemStack(OceanContent.suspiciousStew);
        NBTTagCompound tag=new NBTTagCompound();tag.setInteger("EffectId",effect);tag.setInteger("EffectDuration",ticks);stew.setTagCompound(tag);
        GameRegistry.addShapelessRecipe(stew,Items.bowl,Blocks.red_mushroom,Blocks.brown_mushroom,flower);
    }
    private VillageFoodRecipes114(){}
}
