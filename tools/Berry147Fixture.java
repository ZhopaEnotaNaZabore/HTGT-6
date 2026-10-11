package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.asm.BerryMovementASM;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.BerryMovement147;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120SweetBerryBush;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.entity.Entity;import net.minecraft.world.World;import net.minecraft.item.ItemStack;import net.minecraft.init.Items;
import java.io.*;import java.util.*;import java.lang.reflect.*;
import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.tree.analysis.*;
public final class Berry147Fixture {
 static int n;static void check(boolean b,String m){if(!b)throw new AssertionError(m);n++;}
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));w.provider.worldObj=w;
  Dolphin112Fixture.field(World.class,"worldAccesses").set(w,new ArrayList());
  Swimming115Fixture.Player player=new Swimming115Fixture.Player(w);
  BlockBackport120SweetBerryBush b=(BlockBackport120SweetBerryBush)Backport120Content.sweetBerryBush;
  w.setBlock(0,10,0,b,0,2);Random growth=new Random(){public int nextInt(int bound){return 0;}};
  w.light=8;b.updateTick(w,0,10,0,growth);check(w.getBlockMetadata(0,10,0)==0,"growth light8 rejected");
  w.light=9;b.updateTick(w,0,10,0,growth);check(w.getBlockMetadata(0,10,0)==1,"growth light9 accepted");
  w.setBlockMetadataWithNotify(0,10,0,3,2);b.updateTick(w,0,10,0,growth);check(w.getBlockMetadata(0,10,0)==3,"mature bush stays mature");
  w.setBlock(0,10,0,b,2,2);player.inventory.mainInventory[0]=new ItemStack(Items.dye,1,15);
  check(!b.onBlockActivated(w,0,10,0,player,1,0,0,0)&&w.getBlockMetadata(0,10,0)==2,"bone meal passes through age2 harvest");
  b.func_149853_b(w,new Random(),0,10,0);check(w.getBlockMetadata(0,10,0)==3,"bone meal matures");
  w.lastSpawn=null;w.rejectWrites=true;check(!b.onBlockActivated(w,0,10,0,player,1,0,0,0)&&w.getBlockMetadata(0,10,0)==3&&w.lastSpawn==null,"denied harvest no berries or reset");w.rejectWrites=false;
  cpw.mods.fml.common.registry.GameRegistry.registerItem(Backport120Content.sweetBerries,"berry147_fruit");
  check(b.onBlockActivated(w,0,10,0,player,1,0,0,0)&&w.getBlockMetadata(0,10,0)==1,"mature bonemeal still harvests");
  net.minecraft.entity.item.EntityItem drop=(net.minecraft.entity.item.EntityItem)w.lastSpawn;
  check(drop!=null&&drop.getEntityItem().stackSize>=2&&drop.getEntityItem().stackSize<=3,"mature harvest 2..3 berries");
  w.setBlockMetadataWithNotify(0,10,0,3,2);
  w.isRemote=true;b.func_149853_b(w,new Random(),0,10,0);check(w.getBlockMetadata(0,10,0)==3,"client no growth mutation");w.isRemote=false;
  for(int age=0;age<4;age++)for(int fortune=0;fortune<=3;fortune++){
   final boolean high=true;Random max=new Random(){public int nextInt(int bound){return bound-1;}};Random min=new Random(){public int nextInt(int bound){return 0;}};
   check(b.quantityDropped(age,fortune,min)==(age<2?0:age-1),"loot minimum");check(b.quantityDropped(age,fortune,max)==(age<2?0:age+fortune),"loot maximum fortune");
  }
  w.setBlockMetadataWithNotify(0,10,0,0,2);player.motionX=1;player.motionY=1;player.fallDistance=10;
  b.onEntityCollidedWithBlock(w,0,10,0,player);b.onEntityCollidedWithBlock(w,0,10,0,player);
  check(player.motionX==1&&player.fallDistance==0,"touch queues slowdown and resets fall distance");check(BerryMovement147.consume(player,false)&&player.motionX==0&&player.motionY==0,"one consumption clears inertia");check(!BerryMovement147.consume(player,false),"multiple blocks coalesce");
  BerryMovement147.touch(player);player.posX++;check(!BerryMovement147.consume(player,false),"teleport invalidates");
  BerryMovement147.touch(player);player.ticksExisted+=2;check(!BerryMovement147.consume(player,false),"stale tick invalidates");
  BerryMovement147.touch(player);player.noClip=true;check(!BerryMovement147.consume(player,false),"noclip clears pending");player.noClip=false;check(!BerryMovement147.consume(player,false),"noclip does not defer stale slowdown");
  BerryMovement147.touch(player);check(!BerryMovement147.consume(player,true),"web owns slowdown");
  byte[] bytes=read(Entity.class.getResourceAsStream("Entity.class"));
  for(boolean srg:new boolean[]{false,true}){
   ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
   if(srg){for(FieldNode f:node.fields)if(f.name.equals("isInWeb"))f.name="field_70134_J";for(MethodNode m:node.methods){if(m.name.equals("moveEntity"))m.name="func_70091_d";for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof FieldInsnNode&&((FieldInsnNode)i).name.equals("isInWeb"))((FieldInsnNode)i).name="field_70134_J";}}
   ClassWriter out=new ClassWriter(0);node.accept(out);byte[] patched=BerryMovementASM.transform(out.toByteArray());check(Arrays.equals(patched,BerryMovementASM.transform(patched)),"ASM idempotent");
   ClassNode verified=new ClassNode();new ClassReader(patched).accept(verified,0);for(MethodNode m:verified.methods)if(m.name.equals(srg?"func_70091_d":"moveEntity")&&m.desc.equals("(DDD)V")){new Analyzer(new BasicVerifier()).analyze(verified.name,m);check(true,"actual Entity movement stack verified");}
  }
  ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);String name="fixture/BerryMovementProbe";cw.visit(50,Opcodes.ACC_PUBLIC,name,null,"net/minecraft/entity/passive/EntityCow",null);cw.visitField(Opcodes.ACC_PUBLIC,"isInWeb","Z",null,null).visitEnd();
  for(String axis:new String[]{"outX","outY","outZ"})cw.visitField(Opcodes.ACC_PUBLIC,axis,"D",null,null).visitEnd();
  MethodVisitor ctor=cw.visitMethod(Opcodes.ACC_PUBLIC,"<init>","(Lnet/minecraft/world/World;)V",null,null);ctor.visitCode();ctor.visitVarInsn(Opcodes.ALOAD,0);ctor.visitVarInsn(Opcodes.ALOAD,1);ctor.visitMethodInsn(Opcodes.INVOKESPECIAL,"net/minecraft/entity/passive/EntityCow","<init>","(Lnet/minecraft/world/World;)V",false);ctor.visitInsn(Opcodes.RETURN);ctor.visitMaxs(0,0);ctor.visitEnd();
  MethodVisitor mv=cw.visitMethod(Opcodes.ACC_PUBLIC,"moveEntity","(DDD)V",null,null);mv.visitCode();int local=1;for(String axis:new String[]{"outX","outY","outZ"}){mv.visitVarInsn(Opcodes.ALOAD,0);mv.visitVarInsn(Opcodes.DLOAD,local);mv.visitFieldInsn(Opcodes.PUTFIELD,name,axis,"D");local+=2;}mv.visitInsn(Opcodes.RETURN);mv.visitMaxs(0,0);mv.visitEnd();cw.visitEnd();
  Class<?> probe=new Loader().define(BerryMovementASM.transform(cw.toByteArray()));Entity e=(Entity)probe.getConstructor(World.class).newInstance(w);
  BerryMovement147.touch(e);BerryMovement147.touch(e);e.moveEntity(10,10,10);check(Math.abs(probe.getField("outX").getDouble(e)-8)<.00001&&probe.getField("outY").getDouble(e)==7.5,"transformed method scales locals once");
  e.moveEntity(10,10,10);check(probe.getField("outX").getDouble(e)==10,"next unmarked movement normal");
  System.out.println("PASS berry147: "+n);
 }
 static class Loader extends ClassLoader {Class<?> define(byte[]b){return defineClass(null,b,0,b.length);}}
 static byte[] read(InputStream i)throws Exception{ByteArrayOutputStream b=new ByteArrayOutputStream();byte[]a=new byte[8192];for(int n;(n=i.read(a))>0;)b.write(a,0,n);i.close();return b.toByteArray();}
}
