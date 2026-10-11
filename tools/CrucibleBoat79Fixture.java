package com.mod.htgt6.asm;
import java.io.File;
import java.net.URL;
import java.util.zip.ZipFile;
import java.lang.reflect.Method;
import net.minecraft.launchwrapper.LaunchClassLoader;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
public final class CrucibleBoat79Fixture {
    public static void main(String[] args) throws Exception {
        File file=new File(args[0]);
        FMLDeobfuscatingRemapper.INSTANCE.setup(new File("."),new LaunchClassLoader(new URL[]{file.toURI().toURL()}),"/deobfuscation_data-1.7.10.lzma");
        RuntimeBytecodeRemapper.enableProductionBridge();
        Method verify=ProductionASMRemapFixture120.class.getDeclaredMethod("verifyTarget",ZipFile.class,HTGT6Transformer.class,String.class);
        verify.setAccessible(true);
        String target=args.length>1?args[1]:"net/minecraft/entity/item/EntityBoat";
        try(ZipFile zip=new ZipFile(file)){verify.invoke(null,zip,new HTGT6Transformer(),target);}
        System.out.println("PASS: reconstructed Crucible "+target+" production namespace and bytecode analysis");
    }
}
