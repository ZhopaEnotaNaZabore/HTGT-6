package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Bountiful.BountifulBannerColors;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

/** Canonical 1.20.2 BlockEntityTag representation shared by shield logic and rendering. */
public final class ShieldBannerData120 {
    public static final int MAX_PATTERNS = 6;

    public static boolean hasDecoration(ItemStack stack) {
        NBTTagCompound block = blockEntityTag(stack);
        return block != null && block.hasKey("Base", 3);
    }

    public static int baseColor(ItemStack stack) {
        NBTTagCompound block = blockEntityTag(stack);
        return block == null ? 0 : block.getInteger("Base") & 15;
    }

    public static int baseRgb(ItemStack stack) {
        return BountifulBannerColors.RGB[baseColor(stack)];
    }

    public static NBTTagList patterns(ItemStack stack) {
        NBTTagCompound block = blockEntityTag(stack);
        return block != null && block.hasKey("Patterns", 9)
                ? block.getTagList("Patterns", 10) : new NBTTagList();
    }

    public static void copyBannerDecoration(ItemStack shield, ItemStack banner) {
        if (shield == null || banner == null) return;
        NBTTagCompound root = shield.hasTagCompound()
                ? (NBTTagCompound) shield.getTagCompound().copy() : new NBTTagCompound();
        NBTTagCompound source = blockEntityTag(banner);
        NBTTagCompound block = source == null ? new NBTTagCompound()
                : (NBTTagCompound) source.copy();
        block.setInteger("Base", source != null && source.hasKey("Base", 3)
                ? source.getInteger("Base") & 15 : banner.getItemDamage() & 15);
        trimPatterns(block);
        root.setTag("BlockEntityTag", block);
        shield.setTagCompound(root);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void appendTooltip(ItemStack stack, EntityPlayer player, List lines) {
        NBTTagList list = patterns(stack);
        for (int i = 0; i < list.tagCount() && i < MAX_PATTERNS; ++i) {
            NBTTagCompound pattern = list.getCompoundTagAt(i);
            String code = pattern.getString("Pattern");
            String key = "loom.pattern." + code;
            String translated = StatCollector.translateToLocal(key);
            lines.add(EnumChatFormatting.GRAY + (key.equals(translated) ? code : translated));
        }
    }

    public static NBTTagCompound blockEntityTag(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()
                || !stack.getTagCompound().hasKey("BlockEntityTag", 10)) return null;
        return stack.getTagCompound().getCompoundTag("BlockEntityTag");
    }

    private static void trimPatterns(NBTTagCompound block) {
        if (!block.hasKey("Patterns", 9)) return;
        NBTTagList source = block.getTagList("Patterns", 10);
        if (source.tagCount() <= MAX_PATTERNS) return;
        NBTTagList trimmed = new NBTTagList();
        for (int i = 0; i < MAX_PATTERNS; ++i)
            trimmed.appendTag(source.getCompoundTagAt(i).copy());
        block.setTag("Patterns", trimmed);
    }

    private ShieldBannerData120() { }
}
