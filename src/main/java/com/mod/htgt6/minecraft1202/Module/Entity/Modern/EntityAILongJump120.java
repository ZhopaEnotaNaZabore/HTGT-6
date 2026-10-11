package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

/** Ballistic long-jump goal using only the synchronous 1.7 navigator and collision API. */
public final class EntityAILongJump120 extends EntityAIBase {
    public static final double HORIZONTAL_SPEED=0.55D;
    public static final double GRAVITY=0.08D;
    public static final double MAX_VERTICAL_SPEED=1.25D;
    private final EntityGoat120 goat;
    private int cooldown=80;
    private int airborneTicks;
    private double velocityX,velocityY,velocityZ;

    public EntityAILongJump120(EntityGoat120 goat){this.goat=goat;setMutexBits(5);}

    @Override public boolean shouldExecute(){
        if(cooldown>0){--cooldown;return false;}
        if(!goat.onGround||goat.isInWater()||goat.isRamming()||goat.getAttackTarget()!=null)return false;
        for(int attempt=0;attempt<12;attempt++){
            double angle=goat.getRNG().nextDouble()*Math.PI*2.0D;
            double distance=2.0D+goat.getRNG().nextDouble()*3.0D;
            int x=MathHelper.floor_double(goat.posX+Math.cos(angle)*distance);
            int z=MathHelper.floor_double(goat.posZ+Math.sin(angle)*distance);
            int y=MathHelper.floor_double(goat.posY)+goat.getRNG().nextInt(6)-1;
            if(!safeLanding(x,y,z))continue;
            PathEntity path=goat.getNavigator().getPathToXYZ(x+.5D,y,z+.5D);
            if(path==null)continue;
            double dx=x+.5D-goat.posX,dz=z+.5D-goat.posZ;
            double horizontal=Math.sqrt(dx*dx+dz*dz);
            double vertical=verticalVelocity(horizontal,y-goat.posY,HORIZONTAL_SPEED,GRAVITY);
            if(vertical<0.2D||vertical>MAX_VERTICAL_SPEED||!trajectoryClear(dx,y-goat.posY,dz,vertical))continue;
            velocityX=dx/horizontal*HORIZONTAL_SPEED;velocityY=vertical;velocityZ=dz/horizontal*HORIZONTAL_SPEED;
            return true;
        }
        cooldown=20+goat.getRNG().nextInt(40);return false;
    }

    private boolean safeLanding(int x,int y,int z){
        return goat.worldObj.blockExists(x,y,z)&&goat.worldObj.isAirBlock(x,y,z)
                &&goat.worldObj.isAirBlock(x,y+1,z)&&goat.worldObj.getBlock(x,y-1,z).getMaterial().isSolid();
    }

    private boolean trajectoryClear(double dx,double dy,double dz,double initialY){
        double horizontal=Math.sqrt(dx*dx+dz*dz),time=horizontal/HORIZONTAL_SPEED;
        int samples=Math.max(4,(int)Math.ceil(time*4.0D));
        for(int i=1;i<=samples;i++){
            double t=time*i/samples;
            double ox=dx*t/time,oz=dz*t/time,oy=sampleHeight(initialY,t,GRAVITY);
            AxisAlignedBB box=goat.boundingBox.getOffsetBoundingBox(ox,oy,oz);
            if(!goat.worldObj.getCollidingBoundingBoxes(goat,box).isEmpty())return false;
        }
        return true;
    }

    /** Required launch velocity for a displacement under constant Minecraft gravity. */
    public static double verticalVelocity(double horizontalDistance,double verticalDistance,
                                          double horizontalSpeed,double gravity){
        if(horizontalDistance<=0.0D||horizontalSpeed<=0.0D)return Double.NaN;
        double time=horizontalDistance/horizontalSpeed;
        return (verticalDistance+0.5D*gravity*time*time)/time;
    }

    public static double sampleHeight(double initialVelocity,double time,double gravity){
        return initialVelocity*time-0.5D*gravity*time*time;
    }

    @Override public void startExecuting(){
        goat.getNavigator().clearPathEntity();airborneTicks=0;
        goat.motionX=velocityX;goat.motionY=velocityY;goat.motionZ=velocityZ;
        goat.isAirBorne=true;goat.velocityChanged=true;
        goat.playSound("htgt6:entity.goat.long_jump",1.0F,1.0F);
    }

    @Override public boolean continueExecuting(){return airborneTicks++<40&&(!goat.onGround||airborneTicks<3);}
    @Override public void resetTask(){cooldown=100+goat.getRNG().nextInt(101);}
}
