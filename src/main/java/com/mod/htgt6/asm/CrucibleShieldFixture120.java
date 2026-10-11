package com.mod.htgt6.asm;
import java.lang.reflect.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
public final class CrucibleShieldFixture120 {
 private static final String PLAYER="net/minecraft/entity/player/EntityPlayer";
 public static void main(String[]args)throws Exception {
  Method player=HTGT6Transformer.class.getDeclaredMethod("transformShieldPlayerApi",byte[].class,boolean.class);player.setAccessible(true);
  Method modifier=HTGT6Transformer.class.getDeclaredMethod("transformCrucibleShieldModifier",byte[].class);modifier.setAccessible(true);
  byte[] bridge=make(true,true,"func_70632_aY");
  player.invoke(null,bridge,false);
  try{player.invoke(null,make(true,false,"func_70632_aY"),false);throw new AssertionError("unknown missing hook accepted");}
  catch(InvocationTargetException e){if(!(e.getCause() instanceof IllegalStateException))throw e;}
  for(String name:new String[]{"isBlocking","func_70632_aY"}) {
   byte[] output=(byte[])modifier.invoke(null,(Object)make(false,true,name));
   ClassNode node=new ClassNode();new ClassReader(output).accept(node,0);int count=0;
   for(Object value:node.methods){MethodNode m=(MethodNode)value;new Analyzer(new BasicInterpreter()).analyze(node.name,m);
    for(AbstractInsnNode i=m.instructions.getFirst();i!=null;i=i.getNext())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).name.equals("usesLegacyDamageBlock"))count++;
   }
   if(count!=1)throw new AssertionError("modifier hook count "+count);
   if(!java.util.Arrays.equals(output,(byte[])modifier.invoke(null,(Object)output)))throw new AssertionError("idempotence");
   byte[] source=make(false,true,name);
   byte[] routed=new HTGT6Transformer().transform("net.minecraft.entity.EntityLivingBase$2","net.minecraft.entity.EntityLivingBase$2",source);
   ClassNode routedNode=new ClassNode();new ClassReader(routed).accept(routedNode,0);
   if(java.util.Arrays.equals(source,routed))throw new AssertionError("production dispatch missing");
  }
  byte[] unrelated=make(false,false,"func_70632_aY");
  if(!java.util.Arrays.equals(unrelated,(byte[])modifier.invoke(null,(Object)unrelated)))throw new AssertionError("unrelated Function altered");
  System.out.println("Crucible shield fixture PASS: damage bridge, fail-closed drift, MCP/SRG modifiers, dispatch, verifier, idempotence");
 }
 private static byte[] make(boolean player,boolean relevant,String method){
  ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(Opcodes.V1_6,Opcodes.ACC_PUBLIC,player?PLAYER:"net/minecraft/entity/EntityLivingBase$2",null,"java/lang/Object",player?null:new String[]{"com/google/common/base/Function"});
  MethodVisitor m=w.visitMethod(Opcodes.ACC_PUBLIC,player?"func_70665_d":"apply",player?"(Lnet/minecraft/util/DamageSource;F)V":"(Ljava/lang/Double;)Ljava/lang/Double;",null,null);m.visitCode();
  if(player){if(relevant){m.visitVarInsn(Opcodes.ALOAD,0);m.visitVarInsn(Opcodes.ALOAD,1);m.visitVarInsn(Opcodes.FLOAD,2);m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,PLAYER,"damageEntity_CB","(Lnet/minecraft/util/DamageSource;F)Z",false);m.visitInsn(Opcodes.POP);}m.visitInsn(Opcodes.RETURN);}
  else{if(relevant){m.visitInsn(Opcodes.ACONST_NULL);m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,PLAYER,method,"()Z",false);m.visitInsn(Opcodes.POP);}m.visitInsn(Opcodes.ACONST_NULL);m.visitInsn(Opcodes.ARETURN);}
  m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();
 }
}
