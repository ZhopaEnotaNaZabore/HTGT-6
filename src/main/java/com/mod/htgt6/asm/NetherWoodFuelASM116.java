package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Filter every return, including early vanilla fuel matches and Forge fuel handlers. */
public final class NetherWoodFuelASM116 {
    private static final String OWNER="com/mod/htgt6/minecraft1202/Module/Gameplay/Modern/NetherWoodMaterial116";
    public static byte[] transform(byte[] bytes){
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int methods=0;
        for(MethodNode method:node.methods){
            if(!(method.name.equals("getItemBurnTime")||method.name.equals("func_145952_a"))||!method.desc.equals("(Lnet/minecraft/item/ItemStack;)I"))continue;
            methods++;
            for(AbstractInsnNode n:method.instructions.toArray())if(n instanceof MethodInsnNode&&((MethodInsnNode)n).owner.equals(OWNER))return bytes;
            for(AbstractInsnNode n:method.instructions.toArray())if(n.getOpcode()==Opcodes.IRETURN){
                InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));
                code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,OWNER,"fuelTime","(ILnet/minecraft/item/ItemStack;)I",false));method.instructions.insertBefore(n,code);
            }
        }
        if(methods!=1)throw new IllegalStateException("Nether wood fuel hook drift: "+methods);
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
    private NetherWoodFuelASM116(){}
}
