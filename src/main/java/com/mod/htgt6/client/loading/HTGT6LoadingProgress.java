package com.mod.htgt6.client.loading;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.FMLModContainer;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Tracks one aggregate percentage without exposing Forge lifecycle names. */
public final class HTGT6LoadingProgress {

    private static final Logger LOGGER = LogManager.getLogger("HTGT6-LoadingProgress");
    private static final List<PerModListener> LISTENERS = new ArrayList<PerModListener>();
    private static boolean installed;

    private HTGT6LoadingProgress() {}

    public static synchronized void install() {
        if (installed) return;
        installed = true;
        try {
            List<FMLModContainer> containers = new ArrayList<FMLModContainer>();
            for (ModContainer container : Loader.instance().getActiveModList()) {
                if (container instanceof FMLModContainer) containers.add((FMLModContainer) container);
            }
            Field eventBusField = FMLModContainer.class.getDeclaredField("eventBus");
            eventBusField.setAccessible(true);
            int total = Math.max(1, containers.size());
            for (int index = 0; index < containers.size(); index++) {
                PerModListener listener = new PerModListener(index, total);
                EventBus bus = (EventBus) eventBusField.get(containers.get(index));
                bus.register(listener);
                LISTENERS.add(listener);
            }
            HTGT6LoadingScreen.setProgress(0.08F);
            LOGGER.info("Tracking loading progress for {} FML mods", containers.size());
        } catch (Throwable error) {
            LOGGER.error("Unable to install per-mod loading progress listeners", error);
        }
    }

    private enum Stage {
        CONSTRUCT(0.08F, 0.18F),
        PRE_INIT(0.26F, 0.24F),
        INIT(0.50F, 0.20F),
        POST_INIT(0.70F, 0.18F),
        LOAD_COMPLETE(0.88F, 0.11F);

        final float base;
        final float span;

        Stage(float base, float span) {
            this.base = base;
            this.span = span;
        }
    }

    private static final class PerModListener {
        private final int index;
        private final int total;

        PerModListener(int index, int total) {
            this.index = index;
            this.total = total;
        }

        private void update(Stage stage) {
            HTGT6LoadingScreen.setProgress(stage.base + stage.span * (index + 1.0F) / total);
        }

        @Subscribe public void construction(FMLConstructionEvent event) { update(Stage.CONSTRUCT); }
        @Subscribe public void preInit(FMLPreInitializationEvent event) { update(Stage.PRE_INIT); }
        @Subscribe public void init(FMLInitializationEvent event) { update(Stage.INIT); }
        @Subscribe public void postInit(FMLPostInitializationEvent event) { update(Stage.POST_INIT); }
        @Subscribe public void complete(FMLLoadCompleteEvent event) { update(Stage.LOAD_COMPLETE); }
    }
}
