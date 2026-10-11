/** Armour priority/cap distribution adapted from Forge ISpecialArmor (Minecraft Forge Public License v1.0). */
package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.util.DamageSource;
import net.minecraft.enchantment.*;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
public final class CombatDamage120 {
 private static final boolean DEBUG=false;
 public static float toughness(EntityLivingBase e){
  double result=e.getEntityAttribute(CombatAttack120.TOUGHNESS)==null?0:e.getEntityAttribute(CombatAttack120.TOUGHNESS).getAttributeValue();
  for(int slot=1;slot<=4;slot++){ItemStack stack=e.getEquipmentInSlot(slot);
   if(stack!=null&&stack.getItem() instanceof ItemArmor){String material=((ItemArmor)stack.getItem()).getArmorMaterial().name();
    if(!stack.getAttributeModifiers().get("generic.armorToughness").isEmpty())continue;
    if("DIAMOND".equals(material))result+=2;else if("NETHERITE".equals(material))result+=3;}}
  return (float)result;
 }
 public static float armorResult(float legacy,EntityLivingBase living,DamageSource source,float damage){
  return source.isUnblockable()?legacy:CombatRules120.damageAfterAbsorb(damage,living.getTotalArmorValue()+(living.getEntityAttribute(CombatAttack120.ARMOR)==null?0:(float)living.getEntityAttribute(CombatAttack120.ARMOR).getAttributeValue()),toughness(living));
 }
 public static float sharpness(int level,int type,EnumCreatureAttribute creature){
  return type==0?(level>0?1+Math.max(0,level-1)*.5F:0):(type==1&&creature==EnumCreatureAttribute.UNDEAD||type==2&&creature==EnumCreatureAttribute.ARTHROPOD?level*2.5F:0);
 }
 public static int protection(ItemStack[] armor,DamageSource source){
  if(source.isDamageAbsolute()||"sonic_boom".equals(source.damageType))return 0;
  int value=0;
  for(ItemStack stack:armor){if(stack==null||stack.getEnchantmentTagList()==null)continue;
   net.minecraft.nbt.NBTTagList tags=stack.getEnchantmentTagList();
   for(int i=0;i<tags.tagCount();i++){int id=tags.getCompoundTagAt(i).getShort("id"),level=tags.getCompoundTagAt(i).getShort("lvl");
    if(id==0)value+=level;
    else if(id==1&&source.isFireDamage())value+=level*2;
    else if(id==2&&source==DamageSource.fall)value+=level*3;
    else if(id==3&&source.isExplosion())value+=level*2;
    else if(id==4&&source.isProjectile())value+=level*2;
    else if(id>4&&id<Enchantment.enchantmentsList.length&&Enchantment.enchantmentsList[id]!=null)value+=Enchantment.enchantmentsList[id].calcModifierDamage(level,source);
   }
  }
  return Math.max(0,Math.min(20,value));
 }
        public static float ApplyArmor(EntityLivingBase entity, ItemStack[] inventory, DamageSource source, double damage)
        {
            return ApplyArmor(entity, inventory, source, damage, true);
        }

