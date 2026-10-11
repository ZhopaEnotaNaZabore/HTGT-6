package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112;
import net.minecraft.entity.passive.EntityVillager;import net.minecraft.world.World;import net.minecraft.village.*;import net.minecraft.item.*;import net.minecraft.block.Block;import net.minecraft.init.*;import net.minecraft.nbt.*;import java.lang.reflect.*;import java.util.*;import com.google.gson.*;
public class VillagerTrades146Fixture {
 static int checks,nextId=3000;static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static void register(Object o){if(o instanceof Block){Block b=(Block)o;if(Item.getItemFromBlock(b)==null){String key="fixture:block"+nextId;Block.blockRegistry.addObject(nextId,key,b);Item.itemRegistry.addObject(nextId,key,new ItemBlock(b));nextId++;}}else if(o instanceof Item&&Item.getIdFromItem((Item)o)<0){Item.itemRegistry.addObject(nextId,"fixture:item"+nextId,o);nextId++;}}
 static void registerClass(String cls)throws Exception {for(Field f:Class.forName(cls).getFields())if(Modifier.isStatic(f.getModifiers())){Object o=f.get(null);if(o instanceof Object[])for(Object b:(Object[])o)register(b);else register(o);}}
 static EntityVillager v(BountifulMechanicsFixture120.Grid w,String profession){EntityVillager v=new EntityVillager(w);VillagerCareers112.initializeWatcher(v);VillagerCareers112.setModernProfession(v,profession);return v;}
 public static void prepareContent()throws Exception {
  Field side=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");side.setAccessible(true);side.set(null,cpw.mods.fml.relauncher.Side.CLIENT);
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)uf.get(null);Field pot=net.minecraft.potion.Potion.class.getDeclaredField("potionTypes");net.minecraft.potion.Potion[] expanded=Arrays.copyOf(net.minecraft.potion.Potion.potionTypes,65536);u.putObject(u.staticFieldBase(pot),u.staticFieldOffset(pot),expanded);
  String prefix="com.mod.htgt6.minecraft1202.Module.";
  for(String cls:new String[]{"Registry.Bountiful.BountifulContent","Registry.Combat.CombatContent","Registry.VillageAndPillage.VillagePillageContent","Registry.VillageAndPillage.BannerPatterns145","Registry.Modern.Backport120Content","Registry.Modern.EtFVanillaGapContent120","World.Aquatic.OceanContent"})registerClass(prefix+cls);
 }
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();w.provider.worldObj=w;
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  prepareContent();
  Set<String> missing=VillagerTrades146.missingMappings();check(missing.equals(new TreeSet<String>(Arrays.asList("CAMPFIRE","LEATHER_HORSE_ARMOR"))),"explicit missing only: "+missing);
  check(VillagerTrades146.professions().size()==13,"all 13 professions");int total=0;Random random=new Random(4);
  for(String profession:VillagerTrades146.professions()){
   EntityVillager v=v(w,profession);
   for(int rank=1;rank<=5;rank++){JsonArray defs=VillagerTrades146.definitions(profession,rank);check(defs.size()>0,"rank exists");
    for(JsonElement e:defs){total++;JsonObject def=e.getAsJsonObject();String type=def.get("type").getAsString();
     if(type.equals("TreasureMapForEmeralds"))continue;
     ManagedVillagerOffer145 trade=VillagerTrades146.create(def,v,random);
     boolean absent=def.toString().contains("Items.CAMPFIRE")||def.toString().contains("Items.LEATHER_HORSE_ARMOR");
     check((trade==null)==absent,"real factory output "+def);if(trade==null)continue;
     check(trade.villagerXp>0&&trade.writeToTags().getInteger("maxUses")>0,"typed XP / stock");
     check(trade.getItemToBuy().stackSize>0&&trade.getItemToSell().stackSize>0,"positive counts");
     ManagedVillagerOffer145 restored=new ManagedVillagerOffer145(trade.writeToTags());check(restored.villagerXp==trade.villagerXp&&restored.priceMultiplier==trade.priceMultiplier,"metadata roundtrip");
    }
   }
   VillagerCareers112.populate(v,1);MerchantRecipeList offers=VillagerCareers112.getOffersForEconomy(v);check(VillagerEconomy145.managed(v)&&offers.size()==2,"novice activation "+profession);
   VillagerCareers112.populate(v,99);check(offers.size()==2,"no repeat generation");
  }
  check(total==290,"all official factories");
  EntityVillager farmer=v(w,"farmer");MerchantRecipe old=new MerchantRecipe(new ItemStack(Items.wheat,20),new ItemStack(Items.emerald));old.incrementToolUses();ManagedVillagerOffer145 match=VillagerTrades146.matchLegacy(farmer,old);check(match!=null&&match.villagerXp==2&&match.writeToTags().getInteger("uses")==1,"exact migration preserves use count");
  check(VillagerTrades146.matchLegacy(farmer,new MerchantRecipe(new ItemStack(Items.wheat,19),new ItemStack(Items.emerald)))==null,"different input preserved");
  ItemStack named=new ItemStack(Items.wheat,20);named.setStackDisplayName("mod recipe");check(VillagerTrades146.matchLegacy(farmer,new MerchantRecipe(named,new ItemStack(Items.emerald)))==null,"different NBT preserved");
  check(VillagerTrades146.matchLegacy(farmer,new MerchantRecipe(new ItemStack(Items.wheat,20),new ItemStack(Items.emerald)){})==null,"custom recipe subclass preserved");
  MerchantRecipeList legacyList=new MerchantRecipeList();legacyList.add(old);MerchantRecipe unknown=new MerchantRecipe(new ItemStack(Items.diamond),new ItemStack(Items.emerald,7));legacyList.add(unknown);
  Field buying=EntityVillager.class.getDeclaredField("buyingList");buying.setAccessible(true);buying.set(farmer,legacyList);NBTTagCompound saved=new NBTTagCompound();NBTTagCompound offersTag=new NBTTagCompound();NBTTagList recipesTag=new NBTTagList();recipesTag.appendTag(old.writeToTags());recipesTag.appendTag(unknown.writeToTags());offersTag.setTag("Recipes",recipesTag);saved.setTag("Offers",offersTag);VillagerEconomy145.read(farmer,saved);
  check(legacyList.get(0) instanceof ManagedVillagerOffer145&&legacyList.get(1)==unknown,"exact known offer migration keeps unknown identity");check(VillagerEconomy145.hasTraded(farmer)&&VillagerEconomy145.xp(farmer)==0,"legacy use locks profession without guessed historic XP");
  EntityVillager fresh=v(w,"farmer");VillagerCareers112.populate(fresh,1);MerchantRecipeList freshList=VillagerCareers112.getOffersForEconomy(fresh);ManagedVillagerOffer145 first=(ManagedVillagerOffer145)freshList.get(0);while(VillagerEconomy145.xp(fresh)<10)VillagerEconomy145.onTrade(fresh,first);w.time+=40;VillagerEconomy145.beforeAi(fresh);check(freshList.size()==4&&VillagerEconomy145.rank(fresh)==2,"rank up adds exactly two more offers");VillagerEconomy145.beforeAi(fresh);check(freshList.size()==4,"rank offers never appended twice");
  EntityVillager unemployed=v(w,"none");VillagerCareers112.populate(unemployed,1);check(VillagerCareers112.getOffersForEconomy(unemployed).isEmpty(),"unemployed has no legacy fallback trades");
  w.isRemote=true;MerchantRecipeList client=new MerchantRecipeList();VillagerTrades146.addRank(farmer,client,1);check(client.isEmpty(),"server authority");
  System.out.println("PASS trades146: "+checks+" checks; 290 factories, 13 professions, 65 ranks");
 }
}
