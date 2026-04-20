package com.mod.htgt6;

import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import java.util.Collections;

/**
 * This class allows the CoreMod/ASM logic to be recognized as a standard mod
 * in the Minecraft Forge environment.
 */
public class BiomeExtensionContainer extends DummyModContainer {

    public BiomeExtensionContainer() {
        // Initialize the metadata for the mod list
        super(new ModMetadata());
        ModMetadata meta = getMetadata();

        // Ensure this modId matches your main @Mod ID if you are merging them
        meta.modId = "biomeidextender_htgt6";
        meta.name = "HiTech6 Biome ID Extender";
        meta.version = "1.0.0";
        meta.authorList = Collections.singletonList("HTGT6");
        meta.description = "Pizdets ot Prizma, Biome ID Extender 256=>32726 ALPHA VERSION";

        // Optional: Link to a website or update JSON
        meta.url = "";
        meta.screenshots = new String[0];
        meta.parent = "";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        // Return true to allow this container to participate in the mod loading bus
        return true;
    }
}

