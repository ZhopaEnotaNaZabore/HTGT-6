package com.mod.htgt6.client.vanillatweaks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.renderer.Tessellator; // FIXED: Missing 1.7.10 import
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class GuiModernServerSelect extends GuiScreen {

    private final GuiScreen parent;
    private GuiTextField searchField;
    private GuiServerList serverSlotList;

    private final ServerList serverListConfig;
    private final List<ServerData> allServers = new ArrayList<ServerData>();
    public final List<ServerData> filteredServers = new ArrayList<ServerData>();
    private final Map<String, ResourceLocation> iconCache = new HashMap<String, ResourceLocation>();

    private final OldServerPinger serverPinger = new OldServerPinger();
    public int selectedIndex = -1;
    private GuiButton btnConnect;

    public GuiModernServerSelect(GuiScreen parent) {
        this.parent = parent;
        this.serverListConfig = new ServerList(Minecraft.getMinecraft());
        this.serverListConfig.loadServerList();
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.allServers.clear();

        // Load servers from servers.dat configuration
        for (int i = 0; i < this.serverListConfig.countServers(); ++i) {
            ServerData server = this.serverListConfig.getServerData(i);
            this.allServers.add(server);
            try {
                // Async ping the server to fetch dynamic counts and descriptions
                this.serverPinger.func_147224_a(server);
            } catch (Exception ignored) {}
        }

        // STAYS ONLY 2 BUTTONS: Connect and Back to Menu
        this.buttonList.add(btnConnect = new GuiButton(1, width / 2 - 152, height - 52, 150, 20, "Connect"));
        this.buttonList.add(new GuiButton(2, width / 2 + 2, height - 52, 150, 20, "Back to Menu"));

        // Setup the modern search field layout
        searchField = new GuiTextField(fontRendererObj, 22, 22, 150, 16);
        searchField.setFocused(true);
        searchField.setCanLoseFocus(false);

        updateSearch();
    }

    public void updateSearch() {
        String query = searchField.getText().toLowerCase().trim();
        filteredServers.clear();

        for (ServerData server : allServers) {
            if (query.isEmpty() || server.serverName.toLowerCase().contains(query) || server.serverIP.toLowerCase().contains(query)) {
                filteredServers.add(server);
            }
        }

        serverSlotList = new GuiServerList(this, mc, filteredServers);
        if (selectedIndex >= filteredServers.size()) {
            selectedIndex = -1;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 1. Draw dirt background
        drawDefaultBackground();

        // 2. Draw slots behind text layers
        serverSlotList.drawScreen(mouseX, mouseY, partialTicks);

        // 3. Draw text and titles on top of list shadows
        drawCenteredString(fontRendererObj, "Modern Server Selection", width / 2, 6, 0xFFFFFF);
        searchField.drawTextBox();

        // Handle active validation state checks every frame update
        btnConnect.enabled = (selectedIndex >= 0 && selectedIndex < filteredServers.size());

        // 4. Draw buttons on top of everything
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void drawServerCard(ServerData server, int y, boolean selected) {
        int x = 20;
        int w = width - 40;
        int h = 64;

        // Clean modern card base rectangle without overlapping selections
        drawRect(x, y, x + w, y + h, selected ? 0xAA666666 : 0x44222222);

        // Process base64 dynamic server icons cleanly
        ResourceLocation iconLocation = getServerIcon(server);
        if (iconLocation != null) {
            mc.getTextureManager().bindTexture(iconLocation);
            GL11.glColor4f(1, 1, 1, 1);

            drawTexturedRect(x + 4, y + 4, 56, 56);
        } else {
            // Dark gray box fallback icon frame
            drawRect(x + 4, y + 4, x + 60, y + 60, 0xFF151515);
        }

        // Left Alignment Info Block
        drawString(fontRendererObj, server.serverName, x + 70, y + 8, 0xFFFFFF);
        drawString(fontRendererObj, "IP: " + server.serverIP, x + 70, y + 24, 0xBBBBBB);

        String motd = (server.serverMOTD != null) ? server.serverMOTD : "Polling connection state details...";
        // Prevent string overlapping text components by cropping long descriptions
        if (fontRendererObj.getStringWidth(motd) > (w - 260)) {
            motd = fontRendererObj.trimStringToWidth(motd, w - 270) + "...";
        }
        drawString(fontRendererObj, motd, x + 70, y + 38, 0x888888);

        // Right Alignment Metrics Block (Matching your singleplayer theme offsets)
        int rightAlignX = x + w - 180;

        String players = (server.populationInfo != null) ? server.populationInfo : "0/0";
        drawString(fontRendererObj, "Players:", rightAlignX, y + 20, 0x888888);
        drawString(fontRendererObj, players, rightAlignX, y + 34, 0xAAAAAA);
    }

    private ResourceLocation getServerIcon(ServerData server) {
        // NOTE: If your mappings setup throws an error on getBase64EncodedIconData(),
        // replace it with the SRG mapping name: server.func_147409_e()
        String base64Icon = server.getBase64EncodedIconData();
        if (base64Icon == null || base64Icon.isEmpty()) return null;

        if (iconCache.containsKey(server.serverIP)) {
            return iconCache.get(server.serverIP);
        }

        try {
            byte[] imgBytes = org.apache.commons.codec.binary.Base64.decodeBase64(base64Icon);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgBytes));
            if (img != null) {
                DynamicTexture texture = new DynamicTexture(img);
                ResourceLocation loc = mc.getTextureManager().getDynamicTextureLocation("server_icon_" + server.serverIP.hashCode(), texture);
                iconCache.put(server.serverIP, loc);
                return loc;
            }
        } catch (Exception ignored) {}

        return null;
    }

    public void connectToSelectedServer() {
        if (selectedIndex >= 0 && selectedIndex < filteredServers.size()) {
            ServerData server = filteredServers.get(selectedIndex);
            this.serverPinger.func_147223_a(); // Cancel background ping operations safely
            mc.displayGuiScreen(new GuiConnecting(this, mc, server));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            connectToSelectedServer();
        } else if (button.id == 2) {
            this.serverPinger.func_147223_a();
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (searchField.textboxKeyTyped(c, key)) {
            updateSearch();
            selectedIndex = -1;
        } else if (key == Keyboard.KEY_ESCAPE) {
            this.serverPinger.func_147223_a();
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        searchField.mouseClicked(x, y, button);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        this.serverPinger.func_147223_a(); // Shut down network ping listening threads
    }

    private void drawTexturedRect(int x, int y, int width, int height) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x, y + height, zLevel, 0, 1);
        tess.addVertexWithUV(x + width, y + height, zLevel, 1, 1);
        tess.addVertexWithUV(x + width, y, zLevel, 1, 0);
        tess.addVertexWithUV(x, y, zLevel, 0, 0);
        tess.draw();
    }
}