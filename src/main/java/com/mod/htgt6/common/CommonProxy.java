package com.mod.htgt6.common;

import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.blocks.GlieseCwater;
import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.blocks.blocksGliese792C;
import com.mod.htgt6.common.achievements.HiTecch6AchivementsNEW;
import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.blocks.B_database;
import com.mod.htgt6.common.block.greg.technological.hulls.blockHullDatabase;
import com.mod.htgt6.common.handler.*;
import com.mod.htgt6.common.handler.biometweaks.biomeLukewarmOcean;
import com.mod.htgt6.common.handler.recipe.assembler.AssemblerRecipeHandler;
import com.mod.htgt6.common.handler.recipe.compressor.CompressorRecipeHandler;
import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
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


import com.mod.htgt6.common.prospector.ItemGT6BulkScanner;
import com.mod.htgt6.common.prospector.ItemGT6Scanner;
import com.mod.htgt6.server.utils.ServerLagDebugger;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

import gregapi.api.Abstract_Proxy;
import net.minecraft.item.Item;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;






public class CommonProxy extends Abstract_Proxy {
    public void preInit(FMLPreInitializationEvent event) {
        AssemblerRecipeHandler.registerRecipes();
        CompressorRecipeHandler.registerSMCrecipes();
        Tmodules.ModifiersAndTransformers();
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
        B_database.initGLiese792Bblocks();
        blocksGliese792C.InitGlieseCblocks();
        GlieseCwater.init();
        registerPackets();
        prospectorScanner = new ItemGT6Scanner();
        bulkprospector = new ItemGT6BulkScanner();
        com.mod.htgt6.common.TE.mechanics.ConfigureGasFuels.initFuels();


    }
    public static Item prospectorScanner;
    public static Item bulkprospector;

    public void init(FMLInitializationEvent event) {
        HiTecch6AchivementsNEW.initialization();
        BiomeGenBase LukewarmOcean = new biomeLukewarmOcean(120);
        BiomeDictionary.registerBiomeType(LukewarmOcean,
                BiomeDictionary.Type.OCEAN,
                BiomeDictionary.Type.WATER,
                BiomeDictionary.Type.HOT
        );
        BiomeManager.addSpawnBiome(LukewarmOcean);
        BiomeManager.addBiome(BiomeManager.BiomeType.WARM, new BiomeManager.BiomeEntry(LukewarmOcean, 50));
        MinecraftForge.TERRAIN_GEN_BUS.register(new OceanStructureHandler());
        AssemblerRecipeHandler.registerRecipes();


    }
    public void postInit(FMLPostInitializationEvent event) {
        recipes.registerRecipes();
    }


    private void registerPackets() {}
    public void registerNEI() {}
    }


