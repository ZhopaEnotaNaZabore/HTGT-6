package com.mod.htgt6.asm.transformers;



import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class PacketBufferTransformer implements Opcodes {

    public static byte[] transform(byte[] basicClass) {

        System.out.println(
                "[HTGT6 ASM] Patching PacketBuffer"
        );

        ClassNode cn = new ClassNode();

        new ClassReader(basicClass).accept(cn, 0);

        for (MethodNode mn : cn.methods) {

            for (AbstractInsnNode ain :
                    mn.instructions.toArray()) {

                /*
                 * writeByte -> writeInt
                 */

                if (ain instanceof MethodInsnNode) {

                    MethodInsnNode min =
                            (MethodInsnNode) ain;

                    if (min.name.equals("writeByte")) {

                        min.name = "writeInt";
                        min.desc = "(I)Lio/netty/buffer/ByteBuf;";
                    }

                    /*
                     * readByte -> readInt
                     */

                    if (min.name.equals("readByte")) {

                        min.name = "readInt";
                        min.desc = "()I";
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
