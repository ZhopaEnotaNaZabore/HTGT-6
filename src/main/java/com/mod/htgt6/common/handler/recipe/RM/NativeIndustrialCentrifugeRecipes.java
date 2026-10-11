package com.mod.htgt6.common.handler.recipe.RM;

import com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator;
import gregapi.data.CS;
import gregapi.data.MT;
import gregapi.data.OP;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedValueResolver;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** HTGT6's existing separation chain, also available in its own centrifuge. */
public final class NativeIndustrialCentrifugeRecipes {
    private static final Logger LOG = LogManager.getLogger("HTGT6 Centrifuge");
    private static boolean registered;
    private NativeIndustrialCentrifugeRecipes() { }

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        registerMineralDusts();
        // Exact amounts, chances and timings from GregTechChemistry.zs:69/80/85.
        add(1, new String[0], new String[] { "<liquid:trinium_fluiride>*1000" },
                new String[] { "<gregtech:gt.meta.dust:1260>*1", "<HTGT6:dustNiCu>*2" },
                new String[] { "<liquid:fluorine>*500", "<liquid:hydrogen>*500" },
                new int[] {4300, 8200}, 1800, 410, 3);
        add(1, new String[0], new String[] { "<liquid:heatedbauxidesollution>*1000" },
                new String[] { "<HTGT6:dustHeavyBauxideSludge>*1", "<HTGT6:dustNiCu>*1" },
                new String[] { "<liquid:hydrochloricacid>*200" },
                new int[] {10000, 2200}, 1750, 288, 3);
        add(-1, new String[] { "<HTGT6:BauxideSludge>*4" }, new String[0],
                new String[] { "<gregtech:gt.meta.dust:8008>*1", "<gregtech:gt.meta.dust:9192>*1",
                        "<gregtech:gt.meta.dust:9104>*1", "<gregtech:gt.meta.dust:310>*1", "<gregtech:gt.meta.dust:410>*1" },
                new String[0], new int[] {10000, 8900, 8100, 2800, 1100}, 1750, 288, 3);
    }

    private static void registerMineralDusts() {
        // Match the component ratios declared by GT6MaterialAutoGenerator exactly.
        IndustrialCentrifugeRecipeHandler.addRecipe(
                new ItemStack[] { OP.dust.mat(GT6MaterialAutoGenerator.PHERITHIUM, 10) },
                new FluidStack[0],
                new ItemStack[] { OP.dust.mat(MT.Pyrite, 1), OP.dust.mat(MT.SiO2, 1),
                        OP.dust.mat(MT.OREMATS.Uraninite, 1) },
                new FluidStack[] { MT.Water.liquid(7 * CS.U, false) },
                56 * 20, 512, 3);
        IndustrialCentrifugeRecipeHandler.addRecipe(
                new ItemStack[] { OP.dust.mat(GT6MaterialAutoGenerator.VANITE, 15) },
                new FluidStack[0],
                new ItemStack[] { OP.dust.mat(MT.VanadiumPentoxide, 4), OP.dust.mat(MT.Pyrite, 6),
                        OP.dust.mat(MT.OREMATS.Bastnasite, 5) },
                new FluidStack[0], 82 * 20, 2048, 4);
    }

    private static void add(int selector, String[] in, String[] fin, String[] out, String[] fout,
                            int[] chances, int ticks, int eut, int tier) {
        ItemStack[] inputs = items(in), outputs = items(out);
        FluidStack[] fluidsIn = fluids(fin), fluidsOut = fluids(fout);
        if (inputs == null || outputs == null || fluidsIn == null || fluidsOut == null) return;
        IndustrialCentrifugeRecipeHandler.addRecipe(selector, inputs, fluidsIn, outputs, fluidsOut,
                chances, ticks, eut, tier);
    }
    private static ItemStack[] items(String[] tokens) {
        ItemStack[] result = new ItemStack[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            result[i] = GeneratedValueResolver.item(tokens[i], "Native centrifuge", GeneratedRecipeSettings.DEFAULT, LOG);
            if (result[i] == null) return null;
        }
        return result;
    }
    private static FluidStack[] fluids(String[] tokens) {
        FluidStack[] result = new FluidStack[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            result[i] = GeneratedValueResolver.fluid(tokens[i], "Native centrifuge", GeneratedRecipeSettings.DEFAULT, LOG);
            if (result[i] == null) return null;
        }
        return result;
    }
}
