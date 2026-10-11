package com.mod.htgt6.common.ui;

import com.mod.htgt6.common.crusher.IndustrialCrusherCollision;
import com.mod.htgt6.common.crusher.IndustrialCrusherGeometry;
import java.util.List;

/** Standalone geometry regression: no Minecraft client required. */
public final class IndustrialCrusherCollisionCheck {
    public static void main(String[] args) {
        int cells = 0;
        double referenceVolume = 0;
        for (int facing = 2; facing <= 5; facing++) {
            double volume = 0;
            int count = 0;
            for (int u=-3; u<=3; u++) for (int y=0; y<6; y++) for (int v=0; v<16; v++) {
                List<double[]> boxes = IndustrialCrusherCollision.cell(u,y,v,facing);
                if (!boxes.isEmpty()) {
                    count++;
                    require(IndustrialCrusherGeometry.element(u,y,v)==IndustrialCrusherGeometry.AIR, "structural block occupied");
                }
                for (double[] b : boxes) {
                    for (int axis=0; axis<3; axis++) {
                        require(b[axis]>=-1e-9 && b[axis+3]<=1+1e-9, "box outside cell");
                        require(b[axis]<b[axis+3], "inverted box");
                    }
                    volume += (b[3]-b[0])*(b[4]-b[1])*(b[5]-b[2]);
                }
            }
            if (facing==2) { referenceVolume=volume; cells=count; }
            else require(count==cells && Math.abs(volume-referenceVolume)<1e-8, "rotation changed shape");
        }
        require(solid(0,3.04,2), "input belt missing");
        require(solid(0,1.20,12), "output belt missing");
        require(solid(0,3.65,7.1) && solid(0,3.65,8.7), "rollers missing");
        require(solid(0,3.5,12), "drive missing");
        require(!solid(0,1.5,3), "empty space under input belt blocked");
        require(!solid(2.4,2,12), "side gap blocked");
        require(!solid(0,4,3), "open feed area blocked");
        System.out.println("Crusher collision checks passed: " + cells + " non-ticking cells; four facings.");
    }
    private static boolean solid(double x,double y,double z) {
        int u=(int)Math.floor(x+.5), level=(int)Math.floor(y), v=(int)Math.floor(z+.5);
        double a=x-u+.5,b=y-level,c=z-v+.5;
        for (double[] box:IndustrialCrusherCollision.cell(u,level,v,2)) {
            if(a>=box[0] && a<=box[3] && b>=box[1] && b<=box[4] && c>=box[2] && c<=box[5]) return true;
        }
        return false;
    }
    private static void require(boolean value,String message) {
        if(!value) throw new AssertionError(message);
    }
}
