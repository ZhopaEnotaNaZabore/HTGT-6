package com.mod.htgt6.minecraft1202.Module.Client.Frostburn;

import com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.TileEntityStructure120;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

/** Preview for creative editors; no texture or global render state is retained. */
public final class RenderStructure120 extends TileEntitySpecialRenderer {
    @Override public void renderTileEntityAt(TileEntity value,double x,double y,double z,float partial) {
        if(!(value instanceof TileEntityStructure120)||Minecraft.getMinecraft().thePlayer==null
                ||!Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode)return;
        TileEntityStructure120 tile=(TileEntityStructure120)value;
        if((tile.getBlockMetadata()==1&&!tile.showBoundingBox)||tile.getBlockMetadata()>1||tile.sizeX<=0||tile.sizeY<=0||tile.sizeZ<=0)return;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_LINE_BIT|GL11.GL_COLOR_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x+tile.offsetX,y+tile.offsetY,z+tile.offsetZ);
            GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glLineWidth(2F);GL11.glColor4f(.7F,.85F,1F,.9F);
            int[] bounds=tile.previewBounds();
            RenderGlobal.drawOutlinedBoundingBox(AxisAlignedBB.getBoundingBox(bounds[0],0,bounds[1],bounds[2],tile.sizeY,bounds[3]),-1);
        } finally {GL11.glPopMatrix();GL11.glPopAttrib();}
    }
}
