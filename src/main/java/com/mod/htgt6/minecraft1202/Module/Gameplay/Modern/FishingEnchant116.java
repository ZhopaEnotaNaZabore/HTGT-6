package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import java.util.*;
import net.minecraft.enchantment.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
/** Loot enchantment selection includes discoverable treasure enchantments. */
public final class FishingEnchant116 {
    public static ItemStack enchant(ItemStack stack,Random random,int level){
        int ability=stack.getItem().getItemEnchantability();if(ability<=0)return stack;
        level+=1+random.nextInt(ability/4+1)+random.nextInt(ability/4+1);
        level=Math.max(1,Math.round(level+level*(random.nextFloat()+random.nextFloat()-1)*.15F));
        boolean book=stack.getItem()==Items.book;List<EnchantmentData> pool=new ArrayList<EnchantmentData>(),chosen=new ArrayList<EnchantmentData>();
        for(Enchantment enchant:Enchantment.enchantmentsList){if(enchant==null||!enchant.isAllowedOnBooks()||!book&&!enchant.canApply(stack))continue;
            for(int rank=enchant.getMaxLevel();rank>=enchant.getMinLevel();rank--)if(level>=enchant.getMinEnchantability(rank)&&level<=enchant.getMaxEnchantability(rank)){pool.add(new EnchantmentData(enchant,rank));break;}}
        if(pool.isEmpty())return stack;chosen.add((EnchantmentData)WeightedRandom.getRandomItem(random,pool));
        while(random.nextInt(50)<=level){Enchantment last=chosen.get(chosen.size()-1).enchantmentobj;
            for(Iterator<EnchantmentData> it=pool.iterator();it.hasNext();){Enchantment next=it.next().enchantmentobj;if(!last.canApplyTogether(next)||!next.canApplyTogether(last))it.remove();}
            if(pool.isEmpty())break;chosen.add((EnchantmentData)WeightedRandom.getRandomItem(random,pool));level/=2;
        }
        if(book)stack=new ItemStack(Items.enchanted_book);
        for(EnchantmentData data:chosen)if(book)Items.enchanted_book.addEnchantment(stack,data);else stack.addEnchantment(data.enchantmentobj,data.enchantmentLevel);
        return stack;
    }
    private FishingEnchant116(){}
}
