package com.mod.htgt6.asm;
import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
/** Augment false fluid results, retaining the complete original query and flow path. */
public final class WaterloggedFluidASM {
 private static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/Shared/WaterloggedFluid150";
 public static byte[] transformWorld(byte[]bytes){return transform(bytes,false);}
 public static byte[] transformEntity(byte[]bytes){return transform(bytes,true);}
 private static byte[] transform(byte[]bytes,boolean entity){
  ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int methods=0;
  for(MethodNode m:node.methods){
   boolean eyes=entity&&(m.name.equals("isInsideOfMaterial")||m.name.equals("func_70055_a"))&&m.desc.equals("(Lnet/minecraft/block/material/Material;)Z");
   boolean material=!entity&&(m.name.equals("handleMaterialAcceleration")||m.name.equals("func_72918_a")||m.name.equals("isMaterialInBB")||m.name.equals("func_72875_a")||m.name.equals("isAABBInMaterial")||m.name.equals("func_72830_b"))&&m.desc.startsWith("(Lnet/minecraft/util/AxisAlignedBB;Lnet/minecraft/block/material/Material;")&&m.desc.endsWith(")Z");
   boolean any=!entity&&(m.name.equals("isAnyLiquid")||m.name.equals("func_72953_d"))&&m.desc.equals("(Lnet/minecraft/util/AxisAlignedBB;)Z");
   if(!eyes&&!material&&!any)continue;methods++;boolean installed=false;
   for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(HOOK))installed=true;
   if(installed)continue;
   for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==Opcodes.IRETURN){
    InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new VarInsnNode(Opcodes.ALOAD,1));
    if(material)code.add(new VarInsnNode(Opcodes.ALOAD,2));
    String method=eyes?"eyes":any?"anyLiquid":"material";
    String desc=eyes?"(ZLnet/minecraft/entity/Entity;Lnet/minecraft/block/material/Material;)Z":any?"(ZLnet/minecraft/world/World;Lnet/minecraft/util/AxisAlignedBB;)Z":"(ZLnet/minecraft/world/World;Lnet/minecraft/util/AxisAlignedBB;Lnet/minecraft/block/material/Material;)Z";
    code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,method,desc,false));m.instructions.insertBefore(i,code);
   }
  }
  if(methods!=(entity?1:4))throw new IllegalStateException("Waterlogged query hooks drift: "+methods+", entity="+entity);
  ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);return out.toByteArray();
 }
 private WaterloggedFluidASM(){}
}
