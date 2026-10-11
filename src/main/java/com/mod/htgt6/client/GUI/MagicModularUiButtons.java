package com.mod.htgt6.client.GUI;

import codechicken.nei.recipe.GuiCraftingRecipe;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.mod.htgt6.common.TE.MagicEnergyGeneratorTE;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;

/** Opens NEI after ModularUI has finished handling the click. */
@SideOnly(Side.CLIENT)
public final class MagicModularUiButtons {
    private static final MagicModularUiButtons INSTANCE = new MagicModularUiButtons();
    private static MagicEnergyGeneratorTE pendingGenerator;
    private static int ticksUntilOpen;
    private static boolean registered;
    private MagicModularUiButtons() {}
    public static ButtonWidget<?> createNeiButton(MagicEnergyGeneratorTE generator) {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(34, 12).background(GuiTextures.BUTTON).overlay(IKey.str("NEI"));
        button.tooltip(t -> t.addLine("Show magic fuels in NEI"));
        button.onMouseTapped(mouse -> { requestOpen(generator); return true; });
        return button;
    }
    private static void requestOpen(MagicEnergyGeneratorTE generator) {
        if (!registered) { FMLCommonHandler.instance().bus().register(INSTANCE); registered = true; }
        pendingGenerator = generator; ticksUntilOpen = 2;
    }
    @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || pendingGenerator == null || ticksUntilOpen-- > 0) return;
        MagicEnergyGeneratorTE generator = pendingGenerator; pendingGenerator = null;
        if (!GuiCraftingRecipe.openRecipeGui("magicenergygen")) GuiCraftingRecipe.openRecipeGui("item", new ItemStack(generator.getBlockType(), 1, generator.getBlockMetadata()));
    }
}
