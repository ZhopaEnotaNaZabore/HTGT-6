package com.mod.htgt6.asm;


import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import java.util.Map;
import java.io.File;

@IFMLLoadingPlugin.Name("HT-GT6ASM")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class HTGT6LoadingPlugin implements IFMLLoadingPlugin {

    @Override
    public void injectData(Map<String, Object> data) {
        File mcDir = (File) data.get("mcLocation");
        if (mcDir == null) {
            mcDir = new File(".");
        }
        HTGT6Config.load(mcDir);
        RuntimeBytecodeRemapper.enableProductionBridge();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
                "com.mod.htgt6.asm.DevRuntimeSrgRemapper",
                "com.mod.htgt6.asm.HTGT6Transformer",
                "com.mod.htgt6.asm.RuntimeReverseRemappingTransformer"
        };
    }

    @Override
    public String getModContainerClass() {
        return "com.mod.htgt6.asm.HTGT6DummyContainer";
    }

    @Override
    public String getSetupClass() { return null; }

    @Override
    public String getAccessTransformerClass() { return null; }
}
