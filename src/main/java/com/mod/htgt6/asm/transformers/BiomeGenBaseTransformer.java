package com.mod.htgt6.asm.transformers;



import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class BiomeGenBaseTransformer {

    public static byte[] transform(byte[] basicClass) {

        ClassNode cn = new ClassNode();

        new ClassReader(basicClass).accept(cn, 0);

        for (MethodNode mn : cn.methods) {

            for (AbstractInsnNode ain : mn.instructions.toArray()) {

                if (ain instanceof IntInsnNode) {

                    IntInsnNode iin = (IntInsnNode) ain;

                    if (iin.operand == 256) {

                        iin.operand = 65536;

                        System.out.println(
                                "[HTGT6 ASM] Expanded biome cap"
                        );
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