package com.mod.htgt6.common.ui;

import com.mod.htgt6.common.crusher.IndustrialCrusherGeometry;
import com.mod.htgt6.client.render.IndustrialCrusherCycle;

/** Standalone checks for the canonical structure and recipe-driven conveyor path. */
public final class IndustrialCrusherGeometryCheck {
    public static void main(String[] args) {
        int[] totals = new int[6];
        for (int y = 0; y < 6; y++) {
            int[] layer = new int[6];
            for (int u = -3; u <= 3; u++) for (int v = 0; v < 16; v++) {
                int element = IndustrialCrusherGeometry.element(u, y, v);
                require(element >= 0 && element < 6, "Unknown element");
                totals[element]++; layer[element]++;
                if (v < 6 && Math.abs(u) <= 2 && y >= 3) require(element == 0, "Feed roof must stay open");
                if (y == 0) require(element != 0, "Hole in foundation");
                if (y == 5 && v >= 6) require(element != 0, "Hole in rear roof");
            }
            System.out.println("Layer " + (y + 1) + ": " + java.util.Arrays.toString(layer));
        }
        require(totals[1] == 1 && totals[4] == 1, "Controller/output must be unique");
        require(IndustrialCrusherGeometry.element(0, 0, 0) == 1, "Front controller");
        require(IndustrialCrusherGeometry.element(0, 1, 15) == 4, "Rear product output");
        for (int u : new int[] {-2, 0, 1, 2}) require(IndustrialCrusherGeometry.element(u, 0, 1) == 2, "NEI gateway on a non-flexible block");
        for (int count = 1; count <= 12; count++) for (int step = 0; step < 450; step++) {
            float p = step / 1000F;
            int component = IndustrialCrusherCycle.component(p, count);
            double z = IndustrialCrusherCycle.inputZ(p, count);
            require(component >= 0 && component < count && z >= .7 && z <= 6.2, "Input path out of bounds");
        }
        for (int step = 650; step <= 1000; step++) {
            double z = IndustrialCrusherCycle.outputZ(step / 1000F);
            require(z >= 8.7 && z <= 14.700001, "Output path out of bounds");
        }
        require(IndustrialCrusherGeometry.element(0, 6, 0) == 0, "Height exceeds envelope");
        require(IndustrialCrusherGeometry.element(0, 0, 16) == 0, "Length exceeds chunk");
        System.out.println("Totals [air,controller,base,casing,output,rib]: " + java.util.Arrays.toString(totals));
        System.out.println("IndustrialCrusherGeometryCheck: OK");
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
