package com.mod.htgt6.common.item.htgt6.coins;


import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class HiTech6Coins extends Item {

    public static Item HiTech6Coins;
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
    public static Item TravelerOfUniverseCoin;
    public static Item HiTech6Coin;
    public static Item ChunkLoadingCoin;
    /**
     * 0 = ExplorersCoin
     * 1 = FarmersCoin
     * 2 = WizardyCoin
     * 4 = BlacksmithCoin
     */

    public static void CoinItemStack() {
        HiTech6Coins = new Item();
        GameRegistry.registerItem(HiTech6Coins, "HiTech6Coins");
        BeeCoin = register(new Item(), "BeeCoin");
        BeeCoin.setTextureName(HTGT6.MOD_ID + ":BeeCoin");
        BeeCoin.setUnlocalizedName("BeeCoin");
        ExplorersCoin = register(new Item(),"explorersCoin");
        ExplorersCoin.setTextureName(HTGT6.MOD_ID+":coinExplorer");
        ExplorersCoin.setUnlocalizedName("explorersCoin");
        BlacksmithCoin = register(new Item(), "BlacksmithCoin");
        BlacksmithCoin.setTextureName(HTGT6.MOD_ID+":coinBlackSmith");
        BlacksmithCoin.setUnlocalizedName("BlacksmithCoin");
        FarmersCoin = register(new Item(), "coinFarmer");
        FarmersCoin.setUnlocalizedName("coinFarmer");
        FarmersCoin.setTextureName(HTGT6.MOD_ID+":coinFarmer");
        SteamPunkCoin = register(new Item(), "SteamPunkCoin");
        SteamPunkCoin.setTextureName(HTGT6.MOD_ID+":coinSteamPunk");
        SteamPunkCoin.setUnlocalizedName("SteamPunkCoin");
        WizardyCoin = register(new Item(), "WizardyCoin");
        WizardyCoin.setUnlocalizedName("WizardyCoin");
        WizardyCoin.setTextureName(HTGT6.MOD_ID+":coinWizardy");
        SlayerCoin = register(new Item(), "SlayerCoin");
        SlayerCoin.setUnlocalizedName("SlayerCoin");
        SlayerCoin.setTextureName(HTGT6.MOD_ID+":coinSlayer");
        HiTech6Coin = register(new Item(), "HiTech6Coin");
        HiTech6Coin.setTextureName(HTGT6.MOD_ID+":GymMCCoin");
        HiTech6Coin.setUnlocalizedName("HiTech6Coin");
    }

    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;

    }}

