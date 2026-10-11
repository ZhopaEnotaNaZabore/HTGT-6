package com.mod.htgt6.common.GTHooks.UIHooks;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gregapi.oredict.OreDictMaterial;
import net.minecraft.util.EnumChatFormatting;

/** Animates only the displayed formula; it does not alter recipes or material identity. */
public final class AnimatedMaterialFormula {
    private static final String[] COSMOS_FRAMES = {
            EnumChatFormatting.DARK_PURPLE + "<<<" + EnumChatFormatting.LIGHT_PURPLE
                    + "<COSMOS>" + EnumChatFormatting.DARK_PURPLE + ">>>",
            EnumChatFormatting.LIGHT_PURPLE + "<<<" + EnumChatFormatting.AQUA
                    + "<COSMOS>" + EnumChatFormatting.LIGHT_PURPLE + ">>>",
            EnumChatFormatting.AQUA + "<<<" + EnumChatFormatting.DARK_PURPLE
                    + "<COSMOS>" + EnumChatFormatting.AQUA + ">>>"
    };

    private final OreDictMaterial material;
    private int ticks;

    private AnimatedMaterialFormula(OreDictMaterial material) {
        this.material = material;
    }

    public static void register(OreDictMaterial material) {
        if (FMLCommonHandler.instance().getSide().isClient())
            FMLCommonHandler.instance().bus().register(new AnimatedMaterialFormula(material));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && ++ticks % 8 == 0)
            material.mTooltipChemical = COSMOS_FRAMES[(ticks / 8) % COSMOS_FRAMES.length];
    }
}
