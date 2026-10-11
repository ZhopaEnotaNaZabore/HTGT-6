package com.mod.htgt6.client.GUI;

import codechicken.nei.recipe.GuiCraftingRecipe;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.mod.htgt6.common.TE.TileEntityAssembler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;

/** Opens assembler recipes after ModularUI has closed its click transaction. */
@SideOnly(Side.CLIENT)
public final class AssemblerModularUiButtons {
    private static final AssemblerModularUiButtons INSTANCE = new AssemblerModularUiButtons();
    private static TileEntityAssembler pendingAssembler;
    private static int ticksUntilOpen;
    private static boolean registered;

    private AssemblerModularUiButtons() {}

    public static ButtonWidget<?> createNeiButton(TileEntityAssembler assembler) {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(34, 12).background(GuiTextures.BUTTON).overlay(IKey.str("NEI"));
        button.tooltip(t -> t.addLine("Show assembler recipes in NEI"));
        button.onMouseTapped(mouse -> { requestOpen(assembler); return true; });
        return button;
    }

    private static void requestOpen(TileEntityAssembler assembler) {
        if (!registered) {
            FMLCommonHandler.instance().bus().register(INSTANCE);
            registered = true;
        }
        pendingAssembler = assembler;
        ticksUntilOpen = 2;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || pendingAssembler == null || ticksUntilOpen-- > 0) return;
        TileEntityAssembler assembler = pendingAssembler;
        pendingAssembler = null;
        if (!GuiCraftingRecipe.openRecipeGui("assembling_machine")) {
            GuiCraftingRecipe.openRecipeGui("item",
                    new ItemStack(assembler.getBlockType(), 1, assembler.getBlockMetadata()));
        }
    }
}
