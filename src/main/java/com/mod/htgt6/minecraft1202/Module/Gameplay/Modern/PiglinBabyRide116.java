package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIBase;
/** Legacy Riding chains represent the original maximum of three baby piglin passengers. */
public final class PiglinBabyRide116 extends EntityAIBase {
 private final EntityPiglin116 piglin;private EntityHoglin116 target;private int pathDelay;
 public PiglinBabyRide116(EntityPiglin116 piglin){this.piglin=piglin;setMutexBits(1);}
 public static Entity top(EntityHoglin116 hoglin){if(hoglin==null||!hoglin.isEntityAlive()||!hoglin.isChild())return null;Entity top=hoglin;for(int i=0;i<3;i++){if(top.riddenByEntity==null)return top;if(!(top.riddenByEntity instanceof EntityPiglin116)||!((EntityPiglin116)top.riddenByEntity).isChild())return null;top=top.riddenByEntity;}return null;}
 public static boolean validMount(Entity mount){return mount!=null&&mount.isEntityAlive()&&(mount instanceof EntityPiglin116&&((EntityPiglin116)mount).isChild()||mount instanceof EntityHoglin116&&((EntityHoglin116)mount).isChild());}
 @Override public boolean shouldExecute(){
  if(!piglin.isChild()||piglin.ridingEntity!=null||piglin.babyRideCooldown>0||piglin.isAdmiring()||piglin.getAITarget()!=null)return false;
  piglin.babyRideCooldown=200+piglin.getRNG().nextInt(601);target=null;double best=256;
  for(Object o:piglin.worldObj.getEntitiesWithinAABB(EntityHoglin116.class,piglin.boundingBox.expand(16,8,16))){EntityHoglin116 hoglin=(EntityHoglin116)o;double d=piglin.getDistanceSqToEntity(hoglin);if(d<best&&top(hoglin)!=null&&piglin.getEntitySenses().canSee(hoglin)){best=d;target=hoglin;}}
  return target!=null;
 }
 @Override public void startExecuting(){piglin.babyRideTime=200+piglin.getRNG().nextInt(401);pathDelay=0;}
 @Override public boolean continueExecuting(){return piglin.isChild()&&piglin.ridingEntity==null&&piglin.babyRideTime>0&&!piglin.isAdmiring()&&piglin.getAITarget()==null&&top(target)!=null&&piglin.getDistanceSqToEntity(target)<256;}
 @Override public void updateTask(){if(piglin.getDistanceSqToEntity(target)<2.25){Entity mount=top(target);if(mount!=null){piglin.mountEntity(mount);piglin.getNavigator().clearPathEntity();}return;}if(--pathDelay<=0){pathDelay=20;piglin.getNavigator().tryMoveToEntityLiving(target,.8);}}
 @Override public void resetTask(){piglin.getNavigator().clearPathEntity();target=null;}
}
