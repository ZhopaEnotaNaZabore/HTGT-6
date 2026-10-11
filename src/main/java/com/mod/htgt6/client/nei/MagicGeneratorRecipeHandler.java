package com.mod.htgt6.client.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.mod.htgt6.common.TE.MagicEnergyGeneratorTE;
import com.mod.htgt6.common.block.hitech6.BlockMagicEnergyGenerator;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;

public class MagicGeneratorRecipeHandler extends MachineNEIRecipeHandler {

    private float magicCoreAngle = 0.0F;


    @Override
    public String getRecipeName() {
        return "Magic Energy Generator";
    }

    @Override
    public String getGuiTexture() {
        return "nei:textures/gui/recipebg.png";
    }

    @Override
    public void loadTransferRects() {
        this.transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(68, 33, 24, 24), "magicenergygen"));
    }

    @Override
    public void drawBackground(int recipeIndex) {

        GL11.glColor4f(1F, 1F, 1F, 1F);

        this.magicCoreAngle += 1.0F;

        if (this.magicCoreAngle >= 360F)
            this.magicCoreAngle -= 360F;

        drawSimpleMagicCore(80, 24);
    }

    private void drawSimpleMagicCore(int x, int y) {

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        GL11.glTranslatef((float)x, (float)y, 0.0F);
        GL11.glRotatef(magicCoreAngle, 0.0F, 0.0F, 1.0F);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);

        GL11.glColor4f(0.6F, 0.2F, 1.0F, 1F);

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex2f(-8, -2);
        GL11.glVertex2f( 8, -2);
        GL11.glVertex2f( 8,  2);
        GL11.glVertex2f(-8,  2);

        GL11.glVertex2f(-2, -8);
        GL11.glVertex2f( 2, -8);
        GL11.glVertex2f( 2,  8);
        GL11.glVertex2f(-2,  8);

        GL11.glEnd();

        GL11.glColor4f(0.2F, 1F, 1F, 1F);

        GL11.glBegin(GL11.GL_POLYGON);

        for (int i = 0; i < 24; i++) {
            double a = Math.toRadians(i * 15);
            GL11.glVertex2d(
                    Math.cos(a) * 3,
                    Math.sin(a) * 3
            );
        }

        GL11.glEnd();

        GL11.glPopMatrix();
        GL11.glPopAttrib();

        GL11.glColor4f(1F,1F,1F,1F);
    }

    @Override
    public void drawForeground(int recipe) {

        MagicGeneratorCachedRecipe r =
                (MagicGeneratorCachedRecipe) arecipes.get(recipe);

        GuiDraw.drawString(
                "Magic Energy Generator",
                20,
                5,
                0x404040,
                false);

        GuiDraw.drawString(
                "Produces:",
                52,
                40,
                0x404040,
                false);

        GuiDraw.drawString(
                formatEnergy(r.totalEnergyOutput) + " EU",
                42,
                52,
                34816,
                false
        );

    }
    private String formatEnergy(double energy) {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();

        symbols.setGroupingSeparator(',');

        DecimalFormat df =
                new DecimalFormat("#,###", symbols);

        return df.format((long) energy);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("magicenergygen") && this.getClass() == MagicGeneratorRecipeHandler.class) {
            setupAllCachedRecipes();
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (ingredient == null) return;

        // Open category when pressing U on machine block
        Block block = Block.getBlockFromItem(ingredient.getItem());

        if (block instanceof BlockMagicEnergyGenerator) {
            loadCraftingRecipes("magicenergygen");
            return;
        }

        double fuelVal = MagicEnergyGeneratorTE.getMagicFuelEu(ingredient);

        if (fuelVal > 0) {
            arecipes.add(
                    new MagicGeneratorCachedRecipe(
                            ingredient.copy(),
                            fuelVal
                    )
            );
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        loadUsageRecipes(result);
    }

    private void setupAllCachedRecipes() {

        for (MagicEnergyGeneratorTE.RegisteredMagicFuel fuel : MagicEnergyGeneratorTE.getRegisteredMagicFuels()) {
            arecipes.add(new MagicGeneratorCachedRecipe(fuel.stack, fuel.totalEu));
        }
    }

    public class MagicGeneratorCachedRecipe extends TemplateRecipeHandler.CachedRecipe {
        private final PositionedStack fuelItemSlot;
        public final double totalEnergyOutput;

        public MagicGeneratorCachedRecipe(ItemStack fuelStack, double energyOutput) {
            fuelStack.stackSize = 1;
            this.fuelItemSlot = new PositionedStack(fuelStack, 40, 25);
            this.totalEnergyOutput = energyOutput;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            List<PositionedStack> list = new ArrayList<PositionedStack>();
            if (this.fuelItemSlot != null) {
                list.add(this.fuelItemSlot);
            }
            return list;
        }

        @Override
        public PositionedStack getResult() {
            return null;
        }
    }
}
