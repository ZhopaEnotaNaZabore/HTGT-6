package com.mod.htgt6.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/** Filters before Forge updates its forward/reverse maps and emits the GT6 listener event. */
final class IC2CircuitOreDictionaryASM {
    static final String HOOK =
            "com/mod/htgt6/common/GTHooks/MaterialHooks/IC2CircuitOreDictionaryHook";
    static final String ARGS = "(Ljava/lang/String;Lnet/minecraft/item/ItemStack;)";

    static byte[] transform(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        int patched = 0;
        for (MethodNode method : node.methods) {
            if (!"registerOreImpl".equals(method.name)
                    || !((ARGS + "V").equals(method.desc)
                    || "(Ljava/lang/String;Ladd;)V".equals(method.desc))) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode
                        && HOOK.equals(((MethodInsnNode) instruction).owner)) return bytes;
            }
            LabelNode proceed = new LabelNode();
            InsnList guard = new InsnList();
            guard.add(new VarInsnNode(Opcodes.ALOAD, 0));
            guard.add(new VarInsnNode(Opcodes.ALOAD, 1));
            // Forge may reach us before FML remaps the production ItemStack (add).
            String hookDescriptor = method.desc.substring(0, method.desc.length() - 1) + "Z";
            guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "shouldSkip", hookDescriptor, false));
            guard.add(new JumpInsnNode(Opcodes.IFEQ, proceed));
            guard.add(new InsnNode(Opcodes.RETURN));
            guard.add(proceed);
            guard.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            method.instructions.insert(guard);
            patched++;
        }
        if (patched != 1) throw new IllegalStateException("Forge OreDictionary registration hook drift: " + patched);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private IC2CircuitOreDictionaryASM() { }
}
