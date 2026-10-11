package com.mod.htgt6.common.item.htgt6.fluids;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class dustsDatabase {

    // Original Fluids HTGT6 Dusts
    public static Item DustOsIrRhRu;
    public static Item dustPdKeNiCu;
    public static Item dustPtCl;
    public static Item dustPdNH;
    public static Item dustRhSo4;
    public static Item dustOsIr;
    public static Item dustIrO;
    public static Item dustPGMAlkalineResidue;
    public static Item dustKeNiCu;
    public static Item dustNiCu;
    public static Item dustHeavyBauxideSludge;
    public static Item dustBauxideSludge;
    public static Item dustTitaniumMettalicSludge;

    // Extended Planets Dusts
    public static Item dustDomonce;
    public static Item dustHelema;
    public static Item dustMunce;
    public static Item dustPlemtis;
    public static Item dustRukan;
    public static Item VelaADust;

    // GalaxySpace Dusts
    public static Item dustCallisto;
    public static Item dustCeres;
    public static Item dustEnc;
    public static Item dustEurope;
    public static Item DustGanym;
    public static Item DustIO;
    public static Item dustMercury;
    public static Item dustVenus;
    public static Item PlutoDust;

    /**
     * Call this method from your main Mod Class or CommonProxy during Pre-Initialization
     */
    public static void InitFluidSubItems() {
        // --- Original Fluids HTGT6 Dusts ---
        DustOsIrRhRu = registerColoredItem("DustOsIrRhRu", 0x4F4F4F);
        dustPdKeNiCu = registerColoredItem("dustPdKeNiCu", 0x708090);
        dustPtCl = registerColoredItem("dustPtCl", 0xDCDCDC);
        dustPdNH = registerColoredItem("dustPdNH", 0xE6E6FA);
        dustRhSo4 = registerColoredItem("dustRhSo4", 0xBC8F8F);
        dustOsIr = registerColoredItem("dustOsIr", 0x778899);
        dustIrO = registerColoredItem("dustIrO", 0x2F4F4F);
        dustPGMAlkalineResidue = registerColoredItem("dustPGMAlkalineResidue", 0x67634F);
        dustKeNiCu = registerColoredItem("dustKeNiCu", 0x4682B4);
        dustNiCu = registerColoredItem("dustNiCu", 0xB8860B);
        dustHeavyBauxideSludge = registerColoredItem("dustHeavyBauxideSludge", 0x8B4513);
        dustBauxideSludge = registerColoredItem("BauxideSludge", 0xCD853F);
        dustTitaniumMettalicSludge = registerColoredItem("TitaniumMettalicSludge", 0xC0C0C0);

        // --- Extended Planets Dusts ---
        dustDomonce = registerColoredItem("dustDomonce", 0x5C4033);              // Dark Brown
        dustHelema = registerColoredItem("dustHelema", 0xBDB76B);                // Dark Khaki
        dustMunce = registerColoredItem("dustMunce", 0x4B0082);                  // Indigo Accent
        dustPlemtis = registerColoredItem("dustPlemtis", 0x2E8B57);              // Sea Green
        dustRukan = registerColoredItem("dustRukan", 0x800000);                  // Maroon
        VelaADust = registerColoredItem("VelaADust", 0xDAA520);                  // Goldenrod

        // --- GalaxySpace Dusts ---
        dustCallisto = registerColoredItem("dustCallisto", 0x696969);            // Dim Gray
        dustCeres = registerColoredItem("dustCeres", 0xA9A9A9);                  // Dark Gray
        dustEnc = registerColoredItem("dustEnc", 0xF0F8FF);                      // Ice White (Enceladus)
        dustEurope = registerColoredItem("dustEurope", 0xFFFAF0);                // Floral White (Europa)
        DustGanym = registerColoredItem("DustGanym", 0x8FBC8F);                  // Dark Sea Green
        DustIO = registerColoredItem("DustIO", 0xEEDD82);                        // Sulfur Yellow (Io)
        dustMercury = registerColoredItem("dustMercury", 0x808080);              // Mercury Gray
        dustVenus = registerColoredItem("dustVenus", 0xDEB887);                  // Burly Wood Orange
        PlutoDust = registerColoredItem("PlutoDust", 0x1A1A1A);                  // Off-Black Ice
    }

    /**
     * Helper method to instantiate, name, texture, and color-code items dynamically.
     */
    private static Item registerColoredItem(String name, final int rgbColor) {
        Item coloredItem = new Item() {
            @Override
            @SideOnly(Side.CLIENT)
            public int getColorFromItemStack(ItemStack stack, int pass) {
                return rgbColor;
            }
        };

        coloredItem.setUnlocalizedName(name);
        coloredItem.setTextureName(HTGT6.MOD_ID + ":dustIndex");
        GameRegistry.registerItem(coloredItem, name);

        return coloredItem;
    }
}
