package com.mod.htgt6.minecraft1202.Module.Data.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockKelp;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanAquaticHandler;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeAdapter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.OverworldBiomeParameterMap120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanBiomeRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;

/** Update Aquatic identities/fluid flora contract inside the translated Futura world. */
public final class OceanFuturaCompatibilityFixture120 {
    public static void assertContract(){
        require(FuturaMountainDensityRouter120.SEA_LEVEL==127,
                "Futura ocean sea level lost the modern Y+64 translation");
        climate(OceanBiomeRegistry.warmOcean,.5F,0x43D5EE,"warm_ocean");
        climate(OceanBiomeRegistry.deepWarmOcean,.5F,0x43D5EE,"deep_warm_ocean");
        climate(OceanBiomeRegistry.lukewarmOcean,.5F,0x45ADF2,"lukewarm_ocean");
        climate(OceanBiomeRegistry.deepLukewarmOcean,.5F,0x45ADF2,"deep_lukewarm_ocean");
        climate(OceanBiomeRegistry.coldOcean,.5F,0x3D57D6,"cold_ocean");
        climate(OceanBiomeRegistry.deepColdOcean,.5F,0x3D57D6,"deep_cold_ocean");
        climate(OceanBiomeRegistry.deepFrozenOcean,0.5F,0x3938C9,"deep_frozen_ocean adapter");

        require(FuturaBiomeAdapter120.resolve(OverworldBiomeParameterMap120.Biome.WARM_OCEAN)
                ==OceanBiomeRegistry.warmOcean,"Futura warm ocean identity is disconnected");
        require(FuturaBiomeAdapter120.resolve(OverworldBiomeParameterMap120.Biome.DEEP_COLD_OCEAN)
                ==OceanBiomeRegistry.deepColdOcean,"Futura deep cold ocean identity is disconnected");
        require(FuturaBiomeAdapter120.resolve(OverworldBiomeParameterMap120.Biome.DEEP_FROZEN_OCEAN)
                ==OceanBiomeRegistry.deepFrozenOcean,"Futura deep frozen ocean identity is disconnected");

        require(BlockKelp.isOpenWater(Blocks.water),"Vanilla source water rejected aquatic flora");
        require(BlockKelp.isOpenWater(Backport120Content.aquiferWater),
                "Futura stable aquifer water rejected aquatic flora");
        require(!BlockKelp.isOpenWater(OceanContent.kelp)
                        &&!BlockKelp.isOpenWater(OceanContent.kelpPlant)
                        &&!BlockKelp.isOpenWater(OceanContent.marinePlant),
                "Aquatic placed features can overwrite existing marine flora");
        require(OceanAquaticHandler.aquaticVegetationCount(OceanBiomeRegistry.warmOcean)==80
                        &&OceanAquaticHandler.aquaticVegetationCount(OceanBiomeRegistry.lukewarmOcean)==80
                        &&OceanAquaticHandler.aquaticVegetationCount(OceanBiomeRegistry.coldOcean)==32
                        &&OceanAquaticHandler.aquaticVegetationCount(OceanBiomeRegistry.deepColdOcean)==40
                        &&OceanAquaticHandler.aquaticVegetationCount(BiomeGenBase.ocean)==48
                        &&OceanAquaticHandler.aquaticVegetationCount(BiomeGenBase.frozenOcean)==0,
                "Ocean vegetation counts differ from the 1.20.2 placed features");
    }

    private static void climate(BiomeGenBase biome,float temperature,int waterColor,String name){
        require(biome!=null,name+" was not registered");
        require(Float.floatToIntBits(biome.temperature)==Float.floatToIntBits(temperature),
                name+" temperature differs from the 1.20.2 contract");
        require(Float.floatToIntBits(biome.rainfall)==Float.floatToIntBits(.5F),
                name+" downfall differs from the 1.20.2 contract");
        require(biome.waterColorMultiplier==waterColor,name+" water colour differs from 1.20.2");
    }

    public static void main(String[] ignored){
        assertContract();
        System.out.println("Update Aquatic/Futura fixture OK: ocean identities, Y+64, aquifer flora");
    }

    private static void require(boolean condition,String message){
        if(!condition)throw new IllegalStateException(message);
    }

    private OceanFuturaCompatibilityFixture120(){}
}
