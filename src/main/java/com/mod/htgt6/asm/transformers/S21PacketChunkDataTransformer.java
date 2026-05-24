package com.mod.htgt6.asm.transformers;


import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class S21PacketChunkDataTransformer implements Opcodes {

    public static byte[] transform(byte[] basicClass) {

        System.out.println(
                "[HTGT6 ASM] Patching S21PacketChunkData"
        );

        ClassNode cn = new ClassNode();

        new ClassReader(basicClass).accept(cn, 0);

        for (MethodNode mn : cn.methods) {

            for (AbstractInsnNode ain :
                    mn.instructions.toArray()) {

                /*
                 * byte[] -> int[]
                 */

                if (ain instanceof MethodInsnNode) {

                    MethodInsnNode min =
                            (MethodInsnNode) ain;

                    if (min.desc.contains("[B")) {

                        min.desc =
                                min.desc.replace("[B", "[I");
                    }
                }

                /*
                 * BALOAD -> IALOAD
                 */

                if (ain.getOpcode() == BALOAD) {

                    mn.instructions.set(
                            ain,
                            new InsnNode(IALOAD)
                    );
                }

                /*
                 * BASTORE -> IASTORE
                 */

                if (ain.getOpcode() == BASTORE) {

                    mn.instructions.set(
                            ain,
                            new InsnNode(IASTORE)
                    );
                }
            }

            if (mn.desc.contains("[B")) {
                mn.desc = mn.desc.replace("[B", "[I");
            }
        }

        ClassWriter cw =
                new ClassWriter(ClassWriter.COMPUTE_MAXS);

        cn.accept(cw);

        return cw.toByteArray();
    }
}