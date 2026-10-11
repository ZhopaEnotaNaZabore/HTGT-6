// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.integratedgt;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class IntegratedGTFromEio {

    private IntegratedGTFromEio() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedGTRecipeMaps.add("EIO.zs:57", "gt.recipe.injector", false, 8L, 620L, new long[] {10000L}, new String[] {"<minecraft:glass_bottle>*1"}, new String[] {"<liquid:soularium.molten>*576"}, new String[] {"null"}, new String[] {"<EnderIO:itemSoulVessel>"}, settings, logger);
        GeneratedGTRecipeMaps.add("EIO.zs:63", "gt.recipe.assembler", false, 64L, 4600L, new long[] {10000L}, new String[] {"<EnderIO:itemBasicCapacitor:1>*1", "<gregtech:gt.meta.plate:8725>*1", "<minecraft:skull:2>*1", "<gregtech:gt.meta.plateCurved:140>*8", "<gregtech:gt.meta.wireFine:8728>*32", "<gregapi:gt.integrated_circuit:12>*0"}, new String[] {"<liquid:soularium.molten>*288"}, new String[] {"null"}, new String[] {"<EnderIO:itemFrankenSkull:1>"}, settings, logger);
        GeneratedGTRecipeMaps.add("EIO.zs:66", "gt.recipe.injector", false, 8L, 890L, new long[] {10000L}, new String[] {"<EnderIO:itemAlloy:7>*1"}, new String[] {"<liquid:lava>*5"}, new String[] {"<liquid:soularium.molten>*144"}, new String[] {"null"}, settings, logger);
        GeneratedGTRecipeMaps.add("EIO.zs:93", "gt.recipe.assembler", false, 20L, 1600L, new long[] {10000L}, new String[] {"<gregtech:gt.multitileentity:31022>*1", "<EnderIO:itemBasicCapacitor:1>*1", "<gregtech:gt.meta.wireFine:8728>*16", "<gregtech:gt.meta.stick:140>*4", "<gregtech:gt.meta.plateTiny:8728>*16", "<gregtech:gt.meta.foil:8733>*32", "<gregapi:gt.integrated_circuit:16>*0"}, new String[] {"<liquid:molten.solderingalloy>*576"}, new String[] {"null"}, new String[] {"<EnderIO:blockSolarPanel>*1"}, settings, logger);
        GeneratedGTRecipeMaps.add("EIO.zs:97", "gt.recipe.assembler", false, 77L, 1600L, new long[] {10000L}, new String[] {"<EnderIO:blockSolarPanel>*1", "<EnderIO:itemBasicCapacitor:2>*1", "<EnderIO:itemMaterial:5>*4", "<gregtech:gt.meta.plateTiny:8725>*32", "<gregtech:gt.meta.wireFine:8707>*16", "<gregtech:gt.meta.stickLong:600>*4", "<gregapi:gt.integrated_circuit:16>*0"}, new String[] {"<liquid:molten.solderingalloy>*576"}, new String[] {"null"}, new String[] {"<EnderIO:blockSolarPanel:1>*1"}, settings, logger);
    }
}
