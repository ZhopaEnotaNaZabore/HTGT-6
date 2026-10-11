package com.mod.htgt6.asm;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.mod.htgt6.client.loading.HTGT6LoadingProgress;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import java.util.Collections;

public class HTGT6DummyContainer extends DummyModContainer {
    public HTGT6DummyContainer() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "HTGT6ASM";
        meta.name = "HiTech6 Coremod";
        meta.version = "1.0.4";
        meta.authorList = Collections.singletonList("Developer");
        meta.description = "Coremod loading infrastructure and custom splash rendering interface.";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Subscribe
    public void onConstruction(FMLConstructionEvent event) {
        if (HTGT6Config.ENABLE_NEW_FML_LOADING_SCREEN && FMLLaunchHandler.side().isClient()) {
            HTGT6LoadingProgress.install();
        }
    }
}
