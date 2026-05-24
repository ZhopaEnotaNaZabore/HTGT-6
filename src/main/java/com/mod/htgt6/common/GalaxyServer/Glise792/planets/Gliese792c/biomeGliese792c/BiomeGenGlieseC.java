package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.biomeGliese792c;




import net.minecraft.world.biome.BiomeGenBase;

import net.minecraftforge.common.BiomeDictionary;

public class BiomeGenGlieseC extends BiomeGenBaseGlieseC {


    public BiomeGenGlieseC(int par1) {
        super(par1);
        this.setBiomeName("glieseC");
        this.setColor(16711680);
        this.setHeight(new BiomeGenBase.Height(0.1F, 0.2F));

        BiomeDictionary.registerBiomeType(this,

                BiomeDictionary.Type.OCEAN,
                BiomeDictionary.Type.BEACH,
                BiomeDictionary.Type.WATER,
                BiomeDictionary.Type.WET,
                BiomeDictionary.Type.LUSH
        );
    }
}