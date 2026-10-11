package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.Tessellator;

/** Single two-dimensional model face used by the modern villager hat brim. */
final class ModelPlane120 extends ModelBox {
    private final TexturedQuad face;

    ModelPlane120(ModelRenderer owner,int u,int v,float x,float y,float z,int width,int depth){
        super(owner,u,v,x,y,z,width,0,depth,0.0F);
        PositionTextureVertex nw=new PositionTextureVertex(x,y,z,0,0);
        PositionTextureVertex ne=new PositionTextureVertex(x+width,y,z,0,0);
        PositionTextureVertex se=new PositionTextureVertex(x+width,y,z+depth,0,0);
        PositionTextureVertex sw=new PositionTextureVertex(x,y,z+depth,0,0);
        face=new TexturedQuad(new PositionTextureVertex[]{se,sw,nw,ne},u,v,u+width,v+depth,
                owner.textureWidth,owner.textureHeight);
    }

    @Override public void render(Tessellator tessellator,float scale){face.draw(tessellator,scale);}
}
