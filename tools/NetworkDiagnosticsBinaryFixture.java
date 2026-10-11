package com.mod.htgt6.asm;
import com.mod.htgt6.asm.HTGT6Transformer;
import com.mod.htgt6.asm.RuntimeBytecodeRemapper;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.LaunchClassLoader;
import java.io.*;
import java.net.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public class NetworkDiagnosticsBinaryFixture {
    public static void main(String[] args) throws Exception {
        File jar = new File(args[0]);
        LaunchClassLoader loader = new LaunchClassLoader(new URL[]{jar.toURI().toURL()});
        FMLDeobfuscatingRemapper.INSTANCE.setup(new File("."), loader, "/deobfuscation_data-1.7.10.lzma");
        RuntimeBytecodeRemapper.enableProductionBridge();
        String name = "net/minecraft/network/NetworkManager";
        String notch = FMLDeobfuscatingRemapper.INSTANCE.unmap(name);
        byte[] data;
        try (ZipFile z = new ZipFile(jar); InputStream in = z.getInputStream(z.getEntry(notch + ".class"))) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096]; int n;
            while ((n = in.read(buffer)) != -1) bytes.write(buffer, 0, n);
            data = RuntimeBytecodeRemapper.toSrg(bytes.toByteArray());
        }
        HTGT6Transformer transformer = new HTGT6Transformer();
        for (int pass = 0; pass < 2; pass++) {
            data = transformer.transform(notch, name, data);
            ClassNode node = new ClassNode(); new ClassReader(data).accept(node, 0);
            int hooks = 0;
            for (MethodNode method : node.methods) {
                if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0)
                    new Analyzer(new BasicInterpreter()).analyze(node.name, method);
                for (AbstractInsnNode insn : method.instructions.toArray())
                    if (insn instanceof MethodInsnNode && ((MethodInsnNode)insn).owner.endsWith("/PacketFailureDiagnostics")) hooks++;
            }
            if (hooks != 3) throw new AssertionError("expected three hooks, found " + hooks);
        }
        System.out.println("PASS: exact binary NetworkManager, three hooks, verifier, idempotence");
    }
}
