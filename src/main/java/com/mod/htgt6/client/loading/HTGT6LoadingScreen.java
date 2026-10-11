package com.mod.htgt6.client.loading;

import com.mod.htgt6.asm.HTGT6Config;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

/**
 * Early, dependency-free loading renderer owned by HTGT6.
 *
 * <p>The class deliberately uses LWJGL and classpath PNGs directly. Minecraft's
 * resource manager and FontRenderer are not fully constructed when the first
 * loading frame is shown.</p>
 */
public final class HTGT6LoadingScreen {

    private static final Logger LOGGER = LogManager.getLogger("HTGT6-LoadingScreen");
    private static final String ROOT = "/assets/htgt6/textures/loading/";
    private static final long TIP_INTERVAL_MS = 5000L;
    private static final Object LOCK = new Object();

    private static boolean active;
    private static boolean failed;
    private static float progress = 0.01F;
    private static long startedAt;
    private static Texture background;
    private static Texture logo;
    private static Texture indicators;
    private static Texture secondaryLogo;
    private static Texture mojangLogo;
    private static Texture launchText;
    private static Texture versionText;
    private static Texture gmcText;
    private static Texture percentText;
    private static Texture tipText;
    private static String renderedPercent;
    private static String renderedTip;
    private static String[] tips;

    private HTGT6LoadingScreen() {}

    /** Called from the transformed Minecraft.startGame after Display.create(). */
    public static void start() {
        synchronized (LOCK) {
            if (!HTGT6Config.ENABLE_NEW_FML_LOADING_SCREEN || active || failed) return;
            try {
                if (!Display.isCreated()) return;
                background = loadTexture(ROOT + "background_industry.png", false);
                logo = loadTexture(ROOT + "logo.png", false);
                indicators = loadTexture(ROOT + "progress_indicators.png", false);
                secondaryLogo = loadTexture(ROOT + "secondary_logo.png", false, true);
                mojangLogo = loadTexture("/assets/minecraft/textures/gui/title/mojang.png", true);
                tips = loadTips();
                launchText = createTextTexture("Запуск", 34, Font.BOLD);
                versionText = createTextTexture("V 1.0.4 Build 13", 23, Font.PLAIN);
                gmcText = createTextTexture("GMC", 25, Font.BOLD);
                progress = 0.01F;
                startedAt = System.currentTimeMillis();
                active = true;
                renderLocked();
                LOGGER.info("HTGT6 loading screen started");
            } catch (Throwable error) {
                fail(error);
            }
        }
    }

    /** Receives a normalized, monotonic lifecycle percentage. */
    public static void setProgress(float value) {
        synchronized (LOCK) {
            if (!active || failed) return;
            progress = Math.max(progress, clamp(value, 0.01F, 0.99F));
            try {
                renderLocked();
            } catch (Throwable error) {
                fail(error);
            }
        }
    }

    /** Repaints the current frame when vanilla asks for its temporary screen. */
    public static void renderCurrent() {
        synchronized (LOCK) {
            if (!active || failed) return;
            try {
                renderLocked();
            } catch (Throwable error) {
                fail(error);
            }
        }
    }

    /** Called before FML displays the main menu or a loading error. */
    public static void finish() {
        synchronized (LOCK) {
            if (!active) return;
            try {
                progress = 1.0F;
                renderLocked();
            } catch (Throwable error) {
                LOGGER.warn("Unable to render the final HTGT6 loading frame", error);
            } finally {
                active = false;
                disposeTextures();
                LOGGER.info("HTGT6 loading screen finished");
            }
        }
    }

