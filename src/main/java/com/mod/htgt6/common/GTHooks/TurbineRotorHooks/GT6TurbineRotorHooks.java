package com.mod.htgt6.common.GTHooks.TurbineRotorHooks;

import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.data.MT;
import gregapi.data.OP;
import com.mod.htgt6.common.handler.recipe.RM.AssemblerRecipeHandler;
import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;
import gregapi.oredict.OreDictMaterial;
import net.minecraft.item.ItemStack;

/** Registration and reusable public entry point for HTGT6 turbine rotors. */
public final class GT6TurbineRotorHooks {
    public static final ItemHTGT6TurbineRotor ITEM = new ItemHTGT6TurbineRotor();
    private static boolean itemRegistered;
    private static boolean recipesRegistered;

    private GT6TurbineRotorHooks() {}

    public static synchronized void registerItem() {
        if (itemRegistered) return;
        GameRegistry.registerItem(ITEM, "gt6_turbine_rotor");
        itemRegistered = true;
    }

    /** Native HiTech6 recipes; do not publish to the GT6 map imported by Electronics Factory. */
    public static synchronized void registerRecipes() {
        if (recipesRegistered) return;
        recipesRegistered = true;
        for (OreDictMaterial material : OreDictMaterial.MATERIAL_ARRAY) {
            if (!ItemHTGT6TurbineRotor.isUsableMaterial(material)) continue;
            for (TurbineRotorSize size : TurbineRotorSize.values()) {
                ItemStack components = OP.rotor.mat(material, size.componentCount);
                ItemStack shaft = OP.stickLong.mat(shaftMaterial(size), 1);
                ItemStack output = ITEM.create(size, material);
                if (!valid(components) || !valid(shaft) || !valid(output)) continue;
                int tier = 1;
                while (tier < MachineTierSystem.MAX_TIER
                        && size.assemblyEUt > MachineTierSystem.voltage(tier)) tier++;
                AssemblerRecipeHandler.addRecipe(new ItemStack[] {components, shaft}, output,
                        (int) size.assemblyDuration, (int) size.assemblyEUt, tier);
            }
        }
    }

    public static ItemStack create(TurbineRotorSize size, OreDictMaterial material) {
        return ITEM.create(size, material);
    }

    public static TurbineRotorStats stats(ItemStack stack) {
        return ITEM.getStats(stack);
    }

    public static boolean damage(ItemStack stack, long amount) {
        return ITEM.damageRotor(stack, amount);
    }

    private static OreDictMaterial shaftMaterial(TurbineRotorSize size) {
        switch (size) {
            case SMALL: return MT.Magnalium;
            case NORMAL: return MT.Titanium;
            case LARGE: return MT.TungstenSteel;
            case HUGE: return MT.Americium;
            default: return MT.Steel;
        }
    }

    private static boolean valid(ItemStack stack) {
        return stack != null && stack.getItem() != null && stack.stackSize > 0;
    }
}
