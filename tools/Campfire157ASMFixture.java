package com.mod.htgt6.asm;
import java.io.*;import java.net.*;import java.util.zip.*;
import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.tree.analysis.*;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.LaunchClassLoader;
public final class Campfire157ASMFixture {
 public static void main(String[]args)throws Exception{
  String path="net/minecraft/entity/projectile/EntityPotion";
  byte[] forge=read(Campfire157ASMFixture.class.getClassLoader().getResourceAsStream(path+".class"));
  verify(CampfireWaterASM157.transform(forge),"onImpact");
  File core=new File(args[0]);FMLDeobfuscatingRemapper.INSTANCE.setup(new File("."),new LaunchClassLoader(new URL[]{core.toURI().toURL()}),"/deobfuscation_data-1.7.10.lzma");RuntimeBytecodeRemapper.enableProductionBridge();
  try(ZipFile zip=new ZipFile(core)){
   byte[] srg=RuntimeBytecodeRemapper.toSrg(read(zip.getInputStream(zip.getEntry(FMLDeobfuscatingRemapper.INSTANCE.unmap(path)+".class"))));
   byte[] result=new HTGT6Transformer().transform(path,path,srg);
   verify(RuntimeBytecodeRemapper.toSrg(RuntimeBytecodeRemapper.toNotch(result)),"afterCancellation");
  }
  System.out.println("PASS campfire157 ASM: Forge + actual Crucible, remap roundtrip, verifier, idempotence");
 }
 static void verify(byte[] bytes,String expected)throws Exception{
  ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int hooks=0;
  for(MethodNode m:node.methods){if((m.access&(Opcodes.ACC_NATIVE|Opcodes.ACC_ABSTRACT))==0)new Analyzer(new BasicVerifier()).analyze(node.name,m);
   for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode c=(MethodInsnNode)i;if(c.owner.endsWith("/CampfireWater157")){hooks++;if(!c.name.equals(expected))throw new AssertionError(c.name);if(expected.equals("afterCancellation")){AbstractInsnNode previous=i.getPrevious().getPrevious().getPrevious();if(!(previous instanceof MethodInsnNode)||!((MethodInsnNode)previous).name.equals("isCancelled"))throw new AssertionError("cancellation anchor");}}}}
  if(hooks!=1)throw new AssertionError("hook count "+hooks);
  if(!java.util.Arrays.equals(bytes,CampfireWaterASM157.transform(bytes)))throw new AssertionError("not idempotent");
 }
 static byte[] read(InputStream in)throws Exception{try(InputStream stream=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=stream.read(b))!=-1)out.write(b,0,n);return out.toByteArray();}}
}
