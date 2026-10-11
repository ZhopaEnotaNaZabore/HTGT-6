package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.minecraft1202.Module.Client.Shared.BackportEntityModel;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.BackportModelPart;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulGuardian;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/** Exact 1.12 Guardian mesh: five-piece body, twelve spikes, eye and three-part tail. */
public final class ModelBountifulGuardian extends BackportEntityModel<EntityBountifulGuardian> {
    private static final float[] RX={1.75F,.25F,0,0,.5F,.5F,.5F,.5F,1.25F,.75F,0,0};
    private static final float[] RY={0,0,0,0,.25F,1.75F,1.25F,.75F,0,0,0,0};
    private static final float[] RZ={0,0,.25F,1.75F,0,0,0,0,0,0,.75F,1.25F};
    private static final float[] PX={0,0,8,-8,-8,8,8,-8,0,0,8,-8};
    private static final float[] PY={-8,-8,-8,-8,0,0,0,0,8,8,8,8};
    private static final float[] PZ={8,-8,0,0,-8,-8,8,8,8,-8,0,0};
    private final BackportModelPart body,eye;
    private final BackportModelPart[] spikes=new BackportModelPart[12],tail=new BackportModelPart[3];

    public ModelBountifulGuardian(){super(64,64);
        body=part("body",0,0).box(-6,10,-8,12,12,16).textureOffset(0,28).box(-8,10,-6,2,12,12)
                .textureOffset(0,28).box(6,10,-6,2,12,12).textureOffset(16,40).box(-6,8,-6,12,2,12)
                .textureOffset(16,40).box(-6,22,-6,12,2,12).storePose();
        for(int i=0;i<12;i++){spikes[i]=part("spike_"+i,0,0).box(-1,-4.5F,-1,2,9,2).storePose();body.child(spikes[i]);}
        eye=part("eye",8,0).box(-1,15,0,2,2,1).storePose();body.child(eye);
        tail[0]=part("tail",40,0).box(-2,14,7,4,4,8).storePose();tail[1]=part("tail_1",0,54).box(0,14,0,3,3,7).storePose();
        tail[2]=part("tail_2",41,32).box(0,14,0,2,2,6).textureOffset(25,19).box(1,10.5F,3,1,9,9).storePose();
        body.child(tail[0]);tail[0].child(tail[1]);tail[1].child(tail[2]);
    }
    @Override protected void setupAnim(EntityBountifulGuardian e,float limb,float amount,float age,float yaw,float pitch){
        body.setYaw(yaw*((float)Math.PI/180F));body.setPitch(pitch*((float)Math.PI/180F));
        float retract=(1-e.getSpikesAnimation(age-e.ticksExisted))*.55F;
        for(int i=0;i<12;i++){spikes[i].setPitch((float)Math.PI*RX[i]);spikes[i].setYaw((float)Math.PI*RY[i]);spikes[i].setRoll((float)Math.PI*RZ[i]);float p=1+MathHelper.cos(age*1.5F+i)*.01F-retract;spikes[i].setX(PX[i]*p);spikes[i].setY(16+PY[i]*p);spikes[i].setZ(PZ[i]*p);}
        eye.setZ(-8.25F);eye.setY(0);eye.setX(0);Entity watched=e.getBeamTarget()!=null?e.getBeamTarget():Minecraft.getMinecraft().renderViewEntity;
        if(watched!=null){Vec3 t=Vec3.createVectorHelper(watched.posX,watched.posY+watched.getEyeHeight(),watched.posZ),s=Vec3.createVectorHelper(e.posX,e.posY+e.getEyeHeight(),e.posZ);eye.setY(t.yCoord>s.yCoord?0:1);Vec3 look=e.getLookVec();look.yCoord=0;look=look.normalize();Vec3 side=Vec3.createVectorHelper(s.xCoord-t.xCoord,0,s.zCoord-t.zCoord).normalize();side.rotateAroundY((float)Math.PI/2);double d=look.dotProduct(side);eye.setX(MathHelper.sqrt_float((float)Math.abs(d))*2*(float)Math.signum(d));}
        float wave=e.getTailAnimation(age-e.ticksExisted);tail[0].setYaw(MathHelper.sin(wave)*(float)Math.PI*.05F);tail[1].setYaw(MathHelper.sin(wave)*(float)Math.PI*.1F);tail[1].setX(-1.5F);tail[1].setY(.5F);tail[1].setZ(14);tail[2].setYaw(MathHelper.sin(wave)*(float)Math.PI*.15F);tail[2].setX(.5F);tail[2].setY(.5F);tail[2].setZ(6);
    }
    @Override protected void renderToBuffer(EntityBountifulGuardian e,float scale){body.render(scale);}
}
