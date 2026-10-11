package com.mod.htgt6.asm;
import java.io.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.tree.analysis.*;
/** Check the composed production transformer, not only each independent wrapper. */
public final class Final153ASMFixture {
 public static void main(String[]args)throws Exception{
  HTGT6Transformer transformer=new HTGT6Transformer();int checked=0;
  String[][] cases={{"net.minecraft.entity.Entity","WaterloggedFluid150","BerryMovement147"},{"net.minecraft.entity.EntityLivingBase","ScaffoldingMovement152","PlayerSwimming120"},{"net.minecraft.world.World","WaterloggedFluid150"},{"net.minecraft.entity.passive.EntityHorse","LeatherHorseHooks151"},{"net.minecraft.entity.passive.EntityVillager","VillagerEconomy145"}};
  for(String[] test:cases){InputStream in=Final153ASMFixture.class.getClassLoader().getResourceAsStream(test[0].replace('.','/')+".class");ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);in.close();byte[] result=transformer.transform(test[0],test[0],out.toByteArray());ClassNode node=new ClassNode();new ClassReader(result).accept(node,0);
   for(MethodNode method:node.methods)new Analyzer(new BasicVerifier()).analyze(node.name,method);
   for(int k=1;k<test.length;k++){boolean found=false;for(MethodNode method:node.methods)for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.endsWith('/'+test[k]))found=true;if(!found)throw new AssertionError(test[0]+" missing "+test[k]);checked++;}
  }
  System.out.println("PASS final153 composed ASM: "+checked+" required hooks across five real classes; all methods verified");
 }
}
