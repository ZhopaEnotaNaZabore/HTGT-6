package com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;

/** Charge, wait and fire phases; arrows are committed through the same crossbow path as players. */
public final class PillagerCrossbowGoal139 extends EntityAIBase {
    private final EntityPillager mob;
    private int visibleTicks, delay, pathDelay;
    private ItemStack activeCrossbow;
    public PillagerCrossbowGoal139(EntityPillager mob) { this.mob = mob; setMutexBits(3); }
    @Override public boolean shouldExecute() {
        ItemStack held = mob.getHeldItem();
        return mob.getAttackTarget() != null && mob.getAttackTarget().isEntityAlive()
                && held != null && held.getItem() instanceof ItemCrossbow;
    }
    @Override public boolean continueExecuting() { return shouldExecute(); }
    @Override public void resetTask() {
        visibleTicks = 0; delay = 0; pathDelay = 0; activeCrossbow = null; mob.setCrossbowChargeTicks(0);
        mob.getNavigator().clearPathEntity();
    }
    @Override public void updateTask() {
        EntityLivingBase target = mob.getAttackTarget();
        if (mob.worldObj.isRemote) return;
        if (target == null || !target.isEntityAlive()) { resetTask(); return; }
        boolean visible = mob.getEntitySenses().canSee(target);
        visibleTicks = visible ? Math.max(0, visibleTicks) + 1 : Math.min(0, visibleTicks) - 1;
        ItemStack held = mob.getHeldItem();
        if (held == null || !(held.getItem() instanceof ItemCrossbow)) { resetTask(); return; }
        if (activeCrossbow != held) {
            // Charge belongs to this equipped stack, never to a replacement weapon.
            mob.setCrossbowChargeTicks(0); delay = 0; activeCrossbow = held;
        }
        boolean charged = ItemCrossbow.isCharged(held);
        boolean approach = (mob.getDistanceSqToEntity(target) > 64 || visibleTicks < 5) && delay == 0;
        if (approach) {
            // Modern goal retries navigation every 20..40 ticks, not every tick.
            if (--pathDelay <= 0) {
                mob.getNavigator().tryMoveToEntityLiving(target,
                        charged || mob.getCrossbowChargeTicks() > 0 ? .5D : 1D);
                pathDelay = 20 + mob.getRNG().nextInt(21);
            }
        } else { pathDelay = 0; mob.getNavigator().clearPathEntity(); }
        mob.getLookHelper().setLookPositionWithEntity(target,30,30);
        if (charged) {
            mob.setCrossbowChargeTicks(0);
            if (delay > 0) --delay;
            else if (visible && ItemCrossbow.fire(held, mob.worldObj, mob, target)) delay = 0;
        } else if (mob.getCrossbowChargeTicks() > 0) {
            int ticks = mob.getCrossbowChargeTicks() + 1;
            mob.setCrossbowChargeTicks(ticks);
            if (ticks >= ItemCrossbow.chargeDuration(held) && ItemCrossbow.loadMob(mob,held)) {
                mob.setCrossbowChargeTicks(0);
                delay = 20 + mob.getRNG().nextInt(20);
                mob.worldObj.playSoundAtEntity(mob,"htgt6:item.crossbow.loading_end",1,1);
            }
        } else if (visibleTicks >= 5 && mob.getDistanceSqToEntity(target) <= 64) {
            mob.setCrossbowChargeTicks(1);
            mob.worldObj.playSoundAtEntity(mob,ItemCrossbow.chargeStartSound(held),1,1);
        }
    }
}
