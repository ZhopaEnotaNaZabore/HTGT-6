package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Additive hook; Crucible cancellation remains authoritative, with no Bukkit linkage. */
final class CampfireWaterASM157 {
    static byte[] transform(byte[] bytes) {
        ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
        String owner = "com/mod/htgt6/minecraft1202/Module/Gameplay/VillageAndPillage/CampfireWater157";
        String args = "Lnet/minecraft/entity/projectile/EntityPotion;Lnet/minecraft/util/MovingObjectPosition;";
        int methods = 0;
        for (MethodNode method : node.methods) {
            if (!(method.name.equals("onImpact") || method.name.equals("func_70184_a"))
                    || !method.desc.equals("(Lnet/minecraft/util/MovingObjectPosition;)V")) continue;
            methods++;
            MethodInsnNode cancelled = null;
            for (AbstractInsnNode insn : method.instructions.toArray()) if (insn instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) insn;
                if (call.owner.equals(owner)) return bytes;
                if (call.owner.equals("org/bukkit/event/entity/PotionSplashEvent")
                        && call.name.equals("isCancelled") && call.desc.equals("()Z")) {
                    if (cancelled != null) throw new IllegalStateException("Multiple potion cancellation checks");
                    cancelled = call;
                }
            }
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, 0)); hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
            hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner,
                    cancelled == null ? "onImpact" : "afterCancellation",
                    cancelled == null ? "(" + args + ")V" : "(Z" + args + ")Z", false));
            if (cancelled == null) method.instructions.insert(hook);
            else method.instructions.insert(cancelled, hook);
        }
        if (methods != 1) throw new IllegalStateException("Potion impact hook drift: " + methods);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer); return writer.toByteArray();
    }
}
