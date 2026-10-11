package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public final class WorldProviderIapetus extends AbstractSaturnMoonProvider {
    public CelestialBody getCelestialBody(){return SaturnMoonWorlds.iapetus;}
    public Class<? extends IChunkProvider> getChunkProviderClass(){return ChunkProviderIapetus.class;}
    public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerIapetus.class;}
    public String getDimensionName(){return "Iapetus";}
    public float getGravity(){return 0.070F;}
    public float getFallDamageModifier(){return 0.065F;}
    public float getThermalLevelModifier(){return -4.175F;}
    public long getDayLength(){return 685337L;}
    public boolean canSpaceshipTierPass(int tier){return tier>=7;}
    protected float getSaturnHorizontalAngle(){return 71.0F;}
    protected float getSaturnVerticalAngle(){return 17.0F;}
    protected float getSaturnSkySize(){return 12.0F;}
    protected float getSaturnRingRotation(){return -21.0F;}
}
