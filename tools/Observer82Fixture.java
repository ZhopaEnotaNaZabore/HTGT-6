import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.BlockObserver112;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.ObserverUpdates120;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.util.ForgeDirection;
public final class Observer82Fixture {
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();BlockObserver112 block=new BlockObserver112();
  for(int facing=0;facing<6;facing++){
   w.setBlock(0,20,0,block,facing,0);
   for(ForgeDirection source:ForgeDirection.VALID_DIRECTIONS){
    w.scheduled=0;ObserverUpdates120.notifyChange(w,source.offsetX,20+source.offsetY,source.offsetZ,3);
    check(w.scheduled==(facing==source.ordinal()?1:0),"wrong input face "+facing+"/"+source);
   }
   w.scheduled=0;block.onNeighborBlockChange(w,0,20,0,Blocks.stone);check(w.scheduled==0,"coordinate-less feedback");
   ForgeDirection front=ForgeDirection.getOrientation(facing);
   ObserverUpdates120.afterChange(false,w,front.offsetX,20+front.offsetY,front.offsetZ,3);check(w.scheduled==0,"failed change pulse");
   ObserverUpdates120.notifyChange(w,front.offsetX,20+front.offsetY,front.offsetZ,16);check(w.scheduled==0,"suppressed shape update");
   ObserverUpdates120.notifyChange(w,front.offsetX,20+front.offsetY,front.offsetZ,2);check(w.scheduled==1&&w.lastDelay==2,"metadata-only update timing");
   block.updateTick(w,0,20,0,new java.util.Random(1));check((w.getBlockMetadata(0,20,0)&8)!=0&&w.lastDelay==2,"pulse on");
   for(int side=0;side<6;side++)check(block.isProvidingWeakPower(w,0,20,0,side)==(side==facing?15:0),"output query side");
   w.scheduled=0;ObserverUpdates120.notifyChange(w,front.offsetX,20+front.offsetY,front.offsetZ,3);check(w.scheduled==0,"retrigger while powered");
   block.updateTick(w,0,20,0,new java.util.Random(1));check(w.getBlockMetadata(0,20,0)==facing,"pulse off");
  }
  System.out.println("PASS: all 36 input directions, no generic feedback, successful updates only, flags and two-tick pulses");
 }
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
