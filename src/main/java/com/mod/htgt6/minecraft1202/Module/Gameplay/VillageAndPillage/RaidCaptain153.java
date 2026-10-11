package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVindicator112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityEvoker112;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;

/** Captain state and real head equipment shared by the three leader-capable illagers. */
public final class RaidCaptain153 {
    public static boolean eligible(EntityLiving entity){return entity instanceof EntityPillager||entity instanceof EntityVindicator112||entity instanceof EntityEvoker112;}
    public static boolean isCaptain(EntityLiving entity){return entity instanceof EntityPillager?((EntityPillager)entity).isCaptain():eligible(entity)&&entity.getEntityData().getBoolean("HTGT6RaidCaptain");}
    public static void equip(EntityLiving entity,ItemStack banner){
        if(!eligible(entity))return;
        if(entity instanceof EntityPillager)((EntityPillager)entity).setCaptain(true);
        else entity.getEntityData().setBoolean("HTGT6RaidCaptain",true);
        entity.setCurrentItemOrArmor(4,banner.copy());entity.setEquipmentDropChance(4,2F);
    }
    public static boolean isBanner(ItemStack stack){
        ItemStack reference=EntityPillager.createCaptainBanner();
        return stack!=null&&stack.stackSize>0&&stack.getItem()==reference.getItem()
                &&stack.getItemDamage()==reference.getItemDamage()&&ItemStack.areItemStackTagsEqual(stack,reference);
    }
    private RaidCaptain153() { }
}
