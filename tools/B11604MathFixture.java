import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.TargetSignal116;
/** Runs the actual side-neutral production class with no Minecraft stand-ins. */
public final class B11604MathFixture {
    static int checks;
    public static void main(String[] args) {

        for(int side=0;side<6;side++)for(int origin:new int[]{-128,-2,0,12,1000000}){
            for(int u=0;u<=32;u++)for(int v=0;v<=32;v++){
                double a=origin+u/32D,b=origin+v/32D;
                double x=side<2?a:side<4?a:origin,y=side<2?origin:side<4?b:a,z=side<2?b:side<4?origin:b;
                int expected=Math.max(1,(int)Math.ceil(15D*(1D-2D*Math.max(Math.abs(u/32D-.5D),Math.abs(v/32D-.5D)))));
                eq(TargetSignal116.strength(side,x,y,z),expected,"all faces/edges/corners/negative positions");
            }
        }
        eq(TargetSignal116.strength(0,.5,1,.5),15,"center");
        eq(TargetSignal116.strength(0,.25,1,.5),8,"quarter");
        eq(TargetSignal116.strength(0,0,1,0),1,"corner");
        eq(TargetSignal116.strength(-1,0,0,0),0,"bad face");eq(TargetSignal116.strength(6,0,0,0),0,"bad face");
        eq(TargetSignal116.strength(0,Double.NaN,0,0),0,"NaN");eq(TargetSignal116.strength(0,0,Double.POSITIVE_INFINITY,0),0,"infinity");
        eq(TargetSignal116.pulseTicks(true),20,"arrow and trident ticks");eq(TargetSignal116.pulseTicks(false),8,"other projectile ticks");

        System.out.println("PASS B116-04 math: "+checks+" checks");
    }
    static void yes(boolean value,String label){if(!value)throw new AssertionError(label);checks++;}
    static void eq(int actual,int expected,String label){yes(actual==expected,label+": "+actual+" != "+expected);}
    static void near(double actual,double expected,String label){yes(Math.abs(actual-expected)<1E-10,label+": "+actual+" != "+expected);}
}
