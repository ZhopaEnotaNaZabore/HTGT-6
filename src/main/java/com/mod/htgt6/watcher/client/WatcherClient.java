package com.mod.htgt6.watcher.client;

import com.mod.htgt6.watcher.Watcher;
import com.mod.htgt6.watcher.WatcherProxy;
import com.mod.htgt6.watcher.WatcherRuntime;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;

public final class WatcherClient extends WatcherProxy {
    private GuiWatcherStop screen;

    @Override public void initialize() {
        if (Boolean.getBoolean("htgt6.watcher.preview")) {
            Watcher.STATE.report("PREVIEW_REQUESTED");
        }
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        WatcherRuntime.tick();
        if (!Watcher.STATE.shouldDisplay()) return;
        Minecraft mc = Minecraft.getMinecraft();
        // First client tick is after the entire FML LoadComplete dispatch.
        if (screen == null) {
            screen = new GuiWatcherStop();
            LogManager.getLogger("HTGT6-Watcher").warn("Watcher stop screen: {}", Watcher.STATE.getFinding());
            // A runtime detection must also stop the multiplayer session / integrated world.
            if (mc.theWorld != null) {
                mc.theWorld.sendQuittingDisconnectingPacket();
                mc.loadWorld(null);
            }
            mc.getSoundHandler().stopSounds();
            // Fullscreen is requested once: never fight OS focus changes or Alt-Tab.
            if (!mc.isFullScreen()) mc.toggleFullscreen();
        }
        if (mc.currentScreen != screen) mc.displayGuiScreen(screen);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void gui(GuiOpenEvent event) {
        if (screen != null) {
            event.setCanceled(false);
            event.gui = screen;
        }
    }
}
