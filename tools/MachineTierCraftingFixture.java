package com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3;

import com.google.gson.*;
import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.*;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import java.io.*;
import java.util.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Headless recipe-registry integration: fake mod items, real crafting and matching. */
public final class MachineTierCraftingFixture {
    private static final Logger LOG = LogManager.getLogger("MachineTierFixture");
    private static final GeneratedRecipeSettings SETTINGS = GeneratedRecipeSettings.DEFAULT;
    private static final String MULTI = "gregtech:gt.multitileentity";
    private static String token(int meta) { return "<" + MULTI + ":" + meta + ">"; }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        World world = CrusherOreImportFixture.initializeGT6();
        JsonObject manifest;
        try (Reader in = new InputStreamReader(new FileInputStream("docs/audits/machine-tier-recipes.json"),"UTF-8")) {
            manifest = new JsonParser().parse(in).getAsJsonObject();
        }
        JsonArray changed = manifest.getAsJsonArray("changed"), preserved = manifest.getAsJsonArray("preserved");
        registerToken(token(0));
        for (JsonElement entry : changed)
            for (String[] row : grid(entry.getAsJsonObject()))
                for (String ingredient : row) registerToken(ingredient);
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        recipes.clear();
        for (JsonElement value : changed) {
            ItemStack out = item(token(value.getAsJsonObject().get("output").getAsInt()));
            recipes.add(new ShapedOreRecipe(out,"D",'D',Items.stick));
            recipes.add(new ShapedOreRecipe(out,"DD",'D',Items.stick));
        }
        List<IRecipe> keep = new ArrayList<IRecipe>();
        for (JsonElement value : preserved) {
            IRecipe recipe = new ShapedOreRecipe(item(token(value.getAsJsonObject().get("output").getAsInt())),"DDD",'D',Items.stick);
            keep.add(recipe); recipes.add(recipe);
        }
        for (int id=20081; id<=20084; id++) {
            IRecipe centrifuge = new ShapedOreRecipe(item(token(id)),"SSS",'S',Items.stick);
            keep.add(centrifuge); recipes.add(centrifuge);
        }
        IRecipe t5 = new ShapedOreRecipe(item(token(20626)),"S",'S',Items.stick);
        recipes.add(t5);
        MissingMachineTierRecipes.register(SETTINGS,LOG);
        require(recipes.size() == changed.size()+keep.size()+1,"replace duplicates, preserve other recipes");
        require(recipes.containsAll(keep) && recipes.contains(t5),"authored recipes and T6 must remain");
        int tested = 0;
        for (JsonElement value : changed) {
            JsonObject entry = value.getAsJsonObject(); int output = entry.get("output").getAsInt();
            String[][] grid = grid(entry);
            InventoryCrafting inventory = new InventoryCrafting(new Container() {
                @Override public boolean canInteractWith(EntityPlayer player) { return true; }
            },3,3);
            for (int row=0;row<grid.length;row++) for (int col=0;col<grid[row].length;col++)
                inventory.setInventorySlotContents(row*3+col,ingredient(grid[row][col]));
            ItemStack crafted = CraftingManager.getInstance().findMatchingRecipe(inventory,world);
            require(crafted != null && crafted.isItemEqual(item(token(output))),"grid collision or missing recipe: " + output);
            IRecipe matching = null; int count = 0;
            for (IRecipe recipe : recipes) {
                if (recipe.getRecipeOutput().isItemEqual(item(token(output)))) { matching=recipe; count++; }
            }
            require(count==1,"exactly one recipe per changed machine: " + output);
            // A cheaper hull must never satisfy an upgraded machine's recipe.
            for (int slot=0;slot<9;slot++) {
                ItemStack part = inventory.getStackInSlot(slot);
                if (part == null) continue;
                String name = Item.itemRegistry.getNameForObject(part.getItem());
                if (name != null && (name.toLowerCase(java.util.Locale.ENGLISH).contains("hull"))) {
                    inventory.setInventorySlotContents(slot,new ItemStack(Items.stick));
                    require(!matching.matches(inventory,world),"incorrect hull accepted: " + output);
                    break;
                }
            }
            tested++;
        }
        int size = recipes.size();
        MissingMachineTierRecipes.register(SETTINGS,LOG);
        require(recipes.size()==size && recipes.containsAll(keep),"repeat registration remains safe");
        IRecipe before = null;
        for (IRecipe recipe : recipes) if (recipe.getRecipeOutput().getItemDamage()==20232) before=recipe;
        require(before != null,"press recipe exists");
        require(!GeneratedCrafting3x3.replaceTokens("missing ingredient regression",token(20232),
                new String[][] {{"<absent_mod:missing_part>"}},SETTINGS,LOG),"missing ingredient must fail registration");
        require(recipes.contains(before),"failed replacement must keep working recipe");
        System.out.println("Machine tier crafting PASS: " + tested + " grids resolve unique outputs; " + keep.size()
                + " authored recipes and T6 preserved; safe replacement and idempotence");
    }
    private static String[][] grid(JsonObject entry) {
        JsonArray array = entry.getAsJsonArray("grid"); String[][] result = new String[array.size()][];
        for (int r=0;r<array.size();r++) {
            JsonArray row=array.get(r).getAsJsonArray(); result[r]=new String[row.size()];
            for (int c=0;c<row.size();c++) result[r][c]=row.get(c).isJsonNull()?null:row.get(c).getAsString();
        }
        return result;
    }
    private static void registerToken(String token) {
        if (token == null || "null".equals(token)) return;
        String id=token.substring(1,token.indexOf('>')); String[] fields=id.split(":");
        if (fields[0].equals("ore")) {
            if (OreDictionary.getOres(fields[1]).isEmpty()) OreDictionary.registerOre(fields[1],new ItemStack(Items.diamond));
            return;
        }
        if (GameRegistry.findItem(fields[0],fields[1]) != null) return;
        Loader.instance().activeModContainer().getMetadata().modId=fields[0];
        GameRegistry.registerItem(new Item().setHasSubtypes(true),fields[1]);
    }
    private static ItemStack item(String token) { return GeneratedValueResolver.item(token,"fixture",SETTINGS,LOG); }
    private static ItemStack ingredient(String token) {
        if (token == null || "null".equals(token)) return null;
        if (token.startsWith("<ore:")) return OreDictionary.getOres(token.substring(5,token.length()-1)).get(0).copy();
        return item(token);
    }
    private static void require(boolean ok,String message) { if (!ok) throw new AssertionError(message); }
}
