package com.mod.htgt6.common.handler.recipe.RM;

import com.mod.htgt6.common.item.htgt6.Tools.HiTech6Molds;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;

import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.oredict.OreDictMaterial;


import net.minecraft.item.ItemStack;


import net.minecraftforge.fluids.FluidStack;
import java.util.ArrayList;
import java.util.List;

import static com.mod.htgt6.api.hitech6.RM.RecipeMap.fluid;

public class SolidifierRecipeHandler {

    public static final List<SolidifierRecipe> recipes = new ArrayList<SolidifierRecipe>();
    private static boolean recipesRegistered;

    public static class SolidifierRecipe extends ChemicalPlantRecipe {
        public final ItemStack mold;
        public final String moldType; // Kept for display/debug compatibility.
        public final FluidStack fluidInput;
        public final ItemStack output;
        public final int duration;
        public final long euPerTick;
        public final int tier;
        public final int selector;

        public SolidifierRecipe(ItemStack mold, FluidStack fluidInput, ItemStack output, int duration, long euPerTick, int tier) {
            super("HTGT6 Solidifier", new ItemStack[0], new ItemStack[] { output.copy() },
                    new int[] { 10000 }, new FluidStack[] { fluidInput.copy() }, new FluidStack[0],
                    duration, (int) Math.max(1L, Math.min(Integer.MAX_VALUE, euPerTick)), tier,
                    selectorForMold(mold));
            this.mold = mold.copy();
            this.mold.stackSize = 1;
            this.moldType = HiTech6Molds.getMoldName(this.mold);
            this.fluidInput = fluidInput.copy();
            this.output = output.copy();
            this.duration = duration;
            this.euPerTick = euPerTick;
            this.tier = tier;
            this.selector = selectorForMold(mold);
        }

        public SolidifierRecipe(int selector, FluidStack fluidInput, ItemStack output,
                                int duration, long euPerTick, int tier) {
            super("HTGT6 Solidifier", new ItemStack[0], new ItemStack[] { output.copy() },
                    new int[] { 10000 }, new FluidStack[] { fluidInput.copy() }, new FluidStack[0],
                    duration, (int) Math.max(1L, Math.min(Integer.MAX_VALUE, euPerTick)), tier, selector);
            this.mold = null;
            this.moldType = "selector." + selector;
            this.fluidInput = fluidInput.copy();
            this.output = output.copy();
            this.duration = duration;
            this.euPerTick = euPerTick;
            this.tier = tier;
            this.selector = selector;
        }
    }

    /** New virtual selector layout: 1 ingot, 2 plate, then the remaining legacy shapes. */
    public static int selectorForMold(ItemStack mold) {
        if (mold == null) return -1;
        switch (mold.getItemDamage()) {
            case HiTech6Molds.META_INGOT: return 1;
            case HiTech6Molds.META_PLATE: return 2;
            case HiTech6Molds.META_BOLT: return 3;
            case HiTech6Molds.META_SCREW: return 4;
            case HiTech6Molds.META_ROD: return 5;
            case HiTech6Molds.META_LONG_ROD: return 6;
            case HiTech6Molds.META_BALL: return 7;
            case HiTech6Molds.META_ROTOR: return 8;
            case HiTech6Molds.META_SMALL_CASING: return 9;
            default: return -1;
        }
    }

    /**
     * Конструктор
     */
    public static void addRecipe(ItemStack mold, net.minecraftforge.fluids.Fluid gtFluid, int fluidAmount, ItemStack output, int duration, int euPerTick, int tier) {
        addRecipe(mold, fluid(gtFluid, fluidAmount), output, duration, euPerTick, tier);
    }

    /** Preferred overload for a prepared GT6 FluidStack. */
    public static void addRecipe(ItemStack mold, FluidStack fluidInput, ItemStack output,
                                 int duration, int euPerTick, int tier) {
        if (mold == null || fluidInput == null || output == null || output.getItem() == null) return;

        if (!HiTech6Molds.isMold(mold) || mold.getItemDamage() == HiTech6Molds.META_EMPTY) return;

        // Кросс мод регистри
        recipes.add(new SolidifierRecipe(mold, fluidInput, output, duration, euPerTick, tier));
    }

    /** Addon-facing constructor for new virtual configurations without registering a physical mold. */
    public static SolidifierRecipe addRecipe(int selectorTag, FluidStack fluidInput, ItemStack output,
                                             int duration, int euPerTick, int tier) {
        if (selectorTag < 1 || selectorTag > 24 || fluidInput == null || fluidInput.getFluid() == null
                || fluidInput.amount <= 0 || output == null || output.getItem() == null) return null;
        SolidifierRecipe recipe = new SolidifierRecipe(selectorTag, fluidInput, output,
                Math.max(1, duration), Math.max(1, euPerTick), Math.max(1, tier));
        recipes.add(recipe);
        return recipe;
    }

