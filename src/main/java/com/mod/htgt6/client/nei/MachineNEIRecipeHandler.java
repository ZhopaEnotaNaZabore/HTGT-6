package com.mod.htgt6.client.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import java.util.List;

/** NEI's widget hit box must cover everything the handler draws, not its default 65px. */
public abstract class MachineNEIRecipeHandler extends TemplateRecipeHandler {
    protected int panelHeight(int recipeIndex) { return 138; }

    @Override public int getRecipeHeight(int recipeIndex) {
        int height = panelHeight(recipeIndex);
        if (recipeIndex < 0 || recipeIndex >= arecipes.size()) return height;
        CachedRecipe recipe = arecipes.get(recipeIndex);
        height = bottom(height, recipe.getIngredients());
        height = bottom(height, recipe.getOtherStacks());
        PositionedStack result = recipe.getResult();
        return result == null ? height : Math.max(height, result.rely + 18);
    }

    private static int bottom(int height, List<PositionedStack> stacks) {
        if (stacks != null) for (PositionedStack stack : stacks)
            if (stack != null) height = Math.max(height, stack.rely + 18);
        return height;
    }
}
