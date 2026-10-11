package com.mod.htgt6.common.handler.recipe.RM;

import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityBaseMachine;
import gregapi.data.IL;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;
import gregapi.util.ST;
import net.minecraft.item.ItemStack;

public class MetalFormerRecipeMap extends RecipeMap<MachineRecipe> {

    public static final MetalFormerRecipeMap INSTANCE = new MetalFormerRecipeMap();

    private MetalFormerRecipeMap() {
    }

    /** Helper container to associate a material with its tier and voltage requirements */
    public static class MaterialConfig {
        public final OreDictMaterial material;
        public final int minTier;
        public final long euPerTick;

        public MaterialConfig(OreDictMaterial material, int minTier) {
            this.material = material;
            this.minTier = minTier;
            int vIndex = Math.max(0, Math.min(minTier - 1, TileEntityBaseMachine.VOLTAGES.length - 1));
            this.euPerTick = TileEntityBaseMachine.VOLTAGES[vIndex];
        }

        public MaterialConfig(OreDictMaterial material, int minTier, long euPerTick) {
            this.material = material;
            this.minTier = minTier;
            this.euPerTick = euPerTick;
        }
    }

    public void registerRecipes() {
        MaterialConfig[] metals = new MaterialConfig[] {
                //LV T1
                new MaterialConfig(MT.Iron, 1, 16),
                new MaterialConfig(MT.Copper, 1, 18),
                new MaterialConfig(MT.Sn, 1, 12),
                new MaterialConfig(MT.Bronze, 1, 32),
                new MaterialConfig(MT.Ni, 1, 32),
                new MaterialConfig(MT.Ag, 1, 24),
                new MaterialConfig(MT.Pb, 1, 32),
                new MaterialConfig(MT.Au, 1, 24),
                new MaterialConfig(MT.Cr, 1, 32),
                new MaterialConfig(MT.Constantan, 1, 32),
                new MaterialConfig(MT.Cupronickel, 1, 32),
                new MaterialConfig(MT.AnnealedCopper, 1, 32),
                new MaterialConfig(MT.RedAlloy, 1, 32),
                new MaterialConfig(MT.Bi, 1, 32),






                //MV T2
                new MaterialConfig(MT.Steel, 2, 64),
                new MaterialConfig(MT.ElectricalSteel, 2, 64),
                new MaterialConfig(MT.Electrum, 2, 82),
                new MaterialConfig(MT.ObsidianSteel, 2, 64),
                new MaterialConfig(MT.Aluminium, 2, 48),
                new MaterialConfig(MT.PulsatingIron, 2, 64),
                new MaterialConfig(MT.Si, 2, 64),
                new MaterialConfig(MT.RedstoneAlloy, 2, 96),
                new MaterialConfig(MT.Cobalt, 2, 64),
                new MaterialConfig(MT.CobaltBrass, 2, 64),
                new MaterialConfig(MT.Kanthal, 2, 108),
                new MaterialConfig(MT.Zr, 2, 77),









                //HV T3
                new MaterialConfig(MT.Neodymium, 3, 512),
                new MaterialConfig(MT.VibrantAlloy, 3, 512),
                new MaterialConfig(MT.Ti, 3, 512),
                new MaterialConfig(MT.Pd, 3, 512),
                new MaterialConfig(MT.Pt, 3, 512),
                new MaterialConfig(MT.Ke, 3, 512),
                new MaterialConfig(MT.RoseGold, 3, 512),
                new MaterialConfig(MT.BlackSteel, 3, 512),
                new MaterialConfig(MT.BlueSteel, 3, 512),
                new MaterialConfig(MT.RedSteel, 3, 512),
                new MaterialConfig(MT.V, 3, 512),
                new MaterialConfig(MT.Uranium, 3, 512),
                new MaterialConfig(MT.Uranium235, 3, 512),
                new MaterialConfig(MT.U_233, 3, 512),
                new MaterialConfig(MT.Pu_238, 3, 512),
                new MaterialConfig(MT.Pu_239, 3, 512),
                new MaterialConfig(MT.Pu_240, 3, 512),
                new MaterialConfig(MT.Pu_241, 3, 512),
                new MaterialConfig(MT.Pu_243, 3, 512),
                new MaterialConfig(MT.Thorium, 3, 512),
                new MaterialConfig(MT.Nichrome, 3, 512),













                //EV T4 2048
                new MaterialConfig(MT.W, 4, 2048),
                new MaterialConfig(MT.TungstenSteel, 4, 2048),
                new MaterialConfig(MT.TungstenCarbide, 4, 2048),
                new MaterialConfig(MT.TungstenAlloy, 4, 2048),
                new MaterialConfig(MT.Ir, 4, 2048),
                new MaterialConfig(MT.Os, 4, 2048),
                new MaterialConfig(MT.EnderiumBase, 4, 2048),


                //IV T5 8192
                new MaterialConfig(MT.Iritanium, 5, 7266),
                new MaterialConfig(MT.Graphene, 5, 7266),
                new MaterialConfig(MT.Nb, 5, 7266),
                new MaterialConfig(MT.Enderium, 5, 7266),
                new MaterialConfig(MT.Nq, 5, 7266),


                //LUV T6 32768
                new MaterialConfig(MT.Osmiridium, 6, 28212),
                new MaterialConfig(MT.NaquadahEnriched, 6, 28212),



                //ZPM T7 131072
                new MaterialConfig(MT.Adamantium, 7, 88120),
                new MaterialConfig(MT.Naquadria, 7, 88120),
                new MaterialConfig(MT.Trinitanium, 7, 88120),






                //UV T8 524288
                new MaterialConfig(MT.Vibranium, 8, 301670),
                new MaterialConfig(MT.VibraniumSilver, 8, 301670),
                new MaterialConfig(MT.VibraniumSteel, 8, 301670),
                new MaterialConfig(MT.Vibramantium, 8, 301670),




                //PUV T9 2097152
                new MaterialConfig(MT.Trinaquadalloy, 9, 10240000),

                //UX T10 8388608
                new MaterialConfig(MT.Bedrockium, 10, 5412900),
                new MaterialConfig(MT.Bedrock_HSLA_Alloy, 10, 5412900),


                //OLV T11 33554432

                //OMV T12 134217728

                //OHV T13 536870912
                new MaterialConfig(MT.Infinity, 13, 536870912),
                new MaterialConfig(MT.Infinite, 13, 536870912),


                //OEV T14
                //OIV T15
                //MAX T16
                new MaterialConfig(MT.Bedrock, 16, 536870912 ),

        };

        // Standard Shapes
        registerBulkRecipes(metals, OP.ingot, 1, OP.stick, 2, IL.Shape_Extruder_Rod.get(1), 412);
        registerBulkRecipes(metals, OP.ingot, 1, OP.plate, 1, IL.Shape_Extruder_Plate.get(1), 536);
        registerBulkRecipes(metals, OP.ingot, 1, OP.plateCurved, 1, IL.Shape_Extruder_Plate_Curved.get(1), 536);
        registerBulkRecipes(metals, OP.ingot, 1, OP.stickLong, 1, IL.Shape_Extruder_Rod_Long.get(1), 412);
        registerBulkRecipes(metals, OP.ingot, 1, OP.bolt, 8, IL.Shape_Extruder_Bolt.get(1), 380);
        registerBulkRecipes(metals, OP.ingot, 1, OP.ring, 4, IL.Shape_Extruder_Ring.get(1), 360);
        registerBulkRecipes(metals, OP.ingot, 1, OP.casingSmall, 2, IL.Shape_Extruder_Ring.get(1), 360);
        registerBulkRecipes(metals, OP.ingot, 4, OP.gearGt, 1, IL.Shape_Extruder_Gear.get(1), 720);
        registerBulkRecipes(metals, OP.ingot, 1, OP.gearGtSmall, 1, IL.Shape_Extruder_Gear_Small.get(1), 400);
        registerBulkRecipes(metals, OP.ingot, 1, OP.foil, 4, IL.Shape_Extruder_Foil.get(1), 360);
        registerBulkRecipes(metals, OP.ingot, 1, OP.plateTiny, 9, IL.Shape_Extruder_Foil.get(1), 456);
        registerBulkRecipes(metals, OP.ingot, 9, OP.block, 1, IL.Shape_Extruder_Block.get(1), 1296);




        // GT6 pipes and wires are MTE stacks; RecipeMap.gt() preserves their NBT.
        registerBulkRecipes(metals, OP.ingot, 1, OP.pipeTiny, 2, IL.Shape_Extruder_Pipe_Tiny.get(1), 360);
        registerBulkRecipes(metals, OP.ingot, 1, OP.pipeSmall, 1, IL.Shape_Extruder_Pipe_Small.get(1), 380);
        registerBulkRecipes(metals, OP.ingot, 3, OP.pipeMedium, 1, IL.Shape_Extruder_Pipe_Medium.get(1), 412);
        registerBulkRecipes(metals, OP.ingot, 6, OP.pipeLarge, 1, IL.Shape_Extruder_Pipe_Large.get(1), 460);
        registerBulkRecipes(metals, OP.ingot, 12, OP.pipeHuge, 1, IL.Shape_Extruder_Pipe_Huge.get(1), 832);

        registerBulkRecipes(metals, OP.ingot, 1, OP.wireGt01, 2, IL.Shape_Extruder_Wire.get(1), 300);
    }

