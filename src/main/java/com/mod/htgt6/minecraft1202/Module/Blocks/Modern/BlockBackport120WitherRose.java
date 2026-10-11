package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.block.BlockBush;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** Wither rose with the vanilla contact effect. */
public final class BlockBackport120WitherRose extends BlockBush {
    public BlockBackport120WitherRose(){
        setBlockName("backport_1_20.wither_rose");
        setBlockTextureName("htgt6:1_20_backport/wither_rose");
        setCreativeTab(CreativeTabs.tabDecorations);
    }
    @Override public void onEntityCollidedWithBlock(World world,int x,int y,int z,Entity entity){
        if(!world.isRemote&&entity instanceof EntityLivingBase){
            EntityLivingBase living=(EntityLivingBase)entity;
            if(living.getCreatureAttribute()!=EnumCreatureAttribute.UNDEAD)
                living.addPotionEffect(new PotionEffect(Potion.wither.id,40));
        }
    }
}
