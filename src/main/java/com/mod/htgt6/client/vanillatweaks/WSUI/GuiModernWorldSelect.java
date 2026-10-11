package com.mod.htgt6.client.vanillatweaks.WSUI;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.SaveFormatComparator;
import net.minecraft.world.storage.WorldInfo;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/** Minecraft 1.12-style world selection adapted to the 1.7.10 save API. */
@SideOnly(Side.CLIENT)
public final class GuiModernWorldSelect extends GuiScreen implements GuiYesNoCallback {
    private static final int CONFIRM_DELETE = 100;
    private static final int CONFIRM_NEWER = 101;
    private final GuiScreen parent;
    private final List<WorldEntry> allWorlds = new ArrayList<WorldEntry>();
    public final List<WorldEntry> filteredWorlds = new ArrayList<WorldEntry>();
    public int selectedIndex = -1;
    private GuiWorldList worldList;
    private GuiTextField searchField;
    private GuiButton selectButton, editButton, deleteButton, recreateButton;
    private WorldEntry pendingEntry;
    private ResourceLocation fallbackIcon;
    private boolean launching;

    public GuiModernWorldSelect(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        searchField = new GuiTextField(fontRendererObj, width / 2 - 100, 22, 200, 20);
        searchField.setMaxStringLength(128);
        searchField.setText("");
        int y = height - 52;
        buttonList.add(selectButton = new GuiButton(1, width / 2 - 154, y, 150, 20, tr("selectWorld.select")));
        buttonList.add(new GuiButton(3, width / 2 + 4, y, 150, 20, tr("selectWorld.create")));
        y = height - 28;
        buttonList.add(editButton = new GuiButton(4, width / 2 - 154, y, 72, 20, tr("selectWorld.edit")));
        buttonList.add(deleteButton = new GuiButton(2, width / 2 - 76, y, 72, 20, tr("selectWorld.delete")));
        buttonList.add(recreateButton = new GuiButton(5, width / 2 + 4, y, 72, 20, tr("selectWorld.recreate")));
        buttonList.add(new GuiButton(0, width / 2 + 82, y, 72, 20, tr("gui.cancel")));
        loadWorlds();
        worldList = new GuiWorldList(this, mc, filteredWorlds);
        updateButtons();
    }

    @SuppressWarnings("unchecked")
    private void loadWorlds() {
        releaseIcons();
        createFallbackIcon();
        allWorlds.clear(); filteredWorlds.clear(); selectedIndex = -1;
        try {
            List<SaveFormatComparator> saves = mc.getSaveLoader().getSaveList();
            Collections.sort(saves);
            for (SaveFormatComparator save : saves) {
                File directory = new File(new File(mc.mcDataDir, "saves"), save.getFileName());
                WorldEntry entry = new WorldEntry(save, directory);
                loadIcon(entry);
                allWorlds.add(entry);
            }
        } catch (AnvilConverterException error) {
            mc.displayGuiScreen(new GuiErrorScreen(tr("selectWorld.unable_to_load"), error.getMessage()));
        }
        updateSearch();
    }

    private void loadIcon(WorldEntry entry) {
        if (!entry.iconFile.isFile()) return;
        try {
            BufferedImage image = ImageIO.read(entry.iconFile);
            if (image == null || image.getWidth() != 64 || image.getHeight() != 64) return;
            entry.iconTexture = mc.getTextureManager().getDynamicTextureLocation(
                    "worlds/" + entry.folderName + "/icon", new DynamicTexture(image));
        } catch (Exception ignored) { }
    }

    private void releaseIcons() {
        if (mc == null) return;
        for (WorldEntry entry : allWorlds) {
            if (entry.iconTexture != null) mc.getTextureManager().deleteTexture(entry.iconTexture);
            entry.iconTexture = null;
        }
        if (fallbackIcon != null) {
            mc.getTextureManager().deleteTexture(fallbackIcon);
            fallbackIcon = null;
        }
    }

