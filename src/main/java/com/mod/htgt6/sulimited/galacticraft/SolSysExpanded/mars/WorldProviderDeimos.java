package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.mars;

import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysExpanded;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public final class WorldProviderDeimos extends AbstractMarsMoonProvider {
    public CelestialBody getCelestialBody(){return SolSysExpanded.deimos;} public Class<? extends IChunkProvider> getChunkProviderClass(){return ChunkProviderDeimos.class;} public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerDeimos.class;} public String getDimensionName(){return "Deimos";} public float getGravity(){return 0.031F;} public float getFallDamageModifier(){return 0.018F;} public float getThermalLevelModifier(){return -1.7F;} public long getDayLength(){return 109080L;} public boolean canSpaceshipTierPass(int tier){return tier>=2;} protected float marsHorizontalAngle(){return 61;} protected float marsVerticalAngle(){return 20;} protected float marsSize(){return 18;}
}
