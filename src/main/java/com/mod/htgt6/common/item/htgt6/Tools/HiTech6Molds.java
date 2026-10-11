package com.mod.htgt6.common.item.htgt6.Tools;



import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.util.List;

public class HiTech6Molds extends Item {

    public static Item emptyMold;

    public static final int META_EMPTY = 0;
    public static final int META_BOLT = 1;
    public static final int META_SCREW = 2;
    public static final int META_ROD = 3;
    public static final int META_LONG_ROD = 4;
    public static final int META_INGOT = 5;
    public static final int META_PLATE = 6;
    public static final int META_BALL = 7;
    public static final int META_ROTOR = 8;
    public static final int META_SMALL_CASING = 9;

    // Subtype definitions
    public static final String[] MOLD_NAMES = new String[] {
            "empty", "bolt", "screw", "rod", "longrod", "ingot", "plate", "ball", "rotor" , "smallcasing"
    };

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public HiTech6Molds() {
        super();
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        // Registry entry is retained only so existing worlds do not lose numeric item IDs.
        // New solidifier recipes use virtual Selector Tags and no longer expose physical molds.
        this.setCreativeTab(null);
    }

    public static void initMolds() {
        // Registration
        emptyMold = register(new HiTech6Molds(), "EmptyMold");

        // Base Unlocalized Name
        emptyMold.setUnlocalizedName("mold");
    }

    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
    }

    public static ItemStack stack(int meta) {
        if (emptyMold == null || meta < 0 || meta >= MOLD_NAMES.length) return null;
        return new ItemStack(emptyMold, 1, meta);
    }

    public static boolean isMold(ItemStack stack) {
        return stack != null && stack.getItem() == emptyMold
                && stack.getItemDamage() >= 0 && stack.getItemDamage() < MOLD_NAMES.length;
    }

    public static String getMoldName(ItemStack stack) {
        return isMold(stack) ? MOLD_NAMES[stack.getItemDamage()] : "";
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack.getItemDamage();
        if (meta < 0 || meta >= MOLD_NAMES.length) {
            meta = 0;
        }
        return super.getUnlocalizedName() + "." + MOLD_NAMES[meta];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        // Compatibility-only item: intentionally hidden from creative inventory and NEI.
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        this.icons = new IIcon[MOLD_NAMES.length];
        for (int i = 0; i < MOLD_NAMES.length; i++) {
            this.icons[i] = iconRegister.registerIcon(HTGT6.MOD_ID + ":molds/mold_" + MOLD_NAMES[i]);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        if (meta < 0 || meta >= icons.length) {
            meta = 0;
        }
        return this.icons[meta];
    }
}
