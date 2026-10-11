package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAllay120;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Allay renderer with the synchronized carried item placed between both hands. */
public final class RenderAllay120 extends RenderLiving {
    private static final ResourceLocation TEXTURE=new ResourceLocation("htgt6","textures/entity/allay/allay.png");
    public RenderAllay120(){super(new ModelAllay120(),.25F);}
    @Override protected ResourceLocation getEntityTexture(Entity entity){return TEXTURE;}
    @Override protected void renderEquippedItems(EntityLivingBase living,float partialTick){
        super.renderEquippedItems(living,partialTick);ItemStack stack=((EntityAllay120)living).getCarriedStack();if(stack==null)return;
        GL11.glPushMatrix();GL11.glTranslatef(0.0F,1.05F,-.38F);GL11.glRotatef(-35.0F,1,0,0);GL11.glScalef(.55F,.55F,.55F);
        Minecraft.getMinecraft().entityRenderer.itemRenderer.renderItem(living,stack,0);GL11.glPopMatrix();
    }
}
