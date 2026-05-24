package com.mod.htgt6.common.achievements;

import com.mod.htgt6.common.block.greg.technological.hulls.blockHullDatabase;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.data.CS;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.util.ST;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import java.util.concurrent.ConcurrentHashMap;
public class HiTecch6AchivementsNEW {
    // Loader-based Registry Holders
    public static MultiTileEntityRegistry aRegistry;
    public static ConcurrentHashMap<String, Achievement> achievementList = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Boolean> issueAchievement = new ConcurrentHashMap<>();

    // Grid alignment logic offsets
    public static int adjX = 0;
    public static int adjY = 0;

    // Classic Achievements (References are safely kept intact)
    public static Achievement GetStarted, Flint, FirstStorage, Clay, KnifeC, EVTIER, Rock, TierULV, TinAlloy;
    public static AchievementPage HiTech6;

    public static void initialization() {
        // Fetching multi-tile registry context matching the Loader's logic
        aRegistry = MultiTileEntityRegistry.getRegistry("gt.multitileentity");
        System.out.println("HiTech6Achivements Loaded.");

        // Register achievements using the dynamic registration method
        TinAlloy =  registerAchievement("advancement.Hitech6.Tin_Ally", 16, 16, OP.ingot.dat(MT.TinAlloy).getStack(1), "", false);
        GetStarted = registerAchievement("advancement.HiTech6.Get_Started", 0, 1, new ItemStack(Blocks.dirt), "", false);
        Flint = registerAchievement("advancement.HiTech6.Flint", 1, 2, new ItemStack(Items.flint), "advancement.HiTech6.Get_Started", false);
        FirstStorage = registerAchievement("advancement.HiTech6.FirstStorage", 2, 3, new ItemStack(Blocks.chest), "advancement.HiTech6.Flint", false);
        Clay = registerAchievement("advancement.HiTech6.clay", 3, 4, new ItemStack(Items.clay_ball), "advancement.HiTech6.FirstStorage", false);
        EVTIER = registerAchievement("advancement.HiTech6.EVTIER", 20, 20, new ItemStack(blockHullDatabase.HullEV), null, true);

        // Complex Itemstack formatting preserving original gregapi calls
        ItemStack knifeStack = CS.ToolsGT.sMetaTool.getToolWithStats(34, null, null);
        KnifeC = registerAchievement("advancement.HiTech6.Knife", 22, 22, knifeStack, "advancement.HiTech6.Knife", false);

        TierULV = registerAchievement("advancement.HiTech6.ULVtier", 21, 20, new ItemStack(blockHullDatabase.hullULV), null, false);

        ItemStack rockStack = MT.Stone.mDictionaryBook;
        Rock = registerAchievement("advancement.HiTech6.Rock", 21, 21, rockStack, "advancement.HiTech6.Rock", false);

        // Map collection directly to page array dynamically following Loader logic
        HiTech6 = new AchievementPage("HiTech6", achievementList.values().toArray(new Achievement[0]));
        AchievementPage.registerAchievementPage(HiTech6);

        // Self-register instances handler buses (replaces old separate classes with unified logic)
        HiTecch6AchivementsNEW instance = new HiTecch6AchivementsNEW();
        MinecraftForge.EVENT_BUS.register(instance);
        FMLCommonHandler.instance().bus().register(instance);
    }

    /**
     * Replicates the exact instantiation and sorting matrix functionality of the bytecode Loader class
     */
    public static Achievement registerAchievement(String textId, int x, int y, ItemStack icon, String requirement, boolean special) {
        Achievement req = (requirement == null) ? null : getAchievement(requirement);

        // Instantiate using standard 1.7.10 Achievement constructor args mapped inside the Loader bytecode
        Achievement achievement = new Achievement(textId, textId, x + adjX, y + adjY, icon, req);

        if (special) {
            achievement.setSpecial();
        }

        achievement.registerStat();
        achievementList.put(textId, achievement);
        return achievement;
    }

    public static Achievement getAchievement(String textId) {
        return achievementList.get(textId);
    }

    public void issueAchievement(EntityPlayer entityplayer, String textId) {
        if (entityplayer != null) {
            entityplayer.triggerAchievement((StatBase)this.achievementList.get(textId));

    }
    }





    @SubscribeEvent
    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        EntityPlayer player = event.player;
        ItemStack stack = event.crafting;
        if (player == null || stack == null) {
            ;
        }
    }

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack stack = event.item.getEntityItem();
        if (player == null || stack == null) {
            ;
        }
    }
    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entityLiving;
            InventoryPlayer inv = player.inventory;
            if (player == null) {
                return;
            }

            if (inv.hasItemStack(ST.make(Items.flint, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.Flint");
            }
            if (inv.hasItemStack(ST.make(Blocks.dirt, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.Get_Started");
            }
            if (inv.hasItemStack(ST.make(Blocks.chest, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.FirstStorage");
            }
            if (inv.hasItemStack(ST.make(Items.clay_ball, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.clay");
            }
            if (inv.hasItemStack(ST.make(blockHullDatabase.HullEV, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.EVTIER");
            }
            if (inv.hasItemStack(this.aRegistry.getItem(34))) {
                this.issueAchievement(player, "advancement.HiTech6.Knife");
            }
            if (inv.hasItemStack(ST.make(blockHullDatabase.hullULV, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.ULVtier");
            }
            if (inv.hasItemStack(OP.ingot.dat(MT.TinAlloy).getStack(1))) {
                this.issueAchievement(player, "advancement.Hitech6.Tin_Ally");
            }



        }
}
}



