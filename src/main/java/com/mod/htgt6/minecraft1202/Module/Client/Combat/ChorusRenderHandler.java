package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Connected multipart Chorus Plant and recessed Chorus Flower models from 1.12. */
public final class ChorusRenderHandler implements ISimpleBlockRenderingHandler {
    private final int renderId;
    public ChorusRenderHandler(int renderId){this.renderId=renderId;}

    @Override public boolean renderWorldBlock(IBlockAccess world,int x,int y,int z,Block block,int modelId,RenderBlocks renderer){
        if(block==CombatContent.chorusFlower){
            box(renderer,block,x,y,z,.125,.125,.125,.875,.875,.875);
            box(renderer,block,x,y,z,.125,.875,.125,.875,1,.875);box(renderer,block,x,y,z,.125,0,.125,.875,.125,.875);
            box(renderer,block,x,y,z,0,.125,.125,.125,.875,.875);box(renderer,block,x,y,z,.875,.125,.125,1,.875,.875);
            box(renderer,block,x,y,z,.125,.125,0,.875,.875,.125);box(renderer,block,x,y,z,.125,.125,.875,.875,.875,1);
            return true;
        }
        box(renderer,block,x,y,z,.125,.125,.125,.875,.875,.875);
        if(connect(world,x,y-1,z,true))box(renderer,block,x,y,z,.25,0,.25,.75,.25,.75);
        if(connect(world,x,y+1,z,false))box(renderer,block,x,y,z,.25,.75,.25,.75,1,.75);
        if(connect(world,x,y,z-1,false))box(renderer,block,x,y,z,.25,.25,0,.75,.75,.25);
        if(connect(world,x,y,z+1,false))box(renderer,block,x,y,z,.25,.25,.75,.75,.75,1);
        if(connect(world,x-1,y,z,false))box(renderer,block,x,y,z,0,.25,.25,.25,.75,.75);
        if(connect(world,x+1,y,z,false))box(renderer,block,x,y,z,.75,.25,.25,1,.75,.75);
        return true;
    }
    private static boolean connect(IBlockAccess world,int x,int y,int z,boolean down){Block b=world.getBlock(x,y,z);return b==CombatContent.chorusPlant||b==CombatContent.chorusFlower||(down&&b==Blocks.end_stone);}
    private static void box(RenderBlocks r,Block b,int x,int y,int z,double x0,double y0,double z0,double x1,double y1,double z1){r.setRenderBounds(x0,y0,z0,x1,y1,z1);r.renderStandardBlock(b,x,y,z);}

    @Override public void renderInventoryBlock(Block block,int metadata,int modelId,RenderBlocks renderer){GL11.glPushMatrix();GL11.glTranslatef(-.5F,-.5F,-.5F);inventoryBox(renderer,block,.125,.125,.125,.875,.875,.875);GL11.glPopMatrix();}
    private static void inventoryBox(RenderBlocks r,Block b,double x0,double y0,double z0,double x1,double y1,double z1){r.setRenderBounds(x0,y0,z0,x1,y1,z1);Tessellator t=Tessellator.instance;IIcon i=b.getIcon(0,0);t.startDrawingQuads();t.setNormal(0,-1,0);r.renderFaceYNeg(b,0,0,0,i);t.draw();t.startDrawingQuads();t.setNormal(0,1,0);r.renderFaceYPos(b,0,0,0,i);t.draw();t.startDrawingQuads();t.setNormal(0,0,-1);r.renderFaceZNeg(b,0,0,0,i);t.draw();t.startDrawingQuads();t.setNormal(0,0,1);r.renderFaceZPos(b,0,0,0,i);t.draw();t.startDrawingQuads();t.setNormal(-1,0,0);r.renderFaceXNeg(b,0,0,0,i);t.draw();t.startDrawingQuads();t.setNormal(1,0,0);r.renderFaceXPos(b,0,0,0,i);t.draw();}
    @Override public boolean shouldRender3DInInventory(int modelId){return true;}
    @Override public int getRenderId(){return renderId;}
}
