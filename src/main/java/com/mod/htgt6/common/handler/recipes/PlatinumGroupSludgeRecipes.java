package com.mod.htgt6.common.handler.recipes;

import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.data.TD;
import gregapi.oredict.OreDictItemData;
import gregapi.oredict.OreDictMaterial;
import gregapi.recipes.Recipe;
import gregapi.util.OM;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;

/** PGM extraction must enter the modpack's sludge refining chain. */
public final class PlatinumGroupSludgeRecipes {
    private static boolean enabled;
    private static final long UNIT = 648648000L;
    private PlatinumGroupSludgeRecipes() {}

    public static boolean isGroup(OreDictMaterial m) {
        return m == MT.Pt || m == MT.Pd || m == MT.Ir || m == MT.Os
                || m == MT.Ru || m == MT.Rh || m == MT.Ke;
    }

    /** Cauldron washing does not use RecipeMap at all. */
    public static OreDictMaterial washingMaterial(OreDictMaterial material) {
        return enabled && isGroup(material) ? MT.PlatinumGroupSludge : material;
    }

    /** The physical crucible reads material data directly, independently of its NEI recipe map. */
    public static OreDictItemData crucibleData(OreDictItemData data) {
        if (!enabled || data == null) return data;
        if (data.hasValidPrefixMaterialData() && isGroup(data.mMaterial.mMaterial)
                && (data.mPrefix == OP.dust || data.mPrefix == OP.dustSmall || data.mPrefix == OP.dustTiny
                || data.mPrefix == OP.ingot || data.mPrefix == OP.nugget || data.mPrefix == OP.blockIngot)) return data;
        java.util.List<gregapi.oredict.OreDictMaterialStack> materials = new ArrayList<gregapi.oredict.OreDictMaterialStack>();
        boolean changed = false;
        for (gregapi.oredict.OreDictMaterialStack part : data.getAllMaterialStacks()) {
            if (isGroup(part.mMaterial)) {
                materials.add(OM.stack(MT.PlatinumGroupSludge, part.mAmount));
                changed = true;
            } else if (isGroup(part.mMaterial.mTargetSmelting.mMaterial)) {
                materials.add(OM.stack(MT.PlatinumGroupSludge, gregapi.util.UT.Code.units(
                        part.mAmount, UNIT, part.mMaterial.mTargetSmelting.mAmount, false)));
                changed = true;
            } else materials.add(part.clone());
        }
        if (!changed || materials.isEmpty()) return data;
        gregapi.oredict.OreDictMaterialStack primary = materials.remove(0);
        return new OreDictItemData(primary, materials.toArray(new gregapi.oredict.OreDictMaterialStack[materials.size()]));
    }

    /** Runs in preInit, before GT6 generates prefix items. */
    public static void prepareItems() {
        for (gregapi.oredict.OreDictPrefix p : new gregapi.oredict.OreDictPrefix[] {
                OP.dust, OP.dustSmall, OP.dustTiny, OP.dustImpure, OP.dustPure,
                OP.crushed, OP.crushedTiny, OP.crushedPurified, OP.crushedPurifiedTiny,
                OP.crushedCentrifuged, OP.crushedCentrifugedTiny}) {
            p.forceItemGeneration(MT.PlatinumGroupSludge);
        }
    }

    public static void install() {
        enabled = true;
        for (OreDictMaterial m : OreDictMaterial.MATERIAL_ARRAY) {
            if (m == null) continue;
            for (int i = 0; i < m.mByProducts.size(); i++) {
                if (isGroup(m.mByProducts.get(i))) m.mByProducts.set(i, MT.PlatinumGroupSludge);
            }
            if (isGroup(m.mTargetCrushing.mMaterial))
                m.setCrushing(MT.PlatinumGroupSludge, m.mTargetCrushing.mAmount);
        }
        int changed = 0;
        for (Recipe.RecipeMap map : Recipe.RecipeMap.RECIPE_MAPS.values()) {
            for (Recipe recipe : new ArrayList<Recipe>(map.mRecipeList)) {
                if (normalize(recipe)) {
                    changed++;
                    enforceOutputCapacity(recipe, map.mOutputItemsCount);
                }
            }
        }
        @SuppressWarnings("unchecked")
        Map<ItemStack, ItemStack> furnace = FurnaceRecipes.smelting().getSmeltingList();
        for (Map.Entry<ItemStack, ItemStack> e : furnace.entrySet()) {
            if (!isCleanConversion(new ItemStack[] {e.getKey()}, null, new ItemStack[] {e.getValue()}, null)) {
                ItemStack replacement = replace(e.getValue());
                if (replacement != e.getValue()) { e.setValue(replacement); changed++; }
            }
        }
        LogManager.getLogger("HTGT6-PGM").info("Redirected {} recipes to Platinum Group Sludge (Pt/Pd/Ir/Os/Ru/Rh/Ke)", changed);
    }

