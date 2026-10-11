package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public final class WorldProviderTethys extends AbstractSaturnMoonProvider {
    public CelestialBody getCelestialBody(){return SaturnMoonWorlds.tethys;}
    public Class<? extends IChunkProvider> getChunkProviderClass(){return ChunkProviderTethys.class;}
    public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerTethys.class;}
    public String getDimensionName(){return "Tethys";}
    public float getGravity(){return 0.075F;}
    public float getFallDamageModifier(){return 0.04F;}
    public float getThermalLevelModifier(){return -4.675F;}
    public long getDayLength(){return 163106L;}
    public boolean canSpaceshipTierPass(int tier){return tier>=5;}
    protected float getSaturnHorizontalAngle(){return 38.0F;}
    protected float getSaturnVerticalAngle(){return 29.0F;}
    protected float getSaturnSkySize(){return 32.0F;}
    protected float getSaturnRingRotation(){return 11.0F;}
}
