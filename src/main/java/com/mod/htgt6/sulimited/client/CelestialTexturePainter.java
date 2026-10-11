package com.mod.htgt6.sulimited.client;

import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceTechExpandedRegistry;
import com.mod.htgt6.sulimited.galacticraft.Glise792.sysGliese792;
import com.mod.htgt6.sulimited.galacticraft.Sirius.SiriusSystem;
import com.mod.htgt6.sulimited.galacticraft.CataloguedStarRegistry;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor.MinorWorlds;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Creates coloured celestial-map icons from reusable white index textures.
 * The source texture controls detail and alpha; a three-colour ramp controls
 * the appearance of a specific star or planet.
 */
public final class CelestialTexturePainter {
    private static final String INDEX_PATH = "textures/gui/celestialbodies/index/";
    private static final int ROTATION_FRAMES = 12;
    private static final int MAP_ROTATION_FRAMES = 24;
    private static final int MAP_TEXTURE_SIZE = 64;
    private static final int SKY_TEXTURE_SIZE = 256;
    private static final int RA_ROTATION_FRAMES = 12;
    private static final String AMUNRA_ICON_PATH = "textures/gui/celestialbodies/";
    private static ResourceLocation[] starRotationFrames = new ResourceLocation[0];
    private static ResourceLocation[] planetBRotationFrames = new ResourceLocation[0];
    private static ResourceLocation[] planetCRotationFrames = new ResourceLocation[0];
    private static ResourceLocation[] starSkyFrames = new ResourceLocation[0];
    private static ResourceLocation[] planetBSkyFrames = new ResourceLocation[0];
    private static ResourceLocation[] planetCSkyFrames = new ResourceLocation[0];
    private static ResourceLocation[] atmosphereGlowFrames = new ResourceLocation[0];
    private static ResourceLocation[] siriusARotationFrames = new ResourceLocation[0];
    private static ResourceLocation[] siriusBRotationFrames = new ResourceLocation[0];
    private static ResourceLocation[] pulsarRotationFrames = new ResourceLocation[0];
    private static int lastStarFrame = -1;
    private static int lastPlanetBFrame = -1;
    private static int lastPlanetCFrame = -1;
    private static int lastSiriusAFrame = -1;
    private static int lastSiriusBFrame = -1;
    private static int lastPulsarFrame = -1;
    private static final Map<String, ResourceLocation[]> raRotationFrames = new HashMap<String, ResourceLocation[]>();
    private static final Map<String, ResourceLocation[]> raSkyFrames = new HashMap<String, ResourceLocation[]>();
    private static final Map<String, Integer> raLastFrames = new HashMap<String, Integer>();
    private static final Map<String, Integer> raFrameRates = new HashMap<String, Integer>();

    private static final RaTexture[] RA_TEXTURES = {
            new RaTexture("starRa", "sun-red2.png", "StarIndex.png", 2, true),
            new RaTexture("starAmun", "sun-blue.png", "StarIndex.png", 3, true),
            new RaTexture("osiris", "planet-mercury.png", "RockyPlanetIndex.png", 4, false),
            new RaTexture("horus", "planet-horus.png", "RockyPlanetIndex.png", 4, false),
            new RaTexture("baal", "planet-gas03.png", "GasGiantIndex.png", 6, false),
            new RaTexture("baalRings", "micromoon.png", "RockyPlanetIndex.png", 3, false),
            new RaTexture("khonsu", "moon.png", "RockyPlanetIndex.png", 4, false),
            new RaTexture("neper", "planet-life-o2.png", "OceanPlanetIndex.png", 5, false),
            new RaTexture("iah", "moon.png", "RockyPlanetIndex.png", 5, false),
            new RaTexture("asteroidBeltMehen", "micromoon.png", "RockyPlanetIndex.png", 3, false),
            new RaTexture("sekhmet", "planet-gas02.png", "GasGiantIndex.png", 7, false),
            new RaTexture("bast", "moon.png", "RockyPlanetIndex.png", 4, false),
            new RaTexture("maahes", "planet-life-ch4.png", "OceanPlanetIndex.png", 5, false),
            new RaTexture("thoth", "moon.png", "RockyPlanetIndex.png", 5, false),
            new RaTexture("seth", "planet-ice2.png", "OceanPlanetIndex.png", 6, false),
            new RaTexture("anubis", "moon.png", "RockyPlanetIndex.png", 5, false),
            new RaTexture("kebe", "moon.png", "RockyPlanetIndex.png", 6, false)
    };

