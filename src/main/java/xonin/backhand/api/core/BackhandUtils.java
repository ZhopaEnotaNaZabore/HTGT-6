package xonin.backhand.api.core;

import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraftforge.common.util.FakePlayer;

/**
 * Compatibility facade for binaries compiled against Backhand. All state and
 * behavior delegate to HTGT6's native 1.7 off-hand module.
 */
public final class BackhandUtils {
    public static final List<Class<? extends Item>> offhandPriorityItems =
            new ArrayList<Class<? extends Item>>();
    public static final List<Class<? extends Item>> deprioritizedMainhand =
            new ArrayList<Class<? extends Item>>();

    public BackhandUtils() { }
    public static void setPlayerOffhandItem(EntityPlayer player, ItemStack stack) {
        OffhandHooks.setOffhand(player, stack);
    }
    public static ItemStack getOffhandItem(EntityPlayer player) { return OffhandHooks.getOffhand(player); }
    public static void useOffhandItem(EntityPlayer player, Runnable action) {
        useOffhandItem(player, true, action);
    }
    public static void useOffhandItem(EntityPlayer player, boolean synchronize, Runnable action) {
        if (player == null || action == null) return;
        int previous = swapToOffhand(player);
        try { action.run(); } finally { swapBack(player, previous); }
    }
    public static boolean useOffhandItem(EntityPlayer player, BooleanSupplier action) {
        return useOffhandItem(player, true, action);
    }
    public static boolean useOffhandItem(EntityPlayer player, boolean synchronize, BooleanSupplier action) {
        if (player == null || action == null) return false;
        int previous = swapToOffhand(player);
        try { return action.getAsBoolean(); } finally { swapBack(player, previous); }
    }
    public static boolean isUsingOffhand(EntityPlayer player) {
        return player != null && player.inventory.currentItem == getOffhandSlot(player);
    }
    public static int getOffhandSlot(EntityPlayer player) { return OffhandHooks.OFFHAND_SLOT; }
    @SafeVarargs public static void addOffhandPriorityItem(Class<? extends Item>... items) {
        if (items != null) offhandPriorityItems.addAll(Arrays.asList(items));
    }
    @SafeVarargs public static void addDeprioritizedMainhandItem(Class<? extends Item>... items) {
        if (items != null) deprioritizedMainhand.addAll(Arrays.asList(items));
    }
    public static boolean isValidPlayer(Entity entity) {
        if (!(entity instanceof EntityPlayer) || entity instanceof FakePlayer) return false;
        return !(entity instanceof EntityPlayerMP)
                || ((EntityPlayerMP) entity).playerNetServerHandler != null;
    }
    public static boolean isItemTool(Item item) {
        if (item instanceof ItemTool || item instanceof ItemSword || item instanceof ItemHoe) return true;
        for (Class<?> type = item == null ? null : item.getClass(); type != null; type = type.getSuperclass())
            if ("tconstruct.library.tools.HarvestTool".equals(type.getName())) return true;
        return false;
    }
    public static int swapToOffhand(EntityPlayer player) {
        int previous = player.inventory.currentItem;
        player.inventory.currentItem = getOffhandSlot(player);
        return previous;
    }
    public static void swapBack(EntityPlayer player, int previous) {
        if (player != null) player.inventory.currentItem = previous;
    }
}
