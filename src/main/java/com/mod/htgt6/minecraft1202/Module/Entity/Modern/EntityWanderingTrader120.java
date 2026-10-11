package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookAtTradePlayer;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITradePlayer;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;

/** Wandering Trader implemented through the 1.7 IMerchant/villager protocol. */
public final class EntityWanderingTrader120 extends EntityVillager {
    public static final int DEFAULT_DESPAWN_DELAY=48000;
    public static final int COMMON_OFFERS=5,SPECIAL_OFFERS=1;
    private MerchantRecipeList wanderingOffers;
    private int despawnDelay=DEFAULT_DESPAWN_DELAY;
    private int targetX,targetY,targetZ;

    public EntityWanderingTrader120(World world){
        super(world,0);setSize(.6F,1.95F);getNavigator().setAvoidsWater(true);
        tasks.taskEntries.clear();targetTasks.taskEntries.clear();
        tasks.addTask(0,new EntityAISwimming(this));
        tasks.addTask(0,new DrinkGoal());
        tasks.addTask(1,new EntityAITradePlayer(this));tasks.addTask(1,new EntityAILookAtTradePlayer(this));
        tasks.addTask(1,new EntityAIAvoidEntity(this,EntityZombie.class,8F,.5D,.5D));
        tasks.addTask(1,new EntityAIAvoidEntity(this,EntityMob.class,8F,.5D,.5D));
        tasks.addTask(2,new WanderToTargetGoal());tasks.addTask(3,new EntityAIWander(this,.35D));
        tasks.addTask(4,new EntityAIWatchClosest(this,EntityPlayer.class,8F));tasks.addTask(5,new EntityAILookIdle(this));
    }
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.5D);}
    @Override protected void updateAITick(){/* A wandering trader never joins 1.7 village/home bookkeeping. */}
    @Override public EntityVillager createChild(EntityAgeable mate){return null;}
    @Override public boolean isChild(){return false;}
    @Override protected boolean canDespawn(){return false;}
    public void setDespawnDelay(int ticks){despawnDelay=Math.max(0,ticks);}
    public int getDespawnDelay(){return despawnDelay;}
    public void setWanderTarget(int x,int y,int z){targetX=x;targetY=y;targetZ=z;}

    @Override public void onLivingUpdate(){
        super.onLivingUpdate();if(worldObj.isRemote)return;
        if(!isTrading()&&despawnDelay>0&&--despawnDelay==0){setDead();return;}
    }

    @Override public MerchantRecipeList getRecipes(EntityPlayer player){
        if(wanderingOffers==null)wanderingOffers=createOffers(rand);return wanderingOffers;
    }
    @Override public void useRecipe(MerchantRecipe recipe){recipe.incrementToolUses();livingSoundTime=-getTalkInterval();
        playSound("htgt6:entity.wandering_trader.yes",getSoundVolume(),getSoundPitch());
        if(!worldObj.isRemote)worldObj.spawnEntityInWorld(new net.minecraft.entity.item.EntityXPOrb(worldObj,posX,posY+.5,posZ,3+rand.nextInt(4)));}
    @Override protected String getLivingSound(){return "htgt6:entity.wandering_trader.ambient";}
    @Override protected String getHurtSound(){return "htgt6:entity.wandering_trader.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.wandering_trader.death";}
    private final class DrinkGoal extends EntityAIBase {
        int ticks;boolean invisible;
        DrinkGoal(){setMutexBits(3);}
        public boolean shouldExecute(){return !isTrading()&&(worldObj.isDaytime()==isPotionActive(Potion.invisibility));}
        public void startExecuting(){invisible=!worldObj.isDaytime();ticks=32;getNavigator().clearPathEntity();setCurrentItemOrArmor(0,new ItemStack(invisible?Items.potionitem:Items.milk_bucket,1,invisible?8206:0));}
        public boolean continueExecuting(){return ticks>0&&!isTrading();}
        public void updateTask(){if(--ticks==0){if(invisible)addPotionEffect(new PotionEffect(Potion.invisibility.id,3600,0));else curePotionEffects(new ItemStack(Items.milk_bucket));playSound("htgt6:entity.wandering_trader."+(invisible?"drink_potion":"drink_milk"),1,1);}}
        public void resetTask(){setCurrentItemOrArmor(0,null);ticks=0;}
    }
    public static MerchantRecipeList createOffers(Random random){
        return com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.WanderingTraderOffers114.createOffers(random);
    }
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("DespawnDelay",despawnDelay);
        tag.setInteger("WanderTargetX",targetX);tag.setInteger("WanderTargetY",targetY);tag.setInteger("WanderTargetZ",targetZ);
        if(wanderingOffers!=null)tag.setTag("WanderingOffers",wanderingOffers.getRecipiesAsTags());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);
        despawnDelay=tag.hasKey("DespawnDelay")?Math.max(0,tag.getInteger("DespawnDelay")):DEFAULT_DESPAWN_DELAY;
        targetX=tag.getInteger("WanderTargetX");targetY=tag.getInteger("WanderTargetY");targetZ=tag.getInteger("WanderTargetZ");
        if(tag.hasKey("WanderingOffers",10))wanderingOffers=new MerchantRecipeList(tag.getCompoundTag("WanderingOffers"));}

    private final class WanderToTargetGoal extends EntityAIBase {
        WanderToTargetGoal(){setMutexBits(1);}
        @Override public boolean shouldExecute(){return !isTrading()&&getDistanceSq(targetX+.5D,targetY,targetZ+.5D)>4D;}
        @Override public boolean continueExecuting(){return shouldExecute()&&!getNavigator().noPath();}
        @Override public void startExecuting(){getNavigator().tryMoveToXYZ(targetX+.5D,targetY,targetZ+.5D,.35D);}
    }
}