    private CelestialTexturePainter() {}

    public static void applyGliesePalette() {
        if (sysGliese792.starGliese792 == null || sysGliese792.planetGlieseB == null
                || sysGliese792.planetGlieseC == null) {
            return;
        }

        // Map frames rotate a single complete source texture.  They must not
        // combine a shifted colour layer with the previous source image.
        BufferedImage star = colourIndex(loadIndex("StarIndex.png"), 0x4A0000, 0xC51D14, 0xFFAA62);
        BufferedImage planetB = colourIndex(loadIndex("RockyPlanetIndex.png"), 0x220706, 0xA82A12, 0xFFC04A);
        BufferedImage planetC = colourIndex(loadIndex("OceanPlanetIndex.png"), 0x061A35, 0x08788F, 0x77E6D4);
        // These inputs are already projected discs, NOT longitude/latitude maps.
        // Keep their complete colour and alpha together; animate the quad instead
        // of wrapping transparent image margins into the middle of the sphere.
        starRotationFrames = singleDisc("gliese_792_red_dwarf", star);
        planetBRotationFrames = singleDisc("gliese_792_b_magma", planetB);
        planetCRotationFrames = singleDisc("gliese_792_c_ocean", planetC);
        starSkyFrames = starRotationFrames;
        planetBSkyFrames = planetBRotationFrames;
        planetCSkyFrames = planetCRotationFrames;
        atmosphereGlowFrames = createAtmosphereGlowFrames();
        siriusARotationFrames = createRotationFrames("sirius_a_white_dwarf",
                colourIndex(loadIndex("StarIndex.png"), 0x6F8FA7, 0xD8EEFF, 0xFFFFFF));
        siriusBRotationFrames = createRotationFrames("sirius_b_white_dwarf",
                colourIndex(loadIndex("StarIndex.png"), 0x44505C, 0xA9BBC6, 0xF5FBFF));
        pulsarRotationFrames = createRotationFrames("crab_pulsar_beam",
                colourIndex(loadIndex("PulsarIndex.png"), 0x10265C, 0x1595E8, 0xF2FDFF));
        applyCataloguedStarPalettes();
        updateGlieseRotation(0L);
    }

    /**
     * Builds the five outer-Sol map icons from one packaged neutral index.
     * This keeps authored colour variation without shipping one PNG per body.
     */
    public static void applyOuterSolPalettes() {
        setPlanetPalette(MinorWorlds.ariel, "sol_ariel", 0x56666D, 0xB9CACD, 0xF3FAF8);
        setPlanetPalette(MinorWorlds.haumea, "sol_haumea", 0x4C4844, 0xB8B0A2, 0xFFF9E8);
        setPlanetPalette(MinorWorlds.makemake, "sol_makemake", 0x3B1912, 0x9B4D35, 0xDEA27A);
        setPlanetPalette(MinorWorlds.eris, "sol_eris", 0x4C5058, 0xAAA9A2, 0xF0EBDD);
        setPlanetPalette(MinorWorlds.deeDee, "sol_deedee", 0x292725, 0x776B60, 0xC4B5A4);
    }

    private static void setPlanetPalette(CelestialBody body, String name,
                                         int shadow, int midtone, int highlight) {
        if (body != null) body.setBodyIcon(createTexture("RockyPlanetIndex.png", name,
                shadow, midtone, highlight));
    }

    /** Applies AmunRa's authored PNG palettes to reusable neutral index textures. */
    public static void applyRaPalette() {
        raRotationFrames.clear();
        raSkyFrames.clear();
        raLastFrames.clear();
        raFrameRates.clear();

        for (RaTexture texture : RA_TEXTURES) {
            CelestialBody body = findBody(texture.bodyName);
            if (body == null) continue;
            int[] palette = extractPalette(texture.originalTexture);
            BufferedImage coloured = colourIndex(loadIndex(texture.indexTexture),
                    palette[0], palette[1], palette[2]);
            String dynamicName = "ra_" + texture.bodyName.toLowerCase(Locale.ROOT);
            ResourceLocation[] mapFrames = createRotationFrames(dynamicName, coloured, RA_ROTATION_FRAMES);
            ResourceLocation[] skyFrames = texture.star
                    ? createStarPulseFrames(dynamicName, coloured)
                    : createSkyRotationFrames(dynamicName, coloured);
            raRotationFrames.put(normalize(texture.bodyName), mapFrames);
            raSkyFrames.put(normalize(texture.bodyName), skyFrames);
            raFrameRates.put(normalize(texture.bodyName), texture.frameRate);
            body.setBodyIcon(mapFrames[0]);
        }
        updateRaRotation(0L);
    }

