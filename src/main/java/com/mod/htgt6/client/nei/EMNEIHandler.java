package com.mod.htgt6.client.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.common.handler.recipe.RM.ElectromagneticProcessorRecipeMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class EMNEIHandler extends MachineNEIRecipeHandler {

    public class CachedEMRecipe extends CachedRecipe {
        public PositionedStack input;
        public List<PositionedStack> ingredients=new ArrayList<PositionedStack>();
        public List<PositionedStack> outputs = new ArrayList<PositionedStack>();
        public MachineRecipe recipe;

        public CachedEMRecipe(MachineRecipe recipe) {
            this.recipe = recipe;

            // 1. Input Slot (16, 25)
            if (recipe.inputs != null && recipe.inputs.length > 0 && recipe.inputs[0] != null) {
                input = new PositionedStack(recipe.inputs[0], 16, 25);
                ingredients.add(input);
            }

            for(FluidStack fluid:recipe.fluidInputs)if(fluid!=null)
                ingredients.add(new PositionedStack(gregapi.data.FL.display(fluid,false,false),54,61));
            for(FluidStack fluid:recipe.fluidOutputs)if(fluid!=null)
                outputs.add(new PositionedStack(gregapi.data.FL.display(fluid,false,false),107,61));
            // 2. Output Slots 2x3 Grid starting at (107, 16)
            if (recipe.outputs != null) {
                for (int i = 0; i < Math.min(recipe.outputs.length, 6); i++) {
                    ItemStack stack = recipe.outputs[i];
                    if (stack != null) {
                        int col = i % 3;
                        int row = i / 3;
                        outputs.add(new PositionedStack(stack, 107 + col * 18, 16 + row * 18));
                    }
                }
            }
        }

        @Override
        public List<PositionedStack> getIngredients() { return ingredients; }

        @Override
        public PositionedStack getIngredient() {
            return input;
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            return outputs;
        }

        @Override
        public PositionedStack getResult() {
            return null;
        }
    }

    @Override
    public String getRecipeName() {
        return "Electromagnetic Processor";
    }

    @Override
    public String getGuiTexture() {
        return "htgt6:textures/gui/em_processor.png";
    }

    @Override
    public String getOverlayIdentifier() {
        return "em_processor";
    }

    @Override
    public void loadTransferRects() {
        this.transferRects.add(new RecipeTransferRect(new Rectangle(78, 23, 24, 16), getOverlayIdentifier()));
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(getOverlayIdentifier())) {
            for (MachineRecipe recipe : ElectromagneticProcessorRecipeMap.INSTANCE.getRecipeList()) {
                arecipes.add(new CachedEMRecipe(recipe));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        FluidStack requested=fluid(result);
        for (MachineRecipe recipe : ElectromagneticProcessorRecipeMap.INSTANCE.getRecipeList()) {
            if(requested!=null&&contains(recipe.fluidOutputs,requested)){arecipes.add(new CachedEMRecipe(recipe));continue;}
            if (recipe != null && recipe.outputs != null) {
                for (ItemStack out : recipe.outputs) {
                    if (out != null && NEIServerUtils.areStacksSameTypeCrafting(out, result)) {
                        arecipes.add(new CachedEMRecipe(recipe));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        FluidStack requested=fluid(ingredient);
        for (MachineRecipe recipe : ElectromagneticProcessorRecipeMap.INSTANCE.getRecipeList()) {
            if(requested!=null&&contains(recipe.fluidInputs,requested)){arecipes.add(new CachedEMRecipe(recipe));continue;}
            if (recipe != null && recipe.inputs != null) {
                for (ItemStack in : recipe.inputs) {
                    if (in != null && NEIServerUtils.areStacksSameTypeCrafting(ingredient, in)) {
                        arecipes.add(new CachedEMRecipe(recipe));
                        break;
                    }
                }
            }
        }
    }

    private static FluidStack fluid(ItemStack stack) {
        if(stack==null)return null;
        FluidStack fluid=net.minecraftforge.fluids.FluidContainerRegistry.getFluidForFilledItem(stack);
        return fluid!=null?fluid:gregapi.data.FL.getFluid(stack,true);
    }
    private static boolean contains(FluidStack[] stacks,FluidStack fluid) {
        for(FluidStack stack:stacks)if(stack!=null&&stack.isFluidEqual(fluid))return true;
        return false;
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
        CachedEMRecipe cached = (CachedEMRecipe) arecipes.get(recipeIndex);
        MachineRecipe r = cached.recipe;

        // Render output chance percentages over output slots if less than 100% (10000)
        if (r.outputs != null && r.outputChances != null) {
            for (int i = 0; i < Math.min(r.outputs.length, 6); i++) {
                if (r.outputs[i] == null) continue;

                int chance = r.outputChances[i];
                if (chance < 10000) {
                    int col = i % 3;
                    int row = i / 3;
                    int posX = 107 + col * 18;
                    int posY = 16 + row * 18;

                    String chanceStr = String.format("%.1f%%", chance / 100.0F);

                    GL11.glPushMatrix();
                    GL11.glScalef(0.5F, 0.5F, 0.5F);
                    // Draws centered chance string at the bottom of the slot item
                    GuiDraw.drawStringC(chanceStr, (posX + 8) * 2, (posY + 14) * 2, 0xFFE000, true);
                    GL11.glPopMatrix();
                }
            }
        }

        int yOffset = 88;
        long totalEU = (long) r.euPerTick * (long) r.duration;

        GuiDraw.drawString("Min Tier: " + r.minTier, 8, yOffset, 0xFFFFFF, false);
        GuiDraw.drawString("EU/t: " + r.euPerTick, 8, yOffset + 10, 0x00FF00, false);
        String formattedEU = String.format("Total EU: %,d EU", totalEU).replace('\u00A0', ' ');
        GuiDraw.drawString(formattedEU, 8, yOffset + 20, 0xFFFF00, false);
        GuiDraw.drawString(String.format("Time: %.1f s", (double) r.duration / 20.0), 8, yOffset + 30, 0x00FFFF, false);

        if (r.fluidInputs != null && r.fluidInputs.length > 0) {
            int fluidY = yOffset + 42;
            GuiDraw.drawString("Required Fluids:", 8, fluidY, 0xFFAA00, false);

            for (int i = 0; i < r.fluidInputs.length; i++) {
                FluidStack fluid = r.fluidInputs[i];
                if (fluid != null && fluid.getFluid() != null) {
                    String fluidStr = "- " + fluid.getLocalizedName() + " (" + fluid.amount + " mB)";
                    GuiDraw.drawString(fluidStr, 14, fluidY + 10 + (i * 10), 0x8B0000, false);
                }
            }
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }


}
