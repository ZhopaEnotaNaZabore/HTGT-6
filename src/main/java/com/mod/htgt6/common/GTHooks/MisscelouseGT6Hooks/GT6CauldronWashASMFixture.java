package com.mod.htgt6.common.GTHooks.MisscelouseGT6Hooks;

import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.asm.HTGT6Transformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Bytecode contract test for the optional GT6 cauldron washing patch. */
public final class GT6CauldronWashASMFixture {
    private static final String CLASS_NAME =
            "gregapi.oredict.listeners.OreDictListenerItem_Washing";
    private static final String ENTRY =
            "gregapi/oredict/listeners/OreDictListenerItem_Washing.class";
    private static final String SELF = "com/mod/htgt6/asm/HTGT6Transformer";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Expected the GT6 jar path");
        byte[] original;
        try (ZipFile jar = new ZipFile(args[0])) {
            ZipEntry entry = jar.getEntry(ENTRY);
            if (entry == null) throw new AssertionError("Missing " + ENTRY);
            try (InputStream input = jar.getInputStream(entry)) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                for (int read; (read = input.read(buffer)) >= 0; ) output.write(buffer, 0, read);
                original = output.toByteArray();
            }
        }

        HTGT6Config.FIX_GT6_CAUDRON_ISE = true;
        byte[] transformed = new HTGT6Transformer().transform(CLASS_NAME, CLASS_NAME, original);
        ClassNode node = new ClassNode();
        new ClassReader(transformed).accept(node, 0);

        int nextIntCalls = 0;
        for (MethodNode method : node.methods) {
            if (!"onTickWorld".equals(method.name)) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.getOpcode() != Opcodes.INVOKEVIRTUAL
                        || !"java/util/Random".equals(call.owner)
                        || !"nextInt".equals(call.name) || !"(I)I".equals(call.desc)) continue;
                nextIntCalls++;
                AbstractInsnNode previous = previousCode(instruction);
                if (!(previous instanceof MethodInsnNode))
                    throw new AssertionError("nextInt bound is not guarded");
                MethodInsnNode guard = (MethodInsnNode) previous;
                if (guard.getOpcode() != Opcodes.INVOKESTATIC || !SELF.equals(guard.owner)
                        || !"positiveRandomBound".equals(guard.name) || !"(I)I".equals(guard.desc))
                    throw new AssertionError("Unexpected nextInt guard: " + guard.owner + '.' + guard.name);
            }
        }
        if (nextIntCalls == 0) throw new AssertionError("No Random.nextInt calls found");
        if (HTGT6Transformer.positiveRandomBound(0) != 1
                || HTGT6Transformer.positiveRandomBound(-4) != 1
                || HTGT6Transformer.positiveRandomBound(7) != 7)
            throw new AssertionError("positiveRandomBound contract failed");
        System.out.println("GT6 cauldron ASM fixture passed; guarded calls=" + nextIntCalls);
    }

    private static AbstractInsnNode previousCode(AbstractInsnNode node) {
        AbstractInsnNode previous = node.getPrevious();
        while (previous != null && previous.getOpcode() < 0) previous = previous.getPrevious();
        return previous;
    }

    private GT6CauldronWashASMFixture() { }
}