    private static void renderLocked() {
        if (!Display.isCreated() || background == null || indicators == null) return;
        int width = Math.max(1, Display.getWidth());
        int height = Math.max(1, Display.getHeight());
        float scale = Math.min(width / 1672.0F, height / 941.0F);
        int previousFramebuffer = 0;
        boolean framebufferSupported = GLContext.getCapabilities().GL_EXT_framebuffer_object;
        if (framebufferSupported) {
            previousFramebuffer = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glViewport(0, 0, width, height);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, width, height, 0.0D, -1.0D, 1.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glClearColor(0.01F, 0.025F, 0.04F, 1.0F);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        draw(background, 0, 0, width, height);

        float logoWidth = width * 0.40F;
        float logoHeight = logoWidth * logo.height / logo.width;
        draw(logo, (width - logoWidth) * 0.5F, height * 0.065F, logoWidth, logoHeight);

        float barWidth = width * 0.46F;
        float barHeight = Math.max(42.0F * scale, height * 0.055F);
        float barX = (width - barWidth) * 0.5F;
        float barY = height * 0.505F;
        drawRegion(indicators, barX, barY, barWidth, barHeight,
                20.0F / 2164.0F, 145.0F / 727.0F, 2048.0F / 2164.0F, 365.0F / 727.0F);

        float fillInsetX = barWidth * 0.035F;
        float fillWidth = (barWidth - fillInsetX * 2.0F) * progress;
        float fillY = barY + barHeight * 0.36F;
        float fillHeight = barHeight * 0.29F;
        float fillU0 = 24.0F / 2164.0F;
        float fillU1 = fillU0 + (2024.0F / 2164.0F) * progress;
        drawRegion(indicators, barX + fillInsetX, fillY, fillWidth, fillHeight,
                fillU0, 420.0F / 727.0F, fillU1, 545.0F / 727.0F);

        drawScaledText(launchText, barX + barWidth * 0.18F,
                barY - Math.max(42.0F * scale, 28.0F), Math.max(0.75F, scale));
        String percent = Math.round(progress * 100.0F) + "%";
        if (!percent.equals(renderedPercent)) {
            delete(percentText);
            percentText = createTextTexture(percent, 34, Font.BOLD);
            renderedPercent = percent;
        }
        drawScaledText(percentText, barX + barWidth * 0.78F,
                barY - Math.max(42.0F * scale, 28.0F), Math.max(0.75F, scale));

        float tipX = width * 0.145F;
        float tipY = height * 0.755F;
        float tipWidth = width * 0.71F;
        float tipHeight = height * 0.135F;
        drawPanel(tipX, tipY, tipWidth, tipHeight, scale);
        String tip = currentTip();
        if (!tip.equals(renderedTip)) {
            delete(tipText);
            tipText = createTextTexture(tip, 25, Font.PLAIN);
            renderedTip = tip;
        }
        drawCenteredText(tipText, tipX + tipWidth * 0.5F, tipY + tipHeight * 0.52F,
                tipWidth - Math.max(42.0F, 70.0F * scale), Math.max(0.62F, scale));

        float footerY = height * 0.925F;
        drawScaledText(versionText, width * 0.028F, footerY, Math.max(0.62F, scale));
        float iconHeight = Math.max(36.0F, 55.0F * scale);
        float catWidth = iconHeight * secondaryLogo.width / secondaryLogo.height;
        float gmcScale = Math.max(0.62F, scale);
        float gmcWidth = gmcText.width * gmcScale;
        float catX = width - width * 0.025F - catWidth - gmcWidth - 18.0F * scale;
        float iconY = height - iconHeight - height * 0.018F;
        draw(secondaryLogo, catX, iconY, catWidth, iconHeight);
        drawScaledText(gmcText, catX + catWidth + 8.0F * scale,
                iconY + (iconHeight - gmcText.height * gmcScale) * 0.5F, gmcScale);
        float mojangHeight = iconHeight * 0.58F;
        float mojangWidth = mojangHeight * mojangLogo.width / mojangLogo.height;
        draw(mojangLogo, catX - mojangWidth - 22.0F * scale,
                iconY + (iconHeight - mojangHeight) * 0.5F, mojangWidth, mojangHeight);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopAttrib();
        Display.update();
        if (framebufferSupported) {
            EXTFramebufferObject.glBindFramebufferEXT(
                    EXTFramebufferObject.GL_FRAMEBUFFER_EXT, previousFramebuffer);
        }
    }

    private static void drawPanel(float x, float y, float width, float height, float scale) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.015F, 0.035F, 0.05F, 0.90F);
        quad(x, y, x + width, y + height);
        float border = Math.max(1.0F, 2.0F * scale);
        GL11.glColor4f(0.1F, 0.82F, 0.92F, 0.82F);
        quad(x, y, x + width, y + border);
        quad(x, y + height - border, x + width, y + height);
        quad(x, y, x + border, y + height);
        quad(x + width - border, y, x + width, y + height);
        float corner = Math.max(9.0F, 17.0F * scale);
        GL11.glColor4f(0.95F, 0.60F, 0.08F, 0.92F);
        quad(x + border, y + border, x + border * 3.0F, y + corner);
        quad(x + width - border * 3.0F, y + border, x + width - border, y + corner);
        quad(x + border, y + height - corner, x + border * 3.0F, y + height - border);
        quad(x + width - border * 3.0F, y + height - corner,
                x + width - border, y + height - border);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1, 1, 1, 1);
    }

    private static void quad(float x0, float y0, float x1, float y1) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x0, y0);
        GL11.glVertex2f(x1, y0);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x0, y1);
        GL11.glEnd();
    }

    private static void draw(Texture texture, float x, float y, float width, float height) {
        drawRegion(texture, x, y, width, height, 0, 0, 1, 1);
    }

    private static void drawRegion(Texture texture, float x, float y, float width, float height,
            float u0, float v0, float u1, float v1) {
        if (texture == null || width <= 0 || height <= 0) return;
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.id);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(u0, v0); GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(u1, v0); GL11.glVertex2f(x + width, y);
        GL11.glTexCoord2f(u1, v1); GL11.glVertex2f(x + width, y + height);
        GL11.glTexCoord2f(u0, v1); GL11.glVertex2f(x, y + height);
        GL11.glEnd();
    }

    private static void drawScaledText(Texture texture, float x, float y, float scale) {
        if (texture != null) draw(texture, x, y, texture.width * scale, texture.height * scale);
    }

    private static void drawCenteredText(Texture texture, float centerX, float centerY,
            float availableWidth, float preferredScale) {
        if (texture == null) return;
        float scale = Math.min(preferredScale, availableWidth / texture.width);
        float width = texture.width * scale;
        float height = texture.height * scale;
        draw(texture, centerX - width * 0.5F, centerY - height * 0.5F, width, height);
    }

    private static Texture loadTexture(String path, boolean prepareMojang) throws Exception {
        return loadTexture(path, prepareMojang, false);
    }

    private static Texture loadTexture(String path, boolean prepareMojang,
            boolean prepareSecondaryLogo) throws Exception {
        InputStream stream = HTGT6LoadingScreen.class.getResourceAsStream(path);
        if (stream == null) throw new IllegalStateException("Missing loading resource " + path);
        try {
            BufferedImage image = ImageIO.read(stream);
            if (image == null) throw new IllegalStateException("Unreadable loading resource " + path);
            if (prepareMojang) image = prepareMojangLogo(image);
            if (prepareSecondaryLogo) image = prepareSecondaryLogo(image);
            return upload(image);
        } finally {
            stream.close();
        }
    }

    /**
     * The supplied GMC icon was captured on top of the old loading background.
     * Keep its pixels intact, but mask everything outside its rounded frame.
     */
    private static BufferedImage prepareSecondaryLogo(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int frameRight = Math.max(0, width - 4);
        int frameBottom = Math.max(0, height - 6);
        float radius = Math.min(frameRight + 1, frameBottom + 1) * 0.30F;
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float nearestX = Math.max(radius, Math.min(frameRight - radius, x));
                float nearestY = Math.max(radius, Math.min(frameBottom - radius, y));
                float dx = x - nearestX;
                float dy = y - nearestY;
                boolean inside = x <= frameRight && y <= frameBottom
                        && dx * dx + dy * dy <= radius * radius;
                result.setRGB(x, y, inside ? source.getRGB(x, y) : 0);
            }
        }
        return result;
    }

    private static BufferedImage prepareMojangLogo(BufferedImage source) {
        int minX = source.getWidth(), minY = source.getHeight(), maxX = 0, maxY = 0;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int r = rgb >> 16 & 255, g = rgb >> 8 & 255, b = rgb & 255;
                if (r < 245 || g < 245 || b < 245) {
                    minX = Math.min(minX, x); minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
                }
            }
        }
        if (minX > maxX || minY > maxY) return source;
        BufferedImage result = new BufferedImage(maxX - minX + 5, maxY - minY + 5,
                BufferedImage.TYPE_INT_ARGB);
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                int rgb = source.getRGB(x, y);
                int r = rgb >> 16 & 255, g = rgb >> 8 & 255, b = rgb & 255;
                int alpha = (r > 245 && g > 245 && b > 245) ? 0 : 255;
                if (alpha != 0 && r < 105 && g < 105 && b < 105) r = g = b = 245;
                result.setRGB(x - minX + 2, y - minY + 2,
                        alpha << 24 | r << 16 | g << 8 | b);
            }
        }
        return result;
    }

    private static Texture createTextTexture(String text, int size, int style) {
        Font font = new Font("SansSerif", style, size);
        BufferedImage measure = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = measure.createGraphics();
        graphics.setFont(font);
        FontMetrics metrics = graphics.getFontMetrics();
        int width = Math.max(2, metrics.stringWidth(text) + 8);
        int height = Math.max(2, metrics.getHeight() + 8);
        graphics.dispose();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setFont(font);
        int baseline = 4 + graphics.getFontMetrics().getAscent();
        graphics.setColor(new Color(0, 0, 0, 210));
        graphics.drawString(text, 5, baseline + 2);
        graphics.setColor(Color.WHITE);
        graphics.drawString(text, 3, baseline);
        graphics.dispose();
        return upload(image);
    }

    private static Texture upload(BufferedImage image) {
        int width = image.getWidth(), height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        for (int pixel : pixels) {
            buffer.put((byte) (pixel >> 16 & 255));
            buffer.put((byte) (pixel >> 8 & 255));
            buffer.put((byte) (pixel & 255));
            buffer.put((byte) (pixel >> 24 & 255));
        }
        buffer.flip();
        int id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        return new Texture(id, width, height);
    }

    private static String[] loadTips() {
        List<String> values = new ArrayList<String>();
        InputStream stream = HTGT6LoadingScreen.class.getResourceAsStream(
                "/assets/htgt6/loading_tips_ru.txt");
        if (stream != null) {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) values.add(line);
                }
                reader.close();
            } catch (Exception error) {
                LOGGER.warn("Unable to read HTGT6 loading tips", error);
            }
        }
        if (values.isEmpty()) values.add("Автоматизируйте производство как можно раньше.");
        return values.toArray(new String[values.size()]);
    }

    private static String currentTip() {
        int index = (int) ((System.currentTimeMillis() - startedAt) / TIP_INTERVAL_MS % tips.length);
        return "Подсказка: " + tips[index];
    }

    private static void disposeTextures() {
        delete(background); delete(logo); delete(indicators); delete(secondaryLogo);
        delete(mojangLogo); delete(launchText); delete(versionText); delete(gmcText);
        delete(percentText); delete(tipText);
        background = logo = indicators = secondaryLogo = mojangLogo = null;
        launchText = versionText = gmcText = percentText = tipText = null;
    }

    private static void delete(Texture texture) {
        if (texture != null && Display.isCreated()) GL11.glDeleteTextures(texture.id);
    }

    private static void fail(Throwable error) {
        if (!failed) LOGGER.error("HTGT6 loading screen disabled after a rendering failure", error);
        failed = true;
        active = false;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class Texture {
        final int id;
        final int width;
        final int height;

        Texture(int id, int width, int height) {
            this.id = id;
            this.width = width;
            this.height = height;
        }
    }
}
