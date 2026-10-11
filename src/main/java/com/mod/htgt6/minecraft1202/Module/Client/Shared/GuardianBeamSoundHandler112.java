package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulGuardian;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

/** Starts GuardianSound when watcher 21 changes from no target to a synchronized target. */
public final class GuardianBeamSoundHandler112 {
    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.theWorld == null) return;
        List<Entity> entities = minecraft.theWorld.loadedEntityList;
        for (Entity entity : entities) {
            if (entity instanceof EntityBountifulGuardian) {
                EntityBountifulGuardian guardian = (EntityBountifulGuardian)entity;
                if (guardian.consumeBeamSoundRequest()) {
                    minecraft.getSoundHandler().playSound(new GuardianBeamSound112(guardian));
                }
            }
        }
    }
}
