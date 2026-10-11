package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockScaffolding152;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemBlockScaffolding152;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.ScaffoldingMovement152;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import net.minecraft.world.World;import net.minecraft.init.*;import net.minecraft.item.*;import net.minecraft.entity.item.EntityFallingBlock;import net.minecraft.util.AxisAlignedBB;import java.util.*;
import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.tree.analysis.*;
public final class Scaffold152Fixture {
 static int n;static void check(boolean ok,String m){if(!ok)throw new AssertionError(m);n++;}
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));Dolphin112Fixture.field(World.class,"worldAccesses").set(w,new ArrayList());w.provider.worldObj=w;
  BlockScaffolding152 b=(BlockScaffolding152)VillagePillageContent.scaffolding;cpw.mods.fml.common.registry.GameRegistry.registerBlock(b,ItemBlockScaffolding152.class,"scaffold152");ItemBlockScaffolding152 item=(ItemBlockScaffolding152)Item.getItemFromBlock(b);Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);p.setPosition(0,10,0);
  w.setBlock(0,9,0,Blocks.stone,0,2);check(b.distance(w,0,10,0)==0,"ground distance zero");w.setBlock(0,10,0,b,0,2);check(b.distance(w,0,11,0)==0,"vertical support inherits distance");
  for(int x=1;x<=7;x++){check(b.distance(w,x,10,0)==x,"horizontal distance "+x);w.setBlock(x,10,0,b,x,2);}
  w.setBlock(4,11,0,b,4,2);check(b.distance(w,4,12,0)==4,"vertical extension does not add distance");check(!b.bottom(w,4,11,0)&&b.bottom(w,4,10,0),"bottom derived from actual block below");
  w.setBlock(20,10,0,b,7,2);w.rejectSpawns=true;b.updateTick(w,20,10,0,new Random());check(w.getBlock(20,10,0)==b,"rejected falling spawn retains block");w.rejectSpawns=false;b.updateTick(w,20,10,0,new Random());check(w.getBlock(20,10,0)==Blocks.air&&w.lastSpawn instanceof EntityFallingBlock,"unsupported extension falls");
  w.setBlock(21,10,0,b,15,2);b.updateTick(w,21,10,0,new Random());check(w.getBlock(21,10,0)==Blocks.water&&((EntityFallingBlock)w.lastSpawn).field_145812_b==1,"fall preserves source water");
  w.setBlock(22,10,0,b,3,2);b.updateTick(w,22,10,0,new Random());check(w.getBlock(22,10,0)==Blocks.air&&w.lastSpawn instanceof net.minecraft.entity.item.EntityItem,"loss of prior support drops item");
  w.setBlock(23,10,0,b,7,2);w.rejectWrites=true;b.updateTick(w,23,10,0,new Random());check(w.getBlock(23,10,0)==b&&w.lastSpawn.isDead,"denied block removal cancels falling entity");w.rejectWrites=false;
  w.loaded=false;check(b.distance(w,0,10,0)==-1,"unloaded support defers");w.loaded=true;
  p.rotationYaw=0;ItemStack stack=new ItemStack(item,10);check(item.onItemUse(stack,p,w,0,10,0,2,0,0,0)&&w.getBlock(0,11,0)==b&&stack.stackSize==9,"side click extends tower up");check(item.onItemUse(stack,p,w,0,10,0,1,0,0,0)&&w.getBlock(0,10,1)==b,"top click extends facing direction");
  w.rejectWrites=true;int before=stack.stackSize;check(!item.onItemUse(stack,p,w,0,10,0,2,0,0,0)&&stack.stackSize==before,"placement denial does not consume");w.rejectWrites=false;
  check(!item.onItemUse(stack,p,w,30,10,0,1,0,0,0),"initial unsupported placement rejected");
  AxisAlignedBB query=AxisAlignedBB.getBoundingBox(0,10,0,1,12,1);List boxes=new ArrayList();p.setPosition(.5,11+p.yOffset,.5);b.addCollisionBoxesToList(w,0,10,0,query,boxes,p);check(boxes.size()==1,"top supports above entity");p.setSneaking(true);boxes.clear();b.addCollisionBoxesToList(w,0,10,0,query,boxes,p);check(boxes.isEmpty(),"sneak descends grounded tower");p.setSneaking(false);p.setPosition(.5,10+p.yOffset,.5);boxes.clear();b.addCollisionBoxesToList(w,0,10,0,query,boxes,p);check(boxes.isEmpty(),"can ascend from inside");
  p.motionY=-2;p.motionX=1;p.motionZ=-1;p.fallDistance=9;ScaffoldingMovement152.beforeTravel(p,false);check(p.motionY==-.15&&p.motionX==.15&&p.motionZ==-.15&&p.fallDistance==0,"climb speed and fall clamp");ScaffoldingMovement152.beforeTravel(p,true);check(p.motionY==.2,"jump ascends");p.capabilities.isFlying=true;p.motionY=.7;ScaffoldingMovement152.beforeTravel(p,true);check(p.motionY==.7,"creative flight retained");
  byte[] original;java.io.InputStream in=Scaffold152Fixture.class.getClassLoader().getResourceAsStream("net/minecraft/entity/EntityLivingBase.class");java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buf=new byte[8192];int read;while((read=in.read(buf))!=-1)out.write(buf,0,read);in.close();original=out.toByteArray();
  for(boolean srg:new boolean[]{false,true}){ClassNode node=new ClassNode();new ClassReader(original).accept(node,0);if(srg)for(MethodNode m:node.methods)if(m.name.equals("moveEntityWithHeading"))m.name="func_70612_e";ClassWriter writer=new ClassWriter(0);node.accept(writer);byte[] patched=com.mod.htgt6.asm.ScaffoldingMovementASM152.transform(writer.toByteArray());check(Arrays.equals(patched,com.mod.htgt6.asm.ScaffoldingMovementASM152.transform(patched)),"ASM idempotence "+srg);ClassNode changed=new ClassNode();new ClassReader(patched).accept(changed,0);for(MethodNode m:changed.methods)if(m.name.equals(srg?"func_70612_e":"moveEntityWithHeading")){new Analyzer(new BasicVerifier()).analyze(changed.name,m);check(true,"real movement bytecode verifies "+srg);}}
  System.out.println("PASS scaffold152: "+n);
 }
}
