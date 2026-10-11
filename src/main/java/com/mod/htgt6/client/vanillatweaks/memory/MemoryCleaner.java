package com.mod.htgt6.client.vanillatweaks.memory;

import com.mod.htgt6.client.vanillatweaks.CHAT.ChatManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.world.WorldEvent;

/** Releases only HiTech6-owned caches. JVM collection remains the collector's job. */
public class MemoryCleaner {

    static final int CLEANUP_NONE = 0;
    static final int CLEANUP_PARTIAL = 1;
    static final int CLEANUP_FULL_DISCONNECT = 2;
    private static boolean cleanupPending;
    private static boolean fullDisconnectPending;

    private final Minecraft mc = Minecraft.getMinecraft();

    public MemoryCleaner() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world != null && event.world.isRemote) {
            requestAggressiveCleanup(false);
        }
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        // FML may deliver this event on Netty Client IO. Never touch the
        // TextureManager or RenderGlobal from that network thread.
        requestAggressiveCleanup(true);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        int requested = takeCleanupRequestForTick();
        if (requested != CLEANUP_NONE)
            executeAggressiveCleanup(requested == CLEANUP_FULL_DISCONNECT);

        ChatManager.tickClientCaches();
    }

    @SubscribeEvent
    public void onMainMenuAction(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        // Vanilla Minecraft 1.7.10 assigns id 4 to the "Quit Game" button.
        if (event.gui instanceof GuiMainMenu && event.button.id == 4) {
            event.setCanceled(true);
            if (!(this.mc.currentScreen instanceof GuiCloseConfirmation)) {
                this.mc.displayGuiScreen(new GuiCloseConfirmation(event.gui));
            }
        }
    }

    public static void executeManualCleanup() {
        requestAggressiveCleanup(false);
    }

    static synchronized void requestAggressiveCleanup(boolean fullDisconnect) {
        cleanupPending = true;
        fullDisconnectPending |= fullDisconnect;
    }

    static synchronized int takeCleanupRequestForTick() {
        if (!cleanupPending) return CLEANUP_NONE;
        int result = fullDisconnectPending ? CLEANUP_FULL_DISCONNECT : CLEANUP_PARTIAL;
        cleanupPending = false;
        fullDisconnectPending = false;
        return result;
    }

    private static void executeAggressiveCleanup(boolean fullDisconnect) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (!mc.func_152345_ab()) {
                // Defensive fallback for any future caller: defer instead of
                // risking GL calls without the Minecraft render context.
                requestAggressiveCleanup(fullDisconnect);
                return;
            }
            if (fullDisconnect) {
                ChatManager.clear();
            }

            // Never sweep TextureManager: distant players and other mods may still
            // own those textures. Never rebuild the world renderer or force GC.
            ChatManager.requestClientCacheCleanup();

        } catch (Exception e) {
            System.err.println("[MemoryCleaner] Cache cleanup failed: " + e.getMessage());
        }
    }
}
