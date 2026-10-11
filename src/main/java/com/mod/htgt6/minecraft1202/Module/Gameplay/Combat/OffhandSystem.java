package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketOffhandSync;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketShieldState;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketOffhandCrossbowState;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketSwapOffhand;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketUseOffhand;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSword;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Server-authoritative off-hand lifecycle, interaction routing and synchronization. */
public final class OffhandSystem {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-Offhand");
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("MC1202_Offhand");
    private static final int MAX_PENDING_SWAPS = 128;
    private static final ConcurrentHashMap<UUID, java.util.concurrent.ArrayBlockingQueue<PendingSwap>> PENDING_SWAPS =
            new ConcurrentHashMap<UUID, java.util.concurrent.ArrayBlockingQueue<PendingSwap>>();
    private static final Map<UUID, Integer> LAST_SWAPS = new ConcurrentHashMap<UUID, Integer>();
    private static final ConcurrentHashMap<UUID, java.util.concurrent.ArrayBlockingQueue<Boolean>> PENDING_USE = new ConcurrentHashMap<UUID, java.util.concurrent.ArrayBlockingQueue<Boolean>>();
    private static final Map<UUID, Long> BLOCKING_SINCE = new ConcurrentHashMap<UUID, Long>();
    private static final Map<UUID, Long> SHIELD_DISABLED_UNTIL = new ConcurrentHashMap<UUID, Long>();
    private static final Map<Integer, Boolean> CLIENT_SHIELD_BLOCKING = new ConcurrentHashMap<Integer, Boolean>();
    private static final Map<UUID,java.util.Set<EntityPlayerMP>> CROSSBOW_WATCHERS=new java.util.HashMap<UUID,java.util.Set<EntityPlayerMP>>();
    private static final Map<UUID,PacketOffhandCrossbowState> LAST_CROSSBOW_STATE=new java.util.HashMap<UUID,PacketOffhandCrossbowState>();
    private static boolean initialized;

