package com.mod.htgt6.client.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.mod.htgt6.common.TE.UniversalGasTurbineTE;
import com.mod.htgt6.common.block.hitech6.BlockUniversalGasTurbine;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class GasTurbineRecipeHandler extends TemplateRecipeHandler {
    private float rotorAngle = 0.0F;

    public GasTurbineRecipeHandler() {
    }

    public String getRecipeName() {
        return "Gas Turbine";
    }

    public String getGuiTexture() {
        return "htgt6:textures/gui/UniversalGasTurbine.png";
    }

    public void loadTransferRects() {
        this.transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(70, 40, 20, 20), "gasturbine", new Object[0]));
    }

    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("gasturbine") && this.getClass() == GasTurbineRecipeHandler.class) {
            Iterator var3 = UniversalGasTurbineTE.VALID_GAS_FUELS.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry)var3.next();
                if (FluidRegistry.isFluidRegistered((String)entry.getKey())) {
                    FluidStack fluid = new FluidStack(FluidRegistry.getFluid((String)entry.getKey()), 1000);
                    this.arecipes.add(new CachedTurbineRecipe(this, fluid, (Integer)entry.getValue()));
                }
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }

    }

    public void loadUsageRecipes(String inputId, Object... results) {
        if (inputId.equals("gasturbine") && this.getClass() == GasTurbineRecipeHandler.class) {
            this.loadCraftingRecipes("gasturbine");
        } else {
            super.loadUsageRecipes(inputId, results);
        }

    }

    public void loadUsageRecipes(ItemStack ingredient) {
        if (ingredient != null && ingredient.getItem() != null) {
            Block block = Block.getBlockFromItem(ingredient.getItem());
            if (block instanceof BlockUniversalGasTurbine) {
                this.loadCraftingRecipes("gasturbine");
            } else {
                Iterator var3 = UniversalGasTurbineTE.VALID_GAS_FUELS.entrySet().iterator();

                while(var3.hasNext()) {
                    Map.Entry<String, Integer> entry = (Map.Entry)var3.next();
                    if (FluidRegistry.isFluidRegistered((String)entry.getKey())) {
                        FluidStack fluid = new FluidStack(FluidRegistry.getFluid((String)entry.getKey()), 1000);
                        CachedTurbineRecipe recipe = new CachedTurbineRecipe(this, fluid, (Integer)entry.getValue());
                        if (recipe.containsFluid(ingredient)) {
                            this.arecipes.add(recipe);
                        }
                    }
                }

            }
        }
    }

    public void loadCraftingRecipes(ItemStack result) {
        this.loadUsageRecipes(result);
    }

    public void drawBackground(int recipe) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        int xStart = 0;
        int yStart = 0;
        int xEnd = 166;
        int yEnd = 65;
        float r = 0.7764706F;
        float g = 0.7764706F;
        float b = 0.7764706F;
        float alpha = 1.0F;
        GL11.glColor4f(r, g, b, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2i(xStart, yStart);
        GL11.glVertex2i(xStart, yEnd);
        GL11.glVertex2i(xEnd, yEnd);
        GL11.glVertex2i(xEnd, yStart);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void drawForeground(int recipe) {
        CachedTurbineRecipe currentRecipe = (CachedTurbineRecipe) this.arecipes.get(recipe);
        String name = currentRecipe.fluidStack.getLocalizedName();
        if (name.length() > 14) {
            name = name.substring(0, 12) + "..";
        }

        GuiDraw.drawString("Gas: " + name, 42, 8, 4210752, false);
        int capacity = 1000;
        if (currentRecipe.fluidCellSlot != null && currentRecipe.fluidCellSlot.item != null) {
            ItemStack stack = currentRecipe.fluidCellSlot.item;
            FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(stack);
            if (contained != null && contained.amount > 0) {
                capacity = contained.amount;
            } else if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Fluid")) {
                NBTTagCompound fluidTag = stack.getTagCompound().getCompoundTag("Fluid");
                if (fluidTag.hasKey("Amount")) {
                    capacity = fluidTag.getInteger("Amount");
                }
            }
        }

        long totalPowerFromContainer = (long) currentRecipe.euPerMb * (long) capacity;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        DecimalFormat cleanFormatter = new DecimalFormat("#,###", symbols);
        String formattedTotalPower = cleanFormatter.format(totalPowerFromContainer);
        String formattedEuPerMb = cleanFormatter.format((long) currentRecipe.euPerMb);
        GuiDraw.drawString(String.format("1 Slot (%dmB) =", capacity), 42, 20, 255, false);
        GuiDraw.drawString(formattedTotalPower + " EU", 42, 31, 34816, false);
        GuiDraw.drawString(formattedEuPerMb + " EU/mB", 42, 43, 11141375, false);
        GuiDraw.changeTexture("textures/gui/container/inventory.png");
        GuiDraw.drawTexturedModalRect(15, 19, 8, 7, 18, 18);
        if (Minecraft.getMinecraft().thePlayer != null) {
            this.rotorAngle += 2.0F;
            if (this.rotorAngle >= 360.0F) {
                this.rotorAngle -= 360.0F;
            }
        }

        this.drawRotor(135, 28, this.rotorAngle);
    }

    private void drawRotor(int x, int y, float angle) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, 0.0F);
        GL11.glRotatef(angle, 0.0F, 0.0F, 1.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor3f(0.65F, 0.65F, 0.7F);

        int i;
        for(i = 0; i < 8; ++i) {
            GL11.glPushMatrix();
            GL11.glRotatef((float)i * 45.0F, 0.0F, 0.0F, 1.0F);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(-2.0F, -8.0F);
            GL11.glVertex2f(2.0F, -8.0F);
            GL11.glVertex2f(3.0F, -16.0F);
            GL11.glVertex2f(-3.0F, -16.0F);
            GL11.glEnd();
            GL11.glPopMatrix();
        }

        GL11.glColor3f(0.25F, 0.25F, 0.3F);
        GL11.glBegin(GL11.GL_POLYGON);

        double a;
        for(i = 0; i < 32; ++i) {
            a = Math.toRadians((double)i * 11.25);
            GL11.glVertex2d(Math.cos(a) * 10.0, Math.sin(a) * 10.0);
        }

        GL11.glEnd();
        GL11.glColor3f(0.85F, 0.7F, 0.2F);
        GL11.glBegin(GL11.GL_POLYGON);

        for(i = 0; i < 32; ++i) {
            a = Math.toRadians((double)i * 11.25);
            GL11.glVertex2d(Math.cos(a) * 5.0, Math.sin(a) * 5.0);
        }

        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    // --- NEI RECIPE INNER CLASS IMPLEMENTATION ---
    public class CachedTurbineRecipe extends TemplateRecipeHandler.CachedRecipe {
        public FluidStack fluidStack;
        public int euPerMb;
        public PositionedStack fluidCellSlot;

        public CachedTurbineRecipe(GasTurbineRecipeHandler handler, FluidStack fluidStack, int euPerMb) {
            this.fluidStack = fluidStack;
            this.euPerMb = euPerMb;

            // Generate standard container item for this fluid (e.g. filled fluid cells/buckets)
            ItemStack filledContainer = FluidContainerRegistry.fillFluidContainer(fluidStack, FluidContainerRegistry.EMPTY_BUCKET);
            if (filledContainer == null) {
                // Fallback approach if standard empty bucket didn't match anything
                FluidContainerRegistry.FluidContainerData[] data = FluidContainerRegistry.getRegisteredFluidContainerData();
                for (FluidContainerRegistry.FluidContainerData containerData : data) {
                    if (containerData.fluid.getFluid() == fluidStack.getFluid()) {
                        filledContainer = containerData.filledContainer;
                        break;
                    }
                }
            }

            // Assign container item stack to your target coordinate layout matching your drawForeground matrix
            if (filledContainer != null) {
                this.fluidCellSlot = new PositionedStack(filledContainer, 16, 20);
            }
        }

        @Override
        public List<PositionedStack> getIngredients() {
            List<PositionedStack> list = new ArrayList<PositionedStack>();
            if (this.fluidCellSlot != null) {
                list.add(this.fluidCellSlot);
            }
            return list;
        }

        @Override
        public PositionedStack getResult() {
            return null;
        }

        public boolean containsFluid(ItemStack ingredient) {
            if (ingredient == null) return false;
            FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(ingredient);
            return contained != null && contained.getFluid() == this.fluidStack.getFluid();
        }
    }
}