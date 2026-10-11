package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoat;

/** 1.9 boat hull atlas plus animated left/right paddles. */
public final class ModelCombatBoat120 extends ModelBase {
    public final ModelRenderer[] hull = new ModelRenderer[5];
    public final ModelRenderer leftPaddle;
    public final ModelRenderer rightPaddle;
    private float partialTick;

    public void setPartialTick(float value) { partialTick = value; }

    public ModelCombatBoat120() {
        textureWidth = 128; textureHeight = 64;
        hull[0] = part(0,0,-14,-9,-3,28,16,3,0,3,1,(float)Math.PI/2,0);
        hull[1] = part(0,19,-13,-7,-1,18,6,2,-15,4,4,0,(float)Math.PI*1.5F);
        hull[2] = part(0,27,-8,-7,-1,16,6,2,15,4,0,0,(float)Math.PI/2);
        hull[3] = part(0,35,-14,-7,-1,28,6,2,0,4,-9,0,(float)Math.PI);
        hull[4] = part(0,43,-14,-7,-1,28,6,2,0,4,9,0,0);
        leftPaddle = paddle(false); rightPaddle = paddle(true);
        leftPaddle.setRotationPoint(3.0F, -5.0F, 9.0F);
        rightPaddle.setRotationPoint(3.0F, -5.0F, -9.0F);
        leftPaddle.rotateAngleZ = rightPaddle.rotateAngleZ = 0.19634955F;
    }
    private ModelRenderer part(int u,int v,float x,float y,float z,int w,int h,int d,
                               float px,float py,float pz,float rx,float ry) {
        ModelRenderer model=new ModelRenderer(this,u,v);
        model.addBox(x,y,z,w,h,d);model.setRotationPoint(px,py,pz);
        model.rotateAngleX=rx;model.rotateAngleY=ry;return model;
    }
    private ModelRenderer paddle(boolean right) {
        ModelRenderer model = new ModelRenderer(this, 62, right ? 20 : 0);
        model.addBox(-1.0F, 0.0F, -5.0F, 2, 2, 18);
        model.addBox(right ? 0.001F : -1.001F, -3.0F, 8.0F, 1, 6, 7);
        return model;
    }
    @Override public void render(Entity raw, float a, float b, float c, float d, float e, float scale) {
        for (ModelRenderer side : hull) side.render(scale);
        EntityCombatBoat boat = (EntityCombatBoat) raw;
        animate(leftPaddle, boat.getRowingTime(0, partialTick), false);
        animate(rightPaddle, boat.getRowingTime(1, partialTick), true);
        leftPaddle.render(scale); rightPaddle.render(scale);
    }
    private static void animate(ModelRenderer paddle, float phase, boolean right) {
        float swing = ((float)Math.sin(-phase) + 1.0F) * 0.5F;
        paddle.rotateAngleX = lerp(-(float)Math.PI / 3.0F, -(float)Math.PI / 12.0F, swing);
        paddle.rotateAngleY = lerp(-(float)Math.PI / 4.0F, (float)Math.PI / 4.0F,
                ((float)Math.sin(-phase + 1.0F) + 1.0F) * 0.5F);
        if (right) paddle.rotateAngleY = (float)Math.PI - paddle.rotateAngleY;
    }
    private static float lerp(float from, float to, float amount) { return from + (to - from) * amount; }
}
