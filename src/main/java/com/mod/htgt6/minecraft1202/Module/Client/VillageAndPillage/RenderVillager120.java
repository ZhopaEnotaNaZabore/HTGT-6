package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerSleep120;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderVillager;
import net.minecraft.client.renderer.texture.LayeredTexture;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** 1.20 biome/profession/level villager skins composed by the native 1.7 texture manager. */
public final class RenderVillager120 extends RenderVillager {
    private static final String ROOT="htgt6:textures/entity/village_pillage/villager/";
    public RenderVillager120(){
        super();ModelVillager120 model=new ModelVillager120(0);
        mainModel=model;villagerModel=model;
    }
    @Override protected ResourceLocation getEntityTexture(EntityVillager villager){
        return new ResourceLocation(ROOT+"villager.png");
    }
    /** Separate native render passes preserve the per-layer hat visibility rules. */
    @Override protected void renderModel(EntityLivingBase entity,float swing,float amount,float age,
            float yaw,float pitch,float scale){
        ModelVillager120 model=(ModelVillager120)mainModel;
        model.setHeadVisible(true);
        super.renderModel(entity,swing,amount,age,yaw,pitch,scale);
        if(!(entity instanceof EntityVillager)||entity.isInvisible())return;
        EntityVillager villager=(EntityVillager)entity;
        String type=VillagerCareers112.getVisualType(villager);
        String profession=VillagerCareers112.getVisualProfession(villager);
        String typePath=ROOT+"type/"+type+".png",professionPath=ROOT+"profession/"+profession+".png";
        String professionHat=VillagerHatMetadata120.hat(professionPath);
        boolean head="none".equals(professionHat)||"partial".equals(professionHat)
                &&!"full".equals(VillagerHatMetadata120.hat(typePath));
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_CURRENT_BIT);
        try{
            GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1,1,1,1);
            model.setHeadVisible(head);bindTexture(new ResourceLocation(typePath));
            model.render(entity,swing,amount,age,yaw,pitch,scale);
            model.setHeadVisible(true);
            if(!villager.isChild()&&!"none".equals(profession)&&profession.length()>0){
                bindTexture(new ResourceLocation(professionPath));model.render(entity,swing,amount,age,yaw,pitch,scale);
                if(!"nitwit".equals(profession)){
                    int level=Math.max(1,Math.min(5,VillagerCareers112.getVisualLevel(villager)));
                    bindTexture(new ResourceLocation(ROOT+"profession_level/"+new String[]{"stone","iron","gold","emerald","diamond"}[level-1]+".png"));
                    model.render(entity,swing,amount,age,yaw,pitch,scale);
                }
            }
        }finally{model.setHeadVisible(true);GL11.glPopAttrib();}
    }

    @Override protected void rotateCorpse(EntityLivingBase entity, float age, float yaw, float partialTicks) {
        if (entity instanceof EntityVillager && VillagerSleep120.isSleeping((EntityVillager)entity)) {
            GL11.glRotatef(VillagerSleep120.renderYaw((EntityVillager)entity), 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(270.0F, 0.0F, 1.0F, 0.0F);
            return;
        }
        super.rotateCorpse(entity, age, yaw, partialTicks);
    }

    @Override protected void renderLivingAt(EntityLivingBase entity,double x,double y,double z){
        if(entity instanceof EntityVillager&&VillagerSleep120.isSleeping((EntityVillager)entity)){
            switch(VillagerSleep120.bedDirection((EntityVillager)entity)){
                case 0:z-=1.8D;break;case 1:x+=1.8D;break;case 2:z+=1.8D;break;default:x-=1.8D;
            }
        }
        super.renderLivingAt(entity,x,y,z);
    }
}
