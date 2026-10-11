package com.mod.htgt6.client.GUI;

import codechicken.nei.recipe.GuiCraftingRecipe;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.mod.htgt6.common.TE.UniversalEngineTE;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;

@SideOnly(Side.CLIENT)
public final class EngineModularUiButtons {
    private static final EngineModularUiButtons INSTANCE = new EngineModularUiButtons();
    private static UniversalEngineTE pendingEngine;
    private static int ticksUntilOpen;
    private static boolean registered;
    private EngineModularUiButtons() {}

    public static ButtonWidget<?> createNeiButton(UniversalEngineTE engine) {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(34, 12);
        button.background(GuiTextures.BUTTON);
        button.overlay(IKey.str("NEI"));
        button.tooltip(tooltip -> tooltip.addLine("Show universal engine fuels in NEI"));
        button.onMouseTapped(mouse -> { requestOpen(engine); return true; });
        return button;
    }

    private static void requestOpen(UniversalEngineTE engine) {
        if (!registered) { FMLCommonHandler.instance().bus().register(INSTANCE); registered = true; }
        pendingEngine = engine;
        ticksUntilOpen = 2;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || pendingEngine == null || ticksUntilOpen-- > 0) return;
        UniversalEngineTE engine = pendingEngine;
        pendingEngine = null;
        if (!GuiCraftingRecipe.openRecipeGui("universalengine")) {
            GuiCraftingRecipe.openRecipeGui("item", new ItemStack(engine.getBlockType(), 1, engine.getBlockMetadata()));
        }
    }
}
