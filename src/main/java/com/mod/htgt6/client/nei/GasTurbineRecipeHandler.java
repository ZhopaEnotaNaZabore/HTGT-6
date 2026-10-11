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
import gregapi.data.FL;
import org.lwjgl.opengl.GL11;

public class GasTurbineRecipeHandler extends MachineNEIRecipeHandler {
    private float rotorAngle = 0.0F;

    public GasTurbineRecipeHandler() {
    }

    public String getRecipeName() {
        return "Gas Turbine";
    }

    public String getGuiTexture() {
        // The ModularUI machine screen has no legacy PNG backing.  The NEI
        // card renders itself below, so use NEI's neutral texture as fallback.
        return "nei:textures/gui/recipebg.png";
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
        // Compact, self-contained panel; it no longer borrows the obsolete
        // in-world GUI texture.
        GL11.glColor4f(0.11F, 0.13F, 0.16F, 1.0F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2i(0, 0);
        GL11.glVertex2i(0, 68);
        GL11.glVertex2i(166, 68);
        GL11.glVertex2i(166, 0);
        GL11.glEnd();

        GL11.glColor4f(0.18F, 0.25F, 0.31F, 1.0F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2i(1, 1);
        GL11.glVertex2i(1, 14);
        GL11.glVertex2i(165, 14);
        GL11.glVertex2i(165, 1);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Vanilla slot frame keeps the fluid-display stack visually aligned
        // with the GT-style virtual fluid slot used by the turbine GUI.
        GuiDraw.changeTexture("textures/gui/container/inventory.png");
        GuiDraw.drawTexturedModalRect(12, 18, 7, 6, 20, 20);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void drawForeground(int recipe) {
        CachedTurbineRecipe currentRecipe = (CachedTurbineRecipe) this.arecipes.get(recipe);
        String name = currentRecipe.fluidStack.getLocalizedName();
        if (name.length() > 14) {
            name = name.substring(0, 12) + "..";
        }

        GuiDraw.drawString("Universal Gas Turbine", 6, 4, 0xB7D9FF, false);
        GuiDraw.drawString("Gas: " + name, 42, 20, 0xFFFFFF, false);
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
        GuiDraw.drawString(String.format("Fuel unit: %d mB", capacity), 42, 32, 0xFFFF55, false);
        GuiDraw.drawString(formattedTotalPower + " EU", 42, 43, 0x55FF55, false);
        GuiDraw.drawString(formattedEuPerMb + " EU/mB", 42, 54, 0xFFAA55, false);
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

            // GT6's virtual display item works for every registered gas,
            // including gases which do not have a bucket/cell container.
            this.fluidCellSlot = new PositionedStack(FL.display(fluidStack, false, false), 14, 20);
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
