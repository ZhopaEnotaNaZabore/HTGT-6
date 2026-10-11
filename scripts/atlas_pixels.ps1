# Deterministic packing/resizing only: original colours and silhouettes are not redrawn.
if (-not ('ItemAtlasPixels' -as [type])) {
    $drawingReferences = @('System.Drawing')
    if ($PSVersionTable.PSEdition -eq 'Core') {
        $drawingReferences = @('System.Drawing.Common','System.Drawing.Primitives','System.Runtime')
    }
    Add-Type -ReferencedAssemblies $drawingReferences -TypeDefinition @'
using System;
using System.Drawing;
public static class ItemAtlasPixels {
    public static void Copy(Bitmap from, Bitmap to, int left, int top) {
        for (int y=0; y<from.Height; y++) for (int x=0; x<from.Width; x++)
            to.SetPixel(left+x, top+y, from.GetPixel(x,y));
    }
    public static void Fit(Bitmap from, Bitmap to, int left, int top, int width, int height, int dx, int dy) {
        FitSize(from,to,left,top,width,height,dx,dy,32);
    }
    public static void FitSize(Bitmap from, Bitmap to, int left, int top, int width, int height, int dx, int dy, int size) {
        int x0=width, y0=height, x1=-1, y1=-1;
        for (int y=0; y<height; y++) for (int x=0; x<width; x++) {
            if (from.GetPixel(left+x,top+y).A == 0) continue;
            x0=Math.Min(x0,x); y0=Math.Min(y0,y); x1=Math.Max(x1,x); y1=Math.Max(y1,y);
        }
        if (x1<0) throw new ArgumentException("Empty atlas icon");
        int w=x1-x0+1, h=y1-y0+1;
        double scale=(size*0.875)/Math.Max(w,h);
        int ow=Math.Max(1,(int)Math.Round(w*scale)), oh=Math.Max(1,(int)Math.Round(h*scale));
        for (int y=0; y<oh; y++) for (int x=0; x<ow; x++) {
            int sx=x0+Math.Min(w-1,(int)((x+0.5)*w/ow));
            int sy=y0+Math.Min(h-1,(int)((y+0.5)*h/oh));
            to.SetPixel(dx+(size-ow)/2+x,dy+(size-oh)/2+y,from.GetPixel(left+sx,top+sy));
        }
    }
}
'@
}
