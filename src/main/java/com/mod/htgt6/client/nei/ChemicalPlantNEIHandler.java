package com.mod.htgt6.client.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import codechicken.nei.recipe.GuiRecipe;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.ui.MachineUiTheme;
import gregapi.data.FL;
import gregapi.data.IL;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/** Vector-drawn NEI page for item, fluid and hybrid Chemical Plant recipes. */
public class ChemicalPlantNEIHandler extends MachineNEIRecipeHandler {
    public static final String OVERLAY_ID = "htgt6_chemical_plant";

    public final class CachedChemicalRecipe extends CachedRecipe {
        final ChemicalPlantRecipe recipe;
        final List<PositionedStack> ingredients = new ArrayList<PositionedStack>();
        final List<PositionedStack> extras = new ArrayList<PositionedStack>();
        PositionedStack result;
        PositionedStack selector;

        CachedChemicalRecipe(ChemicalPlantRecipe recipe) {
            this.recipe = recipe;
            for (int i = 0; i < recipe.inputs.length; i++) {
                ItemStack stack = recipe.inputs[i];
                if (stack != null) ingredients.add(new PositionedStack(stack, itemInputX(i), itemInputY(i)));
            }
            for (int i = 0; i < recipe.fluidInputs.length; i++) {
                FluidStack fluid = recipe.fluidInputs[i];
                if (fluid != null) ingredients.add(new PositionedStack(FL.display(fluid, false, false),
                        fluidInputX(i), fluidInputY(i)));
            }
            int itemOutput = 0;
            for (ItemStack stack : recipe.outputs) {
                if (stack == null) continue;
                PositionedStack positioned = new PositionedStack(stack,
                        itemOutputX(itemOutput), itemOutputY(itemOutput));
                if (result == null) result = positioned; else extras.add(positioned);
                itemOutput++;
            }
            for (int i = 0; i < recipe.fluidOutputs.length; i++) {
                FluidStack fluid = recipe.fluidOutputs[i];
                if (fluid != null) extras.add(new PositionedStack(FL.display(fluid, false, false),
                        fluidOutputX(i), fluidOutputY(i)));
            }
            ItemStack selectorStack = recipe.getSelectorDisplayStack();
            if (selectorStack != null) {
                selector = new PositionedStack(selectorStack, selectorX(), selectorY());
                extras.add(selector);
            }
        }

        @Override public List<PositionedStack> getIngredients() {
            return getCycledIngredients(cycleticks / 20, ingredients);
        }
        @Override public PositionedStack getResult() { return result; }
        @Override public List<PositionedStack> getOtherStacks() { return extras; }
    }

    protected List<ChemicalPlantRecipe> getRecipes() {
        return ChemicalPlantRecipeHandler.INSTANCE.getRecipeList();
    }
    protected String getDisplayName() { return "Chemical Plant"; }
    protected String getMachineHeader() { return "CHEMICAL PLANT"; }
    protected String getMachineOverlayId() { return OVERLAY_ID; }
    protected int itemInputX(int index) { return 8 + index % 3 * 18; }
    protected int itemInputY(int index) { return 25 + index / 3 * 18; }
    protected int fluidInputX(int index) { return 64 + index % 2 * 18; }
    protected int fluidInputY(int index) { return 25 + index / 2 * 18; }
    protected int itemOutputX(int index) { return 108 + index % 2 * 18; }
    protected int itemOutputY(int index) { return 25 + index / 2 * 24; }
    protected int fluidOutputX(int index) { return 146; }
    protected int fluidOutputY(int index) { return 25 + index * 18; }
    protected int selectorX() { return 88; }
    protected int selectorY() { return 60; }
    protected int arrowX() { return 90; }
    protected int arrowY() { return 43; }

    @Override public String getRecipeName() { return getDisplayName(); }
    @Override public String getGuiTexture() { return "minecraft:textures/gui/container/inventory.png"; }
    @Override public String getOverlayIdentifier() { return getMachineOverlayId(); }

