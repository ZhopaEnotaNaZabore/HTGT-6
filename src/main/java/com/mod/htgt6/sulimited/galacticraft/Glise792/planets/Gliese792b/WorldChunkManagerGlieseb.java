package com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b;


import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;
import net.minecraft.world.biome.BiomeGenBase;

public class WorldChunkManagerGlieseb extends WorldChunkManagerSpace {
    public WorldChunkManagerGlieseb() {}
   // public BiomeGenBase getBiome() {
   //     return BiomeGenBaseGlieseB.glieseb;
  //  }

    @Override
    public BiomeGenBase getBiome() {
        // Now returns the shared wrapper biome
        return BiomeGenSpaceUltimate.spaceUltimate;
    }
}
