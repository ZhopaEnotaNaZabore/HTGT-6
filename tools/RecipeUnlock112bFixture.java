import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.*;
import cpw.mods.fml.relauncher.Side;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraftforge.oredict.*;

public final class RecipeUnlock112bFixture {
    private static int checks;
    private static Method refresh;
    private static Object progress;
    private static Set<String> discovered, unlocked;
    private static void check(boolean value, String label) {
        checks++;
        if (!value) throw new AssertionError(label);
    }
    private static RecipeBookRegistry112.Entry add(IRecipe recipe) {
        CraftingManager.getInstance().getRecipeList().add(recipe);
        return RecipeBookRegistry112.byRecipe(recipe);
    }
    private static List<String> refresh(boolean changed) throws Exception {
        return (List<String>) refresh.invoke(null, progress, changed);
    }
    public static void main(String[] args) throws Exception {
        Field side = Class.forName("cpw.mods.fml.relauncher.FMLRelaunchLog").getDeclaredField("side");
        side.setAccessible(true); side.set(null, Side.CLIENT);
        BountifulMechanicsFixture120.initialize();
        CraftingManager.getInstance().getRecipeList().clear();
        RecipeBookRegistry112.invalidate();
        Class<?> type = Class.forName(RecipeBookSystem112.class.getName() + "$Progress");
        Constructor<?> ctor = type.getDeclaredConstructor(); ctor.setAccessible(true); progress = ctor.newInstance();
        Field d = type.getDeclaredField("discovered"); d.setAccessible(true);
        Field u = type.getDeclaredField("unlocked"); u.setAccessible(true);
        discovered = (Set<String>) d.get(progress); unlocked = (Set<String>) u.get(progress);
        refresh = RecipeBookSystem112.class.getDeclaredMethod("refreshUnlocks", type, boolean.class);
        refresh.setAccessible(true);
        discovered.add(RecipeBookRegistry112.stackKey(new ItemStack(Items.stick)));
        String missing = "htgt6Recipe112bMissing";
        IRecipe shaped = new ShapedOreRecipe(new ItemStack(Items.apple), "AB", 'A', Items.stick, 'B', missing);
        IRecipe shapeless = new ShapelessOreRecipe(new ItemStack(Items.apple), Items.stick, missing);
        List<RecipeBookRegistry112.Entry> invalid = new ArrayList<RecipeBookRegistry112.Entry>();
        invalid.add(add(shaped)); invalid.add(add(shapeless));
        invalid.add(add(new ShapelessRecipes(new ItemStack(Items.apple), Arrays.asList(new ItemStack(Items.stick), null))));
        invalid.add(add(new ShapedRecipes(2, 2, new ItemStack[]{new ItemStack(Items.stick)}, new ItemStack(Items.apple))));
        check(refresh(true).isEmpty(), "unresolved/malformed recipes must not unlock from known partial inputs");
        for (RecipeBookRegistry112.Entry entry : invalid) check(!unlocked.contains(entry.key), "invalid key not persisted");
        RecipeBookRegistry112.Entry hole = add(new ShapedRecipes(2, 1,
                new ItemStack[]{new ItemStack(Items.stick), null}, new ItemStack(Items.apple)));
        check(refresh(false).equals(Collections.singletonList(hole.key)), "valid shaped hole still unlocks on registry revision");
        check(refresh(false).isEmpty(), "unchanged revision no duplicate unlock");
        OreDictionary.registerOre(missing, new ItemStack(Items.diamond));
        RecipeBookRegistry112.invalidate();
        check(refresh(false).isEmpty(), "resolved recipe waits for undiscovered ingredient");
        discovered.add(RecipeBookRegistry112.stackKey(new ItemStack(Items.diamond)));
        List<String> newly = refresh(true);
        check(newly.size() == 2, "both resolved recipes unlock after discovery");
        check(newly.contains(RecipeBookRegistry112.byRecipe(shaped).key), "shaped unlocked");
        check(newly.contains(RecipeBookRegistry112.byRecipe(shapeless).key), "shapeless unlocked");
        check(refresh(true).isEmpty(), "repeat discovery no duplicate unlock");
        check(unlocked.size() == 3, "only valid recipes stored");
        System.out.println("PASS recipe112b: " + checks + " checks, unresolved discovery, malformed dimensions, shaped holes, revision retry, resolution and duplicate guard");
    }
}