    /** Existing maps have already validated their slots before normalization. */
    static void enforceOutputCapacity(Recipe recipe, int itemSlots) {
        if (recipe.mOutputs.length > itemSlots) {
            recipe.mEnabled = false;
            LogManager.getLogger("HTGT6-PGM").warn("Disabled PGM extraction recipe: sludge requires {} item slots, machine supports {}",
                    recipe.mOutputs.length, itemSlots);
        }
    }

    /** Also called by the GT Recipe constructor hook for late/on-demand recipes. */
    public static boolean normalize(Recipe recipe) {
        if (!enabled || recipe == null || isRefining(recipe)
                || isCleanConversion(recipe.mInputs, recipe.mFluidInputs, recipe.mOutputs, recipe.mFluidOutputs)) return false;
        boolean changed = false;
        if (recipe.mOutputs != null) for (int i = 0; i < recipe.mOutputs.length; i++) {
            ItemStack old = recipe.mOutputs[i], replacement = replace(old);
            if (old != replacement) { recipe.mOutputs[i] = replacement; changed = true; }
        }
        // Fluid extraction/smelting must not be an alternative to the purification chain.
        if (recipe.mFluidOutputs != null) for (int i = 0; i < recipe.mFluidOutputs.length; i++) {
            FluidStack fluid = recipe.mFluidOutputs[i];
            OreDictMaterial material = fluidMaterial(fluid);
            if (material == null) continue;
            long amount = (long) fluid.amount * material.mLiquidUnit / material.mLiquid.amount;
            ItemStack sludge = OM.dust(MT.PlatinumGroupSludge, amount);
            if (sludge == null) throw new IllegalStateException("Missing PGM sludge for liquid output");
            int count = recipe.mOutputs.length;
            recipe.mOutputs = java.util.Arrays.copyOf(recipe.mOutputs, count + 1);
            recipe.mOutputs[count] = sludge;
            long[] chances = new long[count + 1];
            for (int j = 0; j < count; j++) chances[j] = recipe.getOutputChance(j);
            chances[count] = 10000;
            recipe.mChances = chances;
            recipe.mFluidOutputs[i] = null;
            changed = true;
        }
        if (changed && recipe.mFluidOutputs != null) {
            java.util.List<FluidStack> fluids = new ArrayList<FluidStack>();
            for (FluidStack f : recipe.mFluidOutputs) if (f != null) fluids.add(f);
            recipe.mFluidOutputs = fluids.toArray(new FluidStack[fluids.size()]);
        }
        return changed;
    }

    private static boolean isRefining(Recipe recipe) {
        // Exact existing HiTech6 chemistry intermediates, not a blanket machine-map exemption.
        if (recipe.mInputs != null) for (ItemStack input : recipe.mInputs) {
            if (input == null) continue;
            String name = Item.itemRegistry.getNameForObject(input.getItem());
            if ("HTGT6:dustPtCl".equals(name) || "HTGT6:dustPdNH".equals(name)
                    || "HTGT6:dustIrO".equals(name) || "HTGT6:dustRhSo4".equals(name)) return true;
        }
        if (recipe.mFluidInputs != null) for (FluidStack f : recipe.mFluidInputs)
            if (f != null && ("trinium_fluiride".equals(f.getFluid().getName())
                    || "acidic_osmium_sollution".equals(f.getFluid().getName())
                    || "ruthenium_salt".equals(f.getFluid().getName()))) return true;
        return false;
    }

