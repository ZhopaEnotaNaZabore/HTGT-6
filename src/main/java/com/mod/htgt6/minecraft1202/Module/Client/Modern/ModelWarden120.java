package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWarden120;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/** Warden geometry and state animations implemented on the 1.7 ModelRenderer API. */
public final class ModelWarden120 extends ModelBase {
    public final ModelRenderer bone,body,head,leftTendril,rightTendril,leftRib,rightRib;
    public final ModelRenderer leftArm,rightArm,leftLeg,rightLeg;

    final java.util.Map<String,ModelRenderer> parts=new java.util.LinkedHashMap<String,ModelRenderer>();
    private final java.util.Map<ModelRenderer,ModelRenderer> meshes=new java.util.IdentityHashMap<ModelRenderer,ModelRenderer>();
    private final java.util.Map<ModelRenderer,float[]> pivots=new java.util.IdentityHashMap<ModelRenderer,float[]>();

    public ModelWarden120(){
        textureWidth=128;textureHeight=128;
        bone=new ModelRenderer(this);bone.setRotationPoint(0,24,0);
        body=part(bone,0,0,-9,-13,-4,18,21,11,0,-21,0,false);
        rightRib=part(body,90,11,-2,-11,-.1F,9,21,0,-7,-2,-4,false);
        leftRib=part(body,90,11,-7,-11,-.1F,9,21,0,7,-2,-4,true);
        head=part(body,0,32,-8,-16,-5,16,16,10,0,-13,0,false);
        rightTendril=part(head,52,32,-16,-13,0,16,16,0,-8,-12,0,false);
        leftTendril=part(head,58,0,0,-13,0,16,16,0,8,-12,0,false);
        rightArm=part(body,44,50,-4,0,-4,8,28,8,-13,-13,1,false);
        leftArm=part(body,0,58,-4,0,-4,8,28,8,13,-13,1,false);
        rightLeg=part(bone,76,48,-3.1F,0,-3,6,13,6,-5.9F,-13,0,false);
        leftLeg=part(bone,76,76,-2.9F,0,-3,6,13,6,5.9F,-13,0,false);
        String[] names={"bone","body","head","left_tendril","right_tendril","left_ribcage","right_ribcage","left_arm","right_arm","left_leg","right_leg"};
        ModelRenderer[] nodes={bone,body,head,leftTendril,rightTendril,leftRib,rightRib,leftArm,rightArm,leftLeg,rightLeg};
        for(int i=0;i<nodes.length;i++) {
            parts.put(names[i],nodes[i]);
            pivots.put(nodes[i],new float[]{nodes[i].rotationPointX,nodes[i].rotationPointY,nodes[i].rotationPointZ});
        }
    }

    /** Select mesh visibility without hiding child bones or losing their transforms. */
    void selectLayer(int layer) {
        for(java.util.Map.Entry<ModelRenderer,ModelRenderer> entry:meshes.entrySet()) {
            ModelRenderer part=entry.getKey();
            boolean limbs=part==head||part==leftArm||part==rightArm||part==leftLeg||part==rightLeg;
            entry.getValue().showModel=layer<0 || (layer==0&&limbs) ||
                    ((layer==1||layer==2)&&(limbs||part==body)) ||
                    (layer==3&&(part==leftTendril||part==rightTendril)) || (layer==4&&part==body);
        }
    }

    private void resetPose() {
        for(ModelRenderer part:parts.values()) {
            float[] pivot=pivots.get(part);
            part.setRotationPoint(pivot[0],pivot[1],pivot[2]);
            part.rotateAngleX=part.rotateAngleY=part.rotateAngleZ=0;
        }
    }

    private ModelRenderer part(ModelRenderer parent,int u,int v,float x,float y,float z,
            int width,int height,int depth,float px,float py,float pz,boolean mirror){
        ModelRenderer part=new ModelRenderer(this);
        ModelRenderer mesh=new ModelRenderer(this,u,v);mesh.mirror=mirror;
        mesh.addBox(x,y,z,width,height,depth);part.addChild(mesh);meshes.put(part,mesh);
        part.setRotationPoint(px,py,pz);parent.addChild(part);return part;
    }

    @Override public void setRotationAngles(float limb,float amount,float age,float yaw,float pitch,float scale,Entity raw){
        resetPose();
        EntityWarden120 warden=(EntityWarden120)raw;int state=warden.getWardenState();
        head.rotateAngleY=yaw*.017453292F;head.rotateAngleX=pitch*.017453292F;
        float strength=Math.min(.5F,3F*amount),phase=limb*.8662F;
        float cosine=MathHelper.cos(phase),sine=MathHelper.sin(phase),lean=Math.min(.35F,strength);
        head.rotateAngleZ=.3F*sine*strength+.06F*MathHelper.cos(age*.1F);
        head.rotateAngleX+=1.2F*MathHelper.cos(phase+(float)Math.PI/2)*lean+.06F*MathHelper.sin(age*.1F);
        body.rotateAngleZ=.1F*sine*strength+.025F*MathHelper.sin(age*.1F);
        body.rotateAngleX=cosine*lean+.025F*MathHelper.cos(age*.1F);body.rotateAngleY=0;
        leftLeg.rotateAngleX=cosine*strength;rightLeg.rotateAngleX=MathHelper.cos(phase+(float)Math.PI)*strength;
        leftArm.rotateAngleX=-.8F*cosine*strength;rightArm.rotateAngleX=-.8F*sine*strength;
        leftArm.rotateAngleY=rightArm.rotateAngleY=leftArm.rotateAngleZ=rightArm.rotateAngleZ=0;
        leftTendril.rotateAngleX=warden.getHeartIntensity()*MathHelper.cos(age*2.25F)*(float)Math.PI*.1F;
        rightTendril.rotateAngleX=-leftTendril.rotateAngleX;
        leftTendril.rotateAngleZ=rightTendril.rotateAngleZ=leftRib.rotateAngleY=rightRib.rotateAngleY=0;
        int duration=state==EntityWarden120.EMERGING?EntityWarden120.EMERGE_TICKS:
                state==EntityWarden120.DIGGING?EntityWarden120.DIG_TICKS:
                state==EntityWarden120.SONIC?EntityWarden120.SONIC_DURATION:
                state==EntityWarden120.MELEE?18:84;
        float elapsed=Math.max(0,duration-warden.getWardenStateTicks()+age-warden.ticksExisted);
        WardenAnimation120.apply(this,state,elapsed);
    }

    @Override public void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale){
        setRotationAngles(limb,amount,age,yaw,pitch,scale,entity);bone.render(scale);
    }
}
