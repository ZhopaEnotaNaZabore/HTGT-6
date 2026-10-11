package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Candle;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Native 1.7 cuboid renderer for the four Minecraft 1.20 candle arrangements. */
public final class CandleRenderHandler120 implements ISimpleBlockRenderingHandler {
    private final int renderId;
    public CandleRenderHandler120(int renderId){this.renderId=renderId;}

    @Override public void renderInventoryBlock(Block block,int metadata,int modelId,RenderBlocks renderer){
        GL11.glPushMatrix();GL11.glTranslatef(-.5F,-.5F,-.5F);
        renderInventoryCube(block,renderer,BlockBackport120Candle.candleBounds(0),metadata);
        GL11.glPopMatrix();renderer.setRenderBoundsFromBlock(block);
    }

    private static void renderInventoryCube(Block block,RenderBlocks renderer,int[] cube,int metadata){
        renderer.setRenderBounds(cube[0]/16D,cube[1]/16D,cube[2]/16D,cube[3]/16D,cube[4]/16D,cube[5]/16D);
        Tessellator t=Tessellator.instance;
        t.startDrawingQuads();renderer.renderFaceYNeg(block,0,0,0,block.getIcon(0,metadata));t.draw();
        t.startDrawingQuads();renderer.renderFaceYPos(block,0,0,0,block.getIcon(1,metadata));t.draw();
        t.startDrawingQuads();renderer.renderFaceZNeg(block,0,0,0,block.getIcon(2,metadata));t.draw();
        t.startDrawingQuads();renderer.renderFaceZPos(block,0,0,0,block.getIcon(3,metadata));t.draw();
        t.startDrawingQuads();renderer.renderFaceXNeg(block,0,0,0,block.getIcon(4,metadata));t.draw();
        t.startDrawingQuads();renderer.renderFaceXPos(block,0,0,0,block.getIcon(5,metadata));t.draw();
    }

    @Override public boolean renderWorldBlock(IBlockAccess world,int x,int y,int z,Block block,int modelId,RenderBlocks renderer){
        int metadata=world.getBlockMetadata(x,y,z),count=BlockBackport120Candle.count(metadata);boolean rendered=false;
        for(int i=0;i<count;i++){
            int[] c=BlockBackport120Candle.candleBounds(i);renderer.setRenderBounds(c[0]/16D,c[1]/16D,c[2]/16D,c[3]/16D,c[4]/16D,c[5]/16D);
            rendered|=renderer.renderStandardBlock(block,x,y,z);
        }
        renderer.setRenderBoundsFromBlock(block);return rendered;
    }
    @Override public boolean shouldRender3DInInventory(int modelId){return true;}
    @Override public int getRenderId(){return renderId;}
}
