package com.mod.htgt6.client.nei.structure;

/** Run with plain javac/java; no Minecraft or OpenGL context needed. */
public final class StructurePreviewMathCheck {
    private static void check(boolean value,String text) { if(!value) throw new AssertionError(text); }
    public static void main(String[] args) {
        for(int height:new int[]{1,5,7,10,12,13}) {
            int layer=-1;
            for(int y=0;y<height;y++) {
                layer=StructurePreviewMath.nextLayer(layer,height,1);
                check(layer==y,"ALL -> each layer in order");
                for(int other=0;other<height;other++) check(StructurePreviewMath.visible(layer,other)==(other==y),"Only selected layer visible");
            }
            layer=StructurePreviewMath.nextLayer(layer,height,1);
            check(layer==-1,"Last layer -> ALL");
            for(int y=height-1;y>=0;y--) { layer=StructurePreviewMath.nextLayer(layer,height,-1); check(layer==y,"Reverse order"); }
            check(StructurePreviewMath.nextLayer(layer,height,-1)==-1,"Reverse wrap");
        }
        double[] start={0,0,-10},end={0,0,10};
        double front=StructurePreviewMath.hit(start,end,0,0,-2),back=StructurePreviewMath.hit(start,end,0,0,2);
        check(front<back && front>=0,"Nearest block wins");
        check(Double.isInfinite(StructurePreviewMath.hit(start,end,2,0,0)),"Parallel outside slab misses");
        check(StructurePreviewMath.hit(end,start,0,0,2)<StructurePreviewMath.hit(end,start,0,0,-2),"Reverse camera");
        check(StructurePreviewMath.hit(new double[]{0,0,0},end,0,0,0)==0,"Camera inside block");
        for(int[] size:new int[][]{{5,12,5},{9,13,9},{5,7,10},{5,10,5},{7,5,7},{5,5,5},{5,7,16}}) {
            for(int pitch=-75;pitch<=75;pitch+=15) for(int viewportHeight:new int[]{50,130,260}) {
                float scale=StructurePreviewMath.fit(size[0],size[1],size[2],160,viewportHeight,pitch);
                double diagonal=Math.hypot(size[0],size[2]),angle=Math.toRadians(pitch);
                check(scale>0 && scale*diagonal<=150.01,"Horizontal model fit");
                check(scale*(size[1]*Math.abs(Math.cos(angle))+diagonal*Math.abs(Math.sin(angle)))<=viewportHeight-9.99,"Vertical model fit");
            }
        }
        System.out.println("PASS: all layer cycles, isolated layers, nearest-block picking, 7 structure sizes");
    }
}
