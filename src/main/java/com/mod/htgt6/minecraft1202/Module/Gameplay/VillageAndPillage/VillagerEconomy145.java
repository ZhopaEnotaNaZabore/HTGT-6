package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;
import java.util.Map;
import java.util.WeakHashMap;
import java.lang.reflect.Field;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.nbt.*;
import net.minecraft.village.*;
import net.minecraft.item.ItemStack;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112;
import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWanderingTrader120;
/** Modern economy only for offers explicitly supplied with modern XP metadata. */
public final class VillagerEconomy145 {
    private static final String KEY="HTGT6Economy145";
    private static final Map<EntityVillager,Data> DATA=new WeakHashMap<EntityVillager,Data>();
    private static final Field RESET=ReflectionHelper.findField(EntityVillager.class,"timeUntilReset","field_70961_j");
    private VillagerEconomy145(){}
    private static Data data(EntityVillager v){Data d=DATA.get(v);if(d==null){d=new Data();DATA.put(v,d);}return d;}
    private static boolean eligible(EntityVillager v){return !(v instanceof EntityWanderingTrader120)&&v.getProfession()>=0&&v.getProfession()<5;}
    public static boolean managed(EntityVillager v){return eligible(v)&&data(v).managed;}
    public static void reset(EntityVillager v){DATA.remove(v);}
    public static int rank(EntityVillager v){return data(v).rank;}
    public static int xp(EntityVillager v){return data(v).xp;}
    public static boolean hasTraded(EntityVillager v){return managed(v)&&data(v).traded;}
    public static void addOffer(EntityVillager v,MerchantRecipeList list,ManagedVillagerOffer145 offer){
        if(!eligible(v))throw new IllegalArgumentException("Foreign villager profession owns its offers");
        data(v).managed=true;list.add(offer);
    }
    public static void beforeAi(EntityVillager v){
        if(!managed(v)||v.worldObj.isRemote)return;
        try{RESET.setInt(v,0);}catch(IllegalAccessException e){throw new IllegalStateException(e);}
        Data d=data(v);long now=v.worldObj.getTotalWorldTime();
        if(d.levelAt>=0&&now>=d.levelAt&&!v.isTrading()){
            d.levelAt=-1;if(d.rank<5&&d.xp>=threshold(d.rank)){d.rank++;MerchantRecipeList list=VillagerCareers112.getOffersForEconomy(v);if(list!=null)VillagerTrades146.addRank(v,list,d.rank);VillagerCareers112.refreshVisualData(v);}
        }

    }
    public static void onTrade(EntityVillager v,MerchantRecipe offer){
        if(!managed(v)||v.worldObj.isRemote)return;beforeAi(v);Data d=data(v);d.traded=true;
        if(!(offer instanceof ManagedVillagerOffer145))return;
        if(v.getCustomer()!=null)VillagerGossip114.add(v,v.getCustomer().getUniqueID(),4,2);
        d.xp=(int)Math.min(Integer.MAX_VALUE,(long)d.xp+((ManagedVillagerOffer145)offer).villagerXp);
        com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage.RaidNetwork150.sendMerchant(v,v.getCustomer());
        boolean upgrade=d.rank<5&&d.xp>=threshold(d.rank);
        if(upgrade)d.levelAt=v.worldObj.getTotalWorldTime()+40;
        if(((ManagedVillagerOffer145)offer).rewardsExperience())v.worldObj.spawnEntityInWorld(new EntityXPOrb(v.worldObj,v.posX,v.posY+.5D,v.posZ,3+v.getRNG().nextInt(4)+(upgrade?5:0)));
    }
    /** Called after setCustomer, before vanilla sends MC|TrList. Discounts never enter base NBT. */
    public static void onCustomer(EntityVillager v,EntityPlayer customer){
        if(v.worldObj.isRemote||!eligible(v))return;
        MerchantRecipeList offers=customer==null?VillagerCareers112.getOffersForEconomy(v):v.getRecipes(customer);
        if(offers==null)return;int amplifier=customer==null?-1:VillageEffects120.heroAmplifier(customer);
        int reputation=customer==null?0:VillagerGossip114.reputation(v,customer.getUniqueID());
        for(Object raw:offers)if(raw instanceof ManagedVillagerOffer145){ManagedVillagerOffer145 offer=(ManagedVillagerOffer145)raw;offer.applyHero(amplifier);offer.applyReputation(reputation);}
        if(customer!=null)com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage.RaidNetwork150.sendMerchant(v,customer);
    }
    private static void resetUses(MerchantRecipe offer){NBTTagCompound tag=offer.writeToTags();tag.setInteger("uses",0);offer.readFromTags(tag);}
    private static void catchUpDemand(EntityVillager v,Data d){
        int missed=2-d.restocks;if(missed<=0)return;MerchantRecipeList offers=VillagerCareers112.getOffersForEconomy(v);if(offers==null)return;
        for(Object raw:offers)if(raw instanceof ManagedVillagerOffer145){ManagedVillagerOffer145 offer=(ManagedVillagerOffer145)raw;resetUses(offer);for(int i=0;i<missed;i++)offer.updateDemand();}
    }
    public static int threshold(int rank){return rank<=1?10:rank==2?70:rank==3?150:rank==4?250:Integer.MAX_VALUE;}
    public static void workTick(EntityVillager v,VillagePoiData120.Entry poi){
        if(!managed(v)||v.worldObj.isRemote||!v.isEntityAlive())return;Data d=data(v);long now=v.worldObj.getTotalWorldTime();
        beforeAi(v);
        if(v.isChild()||v.isTrading()||poi==null||!v.getUniqueID().toString().equals(poi.owner)
            ||!VillagerCareers112.getModernProfession(v).equals(poi.type)||!v.worldObj.blockExists(poi.x,poi.y,poi.z))return;
        if(!poi.type.equals(VillagePoiType120.forBlock(v.worldObj.getBlock(poi.x,poi.y,poi.z))))return;
        long dayTime=v.worldObj.getWorldTime(),clock=Math.floorMod(dayTime,24000L);
        if(clock<2000||clock>=9000||v.getDistanceSq(poi.x+.5,poi.y+.5,poi.z+.5)>3)return;
        if(now>d.lastRestock+12000||(d.lastDayCheck>0&&dayTime/24000>d.lastDayCheck/24000)){catchUpDemand(v,d);d.restocks=0;d.lastRestock=now;}
        d.lastDayCheck=dayTime;
        if(d.restocks>=2||d.restocks>0&&now<=d.lastRestock+2400)return;
        MerchantRecipeList offers=VillagerCareers112.getOffersForEconomy(v);if(offers==null)return;boolean used=false;
        for(Object raw:offers)if(raw instanceof MerchantRecipe&&((MerchantRecipe)raw).writeToTags().getInteger("uses")>0)used=true;
        if(!used)return;
        for(Object raw:offers)if(raw instanceof MerchantRecipe){MerchantRecipe offer=(MerchantRecipe)raw;
            if(offer.getClass()!=MerchantRecipe.class&&!(offer instanceof ManagedVillagerOffer145))continue;
            if(offer instanceof ManagedVillagerOffer145)((ManagedVillagerOffer145)offer).updateDemand();
            resetUses(offer);
        }
        d.lastRestock=now;d.restocks++;
    }
    public static void write(EntityVillager v,NBTTagCompound tag){if(!managed(v))return;Data d=data(v);NBTTagCompound out=new NBTTagCompound();out.setInteger("Rank",d.rank);out.setInteger("Xp",d.xp);out.setBoolean("Traded",d.traded);out.setLong("LevelAt",d.levelAt);out.setInteger("Restocks",d.restocks);out.setLong("LastRestock",d.lastRestock);out.setLong("LastDayCheck",d.lastDayCheck);tag.setTag(KEY,out);}
    public static void read(EntityVillager v,NBTTagCompound tag){
        if(!eligible(v))return;Data d=data(v);d.managed=false;
        MerchantRecipeList offers=VillagerCareers112.getOffersForEconomy(v);
        NBTTagList saved=tag.getCompoundTag("Offers").getTagList("Recipes",10);
        if(offers!=null)for(int i=0;i<offers.size()&&i<saved.tagCount();i++){
            MerchantRecipe old=(MerchantRecipe)offers.get(i);NBTTagCompound raw=saved.getCompoundTagAt(i);
            if(old instanceof ManagedVillagerOffer145){d.managed=true;continue;}
            if(old.getClass()!=MerchantRecipe.class)continue;
            if(!raw.hasKey("HTGT6VillagerXp",3)){ManagedVillagerOffer145 migrated=VillagerTrades146.matchLegacy(v,old);if(migrated!=null){offers.set(i,migrated);d.managed=true;}continue;}
            ManagedVillagerOffer145 restored=new ManagedVillagerOffer145(raw);
            if(ItemStack.areItemStacksEqual(old.getItemToBuy(),restored.baseFirst())&&ItemStack.areItemStacksEqual(old.getItemToSell(),restored.getItemToSell())&&ItemStack.areItemStacksEqual(old.getSecondItemToBuy(),restored.getSecondItemToBuy())){offers.set(i,restored);d.managed=true;}
        }
        if(!d.managed)return;
        if(!tag.hasKey(KEY,10)){d.rank=1;for(Object raw:offers){MerchantRecipe offer=(MerchantRecipe)raw;if(offer instanceof ManagedVillagerOffer145)d.rank=Math.max(d.rank,((ManagedVillagerOffer145)offer).tradeRank);if(offer.writeToTags().getInteger("uses")>0)d.traded=true;}d.xp=0;d.levelAt=-1;return;}
        NBTTagCompound in=tag.getCompoundTag(KEY);d.rank=Math.max(1,Math.min(5,in.getInteger("Rank")));d.xp=Math.max(0,in.getInteger("Xp"));d.traded=in.getBoolean("Traded");d.levelAt=in.hasKey("LevelAt")?in.getLong("LevelAt"):-1;d.restocks=Math.max(0,Math.min(2,in.getInteger("Restocks")));d.lastRestock=in.getLong("LastRestock");d.lastDayCheck=in.getLong("LastDayCheck");
    }
    private static final class Data{boolean managed,traded;int rank=1,xp,restocks;long levelAt=-1,lastRestock,lastDayCheck;}
}
