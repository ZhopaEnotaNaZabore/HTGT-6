package com.mod.htgt6.client.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.mod.htgt6.common.TE.RadioisotopeGeneratorTE;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class RadioisotopeGeneratorRecipeHandler extends MachineNEIRecipeHandler {
    @Override protected int panelHeight(int index) {
        return index >= 0 && index < arecipes.size()
                ? 156 + 10 * ((CachedIsotopeRecipe) arecipes.get(index)).fuel.outputs.size() : 156;
    }
    private static final String[] TIERS = {
            "ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "UHV", "UEV", "UIV", "UMV", "UXV", "MAX"
    };

    @Override
    public String getRecipeName() {
        return "Radioisotope Generator";
    }

    @Override
    public String getGuiTexture() {
        // TemplateRecipeHandler requires a valid texture even though this
        // handler paints its complete recipe panel with vector geometry.
        return "minecraft:textures/gui/container/inventory.png";
    }

    @Override
    public void loadTransferRects() {
        this.transferRects.add(new RecipeTransferRect(new Rectangle(45, 30, 22, 16), "isotope_decay"));
    }

    @Override
    public void drawBackground(int recipe) {
        // --- STEP 1: INITIALIZE OPENGL STATES ---
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // --- STEP 2: DRAW MAIN INTERFACE BACKDROP ---
        // Draws a sleek dark panel from (0,0) matching NEI standard texture dimensions (166x75)
        GL11.glColor4f(0.20f, 0.20f, 0.22f, 1.0f); // Dark industrial slate base
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(0, 75);
        GL11.glVertex2f(166, 75);
        GL11.glVertex2f(166, 0);
        GL11.glEnd();

        // Optional panel subtle inner dark gray border line loop
        GL11.glColor4f(0.12f, 0.12f, 0.14f, 1.0f);
        GL11.glLineWidth(1.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(1, 1);
        GL11.glVertex2f(1, 74);
        GL11.glVertex2f(165, 74);
        GL11.glVertex2f(165, 1);
        GL11.glEnd();

        // --- STEP 3: DRAW INVENTORY SLOTS USING GEOMETRY ---
        // Setup coordinates matching your PositionedStack layouts
        int neiInputX = 15;
        int neiInputY = 24;
        int neiOutputXStart = 60;
        int neiOutputYStart = 15;

        // Function helper to render slots in pure OpenGL without binding dispenser textures
        drawVectorSlot(neiInputX, neiInputY);

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int slotX = neiOutputXStart + (col * 18);
                int slotY = neiOutputYStart + (row * 18);
                drawVectorSlot(slotX, slotY);
            }
        }

        // --- STEP 4: DRAW THE PURE VECTOR RADIOACTIVITY SIGN ---
        // Positioned cleanly in the gap between slot matrices
        int hazardX = neiInputX + 24;
        int hazardY = neiInputY + 9;
     //   drawRadioactivitySign(hazardX, hazardY, true);

        // --- STEP 5: CLEANUP & RESTORE PIPELINE STATES ---
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
    private void drawVectorSlot(int x, int y) {
        // Outer Slot background well
        GL11.glColor4f(0.14f, 0.14f, 0.14f, 1.0f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + 18);
        GL11.glVertex2f(x + 18, y + 18);
        GL11.glVertex2f(x + 18, y);
        GL11.glEnd();

        // Inner slot inset surface highlight
        GL11.glColor4f(0.35f, 0.35f, 0.35f, 1.0f);
        GL11.glLineWidth(1.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + 18);
        GL11.glVertex2f(x + 18, y + 18);
        GL11.glVertex2f(x + 18, y);
        GL11.glEnd();
    }
    private void drawRadioactivitySign(int cx, int cy, boolean active) {
        float[] baseColor = active ? new float[]{0.95f, 0.85f, 0.1f, 1.0f} : new float[]{0.5f, 0.5f, 0.5f, 0.4f};
        float[] signColor = active ? new float[]{0.15f, 0.15f, 0.15f, 1.0f} : new float[]{0.3f, 0.3f, 0.3f, 0.5f};

        // Outer Yellow Warning Triangle Shape outline
        GL11.glColor4f(baseColor[0], baseColor[1], baseColor[2], baseColor[3]);
        GL11.glLineWidth(2.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(cx, cy - 10);
        GL11.glVertex2f(cx - 11, cy + 9);
        GL11.glVertex2f(cx + 11, cy + 9);
        GL11.glEnd();

        float symbolCy = cy + 1.5f;
        int segments = 12;
        double outerRadius = 5.0;
        double innerRadius = 1.5;
        double[] bladeAngles = { -90, 30, 150 };

        // Trefoil Radiation Fans
        GL11.glColor4f(signColor[0], signColor[1], signColor[2], signColor[3]);
        for (double startAngle : bladeAngles) {
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            for (int i = 0; i <= segments; i++) {
                double angleRad = Math.toRadians(startAngle + (i * (60.0 / segments)));
                double cos = Math.cos(angleRad);
                double sin = Math.sin(angleRad);
                GL11.glVertex2f((float)(cx + cos * innerRadius), (float)(symbolCy + sin * innerRadius));
                GL11.glVertex2f((float)(cx + cos * outerRadius), (float)(symbolCy + sin * outerRadius));
            }
            GL11.glEnd();
        }

        // Center Isolation Dot
        GL11.glColor4f(signColor[0], signColor[1], signColor[2], signColor[3]);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, symbolCy);
        for (int i = 0; i <= segments; i++) {
            double angleRad = Math.toRadians(i * (360.0 / segments));
            GL11.glVertex2f((float)(cx + Math.cos(angleRad) * 0.9), (float)(symbolCy + Math.sin(angleRad) * 0.9));
        }
        GL11.glEnd();
        GL11.glLineWidth(1.0f);
    }

    @Override
    public void drawForeground(int recipe) {
        CachedIsotopeRecipe rec = (CachedIsotopeRecipe) arecipes.get(recipe);
        int y = 90;

        GuiDraw.drawString("=== Продукты ядерного распада ===", 6, y, 0x0FF00, false);
        y += 10;

        GuiDraw.drawString("Fuel: " + rec.fuelStack.getDisplayName(), 6, y, 0xAAAAAA, false);
        y += 10;

        GuiDraw.drawString("Decay: " + rec.decayTime + " ticks (" + (rec.decayTime / 20.0) + "s)", 6, y, 0x55FFFF, false);
        y += 10;

        GuiDraw.drawString("Output: " + rec.euPerTick + " EU/t | Total: " + rec.totalEU + " EU", 6, y, 0x55FF55, false);
        y += 10;

        String tierStr = (rec.requiredTier >= 1 && rec.requiredTier <= TIERS.length) ? TIERS[rec.requiredTier - 1] : String.valueOf(rec.requiredTier);
        GuiDraw.drawString("Required Min Tier: " + tierStr, 6, y, 0xFFAA00, false);
        y += 12;

        GuiDraw.drawString("Chances (" + rec.fuel.outputs.size() + " outputs):", 6, y, 0xFF55FF, false);
        y += 10;

        for (RadioisotopeGeneratorTE.DecayOutput out : rec.fuel.outputs) {
            String cleanName = out.stack.getDisplayName();
            if (cleanName.length() > 18) {
                cleanName = cleanName.substring(0, 16) + "..";
            }
            GuiDraw.drawString("- " + cleanName + " x" + out.stack.stackSize + " -> " + (out.chance * 100D) + "%", 6, y, 0xCCCCCC, false);
            y += 10;
        }
    }

    @Override
    public String getOverlayIdentifier() {
        return "isotope_decay";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("isotope_decay") && getClass() == RadioisotopeGeneratorRecipeHandler.class) {
            for (Map.Entry<String, RadioisotopeGeneratorTE.IsotopeFuel> e : RadioisotopeGeneratorTE.VALID_FUELS.entrySet()) {
                arecipes.add(new CachedIsotopeRecipe(e.getValue()));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadUsageRecipes(String inputId, Object... ingredients) {
        if (inputId.equals("isotope_decay") && getClass() == RadioisotopeGeneratorRecipeHandler.class) {
            loadCraftingRecipes("isotope_decay");
        } else {
            super.loadUsageRecipes(inputId, ingredients);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (Map.Entry<String, RadioisotopeGeneratorTE.IsotopeFuel> e : RadioisotopeGeneratorTE.VALID_FUELS.entrySet()) {
            for (RadioisotopeGeneratorTE.DecayOutput out : e.getValue().outputs) {
                if (out.stack.isItemEqual(result)) {
                    arecipes.add(new CachedIsotopeRecipe(e.getValue()));
                    break;
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        String key = RadioisotopeGeneratorTE.getFuelKey(ingredient);
        RadioisotopeGeneratorTE.IsotopeFuel fuel = RadioisotopeGeneratorTE.VALID_FUELS.get(key);
        if (fuel != null) {
            arecipes.add(new CachedIsotopeRecipe(fuel));
        }
    }

    public class CachedIsotopeRecipe extends CachedRecipe {
        public final PositionedStack inputFuel;
        public final List<PositionedStack> outputDecays;
        public final PositionedStack primaryResult;
        public final RadioisotopeGeneratorTE.IsotopeFuel fuel;
        public final ItemStack fuelStack;
        public final int decayTime;
        public final int euPerTick;
        public final int requiredTier;
        public final long totalEU;

        public CachedIsotopeRecipe(RadioisotopeGeneratorTE.IsotopeFuel fuelData) {
            this.fuel = fuelData;
            this.fuelStack = fuelData.inputStack;
            this.inputFuel = new PositionedStack(fuelStack, 15, 24);
            this.outputDecays = new ArrayList<PositionedStack>();

            PositionedStack firstResult = null;
            int slot = 0;
            for (RadioisotopeGeneratorTE.DecayOutput output : fuelData.outputs) {
                int x = 60 + (slot % 3) * 18;
                int y = 15 + (slot / 3) * 18;

                PositionedStack posStack = new PositionedStack(output.stack.copy(), x, y);
                if (slot == 0) {
                    firstResult = posStack;
                } else {
                    outputDecays.add(posStack);
                }
                slot++;
            }

            this.primaryResult = firstResult;
            this.decayTime = fuelData.decayTime;
            this.euPerTick = fuelData.euPerTick;
            this.requiredTier = fuelData.tier;
            this.totalEU = (long) fuelData.decayTime * (long) fuelData.euPerTick;
        }

        @Override public PositionedStack getIngredient() { return inputFuel; }
        @Override public List<PositionedStack> getOtherStacks() { return outputDecays; }
        @Override public PositionedStack getResult() { return primaryResult; }
    }
}