    /** Guaranteed local fallback; 1.7.10 has no 1.12 unknown-world resource. */
    private void createFallbackIcon() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int color;
                if (y < 31) color = ((x + y) & 7) < 4 ? 0xFF6689B7 : 0xFF6183AE;
                else if (y < 37 - Math.abs(x - 32) / 5) color = 0xFF6C8D45;
                else color = ((x / 4 + y / 4) & 1) == 0 ? 0xFF735135 : 0xFF65472F;
                image.setRGB(x, y, color);
            }
        }
        fallbackIcon = mc.getTextureManager().getDynamicTextureLocation(
                "worlds/fallback", new DynamicTexture(image));
    }

    private void updateSearch() {
        filteredWorlds.clear();
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        for (WorldEntry entry : allWorlds) {
            String display = entry.displayName == null ? "" : entry.displayName.toLowerCase();
            if (display.contains(query) || entry.folderName.toLowerCase().contains(query))
                filteredWorlds.add(entry);
        }
    }

    private WorldEntry selected() {
        return selectedIndex < 0 || selectedIndex >= filteredWorlds.size() ? null : filteredWorlds.get(selectedIndex);
    }

    private void updateButtons() {
        boolean enabled = selected() != null && !launching;
        selectButton.enabled = editButton.enabled = deleteButton.enabled = recreateButton.enabled = enabled;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        WorldEntry entry = selected();
        switch (button.id) {
            case 0: mc.displayGuiScreen(parent); break;
            case 1: play(entry, false); break;
            case 2:
                if (entry != null) { pendingEntry = entry; mc.displayGuiScreen(new GuiYesNo(this,
                        tr("selectWorld.deleteQuestion"), "'" + entry.displayName + "' " + tr("selectWorld.deleteWarning"), CONFIRM_DELETE)); }
                break;
            case 3: mc.displayGuiScreen(new GuiCreateWorld112(this)); break;
            case 4: if (entry != null) mc.displayGuiScreen(new GuiWorldEdit112(this, entry.folderName)); break;
            case 5: recreate(entry); break;
            default: break;
        }
    }

    public void playSelectedWorld() { play(selected(), false); }

    private void play(WorldEntry entry, boolean warningAccepted) {
        if (entry == null || launching) return;
        if (entry.isFromNewerVersion() && !warningAccepted) {
            pendingEntry = entry;
            mc.displayGuiScreen(new GuiYesNo(this, tr("selectWorld.versionQuestion"),
                    tr("selectWorld.versionWarning", entry.versionName), tr("selectWorld.versionJoinButton"),
                    tr("gui.cancel"), CONFIRM_NEWER));
            return;
        }
        if (!mc.getSaveLoader().canLoadWorld(entry.folderName)) return;
        launching = true;
        mc.launchIntegratedServer(entry.folderName, entry.displayName, null);
    }

    private void recreate(WorldEntry entry) {
        if (entry == null) return;
        WorldInfo info = mc.getSaveLoader().getWorldInfo(entry.folderName);
        if (info == null) return;
        GuiCreateWorld112 create = new GuiCreateWorld112(this);
        create.func_146318_a(info);
        mc.displayGuiScreen(create);
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        WorldEntry entry = pendingEntry; pendingEntry = null;
        if (result && id == CONFIRM_DELETE && entry != null) {
            mc.getSaveLoader().flushCache();
            mc.getSaveLoader().deleteWorldDirectory(entry.folderName);
            mc.displayGuiScreen(this);
        } else if (result && id == CONFIRM_NEWER) {
            mc.displayGuiScreen(this); play(entry, true);
        } else mc.displayGuiScreen(this);
    }

    @Override
    protected void keyTyped(char character, int key) {
        if (searchField.textboxKeyTyped(character, key)) {
            updateSearch(); selectedIndex = -1; updateButtons();
        } else if (key == Keyboard.KEY_RETURN && selected() != null) playSelectedWorld();
        else super.keyTyped(character, key);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        searchField.mouseClicked(x, y, button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partial) {
        worldList.drawScreen(mouseX, mouseY, partial);
        drawCenteredString(fontRendererObj, tr("selectWorld.title"), width / 2, 8, 0xFFFFFF);
        searchField.drawTextBox();
        updateButtons();
        super.drawScreen(mouseX, mouseY, partial);
    }

    public void drawWorldEntry(WorldEntry entry, int y, boolean selected) {
        int x = width / 2 - 154;
        if (selected) drawRect(x - 2, y - 2, x + 306, y + 34, 0x80FFFFFF);
        // A screen transition may close this GUI between GuiSlot's update and
        // final draw pass. Recreate the released fallback before that late pass.
        if (fallbackIcon == null) createFallbackIcon();
        ResourceLocation texture = entry.iconTexture == null ? fallbackIcon : entry.iconTexture;
        if (texture != null) {
            mc.getTextureManager().bindTexture(texture);
            GL11.glColor4f(1, 1, 1, 1);
            drawTexture(x, y, 32, 32);
        } else {
            // Minecraft can render the old screen for the remainder of the
            // frame after onGuiClosed() has already released its dynamic
            // textures. Never pass a null ResourceLocation to bindTexture.
            drawRect(x, y, x + 32, y + 16, 0xFF6285B2);
            drawRect(x, y + 16, x + 32, y + 20, 0xFF6C8D45);
            drawRect(x, y + 20, x + 32, y + 32, 0xFF6A4B32);
        }
        String name = entry.displayName == null || entry.displayName.isEmpty()
                ? tr("selectWorld.world") : entry.displayName;
        String date = entry.folderName + " (" + DateFormat.getDateTimeInstance().format(new Date(entry.lastPlayed)) + ")";
        String mode = entry.conversionRequired ? tr("selectWorld.conversion") + " " : "";
        mode += entry.hardcore ? EnumChatFormatting.DARK_RED + tr("gameMode.hardcore") + EnumChatFormatting.RESET
                : tr("gameMode." + entry.gameMode);
        if (entry.cheats) mode += ", " + tr("selectWorld.cheats");
        mode += ", " + tr("selectWorld.version") + " "
                + (entry.isFromNewerVersion() ? EnumChatFormatting.RED : "") + entry.versionName;
        fontRendererObj.drawString(name, x + 35, y + 1, 0xFFFFFF);
        fontRendererObj.drawString(date, x + 35, y + 12, 0x808080);
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(mode, 270), x + 35, y + 23, 0x808080);
    }

    private void drawTexture(int x, int y, int width, int height) {
        Tessellator t = Tessellator.instance; t.startDrawingQuads();
        t.addVertexWithUV(x, y + height, zLevel, 0, 1); t.addVertexWithUV(x + width, y + height, zLevel, 1, 1);
        t.addVertexWithUV(x + width, y, zLevel, 1, 0); t.addVertexWithUV(x, y, zLevel, 0, 0); t.draw();
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); releaseIcons(); }

    private static String tr(String key, Object... args) { return I18n.format(key, args); }
}