    @Override public void loadTransferRects() {
        transferRects.add(new RecipeTransferRect(
                new java.awt.Rectangle(arrowX() - 2, arrowY() - 9, 16, 16), getMachineOverlayId()));
    }

    @Override public void loadCraftingRecipes(String outputId, Object... results) {
        if (getMachineOverlayId().equals(outputId)) {
            for (ChemicalPlantRecipe recipe : getRecipes()) {
                arecipes.add(new CachedChemicalRecipe(recipe));
            }
            return;
        }
        super.loadCraftingRecipes(outputId, results);
    }

    @Override public void loadCraftingRecipes(ItemStack result) {
        FluidStack contained = fluidFromDisplayOrContainer(result);
        for (ChemicalPlantRecipe recipe : getRecipes()) {
            if (matchesItem(result, recipe.outputs) || matchesFluid(contained, recipe.fluidOutputs)) {
                arecipes.add(new CachedChemicalRecipe(recipe));
            }
        }
    }

    @Override public void loadUsageRecipes(ItemStack ingredient) {
        FluidStack contained = fluidFromDisplayOrContainer(ingredient);
        for (ChemicalPlantRecipe recipe : getRecipes()) {
            boolean selectorMatches = ingredient != null && IL.Circuit_Selector.equal(ingredient)
                    && recipe.selectorTag == ingredient.getItemDamage();
            if (selectorMatches || matchesItem(ingredient, recipe.inputs)
                    || matchesFluid(contained, recipe.fluidInputs)) {
                arecipes.add(new CachedChemicalRecipe(recipe));
            }
        }
    }

    private static boolean matchesItem(ItemStack target, ItemStack[] stacks) {
        if (target == null) return false;
        for (ItemStack stack : stacks) if (stack != null && NEIServerUtils.areStacksSameTypeCrafting(target, stack)) return true;
        return false;
    }

    private static boolean matchesFluid(FluidStack target, FluidStack[] stacks) {
        if (target == null) return false;
        for (FluidStack stack : stacks) if (stack != null && stack.isFluidEqual(target)) return true;
        return false;
    }

    private static FluidStack fluidFromDisplayOrContainer(ItemStack stack) {
        if (stack == null) return null;
        FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
        return fluid != null ? fluid : FL.getFluid(stack, true);
    }

