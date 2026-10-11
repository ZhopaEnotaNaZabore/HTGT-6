package com.mod.htgt6.common.electronics;

import com.mod.htgt6.client.render.ElectronicAssemblyCycle;

/** Standalone checks, no Minecraft runtime required. */
public final class ElectronicFactoryGeometryCheck {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        int[] counts = new int[7];
        for (int u=-2;u<=2;u++) for(int y=-3;y<=3;y++) for(int v=0;v<=9;v++) {
            int current=ElectronicFactoryGeometry.element(u,y,v);
            int old=ElectronicFactoryGeometry.legacyElement(u,y,v);
            counts[current]++;
            check(current==old || current==ElectronicFactoryGeometry.RIB,"Only shell cells may migrate");
            check((current==0)==(old==0),"Interior clearance must remain unchanged");
            if(current==ElectronicFactoryGeometry.FLOOR) check(y==-3 && Math.abs(u)<2 && v>0 && v<9,"Port position");
        }
        check(counts[0]==120 && counts[1]==1 && counts[5]==1,"Core and terminals");
        check(counts[3]==24 && counts[4]==60,"Deck and windows");
        for(int count=1;count<=12;count++) {
            for(int i=0;i<count;i++) {
                float p=(i+.5F)*.45F/count;
                check(ElectronicAssemblyCycle.component(p,count)==i,"Ingredient sequence");
                double z=ElectronicAssemblyCycle.inputZ(p,count);
                check(z>=.9 && z<=3.95,"Ingredient stays before assembly chamber");
            }
        }
        check(!ElectronicAssemblyCycle.assembling(.44F) && ElectronicAssemblyCycle.assembling(.45F)
                && !ElectronicAssemblyCycle.assembling(.70F),"Phase boundaries");
        double last=0;
        for(int step=70;step<=100;step++) {
            double z=ElectronicAssemblyCycle.outputZ(step/100F);
            check(z>=last && z>=5.08 && z<=8.781,"Product travels toward rear output"); last=z;
        }
        check(Math.abs(ElectronicAssemblyCycle.stroke(.575F)-1)<.001,"Assembly tool stroke");
        check(ElectronicAssemblyCycle.component(0,0)==0,"Fluid-only recipe safety");
        System.out.println("PASS: layout, legacy clearance, 12 ingredients, assembly and output phases");
        System.out.println("Cells by type: " + java.util.Arrays.toString(counts));
    }
}
