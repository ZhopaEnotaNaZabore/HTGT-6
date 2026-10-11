package com.mod.htgt6.minecraft1202.Module.Entity.Combat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;

public class EntityCombatArrow extends EntityArrow implements cpw.mods.fml.common.registry.IEntityAdditionalSpawnData {
    public static final int TYPE_TIPPED = 1;
    public static final int TYPE_SPECTRAL = 2;
    public static final int TYPE_NORMAL = 0;
    private int groundTime;
    private int potionDamage;
    private boolean applied;
    private final java.util.Set<java.util.UUID> piercedTargets = new java.util.HashSet<java.util.UUID>();
    private boolean repeatCollision;
    private NBTTagCompound potionTag = new NBTTagCompound();
    private static final java.lang.reflect.Field IN_GROUND = cpw.mods.fml.relauncher.ReflectionHelper.findField(
            EntityArrow.class, "inGround", "field_70254_i");
    public void setPotionTag(NBTTagCompound tag) {
        potionTag = tag == null ? new NBTTagCompound() : (NBTTagCompound) tag.copy();
        updateColor();
    }
    @Override protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(20, Integer.valueOf(TYPE_NORMAL));
        dataWatcher.addObject(21, Integer.valueOf(-1));
        dataWatcher.addObject(22, Byte.valueOf((byte)0));
    }
    private void updateColor() {
        int color = -1;
        if(getCombatType() == TYPE_TIPPED) {
            if(potionTag.hasKey("CustomPotionColor",99)) color=potionTag.getInteger("CustomPotionColor");
            else if(potionDamage!=0 || !customEffects().isEmpty())
                color=net.minecraft.potion.PotionHelper.calcPotionLiquidColor(hitEffects());
        }
        dataWatcher.updateObject(21,Integer.valueOf(color));
    }
    public int getPotionColor() { return dataWatcher.getWatchableObjectInt(21); }
    private java.util.List<net.minecraft.potion.PotionEffect> customEffects() {
        java.util.List<net.minecraft.potion.PotionEffect> result = new java.util.ArrayList<net.minecraft.potion.PotionEffect>();
        net.minecraft.nbt.NBTTagList custom = potionTag.getTagList("CustomPotionEffects",10);
        for(int i=0;i<custom.tagCount();i++) {
            net.minecraft.potion.PotionEffect effect=net.minecraft.potion.PotionEffect.readCustomPotionEffectFromNBT(custom.getCompoundTagAt(i));
            if(effect!=null)result.add(effect);
        }
        return result;
    }
    private boolean isEmbedded() {
        try { return IN_GROUND.getBoolean(this); }
        catch(IllegalAccessException failure) { throw new IllegalStateException("Arrow ground state unavailable",failure); }
    }
    @Override public void onUpdate() {
        super.onUpdate();
        tickPotionState();
    }
    /** Runs after legacy movement has resolved whether the arrow remains embedded. */
    public void tickPotionState() {
        boolean embedded=isEmbedded();
        groundTime=embedded ? groundTime+1 : 0;
        if(worldObj.isRemote) {
            if(!embedded)spawnPotionParticles(2);
            else if(groundTime%5==0)spawnPotionParticles(1);
        } else if(getCombatType()==TYPE_TIPPED && embedded && groundTime>=600 && !customEffects().isEmpty()) {
            worldObj.setEntityState(this,(byte)0);
            potionDamage=0;potionTag=new NBTTagCompound();
            dataWatcher.updateObject(20,Integer.valueOf(TYPE_NORMAL));
            updateColor();
        }
    }
    private void spawnPotionParticles(int count) {
        int color=getPotionColor();if(color<0)return;
        for(int i=0;i<count;i++)worldObj.spawnParticle("mobSpell",
                posX+(rand.nextDouble()-.5)*width,posY+rand.nextDouble()*height,
                posZ+(rand.nextDouble()-.5)*width,
                ((color>>16)&255)/255.0,((color>>8)&255)/255.0,(color&255)/255.0);
    }
    @Override public void handleHealthUpdate(byte state) {
        if(state==0)spawnPotionParticles(20);else super.handleHealthUpdate(state);
    }
    public java.util.List<net.minecraft.potion.PotionEffect> hitEffects() {
        java.util.List<net.minecraft.potion.PotionEffect> result = new java.util.ArrayList<net.minecraft.potion.PotionEffect>();
        if(getCombatType()!=TYPE_TIPPED)return result;
        java.util.List base = net.minecraft.potion.PotionHelper.getPotionEffects(potionDamage, false);
        if(base != null) for(Object value : base) {
            net.minecraft.potion.PotionEffect e = (net.minecraft.potion.PotionEffect)value;
            result.add(new net.minecraft.potion.PotionEffect(e.getPotionID(), Math.max(1,e.getDuration()/8), e.getAmplifier(), e.getIsAmbient()));
        }
        result.addAll(customEffects());
        return result;
    }
    public ItemStack pickupStack() {
        if(getCombatType()==TYPE_NORMAL || (getCombatType()==TYPE_TIPPED && potionDamage==0 && customEffects().isEmpty())) return new ItemStack(Items.arrow);
        ItemStack result = getCombatType() == TYPE_SPECTRAL ? new ItemStack(CombatContent.spectralArrow)
                : new ItemStack(CombatContent.tippedArrow,1,potionDamage);
        if(getCombatType() == TYPE_TIPPED && !potionTag.hasNoTags()) result.setTagCompound((NBTTagCompound)potionTag.copy());
        return result;
    }


    public EntityCombatArrow(World world) { super(world); }
    public EntityCombatArrow(World world, EntityLivingBase shooter, float velocity, int type, int potionDamage) {
        super(world, shooter, velocity);
        dataWatcher.updateObject(20,Integer.valueOf(type));
        this.potionDamage = potionDamage;
        updateColor();
    }
    protected EntityCombatArrow(World world,EntityLivingBase shooter,EntityLivingBase target,float velocity,float inaccuracy) {
        super(world,shooter,target,velocity,inaccuracy);
        dataWatcher.updateObject(20,Integer.valueOf(TYPE_TIPPED));
    }
    /** Called around EntityArrow's real damage result, after all Forge cancellation hooks. */
    public static boolean damageTarget(net.minecraft.entity.Entity target,net.minecraft.util.DamageSource source,float damage,EntityArrow projectile) {
        EntityCombatArrow combat=projectile instanceof EntityCombatArrow?(EntityCombatArrow)projectile:null;
        if(combat!=null && combat.getPierceLevel()>0) {
            if(combat.piercedTargets.contains(target.getUniqueID()))return false;
            if(combat.piercedTargets.size()>=combat.getPierceLevel()+1){combat.setDead();return false;}
            // Modern arrows count attempted hits too; canceled damage still follows the vanilla bounce.
            combat.piercedTargets.add(target.getUniqueID());
        }
        boolean accepted=target.attackEntityFrom(source,damage);
        if(combat!=null)combat.repeatCollision=accepted && combat.getPierceLevel()>0;
        if(accepted && !target.worldObj.isRemote && target instanceof EntityLivingBase
                && !(target instanceof net.minecraft.entity.monster.EntityEnderman)
                && projectile instanceof EntityCombatArrow) {
            EntityCombatArrow arrow=(EntityCombatArrow)projectile;
            if(arrow.getPierceLevel()>0 || arrow.markApplied()) {
                EntityLivingBase living=(EntityLivingBase)target;
                if(arrow.getCombatType()==TYPE_SPECTRAL)
                    living.addPotionEffect(new net.minecraft.potion.PotionEffect(CombatContent.glowing.id,200,0));
                else for(net.minecraft.potion.PotionEffect effect:arrow.hitEffects())
                    living.addPotionEffect(new net.minecraft.potion.PotionEffect(effect));
            }
        }
        return accepted;
    }
    public void setPierceLevel(int level) { dataWatcher.updateObject(22,Byte.valueOf((byte)Math.max(0,Math.min(4,level)))); }
    public int getPierceLevel() { return dataWatcher.getWatchableObjectByte(22)&255; }
    /** Replacement for the collision predicate, preserving every vanilla eligibility check. */
    public static boolean canHit(net.minecraft.entity.Entity target,EntityArrow projectile) {
        return target.canBeCollidedWith() && (!(projectile instanceof EntityCombatArrow)
                || !((EntityCombatArrow)projectile).piercedTargets.contains(target.getUniqueID()));
    }
    public static void updateEmbeddedArrowCount(EntityLivingBase target,int count,EntityArrow projectile) {
        if(!(projectile instanceof EntityCombatArrow) || ((EntityCombatArrow)projectile).getPierceLevel()==0)target.setArrowCountInEntity(count);
    }
    public static void finishEntityHit(EntityArrow projectile) {
        if(!(projectile instanceof EntityCombatArrow) || ((EntityCombatArrow)projectile).getPierceLevel()==0)projectile.setDead();
    }
    public static boolean repeatCollision(EntityArrow projectile) {
        if(!(projectile instanceof EntityCombatArrow))return false;
        EntityCombatArrow arrow=(EntityCombatArrow)projectile;
        boolean again=arrow.repeatCollision && !arrow.isDead;
        arrow.repeatCollision=false;
        return again;
    }
    public int getCombatType() { return dataWatcher.getWatchableObjectInt(20); }
    public int getPotionDamage() { return potionDamage; }
    @Override public void writeSpawnData(io.netty.buffer.ByteBuf data){data.writeInt(getCombatType());data.writeInt(potionDamage);data.writeInt(getPotionColor());data.writeByte(getPierceLevel());}
    @Override public void readSpawnData(io.netty.buffer.ByteBuf data){dataWatcher.updateObject(20,Integer.valueOf(data.readInt()));potionDamage=data.readInt();dataWatcher.updateObject(21,Integer.valueOf(data.readInt()));setPierceLevel(data.readUnsignedByte());}
    public boolean markApplied() { if (applied) return false; applied = true; return true; }
    @Override public void onCollideWithPlayer(EntityPlayer player) {
        if(worldObj.isRemote || isDead || arrowShake > 0) return;
        if(!isEmbedded())return;
        boolean pickup = canBePickedUp == 2 && player.capabilities.isCreativeMode;
        if(canBePickedUp == 1) pickup = player.inventory.addItemStackToInventory(pickupStack());
        if(pickup) {
            playSound("random.pop",.2F,((rand.nextFloat()-rand.nextFloat())*.7F+1)*2);
            player.onItemPickup(this,1);setDead();
        }
    }
    @Override public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setByte("PierceLevel",(byte)getPierceLevel());
        net.minecraft.nbt.NBTTagList ignored=new net.minecraft.nbt.NBTTagList();
        for(java.util.UUID id:piercedTargets)ignored.appendTag(new net.minecraft.nbt.NBTTagString(id.toString()));
        tag.setTag("PiercedTargets",ignored);
        tag.setInteger("CombatType", getCombatType());
        tag.setTag("PotionTag",potionTag.copy());tag.setInteger("Potion", potionDamage); tag.setBoolean("EffectApplied", applied);
    }
    @Override public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        setPierceLevel(tag.getByte("PierceLevel")&255);piercedTargets.clear();repeatCollision=false;
        net.minecraft.nbt.NBTTagList ignored=tag.getTagList("PiercedTargets",8);
        for(int i=0;i<ignored.tagCount() && piercedTargets.size()<5;i++)try{piercedTargets.add(java.util.UUID.fromString(ignored.getStringTagAt(i)));}catch(IllegalArgumentException invalid){}
        dataWatcher.updateObject(20,Integer.valueOf(tag.getInteger("CombatType")));
        potionDamage = tag.getInteger("Potion");setPotionTag(tag.getCompoundTag("PotionTag"));groundTime=0; applied = tag.getBoolean("EffectApplied");
    }
}
