package com.mod.htgt6.minecraft1202.Module.World.Combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.EnchantmentCombat;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;

/** The 1.20.2 End City main pool; the later Spire trim pool belongs to the trim module. */
public final class EndCityLoot120 {
    public static final int MAIN_WEIGHT=85;
    private static final int[] WEIGHTS={5,10,15,2,5,3,1,1,1,3,3,3,3,3,3,3,3,3,3,3,3,3,3};
    private static final int[] MIN={2,4,2,2,1},MAX={7,8,7,6,10};
    private static Item[] items(){return new Item[]{Items.diamond,Items.iron_ingot,Items.gold_ingot,Items.emerald,CombatContent.beetrootSeeds,
        Items.saddle,Items.iron_horse_armor,Items.golden_horse_armor,Items.diamond_horse_armor,
        Items.diamond_sword,Items.diamond_boots,Items.diamond_chestplate,Items.diamond_leggings,Items.diamond_helmet,Items.diamond_pickaxe,Items.diamond_shovel,
        Items.iron_sword,Items.iron_boots,Items.iron_chestplate,Items.iron_leggings,Items.iron_helmet,Items.iron_pickaxe,Items.iron_shovel};}
    public static ItemStack roll(Random random){
        int choice=random.nextInt(MAIN_WEIGHT),index=0;
        while(choice>=WEIGHTS[index])choice-=WEIGHTS[index++];
        ItemStack stack=new ItemStack(items()[index],index<5?MIN[index]+random.nextInt(MAX[index]-MIN[index]+1):1);
        return index<9?stack:enchant(random,stack,20+random.nextInt(20));
    }
    public static void fill(IInventory inventory,Random random){
        List<Integer> free=new ArrayList<Integer>();
        for(int i=0;i<inventory.getSizeInventory();i++)if(inventory.getStackInSlot(i)==null)free.add(i);
        List<ItemStack> loot=new ArrayList<ItemStack>();
        for(int i=0,count=2+random.nextInt(5);i<count;i++)loot.add(roll(random));
        // Keep stack splitting separate from pool rolls, and never overwrite occupied slots.
        List<ItemStack> splittable=new ArrayList<ItemStack>();
        for(java.util.Iterator<ItemStack> it=loot.iterator();it.hasNext();){ItemStack stack=it.next();if(stack.stackSize>1){splittable.add(stack);it.remove();}}
        while(free.size()-loot.size()-splittable.size()>0&&!splittable.isEmpty()){
            ItemStack stack=splittable.remove(random.nextInt(splittable.size()));
            ItemStack split=stack.splitStack(1+random.nextInt(stack.stackSize/2));
            if(stack.stackSize>1&&random.nextBoolean())splittable.add(stack);else loot.add(stack);
            if(split.stackSize>1&&random.nextBoolean())splittable.add(split);else loot.add(split);
        }
        loot.addAll(splittable);java.util.Collections.shuffle(loot,random);java.util.Collections.shuffle(free,random);
        for(ItemStack stack:loot){if(free.isEmpty())break;inventory.setInventorySlotContents(free.remove(free.size()-1),stack);}
        inventory.markDirty();
    }
    public static ItemStack enchant(Random random,ItemStack stack,int level){
        int ability=stack.getItem().getItemEnchantability();if(ability<=0)return stack;
        level+=1+random.nextInt(ability/4+1)+random.nextInt(ability/4+1);
        level=Math.max(1,Math.round(level*(1+(random.nextFloat()+random.nextFloat()-1)*.15F)));
        List<EnchantmentData> candidates=new ArrayList<EnchantmentData>();
        for(Enchantment enchantment:Enchantment.enchantmentsList){
            if(enchantment==null)continue;
            boolean treasure=enchantment instanceof EnchantmentCombat&&((EnchantmentCombat)enchantment).isTreasure();
            if(!(treasure?enchantment.canApply(stack):enchantment.canApplyAtEnchantingTable(stack)))continue;
            for(int rank=enchantment.getMaxLevel();rank>=enchantment.getMinLevel();rank--)
                if(level>=enchantment.getMinEnchantability(rank)&&level<=enchantment.getMaxEnchantability(rank)){candidates.add(new EnchantmentData(enchantment,rank));break;}
        }
        if(candidates.isEmpty())return stack;
        EnchantmentData selected=(EnchantmentData)WeightedRandom.getRandomItem(random,candidates);stack.addEnchantment(selected.enchantmentobj,selected.enchantmentLevel);
        while(random.nextInt(50)<=level){
            for(java.util.Iterator<EnchantmentData> it=candidates.iterator();it.hasNext();){Enchantment other=it.next().enchantmentobj;
                if(!selected.enchantmentobj.canApplyTogether(other)||!other.canApplyTogether(selected.enchantmentobj))it.remove();}
            if(candidates.isEmpty())break;
            selected=(EnchantmentData)WeightedRandom.getRandomItem(random,candidates);stack.addEnchantment(selected.enchantmentobj,selected.enchantmentLevel);level/=2;
        }
        return stack;
    }
    private EndCityLoot120(){ }
}
