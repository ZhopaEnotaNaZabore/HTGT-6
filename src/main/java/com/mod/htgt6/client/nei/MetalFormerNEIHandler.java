package com.mod.htgt6.client.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.common.handler.recipe.RM.MetalFormerRecipeMap;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class MetalFormerNEIHandler extends MachineNEIRecipeHandler {

    public class CachedMetalFormerRecipe extends CachedRecipe {
        public PositionedStack input;
        public PositionedStack mold;
        public PositionedStack output;
        public MachineRecipe recipe;

        public CachedMetalFormerRecipe(MachineRecipe recipe) {
            this.recipe = recipe;

            // 1. Deep Input Slot at (16, 25) - Aligned with ContainerMetalFormer
            if (recipe.inputs != null && recipe.inputs.length > 0 && recipe.inputs[0] != null) {
                input = new PositionedStack(recipe.inputs[0], 16, 25);
            }

            // 2. Special Mold Slot at (54, 63) - Aligned with ContainerMetalFormer
            if (recipe instanceof MetalFormerRecipeMap.MetalFormerRecipe) {
                ItemStack moldStack = ((MetalFormerRecipeMap.MetalFormerRecipe) recipe).getMold();
                if (moldStack != null) {
                    mold = new PositionedStack(moldStack, 54, 63);
                }
            }

            // 3. Deep Output Slot at (107, 25) - Aligned with ContainerMetalFormer
            if (recipe.outputs != null && recipe.outputs.length > 0 && recipe.outputs[0] != null) {
                output = new PositionedStack(recipe.outputs[0], 107, 25);
            }
        }

        @Override
        public PositionedStack getIngredient() {
            return input;
        }

        @Override
        public PositionedStack getResult() {
            return output;
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            List<PositionedStack> stacks = new ArrayList<PositionedStack>();
            if (mold != null) {
                stacks.add(mold);
            }
            return stacks;
        }
    }

    @Override
    public String getRecipeName() {
        return "Metal Former";
    }

    @Override
    public String getGuiTexture() {
        return "htgt6:textures/gui/metal_former.png";
    }

    @Override
    public String getOverlayIdentifier() {
        return "metal_former";
    }

    @Override
    public void loadTransferRects() {
        // Arrow rectangle matching the standard Gui progress bar (X: 61, Y: 24, W: 24, H: 16)
        this.transferRects.add(new RecipeTransferRect(new Rectangle(61, 24, 24, 16), getOverlayIdentifier()));
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(getOverlayIdentifier())) {
            for (MachineRecipe recipe : MetalFormerRecipeMap.INSTANCE.getRecipeList()) {
                arecipes.add(new CachedMetalFormerRecipe(recipe));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (MachineRecipe recipe : MetalFormerRecipeMap.INSTANCE.getRecipeList()) {
            if (recipe != null && recipe.outputs != null) {
                for (ItemStack out : recipe.outputs) {
                    if (out != null && NEIServerUtils.areStacksSameTypeCrafting(out, result)) {
                        arecipes.add(new CachedMetalFormerRecipe(recipe));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (MachineRecipe recipe : MetalFormerRecipeMap.INSTANCE.getRecipeList()) {
            if (recipe == null) continue;

            // Check if ingredient matches main input
            boolean matches = false;
            if (recipe.inputs != null) {
                for (ItemStack in : recipe.inputs) {
                    if (in != null && NEIServerUtils.areStacksSameTypeCrafting(ingredient, in)) {
                        matches = true;
                        break;
                    }
                }
            }

            // Check if ingredient matches required Mold catalyst
            if (!matches && recipe instanceof MetalFormerRecipeMap.MetalFormerRecipe) {
                ItemStack moldStack = ((MetalFormerRecipeMap.MetalFormerRecipe) recipe).getMold();
                if (moldStack != null && NEIServerUtils.areStacksSameTypeCrafting(ingredient, moldStack)) {
                    matches = true;
                }
            }

            if (matches) {
                arecipes.add(new CachedMetalFormerRecipe(recipe));
            }
        }
    }

    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiDraw.changeTexture(getGuiTexture());
        GuiDraw.drawTexturedModalRect(0, 0, 0, 0, 166, 84);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void drawExtras(int recipeIndex) {
        CachedMetalFormerRecipe cached = (CachedMetalFormerRecipe) arecipes.get(recipeIndex);
        MachineRecipe r = cached.recipe;

        int yOffset = 88;
        long totalEU = (long) r.euPerTick * (long) r.duration;

        GuiDraw.drawString("Min Tier: " + getTierName(r.minTier), 8, yOffset, 0xFFFFFF, false);
        GuiDraw.drawString("EU/t: " + r.euPerTick, 8, yOffset + 10, 0x00FF00, false);
        String formattedEU = String.format("Total EU: %,d EU", totalEU).replace('\u00A0', ' ');
        GuiDraw.drawString(formattedEU, 8, yOffset + 20, 0xFFFF00, false);
        GuiDraw.drawString(String.format("Time: %.1f s", (double) r.duration / 20.0), 8, yOffset + 30, 0x00FFFF, false);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }



    public static final String[] TIER_NAMES = {
            "LV", "MV", "HV", "EV",
            "IV", "LuV", "ZPM", "UV", "PUV1",
            "UX", "OLV", "OMV", "OHV", "OEV",
            "OIV", "MAX"
    };

    public static String getTierName(int minTier) {
        if (minTier >= 1 && minTier <= TIER_NAMES.length) {
            return TIER_NAMES[minTier - 1];
        }
        return "LV"; // Default or fallback
    }
}
