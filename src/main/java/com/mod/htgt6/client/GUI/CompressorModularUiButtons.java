package com.mod.htgt6.client.GUI;

import codechicken.nei.recipe.GuiCraftingRecipe;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.mod.htgt6.common.TE.TESuperMasicCompressor;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;

/** Opens compressor recipes after ModularUI has closed its click transaction. */
@SideOnly(Side.CLIENT)
public final class CompressorModularUiButtons {
    private static final CompressorModularUiButtons INSTANCE = new CompressorModularUiButtons();
    private static TESuperMasicCompressor pendingCompressor;
    private static int ticksUntilOpen;
    private static boolean registered;

    private CompressorModularUiButtons() {}

    public static ButtonWidget<?> createNeiButton(TESuperMasicCompressor compressor) {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(34, 12).background(GuiTextures.BUTTON).overlay(IKey.str("NEI"));
        button.tooltip(t -> t.addLine("Show compressor recipes in NEI"));
        button.onMouseTapped(mouse -> { requestOpen(compressor); return true; });
        return button;
    }

    private static void requestOpen(TESuperMasicCompressor compressor) {
        if (!registered) {
            FMLCommonHandler.instance().bus().register(INSTANCE);
            registered = true;
        }
        pendingCompressor = compressor;
        ticksUntilOpen = 2;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || pendingCompressor == null || ticksUntilOpen-- > 0) return;
        TESuperMasicCompressor compressor = pendingCompressor;
        pendingCompressor = null;
        if (!GuiCraftingRecipe.openRecipeGui("compressing_machine")) {
            GuiCraftingRecipe.openRecipeGui("item",
                    new ItemStack(compressor.getBlockType(), 1, compressor.getBlockMetadata()));
        }
    }
}
