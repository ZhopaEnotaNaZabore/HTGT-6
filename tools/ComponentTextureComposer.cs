using System;
using System.Drawing;

// Material patterns are masked onto metal; component geometry never comes from the material.
public static class ComponentTextureComposer {
    // Find a stable interior patch across the animation, excluding the ingot silhouette/bevel.
    public static Rectangle Interior(Bitmap[] frames) {
        int w=frames[0].Width, h=frames[0].Height;
        bool[,] valid=new bool[w,h];
        for(int y=1;y<h-1;y++) for(int x=1;x<w-1;x++) {
            bool ok=true;
            foreach(Bitmap f in frames) {
                if(f.Width!=w || f.Height!=h) throw new ArgumentException("Frame dimensions differ");
                for(int dy=-1;dy<=1;dy++) for(int dx=-1;dx<=1;dx++)
                    if(f.GetPixel(x+dx,y+dy).A<240) ok=false;
            }
            valid[x,y]=ok;
        }
        Rectangle best=Rectangle.Empty;
        for(int y=0;y<h;y++) for(int x=0;x<w;x++) {
            int width=w-x;
            for(int bottom=y;bottom<h;bottom++) {
                int run=0; while(run<width && valid[x+run,bottom]) run++;
                width=run; if(width==0) break;
                int height=bottom-y+1;
                if(width*height>best.Width*best.Height) best=new Rectangle(x,y,width,height);
            }
        }
        if(best.Width<2 || best.Height<2) throw new ArgumentException("No opaque material interior");
        return best;
    }

    private static int Mirror(int value,int length) {
        int p=value%(length*2); return p<length?p:length*2-1-p;
    }

    public static Bitmap Compose(Bitmap mask, Bitmap[] patterns, bool spriteMaterial) {
        int size = mask.Width;
        Rectangle patch=spriteMaterial?Interior(patterns):Rectangle.Empty;
        Bitmap output = new Bitmap(size, size * patterns.Length);
        for (int frame = 0; frame < patterns.Length; frame++) {
            Bitmap pattern = patterns[frame];
            double[] red = new double[3], green = new double[3], blue = new double[3], weight = new double[3];
            for (int y=0; y<pattern.Height; y++) for (int x=0; x<pattern.Width; x++) {
                Color c = pattern.GetPixel(x,y);
                if (c.A < 128) continue;
                int band = Math.Min(2, x*3/pattern.Width);
                // Bright opaque texels define the material; transparent ingot surroundings do not.
                double w = Math.Max(c.R,Math.Max(c.G,c.B))/255.0;
                w *= w;
                red[band]+=c.R*w; green[band]+=c.G*w; blue[band]+=c.B*w; weight[band]+=w;
            }
            for (int band=0; band<3; band++) {
                if (weight[band] == 0) { red[band]=green[band]=blue[band]=1; continue; }
                double max = Math.Max(red[band],Math.Max(green[band],blue[band]));
                red[band]=0.2+0.8*red[band]/max;
                green[band]=0.2+0.8*green[band]/max;
                blue[band]=0.2+0.8*blue[band]/max;
            }
            for (int y=0; y<size; y++) for (int x=0; x<size; x++) {
                Color c = mask.GetPixel(x,y);
                int high = Math.Max(c.R,Math.Max(c.G,c.B));
                int low = Math.Min(c.R,Math.Min(c.G,c.B));
                // Preserve outlines, shafts, contacts, existing coloured optical/energy accents.
                if (c.A == 0 || high < 64 || high-low > 40) {
                    output.SetPixel(x,y+frame*size,c); continue;
                }
                double position = (x+y)*2.0/(2*size-2);
                int a=Math.Min(1,(int)position), b=a+1;
                double t=position-a;
                double r=red[a]*(1-t)+red[b]*t;
                double g=green[a]*(1-t)+green[b]*t;
                double bl=blue[a]*(1-t)+blue[b]*t;
                // Preserve actual material detail; palette-only tint loses stars, grain and energy veins.
                int px,py;
                if(spriteMaterial) {
                    px=patch.X+Mirror(x,patch.Width); py=patch.Y+Mirror(y,patch.Height);
                } else {
                    // Uniform sampling in both axes, no fitting to the component's bounding box.
                    int step=Math.Max(1,Math.Min(pattern.Width,pattern.Height)/size);
                    px=(x*step)%pattern.Width; py=(y*step)%pattern.Height;
                }
                Color texture=pattern.GetPixel(px,py);
                r=Math.Min(1.15,0.20+0.35*r+0.80*texture.R/255.0);
                g=Math.Min(1.15,0.20+0.35*g+0.80*texture.G/255.0);
                bl=Math.Min(1.15,0.20+0.35*bl+0.80*texture.B/255.0);
                // Keep metallic highlights without wiping out the overlay.
                double highlight=Math.Max(0,(high-180)/75.0)*0.55;
                r=r*(1-highlight)+highlight; g=g*(1-highlight)+highlight; bl=bl*(1-highlight)+highlight;
                output.SetPixel(x,y+frame*size,Color.FromArgb(c.A,
                    Math.Min(255,(int)Math.Round(c.R*r)),Math.Min(255,(int)Math.Round(c.G*g)),Math.Min(255,(int)Math.Round(c.B*bl))));
            }
        }
        return output;
    }
}
