package com.mod.htgt6.common.ui;

import com.mod.htgt6.common.multiblock.glass.ObservationGlassPattern;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.HashSet;

/** CTM topology and a deterministic preview of actual 64px atlas pixels, without Minecraft. */
public final class ObservationGlassPatternCheck {
    public static void main(String[] args) throws Exception {
        HashSet<Integer> patterns = new HashSet<Integer>();
        for (int i=0;i<256;i++) patterns.add(ObservationGlassPattern.canonical(i));
        check(patterns.size()==47,"47 distinct edge/corner states");
        check(ObservationGlassPattern.canonical(255)==15,"isolated block");
        check(ObservationGlassPattern.canonical(0)==0,"interior pane");
        check(ObservationGlassPattern.canonical(16)==16,"concave corner retained");
        for(int side=0;side<6;side++) {
            int dot=0, uLength=0, vLength=0;
            for(int axis=0;axis<3;axis++) {
                int u=ObservationGlassPattern.offset(side,1,0,axis),v=ObservationGlassPattern.offset(side,0,1,axis);
                dot+=u*v;uLength+=u*u;vLength+=v*v;
            }
            check(dot==0 && uLength==1 && vLength==1,"face UV basis");
        }
        // 5x3 rectangular window: every interior seam must be translucent, perimeter opaque.
        BufferedImage image=new BufferedImage(320,192,BufferedImage.TYPE_INT_ARGB);
        for(int by=0;by<3;by++) for(int bx=0;bx<5;bx++) {
            int mask=(bx==0?1:0)|(bx==4?2:0)|(by==0?4:0)|(by==2?8:0);
            for(int y=0;y<64;y++) for(int x=0;x<64;x++) {
                int pixel=ObservationGlassPattern.pixel(mask,true,x,y);
                int px=bx*64+x,py=by*64+y;
                boolean perimeter=px<4||px>=316||py<4||py>=188;
                check((pixel>>>24)==(perimeter?255:36),"no interior grid");
                image.setRGB(px,py,pixel);
            }
        }
        check((ObservationGlassPattern.pixel(16,true,0,0)>>>24)==255,"concave rim");
        check((ObservationGlassPattern.pixel(16,true,4,4)>>>24)==36,"concave interior");
        ImageIO.write(image,"png",new File("tmp/observation-glass-ctm-check.png"));
        System.out.println("Observation glass: 47 masks, six face orientations, 5x3 seamless window passed");
    }
    private static void check(boolean value,String message) {if(!value)throw new AssertionError(message);}
}
