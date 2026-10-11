import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.LodestoneMath116;
/** Runs the actual side-neutral production class with no Minecraft stand-ins. */
public final class B11605MathFixture {
    static int checks;
    public static void main(String[] args) {

        near(LodestoneMath116.bearing(0,0,0,1,0),0,"forward south");
        near(LodestoneMath116.bearing(0,0,-1,0,90),0,"forward west");
        near(LodestoneMath116.bearing(0,0,0,-1,180),0,"forward north");
        near(LodestoneMath116.bearing(0,0,1,0,270),0,"forward east");
        for(int frame=0;frame<32;frame++){
            eq(LodestoneMath116.textureFrame(frame/32D),(frame+16)&31,"frame center");
            eq(LodestoneMath116.textureFrame(frame/32D-1D),(frame+16)&31,"negative normalized frame");
        }
        eq(LodestoneMath116.textureFrame(.015625),17,"model threshold");eq(LodestoneMath116.textureFrame(.984375),16,"wrap override");
        LodestoneMath116.Wobble a=new LodestoneMath116.Wobble(),b=new LodestoneMath116.Wobble(),control=new LodestoneMath116.Wobble();
        for(int tick=0;tick<4000;tick++){
            double left=a.update(tick,.2D),right=b.update(tick,.8D);
            near(left,control.update(tick,.2D),"interleaving a second stack cannot change the first");
            near(a.update(tick,.8D),left,"at most one update per stack/tick");
            yes(left>=0&&left<1&&right>=0&&right<1,"normalized independent wobble");
        }
        near(a.update(0,.6),.6,"time rollback resets safely");
        for(int x=-30;x<=30;x++)for(int z=-30;z<=30;z++)if(x!=0||z!=0){
            double angle=LodestoneMath116.bearing(-1234,-4321,-1234+x,-4321+z,33);
            double shifted=LodestoneMath116.bearing(99,201,99+x,201+z,33+720);
            near(angle,shifted,"position translation and yaw periodicity");
        }

        System.out.println("PASS B116-05 math: "+checks+" checks");
    }
    static void yes(boolean value,String label){if(!value)throw new AssertionError(label);checks++;}
    static void eq(int actual,int expected,String label){yes(actual==expected,label+": "+actual+" != "+expected);}
    static void near(double actual,double expected,String label){yes(Math.abs(actual-expected)<1E-10,label+": "+actual+" != "+expected);}
}
