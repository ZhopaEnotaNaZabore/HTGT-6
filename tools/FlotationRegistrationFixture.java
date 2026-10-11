package com.mod.htgt6.common.flotation;

import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import com.mod.htgt6.common.handler.FluidDatabase;
import com.mod.htgt6.common.handler.recipe.RM.FlotationChamberRecipeHandler;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.code.ItemStackContainer;
import gregapi.data.CS;
import gregapi.data.MT;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

/** Regression for the loadComplete failure which prevented every world login. */
public final class FlotationRegistrationFixture {
    private static final class Ore extends Block implements gregapi.block.IBlockPlacable {
        Ore() { super(Material.rock); }
        public boolean placeBlock(World world, int x, int y, int z, byte side, short meta,
                NBTTagCompound nbt, boolean cause, boolean updates) { return false; }
    }
    public static void main(String[] args) throws Exception {
        CrusherOreImportFixture.initializeGT6();
        FlotationChamberRecipeHandler handler = FlotationChamberRecipeHandler.INSTANCE;
        // Missing integration content must not mark the entire mod ERRORED.
        handler.registerDefaults();
        require(handler.getRecipeList().isEmpty(), "missing fluids safely leave no recipes");
        Fluid acid = new Fluid("flotation_registration_acid"); FluidRegistry.registerFluid(acid);
        MT.H2SO4.liquid(new FluidStack(acid, 1000));
        Fluid solution = new Fluid("htgt6.chalcopyrite_solution"); FluidRegistry.registerFluid(solution);
        FluidDatabase.CHALCOPYRITE_SOLUTION = solution;
        CS.BlocksGT.stoneToNormalOres.clear();
        CS.BlocksGT.ore = null;
        handler.registerDefaults();
        require(handler.getRecipeList().isEmpty(), "missing ores do not abort loading");

        Ore stone = new Ore(), basalt = new Ore(), small = new Ore();
        GameRegistry.registerBlock(stone, "flotation_stone");
        GameRegistry.registerBlock(basalt, "flotation_basalt");
        GameRegistry.registerBlock(small, "flotation_small");
        CS.BlocksGT.stoneToNormalOres.put(new ItemStackContainer(Blocks.stone, 1, 0), stone);
        CS.BlocksGT.stoneToNormalOres.put(new ItemStackContainer(Blocks.cobblestone, 1, 0), basalt);
        CS.BlocksGT.stoneToNormalOres.put(new ItemStackContainer(Blocks.gravel, 1, 0), stone);
        CS.BlocksGT.stoneToSmallOres.put(new ItemStackContainer(Blocks.stone, 1, 0), small);
        require(OreDictionary.getOres("oreChalcopyrite").isEmpty(), "reproduce missing generic ore entry");
        handler.registerDefaults();
        require(handler.getRecipeList().isEmpty(), "raw host ore blocks cannot replace crushed ore");
        Item crushed = new Item().setHasSubtypes(true), otherCrushed = new Item().setHasSubtypes(true);
        Item purified = new Item().setHasSubtypes(true);
        GameRegistry.registerItem(crushed, "flotation_crushed");
        GameRegistry.registerItem(otherCrushed, "flotation_other_crushed");
        GameRegistry.registerItem(purified, "flotation_purified");
        OreDictionary.registerOre("crushedChalcopyrite", new ItemStack(crushed, 1, MT.OREMATS.Chalcopyrite.mID));
        OreDictionary.registerOre("crushedChalcopyrite", new ItemStack(otherCrushed, 1, MT.OREMATS.Chalcopyrite.mID));
        OreDictionary.registerOre("crushedPurifiedChalcopyrite", new ItemStack(purified, 1, MT.OREMATS.Chalcopyrite.mID));
        handler.registerDefaults();
        require(handler.getRecipeList().size() == 2, "both crushed variants registered; raw and purified excluded");
        for (ChemicalPlantRecipe recipe : handler.getRecipeList()) {
            require(recipe.inputs[0].getItem() == crushed || recipe.inputs[0].getItem() == otherCrushed, "only crushed input accepted");
            require(recipe.inputs[0].getItemDamage() == MT.OREMATS.Chalcopyrite.mID, "correct material metadata");
            require(recipe.inputs[0].stackSize == 32 && recipe.fluidInputs[0].amount == 10000
                    && recipe.fluidOutputs[0].amount == 10000, "original recipe quantities preserved");
        }
        handler.registerDefaults();
        require(handler.getRecipeList().size() == 2, "registration is idempotent");
        System.out.println("FlotationRegistrationFixture: missing fluids/ores, crushed variants, raw/purified exclusion, metadata, quantities and idempotence passed");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
