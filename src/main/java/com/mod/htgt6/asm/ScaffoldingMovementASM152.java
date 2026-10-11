package com.mod.htgt6.asm;
import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
public final class ScaffoldingMovementASM152 {
 private static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/VillageAndPillage/ScaffoldingMovement152";
 private ScaffoldingMovementASM152(){}
 public static byte[] transform(byte[] bytes){
  ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
  for(MethodNode m:node.methods)if((m.name.equals("moveEntityWithHeading")||m.name.equals("func_70612_e"))&&m.desc.equals("(FF)V")){
   for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(HOOK))return bytes;
   InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new FieldInsnNode(Opcodes.GETFIELD,node.name,m.name.equals("func_70612_e")?"field_70703_bu":"isJumping","Z"));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"beforeTravel","(Lnet/minecraft/entity/EntityLivingBase;Z)V",false));m.instructions.insert(code);count++;
  }
  if(count!=1)throw new IllegalStateException("Scaffolding travel drift: "+count);ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(w);return w.toByteArray();
 }
}
