package com.mod.htgt6.sulimited.client;

import galaxyspace.core.client.gui.screen.GSGuiCelestialSelection;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceMapObjectType;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.GalaxyMapForm;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceStarData;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceTechExpandedRegistry;
import com.mod.htgt6.sulimited.galacticraft.GalaxyCentreStarRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent;
import org.lwjgl.util.vector.Vector3f;
import galaxyspace.api.BodiesHelper;
import galaxyspace.core.configs.GSConfigCore;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Mouse;
import org.lwjgl.BufferUtils;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** GalaxySpace's original map frame with a galaxy-only first navigation level. */
public final class GuiGalaxyCatalogMap extends GSGuiCelestialSelection {
    private static final ResourceLocation SPIRAL_GALAXY_ICON =
            new ResourceLocation("htgt6", "textures/sulimited/map/galaxy_spiral.png");
    private static final ResourceLocation MILKY_WAY_ICON = galaxyIcon("milky_way");
    private static final ResourceLocation ANDROMEDA_ICON = galaxyIcon("andromeda");
    private static final ResourceLocation TRIANGLE_ICON = galaxyIcon("triangle");
    private static final ResourceLocation MICRO_ICON = galaxyIcon("micro");
    private final long rotationStartNanos = System.nanoTime();

    private boolean catalogMode = true;
    private final GalaxyCatalogCamera catalogCamera = new GalaxyCatalogCamera();
    private GalaxyMapCatalog.Entry selectedCatalogEntry;
    private long lastCatalogFrame;
    private int catalogWidth, catalogHeight;
    private float frameSeconds = .016F;
    private final Map<String, Float> hoverScales = new HashMap<String, Float>();
    private static final ResourceLocation NEBULA_ICON = galaxyIcon("nebula");
    private int catalogLastDragX = -1;
    private int catalogLastDragY = -1;
    private GalaxyMapCatalog.Entry activeGalaxyEntry;
    private final List<CelestialBody> allPossibleBodies;

    public GuiGalaxyCatalogMap(boolean mapMode, List<CelestialBody> possibleBodies, Integer[] tierData) {
        super(mapMode, withSpaceTechBodies(possibleBodies), tierData);
        List<CelestialBody> sourceBodies = this.possibleBodiesGS;
        if (sourceBodies == null) sourceBodies = this.possibleBodies;
        this.allPossibleBodies = sourceBodies == null
                ? Collections.<CelestialBody>emptyList()
                : new ArrayList<CelestialBody>(sourceBodies);
    }

    @Override
    public void drawCircles() {
        if (!catalogMode) {
            super.drawCircles();
            drawSecondaryStarOrbits();
        }
    }

    /**
     * GalaxySpace hard-codes star.sol to (0,0,0).  The catalogue reserves that
     * coordinate for a galaxy centre, so Sol must use its relocated standard
     * SolarSystem map position like every other star.
     */
    @Override
    protected Vector3f getCelestialBodyPosition(CelestialBody body) {
        if (!catalogMode && body instanceof Star && SpaceTechExpandedRegistry.isBinaryCompanion(body.getName())) {
            Star companion = (Star) body;
            SolarSystem system = companion.getParentSolarSystem();
            if (system != null && system.getMainStar() != null) {
                Integer tick = this.celestialBodyTicks.get(body);
                float animationTick = tick == null ? 1.0F : tick.floatValue();
                float orbitTime = Math.max(0.01F, body.getRelativeOrbitTime());
                float angle = animationTick / (200.0F * orbitTime) + body.getPhaseShift();
                float radius = getSecondaryStarOrbitRadius(body);
                Vector3f offset = new Vector3f((float) Math.sin(angle) * radius,
                        (float) Math.cos(angle) * radius, 0.0F);
                return Vector3f.add(offset, getCelestialBodyPosition(system.getMainStar()), null);
            }
        }
        if (!catalogMode && body instanceof Star && "star.sol".equalsIgnoreCase(body.getUnlocalizedName())) {
            SolarSystem system = ((Star) body).getParentSolarSystem();
            if (system != null && system.getMapPosition() != null) return system.getMapPosition().toVector3f();
        }
        return super.getCelestialBodyPosition(body);
    }

    private static List<CelestialBody> withSpaceTechBodies(List<CelestialBody> bodies) {
        List<CelestialBody> result = bodies == null
                ? new ArrayList<CelestialBody>() : new ArrayList<CelestialBody>(bodies);
        for (CelestialBody body : SpaceTechExpandedRegistry.getCustomMapBodies()) {
            if (!result.contains(body)) result.add(body);
        }
        return result;
    }

