package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/** Shared liquid-aware placement for the registered vanilla oak item and the five added boats. */
public final class BoatPlacement120 {
    public static ItemStack oak(ItemStack stack,World world,EntityPlayer player){return place(stack,world,player,0);}
    public static ItemStack place(ItemStack stack,World world,EntityPlayer player,int variant){
        if(stack==null||stack.stackSize<=0)return stack;
        double eye=player.getEyeHeight()-(world.isRemote?player.getDefaultEyeHeight():0);
        Vec3 start=Vec3.createVectorHelper(player.posX,player.posY+eye,player.posZ),look=player.getLook(1);
        MovingObjectPosition hit=world.rayTraceBlocks(start,start.addVector(look.xCoord*5,look.yCoord*5,look.zCoord*5),true);
        if(hit==null||hit.typeOfHit!=MovingObjectPosition.MovingObjectType.BLOCK)return stack;
        for(Object value:world.getEntitiesWithinAABBExcludingEntity(player,player.boundingBox.addCoord(look.xCoord*5,look.yCoord*5,look.zCoord*5).expand(1,1,1))){
            Entity entity=(Entity)value;float border=entity.getCollisionBorderSize();
            if(entity.canBeCollidedWith()&&entity.boundingBox.expand(border,border,border).isVecInside(start))return stack;
        }
        if(!player.canPlayerEdit(hit.blockX,hit.blockY,hit.blockZ,hit.sideHit,stack))return stack;
        EntityCombatBoat boat=new EntityCombatBoat(world,hit.hitVec.xCoord,hit.hitVec.yCoord,hit.hitVec.zCoord,variant);
        boat.rotationYaw=player.rotationYaw;
        if(!world.getCollidingBoundingBoxes(boat,boat.boundingBox.contract(.1,.1,.1)).isEmpty())return stack;
        if(!world.isRemote&&!world.spawnEntityInWorld(boat))return stack;
        if(!player.capabilities.isCreativeMode)--stack.stackSize;
        return stack;
    }
    private BoatPlacement120(){ }
}
