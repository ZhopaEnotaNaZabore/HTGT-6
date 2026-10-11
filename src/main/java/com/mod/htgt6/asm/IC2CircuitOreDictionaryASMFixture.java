package com.mod.htgt6.asm;

import com.mod.htgt6.common.GTHooks.MaterialHooks.IC2CircuitOreDictionaryHook;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

/** Checks the real Forge entry point, all registration overloads, and the narrow item policy. */
public final class IC2CircuitOreDictionaryASMFixture {
    private static final String TARGET = "net.minecraftforge.oredict.OreDictionary";

    public static void main(String[] args) throws Exception {
        try (InputStream input = IC2CircuitOreDictionaryASMFixture.class.getClassLoader()
                .getResourceAsStream(TARGET.replace('.', '/') + ".class")) {
            verify(read(input));
        }
        for (String path : args) {
            try (ZipFile jar = new ZipFile(path);
                    InputStream input = jar.getInputStream(jar.getEntry(TARGET.replace('.', '/') + ".class"))) {
                verify(read(input));
            }
        }
        for (String ore : new String[] {"circuitBasic", "gt:circuit1"}) {
            require(IC2CircuitOreDictionaryHook.isBlocked(ore, "IC2:itemPartCircuit"), "IC2 T1 must be blocked");
            require(!IC2CircuitOreDictionaryHook.isBlocked(ore, "gregtech:gt.multiitem.technological"),
                    "GT circuits must keep T1 membership");
            require(!IC2CircuitOreDictionaryHook.isBlocked(ore, "IC2:itemPartCircuitAdv"),
                    "Advanced IC2 circuits must be unaffected");
            require(!IC2CircuitOreDictionaryHook.shouldSkip(ore, null), "Preserve Forge null handling");
        }
        for (String ore : new String[] {null, "", "gt:circuit0", "gt:circuit2", "circuitAdvanced", "electronicCircuit"}) {
            require(!IC2CircuitOreDictionaryHook.isBlocked(ore, "IC2:itemPartCircuit"),
                    "Unrelated ore entries must be unaffected: " + ore);
        }
        System.out.println("IC2 circuit OreDictionary fixture passed (development and production Forge).");
    }

    private static void verify(byte[] original) throws Exception {
        HTGT6Transformer transformer = new HTGT6Transformer();
        byte[] patched = transformer.transform(TARGET, TARGET, original);
        require(!Arrays.equals(original, patched), "Transformer dispatch did not apply the guard");
        require(Arrays.equals(patched, transformer.transform(TARGET, TARGET, patched)), "Patch must be idempotent");
        ClassNode node = new ClassNode();
        new ClassReader(patched).accept(node, 0);
        int guards = 0, entryCalls = 0;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) insn;
                if (IC2CircuitOreDictionaryASM.HOOK.equals(call.owner)) {
                    guards++;
                    require("registerOreImpl".equals(method.name), "Guard must cover both name and ID overloads");
                    require("shouldSkip".equals(call.name), "Incorrect hook");
                    AbstractInsnNode next = call.getNext();
                    require(next.getOpcode() == Opcodes.IFEQ && next.getNext().getOpcode() == Opcodes.RETURN,
                            "Blocked registration must return before map updates and events");
                    AbstractInsnNode first = method.instructions.getFirst();
                    require(first.getOpcode() == Opcodes.ALOAD && first.getNext().getOpcode() == Opcodes.ALOAD
                            && first.getNext().getNext() == call, "Guard must precede original Forge code");
                }
                if (node.name.equals(call.owner) && "registerOreImpl".equals(call.name)) entryCalls++;
            }
            if ("registerOreImpl".equals(method.name)) new Analyzer(new BasicVerifier()).analyze(node.name, method);
        }
        require(guards == 1 && entryCalls == 2, "Expected one guard shared by String and numeric-ID registration");
    }

    private static byte[] read(InputStream input) throws Exception {
        if (input == null) throw new AssertionError("Forge OreDictionary bytecode not found");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        for (int count; (count = input.read(buffer)) != -1;) output.write(buffer, 0, count);
        return output.toByteArray();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
