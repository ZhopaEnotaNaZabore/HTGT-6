package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;
import com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage.RaidAndPatrolHandler;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.MathHelper;
import java.util.List;

/** ObtainRaidLeaderBannerGoal using legacy path navigation and authoritative promotion. */
public final class RaidBannerGoal153 extends EntityAIBase {
    private final EntityCreature mob;private EntityItem banner;
    public RaidBannerGoal153(EntityCreature mob){this.mob=mob;setMutexBits(1);}
    public static void attach(EntityCreature mob){
        if(!RaidCaptain153.eligible(mob))return;
        for(Object value:mob.tasks.taskEntries)if(((EntityAITasks.EntityAITaskEntry)value).action instanceof RaidBannerGoal153)return;
        mob.tasks.addTask(1,new RaidBannerGoal153(mob));
    }
    private boolean valid(EntityItem item){return item!=null&&!item.isDead&&item.delayBeforeCanPickup<=0&&RaidCaptain153.isBanner(item.getEntityItem());}
    @Override public boolean shouldExecute(){
        if(!mob.isEntityAlive()||mob.ridingEntity!=null||!RaidAndPatrolHandler.canPromote(mob))return false;
        int x=MathHelper.floor_double(mob.posX),z=MathHelper.floor_double(mob.posZ),radius=Math.max(17,(int)(mob.getNavigator().getPathSearchRange()+8F));
        if(!mob.worldObj.checkChunksExist(x-radius,0,z-radius,x+radius,mob.worldObj.getHeight()-1,z+radius))return false;
        List<EntityItem> items=mob.worldObj.getEntitiesWithinAABB(EntityItem.class,mob.boundingBox.expand(16,8,16));
        banner=null;for(EntityItem item:items)if(valid(item)){banner=item;break;}
        return banner!=null&&(mob.getDistanceSqToEntity(banner)<2D||mob.getNavigator().tryMoveToXYZ(banner.posX,banner.posY,banner.posZ,1.15D));
    }
    @Override public boolean continueExecuting(){return valid(banner)&&RaidAndPatrolHandler.canPromote(mob)&&!mob.getNavigator().noPath();}
    @Override public void startExecuting(){updateTask();}
    @Override public void updateTask(){if(valid(banner)&&mob.getDistanceSqToEntity(banner)<2D)RaidAndPatrolHandler.promote(mob,banner);}
    @Override public void resetTask(){banner=null;mob.getNavigator().clearPathEntity();}
}
