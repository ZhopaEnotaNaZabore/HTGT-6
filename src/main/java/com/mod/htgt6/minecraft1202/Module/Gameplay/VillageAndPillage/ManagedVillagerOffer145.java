package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipe;
/** Known modern offer with explicit villager XP; unknown legacy/custom offers are never guessed. */
public final class ManagedVillagerOffer145 extends MerchantRecipe {
    public final int villagerXp;
    public int tradeRank=1;
    public final float priceMultiplier;
    private int demand, specialPrice;
    private boolean rewardExperience=true;
    public ManagedVillagerOffer145(ItemStack first,ItemStack second,ItemStack result,int maxUses,int xp){
        this(first,second,result,maxUses,xp,.05F);
    }
    public ManagedVillagerOffer145(ItemStack first,ItemStack second,ItemStack result,int maxUses,int xp,float multiplier){
        super(first,second,result);priceMultiplier=multiplier;villagerXp=Math.max(0,xp);NBTTagCompound tag=super.writeToTags();tag.setInteger("maxUses",Math.max(1,maxUses));readFromTags(tag);
    }
    public ManagedVillagerOffer145(NBTTagCompound tag){super(tag);demand=tag.getInteger("HTGT6Demand");rewardExperience=!tag.hasKey("HTGT6RewardExperience")||tag.getBoolean("HTGT6RewardExperience");tradeRank=Math.max(1,Math.min(5,tag.getInteger("HTGT6TradeRank")));priceMultiplier=tag.hasKey("HTGT6PriceMultiplier")?tag.getFloat("HTGT6PriceMultiplier"):.05F;villagerXp=Math.max(0,tag.getInteger("HTGT6VillagerXp"));}
    public ItemStack baseFirst(){return super.getItemToBuy();}
    public int demand(){return demand;}
    public int specialPrice(){return specialPrice;}
    public boolean rewardsExperience(){return rewardExperience;}
    public void updateDemand(){NBTTagCompound tag=super.writeToTags();long next=(long)demand+2L*tag.getInteger("uses")-tag.getInteger("maxUses");demand=(int)Math.max(Integer.MIN_VALUE,Math.min(Integer.MAX_VALUE,next));}
    public void resetSpecialPrice(){specialPrice=0;}
    public void applyHero(int amplifier){specialPrice=amplifier<0?0:-Math.max(1,(int)Math.floor((.3D+.0625D*amplifier)*baseFirst().stackSize));}
    public void applyReputation(int reputation){specialPrice-=(int)Math.floor(reputation*priceMultiplier);}
    @Override public ItemStack getItemToBuy(){
        ItemStack base=baseFirst();if(base==null)return null;ItemStack priced=base.copy();
        long extra=Math.max(0L,(long)Math.floor((float)base.stackSize*(float)demand*priceMultiplier));
        priced.stackSize=(int)Math.max(1L,Math.min(base.getMaxStackSize(),base.stackSize+extra+(long)specialPrice));return priced;
    }
    @Override public NBTTagCompound writeToTags(){NBTTagCompound tag=super.writeToTags();tag.setInteger("HTGT6Demand",demand);tag.setBoolean("HTGT6RewardExperience",rewardExperience);tag.setInteger("HTGT6TradeRank",tradeRank);tag.setInteger("HTGT6VillagerXp",villagerXp);tag.setFloat("HTGT6PriceMultiplier",priceMultiplier);return tag;}
}
