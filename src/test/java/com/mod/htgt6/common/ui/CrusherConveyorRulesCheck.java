package com.mod.htgt6.common.ui;

import com.mod.htgt6.common.crusher.CrusherConveyorRules;

public final class CrusherConveyorRulesCheck {
    public static void main(String[] args){
        for(int facing=2;facing<=5;facing++)for(double u:new double[]{-1.4,0,1.4})for(double v:new double[]{.55,3,6.30}){
            double[] world=CrusherConveyorRules.worldOffset(facing,u,v);
            double[] local=CrusherConveyorRules.localOffset(facing,world[0],world[1]);
            check(Math.abs(local[0]-u)<1e-9&&Math.abs(local[1]-v)<1e-9,"rotation round trip");
        }
        check(!CrusherConveyorRules.hasCapacity(16,0,false),"entity spam limit");
        check(!CrusherConveyorRules.hasCapacity(3,3,true),"mob spam limit");
        check(CrusherConveyorRules.hasCapacity(3,3,false),"items still accepted with three mobs");
        check(CrusherConveyorRules.advance(2,6.3,false)==2,"unpowered pause");
        double[] mobs={7.1,7.0,6.9};double limit=CrusherConveyorRules.MOB_END;
        for(int i=0;i<mobs.length;i++){
            mobs[i]=CrusherConveyorRules.advance(mobs[i],limit,true);
            limit=Math.max(.55,mobs[i]-CrusherConveyorRules.MOB_GAP);
        }
        check(mobs[0]>=7.09&&mobs[1]<7.09&&mobs[2]<7.09,"single mob inside first roller");
        int small=hits(20),large=hits(200),huge=hits(2000);
        check(small==3&&large==25&&huge==250,"health proportional processing");
        check(huge*CrusherConveyorRules.DAMAGE_INTERVAL>1200,"high HP is not a timeout criterion");
        int transit=0;
        for(int t=0;t<CrusherConveyorRules.OUTPUT_TICKS;t++){
            check(!CrusherConveyorRules.canUnload(transit,true),"no direct hopper delivery before end of belt");
            transit=CrusherConveyorRules.advanceOutput(transit,true);
        }
        check(CrusherConveyorRules.canUnload(transit,true),"delivery at belt end");
        check(!CrusherConveyorRules.canUnload(transit,false),"no unpowered delivery");
        check(CrusherConveyorRules.advanceOutput(47,false)==47,"pause retains output position");
        check(CrusherConveyorRules.advanceOutput(transit,true)==transit,"blocked output remains at belt end");
        check(Math.abs(CrusherConveyorRules.outputY(20)-1.43)<1e-6,"chute reaches lower belt");
        check(Math.abs(CrusherConveyorRules.outputZ(120)-14.65)<1e-6,"output stops before rear hatch");
        System.out.println("Crusher conveyor: four orientations, bounded intake, paused motion, mob queue and HP policy passed");
    }
    private static int hits(float hp){return (int)Math.ceil(hp/CrusherConveyorRules.DAMAGE);}
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
