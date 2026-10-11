package com.mod.htgt6.minecraft1202.Module.Entity.Frostburn;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

/** Arrow carrying the Stray's thirty-second Slowness effect. */
public final class EntityFrostburnStrayArrow extends com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatArrow {
    public EntityFrostburnStrayArrow(World world) {
        super(world);
        initializeSlowness();
    }

    public EntityFrostburnStrayArrow(World world, EntityLivingBase shooter,
                                     EntityLivingBase target, float velocity, float inaccuracy) {
        super(world, shooter, target, velocity, inaccuracy);
        initializeSlowness();
    }
    private void initializeSlowness() {
        dataWatcher.updateObject(20,Integer.valueOf(TYPE_TIPPED));
        net.minecraft.nbt.NBTTagCompound tag=new net.minecraft.nbt.NBTTagCompound();
        net.minecraft.nbt.NBTTagList effects=new net.minecraft.nbt.NBTTagList();
        effects.appendTag(new net.minecraft.potion.PotionEffect(net.minecraft.potion.Potion.moveSlowdown.id,600,0)
                .writeCustomPotionEffectToNBT(new net.minecraft.nbt.NBTTagCompound()));
        tag.setTag("CustomPotionEffects",effects);setPotionTag(tag);
    }
    @Override public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        // Old saves stored only legacy EntityArrow data.
        if(!tag.hasKey("CombatType"))initializeSlowness();
    }
}
