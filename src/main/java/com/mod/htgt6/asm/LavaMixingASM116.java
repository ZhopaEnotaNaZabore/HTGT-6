package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class LavaMixingASM116 {
    public static byte[] transform(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
        for(MethodNode method:node.methods)if(method.name.equals("func_149805_n")&&method.desc.equals("(Lnet/minecraft/world/World;III)V")){
            count++;method.instructions.clear();method.tryCatchBlocks.clear();if(method.localVariables!=null)method.localVariables.clear();
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
            for(int i=2;i<=4;i++)method.instructions.add(new VarInsnNode(Opcodes.ILOAD,i));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/mod/htgt6/minecraft1202/Module/Gameplay/Modern/LavaMixing116","mix","(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;III)V",false));method.instructions.add(new InsnNode(Opcodes.RETURN));
        }
        if(count!=1)throw new IllegalStateException("Liquid mixing hook drift: "+count);
        ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);return out.toByteArray();
    }
    private LavaMixingASM116(){}
}
