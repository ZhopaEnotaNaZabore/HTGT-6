package com.mod.htgt6.sulimited.galacticraft;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * Compatibility fallback for external AmunRa providers still compiled against
 * the legacy fixed manager. Native Ra providers use their planet-specific
 * SpaceUltimate managers.
 */
public final class AmunRaWorldChunkManager extends WorldChunkManagerSpace {
    @Override
    public BiomeGenBase getBiome() {
        return BiomeGenSpaceUltimate.spaceUltimate;
    }
}
