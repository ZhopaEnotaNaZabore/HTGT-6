package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.CrossbowPlayerPoseHooks142;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.*;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.CrossbowEnchantments140;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.item.*;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
public final class CrossbowPose142Fixture {
 static int checks;
 static void check(boolean ok,String m){if(!ok)throw new AssertionError(m);checks++;}
 static void near(float a,float b,String m){check(Math.abs(a-b)<.00001F,m);}
 public static void main(String[] args)throws Exception{
  Crossbow140Fixture.main(args);
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();Field f=World.class.getDeclaredField("worldInfo");f.setAccessible(true);f.set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));w.provider.worldObj=w;
  f=World.class.getDeclaredField("worldAccesses");f.setAccessible(true);f.set(w,new ArrayList());
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);p.inventory=new Crossbow140Fixture.Inventory(p);
  ItemCrossbow item=new ItemCrossbow();ItemStack main=new ItemStack(item);p.inventory.mainInventory[0]=main;p.inventory.currentItem=0;
  ModelBiped model=new ModelBiped();
  p.setItemInUse(main,71988);CrossbowPlayerPoseHooks142.apply(model,p);near(model.bipedRightArm.rotateAngleY,-.8F,"main charge arm");near(model.bipedLeftArm.rotateAngleY,.4F+.45F*12/25,"main charge support progress");
  p.clearItemInUse();NBTTagCompound tag=new NBTTagCompound();tag.setBoolean("Charged",true);main.setTagCompound(tag);model.bipedHead.rotateAngleY=.2F;CrossbowPlayerPoseHooks142.apply(model,p);near(model.bipedRightArm.rotateAngleY,-.1F,"main hold follows head");near(model.bipedLeftArm.rotateAngleY,.8F,"main hold support");
  p.swingProgress=.5F;model.bipedRightArm.rotateAngleY=.123F;CrossbowPlayerPoseHooks142.apply(model,p);near(model.bipedRightArm.rotateAngleY,.123F,"swing pose preserved");p.swingProgress=0;
  OffhandSystem.setClientShieldBlocking(p.getEntityId(),true);model.bipedRightArm.rotateAngleY=.321F;CrossbowPlayerPoseHooks142.apply(model,p);near(model.bipedRightArm.rotateAngleY,.321F,"raised shield pose preserved");OffhandSystem.setClientShieldBlocking(p.getEntityId(),false);
  p.inventory.mainInventory[0]=null;ItemStack secondary=new ItemStack(item);OffhandHooks.setOffhand(p,secondary);p.inventory.mainInventory[2]=new ItemStack(Items.arrow,4);
  check(OffhandCrossbowUse120.start(p),"offhand start");for(int i=0;i<25;i++)OffhandCrossbowUse120.tick(p);CrossbowPlayerPoseHooks142.apply(model,p);near(model.bipedLeftArm.rotateAngleY,.8F,"left charge primary mirrored");near(model.bipedRightArm.rotateAngleY,-.85F,"left charge support mirrored");near(model.bipedRightArm.rotateAngleX,-(float)Math.PI/2,"support reaches string");OffhandCrossbowUse120.cancel(p);
  secondary.setTagCompound((NBTTagCompound)tag.copy());CrossbowPlayerPoseHooks142.apply(model,p);near(model.bipedLeftArm.rotateAngleY,.5F,"left hold head yaw");near(model.bipedRightArm.rotateAngleY,-.4F,"left hold support yaw");
  p.setItemInUse(new ItemStack(Items.apple),32);model.bipedLeftArm.rotateAngleY=.456F;CrossbowPlayerPoseHooks142.apply(model,p);near(model.bipedLeftArm.rotateAngleY,.456F,"other use pose preserved");p.clearItemInUse();
  net.minecraft.enchantment.Enchantment pierce=CrossbowEnchantments140.piercing,multi=CrossbowEnchantments140.multishot;
  check(pierce.getMaxLevel()==4&&pierce.getMinEnchantability(4)==31&&pierce.getMaxEnchantability(4)==50,"piercing costs and levels");check(!pierce.canApplyTogether(multi)&&!multi.canApplyTogether(pierce),"mutual exclusion both directions");check(pierce.canApplyTogether(CrossbowEnchantments140.quickCharge)&&CrossbowEnchantments140.quickCharge.canApplyTogether(pierce),"QuickCharge compatible");check(net.minecraft.enchantment.EnchantmentHelper.mapEnchantmentData(31,new ItemStack(item)).containsKey(pierce.effectId),"real table discovers piercing");
  Method factory=ItemCrossbow.class.getDeclaredMethod("createArrow",World.class,net.minecraft.entity.EntityLivingBase.class,net.minecraft.entity.EntityLivingBase.class,ItemStack.class,int.class,ItemStack.class);factory.setAccessible(true);
  ItemStack piercingWeapon=new ItemStack(item);piercingWeapon.addEnchantment(pierce,4);
  com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatArrow arrow=(com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatArrow)factory.invoke(null,w,p,null,new ItemStack(Items.arrow),0,piercingWeapon);
  check(arrow.getPierceLevel()==4,"weapon enchantment reaches real projectile factory");
  ItemStack shield=new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.shield);p.inventory.mainInventory[0]=shield;OffhandHooks.setOffhand(p,null);p.setPosition(0,10,-2);p.rotationYaw=0;p.setItemInUse(shield,71995);arrow.setPosition(0,10,0);
  com.mod.htgt6.minecraft1202.Module.Events.Combat.CombatEventHandler handler=new com.mod.htgt6.minecraft1202.Module.Events.Combat.CombatEventHandler();
  net.minecraftforge.event.entity.living.LivingAttackEvent piercingHit=new net.minecraftforge.event.entity.living.LivingAttackEvent(p,net.minecraft.util.DamageSource.causeArrowDamage(arrow,p),6){public boolean isCancelable(){return true;}};
  handler.onLivingAttack(piercingHit);check(!piercingHit.isCanceled()&&shield.getItemDamage()==0,"piercing bypasses shield without wear");
  arrow.setPierceLevel(0);net.minecraftforge.event.entity.living.LivingAttackEvent normalHit=new net.minecraftforge.event.entity.living.LivingAttackEvent(p,net.minecraft.util.DamageSource.causeArrowDamage(arrow,p),6){public boolean isCancelable(){return true;}};
  handler.onLivingAttack(normalHit);check(normalHit.isCanceled()&&shield.getItemDamage()>0,"ordinary arrow still blocked with shield wear");
  Method transform=com.mod.htgt6.asm.HTGT6Transformer.class.getDeclaredMethod("transformModelBipedTridentPose",byte[].class);transform.setAccessible(true);
  for(boolean srg:new boolean[]{false,true}){
   ClassNode source=new ClassNode();new ClassReader(ModelBiped.class.getName()).accept(source,0);if(srg)for(MethodNode method:source.methods)if(method.name.equals("setRotationAngles"))method.name="func_78087_a";
   ClassWriter writer=new ClassWriter(0);source.accept(writer);byte[] out=(byte[])transform.invoke(null,writer.toByteArray());ClassNode result=new ClassNode();new ClassReader(out).accept(result,0);int hooks=0;
   for(MethodNode method:result.methods)if(method.name.equals(srg?"func_78087_a":"setRotationAngles")){new Analyzer(new BasicVerifier()).analyze(result.name,method);for(AbstractInsnNode ins:method.instructions.toArray())if(ins instanceof MethodInsnNode&&((MethodInsnNode)ins).owner.endsWith("CrossbowPlayerPoseHooks142"))hooks++;}
   check(hooks==1,"actual ModelBiped pose hook MCP/SRG "+srg);
  }
  System.out.println("PASS crossbow pose142: "+checks);
 }
}
