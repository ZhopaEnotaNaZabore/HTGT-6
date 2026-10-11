package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.*;
import net.minecraft.world.EnumDifficulty;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVindicator112;
import com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage.RaidAndPatrolHandler;
import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.*;

/** Modern raid equipment and navigation expressed through the legacy task scheduler. */
public final class RaidMechanics120 {
    private RaidMechanics120() {}
    public static float enchantOdds(int omen) {return omen<=1?0:omen==2?.1F:omen==3?.25F:omen==4?.5F:.75F;}
    public static void applyBuffs(EntityLiving mob,int wave,int omen) {
        if(!(mob instanceof EntityPillager)&&!(mob instanceof EntityVindicator112))return;
        boolean enchanted=mob.getRNG().nextFloat()<=enchantOdds(omen);
        if(mob instanceof EntityPillager) {
            if(!enchanted)return;
            CrossbowEnchantments140.init();
            ItemStack bow=new ItemStack(VillagePillageContent.crossbow);
            if(wave>3)bow.addEnchantment(CrossbowEnchantments140.quickCharge,wave>5?2:1);
            bow.addEnchantment(CrossbowEnchantments140.multishot,1);
            mob.setCurrentItemOrArmor(0,bow);
        } else {
            ItemStack axe=new ItemStack(Items.iron_axe);
            if(enchanted)axe.addEnchantment(Enchantment.sharpness,wave>5?2:1);
            mob.setCurrentItemOrArmor(0,axe);
        }
    }
    public static void attachVillageGoals(EntityCreature mob) {
        for(Object value:mob.tasks.taskEntries)
            if(((EntityAITasks.EntityAITaskEntry)value).action instanceof MoveThroughVillage)return;
        mob.tasks.addTask(4,new MoveThroughVillage(mob));
        mob.tasks.addTask(3,new Celebrate(mob));
        if(mob instanceof EntityVindicator112) {
            mob.getNavigator().setBreakDoors(true);
            mob.tasks.addTask(1,new RaidBreakDoor(mob));
            mob.tasks.addTask(2,new EntityAIOpenDoor(mob,true) {
                @Override public boolean shouldExecute(){return RaidAndPatrolHandler.isActiveRaider(mob)&&super.shouldExecute();}
            });
        }
    }
    private static final class Celebrate extends EntityAIBase {
        private final EntityCreature mob;
        Celebrate(EntityCreature mob){this.mob=mob;setMutexBits(1);}
        @Override public boolean shouldExecute(){return mob.isEntityAlive()&&mob.getAttackTarget()==null&&RaidAndPatrolHandler.lostRaid(mob);}
        @Override public void startExecuting(){mob.getNavigator().clearPathEntity();}
        @Override public void updateTask(){
            if(mob.getRNG().nextInt(50)==0&&mob.onGround)mob.getJumpHelper().setJumping();
            if(mob.getRNG().nextInt(100)==0) {
                String kind=mob instanceof EntityPillager?"pillager":mob instanceof EntityVindicator112?"vindicator":
                    mob instanceof com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityEvoker112?"evoker":
                    mob instanceof net.minecraft.entity.monster.EntityWitch?"witch":"ravager";
                mob.playSound("htgt6:entity."+kind+".celebrate",1,1);
            }
        }
    }
    public static boolean pathRegionLoaded(EntityCreature mob) {
        int r=Math.max(17,(int)mob.getNavigator().getPathSearchRange()+8),x=MathHelper.floor_double(mob.posX),z=MathHelper.floor_double(mob.posZ);
        return mob.worldObj.checkChunksExist(x-r,0,z-r,x+r,mob.worldObj.getHeight()-1,z+r);
    }
    /** Visit occupied homes, remembering the last two; combat remains higher priority. */
    public static final class MoveThroughVillage extends EntityAIBase {
        private final EntityCreature mob;
        private final List<ChunkCoordinates> visited=new ArrayList<ChunkCoordinates>();
        private ChunkCoordinates target;
        private int retry;
        public MoveThroughVillage(EntityCreature mob){this.mob=mob;setMutexBits(1);}
        @Override public boolean shouldExecute(){
            if(retry>0){retry--;return false;}
            if(mob.getAttackTarget()!=null||mob.ridingEntity!=null||!RaidAndPatrolHandler.isActiveRaider(mob)||!pathRegionLoaded(mob))return false;
            List<VillagePoiData120.Entry> homes=VillagePoiData120.get(mob.worldObj).occupiedNear(mob.worldObj,mob.posX,mob.posY,mob.posZ,48);
            Collections.shuffle(homes,mob.getRNG());
            for(VillagePoiData120.Entry e:homes) {
                ChunkCoordinates point=new ChunkCoordinates(e.x,e.y,e.z);
                if("home".equals(e.type)&&!visited.contains(point)){target=point;return true;}
            }
            retry=20;return false;
        }
        @Override public void startExecuting(){move();}
        private void move(){
            if(target==null||!pathRegionLoaded(mob))return;
            if(!mob.getNavigator().tryMoveToXYZ(target.posX+.5,target.posY,target.posZ+.5,1.05)) {
                Vec3 step=RandomPositionGenerator.findRandomTargetBlockTowards(mob,16,7,Vec3.createVectorHelper(target.posX,target.posY,target.posZ));
                if(step!=null)mob.getNavigator().tryMoveToXYZ(step.xCoord,step.yCoord,step.zCoord,1.05);
            }
        }
        @Override public boolean continueExecuting(){return target!=null&&mob.getAttackTarget()==null&&mob.ridingEntity==null
                &&RaidAndPatrolHandler.isActiveRaider(mob)&&!mob.getNavigator().noPath()&&mob.getDistanceSq(target.posX,target.posY,target.posZ)>4;}
        @Override public void resetTask(){if(target!=null){visited.add(target);while(visited.size()>2)visited.remove(0);}target=null;retry=20;mob.getNavigator().clearPathEntity();}
    }
    /** Vanilla vindicators use the vanilla minimum of 240 ticks to break wooden doors on Normal/Hard during raids. */
    private static final class RaidBreakDoor extends EntityAIDoorInteract {
        private int ticks;
        RaidBreakDoor(EntityLiving mob){super(mob);}
        private boolean allowed(){return RaidAndPatrolHandler.isActiveRaider(theEntity)
                &&theEntity.worldObj.difficultySetting.getDifficultyId()>=EnumDifficulty.NORMAL.getDifficultyId()
                &&theEntity.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");}
        @Override public boolean shouldExecute(){return allowed()&&theEntity.getRNG().nextInt(10)==0&&super.shouldExecute()
                &&!field_151504_e.func_150015_f(theEntity.worldObj,entityPosX,entityPosY,entityPosZ);}
        @Override public void startExecuting(){super.startExecuting();ticks=0;}
        @Override public boolean continueExecuting(){return allowed()&&ticks<240&&theEntity.getDistanceSq(entityPosX,entityPosY,entityPosZ)<4
                &&theEntity.worldObj.getBlock(entityPosX,entityPosY,entityPosZ)==field_151504_e
                &&!field_151504_e.func_150015_f(theEntity.worldObj,entityPosX,entityPosY,entityPosZ);}
        @Override public void updateTask(){
            if(!allowed())return;
            ++ticks;
            theEntity.worldObj.destroyBlockInWorldPartially(theEntity.getEntityId(),entityPosX,entityPosY,entityPosZ,ticks*10/240);
            if(ticks%20==0)theEntity.worldObj.playAuxSFX(1010,entityPosX,entityPosY,entityPosZ,0);
            if(ticks==240){theEntity.worldObj.setBlockToAir(entityPosX,entityPosY,entityPosZ);theEntity.worldObj.playAuxSFX(1012,entityPosX,entityPosY,entityPosZ,0);}
        }
        @Override public void resetTask(){super.resetTask();theEntity.worldObj.destroyBlockInWorldPartially(theEntity.getEntityId(),entityPosX,entityPosY,entityPosZ,-1);}
    }
}
