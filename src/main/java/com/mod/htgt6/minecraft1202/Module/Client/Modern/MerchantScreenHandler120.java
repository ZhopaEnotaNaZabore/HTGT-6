package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWanderingTrader120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

/** Replaces native merchant screens with the 1.20 layout while retaining 1.7 packets/containers. */
public final class MerchantScreenHandler120 {
    private EntityVillager trackedVillager;
    private int expectedTicks;
    private String title;
    private int level;
    private boolean wandering;
    @SubscribeEvent public void onEntityInteract(EntityInteractEvent event){
        if(!event.entityPlayer.worldObj.isRemote||!(event.target instanceof EntityVillager))return;
        EntityVillager villager=(EntityVillager)event.target;
        trackedVillager=villager;
        wandering=villager instanceof EntityWanderingTrader120;
        String profession=wandering?"wandering_trader":VillagerCareers112.getVisualProfession(villager);
        title=villager.hasCustomNameTag()?villager.getCustomNameTag():I18n.format(
                wandering?"entity.HTGT6.WanderingTrader.name":"entity.HTGT6.VillagerProfession."+profession+".name");
        level=wandering?0:VillagerCareers112.getVisualLevel(villager);
        expectedTicks=40;
    }
    @SubscribeEvent public void onGuiOpen(GuiOpenEvent event){
        if(expectedTicks<=0||!(event.gui instanceof GuiMerchant))return;
        GuiMerchant old=(GuiMerchant)event.gui;Minecraft minecraft=Minecraft.getMinecraft();
        event.gui=new GuiMerchant120(minecraft.thePlayer.inventory,old.func_147035_g(),minecraft.theWorld,
                title==null?I18n.format("entity.Villager.name"):title,level,!wandering);
        ((GuiMerchant120)event.gui).track(trackedVillager);
        expectedTicks=0;
    }
    @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent event){
        if(event.phase==TickEvent.Phase.END&&expectedTicks>0)expectedTicks--;
    }
}
