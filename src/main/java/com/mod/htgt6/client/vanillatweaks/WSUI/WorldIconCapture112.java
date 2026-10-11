package com.mod.htgt6.client.vanillatweaks.WSUI;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** Captures the clean last gameplay frame and writes the 1.9+ world icon.png. */
public final class WorldIconCapture112 {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-WorldIcon112");
    private static BufferedImage lastFrame;

    private WorldIconCapture112() { }

    public static void clear() {
        lastFrame = null;
    }

    public static void capture(Minecraft minecraft) {
        try {
            lastFrame = resizeToIcon(readFramebuffer(minecraft));
        } catch (RuntimeException error) {
            lastFrame = null;
            LOGGER.warn("Unable to capture the world icon", error);
        }
    }

    public static void save(Minecraft minecraft) {
        if (lastFrame == null || minecraft.getIntegratedServer() == null) return;
        File temporary = null;
        try {
            File saves = new File(minecraft.mcDataDir, "saves").getCanonicalFile();
            File world = new File(saves, minecraft.getIntegratedServer().getFolderName()).getCanonicalFile();
            String savesPrefix = saves.getPath() + File.separator;
            if (!world.getPath().startsWith(savesPrefix)) {
                throw new IllegalArgumentException("World folder is outside the saves directory");
            }
            if (!world.isDirectory() && !world.mkdirs()) {
                throw new IllegalStateException("Cannot create world directory " + world);
            }

            File icon = new File(world, "icon.png");
            temporary = new File(world, "icon.png.tmp");
            if (!ImageIO.write(lastFrame, "png", temporary)) {
                throw new IllegalStateException("No PNG image writer is available");
            }
            try {
                Files.move(temporary.toPath(), icon.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporary.toPath(), icon.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception error) {
            LOGGER.warn("Unable to save the world icon.png", error);
            if (temporary != null && temporary.isFile() && !temporary.delete()) {
                temporary.deleteOnExit();
            }
        } finally {
            lastFrame = null;
        }
    }

    private static BufferedImage readFramebuffer(Minecraft minecraft) {
        Framebuffer framebuffer = minecraft.getFramebuffer();
        int textureWidth = minecraft.displayWidth;
        int textureHeight = minecraft.displayHeight;
        if (OpenGlHelper.isFramebufferEnabled()) {
            textureWidth = framebuffer.framebufferTextureWidth;
            textureHeight = framebuffer.framebufferTextureHeight;
        }

        int count = Math.multiplyExact(textureWidth, textureHeight);
        IntBuffer buffer = BufferUtils.createIntBuffer(count);
        int[] pixels = new int[count];
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        if (OpenGlHelper.isFramebufferEnabled()) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture);
            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA,
                    GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
        } else {
            GL11.glReadPixels(0, 0, textureWidth, textureHeight, GL12.GL_BGRA,
                    GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
        }
        buffer.get(pixels);
        TextureUtil.func_147953_a(pixels, textureWidth, textureHeight);

        int width = OpenGlHelper.isFramebufferEnabled() ? framebuffer.framebufferWidth : textureWidth;
        int height = OpenGlHelper.isFramebufferEnabled() ? framebuffer.framebufferHeight : textureHeight;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        if (OpenGlHelper.isFramebufferEnabled()) {
            int top = textureHeight - height;
            for (int y = 0; y < height; y++) {
                image.setRGB(0, y, width, 1, pixels, (y + top) * textureWidth, textureWidth);
            }
        } else {
            image.setRGB(0, 0, width, height, pixels, 0, width);
        }
        return image;
    }

    private static BufferedImage resizeToIcon(BufferedImage source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int sourceX = (source.getWidth() - side) / 2;
        int sourceY = (source.getHeight() - side) / 2;
        BufferedImage icon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = icon.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(source, 0, 0, 64, 64,
                    sourceX, sourceY, sourceX + side, sourceY + side, null);
        } finally {
            graphics.dispose();
        }
        return icon;
    }
}
