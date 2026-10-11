package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import java.lang.reflect.Field;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.*;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemSuspiciousStew120;

/** Legacy ItemFood's callback requires a player. Apply its data to the actual eating mob. */
public final class FoxFood114 {
    private static final Field ID=ReflectionHelper.findField(ItemFood.class,"potionId","field_77851_ca"),
            DURATION=ReflectionHelper.findField(ItemFood.class,"potionDuration","field_77850_cb"),
            AMPLIFIER=ReflectionHelper.findField(ItemFood.class,"potionAmplifier","field_77857_cc"),
            CHANCE=ReflectionHelper.findField(ItemFood.class,"potionEffectProbability","field_77858_cd");
    public static ItemStack finish(EntityLivingBase eater, ItemStack food) {
        if(eater.worldObj.isRemote||food==null||food.stackSize<1||!(food.getItem() instanceof ItemFood))return food;
        Item item=food.getItem();
        if(item instanceof ItemSuspiciousStew120 && food.hasTagCompound()) {
            NBTTagCompound tag=food.getTagCompound();effect(eater,tag.getInteger("EffectId"),tag.getInteger("EffectDuration"),0);
        } else if(item==Items.fish && food.getItemDamage()==3) {
            effect(eater,Potion.poison.id,1200,1);effect(eater,Potion.hunger.id,300,2);effect(eater,Potion.confusion.id,300,0);
        } else if(item==Items.golden_apple) {
            boolean enchanted=food.getItemDamage()>0;
            effect(eater,Potion.regeneration.id,enchanted?400:100,1);
            effect(eater,Potion.field_76444_x.id,2400,enchanted?3:0);
            if(enchanted){effect(eater,Potion.resistance.id,6000,0);effect(eater,Potion.fireResistance.id,6000,0);}
        } else try {
            if(eater.getRNG().nextFloat()<CHANCE.getFloat(item))effect(eater,ID.getInt(item),DURATION.getInt(item)*20,AMPLIFIER.getInt(item));
        } catch(IllegalAccessException error){throw new IllegalStateException("Cannot read legacy food effect",error);}
        ItemStack container=item instanceof ItemSoup?new ItemStack(Items.bowl):item.getContainerItem(food);
        --food.stackSize;
        if(food.stackSize==0)return container;
        if(container!=null)eater.entityDropItem(container,0);
        return food;
    }
    private static void effect(EntityLivingBase eater,int id,int ticks,int amplifier) {
        if(id>0&&id<Potion.potionTypes.length&&Potion.potionTypes[id]!=null&&ticks>0)
            eater.addPotionEffect(new PotionEffect(id,ticks,amplifier));
    }
    private FoxFood114() {}
}
