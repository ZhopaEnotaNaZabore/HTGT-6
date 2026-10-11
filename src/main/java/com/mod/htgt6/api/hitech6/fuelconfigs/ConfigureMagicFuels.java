package com.mod.htgt6.api.hitech6.fuelconfigs;


import com.mod.htgt6.common.TE.MagicEnergyGeneratorTE;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ConfigureMagicFuels {

    public static void initFuels() {

        // ==========================================
        // DYNAMIC MAGIC FUEL REGISTRATION
        // ==========================================
        // Item overloads accept every metadata variant.  For an exact GT6 OP
        // MetaItem use addMagicFuel(new ItemStack(..., metadata), totalEu).

        // Tier 1: Low-tier Alchemical ingredients & reagents
        MagicEnergyGeneratorTE.addMagicFuel(Items.blaze_powder, 8000.0);
        MagicEnergyGeneratorTE.addMagicFuel(Items.magma_cream, 12000.0);
        MagicEnergyGeneratorTE.addMagicFuel(Items.redstone, 10000.0);
        //надо пофиксить
        MagicEnergyGeneratorTE.addMagicFuel(Items.ghast_tear, 32000.0);

        // Tier 2: Mid-tier Rare Mystical artifacts
        MagicEnergyGeneratorTE.addMagicFuel(Items.ender_eye, 50000.0);
        MagicEnergyGeneratorTE.addMagicFuel(Items.golden_apple, 100000.0); // Standard Golden Apple

        // Tier 3: High-tier Legendary / Relic tier items
        MagicEnergyGeneratorTE.addMagicFuel(Items.nether_star, 12000000.0);

        // Custom Mod Items Example:
        // If you have custom magical materials or items registered elsewhere:
        // MagicEnergyGeneratorTE.addMagicFuel(MyModItems.magical_crystal, 250000.0);
    }

    /** API entry point for integrations that obtain a GT6 OP/MetaItem stack. */
    public static void addMetaFuel(ItemStack stack, long totalEuValue) {
        MagicEnergyGeneratorTE.addMagicFuel(stack, totalEuValue);
    }

}
