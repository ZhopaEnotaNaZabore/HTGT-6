package com.mod.htgt6.minecraft1202.Module.Client.Exploration;

import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityEvokerFangs112;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class RenderEvokerFangs112 extends Render {
    private static final ResourceLocation TEXTURE = new ResourceLocation("htgt6", "textures/entity/exploration_update/illager/fangs.png");
    private final net.minecraft.client.model.ModelBase model = new net.minecraft.client.model.ModelBase() { };
    private final ModelRenderer base, upper, lower;
    public RenderEvokerFangs112() {
        model.textureWidth=64;model.textureHeight=32;
        base=new ModelRenderer(model,0,0);base.addBox(0,0,0,10,12,10);base.setRotationPoint(-5,24,-5);
        upper=new ModelRenderer(model,40,0);upper.addBox(0,0,0,4,14,8);upper.setRotationPoint(1.5F,24,-4);
        lower=new ModelRenderer(model,40,0);lower.addBox(0,0,0,4,14,8);lower.setRotationPoint(-1.5F,24,4);lower.rotateAngleY=(float)Math.PI;
    }
    @Override public void doRender(Entity entity,double x,double y,double z,float yaw,float partial) {
        float progress=((EntityEvokerFangs112)entity).animationProgress(partial); if(progress<=0.0F)return;
        float opening=Math.min(1,progress*2);opening=1-opening*opening*opening;
        upper.rotateAngleZ=(float)Math.PI-opening*.35F*(float)Math.PI;
        lower.rotateAngleZ=(float)Math.PI+opening*.35F*(float)Math.PI;
        base.rotationPointY=upper.rotationPointY=lower.rotationPointY=24-(progress+(float)Math.sin(progress*2.7F))*.6F*12;
        float size=progress>.9F?2*(1-progress)/.1F:2;
        bindTexture(TEXTURE); GL11.glPushMatrix();
        try { GL11.glTranslated(x,y,z); GL11.glRotatef(90.0F-yaw,0,1,0);
            GL11.glScalef(-size,-size,size);GL11.glTranslatef(0,-.626F,0);GL11.glScalef(.5F,.5F,.5F);
            base.render(.0625F);upper.render(.0625F);lower.render(.0625F);
        } finally { GL11.glPopMatrix(); }
    }
    @Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}
