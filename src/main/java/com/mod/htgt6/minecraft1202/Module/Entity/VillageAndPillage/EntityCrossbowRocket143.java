package com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/** Shot-at-angle fireworks: legacy particle transport, modern projectile and explosion rules. */
public final class EntityCrossbowRocket143 extends EntityFireworkRocket implements IProjectile, IEntityAdditionalSpawnData {
    private int life, lifetime;
    private UUID ownerId;
    private EntityLivingBase owner;
    private boolean leftOwner, exploded;

    public EntityCrossbowRocket143(World world) {
        super(world);
        dataWatcher.updateObject(8,new ItemStack(Items.fireworks));
    }

    public EntityCrossbowRocket143(World world,EntityLivingBase shooter,ItemStack ammunition) {
        this(world);
        if(shooter!=null){owner=shooter;ownerId=shooter.getUniqueID();setPosition(shooter.posX,shooter.posY+shooter.getEyeHeight()-.15D,shooter.posZ);}
        setFireworkItem(ammunition);
        int flight=getFireworkItem().getTagCompound()==null?0:getFireworkItem().getTagCompound().getCompoundTag("Fireworks").getByte("Flight");
        lifetime=Math.max(0,10*(1+flight)+rand.nextInt(6)+rand.nextInt(7));
    }

    private void setFireworkItem(ItemStack stack) {
        ItemStack copy=stack==null||stack.getItem()!=Items.fireworks?new ItemStack(Items.fireworks):stack.copy();
        copy.stackSize=1;dataWatcher.updateObject(8,copy);
    }

    public ItemStack getFireworkItem(){ItemStack stack=dataWatcher.getWatchableObjectItemStack(8);return stack==null?new ItemStack(Items.fireworks):stack.copy();}
    public int getLife(){return life;}
    public int getLifetime(){return lifetime;}
    public EntityLivingBase getOwner(){
        if(owner!=null && owner.worldObj==worldObj && owner.getUniqueID().equals(ownerId))return owner;
        if(ownerId!=null)for(Object raw:worldObj.loadedEntityList)if(raw instanceof EntityLivingBase && ((Entity)raw).getUniqueID().equals(ownerId))return owner=(EntityLivingBase)raw;
        return null;
    }

    @Override public void setThrowableHeading(double x,double y,double z,float velocity,float inaccuracy){
        double length=Math.sqrt(x*x+y*y+z*z);
        if(length<1.0E-12D || !Double.isFinite(length))return;
        // Projectile.shoot uses a triangular distribution in the 1.20.2 reference.
        double spread=.0172275D*inaccuracy;
        motionX=(x/length+(rand.nextDouble()-rand.nextDouble())*spread)*velocity;
        motionY=(y/length+(rand.nextDouble()-rand.nextDouble())*spread)*velocity;
        motionZ=(z/length+(rand.nextDouble()-rand.nextDouble())*spread)*velocity;
        prevRotationYaw=rotationYaw=(float)(Math.atan2(motionX,motionZ)*180D/Math.PI);
        prevRotationPitch=rotationPitch=(float)(Math.atan2(motionY,Math.sqrt(motionX*motionX+motionZ*motionZ))*180D/Math.PI);
    }

    @Override public void onUpdate(){
        if(isDead)return;
        lastTickPosX=posX;lastTickPosY=posY;lastTickPosZ=posZ;
        // Deliberately skip EntityFireworkRocket.onUpdate: it accelerates every legacy rocket upward.
        super.onEntityUpdate();
        updateLeftOwner();
        double vx=motionX,vy=motionY,vz=motionZ;
        moveEntity(vx,vy,vz);
        motionX=vx;motionY=vy;motionZ=vz;
        MovingObjectPosition collision=findHit();
        if(collision!=null&&!worldObj.isRemote){
            com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.TargetImpact116.onImpact(this,collision);
            if(collision.entityHit!=null)explode();
            else {
                worldObj.getBlock(collision.blockX,collision.blockY,collision.blockZ).onEntityCollidedWithBlock(worldObj,collision.blockX,collision.blockY,collision.blockZ,this);
                if(explosionCount()>0)explode();
            }
        }
        updateRotation();
        if(life==0)worldObj.playSoundAtEntity(this,"fireworks.launch",3F,1F);
        ++life;
        if(worldObj.isRemote)worldObj.spawnParticle("fireworksSpark",posX,posY-.3D,posZ,rand.nextGaussian()*.05D,-motionY*.5D,rand.nextGaussian()*.05D);
        else if(life>lifetime)explode();
    }

    private void updateRotation(){
        float yaw=(float)(Math.atan2(motionX,motionZ)*180D/Math.PI);
        float pitch=(float)(Math.atan2(motionY,Math.sqrt(motionX*motionX+motionZ*motionZ))*180D/Math.PI);
        rotationYaw=prevRotationYaw+MathHelper.wrapAngleTo180_float(yaw-prevRotationYaw)*.2F;
        rotationPitch=prevRotationPitch+MathHelper.wrapAngleTo180_float(pitch-prevRotationPitch)*.2F;
    }

