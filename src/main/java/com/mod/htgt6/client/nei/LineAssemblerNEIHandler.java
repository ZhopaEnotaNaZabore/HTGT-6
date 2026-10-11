package com.mod.htgt6.client.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.common.handler.recipe.RM.LineAssemblerRecipeHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import gregapi.data.FL;
import org.lwjgl.opengl.GL11;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class LineAssemblerNEIHandler extends MachineNEIRecipeHandler {

    public class CachedLineAssemblerRecipe extends CachedRecipe {
        public List<PositionedStack> inputs = new ArrayList<PositionedStack>();
        public List<PositionedStack> fluids = new ArrayList<PositionedStack>();
        public PositionedStack output;
        public MachineRecipe recipe;

        public CachedLineAssemblerRecipe(MachineRecipe recipe) {
            this.recipe = recipe;

            // 1. Inputs (4x4 Grid matching ContainerLineAssembler: 8 + col * 18, 18 + row * 18)
            if (recipe.inputs != null) {
                for (int i = 0; i < Math.min(recipe.inputs.length, 16); i++) {
                    ItemStack stack = recipe.inputs[i];
                    if (stack != null) {
                        int col = i % 4;
                        int row = i / 4;
                        inputs.add(new PositionedStack(stack, 8 + col * 18, 18 + row * 18));
                    }
                }
            }

            // 2. Output (Matching ContainerLineAssembler: 142, 70)
            if (recipe.outputs != null && recipe.outputs.length > 0 && recipe.outputs[0] != null) {
                output = new PositionedStack(recipe.outputs[0], 142, 70);
            }

            // Match the Line Assembler's four virtual GT fluid-display slots.
            if (recipe.fluidInputs != null) {
                for (int i = 0; i < Math.min(recipe.fluidInputs.length, 4); i++) {
                    FluidStack fluid = recipe.fluidInputs[i];
                    if (fluid != null) {
                        fluids.add(new PositionedStack(FL.display(fluid, false, false), 88, 18 + i * 18));
                    }
                }
            }
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return getCycledIngredients(cycleticks / 20, inputs);
        }

        @Override
        public PositionedStack getResult() {
            return output;
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            return fluids;
        }
    }

    @Override
    public String getRecipeName() {
        return "Line Assembler";
    }

    @Override
    public String getGuiTexture() {
        return "htgt6:textures/gui/LineAssembler.png";
    }

    @Override
    public String getOverlayIdentifier() {
        return "line_assembler";
    }

    @Override
    public void loadTransferRects() {
        // Updated transfer rect position near progress arrow
        this.transferRects.add(new RecipeTransferRect(new Rectangle(110, 40, 24, 17), getOverlayIdentifier()));
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(getOverlayIdentifier())) {
            for (MachineRecipe recipe : LineAssemblerRecipeHandler.INSTANCE.getRecipeList()) {
                arecipes.add(new CachedLineAssemblerRecipe(recipe));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (MachineRecipe recipe : LineAssemblerRecipeHandler.INSTANCE.getRecipeList()) {
            if (recipe != null && recipe.outputs != null && recipe.outputs.length > 0 && recipe.outputs[0] != null) {
                if (NEIServerUtils.areStacksSameTypeCrafting(recipe.outputs[0], result)) {
                    arecipes.add(new CachedLineAssemblerRecipe(recipe));
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (MachineRecipe recipe : LineAssemblerRecipeHandler.INSTANCE.getRecipeList()) {
            if (recipe != null && recipe.inputs != null) {
                for (ItemStack input : recipe.inputs) {
                    if (input != null && NEIServerUtils.areStacksSameTypeCrafting(ingredient, input)) {
                        arecipes.add(new CachedLineAssemblerRecipe(recipe));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiDraw.changeTexture(getGuiTexture());
        // Updated height for larger GUI screen
        GuiDraw.drawTexturedModalRect(0, 0, 0, 0, 166, 92);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void drawExtras(int recipeIndex) {
        CachedLineAssemblerRecipe cached = (CachedLineAssemblerRecipe) arecipes.get(recipeIndex);
        MachineRecipe r = cached.recipe;



        // Tier / EU / Time text shifted down past GUI boundary (Y >= 96)
        int yOffset = 96;

        // Calculate Total EU
        long totalEU = (long) r.euPerTick * (long) r.duration;

        GuiDraw.drawString("Min Tier: " + r.minTier, 8, yOffset, 0xFFFFFF, false);
        GuiDraw.drawString("EU/t: " + r.euPerTick, 8, yOffset + 10, 0x00FF00, false);
        String formattedEU = String.format("Total EU: %,d EU", totalEU).replace('\u00A0', ' ');
        GuiDraw.drawString(formattedEU, 8, yOffset + 20, 0xFFFF00, false);
        GuiDraw.drawString(String.format("Time: %.1f s", (double) r.duration / 20.0), 8, yOffset + 30, 0x00FFFF, false);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
