package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Bridges the inherited legacy boat tick without changing collision resolution. */
final class CombatBoatMotionASM {
    private static final String HOOK = "com/mod/htgt6/minecraft1202/Module/Gameplay/Combat/BoatMotion120";
    static byte[] transform(byte[] bytes) {
        ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
        int collision = 0, heading = 0;
        for (MethodNode method : node.methods) {
            if (!(method.name.equals("onUpdate") || method.name.equals("func_70071_h_")) || !method.desc.equals("()V")) continue;
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn instanceof FieldInsnNode) {
                    FieldInsnNode field = (FieldInsnNode)insn;
                    if (field.getOpcode() == Opcodes.GETFIELD && field.desc.equals("Z")
                            && (field.name.equals("isCollidedHorizontally") || field.name.equals("field_70123_F"))) {
                        method.instructions.set(field, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK,
                                "breakOnCollision", "(Lnet/minecraft/entity/Entity;)Z", false));
                        collision++;
                    }
                } else if (insn instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode)insn;
                    if (call.owner.equals("java/lang/Math") && call.name.equals("atan2") && call.desc.equals("(DD)D")) {
                        InsnList hook = new InsnList();
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "heading",
                                "(DLnet/minecraft/entity/Entity;)D", false));
                        method.instructions.insert(call, hook); heading++;
                    }
                }
            }
        }
        if (collision != 1 || heading != 1) throw new IllegalStateException("Boat tick anchors: " + collision + "/" + heading);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer); return writer.toByteArray();
    }
}
