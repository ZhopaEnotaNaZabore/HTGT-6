package com.mod.htgt6.client.render;

import com.mod.htgt6.common.mixer.MultiTileIndustrialMixer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

/** Mechanical shaft, paddles and mixing bowl occupying the hollow 3x8x3 core. */
public final class IndustrialMixerRenderer extends TileEntitySpecialRenderer {
    private final MultiblockAnimationClock clock = new MultiblockAnimationClock();
    @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z,
                                              float partialTicks) {
        if (!(tile instanceof MultiTileIndustrialMixer)) return;
        MultiTileIndustrialMixer mixer = (MultiTileIndustrialMixer) tile;
        if (!mixer.isStructureFormed() || mixer.getWorldObj() == null) return;

        int facing = mixer.getWorldObj().getBlockMetadata(mixer.xCoord, mixer.yCoord, mixer.zCoord);
        double dx = facing == 4 ? 2D : facing == 5 ? -2D : 0D;
        double dz = facing == 2 ? 2D : facing == 3 ? -2D : 0D;
        double time = clock.sample(tile, MultiblockAnimationClock.frameTime(mixer.getWorldObj().getTotalWorldTime(), partialTicks), mixer.isMachineActive() && mixer.getStructureDiagnostic().startsWith("PROCESSING:"));
        boolean active = mixer.isMachineActive() && mixer.getStructureDiagnostic().startsWith("PROCESSING:");

        MultiblockRenderState state = new MultiblockRenderState("mixer");
        try {
        GL11.glTranslated(x + 0.5D + dx, y + 1.02D, z + 0.5D + dz);


        // Bowl, upper drive and stationary support frame.
        drawBox(-1.30, 0.05, -1.30, 1.30, 0.30, 1.30, .16F, .20F, .23F, 1F);
        // Open bowl: four walls leave the mixing charge visible.
        drawBox(-1.18,.30,-1.18,-1.04,1.35,1.18,.30F,.36F,.39F,1);
        drawBox(1.04,.30,-1.18,1.18,1.35,1.18,.30F,.36F,.39F,1);
        drawBox(-1.04,.30,-1.18,1.04,1.35,-1.04,.30F,.36F,.39F,1);
        drawBox(-1.04,.30,1.04,1.04,1.35,1.18,.30F,.36F,.39F,1);
        for(int i=0;i<4;i++) {
            GL11.glPushMatrix(); GL11.glRotatef(i*90,0,1,0);
            drawBox(1.22,.30,-.12,1.39,7.28,.12,.48F,.54F,.58F,1);
            drawBox(.20,6.60,-.11,1.25,6.78,.11,.42F,.48F,.55F,1);
            GL11.glPopMatrix();
        }
        drawBox(-1.05, 7.15, -1.05, 1.05, 7.82, 1.05, .18F, .23F, .27F, 1F);
        drawBox(-1.27, 7.28, -0.20, 1.27, 7.68, 0.20, .24F, .31F, .34F, 1F);

        GL11.glPushMatrix();
        GL11.glRotatef((float) (time * 13D % 360D), 0F, 1F, 0F);
        drawBox(-0.12, 0.62, -0.12, 0.12, 7.30, 0.12,
                active ? .26F : .38F, active ? .92F : .48F, active ? .96F : .52F, 1F);
        for (int level = 0; level < 3; level++) {
            double bladeY = 1.45D + level * 1.82D;
            GL11.glPushMatrix();
            GL11.glRotatef(level * 41F, 0F, 1F, 0F);
            drawBox(-1.18, bladeY, -0.13, 1.18, bladeY + 0.22, 0.13,
                    .38F, .43F, .45F, 1F);
            drawBox(-1.18,bladeY+.12,-.30,-.92,bladeY+.64,.30,.52F,.59F,.62F,1);
            drawBox(.92,bladeY+.12,-.30,1.18,bladeY+.64,.30,.52F,.59F,.62F,1);
            drawBox(-0.13, bladeY - 0.05, -1.18, 0.13, bladeY + 0.17, 1.18,
                    .38F, .43F, .45F, 1F);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();

        if (mixer.getCurrentRecipePrimaryOutputForRender()!=null) {
            GL11.glPushMatrix(); GL11.glRotatef((float)(time*9%360),0,1,0);
            for(int grain=0;grain<8;grain++) {
                double angle=grain*Math.PI/4, gx=Math.cos(angle)*.62, gz=Math.sin(angle)*.62;
                drawBox(gx-.13,.40,gz-.13,gx+.13,.57,gz+.13,.65F,.59F,.40F,1);
            }
            GL11.glPopMatrix();
        }
        // Powered cyan volume remains subtle; it brightens only while processing.
        if (active) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(false);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            float pulse = .20F + .08F * (float) Math.sin(time * .32D);
            drawBox(-.82, .48, -.82, .82, 1.30, .82, .05F, .82F, .95F, pulse);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(true);
        }

        } finally { state.close(); }
    }

    private static void drawBox(double minX, double minY, double minZ,
                                double maxX, double maxY, double maxZ,
                                float red, float green, float blue, float alpha) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads(); t.setBrightness(0x00F000F0);
        t.setColorRGBA_F(alpha >= .99F ? .30F + red * .70F : red,
                alpha >= .99F ? .30F + green * .70F : green,
                alpha >= .99F ? .30F + blue * .70F : blue, alpha);
        t.addVertexWithUV(minX,minY,minZ,0,0);t.addVertexWithUV(maxX,minY,minZ,1,0);t.addVertexWithUV(maxX,minY,maxZ,1,1);t.addVertexWithUV(minX,minY,maxZ,0,1);
        t.addVertexWithUV(minX,maxY,minZ,0,0);t.addVertexWithUV(minX,maxY,maxZ,1,0);t.addVertexWithUV(maxX,maxY,maxZ,1,1);t.addVertexWithUV(maxX,maxY,minZ,0,1);
        t.addVertexWithUV(minX,minY,minZ,0,0);t.addVertexWithUV(minX,maxY,minZ,1,0);t.addVertexWithUV(maxX,maxY,minZ,1,1);t.addVertexWithUV(maxX,minY,minZ,0,1);
        t.addVertexWithUV(minX,minY,maxZ,0,0);t.addVertexWithUV(maxX,minY,maxZ,1,0);t.addVertexWithUV(maxX,maxY,maxZ,1,1);t.addVertexWithUV(minX,maxY,maxZ,0,1);
        t.addVertexWithUV(minX,minY,minZ,0,0);t.addVertexWithUV(minX,minY,maxZ,1,0);t.addVertexWithUV(minX,maxY,maxZ,1,1);t.addVertexWithUV(minX,maxY,minZ,0,1);
        t.addVertexWithUV(maxX,minY,minZ,0,0);t.addVertexWithUV(maxX,maxY,minZ,1,0);t.addVertexWithUV(maxX,maxY,maxZ,1,1);t.addVertexWithUV(maxX,minY,maxZ,0,1);
        t.draw();
    }
    private static void vertex(Tessellator t, double x, double y, double z) {
        t.addVertexWithUV(x,y,z,x+z,y);
    }
}
