package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Forge 1.7.10 owns applicability on Enchantment, not Item. */
public final class NetheriteHoeEnchantmentASM116 {
    private static final String HOOK = "com/mod/htgt6/minecraft1202/Module/Items/Modern/ItemNetheriteHoe116";
    private static final String DESC = "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/enchantment/Enchantment;)Z";

    public static byte[] transform(byte[] bytes) {
        ClassNode node = new ClassNode();
        // Some early transformers retain Java 5 class versions (legacy StackMap).
        // Expanded frames are valid for both StackMap and StackMapTable.
        new ClassReader(bytes).accept(node, ClassReader.EXPAND_FRAMES);
        int matched = 0;
        boolean changed = false;
        for (MethodNode method : node.methods) {
            if (!(method.name.equals("canApply") || method.name.equals("func_92089_a"))
                    || !method.desc.equals("(Lnet/minecraft/item/ItemStack;)Z")) continue;
            matched++;
            int existing = 0, returns = 0;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction.getOpcode() == Opcodes.IRETURN) returns++;
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.getOpcode() == Opcodes.INVOKESTATIC && call.owner.equals(HOOK)
                        && call.name.equals("allowsEnchantment") && call.desc.equals(DESC)) existing++;
            }
            if (returns == 0) throw new IllegalStateException("Enchantment applicability has no boolean return");
            if (existing != 0) {
                if (existing != returns) throw new IllegalStateException("Partial netherite hoe applicability hooks");
                continue;
            }
            // CoFH 3.x expands incoming frames and then inserts compressed frames.
            // Introducing a new branch here triggers its mixed-frame writer bug.
            // Extend the existing boolean result without adding labels or frames;
            // CoFH's own item-level accept/deny override keeps its original priority.
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction.getOpcode() != Opcodes.IRETURN) continue;
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "allowsEnchantment", DESC, false));
                hook.add(new InsnNode(Opcodes.IOR));
                method.instructions.insertBefore(instruction, hook);
            }
            changed = true;
        }
        if (matched != 1) throw new IllegalStateException("Enchantment applicability methods=" + matched);
        if (!changed) return bytes;
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private NetheriteHoeEnchantmentASM116() {}
}
