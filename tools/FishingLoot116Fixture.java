import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import com.google.gson.*;
import java.util.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.enchantment.*;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraftforge.common.FishingHooks;
public final class FishingLoot116Fixture {
 static int checks;static void check(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
 public static void main(String[] args)throws Exception{
  com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
  Grid w=B11605Fixture.initialize();w.loaded=true;cpw.mods.fml.common.registry.GameRegistry.registerBlock(Backport120Content.bamboo,"fixture_fishing_bamboo");
  B11605Fixture.Player player=new B11605Fixture.Player(w);player.inventory.mainInventory[0]=new ItemStack(Items.fishing_rod);player.getHeldItem().addEnchantment(Enchantment.field_151370_z,2);
  check(OpenWaterFishing116.luck(player)==2,"enchantment contributes fishing luck");player.addPotionEffect(new net.minecraft.potion.PotionEffect(CombatContent.luck.id,200,2));check(OpenWaterFishing116.luck(player)==5,"Luck III adds three");player.addPotionEffect(new net.minecraft.potion.PotionEffect(CombatContent.unluck.id,200,1));check(OpenWaterFishing116.luck(player)==3,"Bad Luck II subtracts two");
  check(ExplorationContent112.vanishingCurse!=null,"curse initialized");SoulSpeed116.init();
  final WeightedRandomFishable removed=(WeightedRandomFishable)EntityFishHook.field_146039_d.get(2);FishingHooks.removeJunk(e->e!=removed);
  FishingHooks.addFish(new WeightedRandomFishable(new ItemStack(Items.diamond),100000));FishingLoot116.register();FishingLoot116.register();
  EntityFishHook hook=new EntityFishHook(w);hook.field_146042_b=player;hook.setPosition(.5,80,.5);w.biome=net.minecraft.world.biome.BiomeGenBase.plains;
  boolean lily=false,shell=false,mending=false,curse=false,addon=false;
  for(int i=0;i<4000;i++){
   ItemStack junk=FishingLoot116.roll(hook,0);check(junk!=null&&junk.getItem()!=Items.bone&&junk.getItem()!=Item.getItemFromBlock(Backport120Content.bamboo),"removed Forge entry and nonjungle bamboo stay absent");lily|=junk.getItem()==Item.getItemFromBlock(Blocks.waterlily);
   ItemStack treasure=FishingLoot116.roll(hook,1);check(treasure!=null&&treasure.getItem()!=Item.getItemFromBlock(Blocks.waterlily),"lily is junk, not treasure");shell|=treasure.getItem()==com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.nautilusShell;
   Map<Integer,Integer> enchantments=EnchantmentHelper.getEnchantments(treasure);mending|=enchantments.containsKey(CombatContent.mending.effectId);curse|=enchantments.containsKey(ExplorationContent112.vanishingCurse.effectId);check(!enchantments.containsKey(SoulSpeed116.enchantment.effectId),"Soul Speed cannot be fished");
   for(Integer a:enchantments.keySet())for(Integer b:enchantments.keySet())if(!a.equals(b))check(Enchantment.enchantmentsList[a].canApplyTogether(Enchantment.enchantmentsList[b]),"loot enchantments compatible");
   addon|=FishingLoot116.roll(hook,2).getItem()==Items.diamond;
  }
  check(lily&&shell&&mending&&curse&&addon,"modern catches, treasure enchantments and Forge additions reachable");
  w.biome=net.minecraft.world.biome.BiomeGenBase.jungle;net.minecraftforge.common.BiomeDictionary.registerBiomeType(w.biome,net.minecraftforge.common.BiomeDictionary.Type.JUNGLE);boolean bamboo=false;for(int i=0;i<1000;i++)bamboo|=FishingLoot116.roll(hook,0).getItem()==Item.getItemFromBlock(Backport120Content.bamboo);check(bamboo,"jungle bamboo catch reachable");
  for(String category:new String[]{"junk","treasure","fish"})for(JsonElement raw:FishingLoot116.table(category).getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries")){
   JsonObject entry=raw.getAsJsonObject();FishingLoot116.Entry fishable=new FishingLoot116.Entry(entry);check(fishable.itemWeight==(entry.has("weight")?entry.get("weight").getAsInt():1),"official item weight");for(int seed=0;seed<20;seed++){ItemStack stack=fishable.func_150708_a(new Random(seed));check(stack.getItem()!=null&&stack.stackSize>0&&(!stack.isItemStackDamageable()||stack.getItemDamage()<=stack.getMaxDamage()),"all official entries resolve and have valid durability");}}
  System.out.println("Fishing loot 116: "+checks+" checks passed");
 }
}
