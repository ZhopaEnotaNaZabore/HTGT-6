package com.mod.htgt6.common.handler.recipes;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3.GeneratedCrafting3x3;
import gregapi.data.MT;
import gregapi.oredict.OreDictMaterial;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Material variants have explicit cores; metadata order is not a voltage tier. */
public final class ThermalMachineCraftingRecipes {
    private static final Logger LOG = LogManager.getLogger("HTGT6-ThermalCrafting");
    private static final OreDictMaterial[] MATERIALS = {
        MT.Pb, MT.Bi, MT.Bronze, MT.Invar, MT.Steel, MT.Cr, MT.Ti,
        MT.W, MT.TungstenSteel, MT.Ta4HfC5, MT.GT6U.Nb2Ti3C4, MT.Netherite
    };
    private static final String FIRECLAY = "<HTGT6:FireclayBrick>";

    private ThermalMachineCraftingRecipes() {}

    public static void register() {
        for (int material = 0; material < MATERIALS.length; material++) {
            for (int dense = 0; dense < 2; dense++) {
                int offset = dense * 50;
                String plate = ore(dense == 0 ? "plate" : "plateQuintuple", material);
                String copper = "<ore:" + (dense == 0 ? "plateDouble" : "plateDense") + "Copper>";
                String pipe = ore(dense == 0 ? "pipeSmall" : "pipeLarge", material);
                if (material >= 2) {
                    // Normal chambers convert the matching solid box. Dense chambers
                    // upgrade their own normal version and retain larger piping.
                    replace(1400 + offset + material, new String[][] {
                        {plate, copper, plate},
                        {pipe, multi((dense == 0 ? 1100 : 1400) + material), pipe},
                        {FIRECLAY, FIRECLAY, FIRECLAY}
                    });
                    replace(1600 + offset + material, new String[][] {
                        {plate, copper, plate},
                        {FIRECLAY, multi((dense == 0 ? 1100 : 1600) + material), FIRECLAY},
                        {FIRECLAY, pipe, FIRECLAY}
                    });
                }
                replace(9000 + offset + material, new String[][] {
                    {plate, copper, plate},
                    {ore("plateCurved", material), multi((dense == 0 ? 1100 : 9000) + material), ore("plateCurved", material)},
                    {FIRECLAY, ore("rotor", material), FIRECLAY}
                });
            }
        }
        // Boilers use a different metadata mapping: 1209 is Netherite.
        int[] boilerMaterials = {0, 1, 2, 3, 4, 5, 6, 7, 8, 11};
        for (int variant = 0; variant < boilerMaterials.length; variant++) {
            String plate = ore("plateDense", boilerMaterials[variant]);
            replace(1250 + variant, new String[][] {
                {null, plate, null}, {plate, multi(1200 + variant), plate}, {plate, null, plate}
            });
        }
    }

    private static String ore(String prefix, int material) {
        return "<ore:" + prefix + MATERIALS[material].mNameInternal + ">";
    }

    private static String multi(int id) { return "<gregtech:gt.multitileentity:" + id + ">"; }

    private static void replace(int id, String[][] grid) {
        // Resolve the whole replacement before removing any working GT6 recipe.
        GeneratedCrafting3x3.replaceTokens("thermal material progression " + id,
                multi(id), grid, GeneratedRecipeSettings.DEFAULT, LOG);
    }
}
