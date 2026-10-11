package com.mod.htgt6.client.nei;

import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.ElectronicFactoryRecipeHandler;

import java.util.List;

/** Electronics Factory recipe page with the same item/fluid/Selector layout as the Chemical Plant. */
public final class ElectronicFactoryNEIHandler extends ChemicalPlantNEIHandler {
    public static final String OVERLAY_ID = "htgt6_electronic_factory";

    @Override protected List<ChemicalPlantRecipe> getRecipes() {
        return ElectronicFactoryRecipeHandler.INSTANCE.getRecipeList();
    }
    @Override protected String getDisplayName() { return "Electronics Factory"; }
    @Override protected String getMachineHeader() { return "ELECTRONICS FACTORY"; }
    @Override protected String getMachineOverlayId() { return OVERLAY_ID; }
    @Override protected int itemInputX(int index) { return 6 + index % 4 * 18; }
    @Override protected int itemInputY(int index) { return 25 + index / 4 * 18; }
    @Override protected int fluidInputX(int index) { return 80 + index % 2 * 18; }
    @Override protected int fluidInputY(int index) { return 25 + index / 2 * 18; }
    @Override protected int itemOutputX(int index) { return 128 + index % 2 * 18; }
    @Override protected int itemOutputY(int index) { return 25 + index / 2 * 24; }
    @Override protected int selectorX() { return 110; }
    @Override protected int selectorY() { return 61; }
    @Override protected int arrowX() { return 111; }
}
