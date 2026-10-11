package com.mod.htgt6.common.handler.recipes;

import com.mod.htgt6.common.handler.recipe.RM.AssemblerRecipeHandler;
import com.mod.htgt6.common.handler.recipe.RM.LineAssemblerRecipeHandler;
import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;
import com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.warn.GeneratedTooltipRegistry;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.removed.GeneratedCraftingRemoval;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.data.IL;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.Logger;

/** Hand-maintained additions applied after the migrated IronChests.zs recipes. */
public final class IronChestProgressionRecipes {
    private IronChestProgressionRecipes() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        Block chest = GameRegistry.findBlock("IronChest", "BlockIronChest");
        if (chest == null || chest == Blocks.air) return;
        // Keep the existing iron (0) and copper (3) recipes and their wooden bases.
        replace(chest, 1, OP.plateTriple.mat(MT.Au, 1), OP.screw.mat(MT.Au, 1),
                new ItemStack(chest, 1, 0), settings, logger);
        replace(chest, 4, OP.plateTriple.mat(MT.Ag, 1), OP.screw.mat(MT.Ag, 1),
                new ItemStack(chest, 1, 3), settings, logger);
        // GT6 generates no triple plates for these two materials.
        replace(chest, 2, OP.plateGem.mat(MT.Diamond, 1), OP.screw.mat(MT.Diamond, 1),
                new ItemStack(chest, 1, 1), settings, logger);
        replace(chest, 6, OP.plate.mat(MT.Obsidian, 1), OP.screw.mat(MT.Obsidian, 1),
                new ItemStack(chest, 1, 2), settings, logger);
        registerAssemblerChests(chest, logger);
        registerDirtChest(chest, settings, logger);

