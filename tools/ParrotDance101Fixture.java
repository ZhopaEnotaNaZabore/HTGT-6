import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityParrot120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.*;
import net.minecraft.init.Blocks;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public final class ParrotDance101Fixture {
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  world.setBlock(0,10,0,Blocks.jukebox,0,3);
  EntityParrot120 parrot=new EntityParrot120(world);parrot.setPosition(.5,10.5,.5);world.loadedEntityList.add(parrot);
  ParrotRecord120.update(world,"records.cat",0,10,0);check(!parrot.isPartyParrot(),"server does not set client dance");
  world.isRemote=true;ParrotRecord120.update(world,"records.cat",0,10,0);check(parrot.isPartyParrot(),"record starts dance");
  parrot.setRecordPlayingNearby(1,10,0,false);check(parrot.isPartyParrot(),"unrelated stop ignored");
  ModelParrot120 model=new ModelParrot120();parrot.ticksExisted=2;parrot.onGround=true;
  model.setRotationAngles(1,1,2,30,40,.0625F,parrot);
  close(model.head.rotationPointX,net.minecraft.util.MathHelper.cos(2),"head sway");
  close(model.head.rotateAngleZ,net.minecraft.util.MathHelper.sin(2)*.4F,"head roll");
  check(model.head.rotateAngleX==0&&model.head.rotateAngleY==0,"dance replaces look pose");
  ParrotRecord120.update(world,null,0,10,0);check(!parrot.isPartyParrot(),"record stop");
  model.setRotationAngles(0,0,2,0,0,.0625F,parrot);
  check(model.head.rotationPointX==0&&model.body.rotationPointX==0&&model.tail.rotationPointX==0
    &&model.head.rotateAngleZ==0&&model.leftWing.rotationPointX==1.5F&&model.leftLeg.rotateAngleZ==0,"pose reset after dance");
  ParrotRecord120.update(world,"records.cat",0,10,0);parrot.setPosition(4,10.5,.5);parrot.updatePartyState();check(!parrot.isPartyParrot(),"out of range stops");
  parrot.setPosition(.5,10.5,.5);ParrotRecord120.update(world,"records.cat",0,10,0);world.setBlockToAir(0,10,0);parrot.updatePartyState();check(!parrot.isPartyParrot(),"removed jukebox stops");
  java.io.InputStream in=net.minecraft.client.renderer.RenderGlobal.class.getResourceAsStream("RenderGlobal.class");
  ClassReader reader=new ClassReader(in);in.close();ClassNode original=new ClassNode();reader.accept(original,0);
  java.lang.reflect.Method transform=Class.forName("com.mod.htgt6.asm.ParrotRecordASM").getDeclaredMethod("transform",byte[].class);transform.setAccessible(true);
  for(boolean srg:new boolean[]{false,true}) {
   for(MethodNode method:original.methods)if(method.name.equals("playRecord"))method.name=srg?"func_72702_a":"playRecord";
   ClassWriter writer=new ClassWriter(0);original.accept(writer);
   byte[] bytes=(byte[])transform.invoke(null,(Object)writer.toByteArray());ClassNode result=new ClassNode();new ClassReader(bytes).accept(result,0);
   int hooks=0;for(MethodNode method:result.methods){new Analyzer(new BasicVerifier()).analyze(result.name,method);
    for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof MethodInsnNode&&((MethodInsnNode)insn).owner.endsWith("/ParrotRecord120"))hooks++;}
   check(hooks==1,"single valid record hook in MCP/SRG");
  }
  System.out.println("PASS dance: start/stop, server exclusion, range, jukebox removal, model pose/reset, MCP/SRG ASM");
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static void close(float a,float b,String message){check(Math.abs(a-b)<.00001,message);}
}
