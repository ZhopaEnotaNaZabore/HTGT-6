package com.mod.htgt6.minecraft1202.Module.Recipes.Inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/** Stable recipe descriptors shared by server progress and the 1.12 client UI. */
public final class RecipeBookRegistry112 {
    public static final class Entry {
        public final String key;
        public final IRecipe recipe;
        public final List<List<ItemStack>> ingredients;
        public final int width;
        public final int height;
        public final boolean shapeless;
        public final boolean hasUnresolvedIngredients;
        public boolean fits(int size) {
            if (size < 1 || size > 3) return false;
            return shapeless ? !ingredients.isEmpty() && ingredients.size() <= size * size
                    : width > 0 && height > 0 && width <= size && height <= size
                    && ingredients.size() == width * height;
        }
        public int targetSlot(int index, int size) {
            if (shapeless) return index;
            return ((size - height) / 2 + index / width) * size + (size - width) / 2 + index % width;
        }

        private Entry(String key, IRecipe recipe, List<List<ItemStack>> ingredients, int width, int height,
                boolean hasUnresolvedIngredients) {
            this.key = key;
            this.recipe = recipe;
            this.ingredients = ingredients;
            this.width = width;
            this.height = height;
            this.hasUnresolvedIngredients = hasUnresolvedIngredients;
            this.shapeless = recipe instanceof ShapelessRecipes || recipe instanceof ShapelessOreRecipe
                    || recipe.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT).contains("shapeless");
        }
    }

    private static final Map<String, Entry> BY_KEY = new LinkedHashMap<String, Entry>();
    private static final Map<IRecipe, Entry> BY_RECIPE = new IdentityHashMap<IRecipe, Entry>();
    private static int revision;
    public static synchronized int revision() { rebuildIfNeeded(); return revision; }
    private static Object[] knownRecipes = new Object[0];
    private static final Map<IRecipe, Integer> POSITIONS = new IdentityHashMap<IRecipe, Integer>();
    public static synchronized void invalidate() { revision++; knownRecipes = new Object[0]; BY_KEY.clear(); BY_RECIPE.clear(); POSITIONS.clear(); }

    public static synchronized List<Entry> entries() {
        rebuildIfNeeded();
        return new ArrayList<Entry>(BY_KEY.values());
    }

    public static synchronized Entry byKey(String key) {
        rebuildIfNeeded();
        return BY_KEY.get(key);
    }

    public static synchronized Entry byRecipe(IRecipe recipe) {
        List list = CraftingManager.getInstance().getRecipeList();
        Integer position = POSITIONS.get(recipe);
        if (knownRecipes.length != list.size() || position == null || list.get(position) != recipe) rebuildIfNeeded();
        return BY_RECIPE.get(recipe);
    }

    private static void rebuildIfNeeded() {
        List list = CraftingManager.getInstance().getRecipeList();
        boolean unchanged = knownRecipes.length == list.size();
        for (int i = 0; unchanged && i < knownRecipes.length; i++) unchanged = knownRecipes[i] == list.get(i);
        if (unchanged) return;
        knownRecipes = list.toArray();
        revision++;
        POSITIONS.clear();
        BY_KEY.clear();
        BY_RECIPE.clear();
        Map<String, Integer> duplicates = new LinkedHashMap<String, Integer>();
        for (int position = 0; position < knownRecipes.length; position++) {
            Object object = knownRecipes[position];
            if (!(object instanceof IRecipe)) continue;
            IRecipe recipe = (IRecipe) object;
            POSITIONS.put(recipe, position);
            ItemStack output = recipe.getRecipeOutput();
            if (output == null || output.getItem() == null) continue;
            RecipeInput input = extract(recipe);
            if (input.groups.isEmpty()) continue;
            String fingerprint = recipe.getClass().getName() + "|" + stackKey(output) + "|" + input.signature;
            String base = recipe.getClass().getName() + "|" + stackKey(output) + "|"
                    + Integer.toHexString(fingerprint.hashCode());
            Integer duplicate = duplicates.get(base);
            int ordinal = duplicate == null ? 0 : duplicate.intValue() + 1;
            duplicates.put(base, Integer.valueOf(ordinal));
            String key = base + "#" + ordinal;
            Entry entry = new Entry(key, recipe, input.groups, input.width, input.height, input.hasUnresolvedIngredients);
            BY_KEY.put(key, entry);
            BY_RECIPE.put(recipe, entry);
        }
    }

    private static RecipeInput extract(IRecipe recipe) {
        Object[] values = null;
        int width = 0, height = 0;
        if (recipe instanceof ShapedRecipes) {
            ShapedRecipes shaped = (ShapedRecipes) recipe;
            values = shaped.recipeItems;
            width = shaped.recipeWidth;
            height = shaped.recipeHeight;
        } else if (recipe instanceof ShapedOreRecipe) {
            values = ((ShapedOreRecipe) recipe).getInput();
            width = oreRecipeDimension(recipe, "width");
            height = oreRecipeDimension(recipe, "height");
        } else if (recipe instanceof ShapelessRecipes) {
            List items = ((ShapelessRecipes) recipe).recipeItems;
            values = items.toArray();
            width = Math.min(3, values.length);
            height = values.length == 0 ? 0 : (values.length + width - 1) / width;
        } else if (recipe instanceof ShapelessOreRecipe) {
            values = ((ShapelessOreRecipe) recipe).getInput().toArray();
            width = Math.min(3, values.length);
            height = values.length == 0 ? 0 : (values.length + width - 1) / width;
        } else {
            try {
                java.lang.reflect.Method method = recipe.getClass().getMethod("getInput");
                Object input = method.invoke(recipe);
                if (input instanceof Object[]) values = (Object[]) input;
                else if (input instanceof java.util.Collection) values = ((java.util.Collection) input).toArray();
                try { width = ((Number) recipe.getClass().getMethod("getRecipeWidth").invoke(recipe)).intValue(); }
                catch (Exception ignored) {
                    int length = values == null ? 0 : values.length;
                    width = recipe.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT).contains("shapeless")
                            ? Math.min(3, length) : Math.min(2, length);
                }
                try { height = ((Number) recipe.getClass().getMethod("getRecipeHeight").invoke(recipe)).intValue(); }
                catch (Exception ignored) { height = values == null ? 0 : (values.length + Math.max(1, width) - 1) / Math.max(1, width); }
            } catch (Exception ignored) { }
        }
        if (values == null) return new RecipeInput(Collections.<List<ItemStack>>emptyList(), 0, 0, "", true);
        List<List<ItemStack>> groups = new ArrayList<List<ItemStack>>();
        StringBuilder signature = new StringBuilder();
        boolean hasUnresolvedIngredients = false;
        boolean shapeless = recipe instanceof ShapelessRecipes || recipe instanceof ShapelessOreRecipe
                || recipe.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT).contains("shapeless");
        for (Object value : values) {
            List<ItemStack> choices = choices(value);
            // Only a null shaped input is a deliberate hole. An empty ore list
            // is an ingredient with no matching item, never a free recipe slot.
            if (choices.isEmpty() && (value != null || shapeless)) hasUnresolvedIngredients = true;
            groups.add(choices);
            signature.append('[');
            for (ItemStack choice : choices) signature.append(stackKey(choice)).append(',');
            signature.append(']');
        }
        return new RecipeInput(groups, width, height, signature.toString(), hasUnresolvedIngredients);
    }

    private static int oreRecipeDimension(IRecipe recipe, String fieldName) {
        try {
            java.lang.reflect.Field field = ShapedOreRecipe.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(recipe);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static List<ItemStack> choices(Object value) {
        if (value instanceof ItemStack) return Collections.singletonList(((ItemStack) value).copy());
        if (value instanceof java.util.Collection) {
            List<ItemStack> result = new ArrayList<ItemStack>();
            for (Object choice : (java.util.Collection) value)
                if (choice instanceof ItemStack) result.add(((ItemStack) choice).copy());
            return result;
        }
        return Collections.emptyList();
    }

    public static String stackKey(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return "empty";
        Object name = Item.itemRegistry.getNameForObject(stack.getItem());
        return String.valueOf(name) + ":" + stack.getItemDamage();
    }

    public static boolean matches(ItemStack actual, ItemStack wanted) {
        return actual != null && wanted != null && actual.getItem() == wanted.getItem()
                && (wanted.getItemDamage() == 32767 || actual.getItemDamage() == wanted.getItemDamage())
                && (!wanted.hasTagCompound() || ItemStack.areItemStackTagsEqual(actual, wanted));
    }

    private static final class RecipeInput {
        final List<List<ItemStack>> groups;
        final int width, height;
        final String signature;
        final boolean hasUnresolvedIngredients;
        RecipeInput(List<List<ItemStack>> groups, int width, int height, String signature, boolean hasUnresolvedIngredients) {
            this.groups = groups; this.width = width; this.height = height; this.signature = signature;
            this.hasUnresolvedIngredients = hasUnresolvedIngredients;
        }
    }

    private RecipeBookRegistry112() { }
}
