package com.mod.htgt6.common.handler;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class recipes {

        public static void registerRecipes() {
            GameRegistry.addShapedRecipe(new ItemStack(ModItems.INGOT_AS_TITANIUM),
                    " R ", " R ", " S ",
                    'R', ModItems.COMPOSITE_T_1,
                    'S', Items.stick);
    }
}
