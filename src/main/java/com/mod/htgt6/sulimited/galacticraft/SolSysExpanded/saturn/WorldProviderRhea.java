package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public final class WorldProviderRhea extends AbstractSaturnMoonProvider {
    public CelestialBody getCelestialBody(){return SaturnMoonWorlds.rhea;}
    public Class<? extends IChunkProvider> getChunkProviderClass(){return ChunkProviderRhea.class;}
    public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerRhea.class;}
    public String getDimensionName(){return "Rhea";}
    public float getGravity(){return 0.071F;}
    public float getFallDamageModifier(){return 0.07F;}
    public float getThermalLevelModifier(){return -4.35F;}
    public long getDayLength(){return 390535L;}
    public boolean canSpaceshipTierPass(int tier){return tier>=6;}
    protected float getSaturnHorizontalAngle(){return -143.0F;}
    protected float getSaturnVerticalAngle(){return 20.0F;}
    protected float getSaturnSkySize(){return 20.0F;}
    protected float getSaturnRingRotation(){return 17.0F;}
}