    private static Entity vehicleRoot(Entity e){while(e.ridingEntity!=null)e=e.ridingEntity;return e;}
    private void updateLeftOwner(){
        if(leftOwner)return;
        EntityLivingBase shooter=getOwner();
        if(shooter==null){leftOwner=true;return;}
        AxisAlignedBB sweep=boundingBox.addCoord(motionX,motionY,motionZ).expand(1,1,1);
        Entity root=vehicleRoot(shooter);
        for(Object raw:worldObj.getEntitiesWithinAABBExcludingEntity(this,sweep)){
            Entity candidate=(Entity)raw;
            if(candidate.canBeCollidedWith()&&vehicleRoot(candidate)==root)return;
        }
        leftOwner=true;
    }

    private MovingObjectPosition findHit(){
        Vec3 start=Vec3.createVectorHelper(posX,posY,posZ),end=Vec3.createVectorHelper(posX+motionX,posY+motionY,posZ+motionZ);
        MovingObjectPosition result=worldObj.func_147447_a(start,end,false,true,false);
        if(result!=null)end=result.hitVec;
        double nearest=start.squareDistanceTo(end);
        EntityLivingBase shooter=getOwner();
        for(Object raw:worldObj.getEntitiesWithinAABBExcludingEntity(this,boundingBox.addCoord(motionX,motionY,motionZ).expand(1,1,1))){
            Entity candidate=(Entity)raw;
            if(candidate.isDead||!candidate.canBeCollidedWith()||(!leftOwner&&shooter!=null&&vehicleRoot(candidate)==vehicleRoot(shooter)))continue;
            AxisAlignedBB box=candidate.boundingBox.expand(.3D,.3D,.3D);
            MovingObjectPosition hit=box.calculateIntercept(start,end);
            if(box.isVecInside(start)){result=new MovingObjectPosition(candidate);nearest=0;}
            else if(hit!=null&&start.squareDistanceTo(hit.hitVec)<nearest){result=new MovingObjectPosition(candidate);nearest=start.squareDistanceTo(hit.hitVec);}
        }
        return result;
    }

    private int explosionCount(){
        ItemStack item=dataWatcher.getWatchableObjectItemStack(8);
        return item==null||!item.hasTagCompound()?0:item.getTagCompound().getCompoundTag("Fireworks").getTagList("Explosions",10).tagCount();
    }

    private void explode(){
        if(worldObj.isRemote||isDead||exploded)return;
        exploded=true;
        worldObj.setEntityState(this,(byte)17);
        int stars=explosionCount();
        if(stars>0){
            float base=5F+2F*stars;
            Vec3 source=Vec3.createVectorHelper(posX,posY,posZ);
            DamageSource damage=new FireworkDamageSource(this,getOwner());
            for(Object raw:worldObj.getEntitiesWithinAABB(EntityLivingBase.class,boundingBox.expand(5,5,5))){
                EntityLivingBase target=(EntityLivingBase)raw;
                if(!target.isEntityAlive()||getDistanceSqToEntity(target)>25D)continue;
                boolean visible=false;
                for(int sample=0;sample<2;sample++){
                    Vec3 end=Vec3.createVectorHelper(target.posX,target.posY+target.height*.5D*sample,target.posZ);
                    if(worldObj.func_147447_a(source,end,false,true,false)==null){visible=true;break;}
                }
                if(visible){float amount=base*(float)Math.sqrt(Math.max(0D,(5D-getDistanceToEntity(target))/5D));
                    target.attackEntityFrom(damage,amount);
                }
            }
        }
        setDead();
    }

    private static final class FireworkDamageSource extends EntityDamageSourceIndirect {
        FireworkDamageSource(Entity rocket,EntityLivingBase shooter){super("fireworks",rocket,shooter);setExplosion();}
        @Override public float getHungerDamage(){return .1F;}
        @Override public boolean isDifficultyScaled(){return getEntity() instanceof EntityLivingBase && !(getEntity() instanceof net.minecraft.entity.player.EntityPlayer);}
    }

    @Override public void writeEntityToNBT(NBTTagCompound tag){
        super.writeEntityToNBT(tag);tag.setInteger("Life",life);tag.setInteger("LifeTime",lifetime);
        tag.setBoolean("ShotAtAngle",true);tag.setBoolean("LeftOwner",leftOwner);
        if(ownerId!=null)tag.setString("OwnerUUID",ownerId.toString());else tag.removeTag("OwnerUUID");
    }
    @Override public void readEntityFromNBT(NBTTagCompound tag){
        super.readEntityFromNBT(tag);life=Math.max(0,tag.getInteger("Life"));lifetime=Math.max(0,tag.getInteger("LifeTime"));
        owner=null;ownerId=null;leftOwner=tag.getBoolean("LeftOwner");exploded=false;
        if(tag.hasKey("OwnerUUID",8))try{ownerId=UUID.fromString(tag.getString("OwnerUUID"));}catch(IllegalArgumentException malformed){}
        setFireworkItem(dataWatcher.getWatchableObjectItemStack(8));
    }
    @Override public void writeSpawnData(ByteBuf data){
        data.writeInt(life);data.writeInt(lifetime);data.writeBoolean(ownerId!=null);
        if(ownerId!=null){data.writeLong(ownerId.getMostSignificantBits());data.writeLong(ownerId.getLeastSignificantBits());}
        ByteBufUtils.writeItemStack(data,getFireworkItem());
    }
    @Override public void readSpawnData(ByteBuf data){
        life=Math.max(0,data.readInt());lifetime=Math.max(0,data.readInt());owner=null;
        ownerId=data.readBoolean()?new UUID(data.readLong(),data.readLong()):null;
        setFireworkItem(ByteBufUtils.readItemStack(data));
    }
}
