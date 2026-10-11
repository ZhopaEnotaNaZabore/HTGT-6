package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import net.minecraft.item.*;
import net.minecraft.enchantment.Enchantment;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
public final class PiglinEquipment116 {
 public static int slot(ItemStack stack){return stack.getItem() instanceof ItemArmor?4-((ItemArmor)stack.getItem()).armorType:0;}
 private static boolean bound(ItemStack stack){if(stack==null||stack.getEnchantmentTagList()==null)return false;for(int i=0;i<stack.getEnchantmentTagList().tagCount();i++){int id=stack.getEnchantmentTagList().getCompoundTagAt(i).getShort("id");if(id>=0&&id<Enchantment.enchantmentsList.length&&Enchantment.enchantmentsList[id]!=null&&Enchantment.enchantmentsList[id].getName().contains("binding"))return true;}return false;}
 private static float damage(ItemStack stack){float modern=com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.AttackCooldown112.getVanillaAttackDamage(stack);if(!Float.isNaN(modern))return modern;float result=1;for(Object o:stack.getAttributeModifiers().get("generic.attackDamage")){net.minecraft.entity.ai.attributes.AttributeModifier modifier=(net.minecraft.entity.ai.attributes.AttributeModifier)o;if(modifier.getOperation()==0)result+=modifier.getAmount();}return result;}
 public static boolean canEquip(EntityPiglin116 piglin,ItemStack next){
  if(next==null||next.stackSize<=0)return false;ItemStack old=piglin.getEquipmentInSlot(slot(next));if(old==null)return true;if(bound(old))return false;
  boolean preferred=PiglinInventory116.loved(next)||next.getItem() instanceof ItemCrossbow,wasPreferred=PiglinInventory116.loved(old)||old.getItem() instanceof ItemCrossbow;
  if(preferred!=wasPreferred)return preferred;
  if(!piglin.isChild()&&old.getItem() instanceof ItemCrossbow&&!(next.getItem() instanceof ItemCrossbow))return false;
  Item a=next.getItem(),b=old.getItem();
  if(a instanceof ItemArmor){if(!(b instanceof ItemArmor))return true;int defense=((ItemArmor)a).damageReduceAmount-((ItemArmor)b).damageReduceAmount;if(defense!=0)return defense>0;}
  else if(a instanceof ItemSword){if(!(b instanceof ItemSword))return true;float diff=damage(next)-damage(old);if(diff!=0)return diff>0;}
  else if(a instanceof ItemTool){if(!(b instanceof ItemTool))return !(b instanceof ItemSword);float diff=damage(next)-damage(old);if(diff!=0)return diff>0;}
  else if(!(a instanceof ItemBow&&b instanceof ItemBow)&&!(a instanceof ItemCrossbow&&b instanceof ItemCrossbow))return false;
  return next.getItemDamage()<old.getItemDamage()||next.isItemEnchanted()&&!old.isItemEnchanted();
 }
 public static boolean equip(EntityPiglin116 piglin,ItemStack stack){if(!canEquip(piglin,stack))return false;piglin.equipPickedItem(slot(stack),stack);return true;}
 private PiglinEquipment116(){}
}
