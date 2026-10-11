package com.mod.htgt6.client.vanillatweaks.TAB;

import com.mod.htgt6.client.vanillatweaks.CHAT.TitleManager;
import com.mod.htgt6.client.vanillatweaks.CHAT.TitleVisuals;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TABClientRenderer {
    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderTabListPre(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
            event.setCanceled(true);
            ScaledResolution sr = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
            renderFancyTabList(sr.getScaledWidth(), sr.getScaledHeight());
        }
    }

    @SubscribeEvent
    public void onRenderTabListPost(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR && this.mc.isSingleplayer()) {
            int tabKeyCode = this.mc.gameSettings.keyBindPlayerList.getKeyCode();
            if (Keyboard.isKeyDown(tabKeyCode) && this.mc.currentScreen == null) {
                ScaledResolution sr = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
                renderFancyTabList(sr.getScaledWidth(), sr.getScaledHeight());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void renderFancyTabList(int width, int height) {
        if (this.mc.thePlayer == null) return;

        List<GuiPlayerInfo> playerList = this.mc.thePlayer.sendQueue != null ?
                this.mc.thePlayer.sendQueue.playerInfoList : null;

        /* Fake list layout fallback for single-player testing */
        if ((playerList == null || playerList.isEmpty()) && this.mc.isSingleplayer()) {
            ArrayList<GuiPlayerInfo> fakeList = new ArrayList<>();
            GuiPlayerInfo me = new GuiPlayerInfo(this.mc.thePlayer.getCommandSenderName());
            me.responseTime = 0;
            fakeList.add(me);

            GuiPlayerInfo b1 = new GuiPlayerInfo("Developer_Bot");
            b1.responseTime = 42;
            fakeList.add(b1);

            GuiPlayerInfo b2 = new GuiPlayerInfo("Laggy_Bot");
            b2.responseTime = 340;
            fakeList.add(b2);
            playerList = fakeList;
        }

        if (playerList == null || playerList.isEmpty()) return;

        // Keep one consistent server snapshot through the whole render pass.
        Map<String, Integer> syncedPings = TABDataHolder.playerPings;

        int maxVisiblePlayers = Math.min(playerList.size(), 20);
        int tabWidth = 260, rowHeight = 14, headerHeight = 24, padding = 6;

        int xPos = (width / 2) - (tabWidth / 2), yPos = 30;
        int totalHeight = headerHeight + maxVisiblePlayers * rowHeight + padding;

        Gui.drawRect(xPos, yPos, xPos + tabWidth, yPos + totalHeight, 0xCD111111);
        Gui.drawRect(xPos, yPos, xPos + tabWidth, yPos + 2, 0xFF9933FF);

        /* Tab Header Section */
        String title = "§d§lPlayers Online";
        this.mc.fontRenderer.drawStringWithShadow(title, xPos + 8, yPos + 7, 0xFFFFFF);

        String currentFps = "0";
        if (this.mc.debug != null && this.mc.debug.contains(" fps")) {
            currentFps = this.mc.debug.split(" fps")[0];
        }

        // Pulling performance telemetry securely out of the data holder
        double calculatedTPS = TABDataHolder.serverTPS;
        String tpsColor = calculatedTPS >= 18.0 ? "§a" : (calculatedTPS >= 14.0 ? "§e" : "§c");
        String perfText = "§7FPS: §f" + currentFps + " §7| TPS: " + tpsColor + String.format("%.1f", calculatedTPS);
        int perfW = this.mc.fontRenderer.getStringWidth(perfText);
        this.mc.fontRenderer.drawStringWithShadow(perfText, xPos + tabWidth - perfW - 8, yPos + 7, 0xFFFFFF);

        /* Player Rows Section */
        int curY = yPos + headerHeight;
        for (int i = 0; i < maxVisiblePlayers; i++) {
            GuiPlayerInfo info = playerList.get(i);
            if ((i & 1) == 0) {
                Gui.drawRect(xPos + 4, curY, xPos + tabWidth - 4, curY + rowHeight, 0x1AFFFFFF);
            }

            String pname = info.name;
            String titlePrefix = TitleVisuals.animate(TitleManager.activePlayerTitles.getOrDefault(pname, ""), this.mc.ingameGUI.getUpdateCounter());
            this.mc.fontRenderer.drawStringWithShadow("§7" + (i + 1) + ". " + titlePrefix + "§f" + pname, xPos + 8, curY + 3, 0xFFFFFF);
        //    this.mc.fontRenderer.drawStringWithShadow("§7" + (i + 1) + ". §f" + pname, xPos + 8, curY + 3, 0xFFFFFF);

            int ping = info.responseTime;

// Fetch synced ping from packet for ALL players on multiplayer
            Integer syncedPing = this.mc.isSingleplayer() ? null : syncedPings.get(pname);
            if (syncedPing != null) {
                ping = syncedPing;
            }

            String pingStr = ping + "ms";
            int color = 0x55FF55;
            if (ping > 150) color = 0xFFFF55;
            if (ping > 300) color = 0xFF5555;

            int pingW = this.mc.fontRenderer.getStringWidth(pingStr);
            this.mc.fontRenderer.drawStringWithShadow(pingStr, xPos + tabWidth - pingW - 22, curY + 3, color);

            drawPingBarIcon(xPos + tabWidth - 16, curY + 3, ping);
            curY += rowHeight;
        }

        if (playerList.size() > maxVisiblePlayers) {
            String overflow = "§7...and " + (playerList.size() - maxVisiblePlayers) + " more players";
            int w = this.mc.fontRenderer.getStringWidth(overflow);
            this.mc.fontRenderer.drawStringWithShadow(overflow, width / 2 - w / 2, curY + 2, 0xFFFFFF);
        }

    }

    private void drawPingBarIcon(int x, int y, int ping) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        this.mc.getTextureManager().bindTexture(Gui.icons);
        int icon = (ping < 0) ? 5 : (ping < 150) ? 0 : (ping < 300) ? 1 : (ping < 600) ? 2 : (ping < 1000) ? 3 : 4;
        this.mc.ingameGUI.drawTexturedModalRect(x, y, 0, 176 + (icon * 8), 10, 8);
    }
}
