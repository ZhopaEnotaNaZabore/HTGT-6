package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.apache.logging.log4j.LogManager;

/** Optional bytecode-only integration: no SkinPort classes are linked by HiTech6. */
public final class SkinPortRenderFixASM implements Opcodes {
    public static final String TARGET = "lain.mods.skinport.init.forge.ForgeSkinPort$DefaultSkinProvider";
    private static final String SKIN = "lain/mods/skins/api/interfaces/ISkin";
    private static final String DATA = "lain/mods/skins/impl/SkinData";
    private static final String COPY = "htgt6$copyDefaultSkin";

    public static byte[] transform(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        for (MethodNode method : node.methods) if (COPY.equals(method.name)) return bytes;
        MethodNode target = null;
        for (MethodNode method : node.methods)
            if ("getSkin".equals(method.name) && ("(Llain/mods/skins/api/interfaces/IPlayerProfile;)L" + SKIN + ";").equals(method.desc)) target = method;
        if (target == null) {
            LogManager.getLogger("HTGT6-ASM").warn("SkinPort default provider signature not recognized; skipping render fix");
            return bytes;
        }
        int returns = 0;
        for (AbstractInsnNode instruction : target.instructions.toArray()) {
            if (instruction.getOpcode() == ARETURN) {
                target.instructions.insertBefore(instruction, new MethodInsnNode(INVOKESTATIC, node.name, COPY,
                        "(L" + SKIN + ";)L" + SKIN + ";", false));
                returns++;
            }
        }
        if (returns == 0) return bytes;
        MethodNode copy = new MethodNode(ACC_PRIVATE | ACC_STATIC, COPY, "(L" + SKIN + ";)L" + SKIN + ";", null, null);
        // Preserve null/unready fallbacks without introducing a new failure during provider startup.
        Label ready = new Label();
        copy.visitVarInsn(ALOAD, 0);
        copy.visitJumpInsn(IFNONNULL, ready);
        copy.visitInsn(ACONST_NULL);
        copy.visitInsn(ARETURN);
        copy.visitLabel(ready);
        copy.visitFrame(F_SAME, 0, null, 0, null);
        copy.visitVarInsn(ALOAD, 0);
        copy.visitMethodInsn(INVOKEINTERFACE, SKIN, "getData", "()Ljava/nio/ByteBuffer;", true);
        copy.visitVarInsn(ASTORE, 1);
        Label present = new Label();
        copy.visitVarInsn(ALOAD, 1);
        copy.visitJumpInsn(IFNONNULL, present);
        copy.visitInsn(ACONST_NULL);
        copy.visitInsn(ARETURN);
        copy.visitLabel(present);
        copy.visitFrame(F_APPEND, 1, new Object[]{"java/nio/ByteBuffer"}, 0, null);
        copy.visitVarInsn(ALOAD, 1);
        copy.visitMethodInsn(INVOKEVIRTUAL, "java/nio/ByteBuffer", "duplicate", "()Ljava/nio/ByteBuffer;", false);
        copy.visitVarInsn(ASTORE, 1);
        copy.visitVarInsn(ALOAD, 1);
        copy.visitMethodInsn(INVOKEVIRTUAL, "java/nio/ByteBuffer", "remaining", "()I", false);
        copy.visitIntInsn(NEWARRAY, T_BYTE);
        copy.visitVarInsn(ASTORE, 2);
        copy.visitVarInsn(ALOAD, 1);
        copy.visitVarInsn(ALOAD, 2);
        copy.visitMethodInsn(INVOKEVIRTUAL, "java/nio/ByteBuffer", "get", "([B)Ljava/nio/ByteBuffer;", false);
        copy.visitInsn(POP);
        copy.visitTypeInsn(NEW, DATA);
        copy.visitInsn(DUP);
        copy.visitMethodInsn(INVOKESPECIAL, DATA, "<init>", "()V", false);
        copy.visitInsn(DUP);
        copy.visitVarInsn(ALOAD, 2);
        copy.visitVarInsn(ALOAD, 0);
        copy.visitMethodInsn(INVOKEINTERFACE, SKIN, "getSkinType", "()Ljava/lang/String;", true);
        copy.visitMethodInsn(INVOKEVIRTUAL, DATA, "put", "([BLjava/lang/String;)V", false);
        copy.visitInsn(ARETURN);
        node.methods.add(copy);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        LogManager.getLogger("HTGT6-ASM").info("Applied FixSkinPortBrokenRender: isolated default skins per profile");
        return writer.toByteArray();
    }
}