    public static void registerRecipes() {
        if (recipesRegistered) return;
        if (HiTech6Molds.emptyMold == null) return;
        // ToLowCase
        ItemStack bolt = HiTech6Molds.stack(HiTech6Molds.META_BOLT);
        ItemStack screw = HiTech6Molds.stack(HiTech6Molds.META_SCREW);
        ItemStack rod = HiTech6Molds.stack(HiTech6Molds.META_ROD);
        ItemStack longrod = HiTech6Molds.stack(HiTech6Molds.META_LONG_ROD);
        ItemStack ingot = HiTech6Molds.stack(HiTech6Molds.META_INGOT);
        ItemStack plate = HiTech6Molds.stack(HiTech6Molds.META_PLATE);
        ItemStack ball = HiTech6Molds.stack(HiTech6Molds.META_BALL);
        ItemStack rotor = HiTech6Molds.stack(HiTech6Molds.META_ROTOR);
        ItemStack casingSmall = HiTech6Molds.stack(HiTech6Molds.META_SMALL_CASING);


        // Предохранитель, без него крашит. MT.iron LV tier
        //Iron
        if (MT.Iron != null && MT.Iron.mLiquid != null) {
            // Ingot Mold
            addRecipe(ingot, MT.Iron.mLiquid.getFluid(), 144, new ItemStack(net.minecraft.init.Items.iron_ingot), 200, 16, 1);

            // Plate Mold
            addRecipe(plate, MT.Iron.mLiquid.getFluid(), 144, OP.plate.mat(MT.Iron, 1), 200, 16, 1);

            // Bolt Mold
            addRecipe(bolt, MT.Iron.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Iron, 1), 60, 16, 1);

            //LongRod
            addRecipe(longrod, MT.Iron.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Iron, 1), 200, 16, 1);

            //screw
            addRecipe(screw, MT.Iron.mLiquid.getFluid(), 18, OP.screw.mat(MT.Iron, 1), 60, 16, 1);

