package com.mod.htgt6.client.nei;

import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.IndustrialCentrifugeRecipeHandler;

import java.util.List;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

/** Item-focused recipe page for the independent Industrial Centrifuge map. */
public final class IndustrialCentrifugeNEIHandler extends ChemicalPlantNEIHandler {
    public static final String OVERLAY_ID = "htgt6_industrial_centrifuge";

    @Override protected List<ChemicalPlantRecipe> getRecipes() {
        return IndustrialCentrifugeRecipeHandler.INSTANCE.getRecipeList();
    }
    @Override protected String getDisplayName() { return StatCollector.translateToLocal("tile.industrial_centrifuge.name"); }
    @Override protected String getMachineHeader() { return "CENTRIFUGE"; }
    @Override protected String getMachineOverlayId() { return OVERLAY_ID; }
    @Override protected int itemInputX(int index) { return 6 + index % 4 * 18; }
    @Override protected int itemInputY(int index) { return 25 + index / 4 * 18; }
    @Override protected int fluidInputX(int index) { return 6 + index * 18; }
    @Override protected int fluidInputY(int index) { return 101; }
    @Override protected int itemOutputX(int index) { return 94 + index % 4 * 18; }
    @Override protected int itemOutputY(int index) { return 25 + index / 4 * 24; }
    @Override protected int fluidOutputX(int index) { return 94 + index * 18; }
    @Override protected int fluidOutputY(int index) { return 101; }
    @Override protected int selectorX() { return 77; }
    @Override protected int selectorY() { return 25; }
    @Override protected int arrowX() { return 78; }
    @Override protected int arrowY() { return 59; }
    @Override protected int panelHeight(int index) { return 156; }
    @Override public int recipiesPerPage() { return 1; }

    @Override public void drawBackground(int index) {
        GuiDraw.drawRect(0, 0, 166, 152, 0xFF252C31);
        GuiDraw.drawRect(3, 20, 160, 102, 0xFF0C1418);
        CachedChemicalRecipe cached = (CachedChemicalRecipe) arecipes.get(index);
        for (PositionedStack stack : cached.ingredients) slot(stack);
        for (PositionedStack stack : cached.extras) slot(stack);
        if (cached.result != null) slot(cached.result);
        GL11.glColor4f(1, 1, 1, 1);
    }
    private static void slot(PositionedStack stack) {
        GuiDraw.drawRect(stack.relx - 1, stack.rely - 1, 18, 18, 0xFF58656B);
        GuiDraw.drawRect(stack.relx, stack.rely, 16, 16, 0xFF242C31);
    }
    @Override public void drawExtras(int index) {
        ChemicalPlantRecipe recipe = ((CachedChemicalRecipe) arecipes.get(index)).recipe;
        drawOutputChances(recipe);
        GuiDraw.drawString("CENTRIFUGE", 6, 7, 0xE3E8EA, false);
        GuiDraw.drawString(">", arrowX(), arrowY(), 0xFFCD73, false);
        GuiDraw.drawString(recipe.source.startsWith("GT6") ? "GT6 Centrifuge" : "HTGT6", 6, 126, 0x80DCEC, false);
        GuiDraw.drawString(String.format("%.1f s  %d EU/t  %s", recipe.duration / 20D,
                recipe.euPerTick, tierName(recipe.minTier)), 6, 138, 0xFFCD73, false);
        for (int i = 0; i < recipe.fluidInputs.length; i++) amount(recipe.fluidInputs[i], fluidInputX(i));
        for (int i = 0; i < recipe.fluidOutputs.length; i++) amount(recipe.fluidOutputs[i], fluidOutputX(i));
    }
    private static void amount(FluidStack stack, int x) {
        String text = stack.amount < 1000 ? Integer.toString(stack.amount)
                : String.format(java.util.Locale.ROOT, "%.1fk", stack.amount / 1000D);
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, 200);
        GuiDraw.drawString(text, x, 110, 0x80EFFF, true);
        GL11.glPopMatrix();
    }
}