    private float getSecondaryStarOrbitRadius(CelestialBody body) {
        // GalaxySpace applies its planet multiplier only to Planet instances.
        // Preserve the previous visual orbit now that Amon is a real Star.
        return 3.0F * body.getRelativeDistanceFromCenter().unScaledDistance * 25.0F;
    }

    private void drawSecondaryStarOrbits() {
        GL11.glColor4f(0.32F, 0.65F, 0.82F, 0.55F);
        GL11.glLineWidth(1.5F);
        for (Star star : SpaceTechExpandedRegistry.getSecondaryStars()) {
            SolarSystem system = star.getParentSolarSystem();
            // allPossibleBodies is the unfiltered catalogue source and therefore
            // also contains Amon while another galaxy is open.  Orbit overlays
            // must use the active galaxy scope just like the star render pass.
            if (system == null || system.getMainStar() == null
                    || activeGalaxyEntry == null
                    || !galaxyKeysMatch(activeGalaxyEntry.galaxyKey,
                            system.getUnlocalizedParentGalaxyName())
                    || possibleBodiesGS == null || !possibleBodiesGS.contains(star)) continue;
            Vector3f centre = getCelestialBodyPosition(system.getMainStar());
            float radius = getSecondaryStarOrbitRadius(star);
            GL11.glPushMatrix();
            GL11.glTranslatef(centre.x, centre.y, centre.z);
            GL11.glBegin(GL11.GL_LINE_LOOP);
            for (int segment = 0; segment < 90; segment++) {
                double angle = Math.PI * 2.0D * segment / 90.0D;
                GL11.glVertex2f((float) Math.cos(angle) * radius, (float) Math.sin(angle) * radius);
            }
            GL11.glEnd();
            GL11.glPopMatrix();
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected String getParentName() {
        if (!catalogMode && activeGalaxyEntry != null) return I18n.format(activeGalaxyEntry.translationKey);
        return super.getParentName();
    }

    @Override
    public HashMap<CelestialBody, Matrix4f> drawCelestialBodies(Matrix4f matrix) {
        if (catalogMode) return new HashMap<CelestialBody, Matrix4f>();
        HashMap<CelestialBody, Matrix4f> rendered = super.drawCelestialBodies(matrix);
        for (CelestialBody body : SpaceTechExpandedRegistry.getCustomMapBodies()) {
            if (!allPossibleBodies.contains(body) || body.getBodyIcon() == null) continue;
            SolarSystem system = getParentSolarSystem(body);
            if (system == null || activeGalaxyEntry == null
                    || !galaxyKeysMatch(activeGalaxyEntry.galaxyKey, system.getUnlocalizedParentGalaxyName())) continue;
            renderSpaceTechBody(body, matrix, rendered);
        }
        return rendered;
    }

    /** GalaxySpace keeps the controls; SpaceTech owns this extensible render pass. */
    private void renderSpaceTechBody(CelestialBody body, Matrix4f worldMatrix,
                                     HashMap<CelestialBody, Matrix4f> rendered) {
        GL11.glPushMatrix();
        try {
            Matrix4f translated = new Matrix4f(worldMatrix);
            Matrix4f.translate(getCelestialBodyPosition(body), translated, translated);
            Matrix4f billboard = new Matrix4f();
            Matrix4f.rotate((float) Math.toRadians(readDoubleField("isometz")),
                    new Vector3f(0.0F, 0.0F, 1.0F), billboard, billboard);
            Matrix4f.rotate((float) -Math.toRadians(readDoubleField("isometx")),
                    new Vector3f(1.0F, 0.0F, 0.0F), billboard, billboard);
            Matrix4f.mul(translated, billboard, billboard);

            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            billboard.store(buffer);
            buffer.flip();
            GL11.glMultMatrix(buffer);

            CelestialBodyRenderEvent.Pre event = new CelestialBodyRenderEvent.Pre(body, body.getBodyIcon(), 8);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled()) {
                int width = getWidthForCelestialBodyStatic(body);
                if (body == selectedBody && selectionCount == 1) width = width / 2 * 3;
                if (body instanceof Star) renderSpaceTechStarGlow(body, width);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                if (event.celestialBodyTexture != null) mc.getTextureManager().bindTexture(event.celestialBodyTexture);
                drawTexturedModalRect1(-width / 2.0F, -width / 2.0F, width, width,
                        0.0F, 0.0F, event.textureSize, event.textureSize, false, false,
                        event.textureSize, event.textureSize);
                rendered.put(body, billboard);
            }
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new CelestialBodyRenderEvent.Post(body));
        } finally {
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    /** Additive stellar aura matching GalaxySpace's native star presentation. */
    private void renderSpaceTechStarGlow(CelestialBody star, int iconWidth) {
        float luminosity = Math.max(0.25F,
                SpaceTechExpandedRegistry.getStellarLuminosity(star.getName()));
        float lightScale = (float) Math.sqrt(luminosity);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(false);

        // A broad low-alpha halo remains visible outside the indexed star
        // texture even at the largest GalaxySpace zoom levels.
        float radius = Math.max(26.0F, iconWidth * 3.6F) * lightScale;
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glColor4f(0.20F, 0.72F, 1.0F, Math.min(0.72F, 0.46F * luminosity));
        GL11.glVertex2f(0.0F, 0.0F);
        GL11.glColor4f(0.04F, 0.30F, 1.0F, 0.0F);
        for (int segment = 0; segment <= 48; segment++) {
            double angle = Math.PI * 2.0D * segment / 48.0D;
            GL11.glVertex2f((float) Math.cos(angle) * radius, (float) Math.sin(angle) * radius);
        }
        GL11.glEnd();

        float innerRadius = Math.max(16.0F, iconWidth * 1.8F) * lightScale;
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glColor4f(0.55F, 0.92F, 1.0F, Math.min(0.85F, 0.58F * luminosity));
        GL11.glVertex2f(0.0F, 0.0F);
        GL11.glColor4f(0.12F, 0.55F, 1.0F, 0.0F);
        for (int segment = 0; segment <= 40; segment++) {
            double angle = Math.PI * 2.0D * segment / 40.0D;
            GL11.glVertex2f((float) Math.cos(angle) * innerRadius, (float) Math.sin(angle) * innerRadius);
        }
        GL11.glEnd();

        GL11.glDepthMask(true);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void drawButtons(int mouseX, int mouseY) {
        if (!catalogMode) {
            super.drawButtons(mouseX, mouseY);
            drawStarSystemHud(mouseX, mouseY);
            return;
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        drawCenteredString(fontRendererObj, I18n.format("sulimited.galaxy_map.select"), width / 2, 22, 0xD8EEFF);
        GalaxyMapCatalog.Entry hovered = hitCatalog(mouseX, mouseY);
        for (GalaxyMapCatalog.Entry entry : GalaxyMapCatalog.getEntries()) {
            float old = hoverScales.containsKey(entry.id) ? hoverScales.get(entry.id) : 1;
            float target = entry == hovered || entry == selectedCatalogEntry ? 1.25F : 1;
            hoverScales.put(entry.id, old + (target - old) * (1 - (float)Math.exp(-10 * frameSeconds)));
            drawGalaxyObject(catalogX(entry.mapX), catalogY(entry.mapY), entry, entry == hovered || entry == selectedCatalogEntry);
        }
        GalaxyMapCatalog.Entry info = hovered != null ? hovered : selectedCatalogEntry;
        if (info != null) drawGalaxyHud(info, catalogX(info.mapX), catalogY(info.mapY));
        drawCatalogButton(12, 12, 104, 30, I18n.format("sulimited.galaxy_map.overview"), mouseX, mouseY);
        drawCatalogButton(width - 80, 12, width - 12, 30, I18n.format("gui.done"), mouseX, mouseY);
        if (selectedCatalogEntry != null) {
            drawCatalogButton(width - 176, height - 58, width - 12, height - 30,
                    I18n.format("sulimited.galaxy_map.open"), mouseX, mouseY);
        }
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void drawCatalogButton(int x0, int y0, int x1, int y1, String label, int mx, int my) {
        boolean hover = mx >= x0 && mx < x1 && my >= y0 && my < y1;
        drawRect(x0, y0, x1, y1, hover ? 0xFF2599BE : 0xFF126588);
        drawRect(x0 + 2, y0 + 2, x1 - 2, y1 - 2, 0xEE06283E);
        drawCenteredString(fontRendererObj, label, (x0 + x1) / 2, (y0 + y1 - 8) / 2, hover ? 0xFFFF55 : 0xD8EEFF);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (catalogMode) {
            if (catalogWidth != width || catalogHeight != height) {
                catalogCamera.resize(width, height); catalogWidth = width; catalogHeight = height;
            }
            long now = System.nanoTime();
            frameSeconds = lastCatalogFrame == 0 ? .016F : Math.min(.1F, (now - lastCatalogFrame) / 1000000000F);
            lastCatalogFrame = now;
            int wheel = Mouse.getDWheel();
            if (wheel != 0) catalogCamera.wheel(wheel);
            catalogCamera.step(frameSeconds);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int catalogX(float relativeX) { return catalogCamera.screenX(relativeX, width); }
    private int catalogY(float relativeY) { return catalogCamera.screenY(relativeY, height); }
    private int catalogIconSize(GalaxyMapCatalog.Entry entry) {
        float hover = hoverScales.containsKey(entry.id) ? hoverScales.get(entry.id) : 1;
        return Math.round(Math.max(16, Math.min(128, 36 * catalogCamera.relativeZoom() * hover)));
    }
    private GalaxyMapCatalog.Entry hitCatalog(int mx, int my) {
        GalaxyMapCatalog.Entry best = null;
        double nearest = Double.MAX_VALUE;
        for (GalaxyMapCatalog.Entry entry : GalaxyMapCatalog.getEntries()) {
            double dx = mx - catalogX(entry.mapX), dy = my - catalogY(entry.mapY);
            double distance = dx * dx + dy * dy;
            double radius = Math.max(14, catalogIconSize(entry) * .55);
            if (distance <= radius * radius && distance < nearest) { best = entry; nearest = distance; }
        }
        return best;
    }

    private void drawGalaxyHud(GalaxyMapCatalog.Entry entry, int targetX, int targetY) {
        int panelWidth = 196;
        int panelHeight = entry.blackHoleName == null ? 58 : 72;
        boolean left = targetX > width - panelWidth - 36;
        int panelX = Math.max(12, Math.min(width - panelWidth - 12, left ? targetX - panelWidth - 28 : targetX + 28));
        int panelY = Math.max(42, Math.min(height - panelHeight - 12, targetY - panelHeight / 2));
        int edgeX = left ? panelX + panelWidth : panelX;

        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE80B1727);
        drawRect(panelX, panelY, panelX + panelWidth, panelY + 2, 0xFF197A9D);
        drawRect(panelX, panelY + panelHeight - 2, panelX + panelWidth, panelY + panelHeight, 0xFF197A9D);
        drawRect(panelX, panelY, panelX + 2, panelY + panelHeight, 0xFF197A9D);
        drawRect(panelX + panelWidth - 2, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF197A9D);
        drawRect(Math.min(targetX, edgeX), targetY, Math.max(targetX, edgeX) + 1, targetY + 1, 0xFF197A9D);
        drawRect(edgeX, Math.min(targetY, panelY + panelHeight / 2), edgeX + 1,
                Math.max(targetY, panelY + panelHeight / 2) + 1, 0xFF197A9D);

        drawString(fontRendererObj, I18n.format(entry.translationKey), panelX + 8, panelY + 8, 0xFFE8F7FF);
        drawString(fontRendererObj, I18n.format("sulimited.galaxy.hud.type") + " "
                + I18n.format(getFormInfoKey(entry.form)), panelX + 8, panelY + 25, 0xFF66C9F2);
        drawString(fontRendererObj, I18n.format("sulimited.galaxy.hud.stars", countRegisteredStars(entry.galaxyKey)),
                panelX + 8, panelY + 38, 0xFFFFFFFF);
        if (entry.blackHoleName != null) {
            drawString(fontRendererObj, I18n.format("sulimited.galaxy.hud.black_hole", entry.blackHoleName),
                    panelX + 8, panelY + 53, 0xFFD8B5FF);
            drawGalaxyCentre(panelX + panelWidth - 16, panelY + 55);
        }
    }

    /** HUD for stars already rendered by GalaxySpace's inner galaxy map. */
    private void drawStarSystemHud(int mouseX, int mouseY) {
        for (java.util.Map.Entry<CelestialBody, org.lwjgl.util.vector.Vector3f> entry : planetPosMap.entrySet()) {
            CelestialBody body = entry.getKey();
            boolean binaryCompanion = SpaceTechExpandedRegistry.isBinaryCompanion(body.getName());
            if (!(body instanceof Star) && !binaryCompanion) continue;
            org.lwjgl.util.vector.Vector3f position = entry.getValue();
            int starX = Math.round(position.x * width / (float) mc.displayWidth);
            int starY = Math.round(position.y * height / (float) mc.displayHeight);
            int radius = Math.max(10, Math.round(9.0F * position.z));
            if (Math.abs(mouseX - starX) > radius || Math.abs(mouseY - starY) > radius) continue;

            if (body instanceof Star && GalaxyCentreStarRegistry.isBlackHole((Star) body)) {
                drawBlackHoleHud((Star) body, starX, starY);
                return;
            }

            SolarSystem system = body instanceof Star ? ((Star) body).getParentSolarSystem() : null;
            BodiesHelper.BodiesData data = BodiesHelper.data.get(body);
            int planetCount = binaryCompanion ? 0 : system == null ? 0 : GalaxyRegistry.getPlanetsForSolarSystem(system).size();
            SpaceStarData starData = SpaceTechExpandedRegistry.getStarData(body.getName());
            if (starData == null) starData = SpaceTechExpandedRegistry.getStarData(body.getUnlocalizedName());
            String starType = getStarType(body, data, starData);
            String temperature = getStarTemperature(body, starData);
            drawStarHudPanel(body, starX, starY, starType, temperature, planetCount, binaryCompanion);
            return;
        }
    }

    private String getStarType(CelestialBody star, BodiesHelper.BodiesData bodyData, SpaceStarData starData) {
        if (starData != null) return I18n.format(starData.typeTranslationKey);
        String key = "star." + star.getName() + ".type";
        String localized = I18n.format(key);
        if (!key.equals(localized)) return localized;
        return bodyData == null ? I18n.format("sulimited.star.unknown") : I18n.format(bodyData.getClassPlanet());
    }

    private String getStarTemperature(CelestialBody star, SpaceStarData starData) {
        if (starData != null) return String.format(java.util.Locale.ROOT, "%.0f K", starData.temperatureKelvin);
        String key = "star." + star.getName() + ".temperature";
        String localized = I18n.format(key);
        return key.equals(localized) ? I18n.format("sulimited.star.unknown") : localized;
    }

    private void drawStarHudPanel(CelestialBody star, int targetX, int targetY, String starType,
                                  String temperature, int planetCount, boolean binaryCompanion) {
        int panelWidth = 194;
        int panelHeight = 72;
        boolean left = targetX > width - panelWidth - 36;
        int panelX = Math.max(12, Math.min(width - panelWidth - 12, left ? targetX - panelWidth - 28 : targetX + 28));
        int panelY = Math.max(42, Math.min(height - panelHeight - 12, targetY - panelHeight / 2));
        int edgeX = left ? panelX + panelWidth : panelX;

        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE80B1727);
        drawRect(panelX, panelY, panelX + panelWidth, panelY + 2, 0xFF197A9D);
        drawRect(panelX, panelY + panelHeight - 2, panelX + panelWidth, panelY + panelHeight, 0xFF197A9D);
        drawRect(panelX, panelY, panelX + 2, panelY + panelHeight, 0xFF197A9D);
        drawRect(panelX + panelWidth - 2, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF197A9D);
        drawRect(Math.min(targetX, edgeX), targetY, Math.max(targetX, edgeX) + 1, targetY + 1, 0xFF197A9D);
        drawRect(edgeX, Math.min(targetY, panelY + panelHeight / 2), edgeX + 1,
                Math.max(targetY, panelY + panelHeight / 2) + 1, 0xFF197A9D);

        drawString(fontRendererObj, star.getLocalizedName(), panelX + 8, panelY + 8, 0xFFE8F7FF);
        drawString(fontRendererObj, I18n.format("sulimited.star.hud.type") + " " + starType,
                panelX + 8, panelY + 26, 0xFF66C9F2);
        drawString(fontRendererObj, I18n.format("sulimited.star.hud.temperature") + " " + temperature,
                panelX + 8, panelY + 39, 0xFFFFD765);
        String finalLine = binaryCompanion
                ? I18n.format("sulimited.star.hud.binary_companion")
                : I18n.format("sulimited.star.hud.planets", planetCount);
        drawString(fontRendererObj, finalLine, panelX + 8, panelY + 53, 0xFFFFFFFF);
    }

    /** A galaxy centre is a Star for the renderer, but not a stellar destination. */
    private void drawBlackHoleHud(Star blackHole, int targetX, int targetY) {
        int panelWidth = 230;
        int panelHeight = 72;
        boolean left = targetX > width - panelWidth - 36;
        int panelX = Math.max(12, Math.min(width - panelWidth - 12, left ? targetX - panelWidth - 28 : targetX + 28));
        int panelY = Math.max(42, Math.min(height - panelHeight - 12, targetY - panelHeight / 2));
        int edgeX = left ? panelX + panelWidth : panelX;

        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE8081324);
        drawRect(panelX, panelY, panelX + panelWidth, panelY + 2, 0xFF9D5A19);
        drawRect(panelX, panelY + panelHeight - 2, panelX + panelWidth, panelY + panelHeight, 0xFF9D5A19);
        drawRect(panelX, panelY, panelX + 2, panelY + panelHeight, 0xFF9D5A19);
        drawRect(panelX + panelWidth - 2, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF9D5A19);
        drawRect(Math.min(targetX, edgeX), targetY, Math.max(targetX, edgeX) + 1, targetY + 1, 0xFF9D5A19);

        drawString(fontRendererObj, blackHole.getLocalizedName(), panelX + 8, panelY + 8, 0xFFFFE6B0);
        drawString(fontRendererObj, I18n.format("sulimited.black_hole.hud.type"), panelX + 8, panelY + 26, 0xFFFFB347);
        drawString(fontRendererObj, I18n.format("sulimited.black_hole.hud.mass",
                I18n.format("sulimited.black_hole.mass." + blackHole.getName())), panelX + 8, panelY + 39, 0xFFFFFFFF);
        drawString(fontRendererObj, I18n.format("sulimited.black_hole.hud.location"), panelX + 8, panelY + 53, 0xFF66C9F2);
    }

    private int countRegisteredStars(String galaxyKey) {
        int count = 0;
        for (SolarSystem system : GalaxyRegistry.getRegisteredSolarSystems().values()) {
            // Every compatible Galacticraft/GalaxySpace add-on ultimately
            // registers its systems here, so this also includes third-party
            // planets and their host stars without per-mod integrations.
            if (system.getMainStar() != null
                    && normalizeGalaxyKey(galaxyKey).equals(normalizeGalaxyKey(system.getUnlocalizedParentGalaxyName()))) {
                count++;
            }
        }
        for (Star star : SpaceTechExpandedRegistry.getSecondaryStars()) {
            SolarSystem system = star.getParentSolarSystem();
            if (system != null && normalizeGalaxyKey(galaxyKey)
                    .equals(normalizeGalaxyKey(system.getUnlocalizedParentGalaxyName()))) count++;
        }
        return count;
    }

    private String normalizeGalaxyKey(String key) {
        if (key == null) return "";
        String normalized = key.toLowerCase(java.util.Locale.ROOT);
        if (normalized.startsWith("galaxy.")) normalized = normalized.substring("galaxy.".length());
        return normalized.replaceAll("[^a-z0-9]", "");
    }

    private String getFormInfoKey(GalaxyMapForm form) {
        if (form == GalaxyMapForm.SPIRAL) return "sulimited.galaxy.form.spiral";
        if (form == GalaxyMapForm.NEBULA) return "sulimited.galaxy.form.nebula";
        return "sulimited.galaxy.form.other";
    }

    private void drawGalaxyCentre(int centerX, int centerY) {
        drawRect(centerX - 7, centerY - 2, centerX + 8, centerY + 3, 0x995F2A9D);
        drawRect(centerX - 2, centerY - 7, centerX + 3, centerY + 8, 0x995F2A9D);
        drawRect(centerX - 4, centerY - 4, centerX + 5, centerY + 5, 0xFF06020D);
        drawRect(centerX - 1, centerY - 1, centerX + 2, centerY + 2, 0xFF000000);
    }

    private void drawGalaxyObject(int centerX, int centerY, GalaxyMapCatalog.Entry entry, boolean hovered) {
        SpaceMapObjectType type = entry.type;
        int size = catalogIconSize(entry);
        int radius = size / 2;
        if (type == SpaceMapObjectType.NEBULA) {
            drawTexturedGalaxy(centerX, centerY, size, NEBULA_ICON);
        } else if (type == SpaceMapObjectType.CLUSTER) {
            drawSquare(centerX - radius, centerY - radius, radius * 2 + 1, 0x223E68FF);
            for (int i = 0; i < 10; i++) {
                double angle = (Math.PI * 2.0D * i) / 10.0D;
                int x = centerX + (int) (Math.cos(angle) * radius * 0.75D);
                int y = centerY + (int) (Math.sin(angle) * radius * 0.75D);
                drawSquare(x, y, 2, 0xFFD9F4FF);
            }
            drawSquare(centerX - 2, centerY - 2, 5, 0xFFFFFFFF);
        } else if (type == SpaceMapObjectType.GALAXY) {
            drawTexturedGalaxy(centerX, centerY, size, getGalaxyIcon(entry.id));
        } else {
            int colour = type == SpaceMapObjectType.BLACK_HOLE ? 0xFF9A58FF : 0xFF7FD8FF;
            drawSpiralGalaxy(centerX, centerY, radius, colour);
            drawSquare(centerX - 2, centerY - 2, 5, 0xFFFFFFFF);
        }
        if (hovered) {
            int border = radius + 3;
            drawRect(centerX - border, centerY - border, centerX + border + 1, centerY - border + 1, 0xFFFFFF55);
            drawRect(centerX - border, centerY + border, centerX + border + 1, centerY + border + 1, 0xFFFFFF55);
        }
    }

    private void drawSquare(int centerX, int centerY, int size, int colour) {
        int halfSize = size / 2;
        drawRect(centerX - halfSize, centerY - halfSize, centerX + halfSize + 1, centerY + halfSize + 1, colour);
    }

    /** Drawn in the same 2-D GUI pass as GalaxySpace's map markers. */
    private void drawSpiralGalaxy(int centerX, int centerY, int radius, int colour) {
        final int[][] arm = {
                {-2, -7}, {3, -6}, {7, -3}, {8, 1}, {6, 5}, {2, 7},
                {-3, 6}, {-7, 3}, {-8, -1}, {-6, -5}
        };
        int haloColour = (colour & 0x00FFFFFF) | 0x33000000;
        drawSquare(centerX, centerY, radius * 2 + 3, haloColour);
        for (int[] point : arm) {
            int x = centerX + point[0] * radius / 7;
            int y = centerY + point[1] * radius / 7;
            drawSquare(x, y, 3, (colour & 0x00FFFFFF) | 0xBB000000);
            drawSquare(centerX - point[0] * radius / 9, centerY - point[1] * radius / 9, 2,
                    (colour & 0x00FFFFFF) | 0x88000000);
        }
        drawSquare(centerX, centerY, 5, colour);
        drawSquare(centerX, centerY, 2, 0xFFFFFFFF);
    }

    /** Smooth geometry rotation keeps native 128px textures and hit areas independent. */
    private void drawTexturedGalaxy(int centerX, int centerY, int size, ResourceLocation icon) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            // Authored alpha keeps the dark dust lanes visible over the starfield.
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(icon);
            GL11.glTranslatef(centerX, centerY, 0);
            double seconds = (System.nanoTime() - rotationStartNanos) / 1000000000.0D;
            double period = icon == ANDROMEDA_ICON ? 90.0D : icon == MICRO_ICON ? 110.0D : 75.0D;
            GL11.glRotatef((float) ((seconds % period) * 360.0D / period), 0, 0, 1);
            float half = size * 0.5F;
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            t.addVertexWithUV(-half, half, 0, 0, 1);
            t.addVertexWithUV(half, half, 0, 1, 1);
            t.addVertexWithUV(half, -half, 0, 1, 0);
            t.addVertexWithUV(-half, -half, 0, 0, 0);
            t.draw();
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static ResourceLocation galaxyIcon(String id) {
        return new ResourceLocation("htgt6", "textures/sulimited/map/realistic/" + id + ".png");
    }

    private ResourceLocation getGalaxyIcon(String galaxyId) {
        if ("milky_way".equals(galaxyId)) return MILKY_WAY_ICON;
        if ("andromeda".equals(galaxyId)) return ANDROMEDA_ICON;
        if ("triangle".equals(galaxyId)) return TRIANGLE_ICON;
        if ("micro".equals(galaxyId)) return MICRO_ICON;
        return SPIRAL_GALAXY_ICON;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (!catalogMode && button == 0 && isCatalogButton(mouseX, mouseY)) {
            returnToCatalog();
            return;
        }
        if (!catalogMode) { super.mouseClicked(mouseX, mouseY, button); return; }
        if (button != 0) return;
        catalogLastDragX = catalogLastDragY = -1;
        if (mouseY >= 12 && mouseY < 30) {
            if (mouseX >= 12 && mouseX < 104) { selectedCatalogEntry = null; catalogCamera.overview(); return; }
            if (mouseX >= width - 80 && mouseX < width - 12) { mc.displayGuiScreen(null); return; }
        }
        if (selectedCatalogEntry != null && mouseX >= width - 176 && mouseX < width - 12
                && mouseY >= height - 58 && mouseY < height - 30) {
            selectGalaxy(selectedCatalogEntry); return;
        }
        GalaxyMapCatalog.Entry hit = hitCatalog(mouseX, mouseY);
        if (hit != null) {
            selectedCatalogEntry = hit;
            catalogCamera.focus(hit.mapX, hit.mapY);
            return;
        }
        catalogLastDragX = mouseX;
        catalogLastDragY = mouseY;
    }

    private boolean isCatalogButton(int mouseX, int mouseY) {
        int edge = BORDER_WIDTH + BORDER_EDGE_WIDTH;
        return mouseX > edge + 6 && mouseX < edge + 93
                && mouseY > edge + 12 && mouseY < edge + 26;
    }

    /** Replaces GalaxySpace's nested galaxy list with the top-level catalogue. */
    private void returnToCatalog() {
        catalogMode = true;
        lastCatalogFrame = 0;
        activeGalaxyEntry = null;
        this.possibleBodies = new ArrayList<CelestialBody>(allPossibleBodies);
        this.possibleBodiesGS = this.possibleBodies;
        this.selectedBody = null;
        this.lastSelectedBody = null;
        this.selectedParent = null;
        this.selectionCount = 0;
        this.zoom = -1.0F;
        this.planetZoom = 0.0F;
        this.translation.x = 0.0F;
        this.translation.y = 0.0F;
        this.position.x = 0.0F;
        this.position.y = 0.0F;
        setField("showGalaxyList", Boolean.FALSE);
        setField("showStarList", Boolean.FALSE);
        setField("starlist", new LinkedHashMap<SolarSystem, Integer>());
        setField("enableNewTierSystem", Boolean.valueOf(GSConfigCore.enableNewTierSystem));
    }

    @Override
    protected void func_146273_a(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!catalogMode) {
            super.func_146273_a(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
            return;
        }
        if (clickedMouseButton == 0 && catalogLastDragX >= 0) {
            catalogCamera.drag(mouseX - catalogLastDragX, mouseY - catalogLastDragY);
            catalogLastDragX = mouseX;
            catalogLastDragY = mouseY;
        }
    }

    private void selectGalaxy(GalaxyMapCatalog.Entry entry) {
        setField("galaxy", entry.galaxyKey);
        setField("selectedParent", null);
        setField("selectedBody", null);
        setField("selectionCount", Integer.valueOf(0));
        setField("zoom", Float.valueOf(-1.0F));
        // The origin is the black-hole centre.  Starting at zero makes the
        // first inner-map frame open from that centre rather than an orbit.
        this.translation.x = 0.0F;
        this.translation.y = 0.0F;
        this.position.x = 0.0F;
        this.position.y = 0.0F;
        activeGalaxyEntry = entry;
        // GalaxySpace's new route graph has no entries for fan systems in the
        // Micro Galaxy and otherwise displays "Tier ?".  SpaceTech supplies an
        // explicit system tier there; all native galaxies retain GS routing.
        boolean hasExplicitTier = false;
        for (SolarSystem system : GalaxyRegistry.getRegisteredSolarSystems().values()) {
            if (galaxyKeysMatch(entry.galaxyKey, system.getUnlocalizedParentGalaxyName())
                    && SpaceTechExpandedRegistry.getSystemTravelTier(system.getName()) > 0) {
                hasExplicitTier = true;
                break;
            }
        }
        setField("enableNewTierSystem", Boolean.valueOf(GSConfigCore.enableNewTierSystem && !hasExplicitTier));
        applyGalaxyScope(entry);
        catalogMode = false;
    }

    /**
     * GalaxySpace creates its sidebar starlist in the constructor, when its
     * selected galaxy is still the default Milky Way.  Rebuild it here after
     * the catalogue selection so another galaxy never inherits that list.
     */
    private void applyGalaxyScope(GalaxyMapCatalog.Entry entry) {
        List<CelestialBody> scopedBodies = new ArrayList<CelestialBody>();
        for (CelestialBody body : allPossibleBodies) {
            SolarSystem system = getParentSolarSystem(body);
            if (system != null && galaxyKeysMatch(entry.galaxyKey, system.getUnlocalizedParentGalaxyName())) {
                scopedBodies.add(body);
            }
        }
        this.possibleBodies = scopedBodies;
        this.possibleBodiesGS = scopedBodies;

        Map<SolarSystem, Integer> scopedStars = new LinkedHashMap<SolarSystem, Integer>();
        int index = 1;
        for (SolarSystem system : GalaxyRegistry.getRegisteredSolarSystems().values()) {
            if (system.getMainStar() != null
                    && galaxyKeysMatch(entry.galaxyKey, system.getUnlocalizedParentGalaxyName())) {
                scopedStars.put(system, Integer.valueOf(index++));
            }
        }
        setField("starlist", scopedStars);
        setField("galaxylist", Collections.singletonList(entry.galaxyKey));
    }

    private SolarSystem getParentSolarSystem(CelestialBody body) {
        if (body instanceof Star) return ((Star) body).getParentSolarSystem();
        if (body instanceof micdoodle8.mods.galacticraft.api.galaxies.Planet) {
            return ((micdoodle8.mods.galacticraft.api.galaxies.Planet) body).getParentSolarSystem();
        }
        if (body instanceof micdoodle8.mods.galacticraft.api.galaxies.IChildBody) {
            micdoodle8.mods.galacticraft.api.galaxies.Planet parent =
                    ((micdoodle8.mods.galacticraft.api.galaxies.IChildBody) body).getParentPlanet();
            return parent == null ? null : parent.getParentSolarSystem();
        }
        return null;
    }

    private boolean galaxyKeysMatch(String first, String second) {
        return normalizeGalaxyKey(first).equals(normalizeGalaxyKey(second));
    }

    private void setField(String name, Object value) {
        for (Class<?> type = getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(this, value);
                return;
            } catch (NoSuchFieldException ignored) {
                // Selection state is stored in a parent GalaxyCraft class.
            } catch (ReflectiveOperationException ignored) {
                return;
            }
        }
    }

    private double readDoubleField(String name) {
        for (Class<?> type = getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                Object value = field.get(this);
                return value instanceof Number ? ((Number) value).doubleValue() : 0.0D;
            } catch (NoSuchFieldException ignored) {
                // GalaxySpace keeps the isometric angles private.
            } catch (ReflectiveOperationException ignored) {
                return 0.0D;
            }
        }
        return 0.0D;
    }

}
