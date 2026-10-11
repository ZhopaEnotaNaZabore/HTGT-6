package com.mod.htgt6.client.render;

import com.mod.htgt6.common.TE.FluidSolidifierTE;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

/** Two-stage interior: molten channel, casting chamber, then powered conveyor to the rear output. */
public final class FluidSolidifierRenderer extends TileEntitySpecialRenderer {
    @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!(tile instanceof FluidSolidifierTE)) return;
        FluidSolidifierTE machine = (FluidSolidifierTE) tile;
        if (!machine.isStructureFormed() || machine.getWorldObj() == null) return;
        int facing = machine.getWorldObj().getBlockMetadata(machine.xCoord, machine.yCoord, machine.zCoord);
        boolean active = machine.isMachineActive()
                && machine.getStructureDiagnostic().startsWith("PROCESSING:");
        float progress = machine.getProcessProgressFraction(partialTicks);
        boolean loaded = machine.getCurrentRecipePrimaryOutputForRender() != null;
        // All moving parts share recipe phase; a paused recipe keeps its exact pose.
        double time = progress * 160D;
        double fill = phase(progress, .12, .42);
        double cooling = phase(progress, .42, .66);
        double release = phase(progress, .66, .76);
        double travel = phase(progress, .76, 1.0);

        MultiblockRenderState state = new MultiblockRenderState("foundry");
        try {
        // Local y=0 is the solid floor layer. Lift the whole mechanical line
        // into the hollow y=1..5 chamber so it cannot be depth-hidden by casing.
        GL11.glTranslated(x + .5D, y + 1.02D, z + .5D);
        if (facing == 3) GL11.glRotated(180D, 0D, 1D, 0D);
        else if (facing == 4) GL11.glRotated(90D, 0D, 1D, 0D);
        else if (facing == 5) GL11.glRotated(-90D, 0D, 1D, 0D);


        // Section 1: elevated melting vessel followed by the refractory channel.
        drawBox(-1.30, .08, 1.10, 1.30, .30, 3.18, .22F, .24F, .25F, 1F);
        // Open crucible walls, not a solid box hiding the molten charge.
        drawBox(-1.13, .30, 1.28, -.94, 2.82, 2.92, .32F, .34F, .35F, 1F);
        drawBox(.94, .30, 1.28, 1.13, 2.82, 2.92, .32F, .34F, .35F, 1F);
        drawBox(-.94, .30, 1.28, .94, 2.82, 1.46, .32F, .34F, .35F, 1F);
        drawBox(-.94, .30, 2.74, .94, 1.55, 2.92, .32F, .34F, .35F, 1F);
        drawBox(-.92, .48, 1.47, .92, .54, 2.72, .075F, .060F, .052F, 1F);
        drawBox(-1.22, 2.82, 1.18, -.91, 3.04, 3.02, .24F, .27F, .28F, 1F);
        drawBox(.91, 2.82, 1.18, 1.22, 3.04, 3.02, .24F, .27F, .28F, 1F);
        drawBox(-.24, .42, 2.78, .24, 1.18, 3.62, .39F, .34F, .28F, 1F);
        // Upper feed pipe remains visible while idle and joins the machine to the first chamber.
        drawBox(-1.43, 3.55, 1.62, 1.43, 3.82, 1.92, .29F, .32F, .33F, 1F);
        drawBox(-.18, 3.05, 1.68, .18, 3.72, 1.86, .38F, .40F, .40F, 1F);

        for (int v = 3; v <= 7; v++) {
            drawBox(-1.22, .16, v + .10, 1.22, .32, v + .90, .13F, .15F, .16F, 1F);
            drawBox(-1.30, .32, v + .10, -1.02, .72, v + .90, .30F, .27F, .23F, 1F);
            drawBox(1.02, .32, v + .10, 1.30, .72, v + .90, .30F, .27F, .23F, 1F);
        }
        drawBox(-.88, .34, 3.08, .88, .55, 8.15, .09F, .07F, .055F, 1F);
        drawBox(-.66, .55, 3.15, .66, .68, 8.10, .24F, .11F, .025F, 1F);

        // Furnace bands, channel expansion joints and covered cooling manifold.
        for(int band=0;band<3;band++) {
            double by=.70+band*.72;
            drawBox(-1.16,by,1.24,-1.12,by+.12,2.96,.68F,.47F,.25F,1);
            drawBox(1.12,by,1.24,1.16,by+.12,2.96,.68F,.47F,.25F,1);
        }
        drawBox(.96,3.36,8.1,1.16,3.55,12.1,.48F,.53F,.57F,1);
        for(int support=0;support<3;support++) {
            double sz=9.4+support*1.8;
            drawBox(-1.29,.62,sz,-1.14,2.25,sz+.16,.50F,.55F,.58F,1);
            drawBox(1.14,.62,sz,1.29,2.25,sz+.16,.50F,.55F,.58F,1);
            drawBox(-1.29,2.12,sz,1.29,2.28,sz+.16,.50F,.55F,.58F,1);
        }

        // Section separator and exchangeable virtual mold carousel.
        drawBox(-1.34, .12, 8.12, -1.08, 2.55, 8.44, .30F, .33F, .34F, 1F);
        drawBox(1.08, .12, 8.12, 1.34, 2.55, 8.44, .30F, .33F, .34F, 1F);
        drawBox(-1.22, 2.55, 8.05, 1.22, 3.35, 9.38, .26F, .29F, .30F, 1F);
        drawBox(-.48, 2.05, 8.34, .48, 2.74, 9.08, .40F, .42F, .42F, 1F);
        drawBox(-.84, .24, 8.00, .84, .48, 9.45, .22F, .25F, .27F, 1F);
        GL11.glPushMatrix();
        GL11.glTranslated(0D, .60D, 8.70D);
        double opening = .38D * release;
        drawBox(-.80 - opening, -.10, -.52, -.36 - opening, .24, .52, .48F, .52F, .55F, 1F);
        drawBox(.36 + opening, -.10, -.52, .80 + opening, .24, .52, .48F, .52F, .55F, 1F);
        drawBox(-.36, -.12, -.52, .36, -.02, .52, .18F, .20F, .22F, 1F);
        if (loaded && fill > 0 && travel == 0) {
            float heat = (float) (1D - cooling);
            double thickness = machine.getActiveSelectorForRender() == 2 ? .055 : .20;
            drawBox(-.31, 0, -.43, .31, thickness * fill, .43,
                    .55F + heat * .45F, .59F - heat * .25F, .63F - heat * .60F, 1F);
        }
        GL11.glPopMatrix();

        // Hydraulic ram: parked high when idle, cycling over the virtual mold while active.
        double ramOffset = loaded ? .48D - .42D * Math.sin(Math.PI * cooling) : .48D;
        drawBox(-.31, 1.46 + ramOffset, 8.47, .31, 2.50 + ramOffset, 8.95,
                active ? .57F : .38F, active ? .62F : .42F, active ? .64F : .43F, 1F);

        // Section 2: moving segmented conveyor terminating exactly at the fixed output gateway.
        for (int v = 9; v < 15; v++) {
            drawBox(-1.25, .18, v + .04, 1.25, .35, v + .96, .055F, .07F, .075F, 1F);
            drawBox(-1.36, .12, v + .08, -1.20, .62, v + .92, .29F, .31F, .32F, 1F);
            drawBox(1.20, .12, v + .08, 1.36, .62, v + .92, .29F, .31F, .32F, 1F);
        }

        // Transverse belt cleats travel along the casting line towards the rear hatch.
        double beltShift = (travel * 5.4D) % .42D;
        for (double beltZ = 9.05 + beltShift; beltZ < 14.88; beltZ += .42) {
            drawBox(-1.17, .35, beltZ, 1.17, .39, Math.min(14.96, beltZ + .09),
                    .22F, .32F, .35F, 1F);
        }
        // Cooling manifold and visible indicator lamps.
        for (int nozzle = 0; nozzle < 3; nozzle++) {
            double nz = 8.3 + nozzle * .37;
            drawBox(.88, 1.4, nz, 1.05, 2.55, nz + .12, .42F, .48F, .51F, 1F);
            drawBox(.55, 1.4, nz, .98, 1.55, nz + .12, .35F, .40F, .43F, 1F);
        }
        drawBox(-1.235, 2.69, 8.44, -1.225, 2.92, 8.83,
                active ? .12F : .65F, active ? .90F : .32F, .12F, 1F);

        if (loaded) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(false);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            float pulse = .68F + .22F * (float) Math.sin(time * .28D);
            // Furnace charge glows for the whole operation.
            drawBox(-.82, .55, 1.55, .82, 2.42, 2.65, 1F, .20F, .01F, .34F + .15F * pulse);
            // During the first half molten metal advances down the channel.
            double flowEnd = 3.2D + 5D * phase(progress, .05, .28);
            if (progress < .45F) {
                drawBox(-.58, .57, 2.70, .58, .72, flowEnd, 1F, .24F, .015F, pulse);
                drawBox(-.16, .70, 2.95, .16, 1.75, 3.14, 1F, .48F, .04F, .85F);
            }
            if (progress >= .28F && progress < .50F) {
                drawBox(-.72, .64, 8.28, .72, 1.28, 9.18, 1F, .34F, .04F,
                        .22F + .16F * pulse);
            }
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            // Small bounded steam wisps rise during cooling; no world particles or entities are spawned.
            if (progress > .42F && progress < .66F) {
                for (int i = 0; i < 7; i++) {
                    double rise = (cooling * 2.6D + i / 7D) % 1D;
                    double spread = .06D + rise * .16D;
                    double steamX = -.45D + (i % 3) * .39D;
                    double steamZ = 8.35D + (i % 4) * .22D;
                    drawBox(steamX - spread, .9D + rise, steamZ - spread,
                            steamX + spread, 1.06D + rise, steamZ + spread,
                            .70F, .84F, .90F, (float) ((1D - rise) * .24D));
                }
            }
            GL11.glDepthMask(true);

            // Once cast, the finished billet travels from the chamber to the rear output.
            if (travel > 0) drawProduct(machine, 8.70D + travel * 6.1D);
        }

        } finally { state.close(); }
    }

    private static double phase(double progress, double start, double end) {
        double t = Math.max(0D, Math.min(1D, (progress - start) / (end - start)));
        return t * t * (3D - 2D * t);
    }

    private void drawProduct(FluidSolidifierTE machine, double position) {
        ItemStack output = machine.getCurrentRecipePrimaryOutputForRender();
        if (output == null) return;
        output.stackSize = 1;
        EntityItem item = new EntityItem(machine.getWorldObj(), 0, 0, 0, output);
        item.hoverStart = 0F;
        item.age = 0;
        float lightX = OpenGlHelper.lastBrightnessX;
        float lightY = OpenGlHelper.lastBrightnessY;
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glTranslated(0, .62, position);
        GL11.glScaled(1.5, 1.5, 1.5);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        RenderManager.instance.renderEntityWithPosYaw(item, 0, 0, 0, 0F, 0F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, lightY);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
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
