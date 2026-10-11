package com.mod.htgt6.minecraft1202.Module.Entity.Combat;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;

public final class EntityLingeringCloud extends Entity {
    private int potionDamage, age, duration=600, waitTime=10, reapplicationDelay=20;
    private boolean dragonBreath;
    private NBTTagCompound potionTag = new NBTTagCompound();
    private int durationOnUse;
    private float radiusOnUse=-.5F, radiusPerTick=-3F/600F;
    private EntityLivingBase owner;
    private java.util.UUID ownerId;
    private final Map<java.util.UUID,Integer> lastApplied=new HashMap<java.util.UUID,Integer>();
    public EntityLingeringCloud(World world){super(world);setSize(6,.5F);noClip=true;}
    public EntityLingeringCloud(World world,EntityLivingBase owner,int potionDamage){
        this(world);this.owner=owner;ownerId=owner==null?null:owner.getUniqueID();this.potionDamage=potionDamage;
        dataWatcher.updateObject(20,Integer.valueOf(potionDamage));setPotionTag(null);
        if(owner!=null)setPosition(owner.posX,owner.posY,owner.posZ);
    }
    public static EntityLingeringCloud dragonBreath(World world,double x,double y,double z){
        EntityLingeringCloud c=new EntityLingeringCloud(world);c.dragonBreath=true;c.waitTime=20;
        c.radiusOnUse=0;c.radiusPerTick=4F/600F;c.dataWatcher.updateObject(21,Byte.valueOf((byte)1));c.setPosition(x,y,z);return c;
    }
    public void setPotionTag(NBTTagCompound tag) {
        potionTag = tag == null ? new NBTTagCompound() : (NBTTagCompound) tag.copy();
        java.util.List<PotionEffect> effects = effects();
        int color = potionTag.hasKey("CustomPotionColor", 99) ? potionTag.getInteger("CustomPotionColor")
                : PotionHelper.calcPotionLiquidColor(effects);
        dataWatcher.updateObject(24, Integer.valueOf(color));
    }
    private java.util.List<PotionEffect> effects() {
        java.util.List<PotionEffect> result = new java.util.ArrayList<PotionEffect>();
        List base = dragonBreath ? java.util.Collections.singletonList(new PotionEffect(net.minecraft.potion.Potion.harm.id,1,1))
                : PotionHelper.getPotionEffects(potionDamage, false);
        if (base != null) for (Object value : base) {
            PotionEffect e = (PotionEffect) value;
            result.add(new PotionEffect(e.getPotionID(), e.getDuration()/4, e.getAmplifier(), e.getIsAmbient()));
        }
        net.minecraft.nbt.NBTTagList custom = potionTag.getTagList("CustomPotionEffects", 10);
        for (int i=0; i<custom.tagCount(); i++) {
            PotionEffect e = PotionEffect.readCustomPotionEffectFromNBT(custom.getCompoundTagAt(i));
            if (e != null) result.add(e);
        }
        return result;
    }
    public void setOwner(EntityLivingBase value){owner=value;ownerId=value==null?null:value.getUniqueID();}
    private EntityLivingBase owner(){
        if(owner==null&&ownerId!=null)for(Object o:worldObj.loadedEntityList)
            if(o instanceof EntityLivingBase&&ownerId.equals(((Entity)o).getUniqueID())){owner=(EntityLivingBase)o;break;}
        return owner;
    }
    @Override protected void entityInit(){
        dataWatcher.addObject(20,Integer.valueOf(0));dataWatcher.addObject(21,Byte.valueOf((byte)0));
        dataWatcher.addObject(24,Integer.valueOf(0x385DC6));
        dataWatcher.addObject(22,Float.valueOf(3));dataWatcher.addObject(23,Byte.valueOf((byte)1));
    }
    public float getRadius(){return dataWatcher.getWatchableObjectFloat(22);}
    public void setRadius(float radius){
        radius=Float.isNaN(radius)?0:Math.max(0,Math.min(32,radius));
        dataWatcher.updateObject(22,Float.valueOf(radius));resize(radius);
    }
    private void resize(float radius){double x=posX,y=posY,z=posZ;setSize(radius*2,.5F);setPosition(x,y,z);}
    public static boolean withinRadius(double dx,double dz,float radius){return dx*dx+dz*dz<=radius*radius;}
    @Override public void onUpdate(){
        super.onUpdate();
        tickCloud();
    }
    public void tickCloud(){
        if(worldObj.isRemote){
            resize(getRadius());boolean waiting=dataWatcher.getWatchableObjectByte(23)!=0;
            if(waiting&&rand.nextBoolean())return;
            int color=isDragonBreath()?0xAA00AA:dataWatcher.getWatchableObjectInt(24);
            float radius=waiting?.2F:getRadius();int count=waiting?2:(int)Math.ceil(Math.PI*radius*radius);
            for(int i=0;i<count;i++){double angle=rand.nextFloat()*Math.PI*2,r=Math.sqrt(rand.nextFloat())*radius;
                worldObj.spawnParticle("mobSpell",posX+Math.cos(angle)*r,posY,posZ+Math.sin(angle)*r,
                    ((color>>16)&255)/255D,((color>>8)&255)/255D,(color&255)/255D);}
            return;
        }
        if(++age>=waitTime+duration){setDead();return;}
        dataWatcher.updateObject(23,Byte.valueOf((byte)(age<waitTime?1:0)));
        if(age<waitTime)return;
        if(radiusPerTick!=0){float next=getRadius()+radiusPerTick;if(next<.5F){setDead();return;}setRadius(next);}
        if(age%5!=0)return;
        java.util.Iterator<Integer> expiry=lastApplied.values().iterator();while(expiry.hasNext())if(expiry.next()<=age)expiry.remove();
        List effects=effects();
        if(effects==null||effects.isEmpty()){lastApplied.clear();return;}
        for(Object o:worldObj.getEntitiesWithinAABB(EntityLivingBase.class,boundingBox)){
            EntityLivingBase target=(EntityLivingBase)o;
            if(!target.isEntityAlive()||lastApplied.containsKey(target.getUniqueID())||!withinRadius(target.posX-posX,target.posZ-posZ,getRadius()))continue;
            lastApplied.put(target.getUniqueID(),age+reapplicationDelay);
            for(Object e:effects){PotionEffect effect=(PotionEffect)e;net.minecraft.potion.Potion potion=net.minecraft.potion.Potion.potionTypes[effect.getPotionID()];
                if(potion==null)continue;
                if(potion.isInstant())potion.affectEntity(owner(),target,effect.getAmplifier(),.5D);
                else target.addPotionEffect(new PotionEffect(effect));
            }
            if(radiusOnUse!=0){float next=getRadius()+radiusOnUse;if(next<.5F){setDead();return;}setRadius(next);}
            if(durationOnUse!=0){duration+=durationOnUse;if(duration<=0){setDead();return;}}
        }
    }
    @Override public boolean interactFirst(EntityPlayer player) {
        if (!isDragonBreath() || player.getHeldItem() == null || player.getHeldItem().getItem() != Items.glass_bottle)
            return false;
        if (worldObj.isRemote) return true;
        ItemStack bottle = player.getHeldItem();
        ItemStack result = new ItemStack(CombatContent.dragonBreath);
        if (!player.capabilities.isCreativeMode && --bottle.stackSize <= 0) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, result);
        } else if (!player.inventory.addItemStackToInventory(result)) {
            player.dropPlayerItemWithRandomChoice(result, false);
        }
        setRadius(getRadius() - 0.5F);
        if(getRadius()<.5F)setDead();
        worldObj.playSoundAtEntity(player, "random.pop", 0.5F, 1.0F);
        return true;
    }
    public boolean isDragonBreath() {
        return dragonBreath || dataWatcher.getWatchableObjectByte(21) != 0;
    }
    @Override public void readEntityFromNBT(NBTTagCompound tag){
        potionDamage=tag.getInteger("Potion");age=tag.getInteger("Age");dragonBreath=tag.getBoolean("DragonBreath");
        durationOnUse=tag.getInteger("DurationOnUse");
        setPotionTag(tag.getCompoundTag("PotionTag"));
        duration=tag.hasKey("Duration",99)?Math.max(0,tag.getInteger("Duration")):600;
        waitTime=tag.hasKey("WaitTime",99)?Math.max(0,tag.getInteger("WaitTime")):(dragonBreath?20:10);
        reapplicationDelay=tag.hasKey("ReapplicationDelay",99)?Math.max(0,tag.getInteger("ReapplicationDelay")):20;
        radiusOnUse=tag.hasKey("RadiusOnUse",99)?tag.getFloat("RadiusOnUse"):(dragonBreath?0:-.5F);
        radiusPerTick=tag.hasKey("RadiusPerTick",99)?tag.getFloat("RadiusPerTick"):(dragonBreath?4F/600F:-3F/600F);
        owner=null;ownerId=tag.hasKey("OwnerMost",99)?new java.util.UUID(tag.getLong("OwnerMost"),tag.getLong("OwnerLeast")):null;
        lastApplied.clear();setRadius(tag.hasKey("Radius",99)?tag.getFloat("Radius"):3);
        dataWatcher.updateObject(20,Integer.valueOf(potionDamage));dataWatcher.updateObject(21,Byte.valueOf((byte)(dragonBreath?1:0)));
        dataWatcher.updateObject(23,Byte.valueOf((byte)(age<waitTime?1:0)));
    }
    @Override public void writeEntityToNBT(NBTTagCompound tag){
        tag.setInteger("Potion",potionDamage);tag.setInteger("Age",age);tag.setBoolean("DragonBreath",dragonBreath);
        tag.setTag("PotionTag",potionTag.copy());tag.setInteger("DurationOnUse",durationOnUse);
        tag.setInteger("Duration",duration);tag.setInteger("WaitTime",waitTime);tag.setInteger("ReapplicationDelay",reapplicationDelay);
        tag.setFloat("Radius",getRadius());tag.setFloat("RadiusOnUse",radiusOnUse);tag.setFloat("RadiusPerTick",radiusPerTick);
        if(ownerId!=null){tag.setLong("OwnerMost",ownerId.getMostSignificantBits());tag.setLong("OwnerLeast",ownerId.getLeastSignificantBits());}
    }
}
