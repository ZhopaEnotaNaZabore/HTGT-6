package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import java.util.List;

/** 1.7-compatible WANT/GET item behavior for an Allay's held-item filter. */
public final class EntityAIAllayCollect120 extends EntityAIBase {
    public static final double SEARCH_RANGE=32.0D;
    private final EntityAllay120 allay;
    private EntityItem target;
    public EntityAIAllayCollect120(EntityAllay120 allay){this.allay=allay;setMutexBits(1);}
    @Override public boolean shouldExecute(){
        ItemStack filter=allay.getCarriedStack();if(filter==null||filter.stackSize>=filter.getMaxStackSize()
                ||!allay.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))return false;
        List<?> items=allay.worldObj.getEntitiesWithinAABB(EntityItem.class,
                allay.boundingBox.expand(SEARCH_RANGE,SEARCH_RANGE,SEARCH_RANGE));double best=Double.MAX_VALUE;
        for(Object raw:items){EntityItem item=(EntityItem)raw;ItemStack candidate=item.getEntityItem();
            if(item.isDead||!EntityAllay120.matchesFilter(filter,candidate))continue;
            double distance=allay.getDistanceSqToEntity(item);if(distance<best){best=distance;target=item;}}
        return target!=null;
    }
    @Override public boolean continueExecuting(){return target!=null&&!target.isDead
            &&EntityAllay120.matchesFilter(allay.getCarriedStack(),target.getEntityItem());}
    @Override public void updateTask(){
        allay.flyToward(target.posX,target.posY+.2D,target.posZ,0.07D);
        if(allay.getDistanceSqToEntity(target)>2.0D)return;
        ItemStack held=allay.getCarriedStack(),found=target.getEntityItem();
        int accepted=Math.min(found.stackSize,held.getMaxStackSize()-held.stackSize);if(accepted<=0)return;
        held.stackSize+=accepted;found.stackSize-=accepted;allay.setCarriedStack(held);
        if(found.stackSize<=0)target.setDead();else target.setEntityItemStack(found);
    }
    @Override public void resetTask(){target=null;}
}
