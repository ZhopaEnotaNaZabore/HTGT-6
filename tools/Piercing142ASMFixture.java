package com.mod.htgt6.asm;
import java.io.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.tree.analysis.*;
public class Piercing142ASMFixture {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static String srg(String s){String[] m={"onUpdate","ticksInAir","canBeCollidedWith","setDead","getIsCritical","setArrowCountInEntity"};String[] p={"func_70071_h_","field_70257_an","func_70067_L","func_70106_y","func_70241_g","func_85034_r"};for(int i=0;i<m.length;i++)if(m[i].equals(s))return p[i];return s;}
 public static void main(String[] args)throws Exception{
  for(boolean production:new boolean[]{false,true}){
   ClassNode node=new ClassNode();InputStream in=Piercing142ASMFixture.class.getClassLoader().getResourceAsStream("net/minecraft/entity/projectile/EntityArrow.class");new ClassReader(in).accept(node,0);in.close();
   if(production){for(FieldNode f:node.fields)f.name=srg(f.name);for(MethodNode m:node.methods){m.name=srg(m.name);for(AbstractInsnNode i:m.instructions.toArray()){if(i instanceof FieldInsnNode)((FieldInsnNode)i).name=srg(((FieldInsnNode)i).name);if(i instanceof MethodInsnNode)((MethodInsnNode)i).name=srg(((MethodInsnNode)i).name);}}}
   PiercingArrowASM.transform(node);ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES){protected String getCommonSuperClass(String a,String b){return "java/lang/Object";}};node.accept(writer);ClassNode output=new ClassNode();new ClassReader(writer.toByteArray()).accept(output,0);
   int hooks=0,deaths=0;for(MethodNode m:output.methods){new Analyzer(new BasicVerifier()).analyze(output.name,m);if(!m.name.equals(production?"func_70071_h_":"onUpdate"))continue;for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode c=(MethodInsnNode)i;if(c.owner.equals(PiercingArrowASM.HOOK))hooks++;if(c.name.equals(production?"func_70106_y":"setDead"))deaths++;}}
   check(hooks==4,"four integration anchors "+production);check(deaths==1,"in-ground expiry still vanilla "+production);
   boolean fail=false;try{PiercingArrowASM.transform(output);}catch(IllegalStateException expected){fail=true;}check(fail,"drift detected "+production);
  }
  System.out.println("PASS Piercing142 ASM: "+checks);
 }
}