    public static void init() {
        if (initialized) return;
        initialized = true;
        NETWORK.registerMessage(PacketSwapOffhand.Handler.class, PacketSwapOffhand.class, 0, Side.SERVER);
        NETWORK.registerMessage(PacketOffhandSync.Handler.class, PacketOffhandSync.class, 1, Side.CLIENT);
        NETWORK.registerMessage(PacketUseOffhand.Handler.class, PacketUseOffhand.class, 2, Side.SERVER);
        NETWORK.registerMessage(PacketShieldState.Handler.class, PacketShieldState.class, 3, Side.CLIENT);
        NETWORK.registerMessage(PacketOffhandCrossbowState.Handler.class,PacketOffhandCrossbowState.class,4,Side.CLIENT);
        OffhandSystem handler = new OffhandSystem();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);
    }

    public static void sync(EntityPlayerMP player) {
        sync(player, player.inventory.currentItem);
    }

    private static void sync(EntityPlayerMP player, int selected) {
        ItemStack main = selected >= 0 && selected < 9 ? player.inventory.mainInventory[selected] : null;
        NETWORK.sendTo(new PacketOffhandSync(OffhandHooks.getOffhand(player), getShieldCooldownTicks(player),
                selected, main), player);
    }

    /** Called on Netty's thread; inventory mutation is deferred to the player tick. */
    public static void requestSwap(EntityPlayerMP player, int sequence, int selectedSlot) {
        if (player != null && selectedSlot >= 0 && selectedSlot < 9) {
            java.util.concurrent.ArrayBlockingQueue<PendingSwap> queue = PENDING_SWAPS.get(player.getUniqueID());
            if (queue == null) {
                java.util.concurrent.ArrayBlockingQueue<PendingSwap> fresh = new java.util.concurrent.ArrayBlockingQueue<PendingSwap>(MAX_PENDING_SWAPS);
                queue = PENDING_SWAPS.putIfAbsent(player.getUniqueID(), fresh);
                if (queue == null) queue = fresh;
            }
            // No client-side swap prediction: rejected excess requests cannot desync item stacks.
            queue.offer(new PendingSwap(sequence, selectedSlot));
        }
    }

    /** Called on Netty's thread; the requested state is validated on the server tick. */
    public static void requestUse(EntityPlayerMP player, boolean using) {
        if(player==null)return;
        java.util.concurrent.ArrayBlockingQueue<Boolean> queue=PENDING_USE.get(player.getUniqueID());
        if(queue==null){
            java.util.concurrent.ArrayBlockingQueue<Boolean> fresh=new java.util.concurrent.ArrayBlockingQueue<Boolean>(128);
            queue=PENDING_USE.putIfAbsent(player.getUniqueID(),fresh);if(queue==null)queue=fresh;
        }
        // Preserve press/release edges in one server tick; on overflow always fail closed.
        if(!queue.offer(Boolean.valueOf(using))){queue.clear();queue.offer(Boolean.FALSE);}
    }

    public static boolean isShieldBlocking(EntityPlayer player) {
        return player != null && BLOCKING_SINCE.containsKey(player.getUniqueID())
                && isShield(OffhandHooks.getOffhand(player)) && !isShieldDisabled(player);
    }

    public static int getShieldUseTicks(EntityPlayer player) {
        if (player == null) return 0;
        Long since = BLOCKING_SINCE.get(player.getUniqueID());
        return since == null ? 0 : (int) Math.max(0L, player.worldObj.getTotalWorldTime() - since.longValue());
    }

    public static void stopBlocking(EntityPlayer player) {
        if (player != null && BLOCKING_SINCE.remove(player.getUniqueID()) != null) broadcastShieldState(player, false);
    }

    public static boolean isShieldDisabled(EntityPlayer player) {
        if (player == null || player.worldObj == null) return false;
        Long until = SHIELD_DISABLED_UNTIL.get(player.getUniqueID());
        if (until == null) return false;
        if (until.longValue() > player.worldObj.getTotalWorldTime()) return true;
        SHIELD_DISABLED_UNTIL.remove(player.getUniqueID());
        return false;
    }

    public static int getShieldCooldownTicks(EntityPlayer player) {
        if (!isShieldDisabled(player)) return 0;
        long remaining = SHIELD_DISABLED_UNTIL.get(player.getUniqueID()).longValue()
                - player.worldObj.getTotalWorldTime();
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, remaining));
    }

    public static void disableShield(EntityPlayer player) {
        if (player == null || player.worldObj == null) return;
        BLOCKING_SINCE.remove(player.getUniqueID());
        broadcastShieldState(player, false);
        SHIELD_DISABLED_UNTIL.put(player.getUniqueID(), Long.valueOf(player.worldObj.getTotalWorldTime()
                + com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ShieldMechanics120.DISABLE_COOLDOWN));
        player.clearItemInUse();
        if (player instanceof EntityPlayerMP) syncInventory((EntityPlayerMP) player);
    }

    /** Applies the authoritative remaining cooldown received by the client. */
    public static void setClientShieldCooldown(EntityPlayer player, int ticks) {
        if (player == null || player.worldObj == null) return;
        if (ticks <= 0) SHIELD_DISABLED_UNTIL.remove(player.getUniqueID());
        else {
            SHIELD_DISABLED_UNTIL.put(player.getUniqueID(), Long.valueOf(
                    player.worldObj.getTotalWorldTime() + ticks));
            BLOCKING_SINCE.remove(player.getUniqueID());
            player.clearItemInUse();
        }
    }

    public static boolean isClientShieldBlocking(EntityPlayer player) {
        return player != null && Boolean.TRUE.equals(CLIENT_SHIELD_BLOCKING.get(Integer.valueOf(player.getEntityId())));
    }

    public static void setClientShieldBlocking(int entityId, boolean blocking) {
        if (blocking) CLIENT_SHIELD_BLOCKING.put(Integer.valueOf(entityId), Boolean.TRUE);
        else CLIENT_SHIELD_BLOCKING.remove(Integer.valueOf(entityId));
    }

    private static void broadcastShieldState(EntityPlayer player, boolean blocking) {
        if (player == null || player.worldObj == null || player.worldObj.isRemote) return;
        NETWORK.sendToAllAround(new PacketShieldState(player.getEntityId(), blocking),
                new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 128.0D));
    }

    private static boolean isShield(ItemStack stack) {
        return stack != null && stack.getItem() == CombatContent.shield;
    }
    @SubscribeEvent public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            validateBaseInventory((EntityPlayerMP) event.player);
            clearState(event.player);
            sync((EntityPlayerMP) event.player);
        }
    }

    private static void validateBaseInventory(EntityPlayerMP player) {
        int inventorySize = player.inventory.getSizeInventory();
        java.util.List slots = player.inventoryContainer.inventorySlots;
        boolean slotFound = slots.size() == 46
                && ((net.minecraft.inventory.Slot) slots.get(45)).inventory == player.inventory
                && ((net.minecraft.inventory.Slot) slots.get(45)).getSlotIndex() == OffhandHooks.OFFHAND_SLOT;
        boolean armorOrder = slots.size() >= 9;
        for (int index = 0; index < 4 && armorOrder; ++index) {
            net.minecraft.inventory.Slot armor = (net.minecraft.inventory.Slot) slots.get(5 + index);
            armorOrder = armor.inventory == player.inventory && armor.getSlotIndex() == 39 - index;
        }
        boolean independent = player.inventory.mainInventory.length == 36
                && player.inventory instanceof InventoryPlayer112
                && ((InventoryPlayer112) player.inventory).htgt6$getOffHandInventory() != player.inventory.mainInventory;
        if (inventorySize != 41 || !slotFound || !armorOrder || !independent)
            throw new IllegalStateException("Incomplete 1.12 player inventory: size=" + inventorySize
                    + ", offhandSlot=" + slotFound + ", armorOrder=" + armorOrder);
        LOGGER.info("Validated native 1.12 inventory for {}: inventory=41, armor=39..36, offhand=40/45",
                player.getCommandSenderName());
    }
    @SubscribeEvent public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        clearCrossbowTracking(event.player);
        OffhandCrossbowUse120.cancel(event.player);
        if (event.player instanceof EntityPlayerMP) sync((EntityPlayerMP) event.player);
    }
    @SubscribeEvent public void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        clearCrossbowTracking(event.player);
        OffhandCrossbowUse120.cancel(event.player);
        if (event.player instanceof EntityPlayerMP) sync((EntityPlayerMP) event.player);
    }
    @SubscribeEvent public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { clearState(event.player); }

    private static void clearState(EntityPlayer player) {
        if (player == null) return;
        UUID id = player.getUniqueID();
        clearCrossbowTracking(player);
        OffhandCrossbowUse120.cancel(player);
        PENDING_SWAPS.remove(id);
        LAST_SWAPS.remove(id);
        PENDING_USE.remove(id);
        BLOCKING_SINCE.remove(id);
        SHIELD_DISABLED_UNTIL.remove(id);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.isCanceled()) return;
        EntityPlayer player = event.entityPlayer;
        ItemStack offhand = OffhandHooks.getOffhand(player);
        if (offhand == null) return;
        ItemStack main = player.getHeldItem();
        boolean shield = isShield(offhand);
        // Swords lost their right-click blocking action in 1.9; a sword in the
        // main hand must therefore permit an offhand shield.
        boolean mainConsumesUse = main != null && (main.getItem() instanceof ItemBlock
                || main.getItem() instanceof com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow
                || main.getItemUseAction() != EnumAction.none && !(main.getItem() instanceof ItemSword));
        if (shield && main != null && main.getItem() instanceof ItemSword
                && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            event.setCanceled(true);
            return;
        }
        if (event.world.isRemote) return;
        // The main hand and the clicked block's UI always get first refusal.
        if (mainConsumesUse) return;

        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            if (shield || offhand.getItem() instanceof com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow) return; // held use is driven by PacketUseOffhand
            ItemStack result = offhand.getItem().onItemRightClick(offhand, event.world, player);
            OffhandHooks.setOffhand(player, result != null && result.stackSize > 0 ? result : null);
        } else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
                && offhand.getItem() instanceof ItemBlock && player.isSneaking()) {
            // Unlike the old implementation, a normal click opens a chest/machine UI.
            // Offhand block placement is an explicit sneak-click operation.
            boolean used = offhand.tryPlaceItemIntoWorld(player, event.world, event.x, event.y, event.z,
                    event.face, 0.5F, 0.5F, 0.5F);
            if (used) {
                if (offhand.stackSize <= 0) OffhandHooks.setOffhand(player, null);
                event.setCanceled(true);
            }
        }
        if (player instanceof EntityPlayerMP) syncInventory((EntityPlayerMP) player);
    }

    @SubscribeEvent public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.worldObj.isRemote) return;
        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
            java.util.concurrent.ArrayBlockingQueue<PendingSwap> queue = PENDING_SWAPS.get(id);
            PendingSwap swap;
            for (int processed = 0; processed < 32 && queue != null && (swap = queue.poll()) != null; processed++) {
                Integer last = LAST_SWAPS.get(id);
                if (last == null || swap.sequence > last.intValue()) {
                    LAST_SWAPS.put(id, Integer.valueOf(swap.sequence));
                    swapAtomically(serverPlayer, swap.selectedSlot);
                }
            }
            java.util.concurrent.ArrayBlockingQueue<Boolean> useQueue=PENDING_USE.get(id);
            Boolean requested;
            for(int processed=0;processed<128&&useQueue!=null&&(requested=useQueue.poll())!=null;processed++) {
                boolean crossbowChanged=requested.booleanValue()?OffhandCrossbowUse120.start(player):OffhandCrossbowUse120.release(player);
                if(crossbowChanged)syncInventory(serverPlayer);
                boolean wasBlocking = BLOCKING_SINCE.containsKey(id);
                if (requested.booleanValue() && isShield(OffhandHooks.getOffhand(player))
                        && !mainHandConsumesUse(player) && !isShieldDisabled(player)) {
                    if (!BLOCKING_SINCE.containsKey(id))
                        BLOCKING_SINCE.put(id, Long.valueOf(player.worldObj.getTotalWorldTime()));
                } else {
                    BLOCKING_SINCE.remove(id);
                }
                boolean nowBlocking = BLOCKING_SINCE.containsKey(id);
                if (wasBlocking != nowBlocking) broadcastShieldState(player, nowBlocking);
            }
        }
        OffhandCrossbowUse120.tick(player);
        syncCrossbowObservers(player);
        ItemStack stack = OffhandHooks.getOffhand(event.player);
        if (stack != null && stack.stackSize <= 0) {
            OffhandHooks.setOffhand(player, null);
            stopBlocking(player);
        }
        if (!isShield(OffhandHooks.getOffhand(player)) || player.isDead) stopBlocking(player);
        if (player.ticksExisted % 20 == 0 && player instanceof EntityPlayerMP) syncInventory((EntityPlayerMP) player);
    }

    @SubscribeEvent public void crossbowStartTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event){
        if(!(event.entityPlayer instanceof EntityPlayerMP)||!(event.target instanceof EntityPlayer))return;
        EntityPlayer target=(EntityPlayer)event.target;EntityPlayerMP watcher=(EntityPlayerMP)event.entityPlayer;
        java.util.Set<EntityPlayerMP> watchers=CROSSBOW_WATCHERS.get(target.getUniqueID());
        if(watchers==null){watchers=new java.util.HashSet<EntityPlayerMP>();CROSSBOW_WATCHERS.put(target.getUniqueID(),watchers);}
        watchers.add(watcher);PacketOffhandCrossbowState state=crossbowState(target);
        LAST_CROSSBOW_STATE.put(target.getUniqueID(),state);NETWORK.sendTo(state,watcher);
    }
    @SubscribeEvent public void crossbowStopTracking(net.minecraftforge.event.entity.player.PlayerEvent.StopTracking event){
        if(!(event.entityPlayer instanceof EntityPlayerMP)||!(event.target instanceof EntityPlayer))return;
        EntityPlayer target=(EntityPlayer)event.target;java.util.Set<EntityPlayerMP> watchers=CROSSBOW_WATCHERS.get(target.getUniqueID());
        if(watchers!=null){watchers.remove(event.entityPlayer);if(watchers.isEmpty()){CROSSBOW_WATCHERS.remove(target.getUniqueID());LAST_CROSSBOW_STATE.remove(target.getUniqueID());}}
        NETWORK.sendTo(crossbowState(target).stopped(),(EntityPlayerMP)event.entityPlayer);
    }
    private static PacketOffhandCrossbowState crossbowState(EntityPlayer player){return new PacketOffhandCrossbowState(player,OffhandCrossbowUse120.useTicks(player,OffhandHooks.getOffhand(player)));}
    private static void syncCrossbowObservers(EntityPlayer player){
        java.util.Set<EntityPlayerMP> watchers=CROSSBOW_WATCHERS.get(player.getUniqueID());if(watchers==null||watchers.isEmpty())return;
        PacketOffhandCrossbowState state=crossbowState(player);
        if(player.ticksExisted%20!=0&&state.samePresentation(LAST_CROSSBOW_STATE.get(player.getUniqueID())))return;
        LAST_CROSSBOW_STATE.put(player.getUniqueID(),state);
        for(EntityPlayerMP watcher:watchers)if(watcher!=player&&watcher.dimension==player.dimension)NETWORK.sendTo(state,watcher);
    }
    private static void clearCrossbowTracking(EntityPlayer player){
        UUID id=player.getUniqueID();java.util.Set<EntityPlayerMP> watchers=CROSSBOW_WATCHERS.remove(id);
        PacketOffhandCrossbowState previous=LAST_CROSSBOW_STATE.remove(id);
        if(watchers!=null){PacketOffhandCrossbowState stopped=(previous==null?crossbowState(player):previous).stopped();for(EntityPlayerMP watcher:watchers)NETWORK.sendTo(stopped,watcher);}
        java.util.Iterator<Map.Entry<UUID,java.util.Set<EntityPlayerMP>>> it=CROSSBOW_WATCHERS.entrySet().iterator();
        while(it.hasNext()){Map.Entry<UUID,java.util.Set<EntityPlayerMP>> entry=it.next();entry.getValue().remove(player);if(entry.getValue().isEmpty()){LAST_CROSSBOW_STATE.remove(entry.getKey());it.remove();}}
    }

    public static boolean mainHandConsumesUse(EntityPlayer player) {
        ItemStack main = player.getHeldItem();
        return main != null && (main.getItem() instanceof ItemBlock
                || main.getItem() instanceof com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow
                || main.getItemUseAction() != EnumAction.none && !(main.getItem() instanceof ItemSword));
    }

    private static void swapAtomically(EntityPlayerMP player, int selected) {
        if (selected < 0 || selected >= 9) {
            syncInventory(player);
            return;
        }
        OffhandCrossbowUse120.cancel(player);
        synchronized (player.inventory) {
            ItemStack main = player.inventory.mainInventory[selected];
            ItemStack secondary = OffhandHooks.getOffhand(player);
            player.inventory.mainInventory[selected] = secondary;
            OffhandHooks.setOffhand(player, main);
            player.inventory.markDirty();
        }
        stopBlocking(player);
        player.inventoryContainer.detectAndSendChanges();
        if (player.openContainer != null && player.openContainer != player.inventoryContainer)
            player.openContainer.detectAndSendChanges();
        NETWORK.sendTo(new PacketOffhandSync(OffhandHooks.getOffhand(player), getShieldCooldownTicks(player),
                selected, player.inventory.mainInventory[selected], true), player);
    }

    public static void syncInventory(EntityPlayerMP player) {
        syncInventory(player, player.inventory.currentItem);
    }

    private static void syncInventory(EntityPlayerMP player, int changedHotbarSlot) {
        player.inventoryContainer.detectAndSendChanges();
        if (player.openContainer != null && player.openContainer != player.inventoryContainer)
            player.openContainer.detectAndSendChanges();
        // The custom acknowledgement is deliberately sent after vanilla slot
        // packets and carries the exact hotbar slot changed by this swap.
        sync(player, changedHotbarSlot);
    }

    private static final class PendingSwap {
        private final int sequence;
        private final int selectedSlot;

        private PendingSwap(int sequence, int selectedSlot) {
            this.sequence = sequence;
            this.selectedSlot = selectedSlot;
        }
    }

    private OffhandSystem() { }
}
