package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public final class WorldProviderMimas extends AbstractSaturnMoonProvider {
    public CelestialBody getCelestialBody(){return SaturnMoonWorlds.mimas;}
    public Class<? extends IChunkProvider> getChunkProviderClass(){return ChunkProviderMimas.class;}
    public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerMimas.class;}
    public String getDimensionName(){return "Mimas";}
    public float getGravity(){return 0.078F;}
    public float getFallDamageModifier(){return 0.025F;}
    public float getThermalLevelModifier(){return -4.9F;}
    public long getDayLength(){return 81389L;}
    public boolean canSpaceshipTierPass(int tier){return tier>=5;}
    protected float getSaturnHorizontalAngle(){return -52.0F;}
    protected float getSaturnVerticalAngle(){return 34.0F;}
    protected float getSaturnSkySize(){return 42.0F;}
    protected float getSaturnRingRotation(){return -8.0F;}
}
