package com.mod.htgt6.common.handler.recipes;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.integratedgt.GeneratedGTRecipeMaps;
import org.apache.logging.log4j.LogManager;

/** Completes the former EV quest concept using HV-capable reactions. */
public final class PlatinumGroupChemistryRecipes {
    private static boolean registered;
    private static final String[] NONE = new String[0];
    private PlatinumGroupChemistryRecipes() {}
    public static void register() {
        if (registered) return;
        // Mechanical by-products normally use dust prefixes, while the legacy
        // chemistry accepts only centrifuged tiny ore. Equal seven-unit batches.
        for (String input : a("<gregtech:gt.meta.dust:8103>*7",
                "<gregtech:gt.meta.dustSmall:8103>*28", "<gregtech:gt.meta.dustTiny:8103>*63")) {
            add("sludge dissolution " + input, "heatmixer", 900, a(input),
                a("<liquid:aquaregia>*5000"), a("<liquid:platiunum_sollution>*5000"),
                a("<HTGT6:DustOsIrRhRu>*2", "<HTGT6:dustPdKeNiCu>*2"));
        }
        add("alkaline residue", "heatmixer", 1200,
            a("<HTGT6:DustOsIrRhRu>*4", "<gregtech:gt.meta.dust:8015>*3"),
            a("<liquid:water>*1000"), NONE, a("<HTGT6:dustPGMAlkalineResidue>*4"));
        add("potassium disulfate solution", "heatmixer", 600,
            a("<gregtech:gt.meta.dust:8233>*13"), a("<liquid:water>*1000"),
            a("<liquid:potassium_disuplhate>*1000"), NONE);
        add("PGM fraction separation", "centrifuge", 2400,
            a("<HTGT6:dustPGMAlkalineResidue>*4"), a("<liquid:potassium_disuplhate>*1000"),
            a("<liquid:ruthenium_salt>*1000"), a("<HTGT6:dustRhSo4>*6", "<HTGT6:dustOsIr>*2"));
        add("iridium osmium separation", "heatmixer", 1800,
            a("<HTGT6:dustOsIr>*2"), a("<liquid:hydrochloricacid>*4000", "<liquid:water>*2000"),
            a("<liquid:acidic_osmium_sollution>*1000"), a("<HTGT6:dustIrO>*3"));
        add("iridium reduction", "heatmixer", 2400,
            a("<HTGT6:dustIrO>*9", "<gregtech:gt.meta.dust:130>*4"), NONE, NONE,
            a("<gregtech:gt.meta.dust:770>*3", "<gregtech:gt.meta.dust:8008>*10"));
        add("osmium reduction", "heatmixer", 2400, NONE,
            a("<liquid:acidic_osmium_sollution>*1000", "<liquid:hydrogen>*2000"),
            a("<liquid:hydrochloricacid>*2000"), a("<gregtech:gt.meta.dust:760>*1"));
        add("rhodium reduction", "heatmixer", 1800,
            a("<HTGT6:dustRhSo4>*6", "<gregtech:gt.meta.dust:110>*2"), NONE, NONE,
            a("<gregtech:gt.meta.dust:450>*1", "<gregtech:gt.meta.dust:8270>*7"));
        add("ruthenium reduction", "heatmixer", 1800,
            a("<gregtech:gt.meta.dust:110>*2"), a("<liquid:ruthenium_salt>*1000"), NONE,
            a("<gregtech:gt.meta.dust:440>*1", "<gregtech:gt.meta.dust:8204>*4"));
        registered = true;
    }
    private static String[] a(String... values) { return values; }
    private static void add(String name, String map, long ticks, String[] inputs,
            String[] fluidsIn, String[] fluidsOut, String[] outputs) {
        long[] chances = new long[outputs.length];
        java.util.Arrays.fill(chances, 10000);
        if (!GeneratedGTRecipeMaps.add("HV PGM chemistry: " + name, "gt.recipe." + map, false,
                480, ticks, chances, inputs, fluidsIn, fluidsOut, outputs,
                GeneratedRecipeSettings.DEFAULT, LogManager.getLogger("HTGT6-PGMChemistry")))
            throw new IllegalStateException("Missing HV PGM chemistry recipe: " + name);
    }
}
