package com.mod.htgt6.common.achievements;

import com.mod.htgt6.common.block.greg.technological.hulls.blockHullDatabase;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120;
import com.mod.htgt6.minecraft1202.Module.Data.Aquatic.AquaticStructureData;
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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
    public static Achievement GetStarted, Flint, FirstStorage, Clay, KnifeC, EVTIER, Rock, TierULV, TinAlloy, Iron, TierLV, TierMV, TierHV, TierIV, TierLUV, TierZPM, TierUV, TierPUV, TierMAX, SpaceTraveler, InfinityHorizons, LuckyDay, RealyLuckyDay;
    public static AchievementPage HiTech6;
    public static Achievement TranscendentDisappointment;
    private static Item dirtChestItem;

    public static void initialization() {
        // Fetching multi-tile registry context matching the Loader's logic
        aRegistry = MultiTileEntityRegistry.getRegistry("gt.multitileentity");
        System.out.println("HiTech6Achivements Loaded.");

        // Register achievements using the dynamic registration method
        Iron = registerAchievement("advancement.Hitech6.Iron", 16, 17, OP.ingot.dat(MT.Iron).getStack(1),  "", false);
        TinAlloy =  registerAchievement("advancement.Hitech6.Tin_Ally", 16, 16, OP.ingot.dat(MT.TinAlloy).getStack(1), "", true);
        GetStarted = registerAchievement("advancement.HiTech6.Get_Started", 0, 1, new ItemStack(Blocks.dirt), "", false);
        Flint = registerAchievement("advancement.HiTech6.Flint", 1, 2, new ItemStack(Items.flint), "advancement.HiTech6.Get_Started", false);
        FirstStorage = registerAchievement("advancement.HiTech6.FirstStorage", 2, 3, new ItemStack(Blocks.chest), "advancement.HiTech6.Flint", false);
        Clay = registerAchievement("advancement.HiTech6.clay", 3, 4, new ItemStack(Items.clay_ball), "advancement.HiTech6.FirstStorage", false);
        LuckyDay = registerAchievement("advancement.HiTech6.LuckyDay", 4, 4,
                new ItemStack(Items.filled_map), null, false);
        RealyLuckyDay = registerAchievement("advancement.HiTech6.RealyLuckyDay", 5, 4,
                new ItemStack(Blocks.chest), "advancement.HiTech6.LuckyDay", true);
        //TIER
        TierULV = registerAchievement("advancement.HiTech6.ULVtier", 21, 20, new ItemStack(blockHullDatabase.hullULV), null, true);
        TierLV = registerAchievement("advancement.HiTech6.LVtier", 21, 21, new ItemStack(blockHullDatabase.hullLV), null, true);
        EVTIER = registerAchievement("advancement.HiTech6.EVTIER", 20, 20, new ItemStack(blockHullDatabase.HullEV), null, true);
        TierMV = registerAchievement("advancement.HiTech6.MVtier", 21, 22, new ItemStack(blockHullDatabase.hullMV), null, true);
        TierHV = registerAchievement("advancement.HiTech6.HVtier", 21, 23, new ItemStack(blockHullDatabase.hullHV), null, true);
        TierIV = registerAchievement("advancement.HiTech6.IVtier", 20, 21, new ItemStack(blockHullDatabase.HullIV), null, true);
        TierLUV = registerAchievement("advancement.HiTech6.LUVtier", 20, 22, new ItemStack(blockHullDatabase.HullLUV), null, true);
        TierZPM = registerAchievement("advancement.HiTech6.ZPMtier", 20, 23, new ItemStack(blockHullDatabase.HullZPM), null, true);
        TierUV = registerAchievement("advancement.HiTech6.UVtier", 19, 20, new ItemStack(blockHullDatabase.HullUV), null, true);
        TierPUV = registerAchievement("advancement.HiTech6.PUVtier", 19, 21, new ItemStack(blockHullDatabase.HullPUV), null, true);
        TierMAX = registerAchievement("advancement.HiTech6.MAXtier", 19, 22, new ItemStack(blockHullDatabase.HullMAX), null, true);
        dirtChestItem = getRegistryItem("IronChest:BlockIronChest");
        if (dirtChestItem != null) {
            TranscendentDisappointment = registerAchievement("advancement.HiTech6.TranscendentDisappointment",
                    18, 22, new ItemStack(dirtChestItem, 1, 7), null, true);
        }

        // Resolve optional external items at runtime, keeping this class safe if a dependency is absent.
        Item tier1Rocket = getRegistryItem("GalacticraftCore:item.rocketTier1");
        if (tier1Rocket != null) {
            SpaceTraveler = registerAchievement("advancement.HiTech6.SpaceTraveler", 18, 24, new ItemStack(tier1Rocket), null, true);
        }
        Item tier6Rocket = getRegistryItem("GalaxySpace:item.Tier6Rocket");
        if (tier6Rocket != null) {
            InfinityHorizons = registerAchievement("advancement.HiTech6.InfinityHorizons", 18, 25, new ItemStack(tier6Rocket), "advancement.HiTech6.SpaceTraveler", true);
        }



        // Complex Itemstack formatting preserving original gregapi calls
        ItemStack knifeStack = CS.ToolsGT.sMetaTool.getToolWithStats(34, null, null);
        KnifeC = registerAchievement("advancement.HiTech6.Knife", 22, 22, knifeStack, "advancement.HiTech6.Knife", false);


        ItemStack rockStack = OP.rockGt.mat(MT.Komatiite, 1); OP.rockGt.mat(MT.Quartzite, 1); OP.rockGt.mat(MT.Stone, 1); OP.rockGt.mat(MT.Kimberlite, 1); OP.rockGt.mat(MT.Granite, 1); OP.rockGt.mat(MT.GraniteBlack, 1); OP.rockGt.mat(MT.GraniteRed, 1);

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

    private static Item getRegistryItem(String registryName) {
        Object item = Item.itemRegistry.getObject(registryName);
        return item instanceof Item ? (Item) item : null;
    }

    private static boolean hasRegistryItem(InventoryPlayer inventory, String registryName) {
        Item item = getRegistryItem(registryName);
        if (item == null) return false;
        for (ItemStack stack : inventory.mainInventory) {
            if (stack != null && stack.getItem() == item) return true;
        }
        return false;
    }

    public void issueAchievement(EntityPlayer entityplayer, String textId) {
        Achievement achievement = this.achievementList.get(textId);
        if (entityplayer instanceof EntityPlayerMP && achievement != null
                && !((EntityPlayerMP) entityplayer).func_147099_x().hasAchievementUnlocked(achievement)) {
            entityplayer.triggerAchievement((StatBase) achievement);
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
            if (player.worldObj.isRemote) {
                return;
            }
            InventoryPlayer inv = player.inventory;
            // Also covers taking a machine output and automation delivering it to the player.
            if (dirtChestItem != null && TranscendentDisappointment != null && player.ticksExisted % 20 == 0) {
                for (ItemStack stack : inv.mainInventory) {
                    if (stack != null && stack.getItem() == dirtChestItem && stack.getItemDamage() == 7) {
                        this.issueAchievement(player, "advancement.HiTech6.TranscendentDisappointment");
                        break;
                    }
                }
            }
            if (player == null) {
                return;
            }

            ItemStack held = player.getHeldItem();
            if (VanillaTreasureMap120.isTreasureMap(held)) {
                this.issueAchievement(player, "advancement.HiTech6.LuckyDay");
                checkTreasureMapArrival(player, held);
            }

            //misscelouse
            if (inv.hasItemStack(OP.rockGt.mat(MT.Komatiite, 1))) {
                this.issueAchievement(player, "advancement.HiTech6.Rock");
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


            //tier achivements
            if (inv.hasItemStack(ST.make(blockHullDatabase.HullEV, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.EVTIER");
            }
            if (inv.hasItemStack(ST.make(blockHullDatabase.hullLV, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.LVtier");
            }
            if (inv.hasItemStack(ST.make(blockHullDatabase.hullULV, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.ULVtier");
            }
            if (inv.hasItemStack(ST.make(blockHullDatabase.hullMV, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.MVtier");
            }
            if (inv.hasItemStack(ST.make(blockHullDatabase.hullHV, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.HVtier");
            }
            if (inv.hasItemStack(ST.make(blockHullDatabase.HullIV, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.IVtier");
            }
            if (inv.hasItemStack(ST.make(blockHullDatabase.HullLUV, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.LUVtier");
            }
            if (inv.hasItemStack(ST.make(blockHullDatabase.HullZPM, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.ZPMtier");
            }
            if (inv.hasItemStack(ST.make(blockHullDatabase.HullUV, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.UVtier");
            }
            if (inv.hasItemStack(ST.make(blockHullDatabase.HullPUV, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.PUVtier");
            }
            if (inv.hasItemStack(ST.make(blockHullDatabase.HullMAX, 1L, 0L))) {
                this.issueAchievement(player, "advancement.HiTech6.MAXtier");
            }

            // The two space titles use the same inventory-driven achievement flow as tiers.
            if (hasRegistryItem(inv, "GalacticraftCore:item.rocketTier1")) {
                this.issueAchievement(player, "advancement.HiTech6.SpaceTraveler");
            }
            if (hasRegistryItem(inv, "GalaxySpace:item.Tier6Rocket")) {
                this.issueAchievement(player, "advancement.HiTech6.InfinityHorizons");
            }



            //toolsGT
            if (inv.hasItemStack(this.aRegistry.getItem(34))) {
                this.issueAchievement(player, "advancement.HiTech6.Knife");
            }


            //MT-Get new ingot achivements
            if (inv.hasItemStack(OP.ingot.dat(MT.TinAlloy).getStack(1))) {
                this.issueAchievement(player, "advancement.Hitech6.Tin_Ally");
            }
            if (inv.hasItemStack(OP.ingot.mat(MT.Iron, 1))) {
                this.issueAchievement(player, "advancement.Hitech6.Iron");

        }
}}

    /**
     * Arms the held map outside the destination and awards only on a later
     * entrance into the exact generated buried-treasure zone.
     */
    private void checkTreasureMapArrival(EntityPlayer player, ItemStack map) {
        if (!map.hasTagCompound() || player.dimension != 0) return;
        NBTTagCompound tag = map.getTagCompound();
        if (!AquaticStructureData.BURIED_TREASURE.equals(tag.getString("TargetKind"))) return;

        int targetX = tag.getInteger("TargetX");
        int targetZ = tag.getInteger("TargetZ");
        double dx = player.posX - (targetX + 0.5D);
        double dz = player.posZ - (targetZ + 0.5D);
        double distanceSq = dx * dx + dz * dz;
        if (distanceSq > 12.0D * 12.0D) {
            tag.setBoolean("TreasureApproachArmed", true);
            return;
        }
        if (distanceSq <= 8.0D * 8.0D && tag.getBoolean("TreasureApproachArmed")
                && AquaticStructureData.isGeneratedTarget(player.worldObj,
                AquaticStructureData.BURIED_TREASURE, targetX, targetZ)) {
            tag.setBoolean("TreasureApproachArmed", false);
            this.issueAchievement(player, "advancement.HiTech6.RealyLuckyDay");
        }
    }

}




