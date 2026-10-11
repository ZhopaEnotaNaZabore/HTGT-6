package com.mod.htgt6.client.inventory112;

import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.RecipeBookRegistry112;
import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.RecipeBookSystem112;
import com.mod.htgt6.minecraft1202.Module.Network.Inventory.PacketPlaceRecipe112;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

/** Client mirror of the authoritative server recipe book. */
public final class RecipeBookClientState112 {
    private static final Set<String> UNLOCKED = new HashSet<String>();
    private static int revision;
    private static final java.util.concurrent.atomic.AtomicLong SESSION = new java.util.concurrent.atomic.AtomicLong();
    private static long appliedSession;

    /** Network events only change the token; all UI mutations stay on the client thread. */
    public static void requestSessionReset() { SESSION.incrementAndGet(); }
    public static long session() { return SESSION.get(); }
    public static void resetSessionIfNeeded() {
        long current = session();
        if (current == appliedSession) return;
        appliedSession = current;
        UNLOCKED.clear();
        revision++;
        RecipeToast112.resetForWorld();
        TutorialSystem112.resetForSession();
    }
    public static void applyForSession(long expectedSession, boolean replace, int tutorialStep,
            List<String> keys, List<ItemStack> outputs, boolean notifications) {
        resetSessionIfNeeded();
        if (expectedSession != session()) return;
        apply(replace, tutorialStep, keys, outputs, notifications);
    }

    public static void apply(boolean replace, int tutorialStep, List<String> keys, List<ItemStack> outputs) {
        apply(replace,tutorialStep,keys,outputs,!replace);
    }
    public static void apply(boolean replace, int tutorialStep, List<String> keys, List<ItemStack> outputs, boolean notifications) {
        if (replace) UNLOCKED.clear();
        UNLOCKED.addAll(keys);
        revision++;
        if (tutorialStep >= 0) TutorialSystem112.setStage(tutorialStep);
        if (notifications && !replace && outputs != null && !outputs.isEmpty()) RecipeToast112.enqueueRecipes(outputs);
    }

    static boolean isUnlocked(IRecipe recipe) {
        RecipeBookRegistry112.Entry entry = RecipeBookRegistry112.byRecipe(recipe);
        return entry != null && UNLOCKED.contains(entry.key);
    }

    static boolean canCraft(IRecipe recipe, int gridSize) {
        RecipeBookRegistry112.Entry entry = RecipeBookRegistry112.byRecipe(recipe);
        Minecraft minecraft = Minecraft.getMinecraft();
        if (entry == null || minecraft.thePlayer == null
                || !entry.fits(gridSize)) return false;
        net.minecraft.inventory.InventoryCrafting grid=null;
        net.minecraft.inventory.Container container=minecraft.thePlayer.openContainer;
        if(container instanceof net.minecraft.inventory.ContainerPlayer)grid=((net.minecraft.inventory.ContainerPlayer)container).craftMatrix;
        else if(container instanceof net.minecraft.inventory.ContainerWorkbench)grid=((net.minecraft.inventory.ContainerWorkbench)container).craftMatrix;
        if(grid==null || grid.getSizeInventory()!=gridSize*gridSize)return false;
        ItemStack[] contents=new ItemStack[grid.getSizeInventory()];
        for(int i=0;i<contents.length;i++)contents[i]=grid.getStackInSlot(i);
        return com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.RecipePlacement112.plan(
                minecraft.thePlayer.inventory.mainInventory,contents,entry,gridSize,false)!=null;
    }

    static void place(IRecipe recipe) {
        RecipeBookRegistry112.Entry entry = RecipeBookRegistry112.byRecipe(recipe);
        if (entry != null && UNLOCKED.contains(entry.key) && Minecraft.getMinecraft().thePlayer != null)
            RecipeBookSystem112.NETWORK.sendToServer(new PacketPlaceRecipe112(entry.key, Minecraft.getMinecraft().thePlayer.openContainer.windowId, net.minecraft.client.gui.GuiScreen.isShiftKeyDown()));
    }

    static int revision() { return revision; }
    private RecipeBookClientState112() { }
}