    /** Advances only the visual spin; orbital positions remain GalaxySpace-owned. */
    public static void updateGlieseRotation(long worldTicks) {
        if (starRotationFrames.length == 0) return;
        lastStarFrame = setFrame(sysGliese792.starGliese792, starRotationFrames, worldTicks / 2L, lastStarFrame);
        lastPlanetBFrame = setFrame(sysGliese792.planetGlieseB, planetBRotationFrames, worldTicks / 3L, lastPlanetBFrame);
        lastPlanetCFrame = setFrame(sysGliese792.planetGlieseC, planetCRotationFrames, worldTicks / 4L, lastPlanetCFrame);
        lastSiriusAFrame = setFrame(SiriusSystem.starSiriusA, siriusARotationFrames, worldTicks / 3L, lastSiriusAFrame);
        lastSiriusBFrame = setFrame(SiriusSystem.planetSiriusB, siriusBRotationFrames, worldTicks / 5L, lastSiriusBFrame);
        lastPulsarFrame = setFrame(CataloguedStarRegistry.getStar("crab_pulsar"), pulsarRotationFrames,
                worldTicks, lastPulsarFrame);
        updateRaRotation(worldTicks);
    }

    private static void updateRaRotation(long worldTicks) {
        for (RaTexture texture : RA_TEXTURES) {
            String key = normalize(texture.bodyName);
            ResourceLocation[] frames = raRotationFrames.get(key);
            CelestialBody body = findBody(texture.bodyName);
            if (frames == null || body == null) continue;
            int previous = raLastFrames.containsKey(key) ? raLastFrames.get(key) : -1;
            raLastFrames.put(key, setFrame(body, frames, worldTicks / texture.frameRate, previous));
        }
    }

    /** Returns a longitude/pulse frame for AmunRa sky renderers, or null for unrelated bodies. */
    public static ResourceLocation getRaSkyFrame(CelestialBody body, long worldTicks) {
        if (body == null) return null;
        String key = normalize(body.getName());
        ResourceLocation[] frames = raSkyFrames.get(key);
        Integer rate = raFrameRates.get(key);
        return frames == null || rate == null ? null : getFrame(frames, worldTicks / rate);
    }

    /** Gliese uses intact discs; the renderer owns their continuous rotation. */
    public static ResourceLocation getSkyStarFrame(long worldTicks) {
        return getFrame(starSkyFrames, worldTicks / 10L);
    }

    public static ResourceLocation getSkyPlanetFrame(boolean oceanWorld, long worldTicks) {
        return getFrame(oceanWorld ? planetBSkyFrames : planetCSkyFrames, worldTicks / (oceanWorld ? 20L : 35L));
    }

    public static ResourceLocation getAtmosphereGlowFrame(long worldTicks) {
        return getFrame(atmosphereGlowFrames, worldTicks / 18L);
    }

    private static ResourceLocation getFrame(ResourceLocation[] frames, long index) {
        return frames.length == 0 ? null : frames[(int) Math.floorMod(index, (long) frames.length)];
    }

    private static int setFrame(micdoodle8.mods.galacticraft.api.galaxies.CelestialBody body,
                                ResourceLocation[] frames, long index, int previous) {
        if (body == null || frames.length == 0) return previous;
        int frame = (int) Math.floorMod(index, (long) frames.length);
        if (frame != previous) body.setBodyIcon(frames[frame]);
        return frame;
    }