    private static OreDictMaterial fluidMaterial(FluidStack f) {
        if (f == null) return null;
        for (OreDictMaterial m : new OreDictMaterial[] {MT.Pt, MT.Pd, MT.Ir, MT.Os, MT.Ru, MT.Rh, MT.Ke})
            if (m.mLiquid != null && m.mLiquid.isFluidEqual(f)) return m;
        return null;
    }

    private static boolean isCleanConversion(ItemStack[] inputs, FluidStack[] fluids,
            ItemStack[] outputs, FluidStack[] outputFluids) {
        boolean recovery = false;
        if (outputs != null) for (ItemStack out : outputs) {
            OreDictItemData d = out == null ? null : OM.anyassociation(out);
            if (d != null && d.hasValidPrefixMaterialData() && isGroup(d.mMaterial.mMaterial)
                    && (d.mPrefix == OP.dust || d.mPrefix == OP.dustSmall || d.mPrefix == OP.dustTiny
                    || d.mPrefix == OP.ingot || d.mPrefix == OP.nugget || d.mPrefix == OP.blockIngot)) recovery = true;
        }
        if (outputFluids != null) for (FluidStack f : outputFluids) if (fluidMaterial(f) != null) recovery = true;
        java.util.Map<OreDictMaterial, Long> balance = new java.util.HashMap<OreDictMaterial, Long>();
        if (inputs != null) for (ItemStack input : inputs) {
            OreDictItemData d = input == null ? null : OM.anyassociation(input);
            if (d == null || input.stackSize <= 0 || !d.hasValidPrefixMaterialData() || !isGroup(d.mMaterial.mMaterial)) continue;
            if (d.mPrefix == OP.dust || d.mPrefix == OP.dustSmall || d.mPrefix == OP.dustTiny
                    || d.mPrefix == OP.ingot || d.mPrefix == OP.nugget || d.mPrefix == OP.blockIngot
                    || (!recovery && !d.mPrefix.contains(TD.Prefix.ORE) && !d.mPrefix.contains(TD.Prefix.DUST_ORE)))
                addAmount(balance, d.mMaterial.mMaterial, d.mMaterial.mAmount * input.stackSize);
        }
        if (fluids != null) for (FluidStack f : fluids) {
            OreDictMaterial m = fluidMaterial(f);
            if (m != null) addAmount(balance, m, (long) f.amount * m.mLiquidUnit / m.mLiquid.amount);
        }
        boolean found = false;
        if (outputs != null) for (ItemStack output : outputs) {
            OreDictItemData d = output == null ? null : OM.anyassociation(output);
            if (d != null && d.hasValidPrefixMaterialData() && isGroup(d.mMaterial.mMaterial)) {
                found = true;
                addAmount(balance, d.mMaterial.mMaterial, -d.mMaterial.mAmount * output.stackSize);
            }
        }
        if (outputFluids != null) for (FluidStack f : outputFluids) {
            OreDictMaterial m = fluidMaterial(f);
            if (m != null) { found = true; addAmount(balance, m, -(long) f.amount * m.mLiquidUnit / m.mLiquid.amount); }
        }
        for (long amount : balance.values()) if (amount < 0) return false;
        return found;
    }

    private static void addAmount(java.util.Map<OreDictMaterial, Long> balance, OreDictMaterial material, long amount) {
        Long previous = balance.get(material);
        balance.put(material, (previous == null ? 0 : previous) + amount);
    }

    private static ItemStack replace(ItemStack stack) {
        OreDictItemData d = stack == null ? null : OM.anyassociation(stack);
        if (d == null || !d.hasValidPrefixMaterialData() || !isGroup(d.mMaterial.mMaterial)) return stack;
        ItemStack replacement;
        if (d.mPrefix.contains(TD.Prefix.ORE) || d.mPrefix.contains(TD.Prefix.DUST_ORE)
                || d.mPrefix == OP.dust || d.mPrefix == OP.dustSmall || d.mPrefix == OP.dustTiny) {
            replacement = d.mPrefix.mat(MT.PlatinumGroupSludge, stack.stackSize);
        } else replacement = null;
        if (replacement == null) replacement = OM.dust(MT.PlatinumGroupSludge,
                d.mMaterial.mAmount * stack.stackSize);
        if (replacement == null) throw new IllegalStateException("Missing PGM sludge output for " + stack);
        return replacement;
    }
}
