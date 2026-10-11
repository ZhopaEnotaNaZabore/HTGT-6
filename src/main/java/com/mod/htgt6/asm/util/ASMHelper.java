package com.mod.htgt6.asm.util;



import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public final class ASMHelper {

    private ASMHelper() {}

    public static ClassNode read(byte[] bytes) {
        ClassNode cn = new ClassNode();
        new ClassReader(bytes).accept(cn, 0);
        return cn;
    }

    public static byte[] write(ClassNode cn) {

        ClassWriter cw = new ClassWriter(
                ClassWriter.COMPUTE_MAXS |
                        ClassWriter.COMPUTE_FRAMES) {

            @Override
            protected String getCommonSuperClass(String a, String b) {
                return "java/lang/Object";
            }
        };

        cn.accept(cw);
        return cw.toByteArray();
    }

}