package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.mars;

import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysExpanded;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public final class WorldProviderPhobos extends AbstractMarsMoonProvider {
    public CelestialBody getCelestialBody(){return SolSysExpanded.phobos;} public Class<? extends IChunkProvider> getChunkProviderClass(){return ChunkProviderPhobos.class;} public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerPhobos.class;} public String getDimensionName(){return "Phobos";} public float getGravity(){return 0.058F;} public float getFallDamageModifier(){return 0.035F;} public float getThermalLevelModifier(){return -1.4F;} public long getDayLength(){return 27552L;} public boolean canSpaceshipTierPass(int tier){return tier>=2;} protected float marsHorizontalAngle(){return -43;} protected float marsVerticalAngle(){return 29;} protected float marsSize(){return 34;}
}
