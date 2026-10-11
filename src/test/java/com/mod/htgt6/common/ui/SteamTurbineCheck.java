package com.mod.htgt6.common.ui;

import com.mod.htgt6.common.turbine.SteamTurbineBalance;
import com.mod.htgt6.common.turbine.SteamTurbineGeometry;

public final class SteamTurbineCheck {
    public static void main(String[] args){
        int controller=0,glass=0,air=0;
        for(int x=-3;x<=3;x++)for(int y=-3;y<=3;y++)for(int z=0;z<13;z++){
            int e=SteamTurbineGeometry.element(x,y,z);
            if(e==1)controller++;if(e==5)glass++;if(e==0)air++;
        }
        check(controller==1&&glass==54&&air>0,"geometry");
        check(SteamTurbineGeometry.element(0,0,0)==1,"front controller");
        check(SteamTurbineGeometry.element(1,1,0)==0,"open front socket");
        int opening=0;
        for(int x=-3;x<=3;x++)for(int y=-3;y<=3;y++){
            int cell=SteamTurbineGeometry.element(x,y,0);
            if(Math.abs(x)<=2&&Math.abs(y)<=2){
                check(cell==(x==0&&y==0?1:0),"5x5 socket must be empty except hub");
                if(cell==0)opening++;
            }else check(cell!=0,"outer front frame remains solid");
        }
        check(opening==24,"24 air cells around the controller");
        check(SteamTurbineGeometry.element(0,-3,12)==2,"energy output location");
        for(long efficiency:new long[]{250000,500000,525000,1000000})for(long voltage:new long[]{32,128,512,34359738368L}){
            int tank=1003,water=0,remainder=0;long reserve=0,total=0;
            for(int tick=0;tick<10000&&(tank>0||reserve>=1000000);tick++){
                SteamTurbineBalance.Step step=SteamTurbineBalance.plan(reserve,tank,200,efficiency,tick%7==0?0:voltage);
                check(step.steam<=200&&step.generated<=voltage,"power and rotor flow limit");
                tank-=step.steam;total+=step.generated;reserve=step.reserve;
                int used=remainder+step.steam;water+=used/200;remainder=used%200;
                check(total*1000000+reserve==(1003L-tank)*efficiency,"exact rotor energy balance");
                check(water*200+remainder==1003-tank,"condensate balance");
            }
            check(tank==0&&reserve<1000000&&water==5&&remainder==3,"tail/reload state");
        }
        System.out.println("Steam turbine geometry, rotor flow, fuel and condensate balance passed");
    }
    private static void check(boolean ok,String label){if(!ok)throw new AssertionError(label);}
}
