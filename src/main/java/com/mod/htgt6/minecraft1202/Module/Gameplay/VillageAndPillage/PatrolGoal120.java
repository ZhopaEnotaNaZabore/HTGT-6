package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import java.util.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.*;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager;
import com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage.RaidAndPatrolHandler;

/** Long-distance patrol navigation; local paths never load ungenerated chunks. */
public final class PatrolGoal120 extends EntityAIBase {
    private final EntityPillager mob;
    private long retryAfter;
    public PatrolGoal120(EntityPillager mob){this.mob=mob;setMutexBits(1);}
    @Override public boolean shouldExecute(){return mob.isPatrolling()&&mob.getPatrolTarget()!=null&&mob.getAttackTarget()==null
            &&mob.ridingEntity==null&&!RaidAndPatrolHandler.isActiveRaider(mob)&&mob.worldObj.getTotalWorldTime()>=retryAfter;}
    @Override public void updateTask(){
        if(!mob.getNavigator().noPath()||!RaidMechanics120.pathRegionLoaded(mob))return;
        List<EntityPillager> companions=new ArrayList<EntityPillager>();
        for(Object value:mob.worldObj.getEntitiesWithinAABB(EntityPillager.class,mob.boundingBox.expand(16,16,16))) {
            EntityPillager other=(EntityPillager)value;
            if(other!=mob&&other.isEntityAlive()&&!RaidAndPatrolHandler.isActiveRaider(other))companions.add(other);
        }
        if(companions.isEmpty()){mob.setPatrolling(false);return;}
        ChunkCoordinates target=mob.getPatrolTarget();
        if(mob.isCaptain()&&mob.getDistanceSq(target.posX,target.posY,target.posZ)<100){mob.findPatrolTarget();return;}
        // The modern goal rotates the target offset by 90 degrees to avoid a rigid line.
        double dx=target.posX-mob.posX,dz=target.posZ-mob.posZ;
        double tx=target.posX+dz*.4-mob.posX,tz=target.posZ-dx*.4-mob.posZ,len=Math.sqrt(tx*tx+tz*tz);
        if(len<.001){mob.findPatrolTarget();return;}
        int x=MathHelper.floor_double(mob.posX+tx/len*10),z=MathHelper.floor_double(mob.posZ+tz/len*10);
        int y=mob.worldObj.getTopSolidOrLiquidBlock(x,z);
        if(mob.getNavigator().tryMoveToXYZ(x,y,z,mob.isCaptain()?.595:.7)) {
            if(mob.isCaptain())for(EntityPillager other:companions)other.setPatrolTarget(new ChunkCoordinates(x,y,z));
        } else {
            x=MathHelper.floor_double(mob.posX)-8+mob.getRNG().nextInt(16);z=MathHelper.floor_double(mob.posZ)-8+mob.getRNG().nextInt(16);
            mob.getNavigator().tryMoveToXYZ(x,mob.worldObj.getTopSolidOrLiquidBlock(x,z),z,.7);
            retryAfter=mob.worldObj.getTotalWorldTime()+200;
        }
    }
}
