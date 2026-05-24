package com.mod.htgt6.asm.transformers;



import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class BiomeCacheTransformer implements Opcodes {

    public static byte[] transform(byte[] basicClass) {

        System.out.println(
                "[HTGT6 ASM] Patching BiomeCache"
        );

        ClassNode cn = new ClassNode();

        new ClassReader(basicClass).accept(cn, 0);

        for (FieldNode fn : cn.fields) {

            /*
             * byte[] -> int[]
             */

            if (fn.desc.equals("[B")) {

                fn.desc = "[I";
            }
        }

        for (MethodNode mn : cn.methods) {

            if (mn.desc.contains("[B")) {

                mn.desc =
                        mn.desc.replace("[B", "[I");
            }
        }

        ClassWriter cw =
                new ClassWriter(ClassWriter.COMPUTE_MAXS);

        cn.accept(cw);

        return cw.toByteArray();
    }
}