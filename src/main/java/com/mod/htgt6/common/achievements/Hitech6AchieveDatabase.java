package com.mod.htgt6.common.achievements;

import com.mod.htgt6.common.handler.ModItems;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

public class Hitech6AchieveDatabase {
    public static Achievement GetStarted, ULV_Coils;


    public static AchievementPage HiTech6;

    public static void initialization() {
        GetStarted = new Hitech6Achievments("advancement.HiTech6.Get_Started", "Welcome_to_the_HiTech6", 0, 1, Items.flint, (Achievement) null).registerStat();
        ULV_Coils = new Hitech6Achievments("advancement.HiTech6.ULV_Coils", "Get_ULV_COIL", 1, 2, ModItems.UL_VCOIL, (Achievement) GetStarted);

    HiTech6 = new AchievementPage("HiTech6", GetStarted, ULV_Coils);

    AchievementPage.registerAchievementPage(HiTech6);
	FMLCommonHandler.instance().bus().register((Object)new PickUpAchievments());
}
}
