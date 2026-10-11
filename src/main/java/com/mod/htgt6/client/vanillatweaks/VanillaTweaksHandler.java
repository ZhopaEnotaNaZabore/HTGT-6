package com.mod.htgt6.client.vanillatweaks;

import com.mod.htgt6.client.vanillatweaks.WSUI.GuiModernWorldSelect;
import com.mod.htgt6.client.vanillatweaks.WSUI.WorldIconCapture112;
import com.mod.htgt6.client.menu.GuiHTGT6PauseMenu;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

/** Replaces only vanilla's 1.7 world selector with the 1.12-compatible implementation. */
public final class VanillaTweaksHandler {
    private static final Field PARENT = ReflectionHelper.findField(
            GuiSelectWorld.class, "field_146632_a", "a");
    private boolean capturePauseFrame;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiIngameMenu && net.minecraft.client.Minecraft.getMinecraft().getIntegratedServer() != null) {
            WorldIconCapture112.clear();
            capturePauseFrame = true;
        } else if (event.gui instanceof GuiSelectWorld) {
            GuiScreen parent = null;
            try { parent = (GuiScreen)PARENT.get(event.gui); } catch (IllegalAccessException ignored) { }
            event.gui = new GuiModernWorldSelect(parent == null ? new GuiMainMenu() : parent);
        }
    }

    /** DrawScreenEvent.Pre still contains the unobscured last gameplay frame. */
    @SubscribeEvent
    public void beforePauseDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (!capturePauseFrame || !isPauseScreen(event.gui)) return;
        capturePauseFrame = false;
        WorldIconCapture112.capture(net.minecraft.client.Minecraft.getMinecraft());
    }

    /** Vanilla's Save and Quit button is id 1. */
    @SubscribeEvent
    public void beforeSaveAndQuit(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (isPauseScreen(event.gui) && event.button.id == 1) {
            WorldIconCapture112.save(net.minecraft.client.Minecraft.getMinecraft());
        }
    }

    private static boolean isPauseScreen(GuiScreen screen) {
        return screen instanceof GuiIngameMenu || screen instanceof GuiHTGT6PauseMenu;
    }
}
