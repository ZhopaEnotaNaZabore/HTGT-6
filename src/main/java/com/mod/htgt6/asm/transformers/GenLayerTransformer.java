package com.mod.htgt6.asm.transformers;



import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class GenLayerTransformer implements Opcodes {

    public static byte[] transform(byte[] basicClass) {

        System.out.println(
                "[HTGT6 ASM] Patching GenLayer"
        );

        ClassNode cn = new ClassNode();

        new ClassReader(basicClass).accept(cn, 0);

        for (MethodNode mn : cn.methods) {

            for (AbstractInsnNode ain :
                    mn.instructions.toArray()) {

                /*
                 * remove biome hardcaps
                 */

                if (ain instanceof IntInsnNode) {

                    IntInsnNode iin =
                            (IntInsnNode) ain;

                    if (iin.operand == 255
                            || iin.operand == 256) {

                        mn.instructions.remove(iin);
                    }
                }
            }
        }

        ClassWriter cw =
                new ClassWriter(ClassWriter.COMPUTE_MAXS);

        cn.accept(cw);

        return cw.toByteArray();
    }
}