    public void registerBulkRecipes(MaterialConfig[] materials, OreDictPrefix inPrefix, long inAmount, OreDictPrefix outPrefix, long outAmount, ItemStack mold, int durationTicks) {
        if (mold == null) return;

        for (MaterialConfig cfg : materials) {
            if (cfg == null || cfg.material == null) continue;

            ItemStack inputStack = gt(inPrefix, cfg.material, inAmount);
            ItemStack outputStack = gt(outPrefix, cfg.material, outAmount);

            // ST.valid checks non-null and valid item stack size
            if (ST.valid(inputStack) && ST.valid(outputStack)) {
                addRecipe(new MetalFormerRecipe(
                        new ItemStack[]{ inputStack },
                        mold,
                        new ItemStack[]{ outputStack },
                        durationTicks,
                        cfg.euPerTick,
                        cfg.minTier
                ));
            }
        }
    }

    public MachineRecipe findRecipe(ItemStack inputItem, ItemStack moldItem) {
        if (inputItem == null || moldItem == null) return null;

        for (MachineRecipe recipe : recipeList) {
            if (matches(recipe, inputItem, moldItem)) {
                return recipe;
            }
        }
        return null;
    }

    public boolean matches(MachineRecipe recipe, ItemStack inputItem, ItemStack moldItem) {
        if (recipe == null || inputItem == null || moldItem == null) return false;

        if (recipe.inputs != null && recipe.inputs.length > 0 && recipe.inputs[0] != null) {
            ItemStack reqInput = recipe.inputs[0];
            if (!areItemStacksEqual(reqInput, inputItem) || inputItem.stackSize < reqInput.stackSize) {
                return false;
            }
        } else {
            return false;
        }

        if (recipe instanceof MetalFormerRecipe) {
            ItemStack reqMold = ((MetalFormerRecipe) recipe).getMold();
            return reqMold != null && areItemStacksEqual(reqMold, moldItem);
        }

        return true;
    }

    public static class MetalFormerRecipe extends MachineRecipe {
        private final ItemStack mold;

        public MetalFormerRecipe(ItemStack[] inputs, ItemStack mold, ItemStack[] outputs, int duration, long euPerTick, int minTier) {
            super(inputs, outputs, null, null, duration, euPerTick, minTier);
            this.mold = mold;
        }

        public ItemStack getMold() {
            return mold;
        }
    }

    public boolean canOutput(ItemStack[] inventory, int outputSlot, MachineRecipe recipe) {
        if (recipe.outputs == null || recipe.outputs.length == 0 || recipe.outputs[0] == null) return true;

        ItemStack recipeOut = recipe.outputs[0];
        ItemStack currentOut = inventory[outputSlot];

        if (currentOut == null) return true;
        if (!currentOut.isItemEqual(recipeOut) || !ItemStack.areItemStackTagsEqual(currentOut, recipeOut)) {
            return false;
        }

        return currentOut.stackSize + recipeOut.stackSize <= 4096;
    }
}
