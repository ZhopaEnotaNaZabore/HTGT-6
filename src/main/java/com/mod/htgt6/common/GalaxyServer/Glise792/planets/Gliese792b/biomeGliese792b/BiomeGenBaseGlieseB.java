package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.biomeGliese792b;

import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenBaseGlieseB extends BiomeGenBase {
    public static final BiomeGenBase glieseb;

    public BiomeGenBaseGlieseB(int var1) {
        super(var1);
        this.spawnableMonsterList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCreatureList.clear();
    }

    @Override
    public BiomeGenBase setColor(int var1) {
        return super.setColor(var1); // Removed incorrect cast to BiomeGenBaseMars
    }

    static {
        glieseb = (new BiomeGenBaseGlieseB(ConfigManagerCore.biomeIDbase + 1)).setBiomeName("glieseb");
    }
}