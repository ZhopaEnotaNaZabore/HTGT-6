package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import net.minecraft.client.model.ModelRenderer;

/** Additive keyframe adapter for the pre-1.8 model API. */
final class WardenAnimation120 {
    static final class Channel {
        final String bone;
        final boolean rotation;
        final float[] keys; // time, x, y, z, interpolation (0 linear, 1 Catmull-Rom)
        Channel(String bone, boolean rotation, float[] keys) {
            this.bone=bone; this.rotation=rotation; this.keys=keys;
        }
        float sample(float seconds, int axis) {
            int count=keys.length/5, left=0;
            while(left<count-1 && seconds>keys[(left+1)*5]) left++;
            int right=Math.min(count-1,left+1);
            float span=keys[right*5]-keys[left*5];
            float t=span==0?0:Math.max(0,Math.min(1,(seconds-keys[left*5])/span));
            float b=keys[left*5+axis], c=keys[right*5+axis];
            if(keys[right*5+4]==0) return b+(c-b)*t;
            float a=keys[Math.max(0,left-1)*5+axis], d=keys[Math.min(count-1,right+1)*5+axis];
            return .5F*(2*b+(c-a)*t+(2*a-5*b+4*c-d)*t*t+(3*b-a-3*c+d)*t*t*t);
        }
    }
    static void apply(ModelWarden120 model, int state, float ticks) {
        Channel[] channels;
        switch(state) {
            case 1: channels=WardenKeyframes120.EMERGE; break;
            case 2: channels=WardenKeyframes120.ROAR; break;
            case 3: channels=WardenKeyframes120.SNIFF; break;
            case 4: channels=WardenKeyframes120.ATTACK; break;
            case 5: channels=WardenKeyframes120.SONIC; break;
            case 6: channels=WardenKeyframes120.DIG; break;
            default: return;
        }
        for(Channel channel:channels) {
            ModelRenderer part=model.parts.get(channel.bone);
            // The original data also contains unused ear channels (no matching bones in the original model).
            if(part==null) continue;
            float x=channel.sample(ticks/20F,1), y=channel.sample(ticks/20F,2), z=channel.sample(ticks/20F,3);
            if(channel.rotation) {
                float radians=(float)Math.PI/180F;
                part.rotateAngleX+=x*radians; part.rotateAngleY+=y*radians; part.rotateAngleZ+=z*radians;
            } else {
                part.rotationPointX+=x; part.rotationPointY-=y; part.rotationPointZ+=z;
            }
        }
    }
}
