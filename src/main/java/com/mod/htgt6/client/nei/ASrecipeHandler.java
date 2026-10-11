package com.mod.htgt6.client.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

import com.mod.htgt6.common.handler.recipe.RM.AssemblerRecipeHandler;
import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.common.ui.MachineUiTheme;
import gregapi.data.FL;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ASrecipeHandler extends MachineNEIRecipeHandler {

    // ==========================================
    // TRANSFER RECTS
    // ==========================================

    @Override
    public void loadTransferRects() {
        transferRects.add(
                new RecipeTransferRect(
                        new java.awt.Rectangle(
                                68, // X
                                22, // Y
                                24, // Width
                                17  // Height
                        ),
                        "assembling_machine"
                )
        );
    }

    // ==========================================
    // CACHED RECIPE
    // ==========================================

    public class CachedAssemblerRecipe extends CachedRecipe {

        public List<PositionedStack> inputs = new ArrayList<PositionedStack>();
        public List<PositionedStack> fluids = new ArrayList<PositionedStack>();
        public PositionedStack output;
        public PositionedStack preview;
        public MachineRecipe recipe;

        public CachedAssemblerRecipe(MachineRecipe recipe) {
            this.recipe = recipe;

            // ==========================================
            // INPUTS
            // ==========================================
            for (int i = 0; i < 9; i++) {
                ItemStack stack = i < recipe.inputs.length ? recipe.inputs[i] : null;

                if (stack == null)
                    continue;

                int x = 10 + (i % 3) * 18;
                int y = 38 + (i / 3) * 18;

                inputs.add(new PositionedStack(stack, x, y));
            }

            // ==========================================
            // OUTPUT
            // ==========================================
            output = new PositionedStack(recipe.outputs[0], 104, 56);
            preview = new PositionedStack(recipe.outputs[0], 10, 8);

            for (int i = 0; i < recipe.fluidInputs.length; i++) {
                FluidStack fluid = recipe.fluidInputs[i];
                if (fluid != null) fluids.add(new PositionedStack(FL.display(fluid, false, false), 136 + i * 18, 56));
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
            List<PositionedStack> stacks = new ArrayList<PositionedStack>(fluids);
            stacks.add(preview);
            return stacks;
        }
    }

    // ==========================================
    // RECIPE NAME
    // ==========================================

    @Override
    public String getRecipeName() {
        return "Assembler";
    }

    // ==========================================
    // GUI TEXTURE
    // ==========================================

    @Override
    public String getGuiTexture() {
        // Required by TemplateRecipeHandler; the actual panel is vector-rendered.
        return "minecraft:textures/gui/container/inventory.png";
    }

    // ==========================================
    // LOAD ALL RECIPES
    // ==========================================

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(getOverlayIdentifier())) {
            for (MachineRecipe recipe : AssemblerRecipeHandler.INSTANCE.getRecipeList()) {
                arecipes.add(new CachedAssemblerRecipe(recipe));
            }
        }
        super.loadCraftingRecipes(outputId, results);
    }

    // ==========================================
    // LOAD RESULT RECIPES
    // ==========================================

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (MachineRecipe recipe : AssemblerRecipeHandler.INSTANCE.getRecipeList()) {
            if (recipe == null || recipe.outputs.length == 0 || recipe.outputs[0] == null || recipe.outputs[0].getItem() == null) {
                continue;
            }

            if (NEIServerUtils.areStacksSameTypeCrafting(recipe.outputs[0], result)) {
                arecipes.add(new CachedAssemblerRecipe(recipe));
            }
        }
    }

    // ==========================================
    // LOAD USAGE RECIPES
    // ==========================================

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        FluidStack containedFluid = FluidContainerRegistry.getFluidForFilledItem(ingredient);
        for (MachineRecipe recipe : AssemblerRecipeHandler.INSTANCE.getRecipeList()) {
            if (recipe == null || recipe.inputs == null) continue;

            boolean matched = false;
            for (ItemStack input : recipe.inputs) {
                if (input == null || input.getItem() == null)
                    continue;

                if (NEIServerUtils.areStacksSameTypeCrafting(ingredient, input)) {
                    arecipes.add(new CachedAssemblerRecipe(recipe));
                    matched = true;
                    break;
                }
            }
            if (!matched && containedFluid != null) {
                for (FluidStack required : recipe.fluidInputs) {
                    if (required != null && required.isFluidEqual(containedFluid)) {
                        arecipes.add(new CachedAssemblerRecipe(recipe));
                        break;
                    }
                }
            }
        }
    }

    // ==========================================
    // DRAW BACKGROUND
    // ==========================================

    @Override
    public void drawBackground(int recipe) {
        MachineUiTheme theme = MachineUiTheme.getActive();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        drawRect(0, 0, 166, 96, theme.getPanelColor());
        drawFrame(0, 0, 166, 96, theme.getFrameColor());
        drawRect(5, 4, 156, 24, theme.getTerminalColor());
        drawRect(5, 32, 156, 60, theme.getTerminalColor());

        drawVectorSlot(9, 7, theme);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                drawVectorSlot(9 + col * 18, 37 + row * 18, theme);
            }
        }
        drawVectorSlot(103, 55, theme);

        CachedAssemblerRecipe cached = (CachedAssemblerRecipe) arecipes.get(recipe);
        for (int i = 0; i < cached.fluids.size(); i++) {
            drawVectorSlot(135 + i * 18, 55, theme);
        }

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

    // ==========================================
    // DRAW EXTRAS
    // ==========================================

    @Override
    public void drawExtras(int recipeIndex) {
        CachedAssemblerRecipe cached = (CachedAssemblerRecipe) arecipes.get(recipeIndex);
        MachineRecipe r = cached.recipe;

        NEIOutputChance.draw(r.outputChances, 0, 104, 56);
        MachineUiTheme theme = MachineUiTheme.getActive();
        GuiDraw.drawString("OUTPUT PREVIEW", 33, 8, theme.getPrimaryColor() & 0xFFFFFF, false);
        GuiDraw.drawString(String.format("TIME: %.1f s", r.duration / 20.0D),
                33, 18, theme.getProductionColor() & 0xFFFFFF, false);
        GuiDraw.drawString("INPUT MATRIX", 9, 31, theme.getLabelColor() & 0xFFFFFF, false);
        GuiDraw.drawString(">>", 75, 60, theme.getProductionColor() & 0xFFFFFF, false);
        GuiDraw.drawString("OUTPUT", 101, 43, theme.getLabelColor() & 0xFFFFFF, false);
        if (r.fluidInputs.length > 0) {
            GuiDraw.drawString("FLUID", 132, 43, theme.getLabelColor() & 0xFFFFFF, false);
        }

        String[] tierNames = {
                "ULV", "LV", "MV", "HV", "EV",
                "IV", "LuV", "ZPM", "UV", "PUV1",
                "UX", "OLV", "OMV", "OHV", "OEV",
                "OIV", "MAX"
        };

        String tierText = (r.minTier >= 0 && r.minTier < tierNames.length)
                ? "Tier " + r.minTier + " (" + tierNames[r.minTier] + ")"
                : "Tier " + r.minTier;

        GuiDraw.drawString(tierText, 8, 100, theme.getTitleColor() & 0xFFFFFF, false);
        GuiDraw.drawString("EU/t: " + r.euPerTick, 8, 112, theme.getEnergyColor() & 0xFFFFFF, false);

        GuiDraw.drawString(
                "Total: " + (r.duration * r.euPerTick) + " EU",
                8,
                124,
                theme.getProductionColor() & 0xFFFFFF,
                false
        );

        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    // ==========================================
    // OVERLAY ID
    // ==========================================

    @Override
    public String getOverlayIdentifier() {
        return "assembling_machine";
    }
}
