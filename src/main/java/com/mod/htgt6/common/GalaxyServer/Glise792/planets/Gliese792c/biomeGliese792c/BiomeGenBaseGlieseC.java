package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.biomeGliese792c;



import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;

import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenBaseGlieseC extends BiomeGenBase
  {

    // =========================
    // MAIN BIOME INSTANCE
    // =========================

    public static final BiomeGenBase gliesec;

    public  BiomeGenBaseGlieseC (int var1) {
        super(var1);
        this.spawnableMonsterList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCreatureList.clear();

        this.setTemperatureRainfall(0.8F, 0.4F);
        this.setEnableSnow();
        this.enableSnow = false;
    }


      @Override
      public BiomeGenBase setColor(int var1) {
          return super.setColor(var1);
      }

      static {

          gliesec = (new BiomeGenBaseGlieseC(ConfigManagerCore.biomeIDbase + 2)).setBiomeName("Gliese_c");

      }
}