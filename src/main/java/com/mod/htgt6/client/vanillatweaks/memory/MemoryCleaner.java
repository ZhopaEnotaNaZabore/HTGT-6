package com.mod.htgt6.client.vanillatweaks.memory;

import com.mod.htgt6.client.vanillatweaks.CHAT.ChatManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MemoryCleaner {

    private final Minecraft mc = Minecraft.getMinecraft();
    private int passiveCleanupTimer = 0;

    public MemoryCleaner() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world != null && event.world.isRemote) {
            executeAggressiveCleanup(false);
        }
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        executeAggressiveCleanup(true);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // --- FIXED: FORCED WINDOW CLOSE INTERCEPTOR HACK ---
        if (org.lwjgl.opengl.Display.isCloseRequested()) {
            // Check if our confirmation UI isn't already active
            if (!(this.mc.currentScreen instanceof GuiCloseConfirmation)) {
                try {
                    // 1. Force Minecraft's internal "running" loop flag back to true!
                    // This stops Minecraft from instantly breaking its engine loop.
                    java.lang.reflect.Field runningField = null;

                    // Search both forge-development and production obfuscated names
                    for (java.lang.reflect.Field f : Minecraft.class.getDeclaredFields()) {
                        if (f.getType() == boolean.class) {
                            String name = f.getName();
                            // 'running' is development name, 'field_71425_J' is 1.7.10 SRG/Production name
                            if (name.equals("running") || name.equals("field_71425_J")) {
                                runningField = f;
                                break;
                            }
                        }
                    }

                    if (runningField != null) {
                        runningField.setAccessible(true);
                        runningField.setBoolean(this.mc, true);
                    }

                    // 2. Consume the OS window close signal to satisfy LWJGL 2
                    org.lwjgl.opengl.Display.processMessages();

                    // 3. Forcefully open the custom confirmation overlay
                    this.mc.displayGuiScreen(new GuiCloseConfirmation(this.mc.currentScreen));

                } catch (Exception e) {
                    System.err.println("[MemoryCleaner] Close interception failed: " + e.getMessage());
                }
            }
        }
        // ----------------------------------------------------

        // Rest of your background memory cleaner loop...
        if (this.mc.theWorld == null) return;

        this.passiveCleanupTimer++;
        if (this.passiveCleanupTimer >= 6000) {
            this.passiveCleanupTimer = 0;

            long maxMem = Runtime.getRuntime().maxMemory();
            long totalMem = Runtime.getRuntime().totalMemory();
            long freeMem = Runtime.getRuntime().freeMemory();
            long usedMem = totalMem - freeMem;

            if (((double) usedMem / maxMem) > 0.85) {
                purgeStaleSkinsCache();
                runDeepGC();
            }
        }
    }

    public static void executeManualCleanup() {
        executeAggressiveCleanup(false);
    }

    private static void executeAggressiveCleanup(boolean fullDisconnect) {
        try {
            if (fullDisconnect) {
                synchronized (ChatManager.messages) {
                    ChatManager.messages.clear();
                }
                synchronized (ChatManager.timestamps) {
                    ChatManager.timestamps.clear();
                }
            }

            Minecraft mc = Minecraft.getMinecraft();
            if (mc.renderGlobal != null) {
                if (fullDisconnect) {
                    mc.renderGlobal.deleteAllDisplayLists();
                } else {
                    // FIX: Live-reload the mesh layers to safely drop stale far-away geometry while instantly drawing back near chunks
                    mc.renderGlobal.loadRenderers();
                }
            }

            purgeStaleSkinsCache();

            // FIX: Multi-pass sweeping to make sure F3 layout recognizes data loss
            runDeepGC();

        } catch (Exception e) {
            System.err.println("[MemoryCleaner] Error occurred during cleanup routine execution: " + e.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    private static void purgeStaleSkinsCache() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            TextureManager texManager = mc.getTextureManager();
            if (texManager == null) return;

            // FIX: Build a safe-list of skin/cape textures used by active entities currently near the client
            Set<ResourceLocation> activeTextures = new HashSet<ResourceLocation>();
            if (mc.theWorld != null && mc.theWorld.playerEntities != null) {
                for (Object obj : mc.theWorld.playerEntities) {
                    if (obj instanceof AbstractClientPlayer) {
                        AbstractClientPlayer player = (AbstractClientPlayer) obj;
                        if (player.getLocationSkin() != null) {
                            activeTextures.add(player.getLocationSkin());
                        }
                        if (player.getLocationCape() != null) {
                            activeTextures.add(player.getLocationCape());
                        }
                    }
                }
            }

            Field mapTextureObjectsField = null;
            for (Field f : TextureManager.class.getDeclaredFields()) {
                if (f.getType() == Map.class) {
                    mapTextureObjectsField = f;
                    break;
                }
            }

            if (mapTextureObjectsField != null) {
                mapTextureObjectsField.setAccessible(true);
                Map mapTextureObjects = (Map) mapTextureObjectsField.get(texManager);

                if (mapTextureObjects != null) {
                    Iterator iterator = mapTextureObjects.entrySet().iterator();

                    while (iterator.hasNext()) {
                        Map.Entry entry = (Map.Entry) iterator.next();
                        ResourceLocation location = (ResourceLocation) entry.getKey();
                        ITextureObject texture = (ITextureObject) entry.getValue();

                        // Only evaluate skin target parameters
                        String path = location.getResourcePath();
                        if (path.contains("skins/") || path.contains("capes/") || path.contains("cloth")) {

                            // FIX: If the skin is actively tracked inside render distance, DO NOT drop it from memory
                            if (activeTextures.contains(location)) {
                                continue;
                            }

                            int glTextureId = texture.getGlTextureId();
                            if (glTextureId != -1) {
                                TextureUtil.deleteTexture(glTextureId);
                            }
                            iterator.remove();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Fail silently
        }
    }

    /**
     * FIX: Forces a multi-generational JVM collection block so the F3 overlay completely refreshes.
     */
    private static void runDeepGC() {
        System.runFinalization();
        System.gc();
        try {
            // Short 10ms thread sync gap allows JVM cleaner hooks to clear reference arrays
            Thread.sleep(10);
        } catch (InterruptedException ignored) {}
        System.runFinalization();
        System.gc();
    }
}