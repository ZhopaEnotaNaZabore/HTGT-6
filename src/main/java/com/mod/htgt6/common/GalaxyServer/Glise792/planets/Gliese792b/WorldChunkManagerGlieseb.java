package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b;

import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.biomeGliese792b.BiomeGenBaseGlieseB;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;
import net.minecraft.world.biome.BiomeGenBase;

public class WorldChunkManagerGlieseb extends WorldChunkManagerSpace {
    public WorldChunkManagerGlieseb() {}
    public BiomeGenBase getBiome() {
        return BiomeGenBaseGlieseB.glieseb;
    }
}
