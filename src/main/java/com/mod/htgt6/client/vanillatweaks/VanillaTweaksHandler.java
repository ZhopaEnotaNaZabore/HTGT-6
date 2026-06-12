package com.mod.htgt6.client.vanillatweaks;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

public class VanillaTweaksHandler {

    // Stores the clean 64x64 gameplay frame in memory temporary
    private BufferedImage cachedPreview = null;
    private boolean shouldCaptureNextFrame = false;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui == null) {
            return;
        }

        if (event.gui.getClass() == GuiSelectWorld.class) {
            System.out.println("Replacing world selection GUI");
            event.gui = new GuiModernWorldSelect(new GuiMainMenu());
        }

        // When the user opens the pause menu, mark it ready to capture the pure background
        if (event.gui instanceof GuiIngameMenu) {
            shouldCaptureNextFrame = true;
        }
    }

    @SubscribeEvent
    public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        // Intercept right before GuiIngameMenu draws its dark tint and buttons!
        if (event.gui instanceof GuiIngameMenu && shouldCaptureNextFrame) {
            shouldCaptureNextFrame = false; // Only capture once per pause session

            Minecraft mc = Minecraft.getMinecraft();
            if (mc.getIntegratedServer() != null) {
                // Take a clean snapshot of the game background and cache it
                cachedPreview = captureCleanFrame(mc);
            }
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.gui instanceof GuiIngameMenu) {
            // Button ID 1 is "Save and Quit to Title"
            if (event.button.id == 1) {
                Minecraft mc = Minecraft.getMinecraft();

                if (mc.getIntegratedServer() != null && cachedPreview != null) {
                    String folderName = mc.getIntegratedServer().getFolderName();
                    saveCachedPreview(mc, folderName);
                }
            }
        }
    }

    private BufferedImage captureCleanFrame(Minecraft mc) {
        try {
            int width = mc.displayWidth;
            int height = mc.displayHeight;

            // Extract the pure game world pixels from the active OpenGL context
            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int i = (x + (height - y - 1) * width) * 4;
                    int r = buffer.get(i) & 0xFF;
                    int g = buffer.get(i + 1) & 0xFF;
                    int b = buffer.get(i + 2) & 0xFF;
                    image.setRGB(x, y, (r << 16) | (g << 8) | b);
                }
            }

            // Crop to square
            int size = Math.min(width, height);
            int cropX = (width - size) / 2;
            int cropY = (height - size) / 2;
            BufferedImage croppedCard = image.getSubimage(cropX, cropY, size, size);

            // Downscale immediately to 64x64 so we only hold a tiny image in RAM
            BufferedImage finalThumbnail = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = finalThumbnail.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(croppedCard, 0, 0, 64, 64, null);
            graphics.dispose();

            return finalThumbnail;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveCachedPreview(Minecraft mc, String folderName) {
        try {
            File saveDir = new File(mc.mcDataDir, "saves/" + folderName);
            if (saveDir.exists() && cachedPreview != null) {
                File targetFile = new File(saveDir, "preview.png");
                ImageIO.write(cachedPreview, "png", targetFile);
                System.out.println("Saved pristine world preview layout for: " + folderName);
            }
        } catch (Exception e) {
            System.err.println("Failed to write cached preview image to disk.");
            e.printStackTrace();
        } finally {
            // Free memory up after saving
            cachedPreview = null;
        }
    }
}