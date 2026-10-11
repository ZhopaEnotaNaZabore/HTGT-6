package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage.RaidAndPatrolHandler;
import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/** Legacy navigation adapter for PathfindToRaidGoal; combat always takes precedence. */
public final class RaidApproachGoal152 extends EntityAIBase {
    private final EntityCreature mob;
    public RaidApproachGoal152(EntityCreature mob){this.mob=mob;setMutexBits(1);}
    public static void attach(EntityCreature mob){
        for(Object value:mob.tasks.taskEntries)
            if(((EntityAITasks.EntityAITaskEntry)value).action instanceof RaidApproachGoal152)return;
        mob.tasks.addTask(3,new RaidApproachGoal152(mob));
    }
    @Override public boolean shouldExecute(){
        if(mob.worldObj.isRemote||!mob.isEntityAlive()||mob.getAttackTarget()!=null||mob.ridingEntity!=null
                ||RaidAndPatrolHandler.activeRaidCenter(mob)==null)return false;
        int x=MathHelper.floor_double(mob.posX),y=MathHelper.floor_double(mob.posY),z=MathHelper.floor_double(mob.posZ);
        return RaidAndPatrolHandler.legacyVillage(mob.worldObj,x,y,z,0)==null
                &&VillagePoiData120.findVillageCenter(mob.worldObj,x,y,z,16)==null;
    }
    @Override public boolean continueExecuting(){return shouldExecute();}
    @Override public void updateTask(){
        if(!shouldExecute()||!mob.getNavigator().noPath())return;
        ChunkCoordinates center=RaidAndPatrolHandler.activeRaidCenter(mob);if(center==null)return;
        int x=MathHelper.floor_double(mob.posX),z=MathHelper.floor_double(mob.posZ);
        // RandomPositionGenerator and legacy pathfinding may read blocks: guard their
        // complete local search area before either runs. Never pull distant chunks.
        int radius=Math.max(17,(int)(mob.getNavigator().getPathSearchRange()+8F));
        if(!mob.worldObj.checkChunksExist(x-radius,0,z-radius,x+radius,mob.worldObj.getHeight()-1,z+radius))return;
        Vec3 destination=RandomPositionGenerator.findRandomTargetBlockTowards(mob,15,4,
                Vec3.createVectorHelper(center.posX+.5D,center.posY+.5D,center.posZ+.5D));
        if(destination!=null)mob.getNavigator().tryMoveToXYZ(destination.xCoord,destination.yCoord,destination.zCoord,1D);
    }
    @Override public void resetTask(){mob.getNavigator().clearPathEntity();}
}