    private static ResourceLocation createTexture(String indexName, String dynamicName,
                                                   int shadow, int midtone, int highlight) {
        BufferedImage index = loadIndex(indexName);
        BufferedImage coloured = new BufferedImage(index.getWidth(), index.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < index.getHeight(); y++) {
            for (int x = 0; x < index.getWidth(); x++) {
                int pixel = index.getRGB(x, y);
                int alpha = pixel >>> 24;
                if (alpha == 0) continue;

                int red = (pixel >>> 16) & 0xFF;
                int green = (pixel >>> 8) & 0xFF;
                int blue = pixel & 0xFF;
                float brightness = (red * 0.2126F + green * 0.7152F + blue * 0.0722F) / 255.0F;
                int colour = brightness < 0.5F
                        ? interpolate(shadow, midtone, brightness * 2.0F)
                        : interpolate(midtone, highlight, (brightness - 0.5F) * 2.0F);
                coloured.setRGB(x, y, (alpha << 24) | colour);
            }
        }

        return Minecraft.getMinecraft().getTextureManager()
                .getDynamicTextureLocation(dynamicName, new DynamicTexture(coloured));
    }

    /** Reuses the white StarIndex texture so the catalogue has consistent detail and alpha. */
    private static void applyCataloguedStarPalettes() {
        setStarPalette("uy_scuti", 0x3A0800, 0xB83A10, 0xFFD08A);
        setStarPalette("ae_andromedae", 0x173B86, 0x72B9FF, 0xF4FBFF);
        setStarPalette("af_andromedae", 0x283D9E, 0xB1D4FF, 0xFFFFFF);
        setStarPalette("romanos_star", 0x28479C, 0x9CCBFF, 0xFFFFFF);
        setStarPalette("m33_var_b", 0x1C4D92, 0x7EC8FF, 0xF8FDFF);
        setStarPalette("m33_b324", 0x713000, 0xD77A1C, 0xFFF0A5);
    }

    private static void setStarPalette(String starId, int shadow, int midtone, int highlight) {
        micdoodle8.mods.galacticraft.api.galaxies.Star star = CataloguedStarRegistry.getStar(starId);
        if (star != null) star.setBodyIcon(createTexture("StarIndex.png", "catalogued_" + starId,
                shadow, midtone, highlight));
    }

    private static ResourceLocation[] createRotationFrames(String dynamicName, BufferedImage source) {
        return createRotationFrames(dynamicName, source, MAP_ROTATION_FRAMES);
    }

    private static ResourceLocation[] singleDisc(String name, BufferedImage source) {
        BufferedImage disc = GlieseDiscAnimation.scaleDisc(source, 128);
        return new ResourceLocation[] { Minecraft.getMinecraft().getTextureManager()
                .getDynamicTextureLocation(name + "_intact_disc", new DynamicTexture(disc)) };
    }

    private static ResourceLocation[] createRotationFrames(String dynamicName, BufferedImage source, int frameCount) {
        BufferedImage scaled = new BufferedImage(MAP_TEXTURE_SIZE, MAP_TEXTURE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D scaler = scaled.createGraphics();
        scaler.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        scaler.drawImage(source, 0, 0, MAP_TEXTURE_SIZE, MAP_TEXTURE_SIZE, null);
        scaler.dispose();

        ResourceLocation[] frames = new ResourceLocation[frameCount];
        for (int frame = 0; frame < frameCount; frame++) {
            BufferedImage rotated = new BufferedImage(MAP_TEXTURE_SIZE, MAP_TEXTURE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = rotated.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.rotate(Math.PI * 2.0D * frame / frameCount,
                    MAP_TEXTURE_SIZE / 2.0D, MAP_TEXTURE_SIZE / 2.0D);
            graphics.drawImage(scaled, 0, 0, null);
            graphics.dispose();
            frames[frame] = Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation(dynamicName + "_spin_" + frame, new DynamicTexture(rotated));
        }
        return frames;
    }

    private static ResourceLocation[] createSkyRotationFrames(String dynamicName, BufferedImage source) {
        return createSkyRotationFrames(dynamicName, source, SKY_TEXTURE_SIZE, ROTATION_FRAMES);
    }

    private static ResourceLocation[] createSkyRotationFrames(String dynamicName, BufferedImage source, int size, int count) {
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D scaler = scaled.createGraphics();
        scaler.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        scaler.drawImage(source, 0, 0, size, size, null);
        scaler.dispose();

        ResourceLocation[] frames = new ResourceLocation[count];
        for (int frame = 0; frame < count; frame++) {
            BufferedImage view = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            int longitudeOffset = frame * size / count;
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    double dx = x - size / 2.0D;
                    double dy = y - size / 2.0D;
                    double distance = Math.sqrt(dx * dx + dy * dy) / (size / 2.0D);
                    if (distance >= 1.0D) continue;
                    int sourceX = (x + longitudeOffset) & (size - 1);
                    int pixel = scaled.getRGB(sourceX, y);
                    int alpha = (int) ((1.0D - Math.max(0.0D, distance - 0.88D) / 0.12D) * 255.0D);
                    int shade = (int) (90.0D + 165.0D * Math.max(0.0D, 1.0D - distance * distance));
                    int red = ((pixel >>> 16) & 0xFF) * shade / 255;
                    int green = ((pixel >>> 8) & 0xFF) * shade / 255;
                    int blue = (pixel & 0xFF) * shade / 255;
                    view.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
                }
            }
            frames[frame] = Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation(dynamicName + "_sky_spin_" + frame, new DynamicTexture(view));
        }
        return frames;
    }

