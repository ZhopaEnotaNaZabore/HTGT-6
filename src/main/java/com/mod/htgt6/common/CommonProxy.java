package com.mod.htgt6.common;

import com.mod.htgt6.common.achievements.Hitech6AchieveDatabase;
import com.mod.htgt6.common.block.greg.technological.hulls.blockHullDatabase;
import com.mod.htgt6.common.handler.*;
import com.mod.htgt6.common.handler.biometweaks.biomeLukewarmOcean;
import com.mod.htgt6.common.handler.worldgeneration.shipwerk.structurehandler.OceanStructureHandler;
import com.mod.htgt6.common.item.IC2.miscIC2;
import com.mod.htgt6.common.item.greg.circuits.CiruitParts;
import com.mod.htgt6.common.item.greg.materials.firecly.fireclys;
import com.mod.htgt6.common.item.greg.technological.sieves.sieves;
import com.mod.htgt6.common.item.htgt6.circuits.CP2;
import com.mod.htgt6.common.item.htgt6.circuits.CircuitsAndParts;
import com.mod.htgt6.common.item.htgt6.coins.HiTech6Coins;
import com.mod.htgt6.common.item.htgt6.fluids.FluidItemsSub;
import com.mod.htgt6.common.item.htgt6.fluids.FluidsHTGT6;
import com.mod.htgt6.common.item.htgt6.govnoebanoe.misscellouse;
import com.mod.htgt6.common.item.htgt6.monocrystalls.crystalls;
import com.mod.htgt6.common.item.htgt6.rocketcomputers.HiTech6RocketTerminals;
import com.mod.htgt6.common.item.htgt6.weapons.HiTech6Weapons;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        com.mod.htgt6.common.handler.recipe.AssemblerRecipes.init();
        ModItems.register();
        ModBlocks.register();
        FluidDatabase.register();
        HiTech6Coins.CoinItemStack();
        HiTech6RocketTerminals.RocketComputers();
        fireclys.Fireclys();
        sieves.Sieves();
        CiruitParts.CircuitPartsDatabase();
        miscIC2.IC2miscItems();
        CircuitsAndParts.CP();
        misscellouse.GVN();
        HiTech6Weapons.weapons();
        crystalls.Monocrystals();
        CP2.logisticsCP2();
        FluidsHTGT6.initialize();
        FluidItemsSub.InitFluidSubItems();
        blockHullDatabase.InitiHulls();


    }

    public void init(FMLInitializationEvent event) {
        Hitech6AchieveDatabase.initialization();
        BiomeGenBase LukewarmOcean = new biomeLukewarmOcean(120);
        BiomeDictionary.registerBiomeType(LukewarmOcean,
                BiomeDictionary.Type.OCEAN,
                BiomeDictionary.Type.WATER,
                BiomeDictionary.Type.HOT
        );
        BiomeManager.addSpawnBiome(LukewarmOcean);
        BiomeManager.addBiome(BiomeManager.BiomeType.WARM, new BiomeManager.BiomeEntry(LukewarmOcean, 50));
        MinecraftForge.TERRAIN_GEN_BUS.register(new OceanStructureHandler());


    }
    public void postInit(FMLPostInitializationEvent event) {
        recipes.registerRecipes();


    }
}
