package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPanda120;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Gene-aware panda renderer, including the 1.20 eating and rolling poses. */
public final class RenderPanda120 extends RenderLiving {
    private static final ResourceLocation[] TEXTURES=new ResourceLocation[7];
    static {for(EntityPanda120.Gene gene:EntityPanda120.Gene.values())TEXTURES[gene.id]=new ResourceLocation(
            "htgt6","textures/entity/panda/"+(gene==EntityPanda120.Gene.NORMAL?"panda":gene.name+"_panda")+".png");}
    public RenderPanda120(){super(new ModelPanda120(),.9F);}
    private static final PandaTransform120.Sink GL=new PandaTransform120.Sink() {
        public void translate(float x,float y,float z){GL11.glTranslatef(x,y,z);}
        public void rotate(float degrees,float x,float y,float z){GL11.glRotatef(degrees,x,y,z);}
    };
    @Override protected ResourceLocation getEntityTexture(Entity entity){return TEXTURES[((EntityPanda120)entity).getVariant().id];}
    @Override protected void rotateCorpse(EntityLivingBase living,float age,float yaw,float partial){
        super.rotateCorpse(living,age,yaw,partial);EntityPanda120 panda=(EntityPanda120)living;
        PandaTransform120.body(panda,partial,GL);
    }
    public static float backRotation(EntityPanda120 panda,float partial) { return panda.rotationPitch+panda.getOnBackAmount(partial)*180F; }
    @Override protected void renderEquippedItems(EntityLivingBase living,float partial){
        super.renderEquippedItems(living,partial);EntityPanda120 panda=(EntityPanda120)living;
        ItemStack stack=panda.getEquipmentInSlot(0);if(!PandaTransform120.showsFood(panda))return;
        GL11.glPushMatrix();
        try {
            PandaTransform120.food(panda,panda.ticksExisted+partial,GL);
            // Legacy item adapter retained until modern GROUND display transforms are ported.
            GL11.glRotatef(90F,1,0,0);GL11.glScalef(.8F,.8F,.8F);
            Minecraft.getMinecraft().entityRenderer.itemRenderer.renderItem(living,stack,0);
        } finally { GL11.glPopMatrix(); }
    }
}
