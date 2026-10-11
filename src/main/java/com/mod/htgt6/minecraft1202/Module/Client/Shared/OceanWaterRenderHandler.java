package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanBiomeRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.material.Material;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.EntityViewRenderEvent;

/** Backports biome-specific underwater fog colour without touching world generation. */
public final class OceanWaterRenderHandler {
    @SubscribeEvent
    public void onFogColors(EntityViewRenderEvent.FogColors event) {
        if (event.entity.worldObj.provider.dimensionId != 0 || event.block.getMaterial() != Material.water) return;
        int x = MathHelper.floor_double(event.entity.posX);
        int z = MathHelper.floor_double(event.entity.posZ);
        BiomeGenBase biome = event.entity.worldObj.getBiomeGenForCoords(x, z);
        // water_fog_color is independent from water_color in 1.20.2.
        int color = OceanBiomeRegistry.isWarm(biome) ? 0x041F33
                : OceanBiomeRegistry.isLukewarm(biome) ? 0x041633 : 0x050533;
        event.red = ((color >> 16) & 255) / 255.0F;
        event.green = ((color >> 8) & 255) / 255.0F;
        event.blue = (color & 255) / 255.0F;
    }
}
