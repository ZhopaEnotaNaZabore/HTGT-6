package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Covers recipes synthesized on demand, including recipe-map subclasses. */
final class PlatinumGroupRecipeASM {
    static byte[] transformDirectProcessing(byte[] bytes, boolean washing) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        String hook = "com/mod/htgt6/common/handler/recipes/PlatinumGroupSludgeRecipes";
        String methodName = washing ? "washingMaterial" : "crucibleData";
        for (MethodNode method : node.methods) for (AbstractInsnNode i : method.instructions.toArray())
            if (i instanceof MethodInsnNode && hook.equals(((MethodInsnNode) i).owner)
                    && methodName.equals(((MethodInsnNode) i).name)) return bytes;
        int patched = 0;
        for (MethodNode method : node.methods) {
            if (washing && "onTickWorld".equals(method.name)) {
                InsnList call = new InsnList();
                call.add(new VarInsnNode(Opcodes.ALOAD, 2));
                call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, hook, methodName,
                        "(Lgregapi/oredict/OreDictMaterial;)Lgregapi/oredict/OreDictMaterial;", false));
                call.add(new VarInsnNode(Opcodes.ASTORE, 2));
                method.instructions.insert(call);
                patched++;
            } else if (!washing) for (AbstractInsnNode i : method.instructions.toArray()) {
                if (!(i instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) i;
                if ("gregapi/util/OM".equals(call.owner) && "anydata_".equals(call.name)) {
                    method.instructions.insert(call, new MethodInsnNode(Opcodes.INVOKESTATIC, hook, methodName,
                            "(Lgregapi/oredict/OreDictItemData;)Lgregapi/oredict/OreDictItemData;", false));
                    patched++;
                }
            }
        }
        if (patched != 1) throw new IllegalStateException("PGM direct processing hook drift: " + patched);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    static byte[] transform(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        String hook = "com/mod/htgt6/common/handler/recipes/PlatinumGroupSludgeRecipes";
        for (MethodNode method : node.methods)
            for (AbstractInsnNode i : method.instructions.toArray())
                if (i instanceof MethodInsnNode && hook.equals(((MethodInsnNode) i).owner)) return bytes;
        int count = 0;
        for (MethodNode method : node.methods) if ("<init>".equals(method.name)) {
            for (AbstractInsnNode i : method.instructions.toArray()) if (i.getOpcode() == Opcodes.RETURN) {
                InsnList call = new InsnList();
                call.add(new VarInsnNode(Opcodes.ALOAD, 0));
                call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, hook, "normalize", "(Lgregapi/recipes/Recipe;)Z", false));
                call.add(new InsnNode(Opcodes.POP));
                method.instructions.insertBefore(i, call);
                count++;
            }
        }
        if (count == 0) throw new IllegalStateException("GT6 Recipe constructor hook drift");
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
