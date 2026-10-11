package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;

/** Shared hoglin/zoglin combat, independent of their animal/undead inheritance. */
public final class HoglinCombat116 {
    public interface Animated {int attackAnimationTicks();}
    public static boolean attack(EntityLivingBase attacker,Entity target){
        if(!(target instanceof EntityLivingBase))return false;
        float base=(float)attacker.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        float damage=attacker.isChild()||base<1?base:base/2+attacker.getRNG().nextInt((int)base);
        boolean hit=target.attackEntityFrom(DamageSource.causeMobDamage(attacker),damage);
        if(hit){net.minecraft.enchantment.EnchantmentHelper.func_151384_a((EntityLivingBase)target,attacker);net.minecraft.enchantment.EnchantmentHelper.func_151385_b(attacker,target);if(!attacker.isChild())throwTarget(attacker,(EntityLivingBase)target);}
        return hit;
    }
    public static void throwTarget(EntityLivingBase attacker,EntityLivingBase target){
        double resistance=target.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue(),force=1-resistance;
        if(force<=0)return;double dx=target.posX-attacker.posX,dz=target.posZ-attacker.posZ,length=Math.sqrt(dx*dx+dz*dz);
        float angle=(attacker.getRNG().nextInt(21)-10);double push=force*(attacker.getRNG().nextFloat()*.5F+.2F),vertical=force*attacker.getRNG().nextFloat()*.5F;
        if(length>1E-5){dx=dx/length*push;dz=dz/length*push;}else{dx=0;dz=0;}
        // Vanilla Vec3.yRot takes radians, including the original [-10,10] input.
        double cosine=Math.cos(angle),sine=Math.sin(angle);
        target.addVelocity(dx*cosine+dz*sine,vertical,dz*cosine-dx*sine);target.velocityChanged=true;
    }
    public static final class Melee extends EntityAIBase {
        private final EntityCreature owner;private int cooldown,pathDelay,lostSight;
        public Melee(EntityCreature owner){this.owner=owner;setMutexBits(3);}
        public boolean shouldExecute(){return owner.getAttackTarget()!=null&&owner.getAttackTarget().isEntityAlive();}
        public boolean continueExecuting(){return shouldExecute()&&lostSight<200;}
        public void resetTask(){owner.getNavigator().clearPathEntity();if(lostSight>=200)owner.setAttackTarget(null);lostSight=0;}
        public void updateTask(){
            EntityLivingBase target=owner.getAttackTarget();if(target==null)return;
            boolean seen=owner.getEntitySenses().canSee(target);lostSight=seen?0:lostSight+1;
            owner.getLookHelper().setLookPositionWithEntity(target,30,30);
            if(--pathDelay<=0){owner.getNavigator().tryMoveToEntityLiving(target,1);pathDelay=10;}
            if(cooldown>0)cooldown--;
            double reach=owner.width*2;reach=reach*reach+target.width;
            if(seen&&cooldown==0&&owner.getDistanceSq(target.posX,target.boundingBox.minY,target.posZ)<=reach){owner.attackEntityAsMob(target);cooldown=owner.isChild()?15:40;}
        }
    }
    private HoglinCombat116(){}
}
