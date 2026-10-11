package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import org.lwjgl.opengl.GL11;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116;
public final class RenderPiglin116 extends RenderBiped {
    private final ResourceLocation texture;
    public RenderPiglin116(boolean zombie){this(zombie?"zombified_piglin":"piglin");}
    public RenderPiglin116(String name){super(new ModelPiglin116(),.5F);texture=new ResourceLocation("htgt6","textures/entity/nether/"+name+".png");}
    @Override protected ResourceLocation getEntityTexture(Entity e){return texture;}
    @Override protected void renderEquippedItems(EntityLiving entity,float partial){
        super.renderEquippedItems(entity,partial);
        if(!(entity instanceof EntityPiglin116)||!((EntityPiglin116)entity).isAdmiring())return;
        ItemStack admired=((EntityPiglin116)entity).getAdmiredItem();if(admired==null)return;
        GL11.glPushMatrix();try{if(entity.isChild()){GL11.glScalef(.5F,.5F,.5F);GL11.glTranslatef(0,1.5F,0);}((ModelPiglin116)mainModel).bipedLeftArm.postRender(.0625F);GL11.glTranslatef(.0625F,.4375F,.0625F);GL11.glRotatef(-90,1,0,0);GL11.glScalef(.375F,.375F,.375F);renderManager.itemRenderer.renderItem(entity,admired,0);}finally{GL11.glPopMatrix();}
    }
}
