package com.mod.htgt6.client.vanillatweaks.CHAT;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;

/** Network/decode work runs off-thread; texture creation/deletion only happens while drawing. */
final class ChatImageCache {
    private static final Map<String, Image> CACHE = new LinkedHashMap<String, Image>(32, .75f, true);
    private static final ThreadPoolExecutor WORKERS = new ThreadPoolExecutor(2, 2, 30, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(24), task -> {
                Thread thread = new Thread(task, "HTGT6-chat-image"); thread.setDaemon(true); return thread;
            });
    private static volatile boolean clearRequested;
    static final class Image {
        volatile BufferedImage decoded;
        volatile String error;
        volatile boolean discarded;
        DynamicTexture texture;
        int width, height;
    }
    private ChatImageCache() {}
    static void clear() { clearRequested = true; }

    private static final Image CLEANING = new Image();
    static { CLEANING.error = "Очистка кеша..."; }

    // Called once per client END tick, even with the chat closed. At most two
    // GL deletions per tick; no worker ever owns texture creation or deletion.
    static void maintain() {
        if (!clearRequested) return;
        WORKERS.getQueue().clear();
        Iterator<Image> it = CACHE.values().iterator();
        for (int count = 0; count < 2 && it.hasNext(); count++) {
            release(it.next()); it.remove();
        }
        if (CACHE.isEmpty()) clearRequested = false;
    }
    static Image get(final String url) {
        // Do not reload images into the cache while it is being drained.
        if (clearRequested) return CLEANING;
        Image image = CACHE.get(url);
        if (image == null) {
            if (CACHE.size() >= 24) {
                Iterator<Image> it = CACHE.values().iterator(); release(it.next()); it.remove();
            }
            final Image target = image = new Image();
            CACHE.put(url, image);
            try {
                WORKERS.execute(() -> {
                    if (target.discarded) return;
                    try {
                        BufferedImage decoded = download(url);
                        if (!target.discarded) target.decoded = decoded;
                    } catch (Exception failure) { target.error = "Изображение недоступно"; }
                });
            } catch (java.util.concurrent.RejectedExecutionException busy) { target.error = "Очередь загрузки заполнена"; }
        }
        BufferedImage decoded = image.decoded;
        if (decoded != null) {
            image.width = decoded.getWidth(); image.height = decoded.getHeight();
            // MC 1.7.10 TextureManager.deleteTexture leaves the object (and pixel array) in its map.
            // Own these transient textures directly so eviction frees both GPU and Java memory.
            image.texture = new DynamicTexture(decoded);
            image.decoded = null;
        }
        return image;
    }
    private static void release(Image image) {
        image.discarded = true; image.decoded = null;
        if (image.texture != null) { image.texture.deleteGlTexture(); image.texture = null; }
    }

    static void checkAddress(URL url) throws Exception {
        ChatImageHttp.checkAddress(url);
    }
    private static BufferedImage download(String source) throws Exception {
        return decode(ChatImageHttp.download(source));
    }
    static BufferedImage decode(byte[] bytes) throws Exception {
        try (ImageInputStream stream = new javax.imageio.stream.MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new java.io.IOException("Not an image");
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName();
                if (!("png".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)))
                    throw new java.io.IOException("Unsupported image format");
                reader.setInput(stream, true, true);
                int w = reader.getWidth(0), h = reader.getHeight(0);
                if (w < 1 || h < 1 || w > 8192 || h > 8192 || (long) w * h > 16000000L)
                    throw new java.io.IOException("Image dimensions exceeded");
                javax.imageio.ImageReadParam param = reader.getDefaultReadParam();
                int sample = Math.max(1, (Math.max(w, h) + 1023) / 1024);
                param.setSourceSubsampling(sample, sample, 0, 0);
                return reader.read(0, param);
            } finally { reader.dispose(); }
        }
    }
    static void draw(String url, int x, int y, int width, int height) {
        Image image = get(url);
        Gui.drawRect(x, y, x + width, y + height, 0x80303A40);
        if (image.texture == null) {
            Minecraft.getMinecraft().fontRenderer.drawString(image.error == null ? "Загрузка изображения..." : image.error,
                    x + 5, y + 5, 0xAFC5CB); return;
        }
        double scale = Math.min(width / (double) image.width, height / (double) image.height);
        int w = Math.max(1, (int) (image.width * scale)), h = Math.max(1, (int) (image.height * scale));
        x += (width - w) / 2; y += (height - h) / 2;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        try {
            GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); GL11.glColor4f(1, 1, 1, 1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, image.texture.getGlTextureId());
            Tessellator t = Tessellator.instance; t.startDrawingQuads();
            t.addVertexWithUV(x, y + h, 0, 0, 1); t.addVertexWithUV(x + w, y + h, 0, 1, 1);
            t.addVertexWithUV(x + w, y, 0, 1, 0); t.addVertexWithUV(x, y, 0, 0, 0); t.draw();
        } finally { GL11.glPopAttrib(); }
    }
}
