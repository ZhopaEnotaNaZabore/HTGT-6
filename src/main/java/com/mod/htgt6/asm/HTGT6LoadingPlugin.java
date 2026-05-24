package com.mod.htgt6.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;

import java.util.Map;




import java.io.File;


@IFMLLoadingPlugin.Name("HTGT6BiomeASM")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class HTGT6LoadingPlugin
        implements IFMLLoadingPlugin {




    @Override
    public void injectData(Map<String, Object> data) {

             Launch.classLoader.addTransformerExclusion("com.mod.htgt6.asm.");
                Launch.classLoader.addTransformerExclusion("com.mod.htgt6.asm.transformers."
        );

        File mcDir =
                (File) data.get("mcLocation");

        HTGT6Config.load(mcDir);
    }
    @Override
    public String[] getASMTransformerClass() {

        return new String[] {
                "com.mod.htgt6.asm.HTGT6Transformer"
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

  //  @Override
   // public void injectData(Map<String, Object> data) {

    //    File mcDir =
    //            (File) data.get("mcLocation");

    //    HTGT6Config.load(mcDir);
 //   }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}