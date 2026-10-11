package com.mod.htgt6.common;

import com.mod.htgt6.sulimited.SULimitedContent;
import com.mod.htgt6.api.hitech6.fuelconfigs.ConfigureEngineFuels;
import com.mod.htgt6.api.hitech6.fuelconfigs.ConfigureGasFuels;
import com.mod.htgt6.api.hitech6.fuelconfigs.ConfigureMagicFuels;
import com.mod.htgt6.api.hitech6.fuelconfigs.RadioISOfuelConfig;
import com.mod.htgt6.common.achievements.HiTecch6AchivementsNEW;
import com.mod.htgt6.common.GTHooks.MisscelouseGT6Hooks.HighTierComponents;
import com.mod.htgt6.common.GTHooks.WireHooks.TieredSuperconductorWireHooks;
import com.mod.htgt6.common.GTHooks.TurbineRotorHooks.GT6TurbineRotorHooks;
import com.mod.htgt6.server.utils.Netty.NetworkOptimizer;
import com.mod.htgt6.common.block.greg.technological.hulls.blockHullDatabase;
import com.mod.htgt6.common.handler.*;

import com.mod.htgt6.common.handler.recipe.RM.*;
import com.mod.htgt6.common.handler.recipe.RecipeRemover;
import com.mod.htgt6.common.item.htgt6.Tools.HiTech6Molds;
import com.mod.htgt6.common.item.htgt6.Tools.HiTech6Tools;
import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
import com.mod.htgt6.common.item.IC2.miscIC2;
import com.mod.htgt6.common.item.greg.circuits.CiruitParts;
import com.mod.htgt6.common.item.greg.materials.firecly.fireclys;
import com.mod.htgt6.common.item.greg.technological.sieves.sieves;
import com.mod.htgt6.common.item.htgt6.circuits.CP2;
import com.mod.htgt6.common.item.htgt6.circuits.CircuitsAndParts;
import com.mod.htgt6.common.item.htgt6.coins.HiTech6Coins;
import com.mod.htgt6.common.item.htgt6.fluids.dustsDatabase;
import com.mod.htgt6.common.item.htgt6.fluids.FluidsHTGT6;
import com.mod.htgt6.common.item.htgt6.govnoebanoe.misscellouse;
import com.mod.htgt6.common.item.htgt6.monocrystalls.crystalls;
import com.mod.htgt6.common.item.htgt6.rocketcomputers.HiTech6RocketTerminals;
import com.mod.htgt6.common.item.htgt6.weapons.HiTech6Weapons;
import com.mod.htgt6.client.vanillatweaks.CHAT.TitleAchievementWatcher;


import com.mod.htgt6.common.prospector.ItemGT6BulkScanner;
import com.mod.htgt6.common.prospector.ItemGT6Scanner;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import gregapi.api.Abstract_Proxy;
import gregapi.data.MT;
import gregapi.data.OP;
import net.minecraft.item.Item;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;






public class CommonProxy extends Abstract_Proxy {
    public void preInit(FMLPreInitializationEvent event) {
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
        dustsDatabase.InitFluidSubItems();
        // Keep the original registration position: 1.7.10 worlds persist
        // numeric block IDs, so moving this earlier would remap existing saves.
        blockHullDatabase.InitiHulls();
        SULimitedContent.registerLegacyWorldBlocks();
        // New hull variants are deliberately appended after all existing
        // blocks, so upgrading a world cannot shift legacy numeric IDs.
        blockHullDatabase.registerAdditionalHulls();
        registerPackets();
        HiTech6Molds.initMolds();
        prospectorScanner = new ItemGT6Scanner();
        bulkprospector = new ItemGT6BulkScanner();
        HiTech6Tools.InitHiTech6Tools();
        ModItems.registerReinforcedCompositePlates();
        com.mod.htgt6.common.item.greg.circuits.HighTierCircuits.register();


    }
    public static Item prospectorScanner;
    public static Item bulkprospector;


    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.common.crusher.CrusherConveyor.Events());
        TieredSuperconductorWireHooks.scheduleAfterGTInit();
        new NetworkOptimizer();
        new TitleAchievementWatcher();
        HiTecch6AchivementsNEW.initialization();
        // Registered once, after every block used as a recipe output exists.
        AssemblerRecipeHandler.registerRecipes();
        ConfigureMagicFuels.initFuels();
        // Every mod has completed preInit here, so optional gas fluids can be
        // validated against the final Forge fluid registry.
        ConfigureGasFuels.initFuels();
        RadioISOfuelConfig.init();
        simpleRM.init();
        ConfigureEngineFuels.initFuels();
        SolidifierRecipeHandler.registerRecipes();

        LineAssemblerRecipeHandler.INSTANCE.registerRecipes();
        ElectromagneticProcessorRecipeMap.INSTANCE.registerRecipes();


    }
    public void postInit(FMLPostInitializationEvent event) {
        HighTierComponents.bindMissingGT6Aliases();
        GT6TurbineRotorHooks.registerRecipes();
        // GT6 deliberately moves itself to the end of the init mod list. Its
        // pipe/wire MultiTileEntities therefore do not exist when HTGT6.init()
        // runs; resolve Metal Former outputs only after GT6 init has finished.
        MetalFormerRecipeMap.INSTANCE.registerRecipes();
        ElectronicFactoryRecipeHandler.INSTANCE.importGT6CircuitAssemblerRecipes();
        // Processing-map copies are made in loadComplete after PGM extraction is normalized.
        RecipeRemover.init();
    }


    public void registerClientOnlyHandlers() {}

    private void registerPackets() {}
    public void registerNEI() {}
    /** Client proxy toggles the visual projection; dedicated servers intentionally do nothing. */
    public void toggleMultiblockHologram(World world, int x, int y, int z, EntityPlayer player) {}
    }


