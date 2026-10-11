package com.mod.htgt6.minecraft1202.Module.Client.Exploration;

import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityEvoker112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVindicator112;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/** Exact 1.12 Illager cuboids and crossed/attacking/spellcasting poses. */
public final class ModelIllager112 extends ModelBiped {
    private final ModelRenderer crossedArms;
    public ModelIllager112(){super(0,0,64,64);
        bipedHead=new ModelRenderer(this,0,0);bipedHead.addBox(-4,-10,-4,8,10,8);
        ModelRenderer hood=new ModelRenderer(this,32,0);hood.addBox(-4,-10,-4,8,12,8,.45F);hood.showModel=false;bipedHead.addChild(hood);
        ModelRenderer nose=new ModelRenderer(this,24,0);nose.setRotationPoint(0,-2,0);nose.addBox(-1,-1,-6,2,4,2);bipedHead.addChild(nose);
        bipedBody=new ModelRenderer(this,16,20);bipedBody.addBox(-4,0,-3,8,12,6);bipedBody.setTextureOffset(0,38).addBox(-4,0,-3,8,18,6,.5F);
        crossedArms=new ModelRenderer(this,44,22);crossedArms.setRotationPoint(0,2,0);crossedArms.addBox(-8,-2,-2,4,8,4);
        ModelRenderer crossedRight=new ModelRenderer(this,44,22);crossedRight.mirror=true;crossedRight.addBox(4,-2,-2,4,8,4);crossedArms.addChild(crossedRight);crossedArms.setTextureOffset(40,38).addBox(-4,2,-2,8,4,4);
        bipedRightArm=new ModelRenderer(this,40,46);bipedRightArm.addBox(-3,-2,-2,4,12,4);bipedRightArm.setRotationPoint(-5,2,0);
        bipedLeftArm=new ModelRenderer(this,40,46);bipedLeftArm.mirror=true;bipedLeftArm.addBox(-1,-2,-2,4,12,4);bipedLeftArm.setRotationPoint(5,2,0);
        bipedRightLeg=new ModelRenderer(this,0,22);bipedRightLeg.addBox(-2,0,-2,4,12,4);bipedRightLeg.setRotationPoint(-2,12,0);
        bipedLeftLeg=new ModelRenderer(this,0,22);bipedLeftLeg.mirror=true;bipedLeftLeg.addBox(-2,0,-2,4,12,4);bipedLeftLeg.setRotationPoint(2,12,0);
    }
    @Override public void setRotationAngles(float limb,float amount,float age,float yaw,float pitch,float scale,Entity e){
        bipedHead.rotateAngleY=yaw*((float)Math.PI/180F);bipedHead.rotateAngleX=pitch*((float)Math.PI/180F);
        crossedArms.rotationPointY=3;crossedArms.rotationPointZ=-1;crossedArms.rotateAngleX=-.75F;
        bipedRightLeg.rotateAngleX=MathHelper.cos(limb*.6662F)*1.4F*amount*.5F;bipedLeftLeg.rotateAngleX=MathHelper.cos(limb*.6662F+(float)Math.PI)*1.4F*amount*.5F;bipedRightLeg.rotateAngleY=bipedLeftLeg.rotateAngleY=0;
        boolean casting=e instanceof EntityEvoker112&&((EntityEvoker112)e).isSpellcasting();boolean attacking=e instanceof EntityVindicator112&&((EntityVindicator112)e).isAggressive();
        crossedArms.showModel=!casting&&!attacking;bipedRightArm.showModel=casting||attacking;bipedLeftArm.showModel=casting||attacking;
        bipedRightArm.rotateAngleX=bipedLeftArm.rotateAngleX=bipedRightArm.rotateAngleY=bipedLeftArm.rotateAngleY=0;bipedRightArm.rotateAngleZ=bipedLeftArm.rotateAngleZ=0;
        if(casting){bipedRightArm.rotationPointX=-5;bipedLeftArm.rotationPointX=5;bipedRightArm.rotationPointZ=bipedLeftArm.rotationPointZ=0;bipedRightArm.rotateAngleX=bipedLeftArm.rotateAngleX=MathHelper.cos(age*.6662F)*.25F;bipedRightArm.rotateAngleZ=2.3561945F;bipedLeftArm.rotateAngleZ=-2.3561945F;}
        else if(attacking){float a=MathHelper.sin(onGround*(float)Math.PI),b=MathHelper.sin((1-(1-onGround)*(1-onGround))*(float)Math.PI);bipedRightArm.rotateAngleY=.15707964F;bipedLeftArm.rotateAngleY=-.15707964F;bipedRightArm.rotateAngleX=-1.8849558F+MathHelper.cos(age*.09F)*.15F+a*2.2F-b*.4F;bipedLeftArm.rotateAngleX=MathHelper.cos(age*.19F)*.5F+a*1.2F-b*.4F;bipedRightArm.rotateAngleZ=MathHelper.cos(age*.09F)*.05F+.05F;bipedLeftArm.rotateAngleZ=-bipedRightArm.rotateAngleZ;}
    }
    @Override public void render(Entity e,float l,float a,float age,float yaw,float pitch,float s){setRotationAngles(l,a,age,yaw,pitch,s,e);bipedHead.render(s);bipedBody.render(s);bipedRightLeg.render(s);bipedLeftLeg.render(s);if(crossedArms.showModel)crossedArms.render(s);else{bipedRightArm.render(s);bipedLeftArm.render(s);}}
}
