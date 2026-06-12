package com.mod.htgt6.common.handler.recipe.compressor;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.recipe.DefaultOverlayHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.mod.htgt6.client.GUI.GuiSuperMasicCompressor;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class CSrecipeHandler extends TemplateRecipeHandler {

    public static void registerSMCNEI() {
        CSrecipeHandler handler = new CSrecipeHandler();

        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);

        API.registerGuiOverlay(GuiSuperMasicCompressor.class,
                "compressing_machine"
        );

        API.registerGuiOverlayHandler(
                GuiSuperMasicCompressor.class,
                new DefaultOverlayHandler(),
                "compressing_machine"
        );
    }

    @Override
    public String getRecipeName() {
        return "Super Masic Compressor";
    }

    @Override
    public String getGuiTexture() {
        return "htgt6:textures/gui/Compressor.png";
    }

    @Override
    public void loadTransferRects() {
        transferRects.add(
                new RecipeTransferRect(
                        new Rectangle(82, 20, 24, 16),
                        "compressing_machine"
                )
        );
    }

    public class CachedCompressorRecipe extends CachedRecipe {

        private final List<PositionedStack> inputs =
                new ArrayList<PositionedStack>();

        private final PositionedStack outputStack;

        public final CompressorRecipeHandler.CompressorRecipe rawRecipe;

        public CachedCompressorRecipe(
                CompressorRecipeHandler.CompressorRecipe recipe
        ) {

            rawRecipe = recipe;

            for (int i = 0; i < recipe.input.length; i++) {

                if (recipe.input[i] != null) {

                    inputs.add(
                            new PositionedStack(
                                    recipe.input[i],
                                    19 + (i * 18),
                                    23
                            )
                    );
                }
            }

            outputStack =
                    new PositionedStack(
                            recipe.output,
                            108,
                            23
                    );
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return inputs;
        }

        @Override
        public PositionedStack getResult() {
            return outputStack;
        }
    }

    @Override
    public void loadCraftingRecipes(
            String outputId,
            Object... results
    ) {

        if ("compressing_machine".equals(outputId)) {

            for (CompressorRecipeHandler.CompressorRecipe recipe
                    : CompressorRecipeHandler.recipes) {

                arecipes.add(
                        new CachedCompressorRecipe(recipe)
                );
            }

            return;
        }

        super.loadCraftingRecipes(outputId, results);
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {

        for (CompressorRecipeHandler.CompressorRecipe recipe
                : CompressorRecipeHandler.recipes) {

            if (NEIServerUtils.areStacksSameTypeCrafting(
                    recipe.output,
                    result
            )) {

                arecipes.add(
                        new CachedCompressorRecipe(recipe)
                );
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {

        for (CompressorRecipeHandler.CompressorRecipe recipe
                : CompressorRecipeHandler.recipes) {

            for (ItemStack inputStack : recipe.input) {

                if (inputStack != null
                        && NEIServerUtils.areStacksSameTypeCrafting(
                        inputStack,
                        ingredient
                )) {

                    arecipes.add(
                            new CachedCompressorRecipe(recipe)
                    );

                    break;
                }
            }
        }
    }
    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1F, 1F, 1F, 1F);

        GuiDraw.changeTexture(getGuiTexture());

        // Draws the localized machine region window inside the NEI view interface
        GuiDraw.drawTexturedModalRect(
                0,
                0,
                0,
                0,
                166,
                82
        );
        drawProgressBar(
                75,    // Fixed X: Shifted from 82 to 70 to match background texture
                24,    // Fixed Y: Shifted from 20 to 23 to match GUI layout
                176,   // Texture U
                0,     // Texture V
                24,    // Width
                16,    // Height
                48,    // Speed
                0      // Direction (Right)
        );

        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void drawExtras(int recipeIndex) {

        CachedCompressorRecipe cached =
                (CachedCompressorRecipe) arecipes.get(recipeIndex);

        CompressorRecipeHandler.CompressorRecipe r =
                cached.rawRecipe;

        // Tier name mappings matching your Assembler layout
        String[] tierNames = {
                "ULV", "LV", "MV", "HV", "EV",
                "IV", "LuV", "ZPM", "UV", "PUV1",
                "UX", "OLV", "OMV", "OHV", "OEV",
                "OIV", "MAX"
        };

        String tierText =
                (r.tier >= 0 && r.tier < tierNames.length)
                        ? "Tier " + r.tier + " (" + tierNames[r.tier] + ")"
                        : "Tier " + r.tier;

        // Render Tier Requirement
        GuiDraw.drawString(
                tierText,
                8,
                78,
                0xFFFFFF,
                false
        );

        // Render EU per tick cost
        GuiDraw.drawString(
                "EU/t: " + r.euPerTick,
                8,
                90,
                0x00FF00,
                false
        );

        // Render Formatted Operation Time
        GuiDraw.drawString(
                String.format("Time: %.1f s", r.duration / 20.0D),
                8,
                102,
                0x00FFFF,
                false
        );

        // Render Total Cumulative Energy Spent
        GuiDraw.drawString(
                "Total: " + (r.duration * r.euPerTick) + " EU",
                8,
                114,
                0xFFFF00,
                false
        );
    }
}