package com.mod.htgt6.asm;
import java.lang.reflect.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
/** Executes a synthetic Crucible teleport branch after adapting external calls to local probes. */
public final class CruciblePearlFixture120 {
 public static String events="";
 public static void mite(Object pearl){events+="mite;";}
 public static final class Handler {public void teleport(Object location){events+="teleport;";}}
 public static void main(String[]args)throws Exception {
  Method transform=HTGT6Transformer.class.getDeclaredMethod("transformBountifulPearl120",byte[].class);transform.setAccessible(true);
  for(String name:new String[]{"onImpact","func_70184_a"}) {
   byte[] output=(byte[])transform.invoke(null,(Object)make(name,"net/minecraft/network/NetHandlerPlayServer",1));
   ClassNode node=new ClassNode();new ClassReader(output).accept(node,0);int hooks=0;
   for(Object value:node.methods){MethodNode m=(MethodNode)value;
    for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)i;
     if(call.name.equals("pearlEndermite120")){hooks++;call.owner="com/mod/htgt6/asm/CruciblePearlFixture120";call.name="mite";call.desc="(Ljava/lang/Object;)V";}
     if(call.name.equals("teleport")){call.owner="com/mod/htgt6/asm/CruciblePearlFixture120$Handler";call.desc="(Ljava/lang/Object;)V";}
    }
    new Analyzer(new BasicInterpreter()).analyze(node.name,m);
   }
   if(hooks!=1)throw new AssertionError("hook count "+hooks);
   ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);node.accept(writer);
   Class<?> type=new Loader().define(writer.toByteArray());Object instance=type.newInstance();Method impact=type.getMethod(name,Handler.class,boolean.class,boolean.class);
   for(boolean cancelled:new boolean[]{false,true})for(boolean disconnected:new boolean[]{false,true}) {
    events="";impact.invoke(instance,new Handler(),cancelled,disconnected);
    String expected=cancelled||disconnected?"":"mite;teleport;";
    if(!expected.equals(events))throw new AssertionError("cancel/disconnect branch: "+events);
   }
  }
  for(byte[] unknown:new byte[][]{make("onImpact","other/Handler",1),make("onImpact","net/minecraft/network/NetHandlerPlayServer",2)}) {
   try{transform.invoke(null,(Object)unknown);throw new AssertionError("unknown/ambiguous teleport accepted");}
   catch(InvocationTargetException e){if(!(e.getCause() instanceof IllegalStateException))throw e;}
  }
  System.out.println("Crucible pearl PASS: MCP/SRG, cancelled/disconnected no spawn, accepted exactly once before teleport, verifier, fail-closed unknown/duplicate anchors");
 }
 private static final class Loader extends ClassLoader {Class<?> define(byte[] bytes){return defineClass(null,bytes,0,bytes.length);}}
 private static byte[] make(String name,String owner,int calls) {
  ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);w.visit(Opcodes.V1_6,Opcodes.ACC_PUBLIC,"fixture/CruciblePearl",null,"java/lang/Object",null);
  MethodVisitor c=w.visitMethod(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);c.visitCode();c.visitVarInsn(Opcodes.ALOAD,0);c.visitMethodInsn(Opcodes.INVOKESPECIAL,"java/lang/Object","<init>","()V",false);c.visitInsn(Opcodes.RETURN);c.visitMaxs(0,0);c.visitEnd();
  MethodVisitor m=w.visitMethod(Opcodes.ACC_PUBLIC,name,"(Lcom/mod/htgt6/asm/CruciblePearlFixture120$Handler;ZZ)V",null,null);m.visitCode();Label end=new Label();m.visitVarInsn(Opcodes.ILOAD,2);m.visitJumpInsn(Opcodes.IFNE,end);m.visitVarInsn(Opcodes.ILOAD,3);m.visitJumpInsn(Opcodes.IFNE,end);
  for(int i=0;i<calls;i++){m.visitVarInsn(Opcodes.ALOAD,1);m.visitInsn(Opcodes.ACONST_NULL);m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,owner,"teleport","(Lorg/bukkit/Location;)V",false);}
  m.visitLabel(end);m.visitInsn(Opcodes.RETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();
}
}
