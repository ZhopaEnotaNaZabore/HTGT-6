package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemLeatherHorseArmor151;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.item.*;

/** Keep vanilla armor indices intact; a separate synchronized color also records leather presence. */
public final class LeatherHorseHooks151 {
    public static final int WATCHER=23;
    private LeatherHorseHooks151(){}
    public static boolean accepts(Item item){return item instanceof ItemLeatherHorseArmor151;}
    public static boolean ironOrLeather(Item item){return item==net.minecraft.init.Items.iron_horse_armor||accepts(item);}
    public static void initialize(EntityHorse horse){horse.getDataWatcher().addObject(WATCHER,Integer.valueOf(-1));}
    public static void equipped(EntityHorse horse,ItemStack armor){horse.getDataWatcher().updateObject(WATCHER,Integer.valueOf(armor!=null&&accepts(armor.getItem())?ItemLeatherHorseArmor151.color(armor):-1));}
    public static int color(EntityHorse horse){return horse.getDataWatcher().getWatchableObjectInt(WATCHER);}
    public static int protection(int vanilla,EntityHorse horse){return color(horse)>=0?3:vanilla;}
}
