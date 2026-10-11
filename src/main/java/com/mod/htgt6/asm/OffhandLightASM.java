package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Reuses Angelica's own item luminance and underwater rules; never replaces its existing sources. */
final class OffhandLightASM {
    private static final String ENTITY="Lnet/minecraft/entity/Entity;";
    private static final String STACK="Lnet/minecraft/item/ItemStack;";
    private static final String COMPAT="com/mod/htgt6/minecraft1202/Module/Client/Combat/OffhandLightCompat";
    static byte[] transform(byte[] bytes) {
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        for(MethodNode m:node.methods)if(m.name.equals("htgt6$offhandLight"))return bytes;
        int count=0;
        for(MethodNode m:node.methods) {
            if(!m.name.equals("getLuminanceFromEntity")||!m.desc.equals("("+ENTITY+")I"))continue;
            for(AbstractInsnNode insn:m.instructions.toArray())if(insn.getOpcode()==Opcodes.IRETURN) {
                InsnList hook=new InsnList();hook.add(new VarInsnNode(Opcodes.ALOAD,0));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,node.name,"htgt6$offhandLight","(I"+ENTITY+")I",false));
                m.instructions.insertBefore(insn,hook);count++;
            }
        }
        if(count==0)throw new IllegalStateException("Angelica entity luminance method not found");
        MethodNode helper=new MethodNode(Opcodes.ACC_PRIVATE|Opcodes.ACC_STATIC,"htgt6$offhandLight","(I"+ENTITY+")I",null,null);
        LabelNode present=new LabelNode();InsnList c=helper.instructions;
        c.add(new VarInsnNode(Opcodes.ALOAD,1));c.add(new MethodInsnNode(Opcodes.INVOKESTATIC,COMPAT,"stack","("+ENTITY+")"+STACK,false));
        c.add(new VarInsnNode(Opcodes.ASTORE,2));c.add(new VarInsnNode(Opcodes.ALOAD,2));c.add(new JumpInsnNode(Opcodes.IFNONNULL,present));
        c.add(new VarInsnNode(Opcodes.ILOAD,0));c.add(new InsnNode(Opcodes.IRETURN));
        c.add(present);c.add(new FrameNode(Opcodes.F_APPEND,1,new Object[]{"net/minecraft/item/ItemStack"},0,null));
        c.add(new VarInsnNode(Opcodes.ILOAD,0));c.add(new VarInsnNode(Opcodes.ALOAD,2));c.add(new VarInsnNode(Opcodes.ALOAD,1));
        c.add(new MethodInsnNode(Opcodes.INVOKESTATIC,COMPAT,"underwater","("+ENTITY+")Z",false));
        c.add(new MethodInsnNode(Opcodes.INVOKESTATIC,node.name,"getLuminanceFromItemStack","("+STACK+"Z)I",false));
        c.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"java/lang/Math","max","(II)I",false));c.add(new InsnNode(Opcodes.IRETURN));
        node.methods.add(helper);ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
}
