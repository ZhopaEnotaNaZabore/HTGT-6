package com.mod.htgt6.common.handler;


import com.mod.htgt6.common.TE.*;
import com.mod.htgt6.common.block.glass.BoronGlass1;
import com.mod.htgt6.common.block.hitech6.*;
import com.mod.htgt6.common.block.info.*;
import com.mod.htgt6.common.block.technological.PCcube;
import com.mod.htgt6.common.chemical.ChemicalPlantContent;
import com.mod.htgt6.common.electronics.ElectronicFactoryContent;
import com.mod.htgt6.common.mining.MiningUnitContent;
import com.mod.htgt6.common.mixer.IndustrialMixerContent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;


public class ModBlocks {

    private static final BoronGlass1 BORON_GLASS_1 = new BoronGlass1();
    private static final PCcube P_CCUBE = new PCcube();
    private static final BlockAssembler BLOCK_ASSEMBLER = new BlockAssembler();
    private static final BlockSuperMasicCompressor BLOCK_SUPER_MASIC_COMPRESSOR = new BlockSuperMasicCompressor();
    private static final BlockUniversalGasTurbine BLOCK_UNIVERSAL_GAS_TURBINE = new BlockUniversalGasTurbine();
    private static final BlockMagicEnergyGenerator BLOCK_MAGIC_ENERGY_GENERATOR  = new BlockMagicEnergyGenerator();
private static final BlockRadioisotopeGenerator BLOCK_RADIOISOTOPE_GENERATOR = new BlockRadioisotopeGenerator();
private static final BlockUniversalEngine BLOCK_UNIVERSAL_ENGINE = new BlockUniversalEngine();
private static final BlockFluidSolidifier BLOCK_FLUID_SOLIDIFIER = new BlockFluidSolidifier();

    // New MiningUnit and Port Blocks

    public static void register() {

        //регистр TileEntity
        GameRegistry.registerTileEntity(TileEntityAssembler.class, "htgt6.Assembler");

       GameRegistry.registerBlock(BLOCK_ASSEMBLER, MachineItemBlock.class, "assembler");




        //Регистр просто блоков
        GameRegistry.registerBlock(BLOCK_FLUID_SOLIDIFIER, MachineItemBlock.class,"BlockSolidifier");
        GameRegistry.registerBlock(BLOCK_UNIVERSAL_ENGINE, MachineItemBlock.class, "UniversalEngineBlock");
        GameRegistry.registerBlock(BLOCK_RADIOISOTOPE_GENERATOR, MachineItemBlock.class,"RadioISOGENblock");
        GameRegistry.registerBlock(BLOCK_UNIVERSAL_GAS_TURBINE, MachineItemBlock.class,"GasTurbineTE");
        GameRegistry.registerBlock(BORON_GLASS_1, "BoronGlass1");
        GameRegistry.registerBlock(P_CCUBE, "PCcube");
        GameRegistry.registerBlock(BLOCK_SUPER_MASIC_COMPRESSOR, MachineItemBlock.class, "SMCompressor");
        GameRegistry.registerBlock(BLOCK_MAGIC_ENERGY_GENERATOR, MachineItemBlock.class, "MGblock");
        // Append new IDs after every legacy HTGT6 block to preserve existing worlds.
        MiningUnitContent.register();
        ChemicalPlantContent.register();
        ElectronicFactoryContent.register();
        IndustrialMixerContent.register();
        com.mod.htgt6.common.flotation.FlotationChamberContent.register();
        com.mod.htgt6.common.centrifuge.IndustrialCentrifugeContent.register();
        com.mod.htgt6.common.crusher.IndustrialCrusherContent.register();
        SuperMassiveCompressorContent.register();
        FluidSolidifierContent.register();
        com.mod.htgt6.common.multiblock.glass.ObservationGlassContent.register();
        com.mod.htgt6.common.turbine.SteamTurbineContent.register();
    }

    public static Block getSuperMassiveCompressorController() {
        return BLOCK_SUPER_MASIC_COMPRESSOR;
    }

    public static Block getFluidSolidifierController() { return BLOCK_FLUID_SOLIDIFIER; }
}
