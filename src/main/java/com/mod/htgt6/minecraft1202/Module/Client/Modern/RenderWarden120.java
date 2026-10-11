package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWarden120;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Native renderer with all five independently masked emissive layers. */
public final class RenderWarden120 extends RenderLiving {
    private static ResourceLocation texture(String name) {
        return new ResourceLocation("htgt6","textures/entity/warden/"+name+".png");
    }
    private static final ResourceLocation TEXTURE=texture("warden");
    private static final ResourceLocation[] LAYERS={texture("warden_bioluminescent_layer"),
            texture("warden_pulsating_spots_1"),texture("warden_pulsating_spots_2"),
            TEXTURE,texture("warden_heart")};
    public RenderWarden120(){super(new ModelWarden120(),.9F);}
    @Override protected ResourceLocation getEntityTexture(Entity entity){return TEXTURE;}

    // 1.7.10 has only four legacy render passes. Draw the five layers here, using
    // the already calculated pose and preserving the caller's GL/lightmap state.
    @Override protected void renderEquippedItems(EntityLivingBase living,float partial) {
        super.renderEquippedItems(living,partial);
        if(living.isInvisible()) return;
        EntityWarden120 warden=(EntityWarden120)living;
        ModelWarden120 model=(ModelWarden120)mainModel;
        float age=warden.ticksExisted+partial;
        float heart=(float)((Math.cos(age*(.12F+.18F*warden.getHeartIntensity()))+1)*.5);
        float[] alpha={1F,Math.max(0,(float)Math.cos(age*.045F)*.25F),
                Math.max(0,(float)Math.cos(age*.045F+Math.PI)*.25F),warden.getHeartIntensity(),heart};
        float lightX=OpenGlHelper.lastBrightnessX,lightY=OpenGlHelper.lastBrightnessY;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_CURRENT_BIT);
        try {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDepthMask(false);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,240F,240F);
            for(int layer=0;layer<LAYERS.length;layer++) {
                if(alpha[layer]<=0) continue;
                bindTexture(LAYERS[layer]);
                GL11.glColor4f(1F,1F,1F,alpha[layer]);
                model.selectLayer(layer);
                model.bone.render(.0625F);
            }
        } finally {
            model.selectLayer(-1);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,lightX,lightY);
            GL11.glPopAttrib();
        }
    }
}
