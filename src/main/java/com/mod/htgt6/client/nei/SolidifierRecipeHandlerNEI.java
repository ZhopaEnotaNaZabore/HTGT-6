package com.mod.htgt6.client.nei;

import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.SolidifierRecipeHandler;

import java.util.List;

/** Fluid-aware multiblock recipe page using virtual Selector Tags instead of physical molds. */
public final class SolidifierRecipeHandlerNEI extends ChemicalPlantNEIHandler {
    @Override @SuppressWarnings({ "rawtypes", "unchecked" })
    protected List<ChemicalPlantRecipe> getRecipes() {
        return (List) SolidifierRecipeHandler.recipes;
    }
    @Override protected String getDisplayName() { return "Fluid Solidifier"; }
    @Override protected String getMachineHeader() { return "FLUID SOLIDIFIER"; }
    @Override protected String getMachineOverlayId() { return "fluid_solidifier"; }
    @Override protected int itemInputX(int index) { return 8 + index * 18; }
    @Override protected int fluidInputX(int index) { return 38; }
    @Override protected int fluidInputY(int index) { return 34; }
    @Override protected int itemOutputX(int index) { return 126 + index * 18; }
    @Override protected int itemOutputY(int index) { return 34; }
    @Override protected int selectorX() { return 69; }
    @Override protected int selectorY() { return 34; }
    @Override protected int arrowX() { return 96; }
    @Override protected int arrowY() { return 43; }
}
