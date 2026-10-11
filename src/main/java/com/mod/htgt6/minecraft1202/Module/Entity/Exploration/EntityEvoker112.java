package com.mod.htgt6.minecraft1202.Module.Entity.Exploration;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.IRaider112;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;

import java.util.List;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Minecraft 1.12 Evoker task ordering, spell warmups and cooldowns. */
public final class EntityEvoker112 extends EntityMob implements IRaider112 {
    private static final int SPELL=20;
    private int spellTicks;
    private EntitySheep wololoTarget;
    public EntityEvoker112(World world){super(world);setSize(.6F,1.95F);experienceValue=10;
        tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(1,new CastingAI());tasks.addTask(2,new EntityAIAvoidEntity(this,EntityPlayer.class,8,.6,1));tasks.addTask(4,new SummonSpellAI());tasks.addTask(5,new FangSpellAI());tasks.addTask(6,new WololoSpellAI());tasks.addTask(8,new EntityAIWander(this,.6));tasks.addTask(9,new EntityAIWatchClosest(this,EntityPlayer.class,3,1));tasks.addTask(10,new EntityAIWatchClosest(this,EntityLiving.class,8));
        targetTasks.addTask(1,new EntityAIHurtByTarget(this,true));targetTasks.addTask(2,new EntityAINearestAttackableTarget(this,EntityPlayer.class,300,true));targetTasks.addTask(3,new EntityAINearestAttackableTarget(this,EntityVillager.class,300,false));targetTasks.addTask(3,new EntityAINearestAttackableTarget(this,EntityIronGolem.class,0,false));
    }
    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(SPELL,Byte.valueOf((byte)0));}
    // Forge 1.7.10 requires this opt-in to execute tasks and targetTasks.
    @Override protected boolean isAIEnabled() { return true; }

    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(24);getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.5);getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(12);}
    public boolean isSpellcasting(){return worldObj.isRemote?dataWatcher.getWatchableObjectByte(SPELL)>0:spellTicks>0;}
    private void setSpell(int id,int ticks){spellTicks=ticks;dataWatcher.updateObject(SPELL,Byte.valueOf((byte)id));}
    @Override protected void updateAITasks(){super.updateAITasks();if(spellTicks>0&&--spellTicks==0)dataWatcher.updateObject(SPELL,Byte.valueOf((byte)0));}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(worldObj.isRemote&&isSpellcasting()){int id=dataWatcher.getWatchableObjectByte(SPELL);double r=id==1?.7:id==2?.4:.7,g=id==1?.7:id==2?.3:.5,b=id==1?.8:id==2?.35:.2;float f=renderYawOffset*((float)Math.PI/180F)+MathHelper.cos(ticksExisted*.6662F)*.25F;double x=MathHelper.cos(f)*.6,z=MathHelper.sin(f)*.6;worldObj.spawnParticle("mobSpell",posX+x,posY+1.8,posZ+z,r,g,b);worldObj.spawnParticle("mobSpell",posX-x,posY+1.8,posZ-z,r,g,b);}}
    @Override protected void dropFewItems(boolean hit,int looting){entityDropItem(new net.minecraft.item.ItemStack(ExplorationContent112.totemOfUndying),0);if(hit){int count=com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.ExplorationLoot120.count(rand,1,looting);if(count>0)dropItem(Items.emerald,count);}}

    private final class CastingAI extends EntityAIBase{CastingAI(){setMutexBits(3);}@Override public boolean shouldExecute(){return spellTicks>0;}@Override public void startExecuting(){getNavigator().clearPathEntity();}@Override public void resetTask(){if(spellTicks<=0)dataWatcher.updateObject(SPELL,Byte.valueOf((byte)0));}@Override public void updateTask(){if(getAttackTarget()!=null)getLookHelper().setLookPositionWithEntity(getAttackTarget(),getRotationYawHead(),getVerticalFaceSpeed());else if(wololoTarget!=null)getLookHelper().setLookPositionWithEntity(wololoTarget,getRotationYawHead(),getVerticalFaceSpeed());}}
    private abstract class SpellAI extends EntityAIBase{int warmup,nextStart;SpellAI(){}@Override public boolean shouldExecute(){return getAttackTarget()!=null&&getAttackTarget().isEntityAlive()&&!isSpellcasting()&&ticksExisted>=nextStart;}@Override public boolean continueExecuting(){return getAttackTarget()!=null&&getAttackTarget().isEntityAlive()&&warmup>0;}@Override public void startExecuting(){warmup=warmupTicks();setSpell(spellId(),castingTicks());nextStart=ticksExisted+interval();}@Override public void updateTask(){if(--warmup==0)cast();}int warmupTicks(){return 20;}abstract int castingTicks();abstract int interval();abstract int spellId();abstract void cast();}
    private final class SummonSpellAI extends SpellAI{@Override public boolean shouldExecute(){if(!super.shouldExecute())return false;int count=worldObj.getEntitiesWithinAABB(EntityVex112.class,boundingBox.expand(16,16,16)).size();return rand.nextInt(8)+1>count;}int castingTicks(){return 100;}int interval(){return 340;}int spellId(){return 1;}void cast(){int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(posY),z=MathHelper.floor_double(posZ);for(int i=0;i<3;i++){EntityVex112 vex=new EntityVex112(worldObj);int vx=x-2+rand.nextInt(5),vy=y+1,vz=z-2+rand.nextInt(5);vex.setLocationAndAngles(vx+.5,vy,vz+.5,0,0);vex.setOwner(EntityEvoker112.this);vex.setBoundOrigin(vx,vy,vz);vex.setLimitedLife(20*(30+rand.nextInt(90)));vex.setCurrentItemOrArmor(0,new net.minecraft.item.ItemStack(Items.iron_sword));worldObj.spawnEntityInWorld(vex);}}}
    private final class FangSpellAI extends SpellAI{int castingTicks(){return 40;}int interval(){return 100;}int spellId(){return 2;}void cast(){EntityLivingBase target=getAttackTarget();if(target==null)return;double low=Math.min(target.posY,posY),high=Math.max(target.posY,posY)+1;float angle=(float)Math.atan2(target.posZ-posZ,target.posX-posX);if(getDistanceSqToEntity(target)<9){for(int i=0;i<5;i++)fang(posX+Math.cos(angle+i*Math.PI*.4)*1.5,posZ+Math.sin(angle+i*Math.PI*.4)*1.5,low,high,angle+i*(float)Math.PI*.4F,0);for(int i=0;i<8;i++){float a=angle+i*(float)Math.PI*2/8+1.2566371F;fang(posX+Math.cos(a)*2.5,posZ+Math.sin(a)*2.5,low,high,a,3);}}else for(int i=0;i<16;i++)fang(posX+Math.cos(angle)*1.25*(i+1),posZ+Math.sin(angle)*1.25*(i+1),low,high,angle,i);}}
    private final class WololoSpellAI extends SpellAI{@SuppressWarnings("unchecked")@Override public boolean shouldExecute(){if(getAttackTarget()!=null||isSpellcasting()||ticksExisted<nextStart||!worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))return false;List<EntitySheep> sheep=worldObj.getEntitiesWithinAABB(EntitySheep.class,boundingBox.expand(16,4,16));for(EntitySheep s:sheep)if(s.getFleeceColor()==11){wololoTarget=s;return true;}return false;}@Override public boolean continueExecuting(){return wololoTarget!=null&&wololoTarget.isEntityAlive()&&warmup>0;}@Override public void resetTask(){wololoTarget=null;}int warmupTicks(){return 40;}int castingTicks(){return 60;}int interval(){return 140;}int spellId(){return 3;}void cast(){if(wololoTarget!=null&&wololoTarget.isEntityAlive())wololoTarget.setFleeceColor(14);}}
    private void fang(double x,double z,double low,double high,float yaw,int delay) {
        int bx=MathHelper.floor_double(x),bz=MathHelper.floor_double(z),y=MathHelper.floor_double(high);
        while(y>=MathHelper.floor_double(low)-1) {
            if(worldObj.getBlock(bx,y-1,bz).isSideSolid(worldObj,bx,y-1,bz,net.minecraftforge.common.util.ForgeDirection.UP)) {
                net.minecraft.util.AxisAlignedBB shape=worldObj.getBlock(bx,y,bz).getCollisionBoundingBoxFromPool(worldObj,bx,y,bz);
                double offset=shape==null?0:Math.max(0,shape.maxY-y);
                worldObj.spawnEntityInWorld(new EntityEvokerFangs112(worldObj,x,y+offset,z,yaw,delay,this));
                return;
            }
            --y;
        }
    }
}