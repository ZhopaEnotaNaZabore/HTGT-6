package com.mod.htgt6.api.hitech6.fuelconfigs;

import com.mod.htgt6.common.TE.RadioisotopeGeneratorTE;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.data.MT;
import gregapi.data.OP;
import ic2.api.item.IC2Items;
import net.minecraft.item.ItemStack;

public class RadioISOfuelConfig {

    public static void init() {

        // Th-232
        registerFuel(OP.dust.mat(MT.Th, 1), 1, 32000, 48,
                OP.dust.mat(MT.Pb, 1), 0.8,
                OP.dust.mat(MT.Bi, 1), 0.22,
                OP.dustSmall.mat(MT.La, 1), 0.18,
                OP.dustSmall.mat(MT.Lu, 1), 0.04,
                OP.dustSmall.mat(MT.Ra, 1), 0.01,
                OP.dustSmall.mat(MT.Po, 1), 0.01);
        //U-238
        registerFuel(OP.dust.mat(MT.U_238, 1), 4, 48000, 156, OP.dust.mat(MT.Pb, 1), 0.8,    OP.dustSmall.mat(MT.Ba, 1), 0.42, OP.dustSmall.mat(MT.Sr, 1), 0.09, OP.dustSmall.mat(MT.Cs, 1),0.06 );
        //U-235
        registerFuel(OP.dust.mat(MT.Uranium235, 1), 4, 10000, 856, OP.dust.mat(MT.Pb, 1), 0.8,    OP.dustSmall.mat(MT.Ba, 1), 0.62, OP.dustSmall.mat(MT.Sr, 1), 0.15, OP.dustSmall.mat(MT.Cs, 1),0.12 );
        //U-233
        registerFuel(OP.dust.mat(MT.U_233, 1), 4, 5000, 1121, OP.dust.mat(MT.Pb, 1), 0.8,    OP.dustSmall.mat(MT.Ba, 1), 0.76, OP.dustSmall.mat(MT.Sr, 1), 0.22, OP.dustSmall.mat(MT.Cs, 1),0.18 );

        registerIC2ExperimentalRods();
        registerGT6Rods();
    }

    /** IC2 Experimental uranium and MOX rods, including dual and quad assemblies. */
    private static void registerIC2ExperimentalRods() {
        registerIC2Rod(IC2Items.getItem("reactorUraniumSimple"), IC2Items.getItem("reactorDepletedUraniumSimple"), 1,
                10000, 856);
        registerIC2Rod(IC2Items.getItem("reactorUraniumDual"), IC2Items.getItem("reactorDepletedUraniumDual"), 2,
                10000, 856);
        registerIC2Rod(IC2Items.getItem("reactorUraniumQuad"), IC2Items.getItem("reactorDepletedUraniumQuad"), 4,
                10000, 856);

        registerIC2Rod(IC2Items.getItem("reactorMOXSimple"), IC2Items.getItem("reactorDepletedMOXSimple"), 1,
                8000, 1200);
        registerIC2Rod(IC2Items.getItem("reactorMOXDual"), IC2Items.getItem("reactorDepletedMOXDual"), 2,
                8000, 1200);
        registerIC2Rod(IC2Items.getItem("reactorMOXQuad"), IC2Items.getItem("reactorDepletedMOXQuad"), 4,
                8000, 1200);
    }

    private static void registerIC2Rod(ItemStack fuel, ItemStack depleted, int rodCount,
                                       int decayTime, int euPerRod) {
        ItemStack input = one(fuel);
        ItemStack output = one(depleted);
        if (input == null || output == null) return;
        registerFuel(input, 4, decayTime, euPerRod * rodCount,
                output, 1.0,
                OP.dustSmall.mat(MT.Ba, rodCount), 0.42,
                OP.dustSmall.mat(MT.Sr, rodCount), 0.12,
                OP.dustSmall.mat(MT.Cs, rodCount), 0.08);
    }

