package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import java.lang.reflect.Method;
import net.minecraft.client.model.ModelRenderer;

/** CPU-only validation: never creates an OpenGL context or closes a game. */
public final class WardenModelFixture120 {
    private static int checks;
    private static void check(boolean value,String message) {
        checks++; if(!value) throw new AssertionError(message);
    }
    private static void near(float actual,float expected,String message) {
        check(Math.abs(actual-expected)<.0002F,message+": "+actual+" != "+expected);
    }
    public static void main(String[] args) throws Exception {
        ModelWarden120 model=new ModelWarden120();
        check(model.parts.size()==11,"Missing model bones");
        near(model.head.rotationPointY,-13,"Head pivot");
        near(model.rightTendril.rotationPointX,-8,"Tendril pivot");
        WardenAnimation120.Channel[][] animations={WardenKeyframes120.EMERGE,WardenKeyframes120.DIG,
                WardenKeyframes120.ROAR,WardenKeyframes120.SNIFF,WardenKeyframes120.ATTACK,WardenKeyframes120.SONIC};
        int frames=0;
        for(WardenAnimation120.Channel[] animation:animations) for(WardenAnimation120.Channel channel:animation) {
            frames+=channel.keys.length/5;
            check(model.parts.containsKey(channel.bone)||channel.bone.endsWith("_ear"),"Unknown bone "+channel.bone);
            for(int i=0;i<channel.keys.length;i+=5) {
                if(i>0) check(channel.keys[i]>channel.keys[i-5],"Unordered keyframes");
                for(int axis=1;axis<=3;axis++) {
                    near(channel.sample(channel.keys[i],axis),channel.keys[i+axis],"Keyframe interpolation");
                }
            }
            for(float t=0;t<7;t+=.025F) for(int axis=1;axis<=3;axis++)
                check(Float.isFinite(channel.sample(t,axis)),"Nonfinite interpolated pose");
        }
        check(frames==534,"Incomplete animation import");
        WardenAnimation120.Channel linear=new WardenAnimation120.Channel("head",true,new float[]{0,0,0,0,0,1,10,20,30,0});
        near(linear.sample(.5F,1),5,"Linear midpoint");
        near(linear.sample(-1,1),0,"Before first key");
        near(linear.sample(2,1),10,"After last key");
        Method reset=ModelWarden120.class.getDeclaredMethod("resetPose");reset.setAccessible(true);
        WardenAnimation120.apply(model,1,0);
        near(model.body.rotationPointY,42,"Emerge starts below ground; Y conversion");
        reset.invoke(model);
        WardenAnimation120.apply(model,1,135);
        near(model.body.rotationPointY,-21,"Emerge ends at rest");
        for(int state=1;state<=6;state++) {
            reset.invoke(model); WardenAnimation120.apply(model,state,10);
            reset.invoke(model);
            for(ModelRenderer part:model.parts.values()) {
                near(part.rotateAngleX,0,"Reset X");near(part.rotateAngleY,0,"Reset Y");near(part.rotateAngleZ,0,"Reset Z");
            }
            near(model.body.rotationPointY,-21,"Reset translation");
        }
        int[] counts={5,6,6,2,1,10};
        for(int layer=0;layer<6;layer++) {
            model.selectLayer(layer==5?-1:layer);
            int visible=0;
            for(ModelRenderer part:model.parts.values()) if(part!=model.bone) {
                ModelRenderer mesh=(ModelRenderer)part.childModels.get(0);
                if(mesh.showModel)visible++;
                check(part.showModel,"Layer hid hierarchy");
            }
            check(visible==counts[layer],"Incorrect emissive mask "+layer);
        }
        System.out.println("Warden model PASS: "+checks+" checks, six animations, 534 keyframes, five layer masks");
    }
}
