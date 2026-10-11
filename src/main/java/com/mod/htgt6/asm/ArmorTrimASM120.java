package com.mod.htgt6.asm;
import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
/** Extends Forge's existing texture hook, retaining custom armor models and materials. */
public final class ArmorTrimASM120 {
    public static byte[] transform(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
        for(MethodNode method:node.methods)if("getArmorTexture".equals(method.name)){
            for(AbstractInsnNode ins:method.instructions.toArray())if(ins.getOpcode()==Opcodes.ARETURN){
                InsnList hook=new InsnList();hook.add(new VarInsnNode(Opcodes.ALOAD,1));hook.add(new VarInsnNode(Opcodes.ILOAD,3));hook.add(new VarInsnNode(Opcodes.ALOAD,4));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/mod/htgt6/minecraft1202/Module/Client/VillageAndPillage/ArmorTrimTexture120","texture","(Ljava/lang/String;Lnet/minecraft/item/ItemStack;ILjava/lang/String;)Ljava/lang/String;",false));
                method.instructions.insertBefore(ins,hook);count++;
            }
        }
        if(count==0)throw new IllegalStateException("Forge armor texture hook missing");
        ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);return out.toByteArray();
    }
}