    /**
     * GT6 nuclear rods are MultiTileEntity items. IDs 92xx are active rods;
     * their matching 93xx IDs are the depleted products.
     */
    private static void registerGT6Rods() {
        MultiTileEntityRegistry registry = MultiTileEntityRegistry.getRegistry("gt.multitileentity");
        if (registry == null) return;

        registerGT6Rod(registry, 9210, 9310, 32000, 48,    // Thorium-232
                OP.dustSmall.mat(MT.La, 1), 0.18,
                OP.dustSmall.mat(MT.Lu, 1), 0.04);
        registerGT6Rod(registry, 9219, 9319, 36000, 96);   // Cyanite
        registerGT6Rod(registry, 9220, 9320, 48000, 156);  // Uranium-238
        registerGT6Rod(registry, 9221, 9321, 10000, 856);  // Uranium-235
        registerGT6Rod(registry, 9222, 9322, 5000, 1121);  // Uranium-233
        registerGT6Rod(registry, 9229, 9329, 24000, 384);  // Yellorium
        registerGT6Rod(registry, 9230, 9330, 30000, 640);  // Plutonium-244
        registerGT6Rod(registry, 9231, 9331, 7000, 1536);  // Plutonium-241
        registerGT6Rod(registry, 9232, 9332, 9000, 1280);  // Plutonium-243
        registerGT6Rod(registry, 9233, 9333, 8000, 1408);  // Plutonium-239
        registerGT6Rod(registry, 9239, 9339, 18000, 768);  // Blutonium
        registerGT6Rod(registry, 9240, 9340, 6500, 1792);  // Americium-245
        registerGT6Rod(registry, 9241, 9341, 7500, 1664);  // Americium-241
        registerGT6Rod(registry, 9249, 9349, 14000, 1024); // Ludicrite
        registerGT6Rod(registry, 9250, 9350, 20000, 512);  // Cobalt-60
        registerGT6Rod(registry, 9260, 9360, 6000, 2048,   // Enriched Naquadah
                OP.dustSmall.mat(MT.La, 1), 0.12,
                OP.dustSmall.mat(MT.Lu, 1), 0.08);
        registerGT6Rod(registry, 9261, 9361, 4000, 4096,   // Naquadria
                OP.dustSmall.mat(MT.La, 1), 0.16,
                OP.dustSmall.mat(MT.Lu, 1), 0.12);
    }

    private static void registerGT6Rod(MultiTileEntityRegistry registry, int fuelId, int depletedId,
                                       int decayTime, int euPerTick, Object... rareEarthOutputs) {
        ItemStack fuel = registry.getItem(fuelId);
        ItemStack depleted = registry.getItem(depletedId);
        if (fuel == null || depleted == null) return;
        Object[] outputs = new Object[10 + rareEarthOutputs.length];
        outputs[0] = depleted; outputs[1] = 1.0;
        outputs[2] = OP.dustSmall.mat(MT.Ba, 1); outputs[3] = 0.36;
        outputs[4] = OP.dustSmall.mat(MT.Sr, 1); outputs[5] = 0.12;
        outputs[6] = OP.dustSmall.mat(MT.Cs, 1); outputs[7] = 0.08;
        outputs[8] = OP.dustSmall.mat(MT.Po, 1); outputs[9] = 0.02;
        System.arraycopy(rareEarthOutputs, 0, outputs, 10, rareEarthOutputs.length);
        registerFuel(fuel, 4, decayTime, euPerTick, outputs);
    }

    private static ItemStack one(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return null;
        ItemStack copy = stack.copy();
        copy.stackSize = 1;
        return copy;
    }

    private static void registerFuel(ItemStack fuelStack, int tier, int decayTime, int euPerTick, Object... outputs) {
        if (fuelStack == null || fuelStack.getItem() == null) return;

        RadioisotopeGeneratorTE.IsotopeFuel fuelData = new RadioisotopeGeneratorTE.IsotopeFuel(tier, decayTime, euPerTick);

        for (int i = 0; i < outputs.length; i += 2) {
            if (i + 1 < outputs.length && outputs[i] instanceof ItemStack && outputs[i + 1] instanceof Double) {
                ItemStack stack = (ItemStack) outputs[i];
                double chance = (Double) outputs[i + 1];
                fuelData.addOutput(stack, chance);
            }
        }

        RadioisotopeGeneratorTE.addIsotopeFuel(fuelStack, fuelData);
    }
}
