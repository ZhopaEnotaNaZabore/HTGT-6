package com.mod.htgt6.api.hitech6.fuelconfigs;


import com.mod.htgt6.common.TE.UniversalGasTurbineTE;
import com.mod.htgt6.common.handler.FluidDatabase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class ConfigureGasFuels {

    public static void initFuels() {
        UniversalGasTurbineTE.clearGasFuels();

        // Original HTGT6 turbine balance.
        registerIfPresent("ic2biogas", 16);
        registerIfPresent("hydrogen", 15);
        registerIfPresent("methane", 45);
        registerIfPresent("carbonmonoxide", 18);
        registerIfPresent("ethane", 60);
        registerIfPresent("liquidethanemethane", 60);
        registerIfPresent("ethylene", 70);
        registerIfPresent("propane", 85);
        registerIfPresent("butane", 100);
        registerIfPresent("heliumhydrogen", 320);

        // Additional existing chemical gases.
        registerIfPresent("propylene", 256);
        registerIfPresent("benzene", 300);
        registerIfPresent("phenol", 214);
        registerIfPresent("toluene", 301);

        // Natural gas is deliberately inexpensive because it is renewable and
        // effectively infinite in the assembled modpack.
        registerIfPresent("gas_natural_gas", 22);
        registerIfPresent("naturalgas", 22);
        registerIfPresent("gas.natural", 22);

        // Native HTGT6 advanced gases, registered during preInit by FluidDatabase.
        registerIfPresent(FluidDatabase.NETHER_GAS, 1024);
        registerIfPresent(FluidDatabase.NITRO_BENZENE, 1412);
        registerIfPresent(FluidDatabase.NAQUADAH_GAS, 3725);
        registerIfPresent(FluidDatabase.NAQUADRIA_GAS, 10200);
        registerIfPresent(FluidDatabase.XENOXEN, 14893);
    }

    /** Do not expose fuels which are absent from the assembled modpack. */
    private static void registerIfPresent(String fluidName, int euPerMb) {
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        registerIfPresent(fluid, euPerMb);
    }

    private static void registerIfPresent(Fluid fluid, int euPerMb) {
        if (fluid != null) UniversalGasTurbineTE.addGasFuel(fluid.getName(), euPerMb);
    }
}
