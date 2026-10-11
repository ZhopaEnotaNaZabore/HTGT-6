package com.mod.htgt6.common.handler.recipe;

import gregapi.code.IItemContainer;
import gregapi.data.IL;
import gregapi.util.CR;
import gregapi.util.ST;
import net.minecraft.item.ItemStack;

public final class RecipeRemover {

    private RecipeRemover() {}

    public static void init() {
        remove(IL.Electric_Motor_LuV);
        // remove(IL.Electric_Motor_ZPM);
        // remove(IL.Electric_Motor_UV);

    }

    private static void remove(IItemContainer item) {
        if (item != null) {
            ItemStack stack = item.get(1);
            if (ST.valid(stack)) {
                // Forces direct output removal from CraftingManager
                CR.remout(stack);
            }
        }
    }
}