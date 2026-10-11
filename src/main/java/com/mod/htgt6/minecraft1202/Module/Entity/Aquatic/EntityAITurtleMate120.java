package com.mod.htgt6.minecraft1202.Module.Entity.Aquatic;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;

/** 1.20 turtle breeding: one parent receives an egg instead of spawning a child immediately. */
public final class EntityAITurtleMate120 extends EntityAIBase {
    private final EntitySeaTurtle turtle;
    private final double speed;
    private EntitySeaTurtle mate;
    private int courtship;

    public EntityAITurtleMate120(EntitySeaTurtle turtle, double speed) {
        this.turtle = turtle;
        this.speed = speed;
        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (!turtle.isInLove() || turtle.hasEgg()) return false;
        mate = findMate();
        return mate != null;
    }

    @Override
    public boolean continueExecuting() {
        return mate != null && mate.isEntityAlive() && mate.isInLove() && courtship < 60;
    }

    @Override public void resetTask() { mate = null; courtship = 0; }

    @Override
    public void updateTask() {
        turtle.getLookHelper().setLookPositionWithEntity(mate, 10.0F, turtle.getVerticalFaceSpeed());
        turtle.getNavigator().tryMoveToEntityLiving(mate, speed);
        if (++courtship >= 60 && turtle.getDistanceSqToEntity(mate) < 9.0D
                && !turtle.hasEgg() && !mate.hasEgg()) {
            turtle.setHasEgg(true);
            turtle.resetInLove();
            mate.resetInLove();
        }
    }

    @SuppressWarnings("unchecked")
    private EntitySeaTurtle findMate() {
        List<EntitySeaTurtle> turtles = turtle.worldObj.getEntitiesWithinAABB(EntitySeaTurtle.class,
                AxisAlignedBB.getBoundingBox(turtle.posX - 8.0D, turtle.posY - 4.0D, turtle.posZ - 8.0D,
                        turtle.posX + 8.0D, turtle.posY + 4.0D, turtle.posZ + 8.0D));
        EntitySeaTurtle closest = null;
        double distance = Double.MAX_VALUE;
        for (EntitySeaTurtle candidate : turtles) {
            double current = turtle.getDistanceSqToEntity(candidate);
            if (turtle.canMateWith(candidate) && !candidate.hasEgg() && current < distance) {
                closest = candidate;
                distance = current;
            }
        }
        return closest;
    }
}
