package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockColoredBed120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
/** Exact 64x64 bed cuboids and pivots, using native ModelRenderer. */
public final class RenderColoredBed120 extends TileEntitySpecialRenderer {
    private final ModelRenderer[] head=half(true),foot=half(false);
    private static ModelRenderer[] half(boolean head){
        ModelBase model=new ModelBase(){};model.textureWidth=model.textureHeight=64;
        ModelRenderer main=new ModelRenderer(model,0,head?0:22);main.addBox(0,0,0,16,16,6);
        ModelRenderer left=new ModelRenderer(model,50,head?6:0);left.addBox(0,6,head?0:-16,3,3,3);
        left.rotateAngleX=(float)Math.PI/2;left.rotateAngleZ=head?(float)Math.PI/2:0;
        ModelRenderer right=new ModelRenderer(model,50,head?18:12);right.addBox(-16,6,head?0:-16,3,3,3);
        right.rotateAngleX=(float)Math.PI/2;right.rotateAngleZ=head?(float)Math.PI:(float)Math.PI*1.5F;
        return new ModelRenderer[]{main,left,right};
    }
    public void renderTileEntityAt(TileEntity tile,double x,double y,double z,float partial){
        if(!(tile.getBlockType() instanceof BlockColoredBed120))return;
        int color=((BlockColoredBed120)tile.getBlockType()).getColor(),meta=tile.getBlockMetadata();
        GL11.glPushMatrix();try{GL11.glTranslated(x,y,z);renderHalf(color,(meta&8)!=0,meta&3);}finally{GL11.glPopMatrix();}
    }
    public void renderHalf(int color,boolean isHead,int direction){
        net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("htgt6","textures/entity/village_pillage/bed/"+VillagePillageContent.BED_COLORS[color&15]+".png"));
        GL11.glPushMatrix();try{
            GL11.glTranslatef(0,.5625F,0);GL11.glRotatef(90,1,0,0);
            GL11.glTranslatef(.5F,.5F,.5F);GL11.glRotatef(180+direction*90,0,0,1);GL11.glTranslatef(-.5F,-.5F,-.5F);
            for(ModelRenderer part:isHead?head:foot)part.render(.0625F);
        }finally{GL11.glPopMatrix();}
    }
}
