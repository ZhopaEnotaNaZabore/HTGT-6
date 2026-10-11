package com.mod.htgt6.asm;
import java.io.*;import java.net.*;import java.util.*;import java.util.zip.*;import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;import net.minecraft.launchwrapper.LaunchClassLoader;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.tree.analysis.*;
public final class Client155Fixture {
 public static void main(String[]args)throws Exception{
  FMLDeobfuscatingRemapper.INSTANCE.setup(new File("."),new LaunchClassLoader(new URL[]{new File(args[0]).toURI().toURL(),new File(args[1]).toURI().toURL()}),"/deobfuscation_data-1.7.10.lzma");RuntimeBytecodeRemapper.enableProductionBridge();byte[] raw;
  try(ZipFile zip=new ZipFile(args[1]);InputStream in=zip.getInputStream(zip.getEntry("net/minecraftforge/client/GuiIngameForge.class"))){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);raw=out.toByteArray();}
  for(byte[] input:new byte[][]{raw,RuntimeBytecodeRemapper.toSrg(raw)}){
   byte[] patched=new HTGT6Transformer().transform("net.minecraftforge.client.GuiIngameForge","net.minecraftforge.client.GuiIngameForge",input);
   if(!Arrays.equals(patched,AquaticAirHudASM.transform(patched)))throw new AssertionError("not idempotent");
   ClassNode node=new ClassNode();new ClassReader(RuntimeBytecodeRemapper.toSrg(patched)).accept(node,0);int hooks=0;
   for(MethodNode m:node.methods)if(m.name.equals("renderAir")){new Analyzer(new BasicVerifier()).analyze(node.name,m);for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).name.equals("showAir")){if(!((MethodInsnNode)i).desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/block/material/Material;)Z"))throw new AssertionError("bad normalized hook signature");hooks++;}}
   if(hooks!=1)throw new AssertionError("HUD hooks "+hooks);
  }
  com.google.gson.Gson gson=new com.google.gson.GsonBuilder().registerTypeAdapter(net.minecraft.client.audio.SoundList.class,new net.minecraft.client.audio.SoundListSerializer()).create();int events=0;
  for(String domain:new String[]{"htgt6","amunra"}){com.google.gson.JsonObject json=new com.google.gson.JsonParser().parse(new InputStreamReader(new FileInputStream("src/main/resources/assets/"+domain+"/sounds.json"),"UTF-8")).getAsJsonObject();for(Map.Entry<String,com.google.gson.JsonElement> e:json.entrySet()){try{gson.fromJson(e.getValue(),net.minecraft.client.audio.SoundList.class);events++;}catch(Exception error){throw new AssertionError(domain+":"+e.getKey(),error);}}}
  System.out.println("PASS client155: actual packaged Forge HUD raw/SRG, idempotence, descriptor and verifier; "+events+" sound events parsed by vanilla serializer");
 }
}
