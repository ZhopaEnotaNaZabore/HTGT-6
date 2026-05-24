package com.mod.htgt6.asm.transformers;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class AnvilChunkLoaderTransformer implements Opcodes {

    public static byte[] transform(byte[] basicClass) {

        System.out.println("[HTGT6 ASM] Patching AnvilChunkLoader");

        ClassNode cn = new ClassNode();

        new ClassReader(basicClass).accept(cn, 0);

        for (MethodNode mn : cn.methods) {

            for (AbstractInsnNode ain :
                    mn.instructions.toArray()) {

                /*
                 * setByteArray -> setIntArray
                 */

                if (ain instanceof MethodInsnNode) {

                    MethodInsnNode min =
                            (MethodInsnNode) ain;

                    if (min.name.equals("setByteArray")) {

                        min.name = "setIntArray";

                        min.desc =
                                "(Ljava/lang/String;[I)V";

                        System.out.println(
                                "[HTGT6 ASM] setByteArray -> setIntArray"
                        );
                    }

                    /*
                     * getByteArray -> getIntArray
                     */

                    if (min.name.equals("getByteArray")) {

                        min.name = "getIntArray";

                        min.desc =
                                "(Ljava/lang/String;)[I";

                        System.out.println(
                                "[HTGT6 ASM] getByteArray -> getIntArray"
                        );
                    }
                }

                /*
                 * [B -> [I
                 */

                if (ain instanceof FieldInsnNode) {

                    FieldInsnNode fin =
                            (FieldInsnNode) ain;

                    if (fin.desc.equals("[B")) {
                        fin.desc = "[I";
                    }
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