package com.mod.htgt6.common.handler.recipe;

import com.mod.htgt6.common.handler.recipe.assembler.AssemblerRecipeHandler;
import com.mod.htgt6.common.handler.recipe.compressor.CompressorRecipeHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class RecipeConfigLoader {

    // ==========================================
    // LOAD RECIPES
    // ==========================================
    public static void loadCompressorRecipes(File file) {
        try {

            if (!file.exists()) {
                createDefaultCompressor(file);
            }

            BufferedReader reader =
                    new BufferedReader(new FileReader(file));

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                parseCompressorRecipe(line);
            }

            reader.close();

            System.out.println(
                    "[HTGT6] Compressor recipes loaded."
            );

        } catch (Exception e) {

            System.out.println(
                    "[HTGT6] Failed loading compressor recipes."
            );

            e.printStackTrace();
        }
    }
    public static void loadAssemblerRecipes(
            File file
    ) {

        try {

            // ==========================================
            // CREATE DEFAULT
            // ==========================================

            if (!file.exists()) {

                createDefault(file);
            }

            BufferedReader reader =
                    new BufferedReader(
                            new FileReader(file)
                    );

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                // COMMENT / EMPTY
                if (line.isEmpty()
                        || line.startsWith("#")) {

                    continue;
                }

                parseRecipe(line);
            }

            reader.close();

            System.out.println(
                    "[HTGT6] Assembler recipes loaded."
            );

        } catch (Exception e) {

            System.out.println(
                    "[HTGT6] Failed loading assembler recipes."
            );

            e.printStackTrace();
        }
    }

    // ==========================================
    // PARSE RECIPE
    // ==========================================
    private static void parseCompressorRecipe(String line) {

        try {

            String[] split = line.split(";");

            ItemStack output =
                    parseStack(split[0]);

            String[] inputStrings =
                    split[1].split(",");

            ItemStack[] inputs =
                    new ItemStack[3];

            for (int i = 0; i < inputStrings.length && i < 3; i++) {

                if (!inputStrings[i].trim().equalsIgnoreCase("null")) {

                    inputs[i] =
                            parseStack(inputStrings[i].trim());
                }
            }

            int duration =
                    Integer.parseInt(split[2].trim());

            int eut =
                    Integer.parseInt(split[3].trim());

            int tier =
                    Integer.parseInt(split[4].trim());

            CompressorRecipeHandler.addRecipe(
                    inputs,
                    output,
                    duration,
                    eut,
                    tier
            );

        } catch (Exception e) {

            System.out.println(
                    "[HTGT6] Failed compressor recipe: "
                            + line
            );

            e.printStackTrace();
        }
    }
    private static void parseRecipe(
            String line
    ) {

        try {


            String[] split =
                    line.split(";");

            if (split.length < 5) {

                System.out.println(
                        "[HTGT6] Invalid recipe line: "
                                + line
                );

                return;
            }

            // ==========================================
            // OUTPUT
            // ==========================================

            ItemStack output =
                    parseStack(split[0]);

            // ==========================================
            // INPUTS
            // ==========================================

            String[] inputStrings =
                    split[1].split(",");

            ItemStack[] inputs =
                    new ItemStack[9];

            for (int i = 0;
                 i < inputStrings.length
                         && i < 9;
                 i++) {

                String in =
                        inputStrings[i].trim();

                if (!in.equalsIgnoreCase("null")
                        && !in.isEmpty()) {

                    inputs[i] =
                            parseStack(in);
                }
            }

            // ==========================================
            // STATS
            // ==========================================

            int duration =
                    Integer.parseInt(
                            split[2].trim()
                    );

            int eut =
                    Integer.parseInt(
                            split[3].trim()
                    );

            int tier =
                    Integer.parseInt(
                            split[4].trim()
                    );

            // ==========================================
            // REGISTER
            // ==========================================

            AssemblerRecipeHandler.addRecipe(
                    inputs,
                    output,
                    duration,
                    eut,
                    tier
            );

            System.out.println(
                    "[HTGT6] Loaded recipe for: "
                            + output.getDisplayName()
            );

        } catch (Exception e) {

            System.out.println(
                    "[HTGT6] Failed recipe: "
                            + line
            );

            e.printStackTrace();
        }
    }

    // ==========================================
    // PARSE STACK
    // FORMAT:
    // modid:item*amount
    // ==========================================

    private static ItemStack parseStack(String s) {
        s = s.trim();
        String[] amountSplit = s.split("\\*");
        String itemID = amountSplit[0];
        int amount = 1;

        if (amountSplit.length > 1) {
            amount = Integer.parseInt(amountSplit[1]);
        }

        String[] itemSplit = itemID.split(":");
        if (itemSplit.length < 2) {
            throw new RuntimeException("Invalid item id: " + s);
        }

        String modid = itemSplit[0];
        String name = itemSplit[1];

        // Try to find it as an Item
        Item item = GameRegistry.findItem(modid, name);

        // FIX: Fallback to Block if Item returned null
        if (item == null) {
            net.minecraft.block.Block block = GameRegistry.findBlock(modid, name);
            if (block != null) {
                item = Item.getItemFromBlock(block);
            }
        }

        if (item == null) {
            // Instead of crashing the whole game, return null or throw a handled error
            System.out.println("[HTGT6] ERROR: Item/Block not found in Registry: " + s);
            return null;
        }

        return new ItemStack(item, amount);
    }
    // ==========================================
    // CREATE DEFAULT CONFIG
    // ==========================================

    private static void createDefault(
            File file
    ) throws IOException {

        file.getParentFile().mkdirs();

        PrintWriter pw =
                new PrintWriter(file);

        pw.println(
                "# ====================================="
        );

        pw.println(
                "# HTGT6 Assembler Recipes"
        );

        pw.println(
                "# FORMAT:"
        );

        pw.println(
                "# output;inputs;duration;eut;tier"
        );

        pw.println(
                "# ====================================="
        );

        pw.println(
                "# INPUT FORMAT:"
        );

        pw.println(
                "# modid:item*amount"
        );

        pw.println(
                "# ====================================="
        );

        pw.println(
                "# EXAMPLE:"
        );

        pw.println(
                "minecraft:torch*16;"
                        + "minecraft:coal*1,"
                        + "minecraft:stick*1;"
                        + "100;"
                        + "8;"
                        + "1"
        );

        pw.close();

        System.out.println(
                "[HTGT6] Created default assembler recipe config."
        );
    }
    private static void createDefaultCompressor(
            File file
    ) throws IOException {

        file.getParentFile().mkdirs();

        PrintWriter pw =
                new PrintWriter(file);

        pw.println("# =====================================");
        pw.println("# HTGT6 Compressor Recipes");
        pw.println("# FORMAT:");
        pw.println("# output;inputs;duration;eut;tier");
        pw.println("# =====================================");
        pw.println("# INPUT FORMAT:");
        pw.println("# modid:item*amount");
        pw.println("# =====================================");

        pw.println(
                "minecraft:diamond*1;"
                        + "minecraft:coal*1,minecraft:coal*1,minecraft:coal*1;"
                        + "200;"
                        + "32;"
                        + "2"
        );

        pw.close();
    }
}