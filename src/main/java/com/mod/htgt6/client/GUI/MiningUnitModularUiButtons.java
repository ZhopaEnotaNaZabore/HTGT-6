package com.mod.htgt6.client.GUI;

import codechicken.nei.recipe.GuiCraftingRecipe;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.mod.htgt6.client.nei.StructureLibNEIHandler;
import com.mod.htgt6.common.mining.MiningUnitContent;
import com.mod.htgt6.common.mining.MultiTEMiningUnit;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

/** Opens the Mining Unit's own NEI 3D structure page after ModularUI closes. */
@SideOnly(Side.CLIENT)
public final class MiningUnitModularUiButtons {
    private static final MiningUnitModularUiButtons INSTANCE = new MiningUnitModularUiButtons();
    private static boolean pendingOpen;
    private static boolean registered;
    private static int ticksUntilOpen;
    private static String pendingOverlay;
    private static Object[] pendingArguments;

    private MiningUnitModularUiButtons() { }

    public static ButtonWidget<?> createStructureButton(MultiTEMiningUnit controller) {
        return createStructureButton(new ItemStack(MiningUnitContent.controller, 1, 0),
                "ui.htgt6.mining_unit.nei_structure");
    }

    /** Reusable entry point for every controller registered in the HTGT6 structure viewer. */
    public static ButtonWidget<?> createStructureButton(ItemStack controller, String tooltipKey) {
        final ItemStack target = controller.copy();
        ButtonWidget<?> button = new ButtonWidget();
        button.size(42, 12).background(GuiTextures.BUTTON).overlay(IKey.str("NEI 3D"));
        button.tooltip(t -> t.addLine(StatCollector.translateToLocal(tooltipKey)));
        button.onMouseTapped(mouse -> {
            requestOpen(StructureLibNEIHandler.OVERLAY_ID, target);
            return true;
        });
        return button;
    }

    /** Opens an ordinary NEI recipe category from a ModularUI screen. */
    public static ButtonWidget<?> createRecipeButton(String overlayId, String tooltipKey) {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(42, 22).background(GuiTextures.BUTTON).overlay(IKey.str("NEI"));
        button.tooltip(t -> t.addLine(StatCollector.translateToLocal(tooltipKey)));
        button.onMouseTapped(mouse -> {
            requestOpen(overlayId);
            return true;
        });
        return button;
    }

    private static void requestOpen(String overlay, Object... arguments) {
        if (!registered) {
            FMLCommonHandler.instance().bus().register(INSTANCE);
            registered = true;
        }
        pendingOpen = true;
        pendingOverlay = overlay;
        pendingArguments = arguments == null ? new Object[0] : arguments;
        ticksUntilOpen = 2;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !pendingOpen || ticksUntilOpen-- > 0) return;
        pendingOpen = false;
        String overlay = pendingOverlay;
        Object[] arguments = pendingArguments;
        pendingOverlay = null;
        pendingArguments = null;
        if (overlay != null) GuiCraftingRecipe.openRecipeGui(overlay,
                arguments == null ? new Object[0] : arguments);
    }
}