            //rotor
            addRecipe(rotor, MT.Iron.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Iron, 1), 460, 16, 1);
            //rod
            addRecipe(rod, MT.Iron.mLiquid.getFluid(), 72, OP.rod.mat(MT.Iron, 1), 200, 16, 1);
            //casing
            addRecipe(casingSmall, MT.Iron.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Iron, 1), 200, 16, 1);
        }
        //РАЗДЕЛЕНИЕ ТУТ ДО НЕКСТ МАТЕРИАЛА
        //Zinc LV
        if (MT.Zn != null && MT.Zn.mLiquid != null) {
            // Ingot Mold
            addRecipe(ingot, MT.Zn.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Zn, 1), 200, 16, 1);

            // Plate Mold
            addRecipe(plate, MT.Zn.mLiquid.getFluid(), 144, OP.plate.mat(MT.Zn, 1), 200, 16, 1);

            // Bolt Mold
            addRecipe(bolt, MT.Zn.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Zn, 1), 60, 16, 1);

            //LongRod
            addRecipe(longrod, MT.Zn.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Zn, 1), 200, 16, 1);

            //screw
            addRecipe(screw, MT.Zn.mLiquid.getFluid(), 18, OP.screw.mat(MT.Zn, 1), 60, 16, 1);

            //rotor
            addRecipe(rotor, MT.Zn.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Zn, 1), 460, 16, 1);
            //rod
            addRecipe(rod, MT.Zn.mLiquid.getFluid(), 72, OP.rod.mat(MT.Zn, 1), 200, 16, 1);
            //casing
            addRecipe(casingSmall, MT.Zn.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Zn, 1), 200, 16, 1);
        }
        //РАЗДЕЛЕНИЕ ТУТ ДО НЕКСТ МАТЕРИАЛА
        //Copper LV
        if (MT.Cu != null && MT.Cu.mLiquid != null) {


            // Ingot Mold
           addRecipe(ingot, MT.Cu.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Cu, 1), 200, 16, 1);

            // Plate Mold
            addRecipe(plate, MT.Cu.mLiquid.getFluid(), 144, OP.plate.mat(MT.Cu, 1), 200, 16, 1);

            // Bolt Mold
            addRecipe(bolt, MT.Cu.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Cu, 1), 60, 16, 1);

            //LongRod
            addRecipe(longrod, MT.Cu.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Cu, 1), 200, 16, 1);

            //screw
            addRecipe(screw, MT.Cu.mLiquid.getFluid(), 18, OP.screw.mat(MT.Cu, 1), 60, 16, 1);

            //rotor
            addRecipe(rotor, MT.Cu.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Cu, 1), 460, 16, 1);
            //rod
            addRecipe(rod, MT.Cu.mLiquid.getFluid(), 72, OP.rod.mat(MT.Cu, 1), 200, 16, 1);
            //casing
            addRecipe(casingSmall, MT.Cu.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Cu, 1), 200, 16, 1);
        }
        //РАЗДЕЛЕНИЕ ТУТ ДО НЕКСТ МАТЕРИАЛА
        //Tin LV
        if (MT.Sn != null && MT.Sn.mLiquid != null) {
            // Ingot Mold
            addRecipe(ingot, MT.Sn.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Sn, 1), 200, 16, 1);

            // Plate Mold
            addRecipe(plate, MT.Sn.mLiquid.getFluid(), 144, OP.plate.mat(MT.Sn, 1), 200, 16, 1);

            // Bolt Mold
            addRecipe(bolt, MT.Sn.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Sn, 1), 60, 16, 1);

            //LongRod
            addRecipe(longrod, MT.Sn.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Sn, 1), 200, 16, 1);

            //screw
            addRecipe(screw, MT.Sn.mLiquid.getFluid(), 18, OP.screw.mat(MT.Sn, 1), 60, 16, 1);

            //rotor
            addRecipe(rotor, MT.Sn.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Sn, 1), 460, 16, 1);
            //rod
            addRecipe(rod, MT.Sn.mLiquid.getFluid(), 72, OP.rod.mat(MT.Sn, 1), 200, 16, 1);
            //casing
            addRecipe(casingSmall, MT.Sn.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Sn, 1), 200, 16, 1);
        }

        //РАЗДЕЛЕНИЕ ТУТ ДО НЕКСТ МАТЕРИАЛА
        //Gold LV
        if (MT.Au != null && MT.Au.mLiquid != null) {
            // Ingot Mold
            addRecipe(ingot, MT.Au.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Au, 1), 200, 16, 1);

            // Plate Mold
            addRecipe(plate, MT.Au.mLiquid.getFluid(), 144, OP.plate.mat(MT.Au, 1), 200, 16, 1);

            // Bolt Mold
            addRecipe(bolt, MT.Au.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Au, 1), 60, 16, 1);

            //LongRod
            addRecipe(longrod, MT.Au.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Au, 1), 200, 16, 1);

            //screw
            addRecipe(screw, MT.Au.mLiquid.getFluid(), 18, OP.screw.mat(MT.Au, 1), 60, 16, 1);

            //rotor
            addRecipe(rotor, MT.Au.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Au, 1), 460, 16, 1);
            //rod
            addRecipe(rod, MT.Au.mLiquid.getFluid(), 72, OP.rod.mat(MT.Au, 1), 200, 16, 1);
            //casing
            addRecipe(casingSmall, MT.Au.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Au, 1), 200, 16, 1);
        }
        // Silver LV
        if (MT.Ag != null && MT.Ag.mLiquid != null) {
            addRecipe(ingot, MT.Ag.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Ag, 1), 200, 16, 1);
            addRecipe(plate, MT.Ag.mLiquid.getFluid(), 144, OP.plate.mat(MT.Ag, 1), 200, 16, 1);
            addRecipe(bolt, MT.Ag.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Ag, 1), 60, 16, 1);
            addRecipe(longrod, MT.Ag.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Ag, 1), 200, 16, 1);
            addRecipe(screw, MT.Ag.mLiquid.getFluid(), 18, OP.screw.mat(MT.Ag, 1), 60, 16, 1);
            addRecipe(rotor, MT.Ag.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Ag, 1), 460, 16, 1);
            addRecipe(rod, MT.Ag.mLiquid.getFluid(), 72, OP.rod.mat(MT.Ag, 1), 200, 16, 1);
            addRecipe(casingSmall, MT.Ag.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Ag, 1), 200, 16, 1);
        }
        if (MT.Bronze != null && MT.Bronze.mLiquid != null) {
            addRecipe(ingot, MT.Bronze.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Bronze, 1), 200, 16, 1);
            addRecipe(plate, MT.Bronze.mLiquid.getFluid(), 144, OP.plate.mat(MT.Bronze, 1), 200, 16, 1);
            addRecipe(bolt, MT.Bronze.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Bronze, 1), 60, 16, 1);
            addRecipe(longrod, MT.Bronze.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Bronze, 1), 200, 16, 1);
            addRecipe(screw, MT.Bronze.mLiquid.getFluid(), 18, OP.screw.mat(MT.Bronze, 1), 60, 16, 1);
            addRecipe(rotor, MT.Bronze.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Bronze, 1), 460, 16, 1);
            addRecipe(rod, MT.Bronze.mLiquid.getFluid(), 72, OP.rod.mat(MT.Bronze, 1), 200, 16, 1);
            addRecipe(casingSmall, MT.Bronze.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Bronze, 1), 200, 16, 1);
        }
        // Steel MV (Tier 2, 63 EU/t)
        if (MT.Steel != null && MT.Steel.mLiquid != null) {
            addRecipe(ingot, MT.Steel.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Steel, 1), 200, 63, 2);
            addRecipe(plate, MT.Steel.mLiquid.getFluid(), 144, OP.plate.mat(MT.Steel, 1), 200, 63, 2);
            addRecipe(bolt, MT.Steel.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Steel, 1), 60, 63, 2);
            addRecipe(longrod, MT.Steel.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Steel, 1), 200, 63, 2);
            addRecipe(screw, MT.Steel.mLiquid.getFluid(), 18, OP.screw.mat(MT.Steel, 1), 60, 63, 2);
            addRecipe(rotor, MT.Steel.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Steel, 1), 460, 63, 2);
            addRecipe(rod, MT.Steel.mLiquid.getFluid(), 72, OP.rod.mat(MT.Steel, 1), 200, 63, 2);
            addRecipe(casingSmall, MT.Steel.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Steel, 1), 200, 63, 2);
        }
        if (MT.Electrum != null && MT.Electrum.mLiquid != null) {
            addRecipe(ingot, MT.Electrum.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Electrum, 1), 200, 16, 1);
            addRecipe(plate, MT.Electrum.mLiquid.getFluid(), 144, OP.plate.mat(MT.Electrum, 1), 200, 16, 1);
            addRecipe(bolt, MT.Electrum.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Electrum, 1), 60, 16, 1);
            addRecipe(longrod, MT.Electrum.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Electrum, 1), 200, 16, 1);
            addRecipe(screw, MT.Electrum.mLiquid.getFluid(), 18, OP.screw.mat(MT.Electrum, 1), 60, 16, 1);
            addRecipe(rotor, MT.Electrum.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Electrum, 1), 460, 16, 1);
            addRecipe(rod, MT.Electrum.mLiquid.getFluid(), 72, OP.rod.mat(MT.Electrum, 1), 200, 16, 1);
            addRecipe(casingSmall, MT.Electrum.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Electrum, 1), 200, 16, 1);
        }
        // Cupronickel LV
        if (MT.Cupronickel != null && MT.Cupronickel.mLiquid != null) {
            addRecipe(ingot, MT.Cupronickel.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Cupronickel, 1), 200, 16, 1);
            addRecipe(plate, MT.Cupronickel.mLiquid.getFluid(), 144, OP.plate.mat(MT.Cupronickel, 1), 200, 16, 1);
            addRecipe(bolt, MT.Cupronickel.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Cupronickel, 1), 60, 16, 1);
            addRecipe(longrod, MT.Cupronickel.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Cupronickel, 1), 200, 16, 1);
            addRecipe(screw, MT.Cupronickel.mLiquid.getFluid(), 18, OP.screw.mat(MT.Cupronickel, 1), 60, 16, 1);
            addRecipe(rotor, MT.Cupronickel.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Cupronickel, 1), 460, 16, 1);
            addRecipe(rod, MT.Cupronickel.mLiquid.getFluid(), 72, OP.rod.mat(MT.Cupronickel, 1), 200, 16, 1);
            addRecipe(casingSmall, MT.Cupronickel.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Cupronickel, 1), 200, 16, 1);
        }
        // TinAlloy LV
        if (MT.TinAlloy != null && MT.TinAlloy.mLiquid != null) {
            addRecipe(ingot, MT.TinAlloy.mLiquid.getFluid(), 144, OP.ingot.mat(MT.TinAlloy, 1), 200, 16, 1);
            addRecipe(plate, MT.TinAlloy.mLiquid.getFluid(), 144, OP.plate.mat(MT.TinAlloy, 1), 200, 16, 1);
            addRecipe(bolt, MT.TinAlloy.mLiquid.getFluid(), 18, OP.bolt.mat(MT.TinAlloy, 1), 60, 16, 1);
            addRecipe(longrod, MT.TinAlloy.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.TinAlloy, 1), 200, 16, 1);
            addRecipe(screw, MT.TinAlloy.mLiquid.getFluid(), 18, OP.screw.mat(MT.TinAlloy, 1), 60, 16, 1);
            addRecipe(rotor, MT.TinAlloy.mLiquid.getFluid(), 425, OP.rotor.mat(MT.TinAlloy, 1), 460, 16, 1);
            addRecipe(rod, MT.TinAlloy.mLiquid.getFluid(), 72, OP.rod.mat(MT.TinAlloy, 1), 200, 16, 1);
            addRecipe(casingSmall, MT.TinAlloy.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.TinAlloy, 1), 200, 16, 1);
        }
        // RedAlloy LV (Duration x3 = 600)
        if (MT.RedAlloy != null && MT.RedAlloy.mLiquid != null) {
            addRecipe(ingot, MT.RedAlloy.mLiquid.getFluid(), 144, OP.ingot.mat(MT.RedAlloy, 1), 600, 16, 1);
            addRecipe(plate, MT.RedAlloy.mLiquid.getFluid(), 144, OP.plate.mat(MT.RedAlloy, 1), 600, 16, 1);
            addRecipe(bolt, MT.RedAlloy.mLiquid.getFluid(), 18, OP.bolt.mat(MT.RedAlloy, 1), 180, 16, 1); // 60 * 3
            addRecipe(longrod, MT.RedAlloy.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.RedAlloy, 1), 600, 16, 1);
            addRecipe(screw, MT.RedAlloy.mLiquid.getFluid(), 18, OP.screw.mat(MT.RedAlloy, 1), 180, 16, 1); // 60 * 3
            addRecipe(rotor, MT.RedAlloy.mLiquid.getFluid(), 425, OP.rotor.mat(MT.RedAlloy, 1), 1380, 16, 1); // 460 * 3
            addRecipe(rod, MT.RedAlloy.mLiquid.getFluid(), 72, OP.rod.mat(MT.RedAlloy, 1), 600, 16, 1);
            addRecipe(casingSmall, MT.RedAlloy.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.RedAlloy, 1), 600, 16, 1);
        }
        if (MT.Si != null && MT.Si.mLiquid != null) {
            addRecipe(ingot, MT.Si.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Si, 1), 200, 63, 2);
            addRecipe(plate, MT.Si.mLiquid.getFluid(), 144, OP.plate.mat(MT.Si, 1), 200, 63, 2);
            addRecipe(bolt, MT.Si.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Si, 1), 60, 63, 2);
            addRecipe(longrod, MT.Si.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Si, 1), 200, 63, 2);
            addRecipe(screw, MT.Si.mLiquid.getFluid(), 18, OP.screw.mat(MT.Si, 1), 60, 63, 2);
            addRecipe(rotor, MT.Si.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Si, 1), 460, 63, 2);
            addRecipe(rod, MT.Si.mLiquid.getFluid(), 72, OP.rod.mat(MT.Si, 1), 200, 63, 2);
            addRecipe(casingSmall, MT.Si.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Si, 1), 200, 63, 2);
        }
        // ElectricalSteel LV (Duration x3 = 600)
        if (MT.ElectricalSteel != null && MT.ElectricalSteel.mLiquid != null) {
            addRecipe(ingot, MT.ElectricalSteel.mLiquid.getFluid(), 144, OP.ingot.mat(MT.ElectricalSteel, 1), 600, 16, 1);
            addRecipe(plate, MT.ElectricalSteel.mLiquid.getFluid(), 144, OP.plate.mat(MT.ElectricalSteel, 1), 600, 16, 1);
            addRecipe(bolt, MT.ElectricalSteel.mLiquid.getFluid(), 18, OP.bolt.mat(MT.ElectricalSteel, 1), 180, 16, 1);
            addRecipe(longrod, MT.ElectricalSteel.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.ElectricalSteel, 1), 600, 16, 1);
            addRecipe(screw, MT.ElectricalSteel.mLiquid.getFluid(), 18, OP.screw.mat(MT.ElectricalSteel, 1), 180, 16, 1);
            addRecipe(rotor, MT.ElectricalSteel.mLiquid.getFluid(), 425, OP.rotor.mat(MT.ElectricalSteel, 1), 1380, 16, 1);
            addRecipe(rod, MT.ElectricalSteel.mLiquid.getFluid(), 72, OP.rod.mat(MT.ElectricalSteel, 1), 600, 16, 1);
            addRecipe(casingSmall, MT.ElectricalSteel.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.ElectricalSteel, 1), 600, 16, 1);
        }
        // ObsidianSteel LV (Duration x3 = 600)
        if (MT.ObsidianSteel != null && MT.ObsidianSteel.mLiquid != null) {
            addRecipe(ingot, MT.ObsidianSteel.mLiquid.getFluid(), 144, OP.ingot.mat(MT.ObsidianSteel, 1), 600, 16, 1);
            addRecipe(plate, MT.ObsidianSteel.mLiquid.getFluid(), 144, OP.plate.mat(MT.ObsidianSteel, 1), 600, 16, 1);
            addRecipe(bolt, MT.ObsidianSteel.mLiquid.getFluid(), 18, OP.bolt.mat(MT.ObsidianSteel, 1), 180, 16, 1);
            addRecipe(longrod, MT.ObsidianSteel.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.ObsidianSteel, 1), 600, 16, 1);
            addRecipe(screw, MT.ObsidianSteel.mLiquid.getFluid(), 18, OP.screw.mat(MT.ObsidianSteel, 1), 180, 16, 1);
            addRecipe(rotor, MT.ObsidianSteel.mLiquid.getFluid(), 425, OP.rotor.mat(MT.ObsidianSteel, 1), 1380, 16, 1);
            addRecipe(rod, MT.ObsidianSteel.mLiquid.getFluid(), 72, OP.rod.mat(MT.ObsidianSteel, 1), 600, 16, 1);
            addRecipe(casingSmall, MT.ObsidianSteel.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.ObsidianSteel, 1), 600, 16, 1);
        }
        // PulsatingIron LV (Duration x3 = 600)
        if (MT.PulsatingIron != null && MT.PulsatingIron.mLiquid != null) {
            addRecipe(ingot, MT.PulsatingIron.mLiquid.getFluid(), 144, OP.ingot.mat(MT.PulsatingIron, 1), 600, 16, 1);
            addRecipe(plate, MT.PulsatingIron.mLiquid.getFluid(), 144, OP.plate.mat(MT.PulsatingIron, 1), 600, 16, 1);
            addRecipe(bolt, MT.PulsatingIron.mLiquid.getFluid(), 18, OP.bolt.mat(MT.PulsatingIron, 1), 180, 16, 1);
            addRecipe(longrod, MT.PulsatingIron.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.PulsatingIron, 1), 600, 16, 1);
            addRecipe(screw, MT.PulsatingIron.mLiquid.getFluid(), 18, OP.screw.mat(MT.PulsatingIron, 1), 180, 16, 1);
            addRecipe(rotor, MT.PulsatingIron.mLiquid.getFluid(), 425, OP.rotor.mat(MT.PulsatingIron, 1), 1380, 16, 1);
            addRecipe(rod, MT.PulsatingIron.mLiquid.getFluid(), 72, OP.rod.mat(MT.PulsatingIron, 1), 600, 16, 1);
            addRecipe(casingSmall, MT.PulsatingIron.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.PulsatingIron, 1), 600, 16, 1);
        }
        // Aluminium MV (Tier 2, 63 EU/t)
        if (MT.Aluminium != null && MT.Aluminium.mLiquid != null) {
            addRecipe(ingot, MT.Aluminium.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Aluminium, 1), 200, 63, 2);
            addRecipe(plate, MT.Aluminium.mLiquid.getFluid(), 144, OP.plate.mat(MT.Aluminium, 1), 200, 63, 2);
            addRecipe(bolt, MT.Aluminium.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Aluminium, 1), 60, 63, 2);
            addRecipe(longrod, MT.Aluminium.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Aluminium, 1), 200, 63, 2);
            addRecipe(screw, MT.Aluminium.mLiquid.getFluid(), 18, OP.screw.mat(MT.Aluminium, 1), 60, 63, 2);
            addRecipe(rotor, MT.Aluminium.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Aluminium, 1), 460, 63, 2);
            addRecipe(rod, MT.Aluminium.mLiquid.getFluid(), 72, OP.rod.mat(MT.Aluminium, 1), 200, 63, 2);
            addRecipe(casingSmall, MT.Aluminium.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Aluminium, 1), 200, 63, 2);
        }

        // StainlessSteel MV (Tier 2, 63 EU/t)
        if (MT.StainlessSteel != null && MT.StainlessSteel.mLiquid != null) {
            addRecipe(ingot, MT.StainlessSteel.mLiquid.getFluid(), 144, OP.ingot.mat(MT.StainlessSteel, 1), 200, 63, 2);
            addRecipe(plate, MT.StainlessSteel.mLiquid.getFluid(), 144, OP.plate.mat(MT.StainlessSteel, 1), 200, 63, 2);
            addRecipe(bolt, MT.StainlessSteel.mLiquid.getFluid(), 18, OP.bolt.mat(MT.StainlessSteel, 1), 60, 63, 2);
            addRecipe(longrod, MT.StainlessSteel.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.StainlessSteel, 1), 200, 63, 2);
            addRecipe(screw, MT.StainlessSteel.mLiquid.getFluid(), 18, OP.screw.mat(MT.StainlessSteel, 1), 60, 63, 2);
            addRecipe(rotor, MT.StainlessSteel.mLiquid.getFluid(), 425, OP.rotor.mat(MT.StainlessSteel, 1), 460, 63, 2);
            addRecipe(rod, MT.StainlessSteel.mLiquid.getFluid(), 72, OP.rod.mat(MT.StainlessSteel, 1), 200, 63, 2);
            addRecipe(casingSmall, MT.StainlessSteel.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.StainlessSteel, 1), 200, 63, 2);
        }
        // Invar LV (x3 time = 600 base)
        if (MT.Invar != null && MT.Invar.mLiquid != null) {
            addRecipe(ingot, MT.Invar.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Invar, 1), 600, 16, 1);
            addRecipe(plate, MT.Invar.mLiquid.getFluid(), 144, OP.plate.mat(MT.Invar, 1), 600, 16, 1);
            addRecipe(bolt, MT.Invar.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Invar, 1), 180, 16, 1);
            addRecipe(longrod, MT.Invar.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Invar, 1), 600, 16, 1);
            addRecipe(screw, MT.Invar.mLiquid.getFluid(), 18, OP.screw.mat(MT.Invar, 1), 180, 16, 1);
            addRecipe(rotor, MT.Invar.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Invar, 1), 1380, 16, 1);
            addRecipe(rod, MT.Invar.mLiquid.getFluid(), 72, OP.rod.mat(MT.Invar, 1), 600, 16, 1);
            addRecipe(casingSmall, MT.Invar.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Invar, 1), 600, 16, 1);
        }
        // Chromium LV (x3 time = 600 base)
        if (MT.Chromium != null && MT.Chromium.mLiquid != null) {
            addRecipe(ingot, MT.Chromium.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Chromium, 1), 600, 16, 1);
            addRecipe(plate, MT.Chromium.mLiquid.getFluid(), 144, OP.plate.mat(MT.Chromium, 1), 600, 16, 1);
            addRecipe(bolt, MT.Chromium.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Chromium, 1), 180, 16, 1);
            addRecipe(longrod, MT.Chromium.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Chromium, 1), 600, 16, 1);
            addRecipe(screw, MT.Chromium.mLiquid.getFluid(), 18, OP.screw.mat(MT.Chromium, 1), 180, 16, 1);
            addRecipe(rotor, MT.Chromium.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Chromium, 1), 1380, 16, 1);
            addRecipe(rod, MT.Chromium.mLiquid.getFluid(), 72, OP.rod.mat(MT.Chromium, 1), 600, 16, 1);
            addRecipe(casingSmall, MT.Chromium.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Chromium, 1), 600, 16, 1);
        }
        // Titanium HV (Tier 3, 412 EU/t)
        if (MT.Titanium != null && MT.Titanium.mLiquid != null) {
            addRecipe(ingot, MT.Titanium.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Titanium, 1), 200, 412, 3);
            addRecipe(plate, MT.Titanium.mLiquid.getFluid(), 144, OP.plate.mat(MT.Titanium, 1), 200, 412, 3);
            addRecipe(bolt, MT.Titanium.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Titanium, 1), 60, 412, 3);
            addRecipe(longrod, MT.Titanium.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Titanium, 1), 200, 412, 3);
            addRecipe(screw, MT.Titanium.mLiquid.getFluid(), 18, OP.screw.mat(MT.Titanium, 1), 60, 412, 3);
            addRecipe(rotor, MT.Titanium.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Titanium, 1), 460, 412, 3);
            addRecipe(rod, MT.Titanium.mLiquid.getFluid(), 72, OP.rod.mat(MT.Titanium, 1), 200, 412, 3);
            addRecipe(casingSmall, MT.Titanium.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Titanium, 1), 200, 412, 3);
        }
        // Cobalt HV (Tier 3, 412 EU/t)
        if (MT.Co != null && MT.Co.mLiquid != null) {
            addRecipe(ingot, MT.Co.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Co, 1), 200, 412, 3);
            addRecipe(plate, MT.Co.mLiquid.getFluid(), 144, OP.plate.mat(MT.Co, 1), 200, 412, 3);
            addRecipe(bolt, MT.Co.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Co, 1), 60, 412, 3);
            addRecipe(longrod, MT.Co.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Co, 1), 200, 412, 3);
            addRecipe(screw, MT.Co.mLiquid.getFluid(), 18, OP.screw.mat(MT.Co, 1), 60, 412, 3);
            addRecipe(rotor, MT.Co.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Co, 1), 460, 412, 3);
            addRecipe(rod, MT.Co.mLiquid.getFluid(), 72, OP.rod.mat(MT.Co, 1), 200, 412, 3);
            addRecipe(casingSmall, MT.Co.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Co, 1), 200, 412, 3);
        }
        // Platinum MV (x3 time = 600 base)
        if (MT.Platinum != null && MT.Platinum.mLiquid != null) {
            addRecipe(ingot, MT.Platinum.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Platinum, 1), 600, 16, 1);
            addRecipe(plate, MT.Platinum.mLiquid.getFluid(), 144, OP.plate.mat(MT.Platinum, 1), 600, 16, 1);
            addRecipe(bolt, MT.Platinum.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Platinum, 1), 180, 16, 1);
            addRecipe(longrod, MT.Platinum.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Platinum, 1), 600, 16, 1);
            addRecipe(screw, MT.Platinum.mLiquid.getFluid(), 18, OP.screw.mat(MT.Platinum, 1), 180, 16, 1);
            addRecipe(rotor, MT.Platinum.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Platinum, 1), 1380, 16, 1);
            addRecipe(rod, MT.Platinum.mLiquid.getFluid(), 72, OP.rod.mat(MT.Platinum, 1), 600, 16, 1);
            addRecipe(casingSmall, MT.Platinum.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Platinum, 1), 600, 16, 1);
        }
        // Palladium HV (x2 time = 400 base, Tier 3)
        if (MT.Palladium != null && MT.Palladium.mLiquid != null) {
            addRecipe(ingot, MT.Palladium.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Palladium, 1), 400, 412, 3);
            addRecipe(plate, MT.Palladium.mLiquid.getFluid(), 144, OP.plate.mat(MT.Palladium, 1), 400, 412, 3);
            addRecipe(bolt, MT.Palladium.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Palladium, 1), 120, 412, 3);
            addRecipe(longrod, MT.Palladium.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Palladium, 1), 400, 412, 3);
            addRecipe(screw, MT.Palladium.mLiquid.getFluid(), 18, OP.screw.mat(MT.Palladium, 1), 120, 412, 3);
            addRecipe(rotor, MT.Palladium.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Palladium, 1), 920, 412, 3);
            addRecipe(rod, MT.Palladium.mLiquid.getFluid(), 72, OP.rod.mat(MT.Palladium, 1), 400, 412, 3);
            addRecipe(casingSmall, MT.Palladium.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Palladium, 1), 400, 412, 3);
        }
        // Trinium HV (x5 time = 1000 base, Tier 3)
        if (MT.Trinium != null && MT.Trinium.mLiquid != null) {
            addRecipe(ingot, MT.Trinium.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Trinium, 1), 1000, 412, 3);
            addRecipe(plate, MT.Trinium.mLiquid.getFluid(), 144, OP.plate.mat(MT.Trinium, 1), 1000, 412, 3);
            addRecipe(bolt, MT.Trinium.mLiquid.getFluid(), 18, OP.bolt.mat(MT.Trinium, 1), 300, 412, 3);
            addRecipe(longrod, MT.Trinium.mLiquid.getFluid(), 144, OP.stickLong.mat(MT.Trinium, 1), 1000, 412, 3);
            addRecipe(screw, MT.Trinium.mLiquid.getFluid(), 18, OP.screw.mat(MT.Trinium, 1), 300, 412, 3);
            addRecipe(rotor, MT.Trinium.mLiquid.getFluid(), 425, OP.rotor.mat(MT.Trinium, 1), 2300, 412, 3);
            addRecipe(rod, MT.Trinium.mLiquid.getFluid(), 72, OP.rod.mat(MT.Trinium, 1), 1000, 412, 3);
            addRecipe(casingSmall, MT.Trinium.mLiquid.getFluid(), 72, OP.casingSmall.mat(MT.Trinium, 1), 1000, 412, 3);
        }

        // The ball mold (META-7) used to have an icon but no recipes at all.
        addBallRecipe(ball, MT.Iron, 200, 16, 1);
        addBallRecipe(ball, MT.Zn, 200, 16, 1);
        addBallRecipe(ball, MT.Cu, 200, 16, 1);
        addBallRecipe(ball, MT.Sn, 200, 16, 1);
        addBallRecipe(ball, MT.Au, 200, 16, 1);
        addBallRecipe(ball, MT.Ag, 200, 16, 1);
        addBallRecipe(ball, MT.Bronze, 200, 16, 1);
        addBallRecipe(ball, MT.Steel, 200, 63, 2);
        addBallRecipe(ball, MT.Electrum, 200, 16, 1);
        addBallRecipe(ball, MT.Cupronickel, 200, 16, 1);
        addBallRecipe(ball, MT.TinAlloy, 200, 16, 1);
        addBallRecipe(ball, MT.RedAlloy, 600, 16, 1);
        addBallRecipe(ball, MT.Si, 200, 63, 2);
        addBallRecipe(ball, MT.ElectricalSteel, 600, 16, 1);
        addBallRecipe(ball, MT.ObsidianSteel, 600, 16, 1);
        addBallRecipe(ball, MT.PulsatingIron, 600, 16, 1);
        addBallRecipe(ball, MT.Aluminium, 200, 63, 2);
        addBallRecipe(ball, MT.StainlessSteel, 200, 63, 2);
        addBallRecipe(ball, MT.Invar, 600, 16, 1);
        addBallRecipe(ball, MT.Chromium, 600, 16, 1);
        addBallRecipe(ball, MT.Titanium, 200, 412, 3);
        addBallRecipe(ball, MT.Co, 200, 412, 3);
        addBallRecipe(ball, MT.Platinum, 600, 16, 1);
        addBallRecipe(ball, MT.Palladium, 400, 412, 3);
        addBallRecipe(ball, MT.Trinium, 1000, 412, 3);


        if (MT.Naquadria != null && MT.Naquadria.mLiquid !=null){
            //test наквадрия
            addRecipe(ingot, MT.Naquadria.mLiquid.getFluid(), 144, OP.ingot.mat(MT.Naquadria, 1), 120, 8388608, 10);

        }
        recipesRegistered = true;
    }

    private static void addBallRecipe(ItemStack ball, OreDictMaterial material,
                                      int duration, int euPerTick, int tier) {
        if (material == null || material.mLiquid == null) return;
        addRecipe(ball, material.mLiquid.getFluid(), 144, OP.round.mat(material, 1), duration, euPerTick, tier);
    }


    public static SolidifierRecipe findRecipe(ItemStack moldSlot, FluidStack tankFluid, ItemStack outputSlot) {
        if (moldSlot == null || tankFluid == null) return null; //
        if (!HiTech6Molds.isMold(moldSlot) || moldSlot.getItemDamage() == HiTech6Molds.META_EMPTY) return null;

        for (SolidifierRecipe recipe : recipes) {
            if (recipe.mold != null && recipe.mold.isItemEqual(moldSlot)) {

                // FIXED: замена '==' на 'isFluidEqual()'
                if (tankFluid.isFluidEqual(recipe.fluidInput) && tankFluid.amount >= recipe.fluidInput.amount) { //[cite: 2]
                    if (canOutput(outputSlot, recipe.output)) {
                        return recipe;
                    }
                }
            }
        }
        return null;
    }

    public static boolean canOutput(ItemStack outputSlot, ItemStack recipeOutput) {
        if (outputSlot == null) return true;
        if (!outputSlot.isItemEqual(recipeOutput)) return false;
        return outputSlot.stackSize + recipeOutput.stackSize <= outputSlot.getMaxStackSize();
    }

    public static void outputRecipe(SolidifierRecipe recipe, ItemStack[] inventory) {
        if (inventory[3] == null) {
            inventory[3] = recipe.output.copy();
        } else {
            inventory[3].stackSize += recipe.output.stackSize;
        }
    }
//Оверклок
    public static int getOverclockedDuration(SolidifierRecipe recipe, int machineTier) {
        if (machineTier <= recipe.tier) return recipe.duration;
        int diff = machineTier - recipe.tier;
        int result = recipe.duration;
        for (int i = 0; i < diff; i++) result /= 2;
        return Math.max(result, 1);
    }
//Тир оверклок
    public static long getOverclockedEUt(SolidifierRecipe recipe, int machineTier) {
        return com.mod.htgt6.api.hitech6.Misc.MachineTierSystem.overclockEUt(
                recipe.euPerTick, recipe.tier, machineTier);
    }
}
