package com.mod.htgt6.client.vanillatweaks.TAB;
//old working setup
//Disabled
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TAB {

    private final Minecraft mc = Minecraft.getMinecraft();

    public static double serverTPS = 20.0;   // updated by the handler / local server
    public static int clientPing = 0;       // updated by the handler

    /* --- channel / ping helpers ------------------------------------------ */
    private long pingSendTime = 0;
    private long lastPingTime = 0;          // Tracks 3-second intervals
    private boolean pingPending = false;

    private io.netty.channel.Channel nettyChannel = null;

    public TAB() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    /* --------------------------------------------------------------------- */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (this.mc.theWorld == null) {
            this.nettyChannel = null;
            this.pingPending = false;
            return;
        }

        /* 1️⃣ Handle Singleplayer Dynamic TPS Calculation */
        if (this.mc.isSingleplayer()) {
            this.nettyChannel = null;
            this.pingPending = false;

            if (this.mc.getIntegratedServer() != null) {
                long[] tickTimes = this.mc.getIntegratedServer().tickTimeArray;
                if (tickTimes != null) {
                    long total = 0;
                    for (long t : tickTimes) {
                        total += t;
                    }
                    double meanTickTime = (double) total / tickTimes.length * 1.0E-6D;
                    TAB.serverTPS = Math.min(20.0, 1000.0 / meanTickTime);
                }
            } else {
                TAB.serverTPS = 20.0;
            }
            return; // No need to process server networking elements in singleplayer
        }

        /* 2️⃣ Handle Multiplayer Network Channel Injection */
        if (this.nettyChannel == null && this.mc.thePlayer != null
                && this.mc.thePlayer.sendQueue != null) {

            try {
                net.minecraft.network.NetworkManager netMgr =
                        this.mc.thePlayer.sendQueue.getNetworkManager();

                if (netMgr != null && netMgr.channel() != null) {
                    this.nettyChannel = netMgr.channel();

                    if (this.nettyChannel.pipeline().get("tab_ping_handler") == null) {
                        this.nettyChannel.pipeline()
                                .addBefore("packet_handler", "tab_ping_handler",
                                        new io.netty.channel.ChannelInboundHandlerAdapter() {

                                            @Override
                                            public void channelRead(
                                                    io.netty.channel.ChannelHandlerContext ctx,
                                                    Object msg) throws Exception {

                                                /* Real‑time ping reply hook */
                                                if (msg instanceof net.minecraft.network.play.server.S37PacketStatistics
                                                        && pingPending) {
                                                    clientPing = (int) (System.currentTimeMillis() - pingSendTime);
                                                    pingPending = false;
                                                }

                                                /* Server TPS packet hook (if custom server-side modification supports it) */
                                                if (msg instanceof net.minecraft.network.play.server.S37PacketStatistics) {
                                                    double tickRate = getTickRate(
                                                            (net.minecraft.network.play.server.S37PacketStatistics) msg);
                                                    TAB.serverTPS = tickRate;
                                                }

                                                super.channelRead(ctx, msg);
                                            }
                                        });
                    }
                }
            } catch (Exception e) {
                System.err.println("[TAB] Failed to inject real‑time ping interceptor: " + e.getMessage());
                e.printStackTrace();
            }
        }

        /* 3️⃣ Dynamically Send Ping Request Every 3 Seconds (3000ms) */
        if (this.nettyChannel != null && !this.pingPending) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastPingTime >= 3000) {
                trySendPing();
            }
        }
    }

    private void trySendPing() {
        try {
            // Using ClientStatus packet to request stats. The server will safely answer back with S37PacketStatistics.
            C16PacketClientStatus pingReq = new C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS);

            this.nettyChannel.writeAndFlush(pingReq);
            this.pingSendTime = System.currentTimeMillis();
            this.lastPingTime = this.pingSendTime;
            this.pingPending = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* --------------------------------------------------------------------- */
    private static double getTickRate(net.minecraft.network.play.server.S37PacketStatistics packet) {
        try {
            Field f = net.minecraft.network.play.server.S37PacketStatistics.class.getDeclaredField("tickRate");
            f.setAccessible(true);
            return f.getDouble(packet);
        } catch (Exception e) {
            // Do not print stack trace to clear console spam if vanilla servers don't have this reflected field
        }
        return 20.0;
    }

    /* --------------------------------------------------------------------- */
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

    /* --------------------------------------------------------------------- */
    @SuppressWarnings("rawtypes")
    private void renderFancyTabList(int width, int height) {
        if (this.mc.thePlayer == null) return;

        List<GuiPlayerInfo> playerList = this.mc.thePlayer.sendQueue != null ?
                this.mc.thePlayer.sendQueue.playerInfoList : null;

        /* Fake list for single‑player debug view */
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

        int maxVisiblePlayers = Math.min(playerList.size(), 20);
        int tabWidth = 260, rowHeight = 14, headerHeight = 24, padding = 6;

        int xPos = (width / 2) - (tabWidth / 2), yPos = 30;
        int totalHeight = headerHeight + maxVisiblePlayers * rowHeight + padding;

        Gui.drawRect(xPos, yPos, xPos + tabWidth, yPos + totalHeight, 0xCD111111);
        Gui.drawRect(xPos, yPos, xPos + tabWidth, yPos + 2, 0xFF9933FF);

        /* Header UI */
        String title = "§d§lPlayers Online";
        this.mc.fontRenderer.drawStringWithShadow(title, xPos + 8, yPos + 7, 0xFFFFFF);

        String currentFps = "0";
        if (this.mc.debug != null && this.mc.debug.contains(" fps")) {
            currentFps = this.mc.debug.split(" fps")[0];
        }

        String tpsColor = serverTPS >= 18.0 ? "§a" : (serverTPS >= 14.0 ? "§e" : "§c");
        String perfText = "§7FPS: §f" + currentFps + " §7| TPS: " + tpsColor + String.format("%.1f", serverTPS);
        int perfW = this.mc.fontRenderer.getStringWidth(perfText);
        this.mc.fontRenderer.drawStringWithShadow(perfText, xPos + tabWidth - perfW - 8, yPos + 7, 0xFFFFFF);

        /* Player Rows Rendering */
        int curY = yPos + headerHeight;
        for (int i = 0; i < maxVisiblePlayers; i++) {
            GuiPlayerInfo info = playerList.get(i);
            if ((i & 1) == 0)
                Gui.drawRect(xPos + 4, curY, xPos + tabWidth - 4, curY + rowHeight, 0x1AFFFFFF);

            String pname = info.name;
            this.mc.fontRenderer.drawStringWithShadow("§7" + (i + 1) + ". §f" + pname, xPos + 8, curY + 3, 0xFFFFFF);

            int ping = info.responseTime;
            if (!this.mc.isSingleplayer() && pname.equals(this.mc.thePlayer.getCommandSenderName()) && clientPing > 0) {
                ping = clientPing; // Override local player with dynamic real-time value
                info.responseTime = clientPing;
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

    /* --------------------------------------------------------------------- */
    private void drawPingBarIcon(int x, int y, int ping) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        this.mc.getTextureManager().bindTexture(Gui.icons);

        int icon;
        if (ping < 0)        icon = 5;
        else if (ping < 150) icon = 0;
        else if (ping < 300) icon = 1;
        else if (ping < 600) icon = 2;
        else if (ping < 1000)icon = 3;
        else                 icon = 4;

        this.mc.ingameGUI.drawTexturedModalRect(x, y, 0, 176 + (icon * 8), 10, 8);
    }
}