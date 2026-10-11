package com.mod.htgt6.client.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.mod.htgt6.common.TE.UniversalEngineTE;
import com.mod.htgt6.common.block.hitech6.BlockUniversalEngine;
import gregapi.data.FL;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/** Compact NEI view of every registered Universal Engine fuel. */
public class UniversalEngineRecipeHandler extends MachineNEIRecipeHandler {
    @Override public String getRecipeName() { return "Universal Engine Fuels"; }
    @Override public String getGuiTexture() { return "textures/gui/container/inventory.png"; }
    @Override public void loadTransferRects() { transferRects.add(new RecipeTransferRect(new Rectangle(0, 0, 166, 65), "universalengine")); }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (!"universalengine".equals(outputId)) { super.loadCraftingRecipes(outputId, results); return; }
        // Query the API on every NEI opening. This picks up all fuels added by
        // ConfigureEngineFuels and external integrations after the handler was
        // registered on the client.
        for (UniversalEngineTE.RegisteredFluidFuel fuel : UniversalEngineTE.getRegisteredFluidFuels()) {
            if (FluidRegistry.isFluidRegistered(fuel.fluidName)) {
                arecipes.add(new CachedEngineFuel(new FluidStack(FluidRegistry.getFluid(fuel.fluidName), 1000),
                        fuel.euPerMb, fuel.type.getDisplayName()));
            }
        }
        for (UniversalEngineTE.RegisteredSolidFuel fuel : UniversalEngineTE.getRegisteredSolidFuels()) {
            arecipes.add(new CachedEngineFuel(fuel.stack, fuel.totalEu));
        }
    }

    @Override public void loadUsageRecipes(String inputId, Object... results) {
        if ("universalengine".equals(inputId)) loadCraftingRecipes(inputId); else super.loadUsageRecipes(inputId, results);
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (ingredient == null) return;
        Block block = Block.getBlockFromItem(ingredient.getItem());
        if (block instanceof BlockUniversalEngine) { loadCraftingRecipes("universalengine"); return; }
        FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(ingredient);
        if (fluid != null) {
            UniversalEngineTE.FuelType type = UniversalEngineTE.getFuelType(fluid.getFluid().getName());
            if (type != null) arecipes.add(new CachedEngineFuel(new FluidStack(fluid.getFluid(), 1000),
                    UniversalEngineTE.getFuelEuPerMb(fluid.getFluid().getName()), type.getDisplayName()));
            return;
        }
        long solidEu = UniversalEngineTE.getSolidFuelEu(ingredient);
        if (solidEu > 0L) arecipes.add(new CachedEngineFuel(ingredient.copy(), solidEu));
    }

    @Override public void loadCraftingRecipes(ItemStack result) { loadUsageRecipes(result); }

    @Override
    public void drawBackground(int recipe) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GuiDraw.drawRect(0, 0, 166, 65, 0xFF20252A);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // The engine accepts every registered fluid, including ones without a
        // bucket/cell item. Draw the same virtual GT fluid slot as the turbine.
        GuiDraw.changeTexture("textures/gui/container/inventory.png");
        GuiDraw.drawTexturedModalRect(12, 20, 7, 6, 20, 20);
    }

    @Override
    public void drawForeground(int recipe) {
        CachedEngineFuel fuel = (CachedEngineFuel) arecipes.get(recipe);
        GuiDraw.drawString("Universal Engine", 42, 5, 0xE5E5E5, false);
        GuiDraw.drawString("Type: " + fuel.type, 42, 18, 0x8ED6FF, false);
        GuiDraw.drawString(fuel.getName(), 42, 30, 0xFFFFFF, false);
        GuiDraw.drawString(fuel.value + (fuel.fluid == null ? " EU / item" : " EU / mB"), 42, 44, 0x9AF7A3, false);
    }

    public class CachedEngineFuel extends CachedRecipe {
        private final FluidStack fluid;
        private final ItemStack solid;
        private final long value;
        private final String type;
        private final PositionedStack ingredient;
        CachedEngineFuel(FluidStack fluid, long value, String type) {
            this.fluid = fluid; this.solid = null; this.value = value; this.type = type;
            // FL.display is the GT6 virtual fluid item used by the turbine NEI
            // handler. It is available for all fluids, not only bucket-capable
            // ones, and provides the native fluid tooltip/texture.
            this.ingredient = new PositionedStack(FL.display(fluid, false, false), 14, 22);
        }
        CachedEngineFuel(ItemStack solid, long value) {
            this.fluid = null; this.solid = solid; this.value = value; this.type = "Solid";
            this.ingredient = new PositionedStack(solid, 16, 24);
        }
        String getName() { return fluid != null ? fluid.getLocalizedName() : solid.getDisplayName(); }
        @Override public List<PositionedStack> getIngredients() {
            List<PositionedStack> result = new ArrayList<PositionedStack>();
            if (ingredient != null) result.add(ingredient);
            return result;
        }
        @Override public PositionedStack getResult() { return null; }
    }
}
