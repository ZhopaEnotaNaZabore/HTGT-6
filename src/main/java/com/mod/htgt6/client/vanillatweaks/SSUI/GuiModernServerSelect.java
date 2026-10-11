package com.mod.htgt6.client.vanillatweaks.SSUI;

import com.mod.htgt6.client.menu.MenuAppearance;
import com.mod.htgt6.common.ui.MenuTheme;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/** Responsive server browser. Both themes share the same controls and connection path. */
public final class GuiModernServerSelect extends GuiScreen implements GuiYesNoCallback {
    private static final ResourceLocation BACKDROP = new ResourceLocation("htgt6", "textures/loading/background_industry.png");
    private static final int CONNECT = 1, DIRECT = 2, ADD = 3, EDIT = 4, DELETE = 5, REFRESH = 6, BACK = 7;
    private final GuiScreen parent;
    private final ServerList saved;
    private final ServerBrowserPreferences preferences;
    private final List<ServerData> all = new ArrayList<ServerData>();
    private final List<ServerData> filtered = new ArrayList<ServerData>();
    private final Map<ServerData, Icon> icons = new LinkedHashMap<ServerData, Icon>(64, 0.75F, true);
    private ServerPingSession pings;
    private ServerBrowserLayout layout;
    private GuiTextField search;
    private ServerData selected, pending;
    private int action, editIndex = -1, tab, offset;
    private String oldAddress, query = "";
    private long lastClick;
    private ServerData lastClicked;
    private boolean draggingScroll;
    private GuiButton connect, edit, delete;

