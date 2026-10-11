package com.mod.htgt6.common.handler.recipes;

import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;
import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import com.mod.htgt6.common.handler.recipe.RM.AssemblerRecipeHandler;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.removed.GeneratedCraftingRemoval;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.data.IL;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.oredict.OreDictMaterial;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.Logger;

/** Retains IronChest's existing upgrade items and their original source/target chest types. */
public final class IronChestModifierRecipes {
    private IronChestModifierRecipes() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        if (GameRegistry.findItem("IronChest", "woodIronUpgrade") == null) return;
        quadruplePlate(MT.Obsidian, OP.plate.mat(MT.Obsidian, 1), logger);
        replace("woodIronUpgrade", MT.Fe, true, new String[]{null}, new int[]{0}, settings, logger);
        replace("woodCopperUpgrade", MT.Cu, true, new String[]{null}, new int[]{0}, settings, logger);
        replace("copperIronUpgrade", MT.Fe, true, new String[]{"woodCopperUpgrade"}, new int[]{1}, settings, logger);
        replace("copperSilverUpgrade", MT.Ag, true, new String[]{"woodCopperUpgrade"}, new int[]{1}, settings, logger);
        replace("ironGoldUpgrade", MT.Au, true,
                new String[]{"woodIronUpgrade", "copperIronUpgrade"}, new int[]{1, 2}, settings, logger);
        replace("silverGoldUpgrade", MT.Au, true, new String[]{"copperSilverUpgrade"}, new int[]{2}, settings, logger);
        replace("goldDiamondUpgrade", MT.Diamond, false,
                new String[]{"ironGoldUpgrade", "silverGoldUpgrade"}, new int[]{2, 3}, settings, logger);
        replace("diamondObsidianUpgrade", MT.Obsidian, false,
                new String[]{"goldDiamondUpgrade"}, new int[]{3}, settings, logger);
    }

    private static void replace(String name, OreDictMaterial material, boolean crafting,
            String[] previous, int[] tiers, GeneratedRecipeSettings settings, Logger logger) {
        Item output = GameRegistry.findItem("IronChest", name);
        // Diamond already has crystalline plates in GT6; use four per quadruple-plate equivalent.
        boolean crystallineDiamond = material == MT.Diamond;
        ItemStack plate = RecipeMap.gt(crystallineDiamond ? OP.plateGem : OP.plateQuadruple, material, 1);
        ItemStack screw = crafting ? OP.screw.mat(material, 1) : null;
        ItemStack tag = IL.Circuit_Selector.getWithMeta(0, 4);
        ItemStack[] bases = new ItemStack[previous.length];
        if (output == null || !valid(plate) || !valid(tag) || (crafting && !valid(screw))) {
            logger.warn("IronChest upgrade {} retained: missing replacement component", name);
            return;
        }
        for (int i = 0; i < previous.length; i++) {
            Item item = previous[i] == null ? Item.getItemFromBlock(Blocks.chest)
                    : GameRegistry.findItem("IronChest", previous[i]);
            if (item == null) {
                logger.warn("IronChest upgrade {} retained: missing predecessor {}", name, previous[i]);
                return;
            }
            bases[i] = new ItemStack(item);
        }
        GeneratedCraftingRemoval.remove("HTGT6 progressive chest modifiers", "<IronChest:" + name + ">", settings, logger);
        for (int i = 0; i < bases.length; i++) {
            ItemStack[] inputs = new ItemStack[9];
            inputs[0] = bases[i].copy();
            inputs[1] = plate.copy(); inputs[1].stackSize = crystallineDiamond ? 12 : 3;
            inputs[2] = tag.copy();
            int tier = tiers[i];
            AssemblerRecipeHandler.addRecipe(inputs, new ItemStack(output), 10 * 20,
                    tier == 0 ? 8 : (int) MachineTierSystem.voltage(tier), tier);
            logger.info("Registered IronChest modifier {} from {} at tier {}", name, previous[i], tier);
            if (crafting) GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(output),
                    "SPS", "PCP", "SPS", 'S', screw.copy(), 'P', plate.copy(), 'C', bases[i].copy()));
        }
    }

    private static void quadruplePlate(OreDictMaterial material, ItemStack plate, Logger logger) {
        ItemStack result = OP.plateQuadruple.mat(material, 1);
        if (!valid(result) || !valid(plate)) {
            logger.warn("Missing quadruple plate ingredients for {}", material.mNameInternal);
            return;
        }
        GameRegistry.addRecipe(new ShapedOreRecipe(result, "PP", "PP", 'P', plate));
    }

    private static boolean valid(ItemStack stack) { return stack != null && stack.getItem() != null; }
}
