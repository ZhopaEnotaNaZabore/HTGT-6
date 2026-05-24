package com.mod.htgt6.asm.transformers;



import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class WorldChunkManagerTransformer implements Opcodes {

    public static byte[] transform(byte[] basicClass) {

        System.out.println(
                "[HTGT6 ASM] Patching WorldChunkManager"
        );

        ClassNode cn = new ClassNode();

        new ClassReader(basicClass).accept(cn, 0);

        for (MethodNode mn : cn.methods) {

            for (AbstractInsnNode ain :
                    mn.instructions.toArray()) {

                /*
                 * Remove biomeID & 255
                 */

                if (ain.getOpcode() == SIPUSH) {

                    IntInsnNode iin =
                            (IntInsnNode) ain;

                    if (iin.operand == 255) {

                        mn.instructions.remove(iin);

                        System.out.println(
                                "[HTGT6 ASM] Removed biome mask"
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
