package com.mod.htgt6.client.render;

/** Feed 0..45%, assembly 45..70%, finished product 70..100%. */
public final class ElectronicAssemblyCycle {
    private ElectronicAssemblyCycle() { }
    public static float clamp(float p) { return Math.max(0,Math.min(1,p)); }
    public static boolean assembling(float p) { return p >= .45F && p < .70F; }
    public static int component(float p,int count) {
        return Math.min(Math.max(0,count-1),(int)(clamp(p/.45F)*Math.max(1,count)));
    }
    public static double inputZ(float p,int count) {
        float sequence=clamp(p/.45F)*Math.max(1,count);
        return .90 + (p >= .45F ? 1 : sequence-(int)sequence)*3.05;
    }
    public static double outputZ(float p) { return 5.08+clamp((p-.70F)/.30F)*3.70; }
    public static float stroke(float p) { return (float)Math.sin(clamp((p-.45F)/.25F)*Math.PI); }
}
