package com.mod.htgt6.common.item.htgt6.coins;


import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class HiTech6Coins extends Item {

    public static Item BeeCoin;
    public static Item ExplorersCoin;
    public static Item BlacksmithCoin;
    public static Item WizardyCoin;
    public static Item SteamPunkCoin;
    public static Item FarmersCoin;
    public static Item SlayerCoin;
    public static Item SpaceTravelerCoin;
    public static Item TechnologistCoin;
    public static Item ComputerMasterCoin;
    public static Item ChemicalistCoin;
    public static Item DragonForgeCoin;
    public static Item InfinityCoin;
    public static Item HiTech6Coin;
    public static Item ChunkLoadingCoin;
    /**
     * 0 = ExplorersCoin
     * 1 = FarmersCoin
     * 2 = WizardyCoin
     * 4 = BlacksmithCoin
     */

    public static void CoinItemStack() {
        BeeCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "BeeCoin");
        BeeCoin.setTextureName(HTGT6.MOD_ID + ":BeeCoin");
        BeeCoin.setUnlocalizedName("BeeCoin");
        ExplorersCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(),"explorersCoin");
        ExplorersCoin.setTextureName(HTGT6.MOD_ID+":coinExplorer");
        ExplorersCoin.setUnlocalizedName("explorersCoin");
        BlacksmithCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "BlacksmithCoin");
        BlacksmithCoin.setTextureName(HTGT6.MOD_ID+":coinBlackSmith");
        BlacksmithCoin.setUnlocalizedName("BlacksmithCoin");
        FarmersCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "coinFarmer");
        FarmersCoin.setUnlocalizedName("coinFarmer");
        FarmersCoin.setTextureName(HTGT6.MOD_ID+":coinFarmer");
        SteamPunkCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "SteamPunkCoin");
        SteamPunkCoin.setTextureName(HTGT6.MOD_ID+":coinSteamPunk");
        SteamPunkCoin.setUnlocalizedName("SteamPunkCoin");
        WizardyCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "WizardyCoin");
        WizardyCoin.setUnlocalizedName("WizardyCoin");
        WizardyCoin.setTextureName(HTGT6.MOD_ID+":coinWizardy");
        SlayerCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "SlayerCoin");
        SlayerCoin.setUnlocalizedName("SlayerCoin");
        SlayerCoin.setTextureName(HTGT6.MOD_ID+":coinSlayer");
        HiTech6Coin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "HiTech6Coin");
        HiTech6Coin.setTextureName(HTGT6.MOD_ID+":GymMCCoin");
        HiTech6Coin.setUnlocalizedName("HiTech6Coin");
        SpaceTravelerCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "SpaceTravelersCoin");
        SpaceTravelerCoin.setUnlocalizedName("SpaceTravelersCoin");
        SpaceTravelerCoin.setTextureName(HTGT6.MOD_ID+":coinSpaceTraveler");
        ChemicalistCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "coinChemicalist");
        ChemicalistCoin.setTextureName(HTGT6.MOD_ID+":coinChemicalist");
        ChemicalistCoin.setUnlocalizedName("coinChemicalist");
        TechnologistCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "TechCoin");
        TechnologistCoin.setUnlocalizedName("TechCoin");
        TechnologistCoin.setTextureName(HTGT6.MOD_ID+":coinTech");
        ChunkLoadingCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "ChunkloadingCoin");
        ChunkLoadingCoin.setUnlocalizedName("ChunkloadingCoin");
        ChunkLoadingCoin.setTextureName(HTGT6.MOD_ID+":coinChunkload12h");
        ComputerMasterCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "AE2coin");
        ComputerMasterCoin.setUnlocalizedName("AE2coin");
        ComputerMasterCoin.setTextureName(HTGT6.MOD_ID+":AE2coin");
        DragonForgeCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "DEcoin");
        DragonForgeCoin.setUnlocalizedName("DEcoin");
        DragonForgeCoin.setTextureName(HTGT6.MOD_ID+":DEcoin");
        InfinityCoin = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "InfinityCoin");
        InfinityCoin.setTextureName(HTGT6.MOD_ID+":coinInfinity");
        InfinityCoin.setUnlocalizedName("InfinityCoin");

    }

    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;

    }}

