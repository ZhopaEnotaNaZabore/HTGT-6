package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class GlieseCwater {

    public static Fluid FluidGlieseCwater;
    public static Block blockGlieseCwaterStill;
    public static Block blockGlieseCwaterFlowing;

    public static void init() {
        // 1. Create and register the Fluid instance
        FluidGlieseCwater = new Fluid("GlieseCwater")
                .setLuminosity(0)
                .setDensity(1000)     // Matches vanilla water density
                .setViscosity(1000)   // Matches vanilla water speed
                .setGaseous(false);
        FluidRegistry.registerFluid(FluidGlieseCwater);

        // 2. Instantiate your custom blocks with correct class names
        blockGlieseCwaterStill = new blockFluidStillGliese792c(FluidGlieseCwater)
                .setBlockName("blockGlieseCwaterStill");

        blockGlieseCwaterFlowing = new blockFluidflowGliese792c(FluidGlieseCwater)
                .setBlockName("blockGlieseCwaterFlowing");

        // 3. Register blocks with the GameRegistry
        GameRegistry.registerBlock(blockGlieseCwaterStill, "Gliese_C_water_still");
        GameRegistry.registerBlock(blockGlieseCwaterFlowing, "Gliese_C_water_flowing");

        // 4. Bind the fluid to its blocks so Forge recognizes them
        FluidGlieseCwater.setBlock(blockGlieseCwaterStill);
    }
}