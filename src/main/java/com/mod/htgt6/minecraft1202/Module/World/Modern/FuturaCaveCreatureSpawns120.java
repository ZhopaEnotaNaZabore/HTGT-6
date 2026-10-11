package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAxolotl120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityGlowSquid120;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.biome.BiomeGenBase;

/** 1.20 cave water-creature tables routed through Futura's 3D biome lookup. */
public final class FuturaCaveCreatureSpawns120 {
    public static final List LUSH_WATER=Collections.unmodifiableList(Arrays.asList(
            new BiomeGenBase.SpawnListEntry(EntityAxolotl120.class,10,4,6),
            new BiomeGenBase.SpawnListEntry(EntityGlowSquid120.class,10,4,6)));
    public static final List DRIPSTONE_WATER=Collections.singletonList(
            new BiomeGenBase.SpawnListEntry(EntityGlowSquid120.class,10,4,6));
    private FuturaCaveCreatureSpawns120(){}
}
