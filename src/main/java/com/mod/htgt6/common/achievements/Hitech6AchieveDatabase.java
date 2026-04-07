package com.mod.htgt6.common.achievements;

import com.mod.htgt6.common.block.greg.technological.hulls.blockHullDatabase;
import com.mod.htgt6.common.handler.ModBlocks;
import com.mod.htgt6.common.handler.ModItems;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.oredict.OreDictionary;

public class Hitech6AchieveDatabase {
    public static Achievement GetStarted, Flint, FirstStorage, Clay, EVTIER;


    public static AchievementPage HiTech6;

    public static void initialization() {
        GetStarted = new Hitech6Achievments("advancement.HiTech6.Get_Started", "Welcome_to_the_HiTech6", 0, 1, Blocks.dirt, (Achievement) null).registerStat();
        Flint = new Hitech6Achievments("advancement.HiTech6.Flint", "Pick_Up_flint", 1, 2, Items.flint, (Achievement) GetStarted).registerStat();
        FirstStorage = new Hitech6Achievments("advancement.HiTech6.FirstStorage", "First_Storage", 2, 3, Blocks.chest, (Achievement) Flint).registerStat();
        Clay = new Hitech6Achievments("advancement.HiTech6.clay", "Get_Clay", 3, 4, Items.clay_ball, (Achievement) FirstStorage).registerStat();
        EVTIER = new Hitech6Achievments("advancement.HiTech6.EVTIER", "HeyYo_EV_TIER", 20, 20, blockHullDatabase.HullEV,  null).registerStat();

    HiTech6 = new AchievementPage("HiTech6", GetStarted, Flint, FirstStorage, Clay, EVTIER);

    AchievementPage.registerAchievementPage(HiTech6);
	FMLCommonHandler.instance().bus().register((Object)new PickUpAchievments());
    FMLCommonHandler.instance().bus().register((Object)new craftingachieve());
    FMLCommonHandler.instance().bus().register((Object)new onLivingUpdate());
}
}
