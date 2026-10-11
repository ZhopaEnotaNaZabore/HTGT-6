package com.mod.htgt6.client.nei.structure;

/** Pure preview math, shared by controls, picking and regression tests. */
public final class StructurePreviewMath {
    private StructurePreviewMath() { }
    public static int nextLayer(int layer,int height,int direction) {
        if(height<=0) return -1;
        int index=(layer+1+direction)%(height+1);
        if(index<0) index+=height+1;
        return index-1;
    }
    public static boolean visible(int selected,int y) { return selected<0 || selected==y; }
    public static double hit(double[] start,double[] end,int x,int y,int z) {
        double near=0,far=1;
        for(int axis=0;axis<3;axis++) {
            double center=axis==0?x:axis==1?y:z, delta=end[axis]-start[axis];
            if(Math.abs(delta)<1e-9) {
                if(start[axis]<center-.5 || start[axis]>center+.5) return Double.POSITIVE_INFINITY;
            } else {
                double a=(center-.5-start[axis])/delta,b=(center+.5-start[axis])/delta;
                near=Math.max(near,Math.min(a,b)); far=Math.min(far,Math.max(a,b));
                if(near>far) return Double.POSITIVE_INFINITY;
            }
        }
        return near;
    }
    public static float fit(int width,int height,int depth,int viewportWidth,int viewportHeight,float pitch) {
        double diagonal=Math.sqrt(width*width+depth*depth), angle=Math.toRadians(pitch);
        double vertical=height*Math.abs(Math.cos(angle))+diagonal*Math.abs(Math.sin(angle));
        return (float)Math.min((viewportWidth-10)/diagonal,(viewportHeight-10)/Math.max(1,vertical));
    }
}
