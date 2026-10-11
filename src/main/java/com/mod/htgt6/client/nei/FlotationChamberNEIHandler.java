package com.mod.htgt6.client.nei;
import java.util.List;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.FlotationChamberRecipeHandler;
public final class FlotationChamberNEIHandler extends ChemicalPlantNEIHandler {
    public static final String OVERLAY_ID="htgt6_flotation_chamber";
    @Override protected List<ChemicalPlantRecipe> getRecipes(){return FlotationChamberRecipeHandler.INSTANCE.getRecipeList();}
    @Override protected String getDisplayName(){return net.minecraft.util.StatCollector.translateToLocal("tile.flotation_chamber.name");}
    @Override protected String getMachineHeader(){return "FLOTATION CHAMBER";}
    @Override protected String getMachineOverlayId(){return OVERLAY_ID;}
}
