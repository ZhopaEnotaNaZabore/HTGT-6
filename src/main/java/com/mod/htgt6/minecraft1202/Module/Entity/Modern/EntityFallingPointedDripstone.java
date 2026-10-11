package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/** Falling tip with the modern stalactite length multiplier and 40 damage cap. */
public final class EntityFallingPointedDripstone extends EntityFallingBlock {
    private final float damageMultiplier;

    public EntityFallingPointedDripstone(World world, double x, double y, double z, int metadata,
                                  float damageMultiplier) {
        super(world, x, y, z, Backport120Content.pointedDripstone, metadata);
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    protected void fall(float distance) {
        int distanceDamage = MathHelper.ceiling_float_int(distance - 1.0F);
        if (distanceDamage <= 0 || worldObj.isRemote) return;
        float damage = Math.min(40.0F, distanceDamage * damageMultiplier);
        @SuppressWarnings("unchecked")
        List<Entity> entities = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox);
        for (Entity entity : entities) entity.attackEntityFrom(DamageSource.fallingBlock, damage);
    }
}
