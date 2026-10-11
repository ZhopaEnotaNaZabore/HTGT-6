package com.mod.htgt6.client.GUI;

import codechicken.nei.recipe.GuiCraftingRecipe;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.mod.htgt6.common.TE.RadioisotopeGeneratorTE;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;

/** Client-only deferred NEI action for the ModularUI RTG screen. */
@SideOnly(Side.CLIENT)
public final class RadioisotopeModularUiButtons {
    private static final RadioisotopeModularUiButtons INSTANCE = new RadioisotopeModularUiButtons();
    private static RadioisotopeGeneratorTE pendingGenerator;
    private static int ticksUntilOpen;
    private static boolean registered;

    private RadioisotopeModularUiButtons() {}

    public static ButtonWidget<?> createNeiButton(RadioisotopeGeneratorTE generator) {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(34, 12).background(GuiTextures.BUTTON).overlay(IKey.str("NEI"));
        button.tooltip(t -> t.addLine("Show isotope decay recipes in NEI"));
        button.onMouseTapped(mouse -> { requestOpen(generator); return true; });
        return button;
    }

    private static void requestOpen(RadioisotopeGeneratorTE generator) {
        if (!registered) {
            FMLCommonHandler.instance().bus().register(INSTANCE);
            registered = true;
        }
        pendingGenerator = generator;
        ticksUntilOpen = 2;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || pendingGenerator == null || ticksUntilOpen-- > 0) return;
        RadioisotopeGeneratorTE generator = pendingGenerator;
        pendingGenerator = null;
        if (!GuiCraftingRecipe.openRecipeGui("isotope_decay")) {
            GuiCraftingRecipe.openRecipeGui("item",
                    new ItemStack(generator.getBlockType(), 1, generator.getBlockMetadata()));
        }
    }
}
