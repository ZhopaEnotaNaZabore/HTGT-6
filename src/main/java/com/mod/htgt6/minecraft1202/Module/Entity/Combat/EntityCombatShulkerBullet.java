package com.mod.htgt6.minecraft1202.Module.Entity.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.UUID;

/** Axis-guided ShulkerBullet adapted to the 1.7 entity, collision and NBT APIs. */
public final class EntityCombatShulkerBullet extends EntityThrowable {
    private EntityLivingBase target,owner;
    private UUID targetId,ownerId;
    private int direction=-1,steps;
    private double tx,ty,tz;
    private boolean leftOwner;
    public EntityCombatShulkerBullet(World world){super(world);setSize(.3125F,.3125F);}
    public EntityCombatShulkerBullet(World world,EntityLivingBase owner,EntityLivingBase target){
        this(world);this.owner=owner;this.target=target;
        ownerId=owner==null?null:owner.getUniqueID();targetId=target==null?null:target.getUniqueID();
        if(owner!=null)setPosition(Math.floor(owner.posX)+.5,Math.floor(owner.posY)+.5,Math.floor(owner.posZ)+.5);
        selectDirection(owner instanceof EntityCombatShulker?axis(((EntityCombatShulker)owner).getAttachFace()):1);
    }
    private EntityLivingBase resolve(UUID id){
        if(id==null||worldObj.loadedEntityList==null)return null;
        for(Object value:worldObj.loadedEntityList)if(value instanceof EntityLivingBase&&id.equals(((Entity)value).getUniqueID()))return (EntityLivingBase)value;
        return null;
    }
    @Override public EntityLivingBase getThrower(){if(owner==null)owner=resolve(ownerId);return owner;}
    private static int axis(int direction){return direction<2?1:direction<4?2:0;}
    private boolean free(int x,int y,int z,int d){return worldObj.isAirBlock(x+Facing.offsetsXForSide[d],y+Facing.offsetsYForSide[d],z+Facing.offsetsZForSide[d]);}
    private void selectDirection(int excludedAxis){
        double height=target==null?.5:target.height*.5;
        int gx=target==null?MathHelper.floor_double(posX):MathHelper.floor_double(target.posX);
        int gy=target==null?MathHelper.floor_double(posY)-1:MathHelper.floor_double(target.posY+height);
        int gz=target==null?MathHelper.floor_double(posZ):MathHelper.floor_double(target.posZ);
        double dx=gx+.5-posX,dy=gy+height-posY,dz=gz+.5-posZ;
        direction=-1;
        if(dx*dx+(gy+.5-posY)*(gy+.5-posY)+dz*dz>=4){
            int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(posY),z=MathHelper.floor_double(posZ);
            ArrayList<Integer> choices=new ArrayList<Integer>();
            int[] desired={gx>x?5:gx<x?4:-1,gy>y?1:gy<y?0:-1,gz>z?3:gz<z?2:-1};
            for(int d:desired)if(d>=0&&axis(d)!=excludedAxis&&free(x,y,z,d))choices.add(d);
            if(choices.isEmpty()){direction=rand.nextInt(6);for(int attempts=5;attempts>0&&!free(x,y,z,direction);attempts--)direction=rand.nextInt(6);}
            else direction=choices.get(rand.nextInt(choices.size()));
            dx=Facing.offsetsXForSide[direction];dy=Facing.offsetsYForSide[direction];dz=Facing.offsetsZForSide[direction];
        }
        double length=Math.sqrt(dx*dx+dy*dy+dz*dz);
        tx=length==0?0:dx/length*.15;ty=length==0?0:dy/length*.15;tz=length==0?0:dz/length*.15;
        steps=10+rand.nextInt(5)*10;isAirBorne=true;
    }
    public static double acceleratedTarget(double value){return Math.max(-1,Math.min(1,value*1.025));}
    public static double steer(double velocity,double desired){return velocity+(desired-velocity)*.2;}
    @Override public void onUpdate(){
        // EntityThrowable's update adds unrelated arrow drag, ground expiry and gravity.
        super.onEntityUpdate();
        tickFlight();
    }
    public void tickFlight(){
        if(!worldObj.isRemote){
            if(worldObj.difficultySetting==net.minecraft.world.EnumDifficulty.PEACEFUL){setDead();return;}
            if(target==null)target=resolve(targetId);
            if(target!=null&&target.isEntityAlive()){
                tx=acceleratedTarget(tx);ty=acceleratedTarget(ty);tz=acceleratedTarget(tz);
                motionX=steer(motionX,tx);motionY=steer(motionY,ty);motionZ=steer(motionZ,tz);
            }else motionY-=.04;
            collide();if(isDead)return;
        }
        setPosition(posX+motionX,posY+motionY,posZ+motionZ);
        double horizontal=Math.sqrt(motionX*motionX+motionZ*motionZ);
        rotationYaw+=MathHelper.wrapAngleTo180_float((float)(Math.atan2(motionX,motionZ)*180/Math.PI)-rotationYaw)*.5F;
        rotationPitch+=MathHelper.wrapAngleTo180_float((float)(Math.atan2(motionY,horizontal)*180/Math.PI)-rotationPitch)*.5F;
        if(worldObj.isRemote){worldObj.spawnParticle("townaura",posX-motionX,posY-motionY+.15,posZ-motionZ,0,0,0);return;}
        if(target!=null&&!target.isDead){
            if(steps>0&&--steps==0)selectDirection(direction<0?-1:axis(direction));
            if(direction>=0){
                int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(posY),z=MathHelper.floor_double(posZ),a=axis(direction);
                if(!free(x,y,z,direction)||(a==0&&x==MathHelper.floor_double(target.posX))||(a==1&&y==MathHelper.floor_double(target.posY))||(a==2&&z==MathHelper.floor_double(target.posZ)))selectDirection(a);
            }
        }
    }
    private void collide(){
        Vec3 start=Vec3.createVectorHelper(posX,posY,posZ),end=Vec3.createVectorHelper(posX+motionX,posY+motionY,posZ+motionZ);
        MovingObjectPosition hit=worldObj.rayTraceBlocks(start,end);
        if(hit!=null)end=hit.hitVec;
        EntityLivingBase shooter=getThrower();if(shooter==null||!boundingBox.intersectsWith(shooter.boundingBox.expand(.3,.3,.3)))leftOwner=true;
        double distance=start.squareDistanceTo(end);Entity entityHit=null;
        for(Object value:worldObj.getEntitiesWithinAABBExcludingEntity(this,boundingBox.addCoord(motionX,motionY,motionZ).expand(1,1,1))){
            Entity e=(Entity)value;if(!e.canBeCollidedWith()||e.noClip||(!leftOwner&&e==shooter))continue;
            MovingObjectPosition candidate=e.boundingBox.expand(.3,.3,.3).calculateIntercept(start,end);
            if(candidate!=null&&start.squareDistanceTo(candidate.hitVec)<=distance){distance=start.squareDistanceTo(candidate.hitVec);entityHit=e;}
        }
        if(entityHit!=null)hit=new MovingObjectPosition(entityHit);
        if(hit!=null)onImpact(hit);
    }
    @Override protected float getGravityVelocity(){return 0;}
    @Override protected void onImpact(MovingObjectPosition hit){
        if(worldObj.isRemote)return;
        if(hit.entityHit!=null&&hit.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this,getThrower()),4)){
            if(hit.entityHit instanceof EntityLivingBase)((EntityLivingBase)hit.entityHit).addPotionEffect(new PotionEffect(CombatContent.levitation.id,200,0));
        }
        if(hit.entityHit==null)playSound("htgt6:entity.shulker_bullet.hit",1,1);
        setDead();
    }
    @Override public boolean canBeCollidedWith(){return true;}
    @Override public boolean attackEntityFrom(DamageSource source,float amount){if(!worldObj.isRemote){playSound("htgt6:entity.shulker_bullet.hurt",1,1);setDead();}return true;}
    @Override public void writeEntityToNBT(NBTTagCompound tag){
        super.writeEntityToNBT(tag);tag.setInteger("Dir",direction);tag.setInteger("Steps",steps);
        tag.setDouble("TXD",tx);tag.setDouble("TYD",ty);tag.setDouble("TZD",tz);
        if(targetId!=null){tag.setLong("TargetMost",targetId.getMostSignificantBits());tag.setLong("TargetLeast",targetId.getLeastSignificantBits());}
        if(ownerId!=null){tag.setLong("OwnerMost",ownerId.getMostSignificantBits());tag.setLong("OwnerLeast",ownerId.getLeastSignificantBits());}
    }
    @Override public void readEntityFromNBT(NBTTagCompound tag){
        super.readEntityFromNBT(tag);direction=tag.hasKey("Dir",99)?tag.getInteger("Dir"):-1;if(direction<0||direction>5)direction=-1;
        steps=Math.max(0,tag.getInteger("Steps"));tx=tag.getDouble("TXD");ty=tag.getDouble("TYD");tz=tag.getDouble("TZD");
        targetId=tag.hasKey("TargetMost",99)?new UUID(tag.getLong("TargetMost"),tag.getLong("TargetLeast")):null;
        ownerId=tag.hasKey("OwnerMost",99)?new UUID(tag.getLong("OwnerMost"),tag.getLong("OwnerLeast")):null;target=owner=null;
    }
}
