package com.mod.htgt6.client.render;
import com.mod.htgt6.common.flotation.MultiTileFlotationChamber;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

/** Three aeration cells inside the common controller geometry. */
public final class FlotationChamberRenderer extends TileEntitySpecialRenderer {
    private final MultiblockAnimationClock clock = new MultiblockAnimationClock();
    @Override public void renderTileEntityAt(TileEntity tile,double x,double y,double z,float partial) {
        if (!(tile instanceof MultiTileFlotationChamber) || tile.getWorldObj()==null) return;
        MultiTileFlotationChamber machine=(MultiTileFlotationChamber)tile;
        if (!machine.isStructureFormed()) return;
        boolean active=machine.isFlotationRunning();
        double time=clock.sample(tile,MultiblockAnimationClock.frameTime(tile.getWorldObj().getTotalWorldTime(),partial),active);
        MultiblockRenderState state=new MultiblockRenderState("mixer");
        try {
            GL11.glTranslated(x+.5,y,z+.5);
            int facing=tile.getBlockMetadata();
            GL11.glRotatef(facing==3?180:facing==4?90:facing==5?270:0,0,1,0);
            // All model coordinates are in the same zero-based grid as NEI.
            GL11.glTranslated(-MultiTileFlotationChamber.CONTROLLER_X-.5,
                    -MultiTileFlotationChamber.CONTROLLER_Y,-MultiTileFlotationChamber.CONTROLLER_Z-.5);
            for (int cell=0;cell<3;cell++) {
                double left=3+cell*3, axis=left+.5;
                // Structural bridges and motor housings are real blocks. Add only
                // exposed shafts, impellers, motor fins and the status lens.
                for(int fin=0;fin<4;fin++)
                    drawBox(left-.05,6.1+fin*.19,5.04,left+1.05,6.17+fin*.19,5.96,.40F,.46F,.48F,1);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                drawBox(left+.25,6.28,4.98,left+.75,6.7,5.01,.1F,.8F,.85F,1);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glPushMatrix();
                try {
                    GL11.glTranslated(axis,0,5.5);
                    GL11.glRotated(time*12%360,0,1,0);
                    drawBox(-.09,2.15,-.09,.09,5,.09,.58F,.62F,.63F,1);
                    for(int level=0;level<2;level++) {
                        double sy=2.3+level*1.05;
                        drawBox(-.45,sy,-.13,.45,sy+.2,.13,.42F,.48F,.5F,1);
                        drawBox(-.13,sy,-.45,.13,sy+.2,.45,.42F,.48F,.5F,1);
                    }
                } finally { GL11.glPopMatrix(); }
                if(machine.hasFlotationCharge()) {
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    GL11.glDepthMask(false);
                    drawBox(left+.02,2.02,3.02,left+1.98,4.85,7.98,.24F,.50F,.42F,.45F);
                    GL11.glDepthMask(true);
                    for(int foam=0;foam<8;foam++) {
                        double fz=3.15+foam*.58;
                        drawBox(left+1.3,4.86,fz,left+1.93,4.95,fz+.38,.86F,.85F,.64F,1);
                    }
                    drawBox(left+1.3,5.01,7.8,left+1.93,5.08,8.85,.86F,.85F,.64F,1);
                    if(active)for(int bubble=0;bubble<12;bubble++) {
                        double bx=left+.18+(bubble%3)*.57, bz=3.4+(bubble/3)*1.1;
                        double by=2.1+(time*.08+bubble*.17)%2.65;
                        drawBox(bx,by,bz,bx+.08,by+.08,bz+.08,.70F,.90F,.80F,1);
                    }
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                }
            }
            // Rear froth launder ends above a small receiver at the right side.
            drawBox(3,4.82,8.55,12.8,4.94,9.3,.32F,.39F,.42F,1);
            drawBox(3,4.94,9.18,12.8,5.2,9.3,.32F,.39F,.42F,1);
            drawBox(12.18,4.82,7.55,12.82,4.94,9.18,.32F,.39F,.42F,1);
            drawBox(12.82,4.94,7.55,12.95,5.2,9.3,.32F,.39F,.42F,1);
            drawBox(12.05,2,4,12.18,2.9,8,.32F,.39F,.42F,1);
            drawBox(12.82,2,4,12.95,2.9,8,.32F,.39F,.42F,1);
            drawBox(12.18,2,4,12.82,2.9,4.12,.32F,.39F,.42F,1);
            drawBox(12.18,2,7.88,12.82,2.9,8,.32F,.39F,.42F,1);
            // Walkways surround the baths; leave the front console and feed accessible.
            rail(0.15,1,0.15,9.7,0.15); rail(11.3,1,0.15,12.85,0.15);
            rail(0.15,1,10.85,12.85,10.85);
            rail(0.15,1,2.7,0.15,4.7); rail(0.15,1,8.3,0.15,10.85);
            rail(12.85,1,0.15,12.85,3.7); rail(12.85,1,8.4,12.85,10.85);
            // Two-stage ore feed on the left, supported by construction blocks.
            drawBox(.15,3,5.15,1.85,3.18,7.85,.4F,.44F,.46F,1);
            drawBox(.15,3.18,5.15,.3,4.2,7.85,.5F,.53F,.55F,1);
            drawBox(1.7,3.18,5.15,1.85,4.2,7.85,.5F,.53F,.55F,1);
            drawBox(.3,3.18,5.15,1.7,4.2,5.3,.5F,.53F,.55F,1);
            drawBox(.3,3.18,7.7,1.7,4.2,7.85,.5F,.53F,.55F,1);
            drawBox(.6,4.2,7,.85,5.7,7.3,.4F,.44F,.46F,1);
            drawBox(.15,5.7,6.5,1.85,5.83,7.9,.5F,.53F,.55F,1);
            drawBox(.15,5.83,6.5,.3,6.6,7.9,.5F,.53F,.55F,1);
            drawBox(1.7,5.83,6.5,1.85,6.6,7.9,.5F,.53F,.55F,1);
            drawBox(.3,5.83,6.5,1.7,6.6,6.65,.5F,.53F,.55F,1);
            drawBox(.3,5.83,7.75,1.7,6.6,7.9,.5F,.53F,.55F,1);
            // Compressor sits next to the left inlet, not over an accessible gateway.
            drawBox(.15,1,2.2,1.85,2.1,3.5,.32F,.38F,.4F,1);
            for(int vent=0;vent<5;vent++)
                drawBox(.3+vent*.27,1.2,2.17,.41+vent*.27,1.9,2.2,.12F,.15F,.17F,1);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            drawBox(1.4,1.35,1,1.6,1.55,2.2,.08F,.5F,.64F,1);
            drawBox(1.5,1.35,1.3,10.5,1.55,1.5,.08F,.5F,.64F,1);
            for(int cell=0;cell<3;cell++) {
                double at=3.5+cell*3;
                drawBox(at-.1,1.35,1.4,at+.1,1.55,2,.08F,.5F,.64F,1);
            }
            // Hazard markings make the long window facade legible from the walkway.
            for(int i=2;i<12;i++) {
                drawBox(i+.05,1.98,1.88,i+.42,2.01,2.02,.95F,.68F,.1F,1);
                drawBox(i+.5,1.98,1.88,i+.87,2.01,2.02,.13F,.14F,.14F,1);
            }
            if(machine.hasFlotationCharge()) {
                drawBox(3.1,4.95,8.65,12.7,5.01,9.12,.86F,.85F,.64F,1);
                drawBox(12.3,4.95,7.55,12.7,5.01,9.12,.86F,.85F,.64F,1);
                drawBox(12.3,2.12,4.15,12.75,2.65,7.85,.24F,.5F,.42F,1);
                drawBox(12.3,2.65,7.55,12.65,4.95,7.8,.86F,.85F,.64F,1);
            }
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        } finally { state.close(); }
    }
    private static void rail(double x1,double y,double z1,double x2,double z2) {
        int posts=Math.max(1,(int)Math.ceil(Math.max(Math.abs(x2-x1),Math.abs(z2-z1))/2));
        for(int i=0;i<=posts;i++) {
            double x=x1+(x2-x1)*i/posts,z=z1+(z2-z1)*i/posts;
            drawBox(x-.05,y,z-.05,x+.05,y+1.2,z+.05,.55F,.58F,.6F,1);
        }
        for(double h:new double[]{.6,1.1})
            drawBox(Math.min(x1,x2)-.05,y+h,Math.min(z1,z2)-.05,
                    Math.max(x1,x2)+.05,y+h+.08,Math.max(z1,z2)+.05,.55F,.58F,.6F,1);
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

}
