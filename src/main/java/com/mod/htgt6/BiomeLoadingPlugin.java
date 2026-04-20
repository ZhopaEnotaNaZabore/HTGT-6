package com.mod.htgt6;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import java.io.File;
import java.util.Map;

/**
 * The entry point for the CoreMod.
 * This class must be referenced in your MANIFEST.MF as 'FMLCorePlugin'.
 */
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"com.mod.htgt6.asm"})
@IFMLLoadingPlugin.SortingIndex(1001) // Ensures it loads after most other coremods
public class BiomeLoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public void injectData(Map<String, Object> data) {
        // Find the 'mcLocation' (the root game folder)
        File mcDir = (File) data.get("mcLocation");
        File configDir = new File(mcDir, "config");

        // Load our config
        BiomeConfig.loadConfig(configDir);
    }

    /**
     * Returns a list of classes that implement IClassTransformer.
     * These are called in the order they appear here.
     */
    @Override
    public String[] getASMTransformerClass() {
        if (BiomeConfig.enabled) {
            return new String[]{
                  //  "com.mod.htgt6.asm.ChunkTransformer",
                  //  "com.mod.htgt6.asm.AnvilTransformer",
                 //   "com.mod.htgt6.asm.PacketTransformer",
                   // "com.mod.htgt6.asm.BiomeBaseTransformer",
                  //  "com.mod.htgt6.asm.ManagerTransformer"
                    "com.mod.htgt6.asm.BiomeTransformer"
            };
        }
        return new String[0];
    }


    /**
     * Returns the class name of the DummyModContainer.
     * This allows your CoreMod to appear in the Mod List and behave like a normal mod.
     */
    @Override
    public String getModContainerClass() {
        return "com.mod.htgt6.BiomeExtensionContainer";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    //  @Override
    // public void injectData(Map<String, Object> data) {
    //     // You can capture the 'runtimeDeobfuscatedEnabled' flag here if needed
    // }

    @Override
    public String getAccessTransformerClass() {
        // If you used an Access Transformer file (rules.at), return its path here.
        // Since we are using ASM to change access/types, this is null.
        return null;
    }
}