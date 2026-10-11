package com.mod.htgt6.minecraft1202.Module.Entity.Modern;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageWorkstationLogic120;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
/** Separate cat identity; inheriting ocelot geometry also preserves native creeper avoidance. */
public final class EntityCat114 extends EntityOcelot {
    private int rested;private boolean ownerWasSleeping;
    private static final java.lang.reflect.Field OWNER_SLEEP_TIMER=cpw.mods.fml.relauncher.ReflectionHelper.findField(EntityPlayer.class,"sleepTimer","field_71076_b");
    public EntityCat114(World world){super(world);setSize(.6F,.7F);tasks.taskEntries.clear();targetTasks.taskEntries.clear();
        tasks.addTask(1,new EntityAISwimming(this));tasks.addTask(2,func_70907_r());
        tasks.addTask(3,new EntityAITempt(this,.6,Items.fish,true){public boolean shouldExecute(){EntityPlayer p=worldObj.getClosestPlayerToEntity(EntityCat114.this,10);return p!=null&&isBreedingItem(p.getHeldItem())&&super.shouldExecute();}});
        tasks.addTask(4,new EntityAIAvoidEntity(this,EntityPlayer.class,16,.8,1.33));tasks.addTask(5,new EntityAIFollowOwner(this,1,10,5));
        tasks.addTask(6,new EntityAIOcelotSit(this,1.33));tasks.addTask(7,new EntityAILeapAtTarget(this,.3F));tasks.addTask(8,new EntityAIOcelotAttack(this));
        tasks.addTask(9,new EntityAIMate(this,.8));tasks.addTask(10,new EntityAIWander(this,.8));tasks.addTask(11,new EntityAIWatchClosest(this,EntityPlayer.class,10));
        targetTasks.addTask(1,new EntityAITargetNonTamed(this,com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulRabbit.class,10,false));
    }
    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(20,Integer.valueOf(0));dataWatcher.addObject(21,Byte.valueOf((byte)1));dataWatcher.addObject(22,Byte.valueOf((byte)0));}
    public int variant(){return dataWatcher.getWatchableObjectInt(20);}public void setVariant(int v){dataWatcher.updateObject(20,Integer.valueOf(Math.max(0,Math.min(10,v))));}
    public int collar(){return dataWatcher.getWatchableObjectByte(21)&15;}public void setCollar(int v){dataWatcher.updateObject(21,Byte.valueOf((byte)(v&15)));}
    public boolean lying(){return dataWatcher.getWatchableObjectByte(22)!=0;}
    @Override public boolean isBreedingItem(ItemStack s){return s!=null&&s.getItem()==Items.fish&&(s.getItemDamage()==0||s.getItemDamage()==1);}
    @Override public boolean canMateWith(EntityAnimal other){return other instanceof EntityCat114&&other!=this&&isTamed()&&((EntityCat114)other).isTamed()&&isInLove()&&other.isInLove();}
    @Override public EntityCat114 createChild(EntityAgeable mate){EntityCat114 child=new EntityCat114(worldObj);child.setVariant(rand.nextBoolean()?variant():((EntityCat114)mate).variant());if(isTamed()){child.setTamed(true);child.func_152115_b(func_152113_b());child.setCollar(rand.nextBoolean()?collar():((EntityCat114)mate).collar());}return child;}
    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData group){setVariant(rand.nextInt(worldObj.getCurrentMoonPhaseFactor()>.9F?11:10));return group;}
    @Override public boolean getCanSpawnHere(){return worldObj.checkNoEntityCollision(boundingBox)&&worldObj.getCollidingBoundingBoxes(this,boundingBox).isEmpty()&&!worldObj.isAnyLiquid(boundingBox);}
    @Override public boolean interact(EntityPlayer player){ItemStack held=player.getHeldItem();
        if(isBreedingItem(held)){
            if(!worldObj.isRemote){
                if(!isTamed()){consume(player,held);if(rand.nextInt(3)==0){setTamed(true);func_152115_b(player.getUniqueID().toString());func_70907_r().setSitting(true);worldObj.setEntityState(this,(byte)7);}else worldObj.setEntityState(this,(byte)6);}
                else if(getHealth()<getMaxHealth()){consume(player,held);heal(2);playSound("htgt6:entity.cat.eat",1,1);}
                else if(getGrowingAge()==0&&!isInLove()){consume(player,held);func_146082_f(player);}
                else if(isChild()){consume(player,held);setGrowingAge(Math.min(0,getGrowingAge()+Math.max(1,-getGrowingAge()/10)));}
            }return true;
        }
        if(isTamed()&&func_152114_e(player)){if(!worldObj.isRemote){int dye=VillageWorkstationLogic120.dyeColor(held);if(dye>=0){if(dye!=collar()){setCollar(dye);consume(player,held);}}else func_70907_r().setSitting(!isSitting());}return true;}
        return false;
    }
    private static void consume(EntityPlayer p,ItemStack s){if(!p.capabilities.isCreativeMode)--s.stackSize;}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(worldObj.isRemote)return;EntityLivingBase owner=getOwner();boolean sleeping=owner instanceof EntityPlayer&&((EntityPlayer)owner).isPlayerSleeping();boolean near=sleeping&&!isSitting()&&getDistanceSqToEntity(owner)<6.25;
        dataWatcher.updateObject(22,Byte.valueOf((byte)(near?1:0)));if(near){getNavigator().clearPathEntity();rested++;}else if(sleeping&&getDistanceSqToEntity(owner)<100&&!isSitting())getNavigator().tryMoveToEntityLiving(owner,1.1);
        if(ownerWasSleeping&&!sleeping){if(rested>16&&owner!=null&&getDistanceSqToEntity(owner)<25&&canGiveMorningGift(worldObj.getCelestialAngle(1),owner instanceof EntityPlayer&&completedOwnerSleep((EntityPlayer)owner))&&rand.nextFloat()<.7F)entityDropItem(morningGift(rand),0);rested=0;}ownerWasSleeping=sleeping;
    }
    public static boolean restrictLegacyMating(boolean allowed,EntityAnimal partner){return allowed&&!(partner instanceof EntityCat114);}
    /** getSleepTimer is client-only in 1.7; the field also retains 100 ticks during normal wakeup. */
    public static boolean completedOwnerSleep(EntityPlayer owner){try{return OWNER_SLEEP_TIMER.getInt(owner)>=100;}catch(IllegalAccessException error){throw new IllegalStateException("Cannot read owner sleep timer",error);}}
    public static boolean canGiveMorningGift(float celestialAngle,boolean completedSleep){return completedSleep&&celestialAngle>.77F&&celestialAngle<.8F;}
    public static ItemStack morningGift(java.util.Random random){int roll=random.nextInt(62);net.minecraft.item.Item[] gifts={BountifulContent.rabbitHide,BountifulContent.rabbitFoot,Items.chicken,Items.feather,Items.rotten_flesh,Items.string};return roll<60?new ItemStack(gifts[roll/10]):new ItemStack(com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.phantomMembrane);}
    @Override protected String getLivingSound(){return "htgt6:entity.cat."+(lying()?"purr":isTamed()?"ambient":"stray_ambient");}
    @Override protected String getHurtSound(){return "htgt6:entity.cat.hurt";}@Override protected String getDeathSound(){return "htgt6:entity.cat.death";}
    @Override protected void dropFewItems(boolean player,int looting){int count=rand.nextInt(3)+Math.round(rand.nextFloat()*Math.max(0,looting));if(count>0)entityDropItem(new ItemStack(Items.string,count),0);}
    @Override public String getCommandSenderName(){return hasCustomNameTag()?getCustomNameTag():net.minecraft.util.StatCollector.translateToLocal("entity.HTGT6.Cat.name");}
    @Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);n.setInteger("CatVariant114",variant());n.setInteger("Collar114",collar());}
    @Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);setVariant(n.getInteger("CatVariant114"));setCollar(n.hasKey("Collar114")?n.getInteger("Collar114"):1);}
}
