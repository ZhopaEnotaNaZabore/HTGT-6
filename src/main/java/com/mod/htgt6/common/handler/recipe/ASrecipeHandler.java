package com.mod.htgt6.common.handler.recipe;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ASrecipeHandler extends TemplateRecipeHandler {

    public class CachedMyRecipe extends CachedRecipe {
        public List<PositionedStack> inputs = new ArrayList<PositionedStack>();
        public PositionedStack output;

        // This field stores the specific recipe data for this specific instance[cite: 1]
        public AssemblerRecipe recipeReference;

        public CachedMyRecipe(AssemblerRecipe recipe) {
            // Store the recipe object so we can access it during drawExtras[cite: 1]
            this.recipeReference = recipe;

            // 3x3 Input Grid logic
            for (int i = 0; i < 9; i++) {
                if (recipe.input[i] != null) {
                    // X: Starts at 10; Y: Starts at 6
                    int x = 10 + (i % 3) * 17;
                    int y = 6 + (i / 3) * 23;
                    inputs.add(new PositionedStack(recipe.input[i], x, y));
                }
            }
            // Output Slot
            this.output = new PositionedStack(recipe.output, 102, 20);

        }

        @Override
        public List<PositionedStack> getIngredients() {
            return getCycledIngredients(cycleticks / 20, inputs);
        }

        @Override
        public PositionedStack getResult() {
            return output;
        }
    }

    @Override
    public String getRecipeName() {
        return "AssemblingMachine";
    }

    @Override
    public String getGuiTexture() {
        return "htgt6:textures/gui/Assembler.png";
    }


    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (AssemblerRecipe recipe : AssemblerRecipes.getInstance().getRecipes()) {
            if (NEIServerUtils.areStacksSameTypeCrafting(recipe.output, result)) {
                arecipes.add(new CachedMyRecipe(recipe));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (AssemblerRecipe recipe : AssemblerRecipes.getInstance().getRecipes()) {
            for (ItemStack inputStack : recipe.input) {
                if (inputStack != null && NEIServerUtils.areStacksSameTypeCrafting(inputStack, ingredient)) {
                    arecipes.add(new CachedMyRecipe(recipe));
                    break;
                }
            }
        }
    }


    @Override
    public void drawExtras(int recipe) {
        // Progress Bar (Yellow Arrow)
        // 74, 23: Screen coordinates matching the grey arrow
        // 176, 0: Texture coordinates (where your yellow arrow lives on the sheet)
        drawProgressBar(73, 20, 176, 0, 24, 17, 48, 0);

       // AssemblerRecipe r = AssemblerRecipes.getInstance().getRecipes().get(recipe);
        CachedMyRecipe cached = (CachedMyRecipe) arecipes.get(recipe);
        AssemblerRecipe r = cached.recipeReference;


        String tierName;
        if (r.euPerTick <= 8) {
            tierName = "(ULV)";
        } else if (r.euPerTick < 32) {
            tierName = "(LV)";
        } else {
            tierName = "(MV+)";
        }

        //

        GuiDraw.drawString(r.euPerTick + " EU/t", 10, 100, 0x404040, false);
        GuiDraw.drawString("Time: " + r.duration + "t", 70, 100, 0x404040, false);
        GuiDraw.drawString("Tier: " + r.tier + " " + tierName, 10, 120, 0x404040, false);

        int totalEU = r.euPerTick * r.duration;
        GuiDraw.drawString("Total: " + totalEU + " EU", 70, 120, 0x404040, false);
    }
    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1, 1, 1, 1);
        GuiDraw.changeTexture(getGuiTexture());
        // (x, y) on screen, (u, v) on texture, width, height
        // This crops out the inventory part and the outer borders
        GuiDraw.drawTexturedModalRect(0, 0, 5, 5, 166, 78);
    }
    @Override
    public String getOverlayIdentifier() {
        return "assembling_machine";
    }
}