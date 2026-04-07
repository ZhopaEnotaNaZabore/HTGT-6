package com.mod.htgt6.common.item.htgt6.govnoebanoe;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;


public class misscellouse extends Item {

    public static Item boronGlassDust;
    public static Item boronGlassPane;
    public static Item itemCarbonHelmet;
    public static Item itemCarbonChestplate;
    public static Item itemCarbonleggins;
    public static Item itemCarbonBoots;

    public static Item itemNanoCrystall;

    public static Item itemDustLapatron;
    public static Item itemRawLapatron;

    public static Item itemQuantumPartHelmet;
    public static Item itemQuantumPartChestplate;
    public static Item itemQuantumPartLeggins;
    public static Item itemQuantumPartBoots;
    public static Item itemQuantumCrystall;

    public static Item itemChipDiamond;
    public static Item itemChipGold;
    public static Item itemChipEmerald;
    public static Item itemChipEnergycrystall;
    public static Item itemChipLapatron;





    public static void GVN() {
        //sykablyat
        boronGlassDust = register(new Item(), "BoronGlassDust1");
        boronGlassDust.setTextureName(HTGT6.MOD_ID+":BoronGlassDust1");
        boronGlassDust.setUnlocalizedName("BoronGlassDust1");
        boronGlassPane = register(new Item(), "BoronGlassPane1");
        boronGlassPane.setTextureName(HTGT6.MOD_ID+":BoronGlassPane1");
        boronGlassPane.setUnlocalizedName("BoronGlassPane1");

        //REGISTER
        itemCarbonHelmet = register(new Item(), "itemCarbonHelmet");
        itemCarbonChestplate = register(new Item(), "itemCarbonChestplate");
        itemCarbonleggins = register(new Item(), "itemCarbonleggins");
        itemCarbonBoots = register(new Item(), "itemCarbonBoots");
        itemNanoCrystall = register(new Item(), "itemNanoCrystall");
        itemDustLapatron = register(new Item(), "itemDustLapatron");
        itemRawLapatron = register(new Item(), "itemRawLapatron");
        itemQuantumPartHelmet = register(new Item(), "itemQuantumPartHelmet");
        itemQuantumPartChestplate = register(new Item(), "itemQuantumPartChestplate");
        itemQuantumPartLeggins = register(new Item(), "itemQuantumPartLeggins");
        itemQuantumPartBoots = register(new Item(), "itemQuantumPartBoots");
        itemQuantumCrystall = register(new Item(), "itemQuantumCrystall");
        itemChipDiamond = register(new Item(), "itemChipDiamond");
        itemChipGold = register(new Item(), "itemChipGold");
        itemChipEmerald = register(new Item(), "itemChipEmerald");
        itemChipEnergycrystall = register(new Item(), "itemChipEnergycrystall");
        itemChipLapatron = register(new Item(), "itemChipLapatron");

        //NAME
        itemCarbonHelmet.setUnlocalizedName("itemCarbonHelmet");
        itemCarbonChestplate.setUnlocalizedName("itemCarbonChestplate");
        itemCarbonleggins.setUnlocalizedName("itemCarbonleggins");
        itemCarbonBoots.setUnlocalizedName("itemCarbonBoots");
        itemNanoCrystall.setUnlocalizedName("itemNanoCrystall");
        itemDustLapatron.setUnlocalizedName("itemDustLapatron");
        itemRawLapatron.setUnlocalizedName("itemRawLapatron");
        itemQuantumPartHelmet.setUnlocalizedName("itemQuantumPartHelmet");
        itemQuantumPartChestplate.setUnlocalizedName("itemQuantumPartChestplate");
        itemQuantumPartLeggins.setUnlocalizedName("itemQuantumPartLeggins");
        itemQuantumPartBoots.setUnlocalizedName("itemQuantumPartBoots");
        itemQuantumCrystall.setUnlocalizedName("itemQuantumCrystall");
        itemChipDiamond.setUnlocalizedName("itemChipDiamond");
        itemChipGold.setUnlocalizedName("itemChipGold");
        itemChipEmerald.setUnlocalizedName("itemChipEmerald");
        itemChipEnergycrystall.setUnlocalizedName("itemChipEnergycrystall");
        itemChipLapatron.setUnlocalizedName("itemChipLapatron");

        //TEXTURE
        itemCarbonHelmet.setTextureName(HTGT6.MOD_ID+":itemCarbonHelmet");
        itemCarbonChestplate.setTextureName(HTGT6.MOD_ID+":itemCarbonChestplate");
        itemCarbonleggins.setTextureName(HTGT6.MOD_ID+":itemCarbonleggins");
        itemCarbonBoots.setTextureName(HTGT6.MOD_ID+":itemCarbonBoots");
        itemNanoCrystall.setTextureName(HTGT6.MOD_ID+":itemNanoCrystall");
        itemDustLapatron.setTextureName(HTGT6.MOD_ID+":itemDustLapatron");
        itemRawLapatron.setTextureName(HTGT6.MOD_ID+":itemRawLapatron");
        itemQuantumPartHelmet.setTextureName(HTGT6.MOD_ID+":itemQuantumPartHelmet");
        itemQuantumPartChestplate.setTextureName(HTGT6.MOD_ID+":itemQuantumPartChestplate");
        itemQuantumPartLeggins.setTextureName(HTGT6.MOD_ID+":itemQuantumPartLeggins");
        itemQuantumPartBoots.setTextureName(HTGT6.MOD_ID+":itemQuantumPartBoots");
        itemQuantumCrystall.setTextureName(HTGT6.MOD_ID+":itemQuantumCrystall");
        itemChipDiamond.setTextureName(HTGT6.MOD_ID+":itemChipDiamond");
        itemChipGold.setTextureName(HTGT6.MOD_ID+":itemChipGold");
        itemChipEmerald.setTextureName(HTGT6.MOD_ID+":itemChipEmerald");
        itemChipEnergycrystall.setTextureName(HTGT6.MOD_ID+":itemChipEnergycrystall");
        itemChipLapatron.setTextureName(HTGT6.MOD_ID+":itemChipLapatron");
    }
    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
    }
}
