import com.mod.htgt6.asm.SkinPortRenderFixASM;
import lain.mods.skins.impl.SkinData;
import lain.mods.skins.api.SkinBundle;
import lain.mods.skins.api.interfaces.ISkin;
import java.util.*;
import java.util.jar.*;
import java.io.*;
import java.lang.reflect.*;
public class SkinPortASMFixture {
 static class Loader extends ClassLoader {
  Loader(){super(SkinPortASMFixture.class.getClassLoader());}
  Class<?> define(byte[] b){return defineClass(SkinPortRenderFixASM.TARGET,b,0,b.length);}
 }
 public static void main(String[] args) throws Exception {
  byte[] bytes;
  try(JarFile jar=new JarFile(args[0]);InputStream in=jar.getInputStream(jar.getJarEntry(SkinPortRenderFixASM.TARGET.replace('.','/')+".class"));ByteArrayOutputStream out=new ByteArrayOutputStream()) {
   byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);bytes=out.toByteArray();
  }
  byte[] patched=SkinPortRenderFixASM.transform(bytes);
  if(!Arrays.equals(patched,SkinPortRenderFixASM.transform(patched)))throw new AssertionError("not idempotent");
  Method copy=new Loader().define(patched).getDeclaredMethod("htgt6$copyDefaultSkin",ISkin.class);copy.setAccessible(true);
  for(String type:new String[]{"default","slim"}) {
   SkinData original=new SkinData();original.put(new byte[]{1,2,3},type);
   ISkin first=(ISkin)copy.invoke(null,original), second=(ISkin)copy.invoke(null,original);
   if(first==second||first==original||!type.equals(second.getSkinType()))throw new AssertionError("copy");
   new SkinBundle().set(Collections.singletonList(first)).onRemoval();
   if(first.isDataReady()||!second.isDataReady()||!original.isDataReady())throw new AssertionError("eviction");
   if(second.getData().get(1)!=2||original.getData().position()!=0)throw new AssertionError("buffer");
  }
  if(copy.invoke(null,new Object[]{null})!=null||copy.invoke(null,new SkinData())!=null)throw new AssertionError("empty fallback");
  System.out.println("PASS: actual SkinPort provider verifies; Steve/Alex copies survive another profile eviction; null/empty fallback; idempotent ASM");
 }
}
