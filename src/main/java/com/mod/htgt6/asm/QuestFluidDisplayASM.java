package com.mod.htgt6.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/** No BQ classes are linked unless the optional mod is present. Works on client and server. */
final class QuestFluidDisplayASM {
    static final String HOOK = "com/mod/htgt6/common/compat/QuestFluidDisplayHook";

    static byte[] transform(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        for (MethodNode method : node.methods)
            for (AbstractInsnNode instruction : method.instructions.toArray())
                if (instruction instanceof MethodInsnNode && HOOK.equals(((MethodInsnNode) instruction).owner)) return bytes;
        int patched = 0;
        for (MethodNode method : node.methods) {
            if ("loadItemStackFromNBT".equals(method.name)
                    && (method.desc.startsWith("(Lnet/minecraft/nbt/NBTTagCompound;)") || method.desc.startsWith("(Ldh;)"))) {
                String nbt = method.desc.substring(1, method.desc.indexOf(')'));
                InsnList entry = new InsnList();
                entry.add(new VarInsnNode(Opcodes.ALOAD, 0));
                entry.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "normalizeNBT", "(" + nbt + ")" + nbt, false));
                entry.add(new VarInsnNode(Opcodes.ASTORE, 0));
                method.instructions.insert(entry);
                patched++;
            }
            if ("getBaseStack".equals(method.name)
                    && ("()Lnet/minecraft/item/ItemStack;".equals(method.desc) || "()Ladd;".equals(method.desc))) {
                String stack = method.desc.substring(2);
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() == Opcodes.ARETURN)
                        method.instructions.insertBefore(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                HOOK, "normalizeStack", "(" + stack + ")" + stack, false));
                }
                patched++;
            }
        }
        if (patched != 2) throw new IllegalStateException("BQ fluid display hook drift: " + patched);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private QuestFluidDisplayASM() { }
}
