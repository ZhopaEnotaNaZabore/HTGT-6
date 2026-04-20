package com.mod.htgt6.client.render.biomes.lukewarm;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraftforge.client.event.EntityViewRenderEvent;


public class FogEventHandler {


    @SubscribeEvent
    public void onFogColor(EntityViewRenderEvent.FogColors event) {
        // Check if camera is inside water
        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(event.entity.worldObj, event.entity, (float) event.renderPartialTicks);

        if (block.getMaterial() == Material.water) {
            // Light Cyan Fog (Values are 0.0f to 1.0f)
            event.red = 0.2F;
            event.green = 1.0F;
            event.blue = 1.0F;
        }
    }

    @SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(event.entity.worldObj, event.entity, (float) event.renderPartialTicks);

        if (block.getMaterial() == Material.water) {
            // 0.02 is vanilla, 0.05 is thick/deep, 0.1 is very dense
            event.density = 0.05F;
            //  event.isCancelable = true; // Required to override vanilla density
        }
    }
}



