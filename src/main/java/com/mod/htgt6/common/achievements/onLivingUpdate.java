package com.mod.htgt6.common.achievements;
//НЕ РАБОТАЕТ
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.stats.StatBase;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;

public class onLivingUpdate {

    @SubscribeEvent
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase living = event.entityLiving;
        World world = living.worldObj;

        if (living instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP)living;
            if(player.inventory.hasItem(Items.flint))
                player.triggerAchievement(Hitech6AchieveDatabase.Flint);
        }}

        public void triggerAchievement(StatBase p_71029_1_)
        {
            this.addStat(p_71029_1_, 1);
        }

        public void addStat(StatBase p_71064_1_, int p_71064_2_) {}

    }
