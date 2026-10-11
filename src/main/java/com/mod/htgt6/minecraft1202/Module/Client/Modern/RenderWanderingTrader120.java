package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import net.minecraft.client.renderer.entity.RenderVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;

/** Official Wandering Trader skin on the native 1.7 villager model. */
public final class RenderWanderingTrader120 extends RenderVillager {
    private static final ResourceLocation TEXTURE=new ResourceLocation("htgt6","textures/entity/wandering_trader.png");
    @Override protected ResourceLocation getEntityTexture(EntityVillager entity){return TEXTURE;}
    @Override protected void renderEquippedItems(net.minecraft.entity.EntityLivingBase entity,float partial){
        super.renderEquippedItems(entity,partial);net.minecraft.item.ItemStack held=entity.getHeldItem();if(held==null)return;
        org.lwjgl.opengl.GL11.glPushMatrix();try{org.lwjgl.opengl.GL11.glTranslatef(0,.5F,-.35F);org.lwjgl.opengl.GL11.glRotatef(-35,1,0,0);org.lwjgl.opengl.GL11.glScalef(.6F,.6F,.6F);net.minecraft.client.Minecraft.getMinecraft().entityRenderer.itemRenderer.renderItem(entity,held,0);}finally{org.lwjgl.opengl.GL11.glPopMatrix();}
    }
}
