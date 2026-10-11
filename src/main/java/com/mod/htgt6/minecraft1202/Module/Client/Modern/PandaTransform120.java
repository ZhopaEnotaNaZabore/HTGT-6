package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPanda120;
import net.minecraft.util.MathHelper;

/** Ordered PandaRenderer transforms, independent of GL for behavioral verification. */
public final class PandaTransform120 {
    private PandaTransform120() { }
    public interface Sink {
        void translate(float x,float y,float z);
        void rotate(float degrees,float x,float y,float z);
    }
    public static void body(EntityPanda120 panda,float partial,Sink sink) {
        int tick=panda.getRollCounter();
        float size=panda.isChild()?.3F:.8F;
        if(tick>0&&tick<32) {
            int phase=tick/8,next=tick+1;
            float start=phase*90F+90F*(tick-phase*8)/7F;
            float end=phase*90F+90F*(next-phase*8)/7F;
            float angle=next<(phase+1)*8?start+(end-start)*partial:start;
            float height=phase==0?(size+.2F)*angle/90F:
                    phase==1?size+.2F+(size-.2F)*(angle-90F)/90F:
                    phase==2?size+size*(270F-angle)/90F:size*(360F-angle)/90F;
            sink.translate(0,height,0);sink.rotate(-angle,1,0,0);
        }
        float sit=panda.getSitAmount(partial);
        if(sit>0) {
            sink.translate(0,.8F*sit,0);
            sink.rotate(panda.rotationPitch+90F*sit,1,0,0);
            sink.translate(0,-sit,0);
            if(panda.isScared()) {
                sink.rotate((float)(Math.cos(panda.ticksExisted*1.25D)*Math.PI*.05000000074505806D),0,1,0);
                if(panda.isChild())sink.translate(0,.8F,.55F);
            }
        }
        float back=panda.getOnBackAmount(partial);
        if(back>0) {
            sink.translate(0,(panda.isChild()?.5F:1.3F)*back,0);
            sink.rotate(panda.rotationPitch+180F*back,1,0,0);
        }
    }
    public static boolean showsFood(EntityPanda120 panda) {
        return panda.getEquipmentInSlot(0)!=null&&panda.isSitting()&&!panda.isScared();
    }
    public static void food(EntityPanda120 panda,float age,Sink sink) {
        float y=1.4F,z=-.6F;
        if(panda.getEatCounter()>0) {
            float wave=MathHelper.sin(age*.6F);
            z-=.2F*wave+.2F;y-=.09F*wave;
        }
        sink.translate(.1F,y,z);
    }
}
