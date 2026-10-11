package com.mod.htgt6.client.GUI;

import codechicken.nei.recipe.GuiCraftingRecipe;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.mod.htgt6.common.TE.UniversalGasTurbineTE;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.client.Minecraft;

/** Client-only actions used by the ModularUI turbine screen. */
@SideOnly(Side.CLIENT)
public final class TurbineModularUiButtons {

    private static final TurbineModularUiButtons INSTANCE = new TurbineModularUiButtons();
    private static boolean registered;
    private static UniversalGasTurbineTE pendingTurbine;
    private static int ticksUntilOpen;

    private TurbineModularUiButtons() {}

    public static ButtonWidget<?> createNeiButton(final UniversalGasTurbineTE turbine) {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(34, 12);
        button.background(GuiTextures.BUTTON);
        button.overlay(IKey.str("NEI"));
        button.tooltip(tooltip -> tooltip.addLine("Show gas turbine fuels in NEI"));
        button.onMouseTapped(mouseButton -> {
            requestNeiOpen(turbine);
            return true;
        });
        return button;
    }

    /**
     * NEI replaces the current GuiScreen.  It must happen after ModularUI has
     * received the matching mouse-release event, not merely later in the same
     * client task queue.
     */
    private static void requestNeiOpen(UniversalGasTurbineTE turbine) {
        if (!registered) {
            FMLCommonHandler.instance().bus().register(INSTANCE);
            registered = true;
        }
        pendingTurbine = turbine;
        ticksUntilOpen = 2;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || pendingTurbine == null) return;
        if (ticksUntilOpen-- > 0) return;

        UniversalGasTurbineTE turbine = pendingTurbine;
        pendingTurbine = null;
        if (!GuiCraftingRecipe.openRecipeGui("gasturbine")) {
            GuiCraftingRecipe.openRecipeGui("item",
                    new ItemStack(turbine.getBlockType(), 1, turbine.getBlockMetadata()));
        }
    }
}