        /** Hybrid servers calculate event damage without wearing armour, then apply wear once. */
        public static float ApplyArmor(EntityLivingBase entity, ItemStack[] inventory, DamageSource source, double damage, boolean damageArmor)
        {
            if (DEBUG)
            {
                System.out.println("Start: " + damage + " " + (damage * 25));
            }
            if("sonic_boom".equals(source.damageType))return (float)damage;
            final double incoming=damage;
            int normalArmor=0;
            for(ItemStack stack:inventory)if(stack!=null&&stack.getItem() instanceof ItemArmor&&!(stack.getItem() instanceof ISpecialArmor))normalArmor+=((ItemArmor)stack.getItem()).damageReduceAmount;
            double extraArmor=entity.getEntityAttribute(CombatAttack120.ARMOR)==null?0:entity.getEntityAttribute(CombatAttack120.ARMOR).getAttributeValue();
            double totalArmor=Math.max(0,normalArmor+extraArmor);
            float modern=CombatRules120.damageAfterAbsorb((float)damage,(float)totalArmor,toughness(entity));
            double armorScale=totalArmor==0||damage<=0?0:(1-modern/damage)*25/(normalArmor+Math.max(0,extraArmor));
            damage *= 25;
            ArrayList<ArmorProperties> dmgVals = new ArrayList<ArmorProperties>();
            for (int x = 0; x < inventory.length; x++)
            {
                ItemStack stack = inventory[x];
                if (stack == null)
                {
                    continue;
                }
                ArmorProperties prop = null;
                if (stack.getItem() instanceof ISpecialArmor)
                {
                    ISpecialArmor armor = (ISpecialArmor)stack.getItem();
                    prop = armor.getProperties(entity, stack, source, damage / 25D, x).copy();
                }
                else if (stack.getItem() instanceof ItemArmor && !source.isUnblockable())
                {
                    ItemArmor armor = (ItemArmor)stack.getItem();
                    prop = new ArmorProperties(0, armor.damageReduceAmount * armorScale / 25D, Integer.MAX_VALUE);
                }
                if (prop != null)
                {
                    prop.Slot = x;
                    dmgVals.add(prop);
                }
            }
            if(extraArmor>0&&!source.isUnblockable()){ArmorProperties extra=new ArmorProperties(0,extraArmor*armorScale/25D,Integer.MAX_VALUE);extra.Slot=-1;dmgVals.add(extra);}
            if (dmgVals.size() > 0)
            {
                ArmorProperties[] props = dmgVals.toArray(new ArmorProperties[dmgVals.size()]);
                StandardizeList(props, damage);
                int level = props[0].Priority;
                double ratio = 0;
                for (ArmorProperties prop : props)
                {
                    if (level != prop.Priority)
                    {
                        damage -= (damage * ratio);
                        ratio = 0;
                        level = prop.Priority;
                    }
                    ratio += prop.AbsorbRatio;

                    double absorb = damage * prop.AbsorbRatio;
                    if (damageArmor && absorb > 0 && prop.Slot >= 0)
                    {
                        ItemStack stack = inventory[prop.Slot];
                        int itemDamage = (int)(absorb / 25D < 1 ? 1 : absorb / 25D);
                        if (stack.getItem() instanceof ISpecialArmor)
                        {
                            ((ISpecialArmor)stack.getItem()).damageArmor(entity, stack, source, itemDamage, prop.Slot);
                        }
                        else
                        {
                            if (DEBUG)
                            {
                                System.out.println("Item: " + stack.toString() + " Absorbed: " + (absorb / 25D) + " Damaged: " + itemDamage);
                            }
                            stack.damageItem(Math.max(1,(int)(incoming/4)), entity);
                        }
                        if (stack.stackSize <= 0)
                        {
                            /*if (entity instanceof EntityPlayer)
                            {
                                stack.onItemDestroyedByUse((EntityPlayer)entity);
                            }*/
                            inventory[prop.Slot] = null;
                        }
                    }
                }
                damage -= (damage * ratio);
            }
            if (DEBUG)
            {
                System.out.println("Return: " + (int)(damage / 25.0F) + " " + damage);
            }
            return (float)(damage / 25.0F);
        }

        /**
         * Sorts and standardizes the distribution of damage over armor.
         *
         * @param armor The armor information
         * @param damage The total damage being received
         */
        private static void StandardizeList(ArmorProperties[] armor, double damage)
        {
            Arrays.sort(armor);

            int     start     = 0;
            double  total     = 0;
            int     priority  = armor[0].Priority;
            int     pStart    = 0;
            boolean pChange   = false;
            boolean pFinished = false;

            if (DEBUG)
            {
                for (ArmorProperties prop : armor)
                {
                    System.out.println(prop);
                }
                System.out.println("========================");
            }

            for (int x = 0; x < armor.length; x++)
            {
                total += armor[x].AbsorbRatio;
                if (x == armor.length - 1 || armor[x].Priority != priority)
                {
                    if (armor[x].Priority != priority)
                    {
                        total -= armor[x].AbsorbRatio;
                        x--;
                        pChange = true;
                    }
                    if (total > 1)
                    {
                        for (int y = start; y <= x; y++)
                        {
                            double newRatio = armor[y].AbsorbRatio / total;
                            if (newRatio * damage > armor[y].AbsorbMax)
                            {
                                armor[y].AbsorbRatio = (double)armor[y].AbsorbMax / damage;
                                total = 0;
                                for (int z = pStart; z <= y; z++)
                                {
                                    total += armor[z].AbsorbRatio;
                                }
                                start = y + 1;
                                x = y;
                                break;
                            }
                            else
                            {
                                armor[y].AbsorbRatio = newRatio;
                                pFinished = true;
                            }
                        }
                        if (pChange && pFinished)
                        {
                            damage -= (damage * total);
                            total = 0;
                            start = x + 1;
                            priority = armor[start].Priority;
                            pStart = start;
                            pChange = false;
                            pFinished = false;
                            if (damage <= 0)
                            {
                                for (int y = x + 1; y < armor.length; y++)
                                {
                                    armor[y].AbsorbRatio = 0;
                                }
                                break;
                            }
                        }
                    }
                    else
                    {
                        for (int y = start; y <= x; y++)
                        {
                            total -= armor[y].AbsorbRatio;
                            if (damage * armor[y].AbsorbRatio > armor[y].AbsorbMax)
                            {
                                armor[y].AbsorbRatio = (double)armor[y].AbsorbMax / damage;
                            }
                            total += armor[y].AbsorbRatio;
                        }
                        damage -= (damage * total);
                        total = 0;
                        if (x != armor.length - 1)
                        {
                            start = x + 1;
                            priority = armor[start].Priority;
                            pStart = start;
                            pChange = false;
                            if (damage <= 0)
                            {
                                for (int y = x + 1; y < armor.length; y++)
                                {
                                    armor[y].AbsorbRatio = 0;
                                }
                                break;
                            }
                        }
                    }
                }
            }
            if (DEBUG)
            {
                for (ArmorProperties prop : armor)
                {
                    System.out.println(prop);
                }
            }
        }

}
