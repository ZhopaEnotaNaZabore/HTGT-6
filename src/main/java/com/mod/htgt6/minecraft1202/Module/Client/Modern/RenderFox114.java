package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import net.minecraft.client.renderer.entity.RenderLiving;import net.minecraft.entity.*;import net.minecraft.util.ResourceLocation;import net.minecraft.item.ItemStack;import org.lwjgl.opengl.GL11;import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityFox114;
public final class RenderFox114 extends RenderLiving {
 public RenderFox114(){super(new ModelFox114(),.4F);}
 protected ResourceLocation getEntityTexture(Entity e){EntityFox114 f=(EntityFox114)e;return new ResourceLocation("htgt6","textures/entity/village114/fox/"+(f.snow()?"snow_fox":"fox")+(f.sleeping()?"_sleep":"")+".png");}
 protected void rotateCorpse(EntityLivingBase e,float age,float yaw,float partial){super.rotateCorpse(e,age,yaw,partial);if(((EntityFox114)e).pouncing()||((EntityFox114)e).faceplanted())GL11.glRotatef(-(e.prevRotationPitch+(e.rotationPitch-e.prevRotationPitch)*partial),1,0,0);}
 protected void renderEquippedItems(EntityLivingBase e,float partial){super.renderEquippedItems(e,partial);ItemStack stack=e.getHeldItem();if(stack==null)return;GL11.glPushMatrix();if(e.isChild()){GL11.glScalef(.75F,.75F,.75F);GL11.glTranslatef(0,.5F,.209375F);}((ModelFox114)mainModel).head.postRender(.0625F);GL11.glTranslatef(.0625F,.1875F,-.5F);GL11.glRotatef(90,1,0,0);GL11.glScalef(.5F,.5F,.5F);renderManager.itemRenderer.renderItem(e,stack,0);GL11.glPopMatrix();}
}
