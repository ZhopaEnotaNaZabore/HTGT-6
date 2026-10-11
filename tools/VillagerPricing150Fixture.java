package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemSuspiciousStew120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;
import net.minecraft.entity.passive.EntityVillager;import net.minecraft.entity.item.EntityXPOrb;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.world.World;import net.minecraft.village.*;import net.minecraft.item.*;import net.minecraft.init.*;import net.minecraft.nbt.*;import net.minecraft.potion.*;import java.lang.reflect.*;import java.util.*;
public class VillagerPricing150Fixture {
 static int checks;static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static ManagedVillagerOffer145 offer(int count,int max,int xp,float mult){return new ManagedVillagerOffer145(new ItemStack(Items.emerald,count),null,new ItemStack(Items.bread),max,xp,mult);}
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();w.provider.worldObj=w;Field wi=World.class.getDeclaredField("worldInfo");wi.setAccessible(true);net.minecraft.world.storage.WorldInfo info=new net.minecraft.world.storage.WorldInfo(new NBTTagCompound());wi.set(w,info);info.setWorldTime(4000);w.time=4000;VillagerTrades146Fixture.prepareContent();VillageEffects120.init();
  ManagedVillagerOffer145 a=offer(20,12,2,.05F);for(int i=0;i<12;i++)a.incrementToolUses();a.updateDemand();check(a.demand()==12&&a.getItemToBuy().stackSize==32,"sold-out demand 12, cost 20+floor(20*12*.05)=32");
  check(a.baseFirst().stackSize==20&&a.writeToTags().getCompoundTag("buy").getByte("Count")==20,"base price immutable in storage");
  a.applyHero(0);check(a.getItemToBuy().stackSize==26&&a.specialPrice()==-6,"Hero I uses base, not inflated price");a.applyHero(4);check(a.getItemToBuy().stackSize==21,"Hero V 55 percent base discount");
  ManagedVillagerOffer145 restored=new ManagedVillagerOffer145(a.writeToTags());check(restored.demand()==12&&restored.specialPrice()==0&&restored.getItemToBuy().stackSize==32,"demand persists, customer discount never leaks into reload");
  a.resetSpecialPrice();check(a.getItemToBuy().stackSize==32,"close clears special price");
  ManagedVillagerOffer145 cheap=offer(1,12,2,.05F);cheap.applyHero(0);check(cheap.specialPrice()==-1&&cheap.getItemToBuy().stackSize==1,"minimum discount and minimum cost");
  ManagedVillagerOffer145 cold=offer(20,12,2,.05F);cold.updateDemand();check(cold.demand()==-12&&cold.getItemToBuy().stackSize==20,"negative demand never gives negative surcharge");
  NBTTagCompound n=cold.writeToTags();n.setInteger("HTGT6Demand",Integer.MAX_VALUE);ManagedVillagerOffer145 high=new ManagedVillagerOffer145(n);check(high.getItemToBuy().stackSize==64,"extreme demand safely clamps stack limit");
  MerchantRecipeList wire=new MerchantRecipeList();wire.add(a);a.applyHero(0);net.minecraft.network.PacketBuffer buffer=new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());wire.func_151391_a(buffer);MerchantRecipeList received=MerchantRecipeList.func_151390_b(buffer);check(((MerchantRecipe)received.get(0)).getItemToBuy().stackSize==26,"vanilla trade packet carries computed price");buffer.release();
  check(wire.canRecipeBeUsed(new ItemStack(Items.emerald,25),null,0)==null&&wire.canRecipeBeUsed(new ItemStack(Items.emerald,26),null,0)==a,"server recipe matching charges displayed amount");
  EntityVillager v=VillagerEconomy145Fixture.villager(w);MerchantRecipeList offers=new MerchantRecipeList();ManagedVillagerOffer145 active=offer(20,12,2,.05F);VillagerEconomy145Fixture.offers(v,offers);VillagerEconomy145.addOffer(v,offers,active);MerchantRecipe legacy=new MerchantRecipe(new ItemStack(Items.diamond),new ItemStack(Items.emerald,7));offers.add(legacy);
  Swimming115Fixture.Player hero=new Swimming115Fixture.Player(w),normal=new Swimming115Fixture.Player(w);VillageEffects120.rewardHero(hero,1);VillagerEconomy145.onCustomer(v,hero);check(active.getItemToBuy().stackSize==14&&legacy.getItemToBuy().stackSize==1,"only managed offers get active customer Hero");VillagerEconomy145.onCustomer(v,null);check(active.getItemToBuy().stackSize==20,"logout/close hook clears");VillagerEconomy145.onCustomer(v,normal);check(active.getItemToBuy().stackSize==20,"normal customer cannot inherit discount");
  VillagerEconomy145.onTrade(v,active);check(w.lastSpawn instanceof EntityXPOrb&&((EntityXPOrb)w.lastSpawn).xpValue>=3&&((EntityXPOrb)w.lastSpawn).xpValue<=6,"ordinary accepted trade XP orb3..6");
  for(int i=0;i<4;i++)VillagerEconomy145.onTrade(v,active);check(((EntityXPOrb)w.lastSpawn).xpValue>=8&&((EntityXPOrb)w.lastSpawn).xpValue<=11,"rank-up accepted trade XP orb8..11");
  Object previous=w.lastSpawn;VillagerEconomy145.onTrade(v,legacy);check(w.lastSpawn==previous,"unknown legacy offer no guessed XP");
  n=active.writeToTags();n.setBoolean("HTGT6RewardExperience",false);ManagedVillagerOffer145 silent=new ManagedVillagerOffer145(n);VillagerEconomy145.onTrade(v,silent);check(w.lastSpawn==previous,"rewardExperience=false respected");
  for(int i=0;i<12;i++)active.incrementToolUses();VillagePoiData120.Entry poi=new VillagePoiData120.Entry(0,10,0,"farmer",v.getUniqueID().toString());w.setBlock(0,10,0,VillagePillageContent.composter,0,2);VillagerEconomy145.workTick(v,poi);check(active.demand()==12&&active.writeToTags().getInteger("uses")==0,"restock demand before stock reset");
  info.setWorldTime(28000);w.time=28000;VillagerEconomy145.workTick(v,poi);check(active.demand()==0,"missed second restock decays demand next day");
  Method eaten=ItemSuspiciousStew120.class.getDeclaredMethod("onFoodEaten",ItemStack.class,World.class,EntityPlayer.class);eaten.setAccessible(true);hero.getFoodStats().setFoodLevel(0);hero.getFoodStats().setFoodSaturationLevel(0);ItemStack stew=new ItemStack(OceanContent.suspiciousStew);NBTTagCompound payload=new NBTTagCompound();payload.setInteger("EffectId",23);payload.setInteger("EffectDuration",7);stew.setTagCompound(payload);eaten.invoke(OceanContent.suspiciousStew,stew,w,hero);PotionEffect saturation=hero.getActivePotionEffect(Potion.potionTypes[23]);check(saturation!=null&&saturation.getDuration()==7&&hero.getFoodStats().getFoodLevel()==0,"stew installs complete saturation effect");for(int i=0;i<7;i++)saturation.onUpdate(hero);check(hero.getFoodStats().getFoodLevel()==7,"seven saturation ticks restore seven hunger");
  for(int i=0;i<400;i++){ItemStack generated=ItemSuspiciousStew120.create(new Random(i));NBTTagCompound tag=generated.getTagCompound();if(tag.getInteger("EffectId")==23)check(tag.getInteger("EffectDuration")>=7&&tag.getInteger("EffectDuration")<=10,"instant loot duration remains ticks");}
  w.isRemote=true;previous=w.lastSpawn;int demand=active.demand();VillagerEconomy145.onTrade(v,active);VillagerEconomy145.onCustomer(v,hero);check(w.lastSpawn==previous&&active.demand()==demand,"client cannot spawn trade XP or change demand");
  System.out.println("PASS pricing150: "+checks+" checks");
 }
}
