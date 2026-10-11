package com.mod.htgt6.common.handler.recipes.modpackgenerated.warn;

import java.util.ArrayList;
import java.util.List;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedValueResolver;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;

/** Client tooltip replacement for MineTweaker addTooltip calls. */
public final class GeneratedTooltipRegistry {

    private static final GeneratedTooltipRegistry INSTANCE = new GeneratedTooltipRegistry();
    private final List<Entry> entries = new ArrayList<Entry>();
    private boolean registered;

    private GeneratedTooltipRegistry() {}

    public static void add(String source, String target, String color, String text,
            GeneratedRecipeSettings settings, Logger logger) {
        if (!FMLCommonHandler.instance().getSide().isClient()) return;
        for (ItemStack stack : GeneratedValueResolver.matchingItems(target, source, settings, logger)) {
            INSTANCE.entries.add(new Entry(stack, color(color) + text));
        }
        if (!INSTANCE.registered) {
            INSTANCE.registered = true;
            MinecraftForge.EVENT_BUS.register(INSTANCE);
        }
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (event.itemStack == null) return;
        for (Entry entry : entries) {
            if (!OreDictionary.itemMatches(entry.target, event.itemStack, false)) continue;
            NBTTagCompound expected = entry.target.getTagCompound();
            if (expected != null && !expected.equals(event.itemStack.getTagCompound())) continue;
            event.toolTip.add(entry.text);
        }
    }

    private static String color(String name) {
        if ("red".equalsIgnoreCase(name)) return EnumChatFormatting.RED.toString();
        if ("green".equalsIgnoreCase(name)) return EnumChatFormatting.GREEN.toString();
        if ("yellow".equalsIgnoreCase(name)) return EnumChatFormatting.YELLOW.toString();
        if ("gold".equalsIgnoreCase(name)) return EnumChatFormatting.GOLD.toString();
        if ("gray".equalsIgnoreCase(name) || "grey".equalsIgnoreCase(name)) {
            return EnumChatFormatting.GRAY.toString();
        }
        if ("aqua".equalsIgnoreCase(name)) return EnumChatFormatting.AQUA.toString();
        return "";
    }

    private static final class Entry {
        private final ItemStack target;
        private final String text;

        private Entry(ItemStack target, String text) {
            this.target = target;
            this.text = text;
        }
    }
}
