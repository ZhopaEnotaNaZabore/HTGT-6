package com.mod.htgt6.api.hitech6.fuelconfigs;



import com.mod.htgt6.common.TE.UniversalEngineTE;
import net.minecraft.init.Items;

public class ConfigureEngineFuels {

    public static void initFuels() {
        // ==========================================
        // 1. SEMIFUEL CATEGORY
        // ==========================================
        UniversalEngineTE.addSemifuel("biomass", 8);
        UniversalEngineTE.addSemifuel("oil", 16);
        UniversalEngineTE.addSemifuel("semifuel", 32);

        // ==========================================
        // 2. DIESEL CATEGORY
        // ==========================================
        UniversalEngineTE.addDiesel("fuel", 128); // BuildCraft/Galacticraft fuel
        UniversalEngineTE.addDiesel("diesel", 256);
        UniversalEngineTE.addDiesel("ethanol", 192);
        UniversalEngineTE.addDiesel("biodiesel", 192);
        //DEV ONLY
        UniversalEngineTE.addDiesel("enrichednitroFuel", 4192631);
        UniversalEngineTE.addFalFuel("admfluid", 12000000);



        // ==========================================
        // 3. FAL (ROCKET FUEL) CATEGORY
        // ==========================================
        UniversalEngineTE.addFalFuel("rocket_fuel", 512);
       // UniversalEngineTE.addFalFuel("fal_fuel", 1024);

        // 4. Chemical fuels
        UniversalEngineTE.addChemicalFuel("creosote", 24);
        UniversalEngineTE.addChemicalFuel("benzene", 300);

        // 5. GeoThermal fuels
        UniversalEngineTE.addGeothermalFuel("lava", 16);

        // 6. Solid fuels (total EU per consumed item)
        UniversalEngineTE.addSolidFuel(Items.coal, 0, 3200L);
        UniversalEngineTE.addSolidFuel(Items.coal, 1, 3200L); // charcoal
        UniversalEngineTE.addSolidFuel(Items.blaze_rod, 0, 2400L);
    }
}
