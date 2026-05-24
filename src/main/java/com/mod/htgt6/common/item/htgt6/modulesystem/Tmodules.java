package com.mod.htgt6.common.item.htgt6.modulesystem;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class Tmodules extends Item {

    // ==========================================
    // TRANSFORMER MODULES
    // ==========================================
    public static Item LowLevelTransformer;
    public static Item MediumLevelTransformer;
    public static Item HighLevelTransformer;
    public static Item UltimateLevelTransformer;
    public static Item OmegaTransformer;

    // ==========================================
    // TIER MODULES
    // ==========================================
    public static Item TierModuleMV;
    public static Item TierModuleHV;
    public static Item TierModuleEV;
    public static Item TierModuleIV;
    public static Item TierModuleLuV;
    public static Item TierModuleZPM;
    public static Item TierModuleUV;
    public static Item TierModulePUV1;
    public static Item TierModuleUX;
    public static Item TierModuleOLV;
    public static Item TierModuleOMV;
    public static Item TierModuleOHV;
    public static Item TierModuleOEV;
    public static Item TierModuleOIV;
    public static Item TierModuleMAX;

    // ==========================================
    // DYNAMIC TRANSFORMER ITEM
    // ==========================================
    public static class ItemTransformer extends Item {
        private final String supportedTiers;

        public ItemTransformer(String supportedTiers) {
            this.supportedTiers = supportedTiers;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean advanced) {
            list.add("§l§aВставьте модуль в Е слот");
            list.add("§l§aЧтобы Использовать модуль модификатор тира");
            list.add("§l§aПозволяет открыть доступ к " + supportedTiers + " тирам");
        }
    }

    // ==========================================
    // DYNAMIC TIER ITEM MODULE
    // ==========================================
    public static class ItemModule extends Item {
        private final String tierName;
        private final String euText;
        private final String transformerNeeded;
        private final boolean isDirectName;

        public ItemModule(String tierName, long baseEuValue, int tierPower) {
            this.tierName = tierName;

            // Calculates: baseEuValue * 4^tierPower
            long calculatedEu = baseEuValue * (long) Math.pow(4, tierPower);
            this.euText = calculatedEu + "EU/t";

            // Determine transformer requirement based on rules
            if (tierPower <= 2) {
                // MV, HV, EV
                this.transformerNeeded = "среднего уровня";
                this.isDirectName = false;
            } else if (tierPower <= 6) {
                // IV, LuV, ZPM, UV
                this.transformerNeeded = "высокого уровня";
                this.isDirectName = false;
            } else if (tierPower <= 13) {
                // PUV1, UX, OLV, OMV, OHV, OEV, OIV
                this.transformerNeeded = "Ультимативный трансформатор";
                this.isDirectName = true;
            } else {
                // MAX
                this.transformerNeeded = "Омега трансформатор";
                this.isDirectName = true;
            }
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean advanced) {
            list.add("§l§aПрокачивает машину до " + tierName + " тира, ");
            list.add("§l§aПотребление энергии §6" + euText + ",");
            list.add("§l§aБуст скорости Х2 за каждый тир рецепта ниже машины. ");
            list.add("§l§aПозволяет запускать рецепты " + tierName + " тира. ");

            if (isDirectName) {
                list.add("§l§aНужен §c" + transformerNeeded);
            } else {
                list.add("§l§aНужен модуль трансформатора §c" + transformerNeeded);
            }

            list.add("§l§aДля запуска машины в " + tierName + " тире.");
            list.add("§l§aПоложите модули в специальные слоты.");
            list.add("§l§aСлоты модификатора со стрелочкой вверх.");
        }
    }

    // ==========================================
    // INIT
    // ==========================================
    public static void ModifiersAndTransformers() {

        // --- TRANSFORMERS (With unique dynamic descriptions) ---
        // Note: Keeping low transformer initialization just in case your registration depends on it.
        LowLevelTransformer = register(new ItemTransformer(""), "LowLevelTransformer");
        LowLevelTransformer.setUnlocalizedName("LowLevelTransformer");
        LowLevelTransformer.setTextureName(HTGT6.MOD_ID + ":LowLevelTransformer");

        // Среднего уровня allows MV, HV, EV
        MediumLevelTransformer = register(new ItemTransformer("MV, HV, EV"), "MediumLevelTransformer");
        MediumLevelTransformer.setUnlocalizedName("MediumLevelTransformer");
        MediumLevelTransformer.setTextureName(HTGT6.MOD_ID + ":MediumLevelTransformer");

        // Высокого уровня allows IV, LuV, ZPM, UV
        HighLevelTransformer = register(new ItemTransformer("IV, LuV, ZPM, UV"), "HighLevelTransformer");
        HighLevelTransformer.setUnlocalizedName("HighLevelTransformer");
        HighLevelTransformer.setTextureName(HTGT6.MOD_ID + ":HighLevelTransformer");

        // Ультимативный allows PUV1, UX, OLV, OMV, OHV, OEV, OIV
        UltimateLevelTransformer = register(new ItemTransformer("PUV1, UX, OLV, OMV, OHV, OEV, OIV"), "UltimateLevelTransformer");
        UltimateLevelTransformer.setUnlocalizedName("UltimateLevelTransformer");
        UltimateLevelTransformer.setTextureName(HTGT6.MOD_ID + ":UltimateLevelTransformer");

        // Омега allows MAX only
        OmegaTransformer = register(new ItemTransformer("MAX"), "OmegaTransformer");
        OmegaTransformer.setUnlocalizedName("OmegaTransformer");
        OmegaTransformer.setTextureName(HTGT6.MOD_ID + ":OmegaTransformer");


        // --- TIER MODULES ---
        // Средний уровень (0-2) -> MV, HV, EV
        TierModuleMV = register(new ItemModule("MV", 128, 0), "TierModuleMV");
        TierModuleMV.setUnlocalizedName("TierModuleMV");
        TierModuleMV.setTextureName(HTGT6.MOD_ID + ":TierModuleMV");

        TierModuleHV = register(new ItemModule("HV", 128, 1), "TierModuleHV");
        TierModuleHV.setUnlocalizedName("TierModuleHV");
        TierModuleHV.setTextureName(HTGT6.MOD_ID + ":TierModuleHV");

        TierModuleEV = register(new ItemModule("EV", 128, 2), "TierModuleEV");
        TierModuleEV.setUnlocalizedName("TierModuleEV");
        TierModuleEV.setTextureName(HTGT6.MOD_ID + ":TierModuleEV");

        // Высокий уровень (3-6) -> IV, LuV, ZPM, UV
        TierModuleIV = register(new ItemModule("IV", 128, 3), "TierModuleIV");
        TierModuleIV.setUnlocalizedName("TierModuleIV");
        TierModuleIV.setTextureName(HTGT6.MOD_ID + ":TierModuleIV");

        TierModuleLuV = register(new ItemModule("LuV", 128, 4), "TierModuleLuV");
        TierModuleLuV.setUnlocalizedName("TierModuleLuV");
        TierModuleLuV.setTextureName(HTGT6.MOD_ID + ":TierModuleLuV");

        TierModuleZPM = register(new ItemModule("ZPM", 128, 5), "TierModuleZPM");
        TierModuleZPM.setUnlocalizedName("TierModuleZPM");
        TierModuleZPM.setTextureName(HTGT6.MOD_ID + ":TierModuleZPM");

        TierModuleUV = register(new ItemModule("UV", 128, 6), "TierModuleUV");
        TierModuleUV.setUnlocalizedName("TierModuleUV");
        TierModuleUV.setTextureName(HTGT6.MOD_ID + ":TierModuleUV");

        // Ультимативный трансформатор (7-13) -> PUV1, UX, OLV, OMV, OHV, OEV, OIV
        TierModulePUV1 = register(new ItemModule("PUV1", 128, 7), "TierModulePUV1");
        TierModulePUV1.setUnlocalizedName("TierModulePUV1");
        TierModulePUV1.setTextureName(HTGT6.MOD_ID + ":TierModulePUV1");

        TierModuleUX = register(new ItemModule("UX", 128, 8), "TierModuleUX");
        TierModuleUX.setUnlocalizedName("TierModuleUX");
        TierModuleUX.setTextureName(HTGT6.MOD_ID + ":TierModuleUX");

        TierModuleOLV = register(new ItemModule("OLV", 128, 9), "TierModuleOLV");
        TierModuleOLV.setUnlocalizedName("TierModuleOLV");
        TierModuleOLV.setTextureName(HTGT6.MOD_ID + ":TierModuleOLV");

        TierModuleOMV = register(new ItemModule("OMV", 128, 10), "TierModuleOMV");
        TierModuleOMV.setUnlocalizedName("TierModuleOMV");
        TierModuleOMV.setTextureName(HTGT6.MOD_ID + ":TierModuleOMV");

        TierModuleOHV = register(new ItemModule("OHV", 128, 11), "TierModuleOHV");
        TierModuleOHV.setUnlocalizedName("TierModuleOHV");
        TierModuleOHV.setTextureName(HTGT6.MOD_ID + ":TierModuleOHV");

        TierModuleOEV = register(new ItemModule("OEV", 128, 12), "TierModuleOEV");
        TierModuleOEV.setUnlocalizedName("TierModuleOEV");
        TierModuleOEV.setTextureName(HTGT6.MOD_ID + ":TierModuleOEV");

        TierModuleOIV = register(new ItemModule("OIV", 128, 13), "TierModuleOIV");
        TierModuleOIV.setUnlocalizedName("TierModuleOIV");
        TierModuleOIV.setTextureName(HTGT6.MOD_ID + ":TierModuleOIV");

        // Омега трансформатор (14) -> MAX
        TierModuleMAX = register(new ItemModule("MAX", 128, 14), "TierModuleMAX");
        TierModuleMAX.setUnlocalizedName("TierModuleMAX");
        TierModuleMAX.setTextureName(HTGT6.MOD_ID + ":TierModuleMAX");
    }

    // ==========================================
    // REGISTER
    // ==========================================
    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
    }
}