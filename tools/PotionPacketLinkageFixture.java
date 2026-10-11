package com.mod.htgt6.asm;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.LaunchClassLoader;
import java.io.*;
import java.net.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class PotionPacketLinkageFixture {
    public static void main(String[] args) throws Exception {
        File jar = new File(args[0]);
        FMLDeobfuscatingRemapper.INSTANCE.setup(new File("."),
                new LaunchClassLoader(new URL[]{jar.toURI().toURL()}), "/deobfuscation_data-1.7.10.lzma");
        RuntimeBytecodeRemapper.enableProductionBridge();
        try (ZipFile z = new ZipFile(jar)) {
            for (String simple : new String[]{"S1DPacketEntityEffect", "S1EPacketRemoveEntityEffect"}) {
                String name = "net/minecraft/network/play/server/" + simple;
                String notch = FMLDeobfuscatingRemapper.INSTANCE.unmap(name);
                byte[] raw;
                try (InputStream in = z.getInputStream(z.getEntry(notch + ".class"))) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] b = new byte[4096]; int n;
                    while ((n = in.read(b)) != -1) out.write(b, 0, n);
                    raw = out.toByteArray();
                }
                byte[] patched = new HTGT6Transformer().transform(notch, name, RuntimeBytecodeRemapper.toSrg(raw));
                byte[] runtime = RuntimeBytecodeRemapper.toSrg(RuntimeBytecodeRemapper.toNotch(patched));
                ProductionASMRemapFixture120.verifyPotionBufferLinkage(z, runtime);
                ClassNode broken = new ClassNode(); new ClassReader(runtime).accept(broken, 0);
                for (MethodNode m : broken.methods) for (AbstractInsnNode i : m.instructions.toArray())
                    if (i instanceof MethodInsnNode && ((MethodInsnNode)i).name.equals("func_150787_b"))
                        ((MethodInsnNode)i).name = "writeVarIntToBuffer";
                ClassWriter writer = new ClassWriter(0); broken.accept(writer);
                boolean rejected = false;
                try { ProductionASMRemapFixture120.verifyPotionBufferLinkage(z, writer.toByteArray()); }
                catch (AssertionError expected) { rejected = true; }
                if (!rejected) throw new AssertionError("Old MCP linkage error escaped detection");
                System.out.println("PASS " + simple + ": runtime read/write resolve; original bug rejected");
            }
        }
    }
}
