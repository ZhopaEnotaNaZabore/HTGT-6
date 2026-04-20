package com.mod.htgt6.client.render.biomes.lukewarm;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.event.terraingen.BiomeEvent;
import org.apache.commons.codec.binary.Hex;
import org.lwjgl.opengl.GL11;

public class warmOceanWater {
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onWaterColor(BiomeEvent.GetWaterColor event) {

        if (event.biome.biomeID == 120) {
            event.newColor = 0x89FD89; // Light Cyan
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderWaterOverlay(RenderBlockOverlayEvent event) {
        if (event.overlayType == RenderBlockOverlayEvent.OverlayType.WATER) {
            // This sets the GL color before the blue texture is rendered on your face
            // R: 0.2, G: 1.0, B: 1.0 (Cyan)
            GL11.glColor4f(0.0F, 2.0F, 1.0F, 0.5F);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onSkyColor(EntityViewRenderEvent.FogColors event) {
        // 1. Get the biome at the player's location
        int x = (int) event.entity.posX;
        int z = (int) event.entity.posZ;
        BiomeGenBase biome = event.entity.worldObj.getBiomeGenForCoords(x, z);

        // 2. Check if it's your custom ocean (ID 500)
        if (biome.biomeID == 120) {
            // Cyan Sky: R=0.4, G=1.0, B=1.0
            event.red = 0.4F;
            event.green = 1.0F;
            event.blue = 0.8F;
        }
    }

}

