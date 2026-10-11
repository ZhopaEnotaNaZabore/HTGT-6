import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.*;
import cpw.mods.fml.relauncher.Side;
import java.lang.reflect.Field;
import java.util.*;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.world.World;
import net.minecraftforge.oredict.*;

public final class Recipe111bFixture {
    private static int checks;
    private static RecipeBookRegistry112.Entry entry(IRecipe recipe) {
        CraftingManager.getInstance().getRecipeList().add(recipe);
        return RecipeBookRegistry112.byRecipe(recipe);
    }
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static void unavailable(RecipeBookRegistry112.Entry entry, String description) {
        ItemStack[] inventory = {new ItemStack(Items.stick, 32)};
        ItemStack[] grid = new ItemStack[9];
        grid[0] = new ItemStack(Items.diamond, 2);
        check(RecipePlacement112.chooseSources(inventory, entry, 1) == null, description + " availability");
        for (boolean all : new boolean[]{false, true})
            check(RecipePlacement112.plan(inventory, new ItemStack[9], entry, 3, all) == null,
                    description + " placement " + all);
        check(inventory[0].stackSize == 32 && grid[0].stackSize == 2, "inputs retained");
    }
    public static void main(String[] args) throws Exception {
        Field side = Class.forName("cpw.mods.fml.relauncher.FMLRelaunchLog").getDeclaredField("side");
        side.setAccessible(true); side.set(null, Side.CLIENT);
        World world = BountifulMechanicsFixture120.initialize();
        String missing = "htgt6Recipe111bMissingIngredient";
        unavailable(entry(new ShapedOreRecipe(new ItemStack(Items.apple), "AB", 'A', Items.stick,
                'B', missing)), "empty shaped ore group");
        unavailable(entry(new ShapelessOreRecipe(new ItemStack(Items.apple), Items.stick, missing)),
                "empty shapeless ore group");
        unavailable(entry(new ShapelessRecipes(new ItemStack(Items.apple), Arrays.asList(new ItemStack(Items.stick), null))),
                "null shapeless input");

        for (int width = 1; width <= 3; width++) for (int height = 1; height <= 3; height++) {
            for (int delta : new int[]{-1, 1}) {
                int count = width * height + delta;
                if (count <= 0) continue;
                ItemStack[] inputs = new ItemStack[count];
                Arrays.fill(inputs, new ItemStack(Items.stick));
                RecipeBookRegistry112.Entry malformed = entry(new ShapedRecipes(width, height, inputs, new ItemStack(Items.apple)));
                check(!malformed.fits(3), "malformed dimensions " + width + "x" + height + "/" + count);
                check(RecipePlacement112.plan(new ItemStack[]{new ItemStack(Items.stick,64)}, new ItemStack[9], malformed, 3, false) == null,
                        "malformed plan refused");
            }
            ItemStack[] inputs = new ItemStack[width * height];
            Arrays.fill(inputs, new ItemStack(Items.stick));
            if (inputs.length > 1) inputs[1] = null;
            RecipeBookRegistry112.Entry valid = entry(new ShapedRecipes(width, height, inputs, new ItemStack(Items.apple)));
            for (int size = 1; size <= 3; size++) {
                boolean fits = width <= size && height <= size;
                check(valid.fits(size) == fits, "valid shape fit");
                RecipePlacement112.Plan plan = RecipePlacement112.plan(new ItemStack[]{new ItemStack(Items.stick,64)},
                        new ItemStack[size * size], valid, size, false);
                check((plan != null) == fits, "valid shape placement");
                if (plan != null) {
                    int occupied = 0;
                    for (ItemStack stack : plan.grid) if (stack != null) occupied++;
                    check(occupied == inputs.length - (inputs.length > 1 ? 1 : 0), "holes preserved");
                }
            }
        }
        OreDictionary.registerOre(missing, new ItemStack(Items.diamond));
        RecipeBookRegistry112.invalidate();
        RecipeBookRegistry112.Entry resolved = entry(new ShapelessOreRecipe(new ItemStack(Items.apple), Items.stick, missing));
        check(RecipePlacement112.chooseSources(new ItemStack[]{new ItemStack(Items.stick), new ItemStack(Items.diamond)}, resolved, 1) != null,
                "resolved ore accepted after registry refresh");
        check(!resolved.fits(0) && !resolved.fits(4), "unsupported grids rejected");
        System.out.println("PASS recipe111b: " + checks + " checks, unavailable ore ingredients, malformed dimensions, valid shapes/holes, resolved ore");
    }
}
