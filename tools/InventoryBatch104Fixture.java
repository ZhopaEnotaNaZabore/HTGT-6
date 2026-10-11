package com.mod.htgt6.client.inventory112;

import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.*;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

/** Failure-path and lifecycle integration tests using the real recipe/hotbar implementations. */
public final class InventoryBatch104Fixture {
    static int checks;
    public static void main(String[] args) throws Exception {
        RecipeClient89Fixture.main(args); // Installs the headless Minecraft/player fixture.
        storage();
        session();
        discovery();
        search();
        System.out.println("PASS batch104: " + checks + " storage, session, discovery and search checks");
    }

    static void storage() throws Exception {
        Field fileField = field(SavedHotbars112.class, "storage");
        ItemStack[][] bars = (ItemStack[][]) field(SavedHotbars112.class, "HOTBARS").get(null);
        NBTTagCompound[][] missing = (NBTTagCompound[][]) field(SavedHotbars112.class, "UNRESOLVED").get(null);
        File directory = new File("build/batch104/storage"); directory.mkdirs();
        File file = new File(directory, "hotbars.dat"); fileField.set(null, file);
        ItemStack[] input = new ItemStack[9]; input[0] = new ItemStack(Items.diamond, 13);
        check(SavedHotbars112.savePreset(0, input), "successful atomic save");
        byte[] original = Files.readAllBytes(file.toPath());
        input[0].stackSize = 2;
        check(bars[0][0].stackSize == 13, "saved stacks detached from inventory");

        // Failure while serializing a modded item must not truncate the old file.
        input[0] = new ItemStack(Items.apple);
        input[0].setTagCompound(new NBTTagCompound() {
            @Override public byte getId() { throw new IllegalStateException("fixture serialization failure"); }
            @Override public NBTBase copy() { return this; }
        });
        check(!SavedHotbars112.savePreset(0, input), "serialization failure reported");
        check(Arrays.equals(original, Files.readAllBytes(file.toPath())), "serialization failure preserves file");
        check(bars[0][0].stackSize == 13, "serialization failure restores memory");

        File blockedParent = new File(directory, "not-a-directory");
        Files.write(blockedParent.toPath(), new byte[] {42});
        fileField.set(null, new File(blockedParent, "hotbars.dat"));
        missing[0][1] = new NBTTagCompound(); missing[0][1].setString("RegistryName", "absent:item");
        input[0] = new ItemStack(Items.coal);
        check(!SavedHotbars112.savePreset(0, input), "directory failure reported");
        check(bars[0][0].stackSize == 13 && missing[0][1] != null, "failed overwrite restores missing entries too");

        // Reject replacement of a nonempty directory and clean the written temporary file.
        File blockedTarget = new File(directory, "blocked-target"); blockedTarget.mkdirs();
        Files.write(new File(blockedTarget, "keep").toPath(), new byte[] {19});
        fileField.set(null, blockedTarget);
        check(!SavedHotbars112.savePreset(0, input), "replacement failure reported");
        check(new File(blockedTarget, "keep").isFile(), "failed replacement preserves existing target");
        check(directory.list((dir, name) -> name.startsWith("hotbars-") && name.endsWith(".tmp")).length == 0, "temporary files cleaned");

        fileField.set(null, file);
        Method read = SavedHotbars112.class.getDeclaredMethod("load"); read.setAccessible(true);
        Files.write(file.toPath(), new byte[] {10, 0}); read.invoke(null);
        check(bars[0][0].stackSize == 13, "truncated file does not commit partial memory");
        NBTTagCompound root = new NBTTagCompound(); NBTTagList entries = new NBTTagList();
        NBTTagCompound invalid = new NBTTagCompound(); new ItemStack(Items.stick).writeToNBT(invalid);
        entries.appendTag(invalid); root.setTag("Hotbars", entries); CompressedStreamTools.write(root, file); read.invoke(null);
        check(bars[0][0] == null, "missing address cannot become slot zero");
        check(SavedHotbars112.savePreset(0, input), "save recovers after prior failures");
        input[0] = null; read.invoke(null);
        check(bars[0][0].getItem() == Items.coal && missing[0][1] == null, "successful overwrite replaces old unresolved data");
    }

