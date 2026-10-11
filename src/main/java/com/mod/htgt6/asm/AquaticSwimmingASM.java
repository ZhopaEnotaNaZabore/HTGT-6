package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class AquaticSwimmingASM {
    static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/Aquatic/PlayerSwimming120";
    static byte[] transform(byte[] bytes) {
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0,gravityCount=0;
        for(MethodNode method:node.methods) {
            boolean srg=method.name.equals("func_70612_e");
            if(!method.desc.equals("(FF)V")||!(srg||method.name.equals("moveEntityWithHeading")))continue;
            for(AbstractInsnNode insn:method.instructions.toArray())
                if(insn instanceof MethodInsnNode&&((MethodInsnNode)insn).owner.equals(HOOK))return bytes;
            InsnList hook=new InsnList();hook.add(new VarInsnNode(Opcodes.ALOAD,0));hook.add(new VarInsnNode(Opcodes.ALOAD,0));
            hook.add(new FieldInsnNode(Opcodes.GETFIELD,node.name,srg?"field_70703_bu":"isJumping","Z"));
            hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"beforeTravel","(Lnet/minecraft/entity/EntityLivingBase;Z)V",false));
            method.instructions.insert(hook);count++;
            for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof LdcInsnNode&&Double.valueOf(.02D).equals(((LdcInsnNode)insn).cst)){
                InsnList gravity=new InsnList();gravity.add(new VarInsnNode(Opcodes.ALOAD,0));gravity.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"waterGravity","(DLnet/minecraft/entity/EntityLivingBase;)D",false));method.instructions.insert(insn,gravity);gravityCount++;break;
            }
        }
        if(count!=1||gravityCount!=1)throw new IllegalStateException("Aquatic swimming travel/gravity hook drift: "+count+"/"+gravityCount);
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
}
