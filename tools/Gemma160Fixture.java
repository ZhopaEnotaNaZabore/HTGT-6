import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.RaidHudState160;
import com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage.RaidNetwork150.Snapshot;

/** Independent acceptance tests, written without reading the local model's output. */
public final class Gemma160Fixture {
    private static int checks;
    public static void main(String[] args) {
        RaidHudState160 state = new RaidHudState160();
        Object world = new Object();
        check(state.snapshot() == null, "initially empty");
        Snapshot packet = new Snapshot(0,true,0,4,.5F);
        state.accept(packet,world,0);
        check(state.snapshot()!=packet, "incoming defensive copy");
        packet.remaining=999; packet.progress=0;
        check(state.snapshot().remaining==4&&state.snapshot().progress==.5F,"incoming mutations isolated");
        Snapshot returned=state.snapshot(); returned.progress=0;returned.visible=false;
        check(state.snapshot().progress==.5F&&state.snapshot().visible,"outgoing mutations isolated");
        for(int i=0;i<60;i++)state.tick(world);
        check(state.snapshot()!=null,"retained through tick60");
        state.accept(new Snapshot(1,true,2,0,1),world,0);
        check(state.snapshot()!=null&&state.snapshot().status==0,"wrong dimension preserves current");
        state.tick(world);check(state.snapshot()==null,"wrong dimension did not refresh timer");
        for(int i=0;i<100;i++)state.tick(world);
        state.accept(new Snapshot(0,true,0,1,1),world,0);
        for(int i=0;i<50;i++)state.tick(world);
        state.accept(new Snapshot(0,true,1,0,1),world,0);
        for(int i=0;i<60;i++)state.tick(world);
        check(state.snapshot()!=null&&state.snapshot().status==1,"accepted update refreshes expiry");
        state.tick(world);check(state.snapshot()==null,"refreshed state expires");
        for(float value:new float[]{Float.NaN,Float.NEGATIVE_INFINITY,-.5F,0,.33F,1,2,Float.POSITIVE_INFINITY}){
            state.accept(new Snapshot(0,true,-1,-4,value),world,0);
            Snapshot s=state.snapshot();float expected=Float.isNaN(value)?0:Math.max(0,Math.min(1,value));
            check(s.progress==expected&&s.status==0&&s.remaining==0&&s.dimension==0,"normalization "+value);
        }
        state.accept(new Snapshot(0,true,99,9999,.5F),world,0);
        check(state.snapshot().status==2&&state.snapshot().remaining==1024,"upper limits");
        state.accept(new Snapshot(0,false,0,0,0),world,0);check(state.snapshot()==null,"hide packet");
        state.accept(new Snapshot(0,true,0,1,1),world,0);state.accept(null,world,0);check(state.snapshot()==null,"null packet");
        state.accept(new Snapshot(0,true,0,1,1),world,0);state.accept(new Snapshot(0,true,0,1,1),null,0);check(state.snapshot()==null,"null world accept");
        state.accept(new Snapshot(0,true,0,1,1),world,0);state.tick(null);check(state.snapshot()==null,"null world tick");
        Object equalA=new String("world"),equalB=new String("world");
        state.accept(new Snapshot(0,true,0,1,1),equalA,0);state.tick(equalB);check(state.snapshot()==null,"identity rather than equality");
        state.accept(new Snapshot(0,true,0,1,1),world,0);state.accept(new Snapshot(2,true,0,1,1),equalA,0);check(state.snapshot()==null,"world change invalidates before rejecting packet");
        state.accept(new Snapshot(-1,true,0,2,.7F),equalA,-1);check(state.snapshot()!=null&&state.snapshot().dimension==-1,"new dimension valid");
        System.out.println("PASS gemma160: "+checks+" independent state tests");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
}
