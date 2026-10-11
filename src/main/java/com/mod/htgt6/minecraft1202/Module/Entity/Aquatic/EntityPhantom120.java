package com.mod.htgt6.minecraft1202.Module.Entity.Aquatic;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Server-owned circle/swoop flight with normal solid-block collision. */
public final class EntityPhantom120 extends EntityMob {
    private boolean anchored, swooping;
    private double anchorX, anchorY, anchorZ, waypointX, waypointY, waypointZ;
    private double angle, radius = 8;
    private int direction = 1, waypointTicks, targetTicks, sweepTicks = 10;

    public EntityPhantom120(World world) { super(world); setSize(.9F,.5F); experienceValue=5; }
    @Override protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(64);
    }
    @Override protected boolean isAIEnabled() { return true; }
    @Override protected void fall(float distance) { }
    @Override public void onLivingUpdate() {
        if (!worldObj.isRemote && worldObj.isDaytime() && getBrightness(1F)>.5F
                && worldObj.canBlockSeeTheSky(MathHelper.floor_double(posX),
                        MathHelper.floor_double(posY+height),MathHelper.floor_double(posZ))) setFire(8);
        super.onLivingUpdate();
        fallDistance=0;
    }
    @Override protected void updateAITasks() {
        // Keep despawning, but do not run EntityCreature's ground navigation/chase.
        ++entityAge;
        despawnEntity();
        if (isDead) return;
        if (!anchored) {
            anchorX=posX; anchorY=posY+4; anchorZ=posZ;
            angle=rand.nextDouble()*Math.PI*2; radius=5+rand.nextDouble()*10;
            direction=rand.nextBoolean()?1:-1; anchored=true;
        }
        EntityLivingBase target=getAttackTarget();
        if(swooping&&ticksExisted%20==0&&!worldObj.getEntitiesWithinAABB(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCat114.class,boundingBox.expand(16,16,16)).isEmpty()){
            setAttackTarget(null);target=null;returnToCircle();targetTicks=100;
        }
        if (target!=null && (!validTarget(target)||!canEntityBeSeen(target))) {
            setAttackTarget(null); target=null; returnToCircle();
            anchorX=posX; anchorY=posY+8; anchorZ=posZ;
        }
        if (--targetTicks<=0) {
            targetTicks=60;
            if (target==null) {
                for (Object raw:worldObj.playerEntities) {
                    EntityPlayer candidate=(EntityPlayer)raw;
                    if (validTarget(candidate)&&canEntityBeSeen(candidate)
                            && (target==null||candidate.posY>target.posY)) target=candidate;
                }
                if (target!=null) {
                    setAttackTarget(target);
                    anchorX=target.posX; anchorY=target.posY+20+rand.nextInt(20); anchorZ=target.posZ;
                    sweepTicks=10; waypointTicks=0;
                }
            }
        }
        if (isCollidedHorizontally||isCollidedVertically||hurtTime>0) returnToCircle();
        if (!swooping&&target!=null&&--sweepTicks<=0) swooping=true;
        if (swooping&&target!=null) {
            waypointX=target.posX; waypointY=target.boundingBox.minY+target.height*.5; waypointZ=target.posZ;
            if (boundingBox.expand(.2,.2,.2).intersectsWith(target.boundingBox)&&canEntityBeSeen(target)) {
                attackEntityAsMob(target); returnToCircle();
            }
        } else if (--waypointTicks<=0||getDistanceSq(waypointX,waypointY,waypointZ)<4) chooseCirclePoint();
        steer();
    }
    private boolean validTarget(EntityLivingBase target) {
        return target.isEntityAlive()&&getDistanceSqToEntity(target)<=64*64
                && (!(target instanceof EntityPlayer)||!((EntityPlayer)target).capabilities.disableDamage);
    }
    private void returnToCircle() {
        swooping=false; sweepTicks=(8+rand.nextInt(4))*20; waypointTicks=0;
    }
    private void chooseCirclePoint() {
        angle+=direction*Math.PI/6;
        waypointX=anchorX+Math.cos(angle)*radius; waypointZ=anchorZ+Math.sin(angle)*radius;
        waypointY=MathHelper.clamp_double(anchorY-4+rand.nextDouble()*9,2,worldObj.getHeight()-2);
        waypointTicks=40;
    }
    private void steer() {
        double dx=waypointX-posX,dy=waypointY-posY,dz=waypointZ-posZ;
        double length=Math.sqrt(dx*dx+dy*dy+dz*dz);
        if (length<.001) { waypointTicks=0; return; }
        double speed=swooping?.65:.35;
        dx=dx/length*speed; dy=dy/length*speed; dz=dz/length*speed;
        if (!clearFlight(dx*3,dy*3,dz*3)) {
            returnToCircle();
            // Probe the swept body, including escape below a ceiling, not just an eye ray.
            boolean found=false;
            for (int vertical=1;vertical>=-1&&!found;--vertical) {
                for (int turn=0;turn<8;++turn) {
                    double heading=angle+turn*Math.PI/4;
                    double x=Math.cos(heading)*.3,y=vertical*.25,z=Math.sin(heading)*.3;
                    if (clearFlight(x*3,y*3,z*3)) {
                        dx=x; dy=y; dz=z; angle=heading; found=true; break;
                    }
                }
            }
            if (!found) { dx=0; dy=0; dz=0; }
        }
        motionX+=(dx-motionX)*.2; motionY+=(dy-motionY)*.2; motionZ+=(dz-motionZ)*.2;
        rotationYaw=(float)(Math.atan2(motionZ,motionX)*180/Math.PI)-90;
        renderYawOffset=rotationYaw;
        rotationPitch=(float)(-Math.atan2(motionY,Math.sqrt(motionX*motionX+motionZ*motionZ))*180/Math.PI);
    }
    private boolean clearFlight(double dx,double dy,double dz) {
        AxisAlignedBB swept=boundingBox.addCoord(dx,dy,dz);
        return swept.minY>=0&&swept.maxY<worldObj.getHeight()
                && worldObj.checkChunksExist(MathHelper.floor_double(swept.minX),MathHelper.floor_double(swept.minY),
                        MathHelper.floor_double(swept.minZ),MathHelper.floor_double(swept.maxX),
                        MathHelper.floor_double(swept.maxY),MathHelper.floor_double(swept.maxZ))
                && worldObj.getCollidingBoundingBoxes(this,swept).isEmpty();
    }
    @Override public void moveEntityWithHeading(float strafe,float forward) {
        moveEntity(motionX,motionY,motionZ);
        motionX*=.91; motionY*=.91; motionZ*=.91;
    }
    @Override public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        if (anchored) { tag.setDouble("AX",anchorX); tag.setDouble("AY",anchorY); tag.setDouble("AZ",anchorZ); }
    }
    @Override public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag); noClip=false;
        anchored=tag.hasKey("AX")&&tag.hasKey("AY")&&tag.hasKey("AZ");
        if (anchored) { anchorX=tag.getDouble("AX"); anchorY=tag.getDouble("AY"); anchorZ=tag.getDouble("AZ"); }
        returnToCircle();
    }
    @Override protected Item getDropItem() { return com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.phantomMembrane; }
    @Override protected void dropFewItems(boolean hit,int looting) {
        int count=rand.nextInt(2)+(looting>0?rand.nextInt(looting+1):0);
        if(count>0)dropItem(getDropItem(),count);
    }
    @Override protected String getLivingSound(){return "htgt6:entity.phantom.ambient";}
    @Override protected String getHurtSound(){return "htgt6:entity.phantom.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.phantom.death";}
}
