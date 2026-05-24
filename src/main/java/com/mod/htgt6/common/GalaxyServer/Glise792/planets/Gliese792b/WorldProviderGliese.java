package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b;

import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.ChunkProviderGlieseB;
import com.mod.htgt6.common.GalaxyServer.Glise792.sysGliese792;
import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.WorldChunkManagerGlieseb;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.api.world.ISolarLevel;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldProviderGliese extends WorldProviderSpace implements IGalacticraftWorldProvider, ISolarLevel {
    public WorldProviderGliese() {
    }

    public CelestialBody getCelestialBody() {
        return sysGliese792.planetGlieseB;
    }

    public Class<? extends IChunkProvider> getChunkProviderClass() {
        return ChunkProviderGlieseB.class;
    }

    public Class<? extends WorldChunkManager> getWorldChunkManagerClass() {
        return WorldChunkManagerGlieseb.class;
    }

    public float getSoundVolReductionAmount() {
        return 10.0F;
    }

    public String getDimensionName() {
        return "Gliese 792 b";
    }

    public double getFuelUsageMultiplier() {
        return 0.9;
    }

    public float getFallDamageModifier() {
        return 1.0F;
    }

    public float getWindLevel() {
        return 0.3F;
    }

    public float getThermalLevelModifier() {
        return -1.0F;
    }

    public boolean canRainOrSnow() {
        return false;
    }

    public boolean hasSunset() {
        return true;
    }

    public long getDayLength() {
        return 24660L;
    }

    public float getGravity() {
        return 0.058F;
    }

    public double getMeteorFrequency() {
        return 10.0;
    }

    public boolean hasBreathableAtmosphere() {
        return false;
    }

    public Vector3 getFogColor() {
        return new Vector3(0.18, 0.05, 0.22);
    }

    public Vector3 getSkyColor() {
        return new Vector3(0.1, 0.02, 0.15);
    }

    public boolean canSpaceshipTierPass(int tier) {
        return tier >= 2;
    }

    public double getSolarEnergyMultiplier() {
        return 1.0;
    }
}
