package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Replaces only the legacy surface refill, preserving depletion and bubble-column hooks. */
final class AquaticBreathingASM {
    static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/Aquatic/PlayerBreathing120";
    static byte[] transform(byte[] bytes) {
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        int count=0;
        for(MethodNode method:node.methods) {
            if(!method.desc.equals("()V")||!(method.name.equals("onEntityUpdate")||method.name.equals("func_70030_z")))continue;
            for(AbstractInsnNode insn:method.instructions.toArray()) {
                if(!(insn instanceof MethodInsnNode))continue;
                MethodInsnNode call=(MethodInsnNode)insn;
                if(call.owner.equals(HOOK))return bytes;
                if(call.getOpcode()!=Opcodes.INVOKEVIRTUAL||!call.desc.equals("(I)V")
                        ||!(call.name.equals("setAir")||call.name.equals("func_70050_g")))continue;
                AbstractInsnNode value=insn.getPrevious();
                while(value!=null&&value.getOpcode()<0)value=value.getPrevious();
                boolean vanilla=value instanceof IntInsnNode&&((IntInsnNode)value).operand==300;
                boolean crucible=value instanceof FieldInsnNode&&value.getOpcode()==Opcodes.GETFIELD
                        &&((FieldInsnNode)value).name.equals("maxAirTicks")&&((FieldInsnNode)value).desc.equals("I")
                        &&((FieldInsnNode)value).owner.equals(node.name);
                if(!vanilla&&!crucible)continue;
                InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));
                code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"surfaceAir",
                        "(ILnet/minecraft/entity/EntityLivingBase;)I",false));
                method.instructions.insertBefore(insn,code);count++;
            }
        }
        if(count!=1)throw new IllegalStateException("Aquatic surface breathing hook drift: "+count);
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
}
