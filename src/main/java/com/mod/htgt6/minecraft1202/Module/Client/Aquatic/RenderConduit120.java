package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.TileEntityConduit120;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Conduit TESR with a complete GL-state boundary for Angelica's state cache. */
public final class RenderConduit120 extends TileEntitySpecialRenderer {
    private static final ResourceLocation BASE=texture("base"),CAGE=texture("cage"),WIND=texture("wind"),OPEN_EYE=texture("open_eye"),CLOSED_EYE=texture("closed_eye");
    private final ModelRenderer core,cage,wind;
    public RenderConduit120(){net.minecraft.client.model.ModelBase model=new net.minecraft.client.model.ModelBase(){};model.textureWidth=32;model.textureHeight=32;core=new ModelRenderer(model,0,0);core.addBox(-4,-4,-4,8,8,8);cage=new ModelRenderer(model,0,0);cage.addBox(-5,-5,-5,10,10,10,.15F);wind=new ModelRenderer(model,0,0);wind.addBox(-5,-5,-5,10,10,10,.35F);}
    private static ResourceLocation texture(String name){return new ResourceLocation("htgt6","textures/entity/conduit/"+name+".png");}
    @Override public void renderTileEntityAt(TileEntity raw,double x,double y,double z,float partial){
        try(RenderStateGuard112 ignored=RenderStateGuard112.capture()){
            GL11.glPushMatrix();try{TileEntityConduit120 tile=(TileEntityConduit120)raw;GL11.glTranslatef((float)x+.5F,(float)y+.5F,(float)z+.5F);float t=tile.getWorldObj()==null?0:tile.getWorldObj().getTotalWorldTime()+partial;GL11.glRotatef(t*2F,0,1,0);bindTexture(BASE);core.render(1F/16F);if(tile.isActive()){GL11.glRotatef(-t*3F,1,0,0);bindTexture(CAGE);cage.render(1F/16F);bindTexture(WIND);wind.render(1F/16F);}eye(tile.isActive()&&((int)t/40)%3!=0);}finally{GL11.glPopMatrix();}
        }
    }
    private void eye(boolean open){bindTexture(open?OPEN_EYE:CLOSED_EYE);GL11.glDisable(GL11.GL_LIGHTING);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);Tessellator q=Tessellator.instance;q.startDrawingQuads();q.addVertexWithUV(-.25,-.25,-.36,0,1);q.addVertexWithUV(.25,-.25,-.36,1,1);q.addVertexWithUV(.25,.25,-.36,1,0);q.addVertexWithUV(-.25,.25,-.36,0,0);q.draw();}
}
