package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class ParrotRecordASM {
    static byte[] transform(byte[] bytes){
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;boolean changed=false;
        for(MethodNode method:node.methods){
            if(!(method.name.equals("playRecord")||method.name.equals("func_72702_a"))
                    ||!method.desc.equals("(Ljava/lang/String;III)V"))continue;
            count++;
            int existing=0;
            for(AbstractInsnNode ins:method.instructions.toArray())if(ins instanceof MethodInsnNode){
                MethodInsnNode call=(MethodInsnNode)ins;
                if(call.getOpcode()==Opcodes.INVOKESTATIC
                        &&call.owner.equals("com/mod/htgt6/minecraft1202/Module/Client/Modern/ParrotRecord120")
                        &&call.name.equals("playRecord")&&call.desc.equals("(Ljava/lang/String;III)V"))existing++;
            }
            if(existing>1)throw new IllegalStateException("Duplicate parrot record hooks: "+existing);
            if(existing==1)continue;
            InsnList hook=new InsnList();hook.add(new VarInsnNode(Opcodes.ALOAD,1));
            for(int slot=2;slot<=4;slot++)hook.add(new VarInsnNode(Opcodes.ILOAD,slot));
            hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "com/mod/htgt6/minecraft1202/Module/Client/Modern/ParrotRecord120","playRecord","(Ljava/lang/String;III)V",false));
            method.instructions.insert(hook);changed=true;
        }
        if(count!=1)throw new IllegalStateException("Parrot record anchors: "+count);
        if(!changed)return bytes;
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
}
