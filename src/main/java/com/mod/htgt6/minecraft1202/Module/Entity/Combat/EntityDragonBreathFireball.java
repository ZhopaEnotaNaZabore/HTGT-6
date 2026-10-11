package com.mod.htgt6.minecraft1202.Module.Entity.Combat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/** Dragon fireball adapter: its impact creates the collectible damaging breath cloud. */
public final class EntityDragonBreathFireball extends EntityFireball {
    public EntityDragonBreathFireball(World world) { super(world); }
    public EntityDragonBreathFireball(World world, EntityLivingBase dragon, double x, double y, double z) {
        super(world, dragon, x, y, z);
    }

    @Override protected void onImpact(MovingObjectPosition hit) {
        if (worldObj.isRemote) return;
        if (hit.entityHit == shootingEntity && shootingEntity != null) return;
        EntityLingeringCloud cloud = EntityLingeringCloud.dragonBreath(worldObj, posX, posY, posZ);
        cloud.setOwner(shootingEntity);
        for(Object value:worldObj.getEntitiesWithinAABB(EntityLivingBase.class,boundingBox.expand(4,2,4))){
            EntityLivingBase target=(EntityLivingBase)value;
            if(getDistanceSqToEntity(target)<16){cloud.setPosition(target.posX,target.posY,target.posZ);break;}
        }
        worldObj.spawnEntityInWorld(cloud);
        worldObj.playAuxSFX(2002, (int) Math.floor(posX), (int) Math.floor(posY),
                (int) Math.floor(posZ), 0);
        setDead();
    }
    @Override public boolean canBeCollidedWith(){return false;}
    @Override public boolean attackEntityFrom(DamageSource source,float amount){return false;}
}
