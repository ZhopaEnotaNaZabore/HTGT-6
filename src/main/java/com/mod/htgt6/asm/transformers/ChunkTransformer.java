package com.mod.htgt6.asm.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class ChunkTransformer implements Opcodes {

    /*
     * Vanilla:
     *
     * private byte[] blockBiomeArray;
     *
     * Patched:
     *
     * private short[] blockBiomeArray;
     */

    public static byte[] transform(byte[] basicClass) {

        System.out.println("[HTGT6 ASM] Patching Chunk");

        ClassNode cn = new ClassNode();

        ClassReader cr = new ClassReader(basicClass);
        cr.accept(cn, 0);

        patchBiomeField(cn);

        patchBiomeMethods(cn);

        ClassWriter cw =
                new ClassWriter(ClassWriter.COMPUTE_MAXS);

        cn.accept(cw);

        return cw.toByteArray();
    }

    private static void patchBiomeField(ClassNode cn) {

        for (FieldNode fn : cn.fields) {

            /*
             * Vanilla:
             * [B
             *
             * Patched:
             * [S
             */

            if (fn.desc.equals("[B")) {

                if (fn.name.equals("blockBiomeArray")
                        || fn.name.equals("field_76651_r")) {

                    System.out.println(
                            "[HTGT6 ASM] Rewriting biome field"
                    );

                    fn.desc = "[S";
                }
            }
        }
    }

    private static void patchBiomeMethods(ClassNode cn) {

        for (MethodNode mn : cn.methods) {

            /*
             * Rewrite method descriptors
             */

            if (mn.desc.contains("[B")) {

                mn.desc = mn.desc.replace("[B", "[S");
            }

            /*
             * Rewrite byte array instructions
             */

            for (AbstractInsnNode ain :
                    mn.instructions.toArray()) {

                /*
                 * NEWARRAY T_BYTE
                 */

                if (ain instanceof IntInsnNode) {

                    IntInsnNode iin =
                            (IntInsnNode) ain;

                    if (iin.getOpcode() == NEWARRAY
                            && iin.operand == T_BYTE) {

                        System.out.println(
                                "[HTGT6 ASM] Replacing T_BYTE -> T_SHORT"
                        );

                        iin.operand = T_SHORT;
                    }
                }

                /*
                 * BALOAD -> SALOAD
                 */

                if (ain.getOpcode() == BALOAD) {

                    mn.instructions.set(
                            ain,
                            new InsnNode(SALOAD)
                    );
                }

                /*
                 * BASTORE -> SASTORE
                 */

                if (ain.getOpcode() == BASTORE) {

                    mn.instructions.set(
                            ain,
                            new InsnNode(SASTORE)
                    );
                }

                /*
                 * GETFIELD [B -> [S
                 */

                if (ain instanceof FieldInsnNode) {

                    FieldInsnNode fin =
                            (FieldInsnNode) ain;

                    if (fin.desc.equals("[B")) {

                        if (fin.name.equals("blockBiomeArray")
                                || fin.name.equals("field_76651_r")) {

                            fin.desc = "[S";
                        }
                    }
                }

                /*
                 * CHECKCAST [B -> [S
                 */

                if (ain instanceof TypeInsnNode) {

                    TypeInsnNode tin =
                            (TypeInsnNode) ain;

                    if (tin.desc.equals("[B")) {

                        tin.desc = "[S";
                    }
                }

                /*
                 * byte[] signatures in method invokes
                 */

                if (ain instanceof MethodInsnNode) {

                    MethodInsnNode min =
                            (MethodInsnNode) ain;

                    if (min.desc.contains("[B")) {

                        min.desc =
                                min.desc.replace("[B", "[S");
                    }
                }
            }
        }
    }
}