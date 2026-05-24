package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.biomeGliese792b;


import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

public class BiomeGenGlieseb extends BiomeGenBaseGlieseB {

    public BiomeGenGlieseb(int par1) {
        super(par1);
        this.setBiomeName("glieseb");
        this.setColor(16711680);
        this.setHeight(new BiomeGenBase.Height(0.1F, 0.2F)); // Adjusted to a standard flat planet height

        // Registering types for cross-mod compatibility
        BiomeDictionary.registerBiomeType(this,
                BiomeDictionary.Type.COLD,
                BiomeDictionary.Type.DRY,
                BiomeDictionary.Type.DEAD,
                BiomeDictionary.Type.SANDY
        );
    }
}