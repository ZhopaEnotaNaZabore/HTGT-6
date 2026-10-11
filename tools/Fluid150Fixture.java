package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.asm.WaterloggedFluidASM;import com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.WaterloggedFluid150;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import net.minecraft.world.World;import net.minecraft.entity.Entity;import net.minecraft.block.material.Material;import net.minecraft.util.AxisAlignedBB;
import java.lang.reflect.*;import java.util.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.tree.analysis.*;
public final class Fluid150Fixture {
 static int n;static void check(boolean b,String s){if(!b)throw new AssertionError(s);n++;}
 static String[] names={"handleMaterialAcceleration","isMaterialInBB","isAABBInMaterial","isAnyLiquid"};static String[] srgs={"func_72918_a","func_72875_a","func_72830_b","func_72953_d"};
 static String box="Lnet/minecraft/util/AxisAlignedBB;",mat="Lnet/minecraft/block/material/Material;";
 static String[] desc={"("+box+mat+"Lnet/minecraft/entity/Entity;)Z","("+box+mat+")Z","("+box+mat+")Z","("+box+")Z"};
 public static void main(String[]args)throws Exception{
  World base=BountifulMechanicsFixture120.initialize();sun.misc.Unsafe u=(sun.misc.Unsafe)Dolphin112Fixture.field(sun.misc.Unsafe.class,"theUnsafe").get(null);
  for(boolean entity:new boolean[]{false,true})for(boolean srg:new boolean[]{false,true}){
   Class<?> clazz=entity?Entity.class:World.class;byte[] bytes=Berry147Fixture.read(clazz.getResourceAsStream(clazz.getSimpleName()+".class"));ClassNode c=new ClassNode();new ClassReader(bytes).accept(c,0);
   if(srg)for(MethodNode m:c.methods){if(entity&&m.name.equals("isInsideOfMaterial"))m.name="func_70055_a";if(!entity)for(int i=0;i<4;i++)if(m.name.equals(names[i]))m.name=srgs[i];}
   ClassWriter wr=new ClassWriter(0);c.accept(wr);byte[] patch=entity?WaterloggedFluidASM.transformEntity(wr.toByteArray()):WaterloggedFluidASM.transformWorld(wr.toByteArray());
   check(Arrays.equals(patch,entity?WaterloggedFluidASM.transformEntity(patch):WaterloggedFluidASM.transformWorld(patch)),"idempotent "+entity+srg);
   ClassNode node=new ClassNode();new ClassReader(patch).accept(node,0);for(MethodNode m:node.methods){boolean hooked=false;for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.endsWith("WaterloggedFluid150"))hooked=true;if(hooked){new Analyzer(new BasicVerifier()).analyze(node.name,m);check(true,"actual query verified");}}
  }
  ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);cw.visit(50,Opcodes.ACC_PUBLIC,"fixture/WaterloggedWorldProbe",null,"com/mod/htgt6/minecraft1202/Module/Data/Bountiful/Campfire149Fixture$TestWorld",null);
  for(int i=0;i<4;i++){MethodVisitor m=cw.visitMethod(Opcodes.ACC_PUBLIC,names[i],desc[i],null,null);m.visitCode();m.visitInsn(Opcodes.ICONST_0);m.visitInsn(Opcodes.IRETURN);m.visitMaxs(0,0);m.visitEnd();}cw.visitEnd();
  Class<?> probe=new Loader().define(WaterloggedFluidASM.transformWorld(cw.toByteArray()));Campfire149Fixture.TestWorld w=(Campfire149Fixture.TestWorld)u.allocateInstance(probe);
  for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
  Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));w.provider.worldObj=w;w.block=VillagePillageContent.campfire;w.meta=8;
  AxisAlignedBB a=AxisAlignedBB.getBoundingBox(.2,10.5,.2,.8,10.8,.8);
  check(w.handleMaterialAcceleration(a,Material.water,null),"transformed acceleration detects static water");check(w.isMaterialInBB(a,Material.water),"transformed material query");check(w.isAABBInMaterial(a,Material.water),"transformed swimming query");check(w.isAnyLiquid(a),"transformed any liquid query");
  check(!w.isMaterialInBB(a,Material.lava),"never claims lava");check(!w.isAABBInMaterial(AxisAlignedBB.getBoundingBox(.2,10.95,.2,.8,11,.8),Material.water),"source surface respected");
  w.meta=4;check(!w.isAnyLiquid(a),"dry campfire remains wood");check(WaterloggedFluid150.material(true,w,a,Material.lava),"original true preserved");w.meta=8;
  ClassWriter ce=new ClassWriter(ClassWriter.COMPUTE_MAXS);ce.visit(50,Opcodes.ACC_PUBLIC,"fixture/WaterloggedEntityProbe",null,"net/minecraft/entity/passive/EntityCow",null);
  MethodVisitor ctor=ce.visitMethod(Opcodes.ACC_PUBLIC,"<init>","(Lnet/minecraft/world/World;)V",null,null);ctor.visitCode();ctor.visitVarInsn(Opcodes.ALOAD,0);ctor.visitVarInsn(Opcodes.ALOAD,1);ctor.visitMethodInsn(Opcodes.INVOKESPECIAL,"net/minecraft/entity/passive/EntityCow","<init>","(Lnet/minecraft/world/World;)V",false);ctor.visitInsn(Opcodes.RETURN);ctor.visitMaxs(0,0);ctor.visitEnd();
  MethodVisitor m=ce.visitMethod(Opcodes.ACC_PUBLIC,"isInsideOfMaterial","("+mat+")Z",null,null);m.visitCode();m.visitInsn(Opcodes.ICONST_0);m.visitInsn(Opcodes.IRETURN);m.visitMaxs(0,0);m.visitEnd();ce.visitEnd();
  Class<?> ec=new Loader().define(WaterloggedFluidASM.transformEntity(ce.toByteArray()));Entity e=(Entity)ec.getConstructor(World.class).newInstance(w);e.setPosition(.5,10.5-e.getEyeHeight(),.5);
  check(e.isInsideOfMaterial(Material.water),"transformed eyes recognizes waterlogged");check(!e.isInsideOfMaterial(Material.lava),"eyes do not report lava");e.setPosition(.5,10.99-e.getEyeHeight(),.5);check(!e.isInsideOfMaterial(Material.water),"eyes above source surface breathe");
  w.unloaded=true;w.reads=0;check(!w.isMaterialInBB(a,Material.water)&&w.reads==0,"unloaded bbox never reads blocks");e.setPosition(.5,10.5-e.getEyeHeight(),.5);check(!e.isInsideOfMaterial(Material.water)&&w.reads==0,"unloaded eyes never reads blocks");w.unloaded=false;
  w.aboveWater=true;e.setPosition(.5,10.99-e.getEyeHeight(),.5);check(e.isInsideOfMaterial(Material.water),"water above fills source to full height");w.aboveWater=false;
  w.block=VillagePillageContent.lantern;w.meta=2;check(w.isAnyLiquid(a),"shared lantern wet bit detected");w.meta=0;check(!w.isAnyLiquid(a),"dry lantern stays iron");
  net.minecraft.entity.player.EntityPlayer player=new Swimming115Fixture.Player(w);player.inventory.mainInventory[0]=new net.minecraft.item.ItemStack(net.minecraft.init.Items.water_bucket);
  check(w.block.onBlockActivated(w,0,10,0,player,1,0,0,0)&&w.meta==2,"lantern accepts bucket");check(w.block.onBlockActivated(w,0,10,0,player,1,0,0,0)&&w.meta==0,"lantern bucket drain");
  System.out.println("PASS fluid150: "+n);
 }
 static class Loader extends ClassLoader{Class<?> define(byte[]b){return defineClass(null,b,0,b.length);}}
}
