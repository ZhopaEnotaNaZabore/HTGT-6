package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** FireworkEntityRenderer's camera-facing ground item with the shot-at-angle rotations. */
public final class RenderCrossbowRocket143 extends Render {
    @Override public void doRender(Entity rocket,double x,double y,double z,float yaw,float partial){
        IIcon icon=Items.fireworks.getIconFromDamage(0);if(icon==null)return;
        try(RenderStateGuard112 ignored=RenderStateGuard112.capture()){
            GL11.glPushMatrix();
            try{
                GL11.glTranslated(x,y,z);bindEntityTexture(rocket);
                GL11.glColor4f(1,1,1,1);GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glRotatef(180-renderManager.playerViewY,0,1,0);
                GL11.glRotatef(-renderManager.playerViewX,1,0,0);
                GL11.glRotatef(180,0,0,1);GL11.glRotatef(180,0,1,0);GL11.glRotatef(90,1,0,0);
                GL11.glScalef(.5F,.5F,.5F);GL11.glTranslatef(-.5F,-.5F,0);
                ItemRenderer.renderItemIn2D(Tessellator.instance,icon.getMaxU(),icon.getMinV(),icon.getMinU(),icon.getMaxV(),icon.getIconWidth(),icon.getIconHeight(),.0625F);
            }finally{GL11.glPopMatrix();}
        }
    }
    @Override protected ResourceLocation getEntityTexture(Entity entity){return TextureMap.locationItemsTexture;}
}
