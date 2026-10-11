package com.mod.htgt6.minecraft1202.Module.Items.Aquatic;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;

/** 1.20.2 Turtle Helmet adapted to the 1.7.10 armour and potion APIs. */
public final class ItemTurtleHelmet120 extends ItemArmor {
    public static final int WATER_BREATHING_TICKS=200;
    public static final int DURABILITY=275;
    public static final int DEFENCE=2;
    public static final int ENCHANTABILITY=9;
    private static final ArmorMaterial TURTLE=EnumHelper.addArmorMaterial("MINECRAFT_1_20_2_TURTLE",25,
            new int[]{DEFENCE,4,3,1},ENCHANTABILITY);

    public ItemTurtleHelmet120(){
        super(TURTLE,0,0);
        setUnlocalizedName("turtle_helmet");
        setTextureName("htgt6:turtle_helmet");
        setCreativeTab(CreativeTabs.tabCombat);
    }

    @Override public void onArmorTick(World world,EntityPlayer player,ItemStack stack){
        if(!world.isRemote&&shouldRefreshWaterBreathing(true,player.isInWater()))
            player.addPotionEffect(new PotionEffect(Potion.waterBreathing.id,WATER_BREATHING_TICKS,0,false));
    }

    public static boolean shouldRefreshWaterBreathing(boolean equipped,boolean inWater){
        return equipped&&!inWater;
    }

    @Override public boolean getIsRepairable(ItemStack armour,ItemStack ingredient){
        return ingredient!=null&&ingredient.getItem()==OceanContent.scute||super.getIsRepairable(armour,ingredient);
    }

    @Override public String getArmorTexture(ItemStack stack,net.minecraft.entity.Entity entity,int slot,String type){
        return "htgt6:textures/models/armor/turtle_layer_1.png";
    }
}
