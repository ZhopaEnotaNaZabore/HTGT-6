package com.mod.htgt6.common.handler.recipes;

import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;
import com.mod.htgt6.common.GTHooks.MisscelouseGT6Hooks.HighTierComponents;
import com.mod.htgt6.common.GTHooks.MisscelouseGT6Hooks.HighTierComponents.Component;
import com.mod.htgt6.common.GTHooks.MisscelouseGT6Hooks.HighTierComponents.Tier;
import com.mod.htgt6.common.handler.recipe.RM.LineAssemblerRecipeHandler;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.*;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.removed.GeneratedCraftingRemoval;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.data.MT;
import gregapi.data.OP;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Logger;

public final class ExtruderProgressionRecipes {
    private ExtruderProgressionRecipes() {}
    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        ItemStack coil=GameRegistry.findItemStack("HTGT6", "UXcoil", 8);
        ItemStack output=GeneratedValueResolver.item("<gregtech:gt.multitileentity:20204>", "UX extruder", settings, logger);
        ItemStack base=GeneratedValueResolver.item("<gregtech:gt.multitileentity:20201>", "UX extruder core", settings, logger);
        ItemStack[] inputs={base,
            HighTierComponents.stack(Component.MOTOR,Tier.UX,4),
            HighTierComponents.stack(Component.PISTON,Tier.UX,4),
            HighTierComponents.stack(Component.PUMP,Tier.UX,2),
            HighTierComponents.stack(Component.CONVEYOR,Tier.UX,2),
            HighTierComponents.stack(Component.ROBOT_ARM,Tier.UX,2),
            HighTierComponents.stack(Component.FIELD_GENERATOR,Tier.UX,2), coil,
            RecipeMap.gt(OP.plateDense,MT.Bedrock_HSLA_Alloy,16),
            RecipeMap.gt(OP.gearGt,MT.Bedrock_HSLA_Alloy,8),
            RecipeMap.gt(OP.screw,MT.Bedrock_HSLA_Alloy,32)};
        if(output==null) return;
        for(ItemStack input:inputs) if(input==null || input.getItem()==null) {
            logger.warn("UX extruder progression skipped: missing ingredient"); return;
        }
        LineAssemblerRecipeHandler.INSTANCE.addRecipe(new MachineRecipe(inputs,new ItemStack[]{output},null,null,
            1200,MachineTierSystem.voltage(10),10));
        // Keep the low-heat 20201 working and preserve existing placed blocks.
        for(int id:new int[]{20202,20203,20204,17129})
            GeneratedCraftingRemoval.remove("HTGT6 extruder progression", "<gregtech:gt.multitileentity:"+id+">",settings,logger);
    }
}
