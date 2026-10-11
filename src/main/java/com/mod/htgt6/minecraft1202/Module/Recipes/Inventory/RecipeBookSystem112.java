package com.mod.htgt6.minecraft1202.Module.Recipes.Inventory;

import com.mod.htgt6.minecraft1202.Module.Network.Inventory.PacketPlaceRecipe112;
import com.mod.htgt6.minecraft1202.Module.Network.Inventory.PacketRecipeBookSync112;
import com.mod.htgt6.minecraft1202.Module.Network.Inventory.PacketTutorialStep112;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.MinecraftForge;

/** Server-authoritative Minecraft 1.12 recipe discovery and placement lifecycle. */
public final class RecipeBookSystem112 {
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("MC1202_Recipes");
    private static final String ROOT = "HTGT6RecipeBook112";
    private static final String DISCOVERED = "DiscoveredItems";
    private static final String UNLOCKED = "UnlockedRecipes";
    private static final String TUTORIAL_STEP = "TutorialStep112";
    private static final String TUTORIAL_VERSION = "TutorialVersion112";
    private static final int CURRENT_TUTORIAL_VERSION = 2;
    private static final Map<UUID, Progress> PROGRESS = new HashMap<UUID, Progress>();
    private static final Map<UUID, PlacementRequest> PENDING_PLACE = new ConcurrentHashMap<UUID, PlacementRequest>();
    private static final Map<UUID, Integer> PENDING_TUTORIAL = new ConcurrentHashMap<UUID, Integer>();
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        NETWORK.registerMessage(PacketRecipeBookSync112.Handler.class, PacketRecipeBookSync112.class, 0, Side.CLIENT);
        NETWORK.registerMessage(PacketPlaceRecipe112.Handler.class, PacketPlaceRecipe112.class, 1, Side.SERVER);
        NETWORK.registerMessage(PacketTutorialStep112.Handler.class, PacketTutorialStep112.class, 2, Side.SERVER);
        RecipeBookSystem112 handler = new RecipeBookSystem112();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);
    }

    private static final class PlacementRequest {
        final String key; final int window; final boolean all;
        PlacementRequest(String key,int window,boolean all){this.key=key;this.window=window;this.all=all;}
    }
    public static void requestPlace(EntityPlayerMP player, String key, int window, boolean all) {
        if (player != null && key != null && key.length() <= 2048 && window >= 0 && window <= 255)
            PENDING_PLACE.put(player.getUniqueID(), new PlacementRequest(key,window,all));
    }

    public static void requestTutorialStep(EntityPlayerMP player, int step) {
        if (player != null && step >= 0 && step <= 5)
            PENDING_TUTORIAL.put(player.getUniqueID(), Integer.valueOf(step));
    }

    @SubscribeEvent public void onOreRegistered(net.minecraftforge.oredict.OreDictionary.OreRegisterEvent event) {
        // Ore recipes retain mutable ingredient lists; recipe-list identity alone cannot detect this change.
        RecipeBookRegistry112.invalidate();
    }

    @SubscribeEvent public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        Progress progress = load(player);
        PROGRESS.put(player.getUniqueID(), progress);
        send(player, true, progress.tutorialStep, new ArrayList<String>(progress.unlocked));
    }

    @SubscribeEvent public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player == null) return;
        PROGRESS.remove(event.player.getUniqueID());
        PENDING_PLACE.remove(event.player.getUniqueID());
        PENDING_TUTORIAL.remove(event.player.getUniqueID());
    }

    @SubscribeEvent public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            Progress progress = load((EntityPlayerMP) event.player);
            PROGRESS.put(event.player.getUniqueID(), progress);
            send((EntityPlayerMP) event.player, true, progress.tutorialStep,
                    new ArrayList<String>(progress.unlocked));
        }
    }

    @SubscribeEvent public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.worldObj.isRemote
                || !(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        Progress progress = PROGRESS.get(player.getUniqueID());
        if (progress == null) {
            progress = load(player);
            PROGRESS.put(player.getUniqueID(), progress);
        }
        PlacementRequest requested = PENDING_PLACE.remove(player.getUniqueID());
        if (requested != null) place(player, progress, requested);
        Integer tutorial = PENDING_TUTORIAL.remove(player.getUniqueID());
        if (tutorial != null && tutorial.intValue() > progress.tutorialStep
                && tutorial.intValue() <= 5) {
            progress.tutorialStep = tutorial.intValue();
            save(player, progress);
        }
        if (player.ticksExisted % 10 != 0) return;
        boolean discovered = discoverInventory(player, progress);
        List<String> newlyUnlocked = refreshUnlocks(progress, discovered);
        if (discovered || !newlyUnlocked.isEmpty()) save(player, progress);
        if (!newlyUnlocked.isEmpty()) send(player, false, -1, newlyUnlocked);
    }

    private static List<String> refreshUnlocks(Progress progress, boolean discovered) {
        int revision = RecipeBookRegistry112.revision();
        if (!discovered && progress.registryRevision == revision) return java.util.Collections.emptyList();
        progress.registryRevision = revision;
        return unlockAvailable(progress);
    }

    private static boolean discoverInventory(EntityPlayer player, Progress progress) {
        boolean changed = false;
        for (ItemStack stack : player.inventory.mainInventory)
            if (stack != null) changed |= progress.discovered.add(RecipeBookRegistry112.stackKey(stack));
        for (ItemStack stack : player.inventory.armorInventory)
            if (stack != null) changed |= progress.discovered.add(RecipeBookRegistry112.stackKey(stack));
        ItemStack offhand = com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks.getOffhand(player);
        if (offhand != null) changed |= progress.discovered.add(RecipeBookRegistry112.stackKey(offhand));
        return changed;
    }

    private static List<String> unlockAvailable(Progress progress) {
        List<String> result = new ArrayList<String>();
        for (RecipeBookRegistry112.Entry entry : RecipeBookRegistry112.entries()) {
            if (progress.unlocked.contains(entry.key) || !allIngredientsDiscovered(progress, entry)) continue;
            progress.unlocked.add(entry.key);
            result.add(entry.key);
        }
        return result;
    }

    private static boolean allIngredientsDiscovered(Progress progress, RecipeBookRegistry112.Entry entry) {
        // Empty mandatory ore groups are unresolved ingredients, not shaped holes.
        // Match the placement gate before recording a permanent discovery.
        if (entry.hasUnresolvedIngredients || !entry.fits(3)) return false;
        boolean hasIngredient = false;
        for (List<ItemStack> group : entry.ingredients) {
            if (group.isEmpty()) continue;
            hasIngredient = true;
            boolean found = false;
            for (ItemStack wanted : group) if (isDiscovered(progress.discovered, wanted)) { found = true; break; }
            if (!found) return false;
        }
        return hasIngredient;
    }

    private static boolean isDiscovered(Set<String> discovered, ItemStack wanted) {
        if (wanted == null || wanted.getItem() == null) return false;
        if (wanted.getItemDamage() != 32767) return discovered.contains(RecipeBookRegistry112.stackKey(wanted));
        String key = RecipeBookRegistry112.stackKey(wanted);
        String prefix = key.substring(0, key.lastIndexOf(':') + 1);
        for (String actual : discovered) if (actual.startsWith(prefix)) return true;
        return false;
    }

    private static void send(EntityPlayerMP player, boolean replace, int tutorialStep, List<String> keys) {
        // Validate the registry once for a batch, not once for each unlocked recipe.
        Map<String, RecipeBookRegistry112.Entry> snapshot = new HashMap<String, RecipeBookRegistry112.Entry>();
        for (RecipeBookRegistry112.Entry entry : RecipeBookRegistry112.entries()) snapshot.put(entry.key, entry);
        List<ItemStack> outputs = new ArrayList<ItemStack>();
        List<String> valid = new ArrayList<String>();
        for (String key : keys) {
            RecipeBookRegistry112.Entry entry = snapshot.get(key);
            if (entry == null || entry.recipe.getRecipeOutput() == null) continue;
            valid.add(key);
            outputs.add(entry.recipe.getRecipeOutput().copy());
        }
        for (PacketRecipeBookSync112 packet : PacketRecipeBookSync112.batches(replace, tutorialStep, valid, outputs))
            NETWORK.sendTo(packet, player);
    }

    private static void place(EntityPlayerMP player, Progress progress, PlacementRequest request) {
        RecipeBookRegistry112.Entry entry = RecipeBookRegistry112.byKey(request.key);
        Container container = player.openContainer;
        if (container == null || container.windowId != request.window || !container.canInteractWith(player) || player.isDead) return;
        InventoryCrafting craftMatrix;
        int gridSize;
        if (container == player.inventoryContainer && container instanceof ContainerPlayer) {
            craftMatrix = ((ContainerPlayer) container).craftMatrix;
            gridSize = 2;
        } else if (container instanceof ContainerWorkbench) {
            craftMatrix = ((ContainerWorkbench) container).craftMatrix;
            gridSize = 3;
        } else {
            return;
        }
        if (entry == null || !progress.unlocked.contains(request.key) || !entry.fits(gridSize)) return;
        ItemStack[] oldGrid = new ItemStack[gridSize * gridSize];
        for(int i=0;i<oldGrid.length;i++) oldGrid[i]=craftMatrix.getStackInSlot(i);
        boolean matchesCurrent=entry.recipe.matches(craftMatrix,player.worldObj);
        RecipePlacement112.Plan plan=RecipePlacement112.plan(player.inventory.mainInventory,oldGrid,entry,gridSize,request.all,matchesCurrent);
        if(plan==null) return;
        // Validate custom recipe restrictions on a detached matrix before changing any live slots.
        InventoryCrafting check=new InventoryCrafting(new Container(){
            @Override public boolean canInteractWith(EntityPlayer p){return false;}
        },gridSize,gridSize);
        for(int i=0;i<plan.grid.length;i++)check.setInventorySlotContents(i,ItemStack.copyItemStack(plan.grid[i]));
        if(!entry.recipe.matches(check,player.worldObj)) return;
        for(int i=0;i<plan.inventory.length;i++)player.inventory.mainInventory[i]=plan.inventory[i];
        for(int i=0;i<plan.grid.length;i++)craftMatrix.setInventorySlotContents(i,plan.grid[i]);
        player.inventory.markDirty();
        container.onCraftMatrixChanged(craftMatrix);
        container.detectAndSendChanges();
    }

    private static Progress load(EntityPlayerMP player) {
        NBTTagCompound root = persisted(player).getCompoundTag(ROOT);
        Progress result = new Progress();
        readStrings(root.getTagList(DISCOVERED, 8), result.discovered);
        readStrings(root.getTagList(UNLOCKED, 8), result.unlocked);
        result.tutorialStep = root.getInteger(TUTORIAL_VERSION) == CURRENT_TUTORIAL_VERSION
                && root.hasKey(TUTORIAL_STEP, 3)
                ? Math.max(0, Math.min(5, root.getInteger(TUTORIAL_STEP))) : 0;
        return result;
    }

    private static void save(EntityPlayerMP player, Progress progress) {
        NBTTagCompound root = new NBTTagCompound();
        root.setTag(DISCOVERED, writeStrings(progress.discovered));
        root.setTag(UNLOCKED, writeStrings(progress.unlocked));
        root.setInteger(TUTORIAL_STEP, progress.tutorialStep);
        root.setInteger(TUTORIAL_VERSION, CURRENT_TUTORIAL_VERSION);
        persisted(player).setTag(ROOT, root);
    }

    private static NBTTagCompound persisted(EntityPlayer player) {
        NBTTagCompound entity = player.getEntityData();
        if (!entity.hasKey(EntityPlayer.PERSISTED_NBT_TAG, 10))
            entity.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        return entity.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }

    private static void readStrings(NBTTagList list, Set<String> target) {
        for (int i = 0; i < list.tagCount(); ++i) target.add(list.getStringTagAt(i));
    }

    private static NBTTagList writeStrings(Set<String> values) {
        NBTTagList list = new NBTTagList();
        for (String value : values) list.appendTag(new NBTTagString(value));
        return list;
    }

    private static final class Progress {
        final Set<String> discovered = new HashSet<String>();
        final Set<String> unlocked = new java.util.LinkedHashSet<String>();
        int tutorialStep;
        int registryRevision = -1;
    }

    private RecipeBookSystem112() { }
}
