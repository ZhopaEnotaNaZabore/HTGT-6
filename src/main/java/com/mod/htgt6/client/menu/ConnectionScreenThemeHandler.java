package com.mod.htgt6.client.menu;

import com.mod.htgt6.common.ui.MenuTheme;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.input.Mouse;

/** Replaces drawing only: vanilla/Forge continue to own connection, cancel and return actions. */
public final class ConnectionScreenThemeHandler extends Gui {
    private final Map<GuiScreen, State> screens = Collections.synchronizedMap(new WeakHashMap<GuiScreen, State>());
    private static Field reason, title, manager;
    private static boolean fieldsReady, fieldsFailed;

    public static boolean supports(Class<?> type) {
        // Exact matches deliberately exclude startup/loading screens and mod-specific subclasses.
        return type == GuiConnecting.class || type == GuiDisconnected.class || type == GuiDownloadTerrain.class;
    }

    private static boolean resolveFields() {
        if (fieldsReady) return true;
        if (fieldsFailed) return false;
        try {
            reason = ReflectionHelper.findField(GuiDisconnected.class, "field_146304_f");
            title = ReflectionHelper.findField(GuiDisconnected.class, "field_146306_a");
            manager = ReflectionHelper.findField(GuiConnecting.class, "field_146371_g");
            fieldsReady = true;
        } catch (RuntimeException e) {
            fieldsFailed = true;
            LogManager.getLogger("HTGT6-Menu").warn("Connection theme unavailable; retaining vanilla screens", e);
        }
        return fieldsReady;
    }

    @SubscribeEvent public void onInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!supports(event.gui.getClass()) || !resolveFields()) return;
        try {
            State state = new State();
            state.disconnected = event.gui.getClass() == GuiDisconnected.class;
            if (state.disconnected) {
                state.title = (String) title.get(event.gui);
                IChatComponent message = (IChatComponent) reason.get(event.gui);
                state.message = message == null ? "" : message.getFormattedText();
            }
            // Keep original button objects, ids and handlers. Only move their visual bounds.
            state.buttons = event.buttonList;
            layout(event.gui, state);
            screens.put(event.gui, state);
        } catch (IllegalAccessException e) {
            LogManager.getLogger("HTGT6-Menu").warn("Cannot read disconnect reason; retaining vanilla screen", e);
        }
    }

    private static void layout(GuiScreen screen, State state) {
        state.width = Math.min(480, screen.width - 32);
        state.left = (screen.width - state.width) / 2;
        state.top = Math.max(16, (screen.height - 210) / 2);
        state.bottom = Math.min(screen.height - 16, state.top + 210);
        state.textTop = state.top + 56;
        state.textBottom = state.bottom - (state.buttons.isEmpty() ? 20 : 50);
        state.visible = Math.max(1, (state.textBottom - state.textTop) / 12);
        state.lines = Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(state.message, state.width - 40);
        int y = state.bottom - 34;
        for (Object entry : state.buttons) {
            GuiButton button = (GuiButton) entry;
            button.width = Math.min(240, state.width - 32);
            button.height = 20;
            button.xPosition = (screen.width - button.width) / 2;
            button.yPosition = y;
            y -= 24;
        }
    }

    @SubscribeEvent public void onDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
        State state = screens.get(event.gui);
        if (state == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        String heading;
        try {
            heading = state.disconnected ? state.title : I18n.format(event.gui.getClass() == GuiDownloadTerrain.class
                    ? "multiplayer.downloadingTerrain" : manager.get(event.gui) == null ? "connect.connecting" : "connect.authorizing");
        } catch (IllegalAccessException e) { return; }
        MenuAppearance.background(event.gui.width, event.gui.height);
        drawRect(state.left, state.top, state.left + state.width, state.bottom, MenuAppearance.panel());
        int edge = MenuTheme.color(0xFF656B89, 0xFF77736D);
        drawRect(state.left, state.top, state.left + state.width, state.top + 1, edge);
        drawRect(state.left, state.bottom - 1, state.left + state.width, state.bottom, edge);
        drawRect(state.left, state.top, state.left + 1, state.bottom, edge);
        drawRect(state.left + state.width - 1, state.top, state.left + state.width, state.bottom, edge);
        drawCenteredString(mc.fontRenderer, mc.fontRenderer.trimStringToWidth(heading, state.width - 32),
                event.gui.width / 2, state.top + 17, 0xFFF1EFF5);
        int center = event.gui.width / 2;
        drawRect(state.left + 18, state.top + 38, center - 6, state.top + 39, edge);
        drawRect(center + 6, state.top + 38, state.left + state.width - 18, state.top + 39, edge);
        drawRect(center - 2, state.top + 36, center + 2, state.top + 41, MenuAppearance.accent());
        if (state.disconnected) {
            int max = Math.max(0, state.lines.size() - state.visible);
            int wheel = Mouse.getDWheel();
            if (wheel != 0) state.scroll = Math.max(0, Math.min(max, state.scroll + (wheel < 0 ? 2 : -2)));
            for (int i = 0; i < state.visible && state.scroll + i < state.lines.size(); i++) {
                drawCenteredString(mc.fontRenderer, (String) state.lines.get(state.scroll + i), center,
                        state.textTop + i * 12, 0xFFF1EFF5);
            }
            if (max > 0) {
                int track = state.textBottom - state.textTop, thumb = Math.max(8, track * state.visible / state.lines.size());
                int y = state.textTop + (track - thumb) * state.scroll / max;
                drawRect(state.left + state.width - 10, y, state.left + state.width - 8, y + thumb, MenuAppearance.accent());
                drawCenteredString(mc.fontRenderer, I18n.format("htgt6.connection.scroll"), center,
                        state.bottom - 46, 0xFF999999);
            }
        } else {
            // Indeterminate activity indicator, never a fabricated percentage.
            int w = state.width - 64, x = state.left + 32, y = state.textTop + 26;
            drawRect(x, y, x + w, y + 3, edge);
            int segment = Math.max(16, w / 5);
            double wave = (Math.sin(System.nanoTime() / 650000000.0) + 1.0) * 0.5;
            int start = x + (int) ((w - segment) * wave);
            drawRect(start, y, start + segment, y + 3, MenuAppearance.accent());
        }
        for (Object entry : state.buttons) MenuAppearance.drawButton((GuiButton) entry, mc, event.mouseX, event.mouseY);
        event.setCanceled(true);
    }

    private static final class State {
        boolean disconnected;
        String title = "", message = "";
        List buttons, lines;
        int left, top, bottom, width, textTop, textBottom, visible, scroll;
    }
}
