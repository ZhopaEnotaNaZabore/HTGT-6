package com.mod.htgt6.common.handler.recipes;

import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.oredict.OreDictMaterial;
import java.util.Iterator;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Replaces the GT6 solid-fuel Burning Box recipes after GT6 flushes CR.BUFFER.
 *
 * <p>The first Lead box (1100) remains the early-game recipe migrated from
 * gamestart.zs. Every later material tier upgrades the previous regular box.
 * A Dense box upgrades the regular box made from the same material.</p>
 */
public final class BurningBoxRecipes {

    private static final Logger LOGGER = LogManager.getLogger("HTGT6-BurningBoxes");

    private static final int[] REGULAR_IDS = {
            1100, 1101, 1102, 1103, 1104, 1105,
            1106, 1107, 1108, 1109, 1110, 1111
    };

    private static final int[] DENSE_IDS = {
            1150, 1151, 1152, 1153, 1154, 1155,
            1156, 1157, 1158, 1159, 1160, 1161
    };

    private static final OreDictMaterial[] MATERIALS = {
            MT.Pb, MT.Bi, MT.Bronze, MT.Invar, MT.Steel, MT.Cr,
            MT.Ti, MT.W, MT.TungstenSteel, MT.Ta4HfC5,
            MT.GT6U.Nb2Ti3C4, MT.Netherite
    };

    private static boolean registered;

    private BurningBoxRecipes() {}

    public static synchronized void register() {
        if (registered) return;

        ItemStack fireclayBrick = GameRegistry.findItemStack("HTGT6", "FireclayBrick", 1);
        if (fireclayBrick == null || fireclayBrick.getItem() == null) {
            LOGGER.warn("FireclayBrick is missing; solid Burning Box replacements were not registered");
            return;
        }

        int removed = 0;
        int added = 0;
        String doubleCopper = oreName(OP.plateDouble, MT.Cu);
        String denseCopper = oreName(OP.plateDense, MT.Cu);
        if (!hasOre(doubleCopper) || !hasOre(denseCopper)) {
            LOGGER.warn("Copper structural plates are missing; solid Burning Box replacements were not registered");
            return;
        }

        // 1100 is replaced by the migrated early-game Lead recipe. Start with
        // Bismuth and require the preceding material tier as the machine core.
        for (int tier = 1; tier < REGULAR_IDS.length; tier++) {
            ItemStack output = multiTile(REGULAR_IDS[tier]);
            // Netherite is an alternative to titanium, not an upgrade of carbide.
            ItemStack previousTier = multiTile(tier == 11 ? 1105 : REGULAR_IDS[tier - 1]);
            String materialPlate = oreName(OP.plate, MATERIALS[tier]);
            if (!valid(output) || !valid(previousTier) || !hasOre(materialPlate)) {
                LOGGER.warn("Skipping regular Burning Box tier {} because an MTE stack or {} is missing",
                        tier, materialPlate);
                continue;
            }

            removed += removeRecipesFor(output);
            GameRegistry.addRecipe(new ShapedOreRecipe(output.copy(),
                    "PCP",
                    "PBP",
                    "FFF",
                    'P', materialPlate,
                    'C', doubleCopper,
                    'B', previousTier.copy(),
                    'F', fireclayBrick.copy()));
            added++;
        }

        // Dense boxes retain GT6's expensive material scale, while the regular
        // box of the same material supplies the functional core.
        for (int tier = 0; tier < DENSE_IDS.length; tier++) {
            ItemStack output = multiTile(DENSE_IDS[tier]);
            ItemStack regularCore = multiTile(REGULAR_IDS[tier]);
            String materialPlate = oreName(OP.plateQuintuple, MATERIALS[tier]);
            if (!valid(output) || !valid(regularCore) || !hasOre(materialPlate)) {
                LOGGER.warn("Skipping Dense Burning Box tier {} because an MTE stack or {} is missing",
                        tier, materialPlate);
                continue;
            }

            removed += removeRecipesFor(output);
            GameRegistry.addRecipe(new ShapedOreRecipe(output.copy(),
                    "PCP",
                    "PBP",
                    "FFF",
                    'P', materialPlate,
                    'C', denseCopper,
                    'B', regularCore.copy(),
                    'F', fireclayBrick.copy()));
            added++;
        }

        ThermalMachineCraftingRecipes.register();
        registered = true;
        LOGGER.info("Replaced {} GT6 solid Burning Box recipes with {} tiered HTGT6 recipes",
                removed, added);
    }

    private static String oreName(gregapi.oredict.OreDictPrefix prefix, OreDictMaterial material) {
        return prefix.mNameInternal + material.mNameInternal;
    }

    private static ItemStack multiTile(int metadata) {
        ItemStack stack = GameRegistry.findItemStack("gregtech", "gt.multitileentity", 1);
        if (stack == null) return null;
        stack.setItemDamage(metadata);
        return stack;
    }

    private static boolean hasOre(String oreName) {
        return !OreDictionary.getOres(oreName).isEmpty();
    }

    private static boolean valid(ItemStack stack) {
        return stack != null && stack.getItem() != null;
    }

    @SuppressWarnings("unchecked")
    private static int removeRecipesFor(ItemStack target) {
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        int removed = 0;
        for (Iterator<IRecipe> iterator = recipes.iterator(); iterator.hasNext();) {
            IRecipe recipe = iterator.next();
            ItemStack result = recipe == null ? null : recipe.getRecipeOutput();
            if (result == null || result.getItem() != target.getItem()
                    || result.getItemDamage() != target.getItemDamage()) continue;
            iterator.remove();
            removed++;
        }
        return removed;
    }
}