        ItemStack[] inputs = new ItemStack[9];
        inputs[0] = new ItemStack(chest, 2, 6);
        inputs[1] = OP.plateGem.mat(MT.CertusQuartz, 16);
        inputs[2] = OP.screw.mat(MT.CertusQuartz, 32);
        inputs[3] = IL.Circuit_Selector.getWithMeta(0, 8); // Required, never consumed.
        FluidStack glass = MT.Glass.mLiquid == null ? null : MT.Glass.mLiquid.copy();
        if (!present(inputs[1]) || !present(inputs[2]) || !present(inputs[3])
                || glass == null || glass.getFluid() == null) {
            logger.warn("Crystal chest assembler recipe skipped: missing GT6 quartz, selector or molten glass");
            return;
        }
        glass.amount = 1296;
        AssemblerRecipeHandler.addRecipe(inputs, new FluidStack[] {glass},
                new ItemStack(chest, 1, 5), 60 * 20, 412, 3);
        GeneratedCraftingRemoval.remove("HTGT6 crystal chest assembler", token(5), settings, logger);
        // The upgrade otherwise bypasses the assembler by transforming a placed diamond chest.
        GeneratedCraftingRemoval.remove("HTGT6 crystal chest assembler",
                "<IronChest:diamondCrystalUpgrade>", settings, logger);
    }

    private static void registerAssemblerChests(Block chest, Logger logger) {
        // Native IronChest branches; crystal retains its separate quartz/glass recipe below.
        assemblerChest(chest, -1, 0, MT.Fe, 0, logger);
        assemblerChest(chest, -1, 3, MT.Cu, 0, logger);
        assemblerChest(chest, 3, 0, MT.Fe, 1, logger);
        assemblerChest(chest, 3, 4, MT.Ag, 1, logger);
        assemblerChest(chest, 0, 1, MT.Au, 1, logger);
        assemblerChest(chest, 4, 1, MT.Au, 2, logger);
        assemblerChest(chest, 1, 2, MT.Diamond, 2, logger);
        assemblerChest(chest, 2, 6, MT.Obsidian, 3, logger);
    }

    private static void assemblerChest(Block chest, int source, int target,
            OreDictMaterial material, int tier, Logger logger) {
        // Brittle materials have existing single plates, not double plates; keep six material units.
        OreDictPrefix prefix = material == MT.Diamond ? OP.plateGem
                : material == MT.Obsidian ? OP.plate : OP.plateDouble;
        ItemStack plates = RecipeMap.gt(prefix, material, prefix == OP.plateDouble ? 3 : 6);
        ItemStack selector = IL.Circuit_Selector.getWithMeta(0, 4);
        if (!present(plates) || !present(selector)) {
            logger.warn("IronChest assembler recipe {} -> {} skipped: missing plates or selector", source, target);
            return;
        }
        ItemStack[] inputs = new ItemStack[9];
        inputs[0] = source < 0 ? new ItemStack(Blocks.chest) : new ItemStack(chest, 1, source);
        inputs[1] = plates;
        inputs[2] = selector;
        AssemblerRecipeHandler.addRecipe(inputs, new ItemStack(chest, 1, target),
                10 * 20, tier == 0 ? 8 : (int) MachineTierSystem.voltage(tier), tier);
        logger.info("Registered IronChest assembler chest {} -> {} at tier {}", source, target, tier);
    }

    private static void registerDirtChest(Block chest, GeneratedRecipeSettings settings, Logger logger) {
        GeneratedTooltipRegistry.add("HTGT6 transcendent disappointment", token(7), "gold",
                "Даже радоксианцам это не по карману...", settings, logger);
        OreDictMaterial transcendent = GT6MaterialAutoGenerator.TRANSCENDENTIC_ALLOY;
        // Fill all sixteen slots, respecting both the machine and GT6 prefix limits (rotors: 16).
        OreDictPrefix[] parts = {OP.plateDense, OP.plateDense, OP.plateDense, OP.plateDense,
                OP.gearGt, OP.gearGt, OP.gearGt, OP.gearGt,
                OP.rotor, OP.rotor, OP.casingSmall, OP.casingSmall,
                OP.screw, OP.screw, OP.wireFine, OP.wireFine};
        ItemStack[] inputs = new ItemStack[parts.length];
        for (int i = 0; i < parts.length; i++) {
            inputs[i] = parts[i].mat(transcendent, 64);
            if (!present(inputs[i])) {
                logger.warn("DirtChest OMV recipe skipped: missing transcendent component {}", parts[i].mNameInternal);
                return;
            }
            inputs[i].stackSize = Math.min(64, inputs[i].getMaxStackSize());
        }
        OreDictMaterial[] melts = {transcendent, GT6MaterialAutoGenerator.STABLE_TIME_SPACE,
                GT6MaterialAutoGenerator.QUANTUM_ALLOY, GT6MaterialAutoGenerator.QUANTINUM};
        FluidStack[] fluids = new FluidStack[melts.length];
        for (int i = 0; i < melts.length; i++) {
            if (melts[i] == null || melts[i].mLiquid == null || melts[i].mLiquid.getFluid() == null) {
                logger.warn("DirtChest OMV recipe skipped: missing molten endgame material {}", i);
                return;
            }
            fluids[i] = melts[i].mLiquid.copy();
            fluids[i].amount = 128000; // Exactly one full LineAssembler tank per fluid.
        }
        LineAssemblerRecipeHandler.INSTANCE.addRecipe(new MachineRecipe(inputs,
                new ItemStack[] {new ItemStack(chest, 1, 7)}, fluids, null,
                60 * 60 * 20, MachineTierSystem.voltage(12), 12));
        GeneratedCraftingRemoval.remove("HTGT6 DirtChest OMV joke", token(7), settings, logger);
    }

    private static void replace(Block chest, int meta, ItemStack plate, ItemStack screw,
            ItemStack base, GeneratedRecipeSettings settings, Logger logger) {
        if (!present(plate) || !present(screw) || !present(base)) {
            logger.warn("IronChest recipe {} retained: missing replacement ingredients", meta);
            return;
        }
        ShapedOreRecipe replacement = new ShapedOreRecipe(new ItemStack(chest, 1, meta),
                "SPS", "PCP", "SPS", 'S', screw, 'P', plate, 'C', base);
        GeneratedCraftingRemoval.remove("HTGT6 IronChest progression", token(meta), settings, logger);
        GameRegistry.addRecipe(replacement);
    }

    private static boolean present(ItemStack stack) { return stack != null && stack.getItem() != null; }
    private static String token(int meta) { return "<IronChest:BlockIronChest:" + meta + ">"; }
}
