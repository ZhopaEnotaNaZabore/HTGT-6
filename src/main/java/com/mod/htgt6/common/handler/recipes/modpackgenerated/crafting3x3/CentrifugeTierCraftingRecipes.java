package com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.removed.GeneratedCraftingRemoval;
import org.apache.logging.log4j.Logger;

/** Uniform ULV-HV crafting progression for the four material-tier GT6 centrifuges. */
public final class CentrifugeTierCraftingRecipes {

    private static final TierRecipe[] TIERS = {
            new TierRecipe(
                    "ULV/Bronze",
                    "<gregtech:gt.multitileentity:20081>",
                    "<gregtech:gt.multiitem.technological:12000>",
                    "<IC2:itemPartCircuit>",
                    "<gregtech:gt.multitileentity:28116>",
                    "<HTGT6:ULVhull>"),
            new TierRecipe(
                    "LV/Steel",
                    "<gregtech:gt.multitileentity:20082>",
                    "<gregtech:gt.multiitem.technological:12001>",
                    "<gregtech:gt.multiitem.technological:30301>",
                    "<gregtech:gt.multitileentity:28366>",
                    "<HTGT6:hullLV>"),
            new TierRecipe(
                    "MV/Titanium",
                    "<gregtech:gt.multitileentity:20083>",
                    "<gregtech:gt.multiitem.technological:12002>",
                    "<gregtech:gt.multiitem.technological:30302>",
                    "<gregtech:gt.multitileentity:28666>",
                    "<HTGT6:hullMV>"),
            new TierRecipe(
                    "HV/TungstenSteel",
                    "<gregtech:gt.multitileentity:20084>",
                    "<gregtech:gt.multiitem.technological:12003>",
                    "<IC2:itemPartCircuitAdv>",
                    "<gregtech:gt.multitileentity:28616>",
                    "<HTGT6:hullHV>")
    };

    private CentrifugeTierCraftingRecipes() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        int added = 0;
        for (TierRecipe tier : TIERS) {
            String source = "HTGT6 centrifuge progression " + tier.name;
            GeneratedCraftingRemoval.remove(source, tier.output, settings, logger);
            if (GeneratedCrafting3x3.addTokens(
                    source,
                    tier.output + "*1",
                    new String[][] {
                            {tier.motor, tier.circuit, tier.motor},
                            {tier.cable, tier.hull, tier.cable},
                            {tier.motor, tier.circuit, tier.motor}
                    },
                    settings,
                    logger)) {
                added++;
            }
        }
        if (added != TIERS.length) {
            logger.warn("Only {} of {} centrifuge tier recipes were registered", added, TIERS.length);
        }
    }

    private static final class TierRecipe {
        final String name;
        final String output;
        final String motor;
        final String circuit;
        final String cable;
        final String hull;

        TierRecipe(String name, String output, String motor, String circuit, String cable, String hull) {
            this.name = name;
            this.output = output;
            this.motor = motor;
            this.circuit = circuit;
            this.cable = cable;
            this.hull = hull;
        }
    }
}
