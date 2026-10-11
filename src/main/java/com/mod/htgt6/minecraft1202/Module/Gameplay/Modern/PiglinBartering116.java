package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

import java.util.Random;
import net.minecraft.init.*;
import net.minecraft.item.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;

/** Java 1.16.2+ barter pool. One gold ingot buys one weighted roll, never emerald trades. */
public final class PiglinBartering116 {
    public static final int[] WEIGHTS={5,8,8,8,10,10,10,20,20,40,40,40,40,40,40,40,40,40};
    public static final int TOTAL_WEIGHT=459;
    public static ItemStack roll(Random random){return result(index(random.nextInt(TOTAL_WEIGHT)),random);}
    public static int index(int roll){if(roll<0||roll>=TOTAL_WEIGHT)throw new IllegalArgumentException("barter roll");for(int i=0;i<WEIGHTS.length;i++){roll-=WEIGHTS[i];if(roll<0)return i;}throw new AssertionError();}
    public static ItemStack result(int entry,Random r){
        switch(entry){
            case 0:return SoulSpeed116.book(1+r.nextInt(3));
            case 1:ItemStack boots=new ItemStack(Items.iron_boots);SoulSpeed116.init();boots.addEnchantment(SoulSpeed116.enchantment,1+r.nextInt(3));return boots;
            case 2:return new ItemStack(Items.potionitem,1,8195);
            case 3:return new ItemStack(Items.potionitem,1,16387);
            case 4:return new ItemStack(Items.potionitem,1,0);
            case 5:return stack(Backport120Content.ironNugget,r,10,36);
            case 6:return stack(Items.ender_pearl,r,2,4);
            case 7:return stack(Items.string,r,3,9);
            case 8:return stack(Items.quartz,r,5,12);
            case 9:return new ItemStack(Blocks.obsidian);
            case 10:return new ItemStack(EtFVanillaGapContent120.cryingObsidian,1+r.nextInt(3));
            case 11:return new ItemStack(Items.fire_charge);
            case 12:return stack(Items.leather,r,2,4);
            case 13:return new ItemStack(Blocks.soul_sand,2+r.nextInt(7));
            case 14:return stack(Items.netherbrick,r,2,8);
            case 15:return stack(CombatContent.spectralArrow,r,6,12);
            case 16:return new ItemStack(Blocks.gravel,8+r.nextInt(9));
            case 17:return new ItemStack(EtFVanillaGapContent120.blackstone,8+r.nextInt(9));
            default:throw new IllegalArgumentException("barter entry");
        }
    }
    private static ItemStack stack(Item item,Random r,int min,int max){return new ItemStack(item,min+r.nextInt(max-min+1));}
    private PiglinBartering116(){}
}