    /** Airless worlds see the stellar disc directly: only heat colour and intensity pulse. */
    private static ResourceLocation[] createStarPulseFrames(String dynamicName, BufferedImage source) {
        return createStarPulseFrames(dynamicName, source, SKY_TEXTURE_SIZE, ROTATION_FRAMES);
    }

    private static ResourceLocation[] createStarPulseFrames(String dynamicName, BufferedImage source, int size, int count) {
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D scaler = scaled.createGraphics();
        scaler.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        scaler.drawImage(source, 0, 0, size, size, null);
        scaler.dispose();

        ResourceLocation[] frames = new ResourceLocation[count];
        for (int frame = 0; frame < count; frame++) {
            double pulse = 0.5D + 0.5D * Math.sin(Math.PI * 2.0D * frame / count);
            BufferedImage view = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
                int pixel = scaled.getRGB(x, y);
                int alpha = pixel >>> 24;
                int red = Math.min(255, (int) (((pixel >>> 16) & 0xFF) * (0.90D + pulse * 0.18D)));
                int green = Math.min(255, (int) (((pixel >>> 8) & 0xFF) * (0.76D + pulse * 0.40D)));
                int blue = Math.min(255, (int) ((pixel & 0xFF) * (0.70D + pulse * 0.25D)));
                view.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
            }
            frames[frame] = Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation(dynamicName + "_heat_pulse_" + frame, new DynamicTexture(view));
        }
        return frames;
    }

    /** The atmospheric planet sees scattered light, not a sharp stellar texture. */
    private static ResourceLocation[] createAtmosphereGlowFrames() {
        ResourceLocation[] frames = new ResourceLocation[1];
        for (int frame = 0; frame < 1; frame++) {
            double pulse = 0.5D + 0.5D * Math.sin(Math.PI * 2.0D * frame / 1);
            BufferedImage glow = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) {
                double dx = x - 128 / 2.0D;
                double dy = y - 128 / 2.0D;
                double distance = Math.sqrt(dx * dx + dy * dy) / (128 / 2.0D);
                if (distance >= 1.0D) continue;
                int alpha = (int) (Math.pow(1.0D - distance, 2.3D) * (88.0D + pulse * 42.0D));
                int red = 185 + (int) (pulse * 45.0D);
                int green = 74 + (int) (pulse * 48.0D);
                int blue = 38 + (int) (pulse * 30.0D);
                glow.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
            }
            frames[frame] = Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation("gliese_792_atmosphere_glow_" + frame, new DynamicTexture(glow));
        }
        return frames;
    }

    private static BufferedImage colourIndex(BufferedImage index, int shadow, int midtone, int highlight) {
        BufferedImage coloured = new BufferedImage(index.getWidth(), index.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < index.getHeight(); y++) {
            for (int x = 0; x < index.getWidth(); x++) {
                int pixel = index.getRGB(x, y);
                int alpha = pixel >>> 24;
                if (alpha == 0) continue;
                int red = (pixel >>> 16) & 0xFF;
                int green = (pixel >>> 8) & 0xFF;
                int blue = pixel & 0xFF;
                float brightness = (red * 0.2126F + green * 0.7152F + blue * 0.0722F) / 255.0F;
                int colour = brightness < 0.5F
                        ? interpolate(shadow, midtone, brightness * 2.0F)
                        : interpolate(midtone, highlight, (brightness - 0.5F) * 2.0F);
                coloured.setRGB(x, y, (alpha << 24) | colour);
            }
        }
        return coloured;
    }

    private static BufferedImage loadIndex(String indexName) {
        ResourceLocation location = new ResourceLocation("htgt6", INDEX_PATH + indexName);
        try (InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream()) {
            return ImageIO.read(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load celestial index texture " + location, exception);
        }
    }

    private static int[] extractPalette(String textureName) {
        ResourceLocation location = new ResourceLocation("amunra", AMUNRA_ICON_PATH + textureName);
        try (InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream()) {
            BufferedImage image = ImageIO.read(stream);
            List<ColourSample> samples = new ArrayList<ColourSample>();
            for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                if ((pixel >>> 24) == 0) continue;
                samples.add(new ColourSample(pixel & 0xFFFFFF));
            }
            if (samples.isEmpty()) return new int[]{0x202020, 0x909090, 0xFFFFFF};
            Collections.sort(samples, new Comparator<ColourSample>() {
                @Override public int compare(ColourSample first, ColourSample second) {
                    return Integer.compare(first.brightness, second.brightness);
                }
            });
            return new int[]{average(samples, 0.05F, 0.30F), average(samples, 0.375F, 0.625F),
                    average(samples, 0.70F, 0.95F)};
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to extract AmunRa palette from " + location, exception);
        }
    }

    private static int average(List<ColourSample> samples, float from, float to) {
        int start = Math.min(samples.size() - 1, (int) (samples.size() * from));
        int end = Math.max(start + 1, Math.min(samples.size(), (int) (samples.size() * to)));
        long red = 0L, green = 0L, blue = 0L;
        for (int index = start; index < end; index++) {
            int colour = samples.get(index).colour;
            red += colour >>> 16 & 0xFF;
            green += colour >>> 8 & 0xFF;
            blue += colour & 0xFF;
        }
        int count = end - start;
        return (int) (red / count) << 16 | (int) (green / count) << 8 | (int) (blue / count);
    }

    private static CelestialBody findBody(String name) {
        micdoodle8.mods.galacticraft.api.galaxies.Star secondary =
                SpaceTechExpandedRegistry.getSecondaryStar(name);
        if (secondary != null) return secondary;
        for (SolarSystem system : GalaxyRegistry.getRegisteredSolarSystems().values()) {
            if (system.getMainStar() != null && name.equalsIgnoreCase(system.getMainStar().getName())) {
                return system.getMainStar();
            }
        }
        for (Planet planet : GalaxyRegistry.getRegisteredPlanets().values()) {
            if (name.equalsIgnoreCase(planet.getName())) return planet;
        }
        for (Moon moon : GalaxyRegistry.getRegisteredMoons().values()) {
            if (name.equalsIgnoreCase(moon.getName())) return moon;
        }
        return null;
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private static final class RaTexture {
        private final String bodyName;
        private final String originalTexture;
        private final String indexTexture;
        private final int frameRate;
        private final boolean star;

        private RaTexture(String bodyName, String originalTexture, String indexTexture, int frameRate, boolean star) {
            this.bodyName = bodyName;
            this.originalTexture = originalTexture;
            this.indexTexture = indexTexture;
            this.frameRate = frameRate;
            this.star = star;
        }
    }

    private static final class ColourSample {
        private final int colour;
        private final int brightness;

        private ColourSample(int colour) {
            this.colour = colour;
            int red = colour >>> 16 & 0xFF;
            int green = colour >>> 8 & 0xFF;
            int blue = colour & 0xFF;
            this.brightness = (red * 54 + green * 183 + blue * 19) / 256;
        }
    }

    private static int interpolate(int first, int second, float progress) {
        progress = Math.max(0.0F, Math.min(1.0F, progress));
        int red = (int) (((first >>> 16 & 0xFF) * (1.0F - progress)) + ((second >>> 16 & 0xFF) * progress));
        int green = (int) (((first >>> 8 & 0xFF) * (1.0F - progress)) + ((second >>> 8 & 0xFF) * progress));
        int blue = (int) (((first & 0xFF) * (1.0F - progress)) + ((second & 0xFF) * progress));
        return red << 16 | green << 8 | blue;
    }
}
