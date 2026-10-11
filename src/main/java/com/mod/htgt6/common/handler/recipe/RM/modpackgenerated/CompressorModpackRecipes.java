// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipe.RM.modpackgenerated;

import com.mod.htgt6.common.handler.recipe.RM.CompressorRecipeHandler;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedValueResolver;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Logger;

public final class CompressorModpackRecipes {

    private CompressorModpackRecipes() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        add("SMC_HiTech6.zs:30", "<GalacticraftCore:item.basicItem:6>*1", 256, 2048, 3, new String[] {"<gregtech:gt.meta.plateDense:290>*5"}, settings, logger);
        add("SMC_HiTech6.zs:31", "<GalacticraftCore:item.basicItem:9>*1", 256, 2048, 3, new String[] {"<gregtech:gt.meta.plateDense:8630>*5"}, settings, logger);
        add("SMC_HiTech6.zs:32", "<GalacticraftCore:item.basicItem:10>*1", 256, 2048, 3, new String[] {"<gregtech:gt.meta.plateDense:8610>*5"}, settings, logger);
        add("SMC_HiTech6.zs:33", "<GalacticraftMars:item.null:5>*1", 256, 2048, 3, new String[] {"<gregtech:gt.meta.plateDense:8750>*5"}, settings, logger);
        add("SMC_HiTech6.zs:34", "<GalacticraftCore:item.basicItem:8>*1", 256, 2048, 3, new String[] {"<gregtech:gt.meta.plateDense:130>*5"}, settings, logger);
        add("SMC_HiTech6.zs:35", "<GalacticraftMars:item.itemBasicAsteroids:6>*1", 256, 2048, 3, new String[] {"<gregtech:gt.meta.plateDense:220>*5"}, settings, logger);
        add("SMC_HiTech6.zs:36", "<GalaxySpace:item.CompressedPlates>*1", 256, 2048, 3, new String[] {"<gregtech:gt.meta.plateDense:270>*5"}, settings, logger);
        add("SMC_HiTech6.zs:37", "<GalaxySpace:item.CompressedPlates:1>*1", 256, 2048, 3, new String[] {"<gregtech:gt.meta.plateDense:120>*5"}, settings, logger);
        add("SMC_HiTech6.zs:38", "<GalaxySpace:item.CompressedPlates:2>*1", 256, 2048, 3, new String[] {"<gregtech:gt.meta.plateDense:280>*5"}, settings, logger);
        add("SMC_HiTech6.zs:39", "<GalaxySpace:item.CompressedPlates:3>*1", 256, 2048, 3, new String[] {"<minecraft:coal_block>*5"}, settings, logger);
        add("SMC_HiTech6.zs:45", "<IC2:reactorPlating>*1", 512, 128, 2, new String[] {"<gregtech:gt.meta.plateDense:820>*1", "<IC2:itemPartAlloy>*9"}, settings, logger);
        add("SMC_HiTech6.zs:46", "<IC2:reactorPlatingExplosive>*1", 512, 128, 2, new String[] {"<IC2:reactorPlating>*1", "<IC2:itemPartAlloy>*16"}, settings, logger);
        add("SMC_HiTech6.zs:47", "<IC2:reactorPlatingHeat>*1", 512, 128, 2, new String[] {"<IC2:reactorPlating>*1", "<gregtech:gt.meta.plateDense:8640>*8"}, settings, logger);
    }

    private static void add(String source, String outputToken, int duration, int euPerTick, int tier,
            String[] inputTokens, GeneratedRecipeSettings settings, Logger logger) {
        ItemStack output = GeneratedValueResolver.item(outputToken, source + " output", settings, logger);
        if (output == null) return;
        ItemStack[] inputs = new ItemStack[inputTokens.length];
        for (int index = 0; index < inputTokens.length; index++) {
            inputs[index] = GeneratedValueResolver.item(inputTokens[index], source + " input[" + index + ']', settings, logger);
            if (inputs[index] == null) return;
        }
        CompressorRecipeHandler.addRecipe(inputs, output, duration, euPerTick, tier);
    }
}
