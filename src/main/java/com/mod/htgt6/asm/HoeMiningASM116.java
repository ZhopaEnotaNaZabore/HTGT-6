package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Adds missing Item overrides; existing transformer-provided overrides keep priority. */
public final class HoeMiningASM116 {
    private static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/Modern/HoeMining116";
    public static byte[] transform(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);boolean srg=false;for(MethodNode m:node.methods)if(m.name.equals("func_77648_a"))srg=true;
        add(node,"getDigSpeed","(Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/Block;I)F","speed");
        add(node,"getToolClasses","(Lnet/minecraft/item/ItemStack;)Ljava/util/Set;","classes");
        add(node,"getHarvestLevel","(Lnet/minecraft/item/ItemStack;Ljava/lang/String;)I","level");
        add(node,srg?"func_82789_a":"getIsRepairable","(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z","repair");
        add(node,srg?"func_77619_b":"getItemEnchantability","()I","enchantability");
        add(node,srg?"func_150894_a":"onBlockDestroyed","(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/block/Block;IIILnet/minecraft/entity/EntityLivingBase;)Z","destroy");
        add(node,srg?"func_77644_a":"hitEntity","(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/EntityLivingBase;)Z","hit");
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
    private static void add(ClassNode node,String name,String desc,String hook){for(MethodNode old:node.methods)if(old.name.equals(name)&&old.desc.equals(desc))return;MethodNode m=new MethodNode(Opcodes.ACC_PUBLIC,name,desc,null,null);m.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));int slot=1;for(Type t:Type.getArgumentTypes(desc)){m.instructions.add(new VarInsnNode(t.getOpcode(Opcodes.ILOAD),slot));slot+=t.getSize();}m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,hook,"(Lnet/minecraft/item/ItemHoe;"+desc.substring(1),false));m.instructions.add(new InsnNode(Type.getReturnType(desc).getOpcode(Opcodes.IRETURN)));node.methods.add(m);}
    private HoeMiningASM116(){}
}
