import com.mod.htgt6.sulimited.client.GalaxyCatalogCamera;
import com.mod.htgt6.sulimited.client.GalaxyMapCatalog;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.*;

public final class GalaxyCatalogFixture {
    static void check(boolean ok, String reason) { if (!ok) throw new AssertionError(reason); }
    static SpaceMapObject object(String name, float x, float y) {
        return new SpaceMapObject(name, "galaxy." + name, "fixture." + name, SpaceMapObjectType.GALAXY, x, y, Integer.MIN_VALUE);
    }
    public static void main(String[] args) throws Exception {
        SpaceTechExpandedRegistry.register(object("early", .1F, .9F));
        check(GalaxyMapCatalog.getEntries().size()==7, "early addon suppresses default galaxies");
        GalaxyMapCatalog.Entry first = GalaxyMapCatalog.getEntries().get(0);
        SpaceTechExpandedRegistry.register(object("late", .83F, .12F));
        check(GalaxyMapCatalog.getEntries().size()==8, "late addon missing from catalogue");
        check(GalaxyMapCatalog.getEntries().get(0)==first, "refresh invalidates selected object");
        GalaxyMapCatalog.Entry last = GalaxyMapCatalog.getEntries().get(7);
        check(last.mapX==.83F && last.mapY==.12F, "catalogue discards registered coordinates");
        SpaceTechExpandedRegistry.bootstrapDefaults();
        check(SpaceTechExpandedRegistry.getObjectCount()==8, "bootstrap is not idempotent");
        GalaxyCatalogCamera a = new GalaxyCatalogCamera(), b = new GalaxyCatalogCamera();
        a.resize(800,450); b.resize(800,450);
        int before = a.screenX(.83F,800);
        a.focus(.83F,.12F); b.focus(.83F,.12F);
        check(a.screenX(.83F,800)==before, "selection jumps instead of animating");
        for (int i=0;i<60;i++) a.step(1/60F);
        for (int i=0;i<30;i++) b.step(1/30F);
        check(Math.abs(a.x-b.x)<.01F && Math.abs(a.zoom-b.zoom)<.0001F,"animation depends on FPS");
        check(Math.abs(a.screenX(.83F,800)-400)<=1 && Math.abs(a.screenY(.12F,450)-225)<=1,"focus misses selected object");
        check(a.relativeZoom()>2.9F,"galaxy does not enlarge on focus");
        int start=a.screenX(.83F,800); a.drag(90,0);
        check(Math.abs(a.screenX(.83F,800)-start-90)<=1,"drag scale disagrees with zoom");
        a.resize(1600,900);
        check(Math.abs(a.screenX(.83F,1600)-2*(start+90))<=2,"resolution change changes map location");
        a.overview(); for(int i=0;i<120;i++) a.step(1/60F);
        check(Math.abs(a.x)<.01F && Math.abs(a.y)<.01F && Math.abs(a.relativeZoom()-1)<.01F,"overview reset");
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(new java.io.File("src/main/resources/assets/htgt6/textures/sulimited/map/realistic/nebula.png"));
        check(image.getWidth()==128 && image.getHeight()==128 && image.getColorModel().hasAlpha(),"nebula dimensions/alpha");
        check((image.getRGB(0,0)>>>24)==0,"nebula has opaque background");
        for (String name : new String[]{"RockyPlanetIndex.png", "OceanPlanetIndex.png"}) {
            java.awt.image.BufferedImage source = javax.imageio.ImageIO.read(new java.io.File("src/main/resources/assets/htgt6/textures/gui/celestialbodies/index/"+name));
            java.awt.image.BufferedImage disc = com.mod.htgt6.sulimited.client.GlieseDiscAnimation.scaleDisc(source,128);
            check((disc.getRGB(0,0)>>>24)==0, "disc loses transparent corner");
            for (int y=0;y<128;y++) for(int x=0;x<128;x++) {
                double dx=x-63.5, dy=y-63.5;
                if(dx*dx+dy*dy<50*50) check((disc.getRGB(x,y)>>>24)>240,"hole inside planet disc");
            }
        }
        float angleA = com.mod.htgt6.sulimited.client.GlieseDiscAnimation.angle(10,.25F,2400);
        float angleB = com.mod.htgt6.sulimited.client.GlieseDiscAnimation.angle(10,.75F,2400);
        check(Math.abs(angleB-angleA-.075F)<.00001F,"map animation quantized to ticks/frames");
        float end = com.mod.htgt6.sulimited.client.GlieseDiscAnimation.angle(2399,.99F,2400);
        float loopStart = com.mod.htgt6.sulimited.client.GlieseDiscAnimation.angle(2400,0,2400);
        check(Math.abs(loopStart+360-end)<.01F,"spin jumps at loop boundary");
        System.out.println("Galaxy catalogue checks passed: early/late addons, positions, smooth FPS-independent focus, drag/resize/reset, 128px transparent nebula.");
        System.out.println("Gliese disc checks passed: complete alpha silhouette, no interior holes, sub-tick rotation and continuous loop.");
    }
}
