package com.mod.htgt6.common.TE.mechanics;


import com.mod.htgt6.common.TE.UniversalGasTurbineTE;

public class ConfigureGasFuels {

    public static void initFuels() {

        // ==========================================
        // GAS FUEL REGISTRATION
        // ==========================================
        // Format: UniversalGasTurbineTE.addGasFuel("fluid_registry_name", EU_per_mB);

        // Example IC2 / Generic Forge Gases
        UniversalGasTurbineTE.addGasFuel("ic2biogas", 16);
        UniversalGasTurbineTE.addGasFuel("hydrogen", 15);
        UniversalGasTurbineTE.addGasFuel("methane", 45);
        UniversalGasTurbineTE.addGasFuel("carbonmonoxide", 18);

        // Advanced/Dense Gases
        UniversalGasTurbineTE.addGasFuel("ethane", 60);
        UniversalGasTurbineTE.addGasFuel("ethylene", 70);
        UniversalGasTurbineTE.addGasFuel("propane", 85);
        UniversalGasTurbineTE.addGasFuel("butane", 100);
        UniversalGasTurbineTE.addGasFuel("heliumhydrogen", 320);

        // Custom HTGT6 Gases (Replace with your actual fluid names)
        // UniversalGasTurbineTE.addGasFuel("htgt6_custom_gas", 120);
    }
}