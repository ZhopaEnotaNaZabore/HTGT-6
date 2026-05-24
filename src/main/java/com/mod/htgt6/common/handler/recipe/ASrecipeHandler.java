package com.mod.htgt6.common.handler.recipe;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.recipe.DefaultOverlayHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;

import com.mod.htgt6.client.GUI.GuiAssembler;

import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ASrecipeHandler
        extends TemplateRecipeHandler {

    // ==========================================
    // REGISTER NEI
    // ==========================================

    public static void registerNEI() {

        ASrecipeHandler handler =
                new ASrecipeHandler();

        API.registerRecipeHandler(
                handler
        );

        API.registerUsageHandler(
                handler
        );

        API.registerGuiOverlay(
                GuiAssembler.class,
                "assembling_machine"
        );

        API.registerGuiOverlayHandler(
                GuiAssembler.class,
                new DefaultOverlayHandler(),
                "assembling_machine"
        );
    }
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

    public class CachedAssemblerRecipe
            extends CachedRecipe {

        public List<PositionedStack> inputs =
                new ArrayList<PositionedStack>();

        public PositionedStack output;

        public AssemblerRecipeHandler.AssemblerRecipe recipe;

        public CachedAssemblerRecipe(
                AssemblerRecipeHandler.AssemblerRecipe recipe
        ) {

            this.recipe = recipe;

            // ==========================================
            // INPUTS
            // ==========================================

            for (int i = 0; i < 9; i++) {

                ItemStack stack =
                        recipe.input[i];

                if (stack == null)
                    continue;

                int x =
                        10 + (i % 3) * 18;

                int y =
                        8 + (i / 3) * 18;

                inputs.add(

                        new PositionedStack(
                                stack,
                                x,
                                y
                        )
                );
            }

            // ==========================================
            // OUTPUT
            // ==========================================

            output =
                    new PositionedStack(
                            recipe.output,
                            98,
                            25
                    );
        }

        @Override
        public List<PositionedStack>
        getIngredients() {

            return getCycledIngredients(
                    cycleticks / 20,
                    inputs
            );
        }

        @Override
        public PositionedStack getResult() {

            return output;
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

        return "htgt6:textures/gui/Assembler.png";
    }

    // ==========================================
    // LOAD ALL RECIPES
    // ==========================================

    @Override
    public void loadCraftingRecipes(
            String outputId,
            Object... results
    ) {

        if (outputId.equals(
                getOverlayIdentifier()
        )) {

            for (AssemblerRecipeHandler.AssemblerRecipe recipe
                    : AssemblerRecipeHandler.recipes) {

                arecipes.add(

                        new CachedAssemblerRecipe(
                                recipe
                        )
                );
            }
        }

        super.loadCraftingRecipes(
                outputId,
                results
        );
    }

    // ==========================================
    // LOAD RESULT RECIPES
    // ==========================================
    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (AssemblerRecipeHandler.AssemblerRecipe recipe : AssemblerRecipeHandler.recipes) {
            if (recipe == null || recipe.output == null || recipe.output.getItem() == null) {
                continue;
            }

            if (NEIServerUtils.areStacksSameTypeCrafting(recipe.output, result)) {
                arecipes.add(new CachedAssemblerRecipe(recipe));
            }
        }
    }

    // ==========================================
    // LOAD USAGE RECIPES
    // ==========================================
    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (AssemblerRecipeHandler.AssemblerRecipe recipe : AssemblerRecipeHandler.recipes) {
            if (recipe == null || recipe.input == null) continue;

            for (ItemStack input : recipe.input) {
                // FIX: Ensure input and item are not null
                if (input == null || input.getItem() == null)
                    continue;

                if (NEIServerUtils.areStacksSameTypeCrafting(ingredient, input)) {
                    arecipes.add(new CachedAssemblerRecipe(recipe));
                    break;
                }
            }
        }
    }
    // ==========================================
    // DRAW BACKGROUND
    // ==========================================

    @Override
    public void drawBackground(
            int recipe
    ) {

        GL11.glColor4f(
                1F,
                1F,
                1F,
                1F
        );

        GuiDraw.changeTexture(
                getGuiTexture()
        );

        // ==========================================
        // DRAW ONLY MACHINE AREA
        // ==========================================

        GuiDraw.drawTexturedModalRect(
                -8,
                0,
                0,
                0,
                166,
                82
        );

        GL11.glDisable(
                GL11.GL_BLEND
        );
    }

    // ==========================================
    // DRAW EXTRAS
    // ==========================================

    @Override
    public void drawExtras(
            int recipeIndex
    ) {

        CachedAssemblerRecipe cached =
                (CachedAssemblerRecipe)
                        arecipes.get(recipeIndex);

        AssemblerRecipeHandler.AssemblerRecipe r =
                cached.recipe;

        // ==========================================
        // PROGRESS BAR
        // ==========================================

        drawProgressBar(
                70,
                23,
                176,
                0,
                24,
                15,
                48,
                0
        );

        // ==========================================
        // INFO
        // ==========================================

        GuiDraw.drawString(
                "Tier: " + r.tier,
                8,
                78,
                0xFFFFFF,
                false
        );

        GuiDraw.drawString(
                "EU/t: " + r.euPerTick,
                8,
                90,
                0x00FF00,
                false
        );

        GuiDraw.drawString(
                "Time: " + r.duration,
                8,
                102,
                0x00FFFF,
                false
        );

        GuiDraw.drawString(
                "Total: "
                        + (r.duration
                        * r.euPerTick)
                        + " EU",
                8,
                114,
                0xFFFF00,
                false
        );

        // ==========================================
        // MV
        // ==========================================

        // int mvEU =
           //     AssemblerRecipeHandler
               //         .getOverclockedEUt(
                     //           r,
                    //            2
                    //    );

      //  int mvTime =
            //    AssemblerRecipeHandler
                     //   .getOverclockedDuration(
                      //          r,
                      //          2
                      //  );

       // GuiDraw.drawString(
          //      "MV: "
            //            + mvEU
             //           + " EU/t "
             //           + mvTime
             //           + " t",
             //   80,
             //   90,
             //   0x00AAFF,
            //    false
    //    );

        // ==========================================
        // HV
        // ==========================================

     //   int hvEU =
          //      AssemblerRecipeHandler
          //              .getOverclockedEUt(
              //                  r,
               //                 3
              //          );

       // int hvTime =
         //       AssemblerRecipeHandler
                  //      .getOverclockedDuration(
                    //            r,
                     //           3
                    //    );

      //  GuiDraw.drawString(
             //   "HV: "
              //          + hvEU
              //          + " EU/t "
              //          + hvTime
              //          + " t",
            //    80,
             //   102,
            //    0xFFAA00,
            //    false
    //    );

        GL11.glColor4f(
                1F,
                1F,
                1F,
                1F
        );
    }

    // ==========================================
    // OVERLAY ID
    // ==========================================

    @Override
    public String getOverlayIdentifier() {

        return "assembling_machine";
    }
}