package com.mod.htgt6.minecraft1202.Module.Entity.Exploration;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.IRaider112;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Minecraft 1.12 vindicator, including the persistent Johnny rule. */
public final class EntityVindicator112 extends EntityMob implements IRaider112 {
    private boolean johnny;

    public EntityVindicator112(World world) {
        super(world);
        setSize(0.6F, 1.95F);
        experienceValue = 5;
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(4, new EntityAIAttackOnCollide(this, 1.0D, false));
        tasks.addTask(8, new EntityAIWander(this, 0.6D));
        tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 3.0F, 1.0F));
        tasks.addTask(10, new EntityAIWatchClosest(this, net.minecraft.entity.EntityLiving.class, 8.0F));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityVillager.class, 0, false));
        targetTasks.addTask(4, new EntityAINearestAttackableTarget(this, EntityIronGolem.class, 0, false));
        targetTasks.addTask(4, new EntityAINearestAttackableTarget(this, net.minecraft.entity.EntityLivingBase.class,
                0, true, true, new net.minecraft.command.IEntitySelector() {
                    @Override public boolean isEntityApplicable(net.minecraft.entity.Entity entity) {
                        return johnny && entity instanceof net.minecraft.entity.EntityLivingBase
                                && entity != EntityVindicator112.this
                                && !(entity instanceof IRaider112);
                    }
                }));
    }

    // Forge 1.7.10 requires this opt-in to execute tasks and targetTasks.
    @Override protected boolean isAIEnabled() { return true; }

    @Override protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(24.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.35D);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(5.0D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32.0D);
    }

    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
        data = super.onSpawnWithEgg(data);
        setCurrentItemOrArmor(0, new ItemStack(Items.iron_axe));
        return data;
    }

    @Override public void setCustomNameTag(String name) {
        super.setCustomNameTag(name);
        if ("Johnny".equals(name)) johnny = true;
    }

    public boolean isJohnny() { return johnny; }
    public boolean isAggressive() { return getAttackTarget() != null; }

    @Override public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setBoolean("Johnny", johnny);
    }

    @Override public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        johnny = tag.getBoolean("Johnny") || "Johnny".equals(getCustomNameTag());
    }

    @Override protected void dropFewItems(boolean recentlyHit, int looting) {
        super.dropFewItems(recentlyHit, looting);
        if (recentlyHit) {
            int count=com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.ExplorationLoot120.count(rand,1,looting);
            if(count>0)dropItem(Items.emerald,count);
        }
        // The equipped axe is handled by dropEquipment; do not generate a second independent axe.
    }
}
