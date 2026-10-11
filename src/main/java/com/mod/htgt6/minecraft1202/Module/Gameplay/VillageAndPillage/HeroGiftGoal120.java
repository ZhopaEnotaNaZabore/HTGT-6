package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import java.util.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.*;
import net.minecraft.init.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;

/** GiveGiftToHero with the original 1.20.2 profession loot distributions. */
public final class HeroGiftGoal120 extends EntityAIBase {
    private final EntityVillager villager;
    private EntityPlayer hero;
    private long next,start;
    private boolean given;
    public HeroGiftGoal120(EntityVillager villager){this.villager=villager;setMutexBits(3);next=villager.worldObj.getTotalWorldTime()+600;}
    public static void attach(EntityVillager villager){
        for(Object raw:villager.tasks.taskEntries)if(((EntityAITasks.EntityAITaskEntry)raw).action instanceof HeroGiftGoal120)return;
        villager.tasks.addTask(6,new HeroGiftGoal120(villager));
    }
    @Override public boolean shouldExecute(){
        if(villager.worldObj.isRemote||villager.worldObj.getTotalWorldTime()<next||VillagerSleep120.isSleeping(villager)||villager.isTrading())return false;
        hero=null;double nearest=32*32;
        for(Object raw:villager.worldObj.playerEntities) {
            EntityPlayer player=(EntityPlayer)raw;double d=villager.getDistanceSqToEntity(player);
            if(player.isEntityAlive()&&VillageEffects120.heroAmplifier(player)>=0&&d<nearest&&villager.getEntitySenses().canSee(player)){hero=player;nearest=d;}
        }
        return hero!=null;
    }
    @Override public void startExecuting(){start=villager.worldObj.getTotalWorldTime();given=false;}
    @Override public boolean continueExecuting(){return !given&&hero!=null&&hero.isEntityAlive()&&VillageEffects120.heroAmplifier(hero)>=0
            &&villager.worldObj.getTotalWorldTime()-start<100&&!VillagerSleep120.isSleeping(villager)&&!villager.isTrading();}
    @Override public void updateTask(){
        villager.getLookHelper().setLookPositionWithEntity(hero,30,30);
        if(villager.getDistanceSqToEntity(hero)<25) {
            if(villager.worldObj.getTotalWorldTime()-start>20) {
                ItemStack gift=gift(VillagerCareers112.getModernProfession(villager),villager.isChild(),villager.getRNG());
                if(gift!=null) {
                    EntityItem item=new EntityItem(villager.worldObj,villager.posX,villager.posY+villager.getEyeHeight()-.3,villager.posZ,gift);
                    double dx=hero.posX-item.posX,dy=hero.posY+1-item.posY,dz=hero.posZ-item.posZ,len=Math.sqrt(dx*dx+dy*dy+dz*dz);
                    if(len>.001){item.motionX=dx/len*.3;item.motionY=dy/len*.3+.1;item.motionZ=dz/len*.3;}
                    item.delayBeforeCanPickup=10;villager.worldObj.spawnEntityInWorld(item);
                }
                given=true;
            }
        } else if(RaidMechanics120.pathRegionLoaded(villager))villager.getNavigator().tryMoveToEntityLiving(hero,.5);
    }
    @Override public void resetTask(){hero=null;next=villager.worldObj.getTotalWorldTime()+600+villager.getRNG().nextInt(6001);villager.getNavigator().clearPathEntity();}
    public static ItemStack gift(String profession,boolean child,Random random){
        if(child)return new ItemStack(Blocks.red_flower,1,0);
        if("armorer".equals(profession))return choose(random,Items.chainmail_helmet,Items.chainmail_chestplate,Items.chainmail_leggings,Items.chainmail_boots);
        if("butcher".equals(profession))return choose(random,BountifulContent.cookedRabbit,Items.cooked_chicken,Items.cooked_porkchop,Items.cooked_beef,BountifulContent.cookedMutton);
        if("cartographer".equals(profession))return choose(random,Items.map,Items.paper);
        if("cleric".equals(profession))return random.nextBoolean()?new ItemStack(Items.redstone):new ItemStack(Items.dye,1,4);
        if("farmer".equals(profession))return choose(random,Items.bread,Items.pumpkin_pie,Items.cookie);
        if("fisherman".equals(profession))return new ItemStack(Items.fish,1,random.nextInt(2));
        if("fletcher".equals(profession)) {
            int choice=random.nextInt(37);if(choice<26)return new ItemStack(Items.arrow);
            if(random.nextInt(2)==0)return null;
            int[] effects={2,10,9,5,12,3,13,14,6,8,4};
            return new ItemStack(CombatContent.tippedArrow,1,8192|effects[choice-26]);
        }
        if("leatherworker".equals(profession))return new ItemStack(Items.leather);
        if("librarian".equals(profession))return new ItemStack(Items.book);
        if("mason".equals(profession))return new ItemStack(Blocks.clay);
        if("shepherd".equals(profession))return new ItemStack(Blocks.wool,1,random.nextInt(16));
        if("toolsmith".equals(profession))return choose(random,Items.stone_pickaxe,Items.stone_axe,Items.stone_hoe,Items.stone_shovel);
        if("weaponsmith".equals(profession))return choose(random,Items.stone_axe,Items.golden_axe,Items.iron_axe);
        return new ItemStack(Items.wheat_seeds);
    }
    private static ItemStack choose(Random random,Item... items){return new ItemStack(items[random.nextInt(items.length)]);}
}