    static void session() throws Exception {
        RecipeToast112.resetForWorld();
        RecipeToast112.enqueueRecipes(Collections.singletonList(new ItemStack(Items.apple)));
        TutorialSystem112.setStage(2);
        Queue queue = (Queue) field(RecipeToast112.class, "QUEUE").get(null);
        check(queue.size() == 1, "tutorial sync retains pending recipe notification");
        RecipeToast112.showTutorial("fixture", 0, 0, "title", "description", false);
        List active = (List) field(RecipeToast112.class, "ACTIVE").get(null);
        long old = RecipeBookClientState112.session();
        RecipeBookClientState112.apply(false, -1, Collections.singletonList("old:key"), null, false);
        int revision = RecipeBookClientState112.revision();
        RecipeBookClientState112.requestSessionReset();
        RecipeBookClientState112.applyForSession(old, false, 4, Collections.singletonList("stale:key"), null, false);
        Set unlocked = (Set) field(RecipeBookClientState112.class, "UNLOCKED").get(null);
        check(unlocked.isEmpty(), "old-session delayed packet rejected");
        check(queue.isEmpty() && active.isEmpty(), "disconnect clears queued and visible toasts");
        check(!TutorialSystem112.isActive(), "disconnect stops tutorial");
        check(RecipeBookClientState112.revision() > revision, "disconnect invalidates open book");
        long current = RecipeBookClientState112.session();
        RecipeBookClientState112.applyForSession(current, true, 1, Collections.singletonList("new:key"), null, false);
        RecipeBookClientState112.resetSessionIfNeeded();
        check(unlocked.size() == 1 && unlocked.contains("new:key") && TutorialSystem112.isActive(), "new session sync accepted and not cleared next tick");
        RecipeBookClientState112.applyForSession(current, false, -1, Collections.singletonList("chunk:2"), null, false);
        check(unlocked.size() == 2 && field(TutorialSystem112.class, "stage").getInt(null) == 1, "snapshot continuation keeps tutorial and keys");
    }

    static void discovery() throws Exception {
        List recipes = CraftingManager.getInstance().getRecipeList(); recipes.clear();
        IRecipe first = new ShapelessRecipes(new ItemStack(Items.apple), Arrays.asList(new ItemStack(Items.stick)));
        recipes.add(first);
        Class<?> type = Class.forName(RecipeBookSystem112.class.getName() + "$Progress");
        Constructor<?> ctor = type.getDeclaredConstructor(); ctor.setAccessible(true); Object progress = ctor.newInstance();
        ((Set) field(type, "discovered").get(progress)).add(RecipeBookRegistry112.stackKey(new ItemStack(Items.stick)));
        Method refresh = RecipeBookSystem112.class.getDeclaredMethod("refreshUnlocks", type, boolean.class); refresh.setAccessible(true);
        check(((List) refresh.invoke(null, progress, false)).contains(RecipeBookRegistry112.byRecipe(first).key), "loaded discovery evaluated without new inventory item");
        check(((List) refresh.invoke(null, progress, false)).isEmpty(), "unchanged registry produces no repeated unlock");
        IRecipe second = new ShapelessRecipes(new ItemStack(Items.bread), Arrays.asList(new ItemStack(Items.stick))); recipes.set(0, second);
        check(((List) refresh.invoke(null, progress, false)).contains(RecipeBookRegistry112.byRecipe(second).key), "same-size replacement unlocks from existing discovery");
        IRecipe third = new ShapelessRecipes(new ItemStack(Items.coal), Arrays.asList(new ItemStack(Items.diamond))); recipes.add(third);
        check(((List) refresh.invoke(null, progress, false)).isEmpty(), "unknown ingredient still blocks unlock");
        ((Set) field(type, "discovered").get(progress)).add(RecipeBookRegistry112.stackKey(new ItemStack(Items.diamond)));
        check(((List) refresh.invoke(null, progress, true)).contains(RecipeBookRegistry112.byRecipe(third).key), "new discovery unlocks unchanged registry");
        RecipeBookRegistry112.invalidate();
        check(((List) refresh.invoke(null, progress, false)).isEmpty(), "explicit invalidation does not repeat unlocked recipes");
    }

    static void search() throws Exception {
        RecipeBook112 book = new RecipeBook112(2); book.initialize(Minecraft.getMinecraft(), null); book.toggle();
        book.mouseClicked(0, 0, 30, 18, 0);
        for (int i = 0; i < 80; i++) book.keyTyped('x', 0);
        Field query = field(RecipeBook112.class, "query");
        check(((String) query.get(book)).length() == 50, "search limited to 50 characters");
        book.keyTyped('\0', org.lwjgl.input.Keyboard.KEY_BACK);
        check(((String) query.get(book)).length() == 49, "backspace works at length limit");
        book.keyTyped('\n', 0);
        check(((String) query.get(book)).length() == 49, "control characters rejected");
        check(!book.mouseClicked(0, 0, 200, 20, 0) && !book.keyTyped('a', 0), "outside click releases search and inventory keys");
        book.mouseClicked(0, 0, 30, 18, 0); book.mouseClicked(0, 0, -15, 10, 0);
        check(!book.keyTyped('a', 0), "category click releases search focus");
        book.keyTyped('\0', org.lwjgl.input.Keyboard.KEY_ESCAPE);
        check(!book.isOpen(), "escape closes book");
    }

    static Field field(Class<?> type, String name) throws Exception { Field f = type.getDeclaredField(name); f.setAccessible(true); return f; }
    static void check(boolean value, String name) { if (!value) throw new AssertionError(name); checks++; }
}
