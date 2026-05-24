package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c;

import com.mod.htgt6.common.GalaxyServer.Glise792.sysGliese792;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;
import micdoodle8.mods.galacticraft.api.vector.Vector3;


import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldProviderGlieseC extends WorldProviderSpace {

    @Override
    public Class<? extends IChunkProvider> getChunkProviderClass() {
        return ChunkProviderGlieseC.class;
    }

    @Override
    public float getWindLevel() {
        return 0.3F;
    }

    @Override
    public float getSoundVolReductionAmount() {
        return 10.0F;
    }

    @Override
    public float getFallDamageModifier() {
        return 1.0F;
    }

    @Override
    public boolean canSpaceshipTierPass(int tier) {
        return tier >= 2;
    }

    @Override
    public CelestialBody getCelestialBody() {
        return sysGliese792.planetGlieseC;
    }

    @Override
    public Class<? extends WorldChunkManager> getWorldChunkManagerClass() {
        return WorldChunkManagerGlieseC.class;
    }

    // =========================
    // SKY / ATMOSPHERE
    // =========================

    @Override
    @SideOnly(Side.CLIENT)
    public Vector3 getSkyColor() {
        // Returns RGB color ratios (Red, Green, Blue) from 0.0 to 1.0
        // Cyan layout: Darker blue-green mix for space transitions
        return new Vector3(0.0D, 0.7D, 0.85D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vector3 getFogColor() {
        // Rich cyan horizon atmosphere thickness fog color
        return new Vector3(0.1D, 0.8D, 0.95D);
    }

    @Override
    public boolean hasBreathableAtmosphere() {
        return true;
    }

    // =========================
    // PLANET SETTINGS
    // =========================

    @Override
    public float getGravity() {
        return 0.058F;

    }


    @Override
    public double getMeteorFrequency() {
        return 1.0D;
    }

    @Override
    public float getThermalLevelModifier() {
        return 0.0F;
    }

    @Override
    public boolean canRainOrSnow() {
        return false;
    }

    @Override
    public boolean hasSunset() {
        return true;
    }

    @Override
    public long getDayLength() {
        return 24000L;
    }

    @Override
    public String getDimensionName() {
        return "Gliese 792 c";
    }

    @Override
    public double getFuelUsageMultiplier() {
        return 1.0D;
    }

    // =========================
    // IMPORTANT GC FIXES
    // =========================

    @Override
    public String getSaveFolder() {
        return "DIMGLIESEC";
    }

    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
        return true;
    }

    @Override
    public boolean canRespawnHere() {
        return false;
    }

    @Override
    public boolean shouldForceRespawn() {
        return true;
    }

}