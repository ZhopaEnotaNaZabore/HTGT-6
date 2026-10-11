package com.mod.htgt6.minecraft1202.Module.Entity.Combat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/** Throwable carrier that creates the lingering cloud at the impact position. */
public final class EntityLingeringPotion extends EntityThrowable {
    private int potionDamage;
    private NBTTagCompound potionTag = new NBTTagCompound();

    public EntityLingeringPotion(World world) { super(world); }
    public EntityLingeringPotion(World world, EntityLivingBase thrower, int potionDamage) {
        super(world, thrower);
        this.potionDamage = potionDamage;
    }

    public EntityLingeringPotion(World world, EntityLivingBase thrower, net.minecraft.item.ItemStack stack) {
        this(world, thrower, stack.getItemDamage());
        if (stack.hasTagCompound()) potionTag = (NBTTagCompound) stack.getTagCompound().copy();
    }

    @Override
    protected void onImpact(MovingObjectPosition hit) {
        if (!worldObj.isRemote) {
            EntityLingeringCloud cloud = new EntityLingeringCloud(worldObj, getThrower(), potionDamage);
            cloud.setPotionTag(potionTag);
            cloud.setPosition(posX, posY, posZ);
            worldObj.spawnEntityInWorld(cloud);
            worldObj.playAuxSFX(2002, (int) Math.floor(posX), (int) Math.floor(posY),
                    (int) Math.floor(posZ), potionDamage);
            setDead();
        }
    }

    @Override protected float getGravityVelocity(){return .05F;}
    @Override protected float func_70182_d(){return .5F;}
    @Override protected float func_70183_g(){return -20F;}

    @Override public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("Potion", potionDamage);tag.setTag("PotionTag", potionTag.copy());
    }
    @Override public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        potionDamage = tag.getInteger("Potion");potionTag = (NBTTagCompound) tag.getCompoundTag("PotionTag").copy();
    }
}