    @Override public void drawBackground(int recipeIndex) {
        MachineUiTheme theme = MachineUiTheme.getActive();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        rect(0, 0, 166, 124, theme.getPanelColor());
        frame(0, 0, 166, 124, theme.getFrameColor());
        rect(4, 4, 158, 17, theme.getTerminalColor());
        rect(4, 22, 158, 75, theme.getTerminalColor());
        CachedChemicalRecipe cached = (CachedChemicalRecipe) arecipes.get(recipeIndex);
        for (PositionedStack stack : cached.ingredients) slot(stack.relx - 1, stack.rely - 1, theme);
        if (cached.result != null) slot(cached.result.relx - 1, cached.result.rely - 1, theme);
        for (PositionedStack stack : cached.extras) slot(stack.relx - 1, stack.rely - 1, theme);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    @Override public void drawExtras(int recipeIndex) {
        ChemicalPlantRecipe recipe = ((CachedChemicalRecipe) arecipes.get(recipeIndex)).recipe;
        drawOutputChances(recipe);
        MachineUiTheme theme = MachineUiTheme.getActive();
        GuiDraw.drawString(getMachineHeader(), 8, 8, theme.getTitleColor() & 0xFFFFFF, false);
        GuiDraw.drawString(">>", arrowX(), arrowY(), theme.getProductionColor() & 0xFFFFFF, false);
        if (recipe.selectorTag >= 0) {
            GuiDraw.drawString("CFG", selectorX(), selectorY() - 8,
                    theme.getProductionColor() & 0xFFFFFF, false);
        }
        GuiDraw.drawString(recipe.source, 7, 100, theme.getPrimaryColor() & 0xFFFFFF, false);
        GuiDraw.drawString(String.format("%.1f s  %d EU/t  %s", recipe.duration / 20.0D,
                recipe.euPerTick, tierName(recipe.minTier)), 7, 112,
                theme.getEnergyColor() & 0xFFFFFF, false);
        for (int i = 0; i < recipe.fluidInputs.length; i++) {
            FluidStack fluid = recipe.fluidInputs[i];
            if (fluid != null) drawFluidAmount(fluid.amount,
                    fluidInputX(i), fluidInputY(i), 0x7FEFFF);
        }
        for (int i = 0; i < recipe.fluidOutputs.length; i++) {
            FluidStack fluid = recipe.fluidOutputs[i];
            if (fluid != null) drawFluidAmount(fluid.amount,
                    fluidOutputX(i), fluidOutputY(i), 0x80FFB0);
        }
    }

    protected void drawOutputChances(ChemicalPlantRecipe recipe) {
        int displayed = 0;
        for (int i = 0; i < recipe.outputs.length; i++) {
            if (recipe.outputs[i] == null) continue;
            NEIOutputChance.draw(recipe.outputChances, i, itemOutputX(displayed), itemOutputY(displayed));
            displayed++;
        }
    }

    protected static String tierName(int tier) {
        String[] names = { "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV",
                "PUV1", "UX", "OLV", "OMV", "OHV", "OEV", "OIV", "MAX" };
        return names[Math.max(0, Math.min(tier - 1, names.length - 1))];
    }

    @Override
    public List<String> handleItemTooltip(GuiRecipe<?> gui, ItemStack stack,
                                           List<String> tooltip, int recipeIndex) {
        FluidStack hovered = FL.getFluid(stack, true);
        if (hovered == null || recipeIndex < 0 || recipeIndex >= arecipes.size()) return tooltip;
        ChemicalPlantRecipe recipe = ((CachedChemicalRecipe) arecipes.get(recipeIndex)).recipe;
        for (FluidStack input : recipe.fluidInputs) {
            if (input != null && input.isFluidEqual(hovered)) {
                tooltip.add(EnumChatFormatting.AQUA + "Input: " + input.amount + " L");
            }
        }
        for (FluidStack output : recipe.fluidOutputs) {
            if (output != null && output.isFluidEqual(hovered)) {
                tooltip.add(EnumChatFormatting.GREEN + "Output: " + output.amount + " L");
            }
        }
        return tooltip;
    }

    private static void drawFluidAmount(int amount, int x, int y, int color) {
        String text = compactAmount(amount);
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y + 8, 200F);
        GuiDraw.drawString(text, 0, 0, color, true);
        GL11.glPopMatrix();
    }

    private static String compactAmount(int amount) {
        if (amount < 1000) return Integer.toString(amount);
        if (amount < 1_000_000) return amount % 1000 == 0
                ? (amount / 1000) + "k" : String.format("%.1fk", amount / 1000.0D);
        return amount % 1_000_000 == 0
                ? (amount / 1_000_000) + "M" : String.format("%.1fM", amount / 1_000_000.0D);
    }

    private static void slot(int x, int y, MachineUiTheme theme) {
        rect(x, y, 18, 18, theme.getFrameColor());
        rect(x + 1, y + 1, 16, 16, theme.getPanelColor());
    }
    private static void frame(int x, int y, int width, int height, int color) {
        rect(x, y, width, 1, color); rect(x, y + height - 1, width, 1, color);
        rect(x, y, 1, height, color); rect(x + width - 1, y, 1, height, color);
    }
    private static void rect(int x, int y, int width, int height, int color) {
        float a = ((color >>> 24) & 255) / 255F, r = ((color >>> 16) & 255) / 255F;
        float g = ((color >>> 8) & 255) / 255F, b = (color & 255) / 255F;
        GL11.glColor4f(r, g, b, a); GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y); GL11.glVertex2f(x, y + height);
        GL11.glVertex2f(x + width, y + height); GL11.glVertex2f(x + width, y); GL11.glEnd();
    }
}
