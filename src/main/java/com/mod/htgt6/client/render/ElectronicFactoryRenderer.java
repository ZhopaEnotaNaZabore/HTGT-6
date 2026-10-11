package com.mod.htgt6.client.render;

import com.mod.htgt6.common.electronics.TEElectronicFactory;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/** Detailed animated production line occupying the Electronics Factory's hollow 3x3 core. */
public final class ElectronicFactoryRenderer extends TileEntitySpecialRenderer {
    private static final ResourceLocation METAL = new ResourceLocation("htgt6", "textures/blocks/electronic_factory_v2_inner.png");
    @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!(tile instanceof TEElectronicFactory)) return;
        TEElectronicFactory factory = (TEElectronicFactory) tile;
        if (!factory.isStructureFormed() || factory.getWorldObj() == null) return;

        int facing = factory.getWorldObj().getBlockMetadata(factory.xCoord, factory.yCoord, factory.zCoord);
        boolean active = factory.isMachineActive() && factory.getStructureDiagnostic().startsWith("PROCESSING:");
        float progress = factory.getProcessProgressFraction(partialTicks);
        boolean loaded = factory.getCurrentRecipeItemInputCount() > 0 || factory.getCurrentRecipePrimaryOutputForRender() != null;
        double time = progress * 160D;
        int oldTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        float oldLightX = OpenGlHelper.lastBrightnessX, oldLightY = OpenGlHelper.lastBrightnessY;

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
        GL11.glTranslated(x + 0.5D, y + 0.5D, z + 0.5D);
        if (facing == 3) GL11.glRotatef(180F, 0F, 1F, 0F);
        else if (facing == 4) GL11.glRotatef(90F, 0F, 1F, 0F);
        else if (facing == 5) GL11.glRotatef(-90F, 0F, 1F, 0F);

        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        bindTexture(METAL);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,220F,220F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Raised service cabinets support a belt aligned with the rear output hatch.
        for (int cabinet=0; cabinet<4; cabinet++) {
            double cz=.72+cabinet*2.0;
            drawBox(-1.34,-2.48,cz,-.96,-.34,cz+1.40,.48F,.55F,.60F,1);
            drawBox(.96,-2.48,cz,1.34,-.34,cz+1.40,.48F,.55F,.60F,1);
            for (int vent=0;vent<4;vent++) {
                double vy=-1.85+vent*.22;
                drawBox(-1.355,vy,cz+.20,-1.34,vy+.05,cz+1.18,.18F,.24F,.28F,1);
                drawBox(1.34,vy,cz+.20,1.355,vy+.05,cz+1.18,.18F,.24F,.28F,1);
            }
        }
        GL11.glTranslated(0,1.18,0);
        GL11.glScaled(1,.60,1);

        // Raised chassis, belt, service channels and supports fill the complete three-wide core.
        drawBox(-1.42D, -2.48D, 0.48D, 1.42D, -2.34D, 8.86D,
                0.16F, 0.20F, 0.22F, 1F);
        drawBox(-1.12D, -2.33D, 0.56D, 1.12D, -2.20D, 8.78D,
                0.075F, 0.105F, 0.12F, 1F);
        drawBox(-1.43D, -2.34D, 0.50D, -1.13D, -1.94D, 8.84D,
                0.25F, 0.31F, 0.34F, 1F);
        drawBox(1.13D, -2.34D, 0.50D, 1.43D, -1.94D, 8.84D,
                0.25F, 0.31F, 0.34F, 1F);
        drawBox(-1.38D, -1.96D, 0.54D, -1.19D, -1.86D, 8.80D,
                0.08F, 0.72F, 0.78F, 0.85F);
        drawBox(1.19D, -1.96D, 0.54D, 1.38D, -1.86D, 8.80D,
                0.08F, 0.72F, 0.78F, 0.85F);

        // Visible transverse rollers and under-frame legs break up the former solid black slab.
        for (int roller = 0; roller <= 20; roller++) {
            double rollerZ = 0.62D + roller * 0.40D;
            drawBox(-1.08D, -2.195D, rollerZ, 1.08D, -2.15D, rollerZ + 0.065D,
                    0.25F, 0.31F, 0.33F, 1F);
        }
        for (int support = 0; support < 6; support++) {
            double supportZ = 0.72D + support * 1.55D;
            drawBox(-1.31D, -2.49D, supportZ, -1.08D, -2.02D, supportZ + 0.18D,
                    0.20F, 0.25F, 0.28F, 1F);
            drawBox(1.08D, -2.49D, supportZ, 1.31D, -2.02D, supportZ + 0.18D,
                    0.20F, 0.25F, 0.28F, 1F);
        }

        // Belt markers advance from the actual synchronized recipe progress, not free-running time.
        double beltPhase = loaded ? (progress * 8.10D) % 0.80D : 0D;
        for (int marker = -1; marker < 12; marker++) {
            double markerZ = 0.72D + marker * 0.80D + beltPhase;
            if (markerZ < 0.62D || markerZ > 8.58D) continue;
            drawBox(-0.78D, -2.145D, markerZ, 0.78D, -2.12D, markerZ + 0.08D,
                    active ? 0.12F : 0.18F, active ? 0.90F : 0.38F,
                    active ? 1.00F : 0.42F, 0.96F);
        }

        double itemZ = -100D;
        if (loaded) {
            if (progress < 0.45F) {
                int componentCount = factory.getCurrentRecipeItemInputCount();
                int componentIndex = ElectronicAssemblyCycle.component(progress,componentCount);
                itemZ = ElectronicAssemblyCycle.inputZ(progress,componentCount);
                drawRecipeComponent(factory.getCurrentRecipeItemInputForRender(componentIndex), itemZ);
            } else if (progress < .70F) {
                itemZ=4.52;
                drawRecipeComponent(progress < .60F
                        ? factory.getCurrentRecipeItemInputForRender(Math.max(0,factory.getCurrentRecipeItemInputCount()-1))
                        : factory.getCurrentRecipePrimaryOutputForRender(),itemZ);
            } else {
                itemZ = ElectronicAssemblyCycle.outputZ(progress);
                drawRecipeComponent(factory.getCurrentRecipePrimaryOutputForRender(), itemZ);
            }
        }
        drawProcessingStations(time, active, itemZ);
        drawAssemblyChamber(time, active && ElectronicAssemblyCycle.assembling(progress));
        float stroke=loaded ? ElectronicAssemblyCycle.stroke(progress) : 0;
        double toolY=.62-stroke*1.10;
        drawBox(-.12,toolY,4.39,.12,1.25,4.65,.80F,.84F,.88F,1);
        drawBox(-.46,toolY-.22,4.20,.46,toolY,4.84,.62F,.68F,.72F,1);
        if (active && ElectronicAssemblyCycle.assembling(progress)) {
            double scan=Math.sin(progress*150)*.36;
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            drawBox(scan-.018,-1.96,4.31,scan+.018,toolY-.22,4.35,.1F,.94F,1F,.75F);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        } finally {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,oldLightX,oldLightY);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
        OpenGlHelper.setActiveTexture(oldTexture);
        }
    }

    private static void drawProcessorBoard(double z) {
        // Illuminated carrier makes tiny dusts/chips readable without replacing their real icon.
        drawBox(-0.64D, -2.145D, z - 0.50D, 0.64D, -2.075D, z + 0.50D,
                0.08F, 0.43F, 0.25F, 1F);
        drawBox(-0.52D, -2.07D, z - 0.40D, 0.52D, -2.025D, z + 0.40D,
                0.12F, 0.18F, 0.19F, 1F);
        for (int pin = -3; pin <= 3; pin++) {
            double pinZ = z + pin * 0.09D;
            drawBox(-0.58D, -2.02D, pinZ, -0.39D, -1.98D, pinZ + 0.040D,
                    0.92F, 0.66F, 0.15F, 1F);
            drawBox(0.39D, -2.02D, pinZ, 0.58D, -1.98D, pinZ + 0.040D,
                    0.92F, 0.66F, 0.15F, 1F);
        }
    }

    /** Each ingredient crosses the complete line once, in recipe order. */
    private static void drawRecipeComponent(ItemStack stack, double z) {
        drawProcessorBoard(z);
        if (stack == null || stack.getItem() == null) return;
        IIcon icon = stack.getIconIndex();
        if (icon == null) return;
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        Minecraft.getMinecraft().getTextureManager().bindTexture(
                stack.getItem().getSpriteNumber() == 0
                        ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        Tessellator t = Tessellator.instance;
        double half = 0.43D;
        double y = -1.972D;
        t.startDrawingQuads();
        t.setBrightness(0x00F000F0);
        t.setColorRGBA_F(1F, 1F, 1F, 1F);
        t.addVertexWithUV(-half, y, z - half, icon.getMinU(), icon.getMinV());
        t.addVertexWithUV(-half, y, z + half, icon.getMinU(), icon.getMaxV());
        t.addVertexWithUV(half, y, z + half, icon.getMaxU(), icon.getMaxV());
        t.addVertexWithUV(half, y, z - half, icon.getMaxU(), icon.getMinV());
        t.draw();
        Minecraft.getMinecraft().getTextureManager().bindTexture(METAL);
    }

    private static void drawProcessingStations(double time, boolean active, double itemZ) {
        double[] stations = { 1.65D, 3.55D, 5.45D, 7.35D };
        float pulse = 0.72F + 0.28F * (float) Math.sin(time * 0.62D);
        for (int station = 0; station < stations.length; station++) {
            double stationZ = stations[station];
            // Full-height sealing arch with crossbar, braces, control pods and laser heads.
            drawBox(-1.37D, -2.18D, stationZ - 0.13D, -1.10D, 1.58D, stationZ + 0.13D,
                    0.22F, 0.29F, 0.32F, 1F);
            drawBox(1.10D, -2.18D, stationZ - 0.13D, 1.37D, 1.58D, stationZ + 0.13D,
                    0.22F, 0.29F, 0.32F, 1F);
            drawBox(-1.37D, 1.42D, stationZ - 0.17D, 1.37D, 1.70D, stationZ + 0.17D,
                    0.25F, 0.33F, 0.36F, 1F);
            drawBox(-1.08D, 1.16D, stationZ - 0.12D, -0.58D, 1.43D, stationZ + 0.12D,
                    0.15F, 0.22F, 0.25F, 1F);
            drawBox(0.58D, 1.16D, stationZ - 0.12D, 1.08D, 1.43D, stationZ + 0.12D,
                    0.15F, 0.22F, 0.25F, 1F);
            drawBox(-0.25D, 0.91D, stationZ - 0.25D, 0.25D, 1.43D, stationZ + 0.25D,
                    0.12F, 0.20F, 0.23F, 1F);
            drawBox(-0.13D, 0.76D, stationZ - 0.13D, 0.13D, 0.94D, stationZ + 0.13D,
                    active ? 0.20F : 0.18F, active ? 0.85F : 0.31F,
                    active ? 0.95F : 0.34F, 1F);
            // Bright numbered station lamps make the production line readable at a distance.
            float lamp = active ? 0.65F + station * 0.07F : 0.20F;
            drawBox(-1.385D, 0.48D, stationZ - 0.17D, -1.365D, 0.83D, stationZ + 0.17D,
                    0.08F, lamp, Math.min(1F, lamp + 0.16F), 1F);
            drawBox(1.365D, 0.48D, stationZ - 0.17D, 1.385D, 0.83D, stationZ + 0.17D,
                    0.08F, lamp, Math.min(1F, lamp + 0.16F), 1F);

            if (active && Math.abs(itemZ-stationZ)<.55D) {
                float proximity = (float) Math.max(0D,
                        1.0D - Math.abs(itemZ - stationZ) / 0.78D);
                GL11.glDepthMask(false);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                // Beam follows the moving carrier while it is inside this station.
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                drawBox(-0.035D, -1.96D, stationZ - 0.035D, 0.035D, 0.79D, stationZ + 0.035D,
                        0.12F, 0.92F, 1.00F, 0.72F * pulse * proximity);
                drawBox(-0.22D, -1.955D, itemZ - 0.22D, 0.22D, -1.90D, itemZ + 0.22D,
                        0.20F, 0.95F, 1.00F, 0.45F * pulse * proximity);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
            }
        }
    }

    /** Central opaque process block separates input staging from finished-product output. */
    private static void drawAssemblyChamber(double time, boolean active) {
        double minZ = 4.13D, maxZ = 4.91D;
        drawBox(-1.44D, -2.32D, minZ, -0.72D, 1.48D, maxZ,
                0.19F, 0.25F, 0.28F, 1F);
        drawBox(0.72D, -2.32D, minZ, 1.44D, 1.48D, maxZ,
                0.19F, 0.25F, 0.28F, 1F);
        drawBox(-0.72D, 0.72D, minZ, 0.72D, 1.48D, maxZ,
                0.22F, 0.30F, 0.33F, 1F);
        drawBox(-0.72D, -2.31D, minZ, 0.72D, -2.02D, maxZ,
                0.14F, 0.19F, 0.21F, 1F);
        // Front and rear lips leave a visible tunnel through the machine block.
        drawBox(-0.74D, -2.02D, minZ, -0.57D, 0.73D, minZ + 0.12D,
                0.28F, 0.36F, 0.39F, 1F);
        drawBox(0.57D, -2.02D, minZ, 0.74D, 0.73D, minZ + 0.12D,
                0.28F, 0.36F, 0.39F, 1F);
        drawBox(-0.74D, -2.02D, maxZ - 0.12D, -0.57D, 0.73D, maxZ,
                0.28F, 0.36F, 0.39F, 1F);
        drawBox(0.57D, -2.02D, maxZ - 0.12D, 0.74D, 0.73D, maxZ,
                0.28F, 0.36F, 0.39F, 1F);
        float pulse = active ? 0.72F + 0.28F * (float) Math.sin(time * 0.8D) : 0.20F;
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        drawBox(-0.58D, -1.78D, minZ + 0.13D, 0.58D, 0.52D, maxZ - 0.13D,
                0.08F, 0.72F, 1.0F, active ? 0.18F * pulse : 0.04F);
        drawBox(-1.455D, -0.62D, minZ + 0.17D, -1.435D, 0.47D, maxZ - 0.17D,
                0.05F, pulse, 1F, active ? 0.85F : 0.32F);
        drawBox(1.435D, -0.62D, minZ + 0.17D, 1.455D, 0.47D, maxZ - 0.17D,
                0.05F, pulse, 1F, active ? 0.85F : 0.32F);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(true);
    }

    private static void drawBox(double minX, double minY, double minZ,
                                double maxX, double maxY, double maxZ,
                                float red, float green, float blue, float alpha) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setBrightness(0x00F000F0);
        t.setColorRGBA_F(red, green, blue, alpha);
        t.addVertexWithUV(minX,minY,minZ,0,0);t.addVertexWithUV(maxX,minY,minZ,1,0);t.addVertexWithUV(maxX,minY,maxZ,1,1);t.addVertexWithUV(minX,minY,maxZ,0,1);
        t.addVertexWithUV(minX,maxY,minZ,0,0);t.addVertexWithUV(minX,maxY,maxZ,1,0);t.addVertexWithUV(maxX,maxY,maxZ,1,1);t.addVertexWithUV(maxX,maxY,minZ,0,1);
        t.addVertexWithUV(minX,minY,minZ,0,0);t.addVertexWithUV(minX,maxY,minZ,1,0);t.addVertexWithUV(maxX,maxY,minZ,1,1);t.addVertexWithUV(maxX,minY,minZ,0,1);
        t.addVertexWithUV(minX,minY,maxZ,0,0);t.addVertexWithUV(maxX,minY,maxZ,1,0);t.addVertexWithUV(maxX,maxY,maxZ,1,1);t.addVertexWithUV(minX,maxY,maxZ,0,1);
        t.addVertexWithUV(minX,minY,minZ,0,0);t.addVertexWithUV(minX,minY,maxZ,1,0);t.addVertexWithUV(minX,maxY,maxZ,1,1);t.addVertexWithUV(minX,maxY,minZ,0,1);
        t.addVertexWithUV(maxX,minY,minZ,0,0);t.addVertexWithUV(maxX,maxY,minZ,1,0);t.addVertexWithUV(maxX,maxY,maxZ,1,1);t.addVertexWithUV(maxX,minY,maxZ,0,1);
        t.draw();
    }
}
