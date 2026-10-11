package com.mod.htgt6.minecraft1202.Module.Entity.Bountiful;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.EnumCreatureAttribute;

public final class EntityBountifulEndermite extends EntityMob {
    private int lifetime;
    private boolean playerSpawned;
    public EntityBountifulEndermite(World world){super(world);setSize(0.4F,0.3F);experienceValue=3;tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(2,new EntityAIAttackOnCollide(this,1.0D,false));tasks.addTask(3,new EntityAIWander(this,0.8D));targetTasks.addTask(1,new EntityAIHurtByTarget(this,true));targetTasks.addTask(2,new EntityAINearestAttackableTarget(this,EntityPlayer.class,0,true));}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8);getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(2);}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(!worldObj.isRemote
            &&!isNoDespawnRequired()&&++lifetime>=2400)setDead();}
    public void setPlayerSpawned(boolean value){playerSpawned=value;}
    public boolean isPlayerSpawned(){return playerSpawned;}
    @Override public EnumCreatureAttribute getCreatureAttribute(){return EnumCreatureAttribute.ARTHROPOD;}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);
        tag.setInteger("Lifetime",lifetime);tag.setBoolean("PlayerSpawned",playerSpawned);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);
        lifetime=tag.getInteger("Lifetime");playerSpawned=tag.getBoolean("PlayerSpawned");}
}
