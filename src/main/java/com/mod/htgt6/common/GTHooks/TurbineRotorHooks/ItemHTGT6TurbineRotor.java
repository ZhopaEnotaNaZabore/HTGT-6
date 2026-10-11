package com.mod.htgt6.common.GTHooks.TurbineRotorHooks;

import com.mod.htgt6.common.handler.ModTab;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.data.OP;
import gregapi.oredict.OreDictMaterial;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import java.util.List;
import java.util.Locale;

/** Material-bearing, damageable turbine rotor owned entirely by HTGT6. */
public final class ItemHTGT6TurbineRotor extends Item {
    private static final String NBT_MATERIAL = "HTGT6.RotorMaterial";
    private static final String NBT_MATERIAL_ID = "HTGT6.RotorMaterialId";
    private static final String NBT_DAMAGE = "HTGT6.RotorDamage";
    private final IIcon[] baseIcons = new IIcon[TurbineRotorSize.values().length];
    private final IIcon[] overlayIcons = new IIcon[TurbineRotorSize.values().length];

    public ItemHTGT6TurbineRotor() {
        setUnlocalizedName("htgt6TurbineRotor");
        setHasSubtypes(true);
        setMaxStackSize(1);
        setMaxDamage(0);
        setCreativeTab(ModTab.INSTANCE);
    }

    public ItemStack create(TurbineRotorSize size, OreDictMaterial material) {
        if (size == null || !isUsableMaterial(material)) return null;
        ItemStack stack = new ItemStack(this, 1, size.ordinal());
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(NBT_MATERIAL, material.mNameInternal);
        tag.setShort(NBT_MATERIAL_ID, material.mID);
        tag.setLong(NBT_DAMAGE, 0L);
        stack.setTagCompound(tag);
        return stack;
    }

    public boolean isRotor(ItemStack stack) {
        return stack != null && stack.getItem() == this && getMaterial(stack) != null;
    }

    public TurbineRotorSize getSize(ItemStack stack) {
        return TurbineRotorSize.fromMetadata(stack == null ? 0 : stack.getItemDamage());
    }

    public OreDictMaterial getMaterial(ItemStack stack) {
        if (stack == null || stack.getItem() != this || !stack.hasTagCompound()) return null;
        NBTTagCompound tag = stack.getTagCompound();
        String name = tag.getString(NBT_MATERIAL);
        OreDictMaterial material = name.isEmpty() ? null : OreDictMaterial.MATERIAL_MAP.get(name);
        if (material != null) return material;
        int id = tag.getShort(NBT_MATERIAL_ID);
        return id > 0 && id < OreDictMaterial.MATERIAL_ARRAY.length
                ? OreDictMaterial.MATERIAL_ARRAY[id] : null;
    }

    public TurbineRotorStats getStats(ItemStack stack) {
        OreDictMaterial material = getMaterial(stack);
        return material == null ? null : TurbineRotorStats.of(getSize(stack), material);
    }

    public long getRotorDamage(ItemStack stack) {
        return stack != null && stack.hasTagCompound()
                ? Math.max(0L, stack.getTagCompound().getLong(NBT_DAMAGE)) : 0L;
    }

    /** @return true when the damage consumed the rotor. */
    public boolean damageRotor(ItemStack stack, long amount) {
        TurbineRotorStats stats = getStats(stack);
        if (stats == null || amount <= 0L) return false;
        long oldDamage = getRotorDamage(stack);
        long damage = amount > Long.MAX_VALUE - oldDamage ? Long.MAX_VALUE : oldDamage + amount;
        stack.getTagCompound().setLong(NBT_DAMAGE, damage);
        if (damage < stats.maxDurability) return false;
        stack.stackSize = Math.max(0, stack.stackSize - 1);
        return true;
    }

    public long getRemainingDurability(ItemStack stack) {
        TurbineRotorStats stats = getStats(stack);
        return stats == null ? 0L : Math.max(0L, stats.maxDurability - getRotorDamage(stack));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return isRotor(stack) && getRotorDamage(stack) > 0L;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        TurbineRotorStats stats = getStats(stack);
        return stats == null ? 0.0D : Math.min(1.0D, getRotorDamage(stack) / (double) stats.maxDurability);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        OreDictMaterial material = getMaterial(stack);
        String materialName = material == null ? StatCollector.translateToLocal("item.htgt6.rotor.unknown")
                : material.getLocal();
        String sizeName = StatCollector.translateToLocal(
                "item.htgt6.turbine_rotor.size." + getSize(stack).key);
        return StatCollector.translateToLocalFormatted("item.htgt6.turbine_rotor.name", materialName, sizeName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        TurbineRotorStats stats = getStats(stack);
        if (stats == null) {
            list.add(EnumChatFormatting.RED + StatCollector.translateToLocal("item.htgt6.rotor.invalid"));
            return;
        }
        list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(
                "item.htgt6.rotor.durability", getRemainingDurability(stack), stats.maxDurability));
        list.add(EnumChatFormatting.AQUA + StatCollector.translateToLocalFormatted(
                "item.htgt6.rotor.efficiency", stats.baseEfficiency * 100.0D));
        list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(
                "item.htgt6.rotor.flow.steam", stats.optimalFlow(TurbineRotorStats.Fuel.STEAM)));
        list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(
                "item.htgt6.rotor.flow.gas", stats.optimalFlow(TurbineRotorStats.Fuel.GAS)));
        list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(
                "item.htgt6.rotor.flow.plasma", stats.optimalFlow(TurbineRotorStats.Fuel.PLASMA)));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (OreDictMaterial material : OreDictMaterial.MATERIAL_ARRAY) {
            if (!isUsableMaterial(material) || !hasGT6RotorPart(material)) continue;
            for (TurbineRotorSize size : TurbineRotorSize.values()) list.add(create(size, material));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        for (TurbineRotorSize size : TurbineRotorSize.values()) {
            String path = "htgt6:gt6hooks/turbine_rotors/turbine_" + size.key;
            baseIcons[size.ordinal()] = register.registerIcon(path);
            overlayIcons[size.ordinal()] = register.registerIcon(path + "_overlay");
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderPasses(int metadata) {
        return 2;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        int index = getSize(stack).ordinal();
        return pass == 0 ? baseIcons[index] : overlayIcons[index];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int metadata) {
        return baseIcons[TurbineRotorSize.fromMetadata(metadata).ordinal()];
    }

    @Override
    public int getColorFromItemStack(ItemStack stack, int pass) {
        if (pass != 0) return 0xFFFFFF;
        OreDictMaterial material = getMaterial(stack);
        if (material == null || material.mRGBaSolid == null || material.mRGBaSolid.length < 3) return 0xFFFFFF;
        return (channel(material.mRGBaSolid[0]) << 16)
                | (channel(material.mRGBaSolid[1]) << 8)
                | channel(material.mRGBaSolid[2]);
    }

    private static int channel(short value) {
        return Math.max(0, Math.min(255, value));
    }

    static boolean isUsableMaterial(OreDictMaterial material) {
        return material != null && !material.mHidden && material.mToolDurability > 0L && material.mToolSpeed > 0.0F;
    }

    static boolean hasGT6RotorPart(OreDictMaterial material) {
        ItemStack part = OP.rotor.mat(material, 1);
        return part != null && part.getItem() != null;
    }
}
