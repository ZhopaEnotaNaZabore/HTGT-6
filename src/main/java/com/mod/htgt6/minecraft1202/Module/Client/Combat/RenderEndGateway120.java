package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.TileEntityEndGateway;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Fixed-function adaptation: six portal faces and a synchronized lifecycle beam. */
public final class RenderEndGateway120 extends TileEntitySpecialRenderer {
    private static final ResourceLocation SKY=new ResourceLocation("htgt6:textures/entity/combat_update/end_sky.png");
    private static final ResourceLocation PORTAL=new ResourceLocation("htgt6:textures/entity/combat_update/end_portal.png");
    @Override public void renderTileEntityAt(TileEntity value,double x,double y,double z,float partial){
        TileEntityEndGateway tile=(TileEntityEndGateway)value;
        GL11.glPushMatrix();GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_CURRENT_BIT);
        try {
            GL11.glTranslated(x,y,z);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glColor4f(1,1,1,1);bindTexture(SKY);cube(0,0,0,1,1,1,0,0xFFFFFF);
            bindTexture(PORTAL);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_ONE,GL11.GL_ONE);GL11.glDepthMask(false);
            double time=(tile.getWorldObj().getTotalWorldTime()+partial)*.002;
            for(int layer=0;layer<4;layer++)cube(-.0001,-.0001,-.0001,1.0001,1.0001,1.0001,time*(layer+1),0x205850);
            if(tile.isSpawning()||tile.isCoolingDown()){
                double progress=tile.isSpawning()?tile.spawnProgress(partial):tile.cooldownProgress(partial);
                double height=Math.sin(progress*Math.PI)*(tile.isSpawning()?256:50);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                cube(.35,.5-height,.35,.65,.5+height,.65,0,tile.isSpawning()?0xF700FA:0x7800E0);
            }
        } finally {GL11.glPopAttrib();GL11.glPopMatrix();}
    }
    private static void cube(double x0,double y0,double z0,double x1,double y1,double z1,double scroll,int color){
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setBrightness(15728880);t.setColorOpaque_I(color);
        face(t,x0,y0,z0,x1,y0,z0,x1,y0,z1,x0,y0,z1,scroll);
        face(t,x0,y1,z0,x0,y1,z1,x1,y1,z1,x1,y1,z0,scroll);
        face(t,x0,y0,z0,x0,y1,z0,x1,y1,z0,x1,y0,z0,scroll);
        face(t,x0,y0,z1,x1,y0,z1,x1,y1,z1,x0,y1,z1,scroll);
        face(t,x0,y0,z0,x0,y0,z1,x0,y1,z1,x0,y1,z0,scroll);
        face(t,x1,y0,z0,x1,y1,z0,x1,y1,z1,x1,y0,z1,scroll);t.draw();
    }
    private static void face(Tessellator t,double a,double b,double c,double d,double e,double f,double g,double h,double i,double j,double k,double l,double v){
        t.addVertexWithUV(a,b,c,0,v);t.addVertexWithUV(d,e,f,1,v);t.addVertexWithUV(g,h,i,1,v+1);t.addVertexWithUV(j,k,l,0,v+1);
    }
}
