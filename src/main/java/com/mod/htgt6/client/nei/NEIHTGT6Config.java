package com.mod.htgt6.client.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.mod.htgt6.client.nei.GasTurbineRecipeHandler;

public class NEIHTGT6Config implements IConfigureNEI {

    @Override
    public void loadConfig() {
        // Create an explicit static link to guarantee initialization
        registerManual();
    }

    // Force-registration fallback method
    public static void registerManual() {
        GasTurbineRecipeHandler handler = new GasTurbineRecipeHandler();
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);

        System.out.println("[HTGT6] NEI Gas Turbine integration SUCCESSFULLY loaded via manual proxy hook.");
    }

    @Override
    public String getName() {
        return "HTGT6 Gas Turbine Plugin";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}