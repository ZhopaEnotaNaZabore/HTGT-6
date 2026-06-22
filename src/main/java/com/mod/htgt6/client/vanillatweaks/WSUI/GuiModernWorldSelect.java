package com.mod.htgt6.client.vanillatweaks.WSUI;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.SaveFormatComparator;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@SideOnly(Side.CLIENT)
public class GuiModernWorldSelect extends GuiScreen implements GuiYesNoCallback {

    private final GuiScreen parent;

    private GuiTextField searchField;

    private GuiWorldList worldList;

    private final List<WorldEntry> allWorlds =
            new ArrayList<WorldEntry>();

    public final List<WorldEntry> filteredWorlds =
            new ArrayList<WorldEntry>();

    public int selectedIndex = -1;

    public GuiModernWorldSelect(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {

        Keyboard.enableRepeatEvents(true);

        this.buttonList.clear();

        searchField = new GuiTextField(
                fontRendererObj,
                width / 2 - 180,
                18,
                360,
                22
        );

        searchField.setMaxStringLength(100);

        buttonList.add(new GuiButton(
                0,
                width / 2 - 155,
                height - 52,
                100,
                20,
                "С ноги в игру"
        ));

        buttonList.add(new GuiButton(
                1,
                width / 2 - 50,
                height - 52,
                100,
                20,
                "Создать новый мир"
        ));

        buttonList.add(new GuiButton(
                2,
                width / 2 + 55,
                height - 52,
                100,
                20,
                "Удалить мир"
        ));

        buttonList.add(new GuiButton(
                3,
                width / 2 - 50,
                height - 28,
                100,
                20,
                "Назад в меню"
        ));

        loadWorlds();

        worldList = new GuiWorldList(
                this,
                mc,
                filteredWorlds
        );
    }

    private void loadWorlds() {

        allWorlds.clear();

        try {

            ISaveFormat saveLoader =
                    mc.getSaveLoader();

            List saves =
                    saveLoader.getSaveList();

            Collections.sort(saves);

            for (Object obj : saves) {

                SaveFormatComparator save =
                        (SaveFormatComparator) obj;

                File worldDir = new File(
                        Minecraft.getMinecraft().mcDataDir,
                        "saves/" + save.getFileName()
                );

                WorldEntry entry = new WorldEntry();

                entry.folderName = save.getFileName();
                entry.displayName = save.getDisplayName();
                entry.lastPlayed = save.getLastTimePlayed();

                loadPlayerData(entry, worldDir);
                loadPreview(entry, worldDir);

                allWorlds.add(entry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        updateSearch();
    }

    private void loadPlayerData(
            WorldEntry entry,
            File worldDir
    ) {

        try {

            File levelDat =
                    new File(worldDir, "level.dat");

            if (!levelDat.exists()) {
                return;
            }

            NBTTagCompound root =
                    CompressedStreamTools.readCompressed(
                            new FileInputStream(levelDat)
                    );

            NBTTagCompound data =
                    root.getCompoundTag("Data");

            if (data.hasKey("Player")) {

                NBTTagCompound player =
                        data.getCompoundTag("Player");

                NBTTagList pos =
                        player.getTagList("Pos", 6);

                entry.posX = pos.func_150309_d(0);
                entry.posY = pos.func_150309_d(1);
                entry.posZ = pos.func_150309_d(2);
            }

            int gameType = data.getInteger("GameType");

            switch (gameType) {

                case 1:
                    entry.gameMode = "Creative";
                    break;

                case 2:
                    entry.gameMode = "Adventure";
                    break;

                default:
                    entry.gameMode = "Survival";
            }

            entry.version = "1.7.10";

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPreview(WorldEntry entry, File worldDir) {
        try {
            File icon = new File(worldDir, "preview.png");
            if (!icon.exists()) {
                icon = new File(worldDir, "icon.png");
            }

            if (!icon.exists()) {
                return;
            }

            BufferedImage image = ImageIO.read(icon);
            DynamicTexture texture = new DynamicTexture(image);

            // FIX: Append the lastPlayed timestamp to the texture name so Minecraft doesn't use the old cached version!
            String uniqueTextureName = "world_" + entry.folderName + "_" + entry.lastPlayed;

            entry.previewTexture = mc.getTextureManager()
                    .getDynamicTextureLocation(uniqueTextureName, texture);

        } catch (Exception ignored) {
        }
    }

    private void updateSearch() {

        filteredWorlds.clear();

        String text =
                searchField.getText().toLowerCase();

        for (WorldEntry world : allWorlds) {

            String display =
                    world.displayName == null
                            ? ""
                            : world.displayName.toLowerCase();

            String folder =
                    world.folderName.toLowerCase();

            if (display.contains(text)
                    || folder.contains(text)) {

                filteredWorlds.add(world);
            }
        }
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (searchField.textboxKeyTyped(c, key)) {
            updateSearch();
            // Reset selection index when filtering so you don't accidentally select out of bounds
            selectedIndex = -1;
        } else {
            super.keyTyped(c, key);
        }
    }

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int button
    ) {

        super.mouseClicked(mouseX, mouseY, button);

        searchField.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }



    @Override
    protected void actionPerformed(
            GuiButton button
    ) {

        switch (button.id) {

            case 0:
                playSelectedWorld();
                break;

            case 1:
                mc.displayGuiScreen(
                        new GuiCreateWorld(this)
                );
                break;

            case 2:

                if (selectedIndex >= 0
                        && selectedIndex < filteredWorlds.size()) {

                    WorldEntry world =
                            filteredWorlds.get(selectedIndex);

                    mc.displayGuiScreen(
                            new GuiYesNo(
                                    this,
                                    "Delete World",
                                    "Delete '"
                                            + world.displayName
                                            + "'?",
                                    selectedIndex
                            )
                    );
                }

                break;

            case 3:
                mc.displayGuiScreen(parent);
                break;
        }
    }

    public void playSelectedWorld() {

        if (selectedIndex < 0
                || selectedIndex >= filteredWorlds.size()) {
            return;
        }

        WorldEntry world =
                filteredWorlds.get(selectedIndex);

        mc.launchIntegratedServer(
                world.folderName,
                world.displayName,
                null
        );
    }

    @Override
    public void confirmClicked(
            boolean result,
            int id
    ) {

        if (result) {

            if (id >= 0
                    && id < filteredWorlds.size()) {

                WorldEntry world =
                        filteredWorlds.get(id);

                mc.getSaveLoader()
                        .deleteWorldDirectory(
                                world.folderName
                        );

                selectedIndex = -1;

                loadWorlds();
            }
        }

        mc.displayGuiScreen(this);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 1. Draw the dirt background first
        drawDefaultBackground();

        // 2. Draw the scrolling list SECOND, so it sits in the background
        worldList.drawScreen(mouseX, mouseY, partialTicks);

        // 3. Draw the title and search bar ON TOP of the list's shadows
        drawCenteredString(fontRendererObj, "Выбрать мир для игры", width / 2, 6, 0xFFFFFF);
        searchField.drawTextBox();

        // 4. Finally, draw the buttons (this is what super does)
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void drawWorldCard(
            WorldEntry world,
            int y,
            boolean selected
    ) {
        // Exact sizing from your image goal
        int x = 20;
        int w = width - 40;
        int h = 64;

        // Draw the card background rectangle
        drawRect(
                x,
                y,
                x + w,
                y + h,
                selected ? 0xAA666666 : 0x44222222 // Darker transparent grey background
        );

        // Render Preview Thumbnail (Icon)
        if (world.previewTexture != null) {
            mc.getTextureManager().bindTexture(world.previewTexture);
            GL11.glColor4f(1, 1, 1, 1);
            drawTexturedRect(x + 4, y + 4, 56, 56);
        }

        // Left Info Block
        drawString(fontRendererObj, world.displayName, x + 70, y + 8, 0xFFFFFF);
        drawString(fontRendererObj, "Folder: " + world.folderName, x + 70, y + 24, 0xBBBBBB);
        drawString(
                fontRendererObj,
                "Pos: " + (int) world.posX + " " + (int) world.posY + " " + (int) world.posZ,
                x + 70,
                y + 38,
                0xBBBBBB
        );

        // Right Info Block (Anchored safely left of the scrollbar)
        int rightAlignX = x + w - 180;

        drawString(fontRendererObj, "Last played:", rightAlignX, y + 20, 0x888888);

        String lastPlayed = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(world.lastPlayed));
        drawString(fontRendererObj, lastPlayed, rightAlignX, y + 34, 0xAAAAAA);
    }

    private void drawTexturedRect(
            int x,
            int y,
            int width,
            int height
    ) {

        Tessellator tess =
                Tessellator.instance;

        tess.startDrawingQuads();

        tess.addVertexWithUV(
                x,
                y + height,
                zLevel,
                0,
                1
        );

        tess.addVertexWithUV(
                x + width,
                y + height,
                zLevel,
                1,
                1
        );

        tess.addVertexWithUV(
                x + width,
                y,
                zLevel,
                1,
                0
        );

        tess.addVertexWithUV(
                x,
                y,
                zLevel,
                0,
                0
        );

        tess.draw();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
}