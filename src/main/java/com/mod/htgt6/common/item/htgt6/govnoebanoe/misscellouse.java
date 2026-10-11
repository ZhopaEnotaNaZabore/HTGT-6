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
        boronGlassDust = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "BoronGlassDust1");
        boronGlassDust.setTextureName(HTGT6.MOD_ID+":BoronGlassDust1");
        boronGlassDust.setUnlocalizedName("BoronGlassDust1");
        boronGlassPane = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "BoronGlassPane1");
        boronGlassPane.setTextureName(HTGT6.MOD_ID+":BoronGlassPane1");
        boronGlassPane.setUnlocalizedName("BoronGlassPane1");

        //REGISTER
        itemCarbonHelmet = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemCarbonHelmet");
        itemCarbonChestplate = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemCarbonChestplate");
        itemCarbonleggins = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemCarbonleggins");
        itemCarbonBoots = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemCarbonBoots");
        itemNanoCrystall = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemNanoCrystall");
        itemDustLapatron = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemDustLapatron");
        itemRawLapatron = register(new Item(), "itemRawLapatron");
        itemQuantumPartHelmet = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemQuantumPartHelmet");
        itemQuantumPartChestplate = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemQuantumPartChestplate");
        itemQuantumPartLeggins = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemQuantumPartLeggins");
        itemQuantumPartBoots = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemQuantumPartBoots");
        itemQuantumCrystall = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemQuantumCrystall");
        itemChipDiamond = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemChipDiamond");
        itemChipGold = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemChipGold");
        itemChipEmerald = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemChipEmerald");
        itemChipEnergycrystall = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemChipEnergycrystall");
        itemChipLapatron = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemChipLapatron");

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
