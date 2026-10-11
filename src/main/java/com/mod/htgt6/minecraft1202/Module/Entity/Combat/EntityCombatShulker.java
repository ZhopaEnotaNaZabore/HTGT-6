package com.mod.htgt6.minecraft1202.Module.Entity.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.world.World;

/** Stationary shell mob from Minecraft 1.9 with opening and homing-projectile behavior. */
public final class EntityCombatShulker extends EntityMob {
    private static final int PEEK_WATCHER = 21;
    private static final int FACE_WATCHER=22,COLOR_WATCHER=23;
    private int shotCooldown,idlePeekTicks;
    private float previousPeekAmount;
    private float currentPeekAmount;

    public EntityCombatShulker(World world) {
        super(world);
        setSize(1.0F, 1.0F);
        experienceValue = 5;
        isImmuneToFire = true;
    }

    @Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(PEEK_WATCHER, Byte.valueOf((byte) 0));dataWatcher.addObject(FACE_WATCHER,Byte.valueOf((byte)0));dataWatcher.addObject(COLOR_WATCHER,Byte.valueOf((byte)16)); }
    @Override protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16.0D);
    }
    @Override protected boolean isAIEnabled() { return false; }
    @Override public float getEyeHeight(){return .5F;}
    @Override protected boolean canDespawn() { return false; }
    @Override public boolean canBePushed() { return false; }
    @Override protected void collideWithEntity(net.minecraft.entity.Entity entity) { }
    @Override public void moveEntity(double x, double y, double z) { /* Attached to a block; gravity must not detach wall/ceiling shulkers. */ }
    public float getPeekAmount() { return (dataWatcher.getWatchableObjectByte(PEEK_WATCHER) & 255) / 100.0F; }

    /** Render interpolation is separate from the authoritative shell/arrow protection state. */
    public float getClientPeekAmount(float partialTicks) {
        return previousPeekAmount + (currentPeekAmount - previousPeekAmount) * partialTicks;
    }

    private void updatePeekAmount() {
        previousPeekAmount = currentPeekAmount;
        float target = getPeekAmount();
        if (currentPeekAmount > target) currentPeekAmount = Math.max(target, currentPeekAmount - 0.05F);
        else if (currentPeekAmount < target) currentPeekAmount = Math.min(target, currentPeekAmount + 0.05F);
        net.minecraft.util.AxisAlignedBB old=boundingBox.copy();
        net.minecraft.util.AxisAlignedBB next=shellBox(posX,posY,posZ,getAttachFace(),currentPeekAmount);boundingBox.setBB(next);
        if(currentPeekAmount>previousPeekAmount&&!worldObj.isRemote){
            int d=net.minecraft.util.Facing.oppositeSide[getAttachFace()];
            double distance=(Math.sin((.5+previousPeekAmount)*Math.PI)-Math.sin((.5+currentPeekAmount)*Math.PI))*.5+.01;
            for(Object value:worldObj.getEntitiesWithinAABBExcludingEntity(this,openingSlab(old,next,d))){
                net.minecraft.entity.Entity e=(net.minecraft.entity.Entity)value;if(e.noClip||e instanceof EntityCombatShulker)continue;
                e.moveEntity(net.minecraft.util.Facing.offsetsXForSide[d]*distance,net.minecraft.util.Facing.offsetsYForSide[d]*distance,net.minecraft.util.Facing.offsetsZForSide[d]*distance);
            }
        }
    }

    public static net.minecraft.util.AxisAlignedBB openingSlab(net.minecraft.util.AxisAlignedBB old,
            net.minecraft.util.AxisAlignedBB next, int direction) {
        net.minecraft.util.AxisAlignedBB slab = next.copy();
        switch (direction) {
            case 0: slab.maxY = old.minY; break;
            case 1: slab.minY = old.maxY; break;
            case 2: slab.maxZ = old.minZ; break;
            case 3: slab.minZ = old.maxZ; break;
            case 4: slab.maxX = old.minX; break;
            case 5: slab.minX = old.maxX; break;
        }
        return slab;
    }

    @Override public void onLivingUpdate() {
        super.onLivingUpdate();
        motionX = motionY = motionZ = 0.0D;
        if(!worldObj.isRemote){int x=(int)Math.floor(posX),y=(int)Math.floor(posY),z=(int)Math.floor(posZ);
            if(!canAttach(x,y,z,getAttachFace())){int face=findFace(x,y,z);if(face>=0)setAttachFace(face);else tryTeleport();}}
        // Run on both sides: the watcher carries a target, not per-frame animation packets.
        if (worldObj.isRemote) { updatePeekAmount(); return; }
        EntityLivingBase target = getAttackTarget();
        if(target==null||!target.isEntityAlive()||getDistanceSqToEntity(target)>=400||target instanceof EntityPlayer&&((EntityPlayer)target).capabilities.disableDamage){
            target=findTarget();setAttackTarget(target);
        }
        boolean active = target != null && worldObj.difficultySetting!=net.minecraft.world.EnumDifficulty.PEACEFUL;
        if(active&&getPeekAmount()<1)shotCooldown=20;
        if(active){idlePeekTicks=0;setPeek(100);getLookHelper().setLookPositionWithEntity(target,180,180);getLookHelper().onUpdateLook();}
        else {
            if(idlePeekTicks>0)--idlePeekTicks;
            else if(rand.nextInt(40)==0)idlePeekTicks=20*(1+rand.nextInt(3));
            setPeek(idlePeekTicks>0?30:0);
        }
        if (active && --shotCooldown <= 0) {
            EntityCombatShulkerBullet bullet = new EntityCombatShulkerBullet(worldObj, this, target);
            worldObj.spawnEntityInWorld(bullet);
            worldObj.playSoundAtEntity(this, "htgt6:entity.shulker.shoot", 2.0F, (rand.nextFloat()-rand.nextFloat())*.2F+1);
            shotCooldown = 20 + rand.nextInt(10)*10;
        }
        updatePeekAmount();
    }

    private void setPeek(int amount){
        int old=dataWatcher.getWatchableObjectByte(PEEK_WATCHER)&255;
        if(old==amount)return;
        dataWatcher.updateObject(PEEK_WATCHER,Byte.valueOf((byte)amount));
        if(!worldObj.isRemote&&(old==0||amount==0))playSound(amount==0?"htgt6:entity.shulker.close":"htgt6:entity.shulker.open",1,1);
    }
    private EntityLivingBase findTarget(){
        if(worldObj.difficultySetting==net.minecraft.world.EnumDifficulty.PEACEFUL)return null;
        EntityLivingBase revenge=getAITarget();
        if(revenge!=null&&revenge.isEntityAlive()&&!isOnSameTeam(revenge)&&getDistanceSqToEntity(revenge)<400)return revenge;
        double x=16,y=16,z=16;int face=getAttachFace();if(face<2)y=4;else if(face<4)z=4;else x=4;
        EntityLivingBase closest=null;double distance=256;
        for(Object value:worldObj.getEntitiesWithinAABB(EntityLivingBase.class,boundingBox.expand(x,y,z))){
            EntityLivingBase candidate=(EntityLivingBase)value;
            boolean player=candidate instanceof EntityPlayer&&!((EntityPlayer)candidate).capabilities.disableDamage;
            boolean hostile=getTeam()!=null&&candidate instanceof net.minecraft.entity.monster.IMob&&!(candidate instanceof EntityCombatShulker);
            double d=getDistanceSqToEntity(candidate);
            if(candidate!=this&&(player||hostile)&&candidate.isEntityAlive()&&!isOnSameTeam(candidate)&&d<distance&&canEntityBeSeen(candidate)){distance=d;closest=candidate;}
        }
        return closest;
    }
    @Override protected String getLivingSound(){return getPeekAmount()>0?"htgt6:entity.shulker.ambient":null;}
    @Override protected String getHurtSound(){return getPeekAmount()==0?"htgt6:entity.shulker.hurt_closed":"htgt6:entity.shulker.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.shulker.death";}
    @Override public boolean attackEntityFrom(net.minecraft.util.DamageSource source, float amount) {
        if (getPeekAmount() == 0 && source.getSourceOfDamage() instanceof EntityArrow) return false;

        boolean hit=super.attackEntityFrom(source,amount);
        if(hit&&getHealth()<getMaxHealth()*0.5F&&rand.nextInt(4)==0)tryTeleport();
        else if(hit&&source.getSourceOfDamage() instanceof EntityCombatShulkerBullet&&getPeekAmount()>0){
            double x=posX,y=posY,z=posZ;net.minecraft.util.AxisAlignedBB old=boundingBox.copy();
            if(tryTeleport()){
                int count=worldObj.getEntitiesWithinAABB(EntityCombatShulker.class,old.expand(8,8,8)).size();
                if(rand.nextFloat()>=(count-1)/5F){EntityCombatShulker child=new EntityCombatShulker(worldObj);child.setColor(getColor());child.setPosition(x,y,z);worldObj.spawnEntityInWorld(child);}
            }
        }
        return hit;
    }
    @Override public int getTotalArmorValue(){return super.getTotalArmorValue()+(getPeekAmount()==0?20:0);}
    public int getAttachFace(){return dataWatcher.getWatchableObjectByte(FACE_WATCHER)&255;}
    public void setAttachFace(int face){dataWatcher.updateObject(FACE_WATCHER,Byte.valueOf((byte)(face>=0&&face<6?face:0)));}
    public int getColor(){return dataWatcher.getWatchableObjectByte(COLOR_WATCHER)&255;}
    public void setColor(int color){dataWatcher.updateObject(COLOR_WATCHER,Byte.valueOf((byte)(color>=0&&color<16?color:16)));}
    public static net.minecraft.util.AxisAlignedBB shellBox(double x,double y,double z,int face,float peek){
        int opening=net.minecraft.util.Facing.oppositeSide[face>=0&&face<6?face:0];
        double extension=.5-Math.sin((.5+peek)*Math.PI)*.5;
        return net.minecraft.util.AxisAlignedBB.getBoundingBox(x-.5,y,z-.5,x+.5,y+1,z+.5).addCoord(
            net.minecraft.util.Facing.offsetsXForSide[opening]*extension,net.minecraft.util.Facing.offsetsYForSide[opening]*extension,net.minecraft.util.Facing.offsetsZForSide[opening]*extension);
    }
    private boolean canAttach(int x,int y,int z,int face){
        if(!worldObj.blockExists(x,y,z)||!worldObj.isAirBlock(x,y,z))return false;
        int nx=x+net.minecraft.util.Facing.offsetsXForSide[face],ny=y+net.minecraft.util.Facing.offsetsYForSide[face],nz=z+net.minecraft.util.Facing.offsetsZForSide[face];
        if(!worldObj.isSideSolid(nx,ny,nz,net.minecraftforge.common.util.ForgeDirection.getOrientation(net.minecraft.util.Facing.oppositeSide[face])))return false;
        return worldObj.getCollidingBoundingBoxes(this,shellBox(x+.5,y,z+.5,face,1).contract(.000001,.000001,.000001)).isEmpty();
    }
    private int findFace(int x,int y,int z){for(int d=0;d<6;d++)if(canAttach(x,y,z,d))return d;return -1;}
    private boolean tryTeleport(){
        if(isDead||worldObj.isRemote)return false;
        for(int i=0;i<5;i++){int x=(int)Math.floor(posX)+rand.nextInt(17)-8,y=(int)Math.floor(posY)+rand.nextInt(17)-8,z=(int)Math.floor(posZ)+rand.nextInt(17)-8;
            if(y<=0||y>=worldObj.getHeight())continue;int face=findFace(x,y,z);if(face<0)continue;
            playSound("htgt6:entity.shulker.teleport",1,1);setAttachFace(face);setPosition(x+.5,y,z+.5);setPeek(0);setAttackTarget(null);return true;
        }return false;
    }
    @Override public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setByte("AttachFace",(byte)getAttachFace());tag.setByte("Peek",dataWatcher.getWatchableObjectByte(PEEK_WATCHER));tag.setByte("Color",(byte)getColor());}
    @Override public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound tag){super.readEntityFromNBT(tag);setAttachFace(tag.getByte("AttachFace"));setColor(tag.hasKey("Color",99)?tag.getByte("Color"):16);dataWatcher.updateObject(PEEK_WATCHER,Byte.valueOf((byte)Math.max(0,Math.min(100,tag.getByte("Peek")))));}
    @Override protected Item getDropItem() { return null; }
    @Override protected void dropFewItems(boolean hit, int looting) {
        if (rand.nextFloat() < Math.min(1.0F, 0.5F + Math.max(0, looting) * 0.0625F)) dropItem(
                com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112.shulkerShell, 1);
    }
}
