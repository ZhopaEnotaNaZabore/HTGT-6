package com.mod.htgt6.client.nei;

import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.IndustrialMixerRecipeHandler;

import java.util.List;

/** Item-focused recipe page for the independent Industrial Mixer map. */
public final class IndustrialMixerNEIHandler extends ChemicalPlantNEIHandler {
    public static final String OVERLAY_ID = "htgt6_industrial_mixer";

    @Override protected List<ChemicalPlantRecipe> getRecipes() {
        return IndustrialMixerRecipeHandler.INSTANCE.getRecipeList();
    }
    @Override protected String getDisplayName() { return "Industrial Mixer"; }
    @Override protected String getMachineHeader() { return "INDUSTRIAL MIXER"; }
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
