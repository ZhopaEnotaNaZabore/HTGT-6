package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/** Exact four-segment Endermite model used by the modern client. */
public final class ModelBountifulEndermite extends ModelBase {
    private static final int[][] SIZES={{4,3,2},{6,4,5},{3,3,1},{1,2,1}};
    private static final int[][] UV={{0,0},{0,5},{0,14},{0,18}};
    private final ModelRenderer[] body=new ModelRenderer[SIZES.length];
    public ModelBountifulEndermite(){textureWidth=64;textureHeight=32;float z=-3.5F;
        for(int i=0;i<body.length;i++){int[] size=SIZES[i];body[i]=new ModelRenderer(this,UV[i][0],UV[i][1]);
            body[i].addBox(-size[0]*.5F,0,-size[2]*.5F,size[0],size[1],size[2]);
            body[i].setRotationPoint(0,24-size[1],z);if(i<body.length-1)
                z+=(size[2]+SIZES[i+1][2])*.5F;}}
    @Override public void setRotationAngles(float limb,float amount,float age,float yaw,float pitch,
                                             float scale,Entity entity){
        for(int i=0;i<body.length;i++){float distance=Math.abs(i-2);
            body[i].rotateAngleY=MathHelper.cos(age*.9F+i*.15F*(float)Math.PI)
                    *(float)Math.PI*.01F*(1+distance);
            body[i].rotationPointX=MathHelper.sin(age*.9F+i*.15F*(float)Math.PI)
                    *(float)Math.PI*.1F*distance;}}
    @Override public void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale){
        setRotationAngles(limb,amount,age,yaw,pitch,scale,entity);
        for(ModelRenderer part:body)part.render(scale);}
}