    public GuiModernServerSelect(GuiScreen parent) {
        this.parent = parent;
        Minecraft mc = Minecraft.getMinecraft();
        saved = new ServerList(mc);
        preferences = new ServerBrowserPreferences(new File(mc.mcDataDir, "config/HTGT6/server-browser.properties"));
        // Required before status replies or Forge's connectToServer access serverDataTag.
        FMLClientHandler.instance().setupServerList();
    }

    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        closePings();
        releaseIcons();
        pings = new ServerPingSession();
        layout = new ServerBrowserLayout(width, height);
        String address = selected == null ? null : selected.serverIP;
        saved.loadServerList();
        all.clear();
        for (int i = 0; i < saved.countServers(); i++) all.add(saved.getServerData(i));
        selected = null;
        for (ServerData server : all) if (server.serverIP.equals(address)) { selected = server; break; }
        search = new GuiTextField(fontRendererObj, 16, 35, width - 32, 18);
        search.setMaxStringLength(128);
        search.setText(query);
        search.setFocused(false);
        for (int i = 0; i < 3; i++) {
            int w = (layout.listWidth - 8) / 3;
            buttonList.add(new MenuAppearance.Button(20 + i, 12 + i * (w + 4), 60, w, 20,
                    (tab == i ? "> " : "") + tr(i == 0 ? "all" : i == 1 ? "favorites" : "recent")));
        }
        int cell = (width - 32) / 3;
        buttonList.add(new MenuAppearance.Button(BACK, 12, height - 26, cell, 20, tr("back")));
        buttonList.add(edit = new MenuAppearance.Button(EDIT, 16 + cell, height - 26, cell, 20, tr("edit")));
        buttonList.add(delete = new MenuAppearance.Button(DELETE, 20 + cell * 2, height - 26, cell, 20, tr("delete")));
        buttonList.add(new MenuAppearance.Button(ADD, 12, height - 50, cell, 20, tr("add")));
        buttonList.add(new MenuAppearance.Button(DIRECT, 16 + cell, height - 50, cell, 20, tr("direct")));
        buttonList.add(new MenuAppearance.Button(REFRESH, 20 + cell * 2, height - 50, cell, 20, tr("refresh")));
        int cx = layout.wide ? layout.detailX + 8 : width - 120;
        int cy = layout.wide ? layout.detailBottom - 28 : height - 80;
        buttonList.add(connect = new MenuAppearance.Button(CONNECT, cx, cy,
                layout.wide ? layout.detailWidth - 16 : 108, 20, tr("connect")));
        filter();
    }

    private static String tr(String key) { return I18n.format("htgt6.servers." + key); }
    private void filter() {
        filtered.clear();
        filtered.addAll(ServerBrowserModel.filter(all, preferences, tab, query));
        if (!filtered.contains(selected)) selected = filtered.isEmpty() ? null : filtered.get(0);
        offset = Math.max(0, Math.min(offset, Math.max(0, filtered.size() - layout.visibleRows)));
        updateButtons();
    }
    private void updateButtons() {
        boolean has = selected != null && filtered.contains(selected);
        connect.enabled = has;
        edit.enabled = has;
        delete.enabled = has;
    }

    @Override public void updateScreen() {
        search.updateCursorCounter();
        if (pings != null) pings.tick();
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        MenuAppearance.background(width, height);
        mc.getTextureManager().bindTexture(BACKDROP);
        GL11.glColor4f(1, 1, 1, 1);
        Tessellator backdrop = Tessellator.instance;
        backdrop.startDrawingQuads();
        backdrop.addVertexWithUV(0, height, zLevel, 0, 1);
        backdrop.addVertexWithUV(width, height, zLevel, 1, 1);
        backdrop.addVertexWithUV(width, 0, zLevel, 1, 0);
        backdrop.addVertexWithUV(0, 0, zLevel, 0, 0);
        backdrop.draw();
        drawGradientRect(0, 0, width, height, MenuTheme.color(0xA010121F, 0xAD18130D),
                MenuTheme.color(0xEF10121A, 0xEF101113));
        drawString(fontRendererObj, tr("title"), 12, 12, 0xFFF1EFF5);
        String count = filtered.size() + " / " + all.size();
        drawString(fontRendererObj, count, width - 12 - fontRendererObj.getStringWidth(count), 12, 0xFFAAAAAA);
        search.drawTextBox();
        if (search.getText().isEmpty() && !search.isFocused()) drawString(fontRendererObj, tr("search"), 20, 40, 0xFF888888);
        for (int row = 0; row < layout.visibleRows && offset + row < filtered.size(); row++) {
            ServerData server = filtered.get(offset + row);
            if (pings != null) pings.request(server);
            drawCard(server, layout.top + row * layout.rowHeight, server == selected);
        }
        if (filtered.isEmpty()) drawCenteredString(fontRendererObj, tr("empty"),
                12 + layout.listWidth / 2, layout.top + 22, 0xFFAAAAAA);
        if (filtered.size() > layout.visibleRows) {
            int track = layout.listBottom - layout.top;
            int thumb = Math.max(12, track * layout.visibleRows / filtered.size());
            int y = layout.top + (track - thumb) * offset / (filtered.size() - layout.visibleRows);
            drawRect(12 + layout.listWidth - 3, y, 12 + layout.listWidth, y + thumb, MenuAppearance.accent());
        }
        drawDetails();
        updateButtons();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawCard(ServerData server, int y, boolean active) {
        int x = 12, w = layout.listWidth - 6, h = layout.rowHeight - 4;
        drawRect(x, y, x + w, y + h, active ? MenuAppearance.selected() : MenuAppearance.panel());
        if (active) frame(x, y, x + w, y + h, MenuAppearance.accent());
        drawIcon(server, x + 7, y + 9, 32);
        text(server.serverName, x + 46, y + 7, w - 70, 0xFFF1EFF5);
        text(server.serverIP, x + 46, y + 20, w - 56, 0xFFAAAAAA);
        text(status(server), x + 46, y + 35, w - 56, statusColor(server));
        drawStar(x + w - 13, y + 11, preferences.favorite(server.serverIP));
    }

    private void drawDetails() {
        if (!layout.wide) {
            text(selected == null ? tr("choose") : selected.serverName, 12, height - 77,
                    width - 142, 0xFFF1EFF5);
            if (selected != null) text(status(selected), 12, height - 65, width - 142, statusColor(selected));
            return;
        }
        int x = layout.detailX, y = layout.top, w = layout.detailWidth;
        drawRect(x, y, x + w, layout.detailBottom, MenuAppearance.panel());
        frame(x, y, x + w, layout.detailBottom, 0xFF565962);
        if (selected == null) { text(tr("choose"), x + 8, y + 12, w - 16, 0xFFAAAAAA); return; }
        if (pings != null) pings.request(selected);
        int room = layout.detailBottom - y;
        // Compact mode still shows all essential connection information at GUI scale 320x240.
        if (room >= 190) { drawIcon(selected, x + 8, y + 10, 48); y += 66; }
        else y += 10;
        text(selected.serverName, x + 8, y, w - 16, 0xFFF1EFF5);
        text(selected.serverIP, x + 8, y + 14, w - 16, 0xFFAAAAAA);
        text(status(selected), x + 8, y + 28, w - 16, statusColor(selected));
        if (y + 60 < layout.detailBottom - 32) {
            text(tr("players") + ": " + safe(selected.populationInfo, "—"), x + 8, y + 44, w - 16, 0xFFCCCCCC);
            text(tr("version") + ": 1.0.4", x + 8, y + 57, w - 16, 0xFFAAAAAA);
        }
        int lineY = y + 76;
        for (Object line : fontRendererObj.listFormattedStringToWidth(safe(selected.serverMOTD, ""), Math.max(16, w - 16))) {
            if (lineY + 10 >= layout.detailBottom - 34) break;
            drawString(fontRendererObj, (String) line, x + 8, lineY, 0xFFBBBBBB);
            lineY += 11;
        }
    }
    private String status(ServerData server) {
        if (pings == null || pings.pending(server)) return tr("pinging");
        return server.pingToServer < 0 ? tr("offline") : tr("online") + " · " + server.pingToServer + " " + tr("ms");
    }
    private int statusColor(ServerData server) {
        return pings == null || pings.pending(server) ? 0xFF999999 : server.pingToServer < 0 ? 0xFFD78383 : 0xFF8CCA9A;
    }
    private static String safe(String value, String fallback) { return value == null || value.isEmpty() ? fallback : value; }
    private void text(String value, int x, int y, int maxWidth, int color) {
        drawString(fontRendererObj, fontRendererObj.trimStringToWidth(value.replace('\n', ' '), Math.max(0, maxWidth)), x, y, color);
    }
    private static void frame(int x, int y, int right, int bottom, int color) {
        drawRect(x, y, right, y + 1, color); drawRect(x, bottom - 1, right, bottom, color);
        drawRect(x, y, x + 1, bottom, color); drawRect(right - 1, y, right, bottom, color);
    }
    private static void drawStar(int x, int y, boolean filled) {
        int color = filled ? MenuAppearance.accent() : 0xFF797B87;
        drawRect(x - 1, y - 5, x + 2, y + 5, color);
        drawRect(x - 5, y - 1, x + 6, y + 2, color);
        drawRect(x - 3, y - 3, x + 4, y + 4, color);
        if (!filled) drawRect(x - 1, y - 1, x + 2, y + 2, 0xFF20222D);
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        if (button.id >= 20 && button.id <= 22) {
            tab = button.id - 20; offset = 0;
            for (Object entry : buttonList) {
                GuiButton b = (GuiButton) entry;
                if (b.id >= 20 && b.id <= 22) b.displayString = (b.id - 20 == tab ? "> " : "")
                        + tr(b.id == 20 ? "all" : b.id == 21 ? "favorites" : "recent");
            }
            filter(); return;
        }
        switch (button.id) {
            case CONNECT: connect(selected); break;
            case BACK: mc.displayGuiScreen(parent); break;
            case REFRESH: initGui(); break;
            case DIRECT:
                action = DIRECT; pending = new ServerData("", "", false);
                mc.displayGuiScreen(new GuiScreenServerList(this, pending)); break;
            case ADD:
                action = ADD; pending = new ServerData("", "", false);
                mc.displayGuiScreen(new GuiScreenAddServer(this, pending)); break;
            case EDIT:
                if (selected == null) break;
                action = EDIT; editIndex = all.indexOf(selected); oldAddress = selected.serverIP;
                // Edit a copy: cancelling a dialog must not mutate the saved entry.
                pending = ServerData.getServerDataFromNBTCompound(selected.getNBTCompound());
                mc.displayGuiScreen(new GuiScreenAddServer(this, pending)); break;
            case DELETE:
                if (selected == null) break;
                action = DELETE; pending = selected; editIndex = all.indexOf(selected);
                mc.displayGuiScreen(new GuiYesNo(this, tr("delete_question"), selected.serverName, DELETE)); break;
            default: break;
        }
    }

    private void connect(ServerData server) {
        if (server == null) return;
        closePings();
        preferences.recordConnection(server.serverIP);
        // Do not construct GuiConnecting directly: Forge initializes its login latch here.
        FMLClientHandler.instance().connectToServer(this, server);
    }

    @Override public void confirmClicked(boolean accepted, int id) {
        ServerData value = pending;
        int previousAction = action;
        pending = null; action = 0;
        if (accepted && value != null) {
            if (previousAction == DIRECT) { connect(value); return; }
            if (previousAction == ADD) saved.addServerData(value);
            else if (previousAction == EDIT && editIndex >= 0 && editIndex < saved.countServers()) {
                saved.func_147413_a(editIndex, value);
                preferences.move(oldAddress, value.serverIP);
            } else if (previousAction == DELETE && editIndex >= 0 && editIndex < saved.countServers()) {
                saved.removeServerData(editIndex);
                preferences.remove(value.serverIP);
            }
            saved.saveServerList();
            selected = previousAction == DELETE ? null : value;
        }
        mc.displayGuiScreen(this);
    }

    @Override protected void keyTyped(char c, int key) {
        if (key == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) { connect(selected); return; }
        if (search.textboxKeyTyped(c, key)) { query = search.getText(); offset = 0; filter(); }
        else if (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN) {
            int index = filtered.indexOf(selected) + (key == Keyboard.KEY_UP ? -1 : 1);
            if (!filtered.isEmpty()) {
                index = Math.max(0, Math.min(filtered.size() - 1, index)); selected = filtered.get(index);
                if (index < offset) offset = index;
                if (index >= offset + layout.visibleRows) offset = index - layout.visibleRows + 1;
            }
        }
    }
    @Override protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        if (mc.currentScreen != this) return;
        search.mouseClicked(x, y, button);
        if (button != 0) return;
        if (filtered.size() > layout.visibleRows && x >= 12 + layout.listWidth - 5
                && x < 12 + layout.listWidth && y >= layout.top && y < layout.listBottom) {
            draggingScroll = true; scrollTo(y); return;
        }
        int index = layout.rowAt(x, y, offset, filtered.size());
        if (index < 0) return;
        ServerData server = filtered.get(index);
        selected = server;
        if (x >= 12 + layout.listWidth - 32) {
            preferences.toggleFavorite(server.serverIP);
            lastClicked = null; filter(); return;
        }
        long now = System.nanoTime();
        if (lastClicked == server && now - lastClick < 250000000L) { lastClicked = null; connect(server); }
        else { lastClicked = server; lastClick = now; }
    }
    @Override public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        int x = Mouse.getEventX() * width / mc.displayWidth;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        if (x < 12 || x >= 12 + layout.listWidth || y < layout.top || y >= layout.listBottom) return;
        offset = Math.max(0, Math.min(Math.max(0, filtered.size() - layout.visibleRows), offset + (wheel < 0 ? 1 : -1)));
    }
    private void scrollTo(int y) {
        int max = Math.max(0, filtered.size() - layout.visibleRows);
        offset = Math.max(0, Math.min(max, (y - layout.top) * max / Math.max(1, layout.listBottom - layout.top - 1)));
    }
    @Override protected void mouseClickMove(int x, int y, int button, long elapsed) {
        super.mouseClickMove(x, y, button, elapsed);
        if (draggingScroll && button == 0) scrollTo(y);
    }
    @Override protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        if (button == 0) draggingScroll = false;
    }
    @Override public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        closePings(); releaseIcons();
    }
    private void closePings() { if (pings != null) { pings.close(); pings = null; } }
    private void releaseIcons() {
        for (Icon icon : icons.values()) if (icon.location != null) mc.getTextureManager().deleteTexture(icon.location);
        icons.clear();
    }
    private void drawIcon(ServerData server, int x, int y, int size) {
        ResourceLocation texture = icon(server);
        if (texture == null) {
            drawRect(x, y, x + size, y + size, 0xFF121419);
            frame(x + 3, y + 3, x + size - 3, y + size - 3, MenuAppearance.accent());
            drawRect(x + 7, y + 8, x + size - 7, y + 11, 0xFF777777);
            drawRect(x + 7, y + 15, x + size - 7, y + 18, 0xFF777777);
            return;
        }
        mc.getTextureManager().bindTexture(texture); GL11.glColor4f(1, 1, 1, 1);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x, y + size, zLevel, 0, 1);
        tess.addVertexWithUV(x + size, y + size, zLevel, 1, 1);
        tess.addVertexWithUV(x + size, y, zLevel, 1, 0);
        tess.addVertexWithUV(x, y, zLevel, 0, 0); tess.draw();
    }
    private ResourceLocation icon(ServerData server) {
        String data = server.getBase64EncodedIconData();
        if (data == null || data.length() > 131072) return null;
        Icon previous = icons.get(server);
        if (previous != null && data.equals(previous.data)) return previous.location;
        if (previous != null && previous.location != null) mc.getTextureManager().deleteTexture(previous.location);
        if (previous == null && icons.size() >= 64) {
            Iterator<Icon> oldest = icons.values().iterator();
            Icon removed = oldest.next(); oldest.remove();
            if (removed.location != null) mc.getTextureManager().deleteTexture(removed.location);
        }
        ResourceLocation location = null;
        try {
            byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(data);
            try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    try {
                        reader.setInput(input);
                        // Vanilla server icons are 64x64. Inspect headers before decoding pixels.
                        if (reader.getWidth(0) == 64 && reader.getHeight(0) == 64) {
                            BufferedImage image = reader.read(0);
                            location = mc.getTextureManager().getDynamicTextureLocation("htgt6_server_icon", new DynamicTexture(image));
                        }
                    } finally { reader.dispose(); }
                }
            }
        } catch (Exception ignored) { }
        icons.put(server, new Icon(data, location));
        return location;
    }
    private static final class Icon {
        final String data; final ResourceLocation location;
        Icon(String data, ResourceLocation location) { this.data = data; this.location = location; }
    }
}
