package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Two-part, six-direction End Rod model adapted to the 1.7 block renderer. */
public final class EndRodRenderHandler implements ISimpleBlockRenderingHandler {
    private final int renderId;

    public EndRodRenderHandler(int renderId) { this.renderId = renderId; }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        Tessellator tessellator = Tessellator.instance;
        IIcon icon = block.getIcon(0, 0);
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        // The end-rod atlas sprite only occupies a few pixels. RenderBlocks maps
        // the complete, mostly transparent sprite to every face, which makes the
        // inventory model appear empty. Use the same cropped UVs as the world model.
        cuboid(tessellator, icon, 0, 0, 0, .375, 0, .375, .625, .0625, .625, 1, true);
        cuboid(tessellator, icon, 0, 0, 0, .4375, .0625, .4375, .5625, 1, .5625, 1, false);
        tessellator.draw();
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        Tessellator t=Tessellator.instance;IIcon icon=block.getIcon(0,0);
        t.setBrightness(block.getMixedBrightnessForBlock(world,x,y,z));t.setColorOpaque_F(1F,1F,1F);
        switch (world.getBlockMetadata(x, y, z)) {
            case 0:
                cuboid(t,icon,x,y,z,.375,.9375,.375,.625,1,.625,1,true);
                cuboid(t,icon,x,y,z,.4375,0,.4375,.5625,.9375,.5625,1,false);
                break;
            case 2:
                cuboid(t,icon,x,y,z,.375,.375,.9375,.625,.625,1,2,true);
                cuboid(t,icon,x,y,z,.4375,.4375,0,.5625,.5625,.9375,2,false);
                break;
            case 3:
                cuboid(t,icon,x,y,z,.375,.375,0,.625,.625,.0625,2,true);
                cuboid(t,icon,x,y,z,.4375,.4375,.0625,.5625,.5625,1,2,false);
                break;
            case 4:
                cuboid(t,icon,x,y,z,.9375,.375,.375,1,.625,.625,0,true);
                cuboid(t,icon,x,y,z,0,.4375,.4375,.9375,.5625,.5625,0,false);
                break;
            case 5:
                cuboid(t,icon,x,y,z,0,.375,.375,.0625,.625,.625,0,true);
                cuboid(t,icon,x,y,z,.0625,.4375,.4375,1,.5625,.5625,0,false);
                break;
            default:
                cuboid(t,icon,x,y,z,.375,0,.375,.625,.0625,.625,1,true);
                cuboid(t,icon,x,y,z,.4375,.0625,.4375,.5625,1,.5625,1,false);
        }
        return true;
    }

    private static void cuboid(Tessellator t,IIcon i,int bx,int by,int bz,double ax,double ay,double az,double bx2,double by2,double bz2,int axis,boolean cap){
        double x0=bx+ax,y0=by+ay,z0=bz+az,x1=bx+bx2,y1=by+by2,z1=bz+bz2;
        t.setNormal(0,-1,0);face(t,i,x0,y0,z0,x1,y0,z0,axis==1,cap,x1,y0,z1,x0,y0,z1);
        t.setNormal(0,1,0);face(t,i,x0,y1,z1,x1,y1,z1,axis==1,cap,x1,y1,z0,x0,y1,z0);
        t.setNormal(0,0,-1);face(t,i,x0,y0,z0,x0,y1,z0,axis==2,cap,x1,y1,z0,x1,y0,z0);
        t.setNormal(0,0,1);face(t,i,x1,y0,z1,x1,y1,z1,axis==2,cap,x0,y1,z1,x0,y0,z1);
        t.setNormal(-1,0,0);face(t,i,x0,y0,z1,x0,y1,z1,axis==0,cap,x0,y1,z0,x0,y0,z0);
        t.setNormal(1,0,0);face(t,i,x1,y0,z0,x1,y1,z0,axis==0,cap,x1,y1,z1,x1,y0,z1);
    }
    private static void face(Tessellator t,IIcon i,double x0,double y0,double z0,double x1,double y1,double z1,boolean end,boolean cap,double x2,double y2,double z2,double x3,double y3,double z3){
        double u0=i.getInterpolatedU(end?(cap?2:2):(cap?2:0)),u1=i.getInterpolatedU(end?(cap?6:4):(cap?6:2));
        double v0=i.getInterpolatedV(end?(cap?2:0):(cap?6:0)),v1=i.getInterpolatedV(end?(cap?6:2):(cap?7:15));
        t.addVertexWithUV(x0,y0,z0,u0,v1);t.addVertexWithUV(x1,y1,z1,u1,v1);t.addVertexWithUV(x2,y2,z2,u1,v0);t.addVertexWithUV(x3,y3,z3,u0,v0);
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return renderId; }
}
