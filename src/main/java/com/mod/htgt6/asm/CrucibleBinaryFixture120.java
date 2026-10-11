package com.mod.htgt6.asm;
import java.io.*;import java.net.*;import java.util.*;import java.util.zip.*;import java.lang.reflect.*;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.tree.analysis.*;
/** Offline reconstruction and ASM audit of the supplied Crucible binary. Does not start a server. */
public final class CrucibleBinaryFixture120 {
 public static void main(String[]a)throws Exception{
  Map<String,byte[]> files=new LinkedHashMap<String,byte[]>();load(a[0],files);load(a[1],files);int patches=0;
  try(ZipFile z=new ZipFile(a[2])){Enumeration<? extends ZipEntry> entries=z.entries();while(entries.hasMoreElements()){
   ZipEntry e=entries.nextElement();if(!e.getName().startsWith("binpatch/server/")||!e.getName().endsWith(".binpatch"))continue;
   DataInputStream in=new DataInputStream(z.getInputStream(e));String label=in.readUTF(),source=in.readUTF(),target=in.readUTF();boolean exists=in.readBoolean();int checksum=exists?in.readInt():0;int size=in.readInt();byte[] diff=new byte[size];in.readFully(diff);in.close();
   String path=source.replace('.','/')+".class";byte[] original=exists?files.get(path):new byte[0];if(original==null)throw new AssertionError("Missing base "+path);
   if(exists){Adler32 adler=new Adler32();adler.update(original);if((int)adler.getValue()!=checksum)throw new AssertionError("Checksum mismatch "+path);}
   files.put(path,new cpw.mods.fml.repackage.com.nothome.delta.GDiffPatcher().patch(original,diff));patches++;
  }}
  try(ZipOutputStream z=new ZipOutputStream(new FileOutputStream(a[3]))){for(Map.Entry<String,byte[]> e:files.entrySet()){z.putNextEntry(new ZipEntry(e.getKey()));z.write(e.getValue());z.closeEntry();}}
  System.out.println("Reconstructed server patches="+patches+" (all base checksums verified)");
  LaunchClassLoader loader=new LaunchClassLoader(new URL[]{new File(a[3]).toURI().toURL()});
  FMLDeobfuscatingRemapper.INSTANCE.setup(new File("."),loader,"/deobfuscation_data-1.7.10.lzma");RuntimeBytecodeRemapper.enableProductionBridge();
  Field f=ProductionASMRemapFixture120.class.getDeclaredField("TARGETS");f.setAccessible(true);
  Set<String> targets=new LinkedHashSet<String>(Arrays.asList((String[])f.get(null)));
  for(String name:files.keySet())if(name.endsWith(".class")) {String mapped=FMLDeobfuscatingRemapper.INSTANCE.map(name.substring(0,name.length()-6));if(mapped.startsWith("net/minecraft/entity/EntityLivingBase$"))targets.add(mapped);}
  HTGT6Transformer transformer=new HTGT6Transformer();int checked=0,failed=0,shield=0,pearl=0;
  for(String target:targets){if(target.startsWith("net/minecraft/client/"))continue;String notch=FMLDeobfuscatingRemapper.INSTANCE.unmap(target);byte[] bytes=files.get(notch+".class");if(bytes==null)continue;
   try{byte[] input=RuntimeBytecodeRemapper.toSrg(bytes);byte[] output=transformer.transform(notch,target,input);ClassNode node=new ClassNode();new ClassReader(output).accept(node,0);
    for(MethodNode m:node.methods){if((m.access&(Opcodes.ACC_ABSTRACT|Opcodes.ACC_NATIVE))==0)new Analyzer(new BasicInterpreter()).analyze(node.name,m);
     for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof MethodInsnNode){String method=((MethodInsnNode)n).name;if(method.equals("usesLegacyDamageBlock"))shield++;if(method.equals("pearlEndermite120"))pearl++;}}
    System.out.println("PASS "+target);checked++;
   }catch(Throwable e){failed++;System.out.println("FAIL "+target+" "+e);}
  }
  System.out.println("Crucible: passed="+checked+" failed="+failed+" shield="+shield+" pearl="+pearl);
  if(failed!=0||shield!=1||pearl!=1)throw new AssertionError("Crucible compatibility incomplete");
 }
 private static void load(String path,Map<String,byte[]> files)throws Exception{try(ZipFile z=new ZipFile(path)){Enumeration<? extends ZipEntry> entries=z.entries();while(entries.hasMoreElements()){ZipEntry e=entries.nextElement();if(e.isDirectory())continue;try(InputStream in=z.getInputStream(e)){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);files.put(e.getName(),out.toByteArray());}}}}
}
