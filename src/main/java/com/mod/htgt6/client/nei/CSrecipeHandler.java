package com.mod.htgt6.client.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.mod.htgt6.common.handler.recipe.RM.CompressorRecipeHandler;
import com.mod.htgt6.common.handler.recipe.RM.CompressorRecipeHandler.CompressionRecipe;
import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.common.ui.MachineUiTheme;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class CSrecipeHandler extends MachineNEIRecipeHandler {


    @Override
    public String getRecipeName() {
        return "Super Masic Compressor";
    }

    @Override
    public String getGuiTexture() {
        // Required by TemplateRecipeHandler; the compressor panel is vector-rendered.
        return "minecraft:textures/gui/container/inventory.png";
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
        private final PositionedStack previewStack;

        public final MachineRecipe rawRecipe;

        public CachedCompressorRecipe(
                MachineRecipe recipe
        ) {

            rawRecipe = recipe;

            for (int i = 0; i < recipe.inputs.length; i++) {

                if (recipe.inputs[i] != null) {

                    inputs.add(
                            new PositionedStack(
                                    recipe.inputs[i],
                                    10 + (i * 18),
                                    48
                            )
                    );
                }
            }

            outputStack =
                    new PositionedStack(
                            recipe.outputs[0],
                            105,
                            48
                    );
            previewStack = new PositionedStack(recipe.outputs[0], 10, 8);
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return inputs;
        }

        @Override
        public PositionedStack getResult() {
            return outputStack;
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            List<PositionedStack> stacks = new ArrayList<PositionedStack>();
            stacks.add(previewStack);
            return stacks;
        }
    }

    @Override
    public void loadCraftingRecipes(
            String outputId,
            Object... results
    ) {

        if ("compressing_machine".equals(outputId)) {

            for (MachineRecipe recipe : CompressorRecipeHandler.INSTANCE.getRecipeList()) {

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

        for (MachineRecipe recipe : CompressorRecipeHandler.INSTANCE.getRecipeList()) {

            if (NEIServerUtils.areStacksSameTypeCrafting(
                    recipe.outputs[0],
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

        for (MachineRecipe recipe : CompressorRecipeHandler.INSTANCE.getRecipeList()) {

            for (ItemStack inputStack : recipe.inputs) {

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
        MachineUiTheme theme = MachineUiTheme.getActive();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        drawRect(0, 0, 166, 96, theme.getPanelColor());
        drawFrame(0, 0, 166, 96, theme.getFrameColor());
        drawRect(5, 4, 156, 24, theme.getTerminalColor());
        drawRect(5, 34, 156, 56, theme.getTerminalColor());
        drawVectorSlot(9, 7, theme);
        for (int i = 0; i < 3; i++) drawVectorSlot(9 + i * 18, 47, theme);
        drawVectorSlot(104, 47, theme);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    private static void drawVectorSlot(int x, int y, MachineUiTheme theme) {
        drawRect(x, y, 18, 18, theme.getFrameColor());
        drawRect(x + 1, y + 1, 16, 16, theme.getPanelColor());
    }

    private static void drawFrame(int x, int y, int width, int height, int color) {
        drawRect(x, y, width, 1, color);
        drawRect(x, y + height - 1, width, 1, color);
        drawRect(x, y, 1, height, color);
        drawRect(x + width - 1, y, 1, height, color);
    }

    private static void drawRect(int x, int y, int width, int height, int color) {
        float a = ((color >>> 24) & 255) / 255.0F;
        float r = ((color >>> 16) & 255) / 255.0F;
        float g = ((color >>> 8) & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + height);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
    }

    @Override
    public void drawExtras(int recipeIndex) {

        CachedCompressorRecipe cached =
                (CachedCompressorRecipe) arecipes.get(recipeIndex);

        MachineRecipe r = cached.rawRecipe;
        NEIOutputChance.draw(r.outputChances, 0, cached.outputStack.relx, cached.outputStack.rely);
        MachineUiTheme theme = MachineUiTheme.getActive();

        GuiDraw.drawString("OUTPUT PREVIEW", 33, 8, theme.getPrimaryColor() & 0xFFFFFF, false);
        GuiDraw.drawString(String.format("TIME: %.1f s", r.duration / 20.0D),
                33, 18, theme.getProductionColor() & 0xFFFFFF, false);
        GuiDraw.drawString("INPUT", 9, 36, theme.getLabelColor() & 0xFFFFFF, false);
        GuiDraw.drawString(">>", 75, 52, theme.getProductionColor() & 0xFFFFFF, false);
        GuiDraw.drawString("OUTPUT", 101, 36, theme.getLabelColor() & 0xFFFFFF, false);
        if (r instanceof CompressionRecipe) {
            CompressionRecipe typed = (CompressionRecipe) r;
            String mode = typed.compressionType == CompressorRecipeHandler.CompressionType.IMPLOSION
                    ? "IMPLOSION" : "COMPRESSOR";
            GuiDraw.drawString(mode + " / " + typed.source, 9, 76,
                    theme.getPrimaryColor() & 0xFFFFFF, false);
        }

        String[] tierNames = {
                "ULV", "LV", "MV", "HV", "EV",
                "IV", "LuV", "ZPM", "UV", "PUV1",
                "UX", "OLV", "OMV", "OHV", "OEV",
                "OIV", "MAX"
        };

        String tierText =
                (r.minTier >= 0 && r.minTier < tierNames.length)
                        ? "Tier " + r.minTier + " (" + tierNames[r.minTier] + ")"
                        : "Tier " + r.minTier;

        GuiDraw.drawString(
                tierText,
                8,
                100,
                theme.getTitleColor() & 0xFFFFFF,
                false
        );

        GuiDraw.drawString(
                "EU/t: " + r.euPerTick,
                8,
                112,
                theme.getEnergyColor() & 0xFFFFFF,
                false
        );

        GuiDraw.drawString(
                "Total: " + (r.duration * r.euPerTick) + " EU",
                8,
                124,
                theme.getProductionColor() & 0xFFFFFF,
                false
        );
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }
}
