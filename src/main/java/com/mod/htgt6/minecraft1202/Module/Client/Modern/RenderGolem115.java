package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import net.minecraft.client.renderer.entity.RenderIronGolem;import net.minecraft.entity.EntityLivingBase;import net.minecraft.util.ResourceLocation;import org.lwjgl.opengl.GL11;
public final class RenderGolem115 extends RenderIronGolem {
 @Override protected int shouldRenderPass(EntityLivingBase e,int pass,float partial){if(pass!=0||e.isInvisible())return -1;float health=e.getHealth()/e.getMaxHealth();String level=health<.25F?"high":health<.5F?"medium":health<.75F?"low":null;if(level==null)return -1;setRenderPassModel(mainModel);bindTexture(new ResourceLocation("htgt6","textures/entity/iron_golem/iron_golem_crackiness_"+level+".png"));GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glColor4f(1,1,1,1);return 1;}
}
