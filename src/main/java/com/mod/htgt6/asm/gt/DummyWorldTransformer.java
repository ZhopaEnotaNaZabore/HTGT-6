package com.mod.htgt6.asm.gt;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class DummyWorldTransformer implements Opcodes {

    public static byte[] transform(byte[] basicClass) {

        try {

            ClassNode cn = new ClassNode();
            new ClassReader(basicClass).accept(cn, 0);

            // =========================================================
            // 1. REMOVE WORLD INHERITANCE (CRITICAL FIX)
            // =========================================================
            cn.superName = "java/lang/Object";

            // =========================================================
            // 2. REMOVE ALL EXISTING METHODS (we rebuild safe ones)
            // =========================================================
            cn.methods.clear();

            // =========================================================
            // 3. SAFE CONSTRUCTOR (NO WORLD CALLS)
            // =========================================================
            MethodNode ctor = new MethodNode(
                    ACC_PUBLIC,
                    "<init>",
                    "()V",
                    null,
                    null
            );

            InsnList ctorInsns = new InsnList();
            ctorInsns.add(new VarInsnNode(ALOAD, 0));
            ctorInsns.add(new MethodInsnNode(
                    INVOKESPECIAL,
                    "java/lang/Object",
                    "<init>",
                    "()V",
                    false
            ));
            ctorInsns.add(new InsnNode(RETURN));

            ctor.instructions = ctorInsns;
            cn.methods.add(ctor);

            // =========================================================
            // 4. SAFE getBiomeGenForCoords (NO WORLDGEN)
            // =========================================================
            MethodNode biomeMethod = new MethodNode(
                    ACC_PUBLIC,
                    "getBiomeGenForCoords",
                    "(II)Lnet/minecraft/world/biome/BiomeGenBase;",
                    null,
                    null
            );

            InsnList biomeInsns = new InsnList();
            biomeInsns.add(new FieldInsnNode(
                    GETSTATIC,
                    "net/minecraft/world/biome/BiomeGenBase",
                    "plains",
                    "Lnet/minecraft/world/biome/BiomeGenBase;"
            ));
            biomeInsns.add(new InsnNode(ARETURN));

            biomeMethod.instructions = biomeInsns;
            cn.methods.add(biomeMethod);

            // =========================================================
            // 5. WRITE CLASS BACK
            // =========================================================
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);

            return cw.toByteArray();

        } catch (Throwable t) {

            t.printStackTrace();
            return basicClass;
        }
    }
}