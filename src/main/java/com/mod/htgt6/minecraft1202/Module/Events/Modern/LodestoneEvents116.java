package com.mod.htgt6.minecraft1202.Module.Events.Modern;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.LodestoneBinding116;

public final class LodestoneEvents116 {
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void interact(PlayerInteractEvent event){
        // Never cancel the client event: 1.7.10 then suppresses the outgoing C08.
        // On the server this path also handles sneak-clicking with the generic vanilla Item.
        if(event.world.isRemote||event.action!=PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK||event.isCanceled()
                ||event.useBlock==Event.Result.DENY||event.useItem==Event.Result.DENY)return;
        if(LodestoneBinding116.bind(event.entityPlayer,event.world,event.x,event.y,event.z,event.face))event.setCanceled(true);
    }
    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent event){
        if(event.phase!=TickEvent.Phase.END||event.world.isRemote||event.world.getTotalWorldTime()%20!=0)return;
        // Frames do not call Item.onUpdate. Scan already loaded entities once per second;
        // never scan chunks or consult DimensionManager for an unloaded target dimension.
        for(Object entity:event.world.loadedEntityList)if(entity instanceof EntityItemFrame){
            EntityItemFrame frame=(EntityItemFrame)entity;ItemStack stack=frame.getDisplayedItem();
            if(!frame.isDead&&LodestoneBinding116.validate(stack,event.world))frame.setDisplayedItem(stack);
        }
    }
}
