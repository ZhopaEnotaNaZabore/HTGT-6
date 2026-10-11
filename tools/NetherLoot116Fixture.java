import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.*;
import com.mod.htgt6.minecraft1202.Module.World.Modern.VillageStructureLoot120;
import net.minecraft.item.*;
import net.minecraft.init.*;
import com.google.gson.*;
import java.util.*;
public final class NetherLoot116Fixture {
 static int checks;static void check(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
 public static void main(String[] args)throws Exception{
  com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
  com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid world=B11605Fixture.initialize();com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.SoulSpeed116.init();NetherVegetationContent116.register();NetherTreasure116.register();
  net.minecraft.block.Block[] blocks={EtFVanillaGapContent120.ancientDebris,EtFVanillaGapContent120.gildedBlackstone,EtFVanillaGapContent120.cryingObsidian,com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.boneBlock,LodestoneContent116.lodestone};
  for(int i=0;i<blocks.length;i++)cpw.mods.fml.common.registry.GameRegistry.registerBlock(blocks[i],"fixture_loot_block_"+i);
  boolean pigstep=false,snout=false,soul=false;
  for(String kind:new String[]{"bridge","hoglin_stable","other","treasure"}){
   String id="minecraft:chests/bastion_"+kind;JsonObject table=VillageStructureLoot120.table(id);
   for(JsonElement pool:table.getAsJsonArray("pools"))for(JsonElement entry:pool.getAsJsonObject().getAsJsonArray("entries"))if(entry.getAsJsonObject().has("name"))check(VillageStructureLoot120.item(entry.getAsJsonObject().get("name").getAsString()).getItem()!=null,"every official entry resolves without external mods");
   for(int seed=0;seed<500;seed++){
    List<ItemStack> loot=VillageStructureLoot120.roll(id,new Random(seed));check(!loot.isEmpty(),"nonempty loot");boolean template=false,lodestone=false;
    for(ItemStack stack:loot){check(stack.getItem()!=null&&stack.stackSize>0&&stack.stackSize<=stack.getMaxStackSize(),"valid stack bounds");
     check(!stack.isItemStackDamageable()||stack.getItemDamage()>=0&&stack.getItemDamage()<=stack.getMaxDamage(),"loot durability bounds");
     pigstep|=stack.getItem()==EtFVanillaGapContent120.musicDiscPigstep;snout|=stack.getItem()==NetherTreasure116.snoutPattern;template|=stack.getItem()==SmithingContent120.upgradeTemplate;lodestone|=stack.getItem()==Item.getItemFromBlock(LodestoneContent116.lodestone);
     soul|=net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.SoulSpeed116.enchantment.effectId,stack)>0;
    }
    if(kind.equals("treasure"))check(template,"treasure guarantees upgrade template for target 1.20.2");if(kind.equals("bridge"))check(lodestone,"bridge guarantees lodestone");
   }
  }
  check(pigstep&&snout&&soul,"rare drops and explicit Soul Speed reachable");
  check("pig".equals(BannerPatterns145.code(NetherTreasure116.snoutPattern)),"own reusable snout loom pattern");
  B11605Fixture.Player player=new B11605Fixture.Player(world);SmithingContent120.prepare();
  Item[] armor={Items.diamond_helmet,Items.diamond_chestplate,Items.diamond_leggings,Items.diamond_boots};
  for(int i=0;i<4;i++){player.inventory.mainInventory[i]=new ItemStack(SmithingContent120.upgrades.get(armor[i]));check(com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.hasNetheriteArmor(player)==(i==3),"netherite achievement requires every distinct armor piece");}
  player.inventory.mainInventory[3]=new ItemStack(SmithingContent120.upgrades.get(Items.diamond_helmet));check(!com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.hasNetheriteArmor(player),"duplicate helmets do not replace boots");
  System.out.println("Nether loot 116: "+checks+" checks passed");
 }
}
