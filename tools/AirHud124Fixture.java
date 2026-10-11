package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.PlayerBreathing120;
import net.minecraft.block.material.Material;
import java.lang.reflect.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
public final class AirHud124Fixture {
 static int checks;
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  Dolphin112Fixture.field(net.minecraft.world.World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));w.provider.worldObj=w;
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);p.eyes=false;p.setAir(0);
  for(int t=0;t<75;t++){check(PlayerBreathing120.showAir(p,Material.water),"surface HUD remains during refill");p.setAir(PlayerBreathing120.surfaceAir(300,p));}
  check(p.getAir()==300&&!PlayerBreathing120.showAir(p,Material.water),"HUD hides only at full air after 75 ticks");
  p.eyes=true;check(PlayerBreathing120.showAir(p,Material.water),"full air underwater still visible");p.eyes=false;p.setAir(20);
  check(!PlayerBreathing120.showAir(p,Material.lava),"no unrelated material override");
  boolean enabled=com.mod.htgt6.asm.HTGT6Config.RUN_MINECRAFT_1_7_9999;com.mod.htgt6.asm.HTGT6Config.RUN_MINECRAFT_1_7_9999=false;
  check(!PlayerBreathing120.showAir(p,Material.water),"disabled module retains vanilla HUD");com.mod.htgt6.asm.HTGT6Config.RUN_MINECRAFT_1_7_9999=enabled;
  Method transform=Class.forName("com.mod.htgt6.asm.AquaticAirHudASM").getDeclaredMethod("transform",byte[].class);transform.setAccessible(true);
  byte[] bytes;try(ZipFile z=new ZipFile(args[0]);java.io.InputStream in=z.getInputStream(z.getEntry("net/minecraftforge/client/GuiIngameForge.class"))){java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);bytes=out.toByteArray();}
  for(boolean srg:new boolean[]{false,true}){
   ClassNode source=new ClassNode();new ClassReader(bytes).accept(source,0);
   if(srg)for(MethodNode method:source.methods)for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).name.equals("isInsideOfMaterial"))((MethodInsnNode)i).name="func_70055_a";
   ClassWriter writer=new ClassWriter(0);source.accept(writer);byte[] patched=(byte[])transform.invoke(null,(Object)writer.toByteArray());
   check(java.util.Arrays.equals(patched,(byte[])transform.invoke(null,(Object)patched)),"idempotent transform "+srg);
   ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);int calls=0;
   for(MethodNode m:node.methods)if(m.name.equals("renderAir")){new Analyzer(new BasicVerifier()).analyze(node.name,m);for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).name.equals("showAir"))calls++;}
   check(calls==1,"single verified HUD hook "+srg);
  }
  System.out.println("PASS air HUD124: "+checks+" checks");
 }
 static void check(boolean v,String n){if(!v)throw new AssertionError(n);checks++;}
}
