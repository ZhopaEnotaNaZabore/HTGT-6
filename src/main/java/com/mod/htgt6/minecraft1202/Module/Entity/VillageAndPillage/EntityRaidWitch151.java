package com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage.RaidAndPatrolHandler;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.IRaider112;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** Raid-spawned witch: vanilla drinking/offense plus modern raider support targeting. */
public final class EntityRaidWitch151 extends EntityWitch implements IRaider112 {
    private int healCooldown;
    public EntityRaidWitch151(World world) {
        super(world);
        for(Object value:new ArrayList(targetTasks.taskEntries)) {
            EntityAITasks.EntityAITaskEntry entry=(EntityAITasks.EntityAITaskEntry)value;
            if(entry.action instanceof EntityAINearestAttackableTarget)targetTasks.removeTask(entry.action);
        }
        targetTasks.addTask(2,new HealTarget());
        targetTasks.addTask(3,new EntityAINearestAttackableTarget(this,EntityPlayer.class,10,true){
            @Override public boolean shouldExecute(){return healCooldown<=0&&super.shouldExecute();}
        });
    }
    @Override public void onLivingUpdate() {
        if(!worldObj.isRemote&&isEntityAlive()&&healCooldown>0)--healCooldown;
        super.onLivingUpdate();
    }
    @Override public void attackEntityWithRangedAttack(EntityLivingBase target,float distance) {
        if(worldObj.isRemote||target==null||!target.isEntityAlive())return;
        if(!(target instanceof IRaider112)){super.attackEntityWithRangedAttack(target,distance);return;}
        if(getAggressive())return; // Vanilla drinking watcher, not attack hostility.
        EntityPotion potion=new EntityPotion(worldObj,this,supportPotion(target.getHealth()));
        potion.rotationPitch-= -20F;
        double x=target.posX+target.motionX-posX,z=target.posZ+target.motionZ-posZ;
        double y=target.posY+target.getEyeHeight()-1.100000023841858D-posY;
        double horizontal=Math.sqrt(x*x+z*z);
        potion.setThrowableHeading(x,y+horizontal*.2D,z,.75F,8F);
        setAttackTarget(null);
        worldObj.spawnEntityInWorld(potion); // Normal projectile path preserves impact/effect events.
    }
    public static ItemStack supportPotion(float health) {
        ItemStack item=new ItemStack(Items.potionitem,1,16384);
        NBTTagCompound tag=new NBTTagCompound(),effect=new NBTTagCompound();NBTTagList effects=new NBTTagList();
        new PotionEffect(health<=4?Potion.heal.id:Potion.regeneration.id,health<=4?1:900,0).writeCustomPotionEffectToNBT(effect);
        effects.appendTag(effect);tag.setTag("CustomPotionEffects",effects);item.setTagCompound(tag);return item;
    }
    private final class HealTarget extends EntityAITarget {
        private EntityLivingBase selected;
        HealTarget(){super(EntityRaidWitch151.this,true);setMutexBits(1);}
        @Override public boolean shouldExecute() {
            if(healCooldown>0||!rand.nextBoolean()||!RaidAndPatrolHandler.isActiveRaider(EntityRaidWitch151.this))return false;
            selected=null;double nearest=getTargetDistance()*getTargetDistance();
            List<EntityLivingBase> nearby=worldObj.getEntitiesWithinAABB(EntityLivingBase.class,boundingBox.expand(getTargetDistance(),4,getTargetDistance()));
            for(EntityLivingBase entity:nearby) {
                if(!(entity instanceof IRaider112)||entity instanceof EntityWitch||!isSuitableTarget(entity,false))continue;
                double distance=getDistanceSqToEntity(entity);if(distance<nearest){nearest=distance;selected=entity;}
            }
            return selected!=null;
        }
        @Override public void startExecuting() {
            // Modern Goal.reducedTickDelay(200) is 100; aiStep decrements every tick.
            healCooldown=100;setAttackTarget(selected);super.startExecuting();
        }
        @Override public boolean continueExecuting(){return getAttackTarget()==selected&&super.continueExecuting();}
        @Override public void resetTask(){selected=null;super.resetTask();}
    }
}
