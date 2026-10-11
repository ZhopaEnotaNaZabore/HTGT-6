package com.mod.htgt6.client.loading;

import com.mod.htgt6.asm.HTGT6Config;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;

/** Applies HiTech6 branding as soon as the native LWJGL window exists. */
public final class HTGT6WindowBranding {

    private static final Logger LOGGER = LogManager.getLogger("HTGT6-WindowBranding");
    private static final String ICON = "/assets/htgt6/textures/window/icon.png";
    private static boolean applied;

    private HTGT6WindowBranding() {}

    public static synchronized void apply() {
        if (applied || !HTGT6Config.ENABLE_WINDOW_BRANDING || !Display.isCreated()) return;
        try {
            Display.setTitle(HTGT6Config.WINDOW_TITLE);
            InputStream stream = HTGT6WindowBranding.class.getResourceAsStream(ICON);
            if (stream == null) throw new IllegalStateException("Missing window icon " + ICON);
            try {
                BufferedImage source = ImageIO.read(stream);
                if (source == null) throw new IllegalStateException("Unreadable window icon " + ICON);
                Display.setIcon(iconSet(source));
            } finally {
                stream.close();
            }
            applied = true;
            LOGGER.info("Applied early HiTech6 window title and icon");
        } catch (Throwable error) {
            LOGGER.error("Could not apply early HiTech6 window branding", error);
        }
    }

    private static ByteBuffer[] iconSet(BufferedImage source) {
        String os = System.getProperty("os.name", "").toUpperCase();
        if (os.contains("WIN")) {
            return new ByteBuffer[] {resize(source, 16), resize(source, 32)};
        }
        if (os.contains("MAC")) return new ByteBuffer[] {resize(source, 128)};
        return new ByteBuffer[] {resize(source, 32)};
    }

    private static ByteBuffer resize(BufferedImage source, int size) {
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, size, size, null);
        graphics.dispose();

        int[] pixels = scaled.getRGB(0, 0, size, size, null, 0, size);
        ByteBuffer buffer = BufferUtils.createByteBuffer(size * size * 4);
        for (int pixel : pixels) {
            buffer.put((byte) (pixel >> 16 & 255));
            buffer.put((byte) (pixel >> 8 & 255));
            buffer.put((byte) (pixel & 255));
            buffer.put((byte) (pixel >> 24 & 255));
        }
        buffer.flip();
        return buffer;
    }
}
