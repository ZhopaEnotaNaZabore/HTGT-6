package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Runs after vanilla source removal, before landing or timeout can drop the block. */
final class ConcreteFallingASM {
    static byte[] transform(byte[] bytes) {
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        int count=0;
        for(MethodNode method:node.methods) {
            if(!(method.name.equals("onUpdate")||method.name.equals("func_70071_h_"))||!method.desc.equals("()V"))continue;
            for(AbstractInsnNode insn:method.instructions.toArray()) {
                if(!(insn instanceof FieldInsnNode))continue;
                FieldInsnNode field=(FieldInsnNode)insn;
                if(field.getOpcode()!=Opcodes.GETFIELD||!field.desc.equals("Z")
                        ||!(field.name.equals("onGround")||field.name.equals("field_70122_E")))continue;
                AbstractInsnNode receiver=field.getPrevious();
                if(!(receiver instanceof VarInsnNode)||receiver.getOpcode()!=Opcodes.ALOAD||((VarInsnNode)receiver).var!=0)
                    throw new IllegalStateException("Unexpected falling block landing receiver");
                LabelNode proceed=new LabelNode();InsnList hook=new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD,0));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/mod/htgt6/minecraft1202/Module/Blocks/Modern/BlockBackport120ConcretePowder",
                        "hardenFalling","(Lnet/minecraft/entity/item/EntityFallingBlock;)Z",false));
                hook.add(new JumpInsnNode(Opcodes.IFEQ,proceed));hook.add(new InsnNode(Opcodes.RETURN));
                hook.add(proceed);hook.add(new FrameNode(Opcodes.F_SAME,0,null,0,null));
                method.instructions.insertBefore(receiver,hook);count++;
            }
        }
        if(count!=1)throw new IllegalStateException("Concrete falling anchors: "+count);
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
}
