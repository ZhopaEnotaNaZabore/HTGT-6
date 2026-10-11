import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.CrossbowItemRenderer139;
public final class Crossbow168Fixture {
 public static void main(String[] args){
  int count=0;
  for(int duration:new int[]{1,10,15,20,25})for(int tick=0;tick<=duration+1;tick++)for(float partial:new float[]{0,.25F,.5F,.75F,1}){
   float age=CrossbowItemRenderer139.chargeAge(tick,partial);
   float progress=CrossbowItemRenderer139.chargeProgress(age,duration);
   float expected=Math.max(0,Math.min(1,(tick+partial-1)/duration));
   if(Math.abs(progress-expected)>1E-6)throw new AssertionError("charge interpolation");
   float wobble=CrossbowItemRenderer139.chargeWobble(age,progress);
   float reference=progress<=.1F?0:(float)Math.sin((age-.1F)*1.3F)*(progress-.1F)*.004F;
   if(Math.abs(wobble-reference)>1E-6)throw new AssertionError("charge wobble");
   count++;
  }
  if(CrossbowItemRenderer139.chargeProgress(0,0)!=0)throw new AssertionError("zero duration");
  System.out.println("PASS crossbow168: "+count+" charge frames + zero duration");
 }
}
