package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public final class WorldProviderDione extends AbstractSaturnMoonProvider {
    public CelestialBody getCelestialBody(){return SaturnMoonWorlds.dione;}
    public Class<? extends IChunkProvider> getChunkProviderClass(){return ChunkProviderDione.class;}
    public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerDione.class;}
    public String getDimensionName(){return "Dione";}
    public float getGravity(){return 0.073F;}
    public float getFallDamageModifier(){return 0.055F;}
    public float getThermalLevelModifier(){return -4.65F;}
    public long getDayLength(){return 236469L;}
    public boolean canSpaceshipTierPass(int tier){return tier>=6;}
    protected float getSaturnHorizontalAngle(){return 126.0F;}
    protected float getSaturnVerticalAngle(){return 25.0F;}
    protected float getSaturnSkySize(){return 26.0F;}
    protected float getSaturnRingRotation(){return -14.0F;}
}
