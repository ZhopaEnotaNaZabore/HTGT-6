package com.mod.htgt6.asm;
import java.io.*;
import java.net.URL;
import java.util.zip.*;
import java.lang.reflect.Method;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.LaunchClassLoader;
public final class CrucibleSwimming117Fixture {
 public static void main(String[] args)throws Exception{
  File file=new File(args[0]);FMLDeobfuscatingRemapper.INSTANCE.setup(new File("."),new LaunchClassLoader(new URL[]{file.toURI().toURL()}),"/deobfuscation_data-1.7.10.lzma");RuntimeBytecodeRemapper.enableProductionBridge();
  Method read=ProductionASMRemapFixture120.class.getDeclaredMethod("read",InputStream.class);read.setAccessible(true);
  Method pose=ProductionASMRemapFixture120.class.getDeclaredMethod("verifySwimmingPoseHooks",String.class,byte[].class);pose.setAccessible(true);
  Method air=ProductionASMRemapFixture120.class.getDeclaredMethod("verifySurfaceBreathing",byte[].class);air.setAccessible(true);
  try(ZipFile zip=new ZipFile(file)){for(String target:new String[]{"net/minecraft/entity/Entity","net/minecraft/entity/player/EntityPlayer","net/minecraft/entity/EntityLivingBase"}){
   String notch=FMLDeobfuscatingRemapper.INSTANCE.unmap(target);byte[] raw=(byte[])read.invoke(null,zip.getInputStream(zip.getEntry(notch+".class")));
   byte[] result=new HTGT6Transformer().transform(notch,target,RuntimeBytecodeRemapper.toSrg(raw));result=RuntimeBytecodeRemapper.toSrg(RuntimeBytecodeRemapper.toNotch(result));
   pose.invoke(null,target,result);if(target.endsWith("EntityLivingBase"))air.invoke(null,(Object)result);
   System.out.println("PASS Crucible swimming hooks: "+target);
  }}
 }
}
