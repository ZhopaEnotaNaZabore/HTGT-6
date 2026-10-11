package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Keep Forge's mutable catch lists; only adapt category selection and water predicate. */
public final class OpenWaterFishingASM116 {
 private static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/Modern/OpenWaterFishing116";
 public static byte[] transform(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);for(MethodNode m:node.methods)for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(HOOK))return bytes;
  int roll=0,tick=0;
  for(MethodNode m:node.methods){
   if(m.name.equals("func_146033_f")&&m.desc.equals("()Lnet/minecraft/item/ItemStack;")){m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();m.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"roll","(Lnet/minecraft/entity/projectile/EntityFishHook;)Lnet/minecraft/item/ItemStack;",false));m.instructions.add(new InsnNode(Opcodes.ARETURN));roll++;}
   if((m.name.equals("onUpdate")||m.name.equals("func_70071_h_"))&&m.desc.equals("()V")){
    for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)i;if(call.owner.equals("net/minecraft/util/MathHelper")&&(call.name.equals("getRandomIntegerInRange")||call.name.equals("func_76136_a"))&&call.desc.equals("(Ljava/util/Random;II)I")){AbstractInsnNode hi=previous(i),lo=previous(hi);if(integer(lo)==100&&integer(hi)==900)m.instructions.set(hi,new IntInsnNode(Opcodes.SIPUSH,600));else if(integer(lo)==10&&integer(hi)==30){m.instructions.set(lo,new IntInsnNode(Opcodes.BIPUSH,20));m.instructions.set(hi,new IntInsnNode(Opcodes.BIPUSH,40));}}}
    for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==Opcodes.RETURN){InsnList hook=new InsnList();hook.add(new VarInsnNode(Opcodes.ALOAD,0));for(String field:new String[]{"field_146045_ax","field_146038_az"}){hook.add(new VarInsnNode(Opcodes.ALOAD,0));hook.add(new FieldInsnNode(Opcodes.GETFIELD,node.name,field,"I"));}hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"tick","(Lnet/minecraft/entity/projectile/EntityFishHook;II)V",false));m.instructions.insertBefore(i,hook);}tick++;
   }
  }
  if(roll!=1||tick!=1)throw new IllegalStateException("Fishing adapter layout "+roll+"/"+tick);ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
 }
 private static AbstractInsnNode previous(AbstractInsnNode n){do{n=n.getPrevious();}while(n!=null&&n.getOpcode()<0);return n;}
 private static int integer(AbstractInsnNode n){if(n instanceof IntInsnNode)return ((IntInsnNode)n).operand;if(n instanceof LdcInsnNode&&((LdcInsnNode)n).cst instanceof Integer)return (Integer)((LdcInsnNode)n).cst;return Integer.MIN_VALUE;}
 private OpenWaterFishingASM116(){}
}
