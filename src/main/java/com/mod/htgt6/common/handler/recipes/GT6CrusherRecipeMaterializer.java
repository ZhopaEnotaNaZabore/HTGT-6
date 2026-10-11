package com.mod.htgt6.common.handler.recipes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gregapi.data.OP;
import gregapi.data.RM;
import gregapi.data.TD;
import gregapi.oredict.OreDictItemData;
import gregapi.recipes.IRecipeMapHandler;
import gregapi.util.OM;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Expands only GT6's input-driven whole-ore Crusher recipes so EnderIO and IE
 * can inherit them. This class deliberately does not remove, rewrite, merge,
 * normalize, or otherwise change any existing GT6 recipe.
 */
public final class GT6CrusherRecipeMaterializer {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-GT6-Crusher-Inheritance");

    private static boolean materialized;

    private GT6CrusherRecipeMaterializer() { }

    public static synchronized void materialize() {
        if (materialized || RM.Crusher == null || RM.Crusher.mRecipeList == null) return;
        materialized = true;

        int before = RM.Crusher.mRecipeList.size();
        Set<String> visited = new HashSet<String>();
        for (String oreName : OreDictionary.getOreNames()) {
            if (oreName == null || !oreName.startsWith("ore")) continue;
            for (ItemStack stack : OreDictionary.getOres(oreName)) {
                OreDictItemData data = association(stack);
                if (!isWholeOre(data) || !visited.add(stackKey(stack))) continue;
                materializeInput(stack);
            }
        }

        LOGGER.info("Materialized {} original GT6 Crusher recipes from {} whole-ore stacks; "
                        + "GT6 outputs were left unchanged",
                Math.max(0, RM.Crusher.mRecipeList.size() - before), visited.size());
    }

    /** World/third-party ore variants need not have been enumerated in OreDictionary. */
    public static void materializeInput(ItemStack stack) {
        OreDictItemData data = association(stack);
        if (!isWholeOre(data) || RM.Crusher == null) return;
        ItemStack input = stack.copy();
        input.stackSize = 1;
        for (IRecipeMapHandler handler : new ArrayList<IRecipeMapHandler>(RM.Crusher.mRecipeMapHandlers)) {
            if (handler != null) handler.addRecipesUsing(RM.Crusher, true, input, data);
        }
    }

    private static OreDictItemData association(ItemStack stack) {
        if (stack == null || stack.getItem() == null || stack.stackSize <= 0) return null;
        OreDictItemData data = OM.anyassociation(stack);
        return data != null && data.hasValidPrefixMaterialData()
                && data.mPrefix != null && data.mMaterial != null
                && data.mMaterial.mMaterial != null ? data : null;
    }

    private static boolean isWholeOre(OreDictItemData data) {
        return data != null && data.mPrefix != OP.oreBedrock
                && data.mPrefix.contains(TD.Prefix.ORE)
                && !data.mPrefix.contains(TD.Prefix.DUST_ORE);
    }

    private static String stackKey(ItemStack stack) {
        return Item.getIdFromItem(stack.getItem()) + ":" + stack.getItemDamage() + ":"
                + (stack.hasTagCompound() ? stack.getTagCompound().toString() : "");
    }
}
