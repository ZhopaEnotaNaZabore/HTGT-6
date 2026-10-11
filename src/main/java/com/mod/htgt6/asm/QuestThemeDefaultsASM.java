package com.mod.htgt6.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Changes BQ's initial/config default only; saved user selections take precedence. */
final class QuestThemeDefaultsASM {
    static byte[] transform(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean changed = false;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof LdcInsnNode
                        && "betterquesting:light".equals(((LdcInsnNode) instruction).cst)) {
                    ((LdcInsnNode) instruction).cst = "htgt6:kanna";
                    changed = true;
                }
            }
        }
        if (!changed) return bytes;
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private QuestThemeDefaultsASM() {}
}
