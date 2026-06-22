package com.mod.htgt6.client.vanillatweaks.TAB;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class TAB {

    private final Minecraft mc = Minecraft.getMinecraft();

    // Dynamically updated TPS
    public static double serverTPS = 20.0;

    // Trackers for our Client-Side TPS calculation
    private long lastTimeUpdate = 0;
    private long lastWorldTime = 0;

    // FIX TRACKERS: Dynamic Client-Side Ping Engine for Multiplayer
    private io.netty.channel.Channel nettyChannel = null;
    private long pingSendTime = 0;
    private final short pingActionId = -9999; // Negative short ensures no conflict with inventory windows
    public static int clientPing = 0;
    private int pingCheckTimer = 0;

    public TAB() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    /**
     * FIX: Calculates TPS and drives the client-side Netty ping engine.
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Reset trackers cleanly if the player leaves the server/world
        if (this.mc.theWorld == null) {
            this.nettyChannel = null;
            return;
        }

        long currentTime = System.currentTimeMillis();
        long interval = this.mc.isSingleplayer() ? 1000 : 5000;

        if (lastTimeUpdate == 0) {
            lastTimeUpdate = currentTime;
            lastWorldTime = this.mc.theWorld.getTotalWorldTime();
            return;
        }

        long timeElapsed = currentTime - lastTimeUpdate;

        if (timeElapsed >= interval) {
            long currentWorldTime = this.mc.theWorld.getTotalWorldTime();
            long ticksElapsed = currentWorldTime - lastWorldTime;

            if (ticksElapsed < 0 || ticksElapsed > (interval / 50) + 100) {
                serverTPS = 20.0;
            } else {
                double calculatedTPS = (ticksElapsed / (double) timeElapsed) * 1000.0;
                serverTPS = Math.min(20.0, calculatedTPS);
            }

            lastTimeUpdate = currentTime;
            lastWorldTime = currentWorldTime;
        }

        // FIX LOGIC: Dynamic connection pinging when connected to multiplayer environments
        if (!this.mc.isSingleplayer() && this.mc.thePlayer != null && this.mc.thePlayer.sendQueue != null) {

            // 1. Inject inbound packet hook to listen for the server's immediate reply echo
            if (this.nettyChannel == null) {
                try {
                    net.minecraft.network.NetworkManager netManager = this.mc.thePlayer.sendQueue.getNetworkManager();
                    if (netManager != null && netManager.channel() != null) {
                        this.nettyChannel = netManager.channel();

                        if (this.nettyChannel.pipeline().get("tab_ping_handler") == null) {
                            this.nettyChannel.pipeline().addBefore("packet_handler", "tab_ping_handler", new io.netty.channel.ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object msg) throws Exception {
                                    if (msg instanceof net.minecraft.network.play.server.S32PacketConfirmTransaction) {
                                        net.minecraft.network.play.server.S32PacketConfirmTransaction packet = (net.minecraft.network.play.server.S32PacketConfirmTransaction) msg;
                                        // Match our custom identification short key
                                        if (packet.func_148890_d() == pingActionId) {
                                            clientPing = (int) (System.currentTimeMillis() - pingSendTime);
                                            if (clientPing < 0) clientPing = 0;
                                        }
                                    }
                                    super.channelRead(ctx, msg);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 2. Request an instantaneous transaction echo update every 2 seconds (40 ticks)
            if (this.nettyChannel != null && this.nettyChannel.isOpen()) {
                pingCheckTimer++;
                if (pingCheckTimer >= 40) {
                    pingCheckTimer = 0;
                    pingSendTime = System.currentTimeMillis();
                    this.mc.thePlayer.sendQueue.addToSendQueue(new net.minecraft.network.play.client.C0FPacketConfirmTransaction(0, pingActionId, false));
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderTabListPre(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
            event.setCanceled(true);
            ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
            renderFancyTabList(scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight());
        }
    }

    @SubscribeEvent
    public void onRenderTabListPost(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR && this.mc.isSingleplayer()) {
            int tabKeyCode = this.mc.gameSettings.keyBindPlayerList.getKeyCode();

            if (Keyboard.isKeyDown(tabKeyCode) && this.mc.currentScreen == null) {
                ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
                renderFancyTabList(scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void renderFancyTabList(int width, int height) {
        if (this.mc.thePlayer == null) return;

        List playerList = null;

        if (this.mc.thePlayer.sendQueue != null && this.mc.thePlayer.sendQueue.playerInfoList != null) {
            playerList = this.mc.thePlayer.sendQueue.playerInfoList;
        }

        if ((playerList == null || playerList.isEmpty()) && this.mc.isSingleplayer()) {
            ArrayList<GuiPlayerInfo> fakeList = new ArrayList<GuiPlayerInfo>();

            GuiPlayerInfo fakePlayer = new GuiPlayerInfo(this.mc.thePlayer.getCommandSenderName());
            fakePlayer.responseTime = 0;
            fakeList.add(fakePlayer);

            GuiPlayerInfo testBot1 = new GuiPlayerInfo("Developer_Bot");
            testBot1.responseTime = 42;
            fakeList.add(testBot1);

            GuiPlayerInfo testBot2 = new GuiPlayerInfo("Laggy_Bot");
            testBot2.responseTime = 340;
            fakeList.add(testBot2);

            playerList = fakeList;
        }

        if (playerList == null || playerList.isEmpty()) return;

        int maxVisiblePlayers = Math.min(playerList.size(), 20);
        int tabWidth = 260;
        int rowHeight = 14;
        int headerHeight = 24;
        int padding = 6;

        int xPosition = (width / 2) - (tabWidth / 2);
        int yPosition = 30;
        int totalHeight = headerHeight + (maxVisiblePlayers * rowHeight) + padding;

        Gui.drawRect(xPosition, yPosition, xPosition + tabWidth, yPosition + totalHeight, 0xCD111111);
        Gui.drawRect(xPosition, yPosition, xPosition + tabWidth, yPosition + 2, 0xFF9933FF);

        String titleText = "§d§lPlayers Online";
        this.mc.fontRenderer.drawStringWithShadow(titleText, xPosition + 8, yPosition + 7, 0xFFFFFF);

        String currentFps = "0";
        if (this.mc.debug != null && this.mc.debug.contains(" fps")) {
            currentFps = this.mc.debug.split(" fps")[0];
        }

        String tpsColor = serverTPS >= 18.0 ? "§a" : (serverTPS >= 14.0 ? "§e" : "§c");
        String performanceText = "§7FPS: §f" + currentFps + " §7| TPS: " + tpsColor + String.format("%.1f", serverTPS);
        int perfWidth = this.mc.fontRenderer.getStringWidth(performanceText);
        this.mc.fontRenderer.drawStringWithShadow(performanceText, xPosition + tabWidth - perfWidth - 8, yPosition + 7, 0xFFFFFF);

        int currentY = yPosition + headerHeight;

        for (int i = 0; i < maxVisiblePlayers; i++) {
            Object obj = playerList.get(i);
            if (!(obj instanceof GuiPlayerInfo)) continue;

            GuiPlayerInfo info = (GuiPlayerInfo) obj;

            if (i % 2 == 0) {
                Gui.drawRect(xPosition + 4, currentY, xPosition + tabWidth - 4, currentY + rowHeight, 0x1AFFFFFF);
            }

            String playerName = info.name;
            this.mc.fontRenderer.drawStringWithShadow("§7" + (i + 1) + ". §f" + playerName, xPosition + 8, currentY + 3, 0xFFFFFF);

            // FIX INTEGRATION: Override static info cache data using real-time Netty pipeline measurements
            int ping = info.responseTime;
            if (!this.mc.isSingleplayer() && playerName.equals(this.mc.thePlayer.getCommandSenderName()) && clientPing > 0) {
                ping = clientPing;
                info.responseTime = clientPing; // Re-sync directly to object cache fields
            }

            String pingStr = ping + "ms";
            int pingTextColor = 0x55FF55;
            if (ping > 150) pingTextColor = 0xFFFF55;
            if (ping > 300) pingTextColor = 0xFF5555;

            int pingStrWidth = this.mc.fontRenderer.getStringWidth(pingStr);
            this.mc.fontRenderer.drawStringWithShadow(pingStr, xPosition + tabWidth - pingStrWidth - 22, currentY + 3, pingTextColor);

            drawPingBarIcon(xPosition + tabWidth - 16, currentY + 3, ping);

            currentY += rowHeight;
        }

        if (playerList.size() > maxVisiblePlayers) {
            String overflowText = "§7...and " + (playerList.size() - maxVisiblePlayers) + " more players";
            int overWidth = this.mc.fontRenderer.getStringWidth(overflowText);
            this.mc.fontRenderer.drawStringWithShadow(overflowText, width / 2 - (overWidth / 2), currentY + 2, 0xFFFFFF);
        }
    }

    private void drawPingBarIcon(int x, int y, int ping) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(Gui.icons);

        int iconIndex;
        if (ping < 0) iconIndex = 5;
        else if (ping < 150) iconIndex = 0;
        else if (ping < 300) iconIndex = 1;
        else if (ping < 600) iconIndex = 2;
        else if (ping < 1000) iconIndex = 3;
        else iconIndex = 4;

        this.mc.ingameGUI.drawTexturedModalRect(x, y, 0, 176 + (iconIndex * 8), 10, 8);
    }
}