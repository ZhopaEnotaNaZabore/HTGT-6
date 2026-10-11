package com.mod.htgt6.common.GTHooks.MaterialHooks;

import gregapi.data.OP;
import gregapi.data.LH;
import gregapi.lang.LanguageHandler;
import net.minecraftforge.oredict.OreDictionary;
import gregapi.oredict.OreDictItemData;
import gregapi.oredict.IOreDictItemDataOverrideItem;
import gregapi.oredict.OreDictMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.creativetab.CreativeTabs;
import cpw.mods.fml.common.registry.GameRegistry;
import java.util.List;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** GT material IDs are the metadata; never use a filtered material-list index here. */
public final class MaterialFrameBoxes extends Item implements IOreDictItemDataOverrideItem {
    public static MaterialFrameBoxes INSTANCE;
    private static final java.util.Set<Integer> available = new java.util.LinkedHashSet<Integer>();

    private MaterialFrameBoxes() {
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(CreativeTabs.tabMaterials);
        setTextureName("htgt6:material_frame_box");
        setUnlocalizedName("htgt6.material_frame_box");
    }

    public static void register() {
        if (INSTANCE == null) {
            INSTANCE = new MaterialFrameBoxes();
            GameRegistry.registerItem(INSTANCE, "material_frame_box", "HTGT6");
        }
    }

    /** Only expose materials with a real rod recipe; metadata always remains the GT material ID. */
    public static void registerMaterial(OreDictMaterial material) {
        if (available.contains((int)material.mID)) return;
        LH.add("oredict.frameGt" + material.mNameInternal + ".name", material.mNameLocal + " Frame Box");
        // Frame recipes are resolved at loadComplete, after GT6's utility closes
        // registration. Forge's event-based API supports late ore registrations.
        OreDictionary.registerOre(OP.frameGt.mNameInternal + material.mNameInternal,
                new ItemStack(INSTANCE, 1, material.mID));
        available.add((int)material.mID);
    }
    private static OreDictMaterial material(ItemStack stack) {
        int id = stack.getItemDamage();
        return id >= 0 && id < OreDictMaterial.MATERIAL_ARRAY.length ? OreDictMaterial.MATERIAL_ARRAY[id] : null;
    }
    @Override public String getUnlocalizedName(ItemStack stack) {
        OreDictMaterial material = material(stack);
        return material == null ? super.getUnlocalizedName(stack) : "oredict.frameGt" + material.mNameInternal;
    }
    @Override public String getItemStackDisplayName(ItemStack stack) {
        OreDictMaterial material = material(stack);
        String key = "item.htgt6.material_frame_box.name";
        String name = material == null ? "?" : material.getLocal();
        return net.minecraft.util.StatCollector.canTranslate(key)
                ? net.minecraft.util.StatCollector.translateToLocalFormatted(key, name)
                : name + " Frame Box";
    }
    @Override public OreDictItemData getOreDictItemData(ItemStack stack) {
        OreDictMaterial material = material(stack);
        return material == null ? null : new OreDictItemData(OP.frameGt, material);
    }
    @Override @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass) {
        OreDictMaterial material = material(stack);
        return material == null ? 0xFFFFFF : gregapi.util.UT.Code.getRGBInt(material.mRGBa[OP.frameGt.mState]);
    }
    @Override @SideOnly(Side.CLIENT) @SuppressWarnings("unchecked")
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int id : available) list.add(new ItemStack(this, 1, id));
    }
}
