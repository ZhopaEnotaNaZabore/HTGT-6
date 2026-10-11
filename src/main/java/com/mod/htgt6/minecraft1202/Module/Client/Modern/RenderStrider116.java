package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityStrider116;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.*;
import net.minecraft.util.ResourceLocation;
public final class RenderStrider116 extends RenderLiving {
    private static final ResourceLocation WARM=new ResourceLocation("htgt6","textures/entity/nether/strider.png"),COLD=new ResourceLocation("htgt6","textures/entity/nether/strider_cold.png"),SADDLE=new ResourceLocation("htgt6","textures/entity/nether/strider_saddle.png");
    public RenderStrider116(){super(new ModelStrider116(),.5F);setRenderPassModel(new ModelStrider116());}
    @Override protected ResourceLocation getEntityTexture(Entity e){return ((EntityStrider116)e).isCold()?COLD:WARM;}
    @Override protected int shouldRenderPass(EntityLivingBase entity,int pass,float partial){if(pass!=0||!((EntityStrider116)entity).isSaddled())return -1;bindTexture(SADDLE);renderPassModel.isChild=entity.isChild();return 1;}
    @Override protected void rotateCorpse(EntityLivingBase entity,float age,float yaw,float partial){if(((EntityStrider116)entity).isCold())yaw+=(float)(Math.cos(entity.ticksExisted*3.25)*Math.PI*.4);super.rotateCorpse(entity,age,yaw,partial);}
}
