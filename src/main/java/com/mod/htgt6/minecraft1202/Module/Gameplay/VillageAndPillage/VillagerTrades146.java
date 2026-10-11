package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.google.gson.*;
import java.io.*;
import java.util.*;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.enchantment.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

/** Ordinary 1.20.2 tables; a missing port item never becomes a substitute offer. */
public final class VillagerTrades146 {
 private static final JsonObject TABLES=load();
 private VillagerTrades146(){}
 private static JsonObject load(){
  InputStream in=VillagerTrades146.class.getResourceAsStream("/assets/htgt6/data/villager_trades_1202.json");
  if(in==null)throw new IllegalStateException("Missing modern villager trade tables");
  try(Reader reader=new InputStreamReader(in,"UTF-8")){return new JsonParser().parse(reader).getAsJsonObject();}
  catch(IOException e){throw new IllegalStateException("Cannot read villager trade tables",e);}
 }
 public static Set<String> professions(){Set<String> out=new HashSet<String>();for(Map.Entry<String,JsonElement> e:TABLES.entrySet())out.add(e.getKey());return out;}
 public static JsonArray definitions(String profession,int rank){JsonObject p=TABLES.has(profession)?TABLES.getAsJsonObject(profession):null;return p==null||!p.has(""+rank)?new JsonArray():p.getAsJsonArray(""+rank);}
 public static Set<String> missingMappings(){
  Set<String> result=new TreeSet<String>();java.util.regex.Pattern p=java.util.regex.Pattern.compile("(?:Items|Blocks)\\.([A-Z_]+)");
  java.util.regex.Matcher m=p.matcher(TABLES.toString());while(m.find()){ItemStack stack=VillagerTradeItems146.stack(m.group(1));if(stack==null||stack.getItem()==null)result.add(m.group(1));}return result;
 }
 public static boolean supports(String profession){return TABLES.has(profession);}
 /** Shuffle factory identities, then choose two, as vanilla does; null factories are not rerolled. */
 public static void addRank(EntityVillager villager,MerchantRecipeList list,int rank){
  if(villager.worldObj.isRemote)return;
  JsonArray definitions=definitions(VillagerCareers112.getModernProfession(villager),rank);
  List<JsonObject> choices=new ArrayList<JsonObject>();for(JsonElement e:definitions)choices.add(e.getAsJsonObject());
  Collections.shuffle(choices,villager.getRNG());
  for(int i=0;i<Math.min(2,choices.size());i++){
   ManagedVillagerOffer145 offer=create(choices.get(i),villager,villager.getRNG());
   if(offer!=null){offer.tradeRank=rank;VillagerEconomy145.addOffer(villager,list,offer);}
  }
 }
 private static int n(JsonArray a,int i){return Integer.parseInt(a.get(i).getAsString());}
 private static float f(JsonArray a,int i){return Float.parseFloat(a.get(i).getAsString().replace("f",""));}
 private static ItemStack item(JsonArray a,int i,int count){ItemStack s=VillagerTradeItems146.stack(a.get(i).getAsString());if(s!=null)s.stackSize=count;return s;}
 private static ItemStack emerald(int count){return new ItemStack(Items.emerald,count);}
 private static ManagedVillagerOffer145 offer(ItemStack first,ItemStack second,ItemStack result,int max,int xp,float mult){
  if(first==null||first.getItem()==null||result==null||result.getItem()==null)return null;
  return new ManagedVillagerOffer145(first,second,result,max,xp,mult);
 }
 public static ManagedVillagerOffer145 create(JsonObject def,EntityVillager v,Random random){
  String type=def.get("type").getAsString();JsonArray a=def.getAsJsonArray("args");
  if(type.equals("EmeraldForItems"))return offer(item(a,0,n(a,1)),null,emerald(a.size()>4?n(a,4):1),n(a,2),n(a,3),.05F);
  if(type.equals("ItemsForEmeralds"))return offer(emerald(n(a,1)),null,item(a,0,n(a,2)),a.size()>4?n(a,3):12,n(a,a.size()>4?4:3),a.size()>5?f(a,5):.05F);
  if(type.equals("ItemsAndEmeraldsToItems")){
   ItemStack second=item(a,0,n(a,1));return second==null?null:offer(emerald(n(a,2)),second,item(a,3,n(a,4)),n(a,5),n(a,6),f(a,7));
  }
  if(type.equals("EnchantedItemForEmeralds")){
   ItemStack result=item(a,0,1);if(result==null)return null;int power=5+random.nextInt(15);
   result=EnchantmentHelper.addRandomEnchantment(random,result,power);
   return offer(emerald(Math.min(64,n(a,1)+power)),null,result,n(a,2),n(a,3),a.size()>4?f(a,4):.05F);
  }
  if(type.equals("EnchantBookForEmeralds")){
   List<Enchantment> choices=new ArrayList<Enchantment>();for(Enchantment e:Enchantment.enchantmentsList)if(e!=null&&e.isAllowedOnBooks())choices.add(e);
   if(choices.isEmpty())return null;Enchantment e=choices.get(random.nextInt(choices.size()));int level=e.getMinLevel()+random.nextInt(e.getMaxLevel()-e.getMinLevel()+1);
   int price=2+random.nextInt(5+level*10)+3*level;
   if(e instanceof com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.EnchantmentCurse112||e instanceof EnchantmentCombat&&((EnchantmentCombat)e).isTreasure())price*=2;
   ItemStack result=Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(e,level));
   return offer(emerald(Math.min(64,price)),new ItemStack(Items.book),result,12,n(a,0),.2F);
  }
  if(type.equals("SuspiciousStewForEmerald")){
   String effect=a.get(0).getAsString();int id=effect.endsWith("NIGHT_VISION")?16:effect.endsWith("JUMP")?8:effect.endsWith("WEAKNESS")?18:effect.endsWith("BLINDNESS")?15:effect.endsWith("POISON")?19:23;
   ItemStack result=new ItemStack(OceanContent.suspiciousStew);NBTTagCompound tag=new NBTTagCompound();tag.setInteger("EffectId",id);tag.setInteger("EffectDuration",n(a,1));result.setTagCompound(tag);
   return offer(emerald(1),null,result,12,n(a,2),.05F);
  }
  if(type.equals("EmeraldsForVillagerTypeItem")){
   String biome=VillagerCareers112.getVisualType(v);String boat=biome.equals("taiga")||biome.equals("snow")?"SPRUCE":biome.equals("desert")||biome.equals("jungle")?"JUNGLE":biome.equals("savanna")?"ACACIA":biome.equals("swamp")?"DARK_OAK":"OAK";
   ItemStack input=VillagerTradeItems146.stack(boat+"_BOAT");input.stackSize=n(a,0);return offer(input,null,emerald(1),n(a,1),n(a,2),.05F);
  }
  if(type.equals("TippedArrowForItemsAndEmeralds")){
   ItemStack second=item(a,0,n(a,1)),result=item(a,2,n(a,3));if(second==null||result==null)return null;
   int[] variants=CombatPotionVariants120.values();if(variants.length==0)return null;result.setItemDamage(variants[random.nextInt(variants.length)]);
   return offer(emerald(n(a,4)),second,result,n(a,5),n(a,6),.05F);
  }
  if(type.equals("TreasureMapForEmeralds")){
   String destination=a.get(2).getAsString().contains("monument")?"monument":"mansion";
   ItemStack map=ExplorationContent112.explorerMap.create(v.worldObj,destination,(int)v.posX,(int)v.posZ);
   return offer(emerald(n(a,0)),new ItemStack(Items.compass),map,n(a,4),n(a,5),.2F);
  }
  if(type.equals("DyedArmorForEmeralds")){
   ItemStack result=item(a,0,1);if(result==null)return null;
   if(result.getItem() instanceof ItemArmor){
    int[] colors={0xF9FFFE,16351261,13061821,3847130,16701501,8439583,15961002,4673362,0x9D9D97,1481884,8991416,3949738,8606770,6192150,11546150,0x1D1D21};int[] rgb=new int[3];int brightness=0,count=1;int color=colors[random.nextInt(16)];
    for(int i=0;i<3;i++)rgb[i]=(color>>(16-i*8))&255;brightness=Math.max(rgb[0],Math.max(rgb[1],rgb[2]));
    for(float chance:new float[]{.7F,.8F})if(random.nextFloat()>chance){color=colors[random.nextInt(16)];int r=color>>16&255,g=color>>8&255,b=color&255;rgb[0]+=r;rgb[1]+=g;rgb[2]+=b;brightness+=Math.max(r,Math.max(g,b));count++;}
    for(int i=0;i<3;i++)rgb[i]/=count;float scale=((float)brightness/count)/Math.max(1,Math.max(rgb[0],Math.max(rgb[1],rgb[2])));
    ((ItemArmor)result.getItem()).func_82813_b(result,((int)(rgb[0]*scale)<<16)|((int)(rgb[1]*scale)<<8)|(int)(rgb[2]*scale));
   }
   return offer(emerald(n(a,1)),null,result,a.size()>2?n(a,2):12,a.size()>3?n(a,3):1,.2F);
  }
  throw new IllegalArgumentException("Unknown trade factory "+type);
 }
 /** Exact deterministic legacy matches only; randomized enchant/NBT output is deliberately left intact. */
 public static ManagedVillagerOffer145 matchLegacy(EntityVillager v,MerchantRecipe recipe){
  if(recipe.getClass()!=MerchantRecipe.class)return null;
  for(int rank=1;rank<=5;rank++)for(JsonElement e:definitions(VillagerCareers112.getModernProfession(v),rank)){
   JsonObject def=e.getAsJsonObject();String type=def.get("type").getAsString();
   if(!type.equals("EmeraldForItems")&&!type.equals("ItemsForEmeralds")&&!type.equals("ItemsAndEmeraldsToItems"))continue;
   ManagedVillagerOffer145 match=create(def,v,new Random(0));
   if(match!=null&&same(recipe,match)){NBTTagCompound tag=recipe.writeToTags();tag.setInteger("HTGT6TradeRank",rank);tag.setInteger("HTGT6VillagerXp",match.villagerXp);tag.setFloat("HTGT6PriceMultiplier",match.priceMultiplier);tag.setInteger("maxUses",match.writeToTags().getInteger("maxUses"));return new ManagedVillagerOffer145(tag);}
  }
  return null;
 }
 private static boolean same(MerchantRecipe a,MerchantRecipe b){return ItemStack.areItemStacksEqual(a.getItemToBuy(),b.getItemToBuy())&&ItemStack.areItemStacksEqual(a.getSecondItemToBuy(),b.getSecondItemToBuy())&&ItemStack.areItemStacksEqual(a.getItemToSell(),b.getItemToSell());}
}
