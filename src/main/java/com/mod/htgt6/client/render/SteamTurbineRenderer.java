package com.mod.htgt6.client.render;

import com.mod.htgt6.common.turbine.MultiTileSteamTurbine;
import com.mod.htgt6.common.GTHooks.TurbineRotorHooks.GT6TurbineRotorHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;

/** Dedicated axial rotor/alternator model, independent of chemical-plant rendering. */
public final class SteamTurbineRenderer extends TileEntitySpecialRenderer {
    private final MultiblockAnimationClock clock=new MultiblockAnimationClock();
    private static final ResourceLocation METAL=new ResourceLocation("htgt6","textures/blocks/hullUlv.png");
    private static final ResourceLocation PARTICLES=new ResourceLocation("minecraft","textures/particle/particles.png");
    private final FloatBuffer view=BufferUtils.createFloatBuffer(16);
    private static final double[][][] BLADE_MESH=createBladeMesh();
    static void bindMetal(){Minecraft.getMinecraft().getTextureManager().bindTexture(METAL);}
    @Override public void renderTileEntityAt(TileEntity tile,double x,double y,double z,float partial){
        if(!(tile instanceof MultiTileSteamTurbine)||tile.getWorldObj()==null)return;
        MultiTileSteamTurbine turbine=(MultiTileSteamTurbine)tile;
        boolean rotor=turbine.rotor()!=null;
        double time=clock.sample(tile,MultiblockAnimationClock.frameTime(tile.getWorldObj().getTotalWorldTime(), partial),turbine.isActive()&&rotor);
        int color=rotor?GT6TurbineRotorHooks.ITEM.getColorFromItemStack(turbine.inventory[0],0):0x87969E;
        float r=(color>>16&255)/255F,g=(color>>8&255)/255F,b=(color&255)/255F;
        MultiblockRenderState state=new MultiblockRenderState("mixer");
        try{
            bindMetal();
            GL11.glTranslated(x+.5,y+.5,z+.5);
            int facing=tile.getBlockMetadata();GL11.glRotatef(facing==3?180:facing==4?90:facing==5?-90:0,0,1,0);
            double radius=turbine.isStructureFormed()?2.48:.48;
            front(radius,rotor,time*12,r,g,b,turbine.isActive());
            if(!turbine.isStructureFormed())return;
            // Fixed bearing pedestals and longitudinal mounting rails.
            box(-1.55,-2.4,.6,-1.32,-2.1,11.5,.25F,.30F,.33F);
            box(1.32,-2.4,.6,1.55,-2.1,11.5,.25F,.30F,.33F);
            for(double at:new double[]{1,4.4,8.2,11}){
                box(-.85,-2.1,at-.30,.85,-1.85,at+.30,.32F,.39F,.43F);
                box(-.40,-1.85,at-.19,.40,-.30,at+.19,.32F,.39F,.43F);
                cylinder(.56,at-.24,at+.24,.49F,.55F,.59F);
                ring(.30,.48,at-.255,.22F,.28F,.32F);
                for(int side:new int[]{-1,1}) {
                    box(side*.65-.07,-1.86,at-.22,side*.65+.07,-1.77,at-.08,.65F,.69F,.72F);
                    box(side*.65-.07,-1.86,at+.08,side*.65+.07,-1.77,at+.22,.65F,.69F,.72F);
                }
            }
            GL11.glPushMatrix();GL11.glRotated(time*12%360,0,0,1);
            cylinder(.24,.55,11.5,.48F,.53F,.56F);
            for(double at:new double[]{.8,4.35,8.15,8.7}) {
                cylinder(.36,at-.12,at+.12,.55F,.61F,.65F);
                for(int i=0;i<6;i++) {
                    double a=i*Math.PI/3;
                    box(Math.cos(a)*.30-.045,Math.sin(a)*.30-.045,at-.14,
                        Math.cos(a)*.30+.045,Math.sin(a)*.30+.045,at-.12,.75F,.77F,.78F);
                }
            }
            GL11.glPopMatrix();
            for(int i=0;i<3;i++){
                double at=2+i*2.4,size=1.25+i*.32;
                // Thick stationary guide casing around two moving blade rows per stage.
                cylinder(size+.18,at+.34,at+.48,.35F,.42F,.47F);
                ring(size*.89,size+.18,at+.33,.52F,.60F,.64F);
                for(int side:new int[]{-1,1})
                    box(side*(size+.12)-.08,-1.98,at+.30,side*(size+.12)+.08,0,at+.50,.30F,.36F,.40F);
                if(rotor){
                    rotor(size,at,time*12+i*11,r,g,b);
                    rotor(size*.93,at-.43,time*12+i*11+9,r*.85F,g*.85F,b*.85F);
                }
            }
            // Rear alternator, copper windings and stationary end caps.
            cylinder(.93,9.1,11.2,.20F,.26F,.30F);
            for(int i=0;i<12;i++) {
                GL11.glPushMatrix();GL11.glRotatef(i*30,0,0,1);
                box(1.0,-.14,9.18,1.27,.14,11.16,.68F,.34F,.16F);
                for(int band=0;band<5;band++)
                    box(1.27,-.15,9.25+band*.38,1.30,.15,9.31+band*.38,.88F,.49F,.22F);
                GL11.glPopMatrix();
            }
            cylinder(1.50,8.95,9.12,.42F,.47F,.51F);
            cylinder(1.50,11.22,11.39,.42F,.47F,.51F);
            ring(.24,1.50,8.94,.42F,.47F,.51F);
            ring(.24,1.50,11.40,.42F,.47F,.51F);
            // Fixed lubrication/service lines and small travelling flow indicators.
            for(int side:new int[]{-1,1}) {
                box(side*2.14-.06,-1.45,.8,side*2.14+.06,-1.33,10.8,.30F,.49F,.52F);
                for(double at:new double[]{1,4.4,8.2})
                    box(Math.min(side*.55,side*2.14),-1.45,at-.06,
                        Math.max(side*.55,side*2.14),-1.33,at+.06,.30F,.49F,.52F);
                if(turbine.isActive())for(int i=0;i<3;i++) {
                    double at=.9+(time*.10+i*3.1)%9.4;
                    box(side*2.14-.075,-1.47,at,side*2.14+.075,-1.31,at+.17,.28F,.85F,.85F);
                }
            }
            if(turbine.isActive())steam(time);
        }finally{state.close();}
    }
    static void front(double radius,boolean hasRotor,double angle,float r,float g,float b,boolean active){
        box(-radius,-radius,-.525,radius,radius,-.505,.055F,.07F,.09F);
        cylinder(radius*.97,-.62,-.51,.32F,.38F,.43F);
        ring(radius*.91,radius*.97,-.625,.55F,.62F,.67F);
        ring(radius*.86,radius*.90,-.63,.23F,.29F,.34F);
        if(hasRotor)rotor(radius*.85,-.66,angle,r,g,b);
        double hub=radius*.18;
        cylinder(hub,-.84,-.54,.36F,.43F,.47F);
        ring(0,hub,-.845,.42F,.49F,.54F);
        for(int i=0;i<8;i++){
            double a=i*Math.PI/4,cx=Math.cos(a)*radius*.94,cy=Math.sin(a)*radius*.94;
            double bolt=radius*.022;
            box(cx-bolt,cy-bolt,-.65,cx+bolt,cy+bolt,-.627,.70F,.74F,.77F);
        }
        box(-hub*.27,-hub*.27,-.85,hub*.27,hub*.27,-.846,active?.2F:.1F,active?1F:.4F,.75F);
    }
    private static void rotor(double radius,double z,double angle,float r,float g,float b){
        GL11.glPushMatrix();GL11.glTranslated(0,0,z);GL11.glRotated(angle%360,0,0,1);
        GL11.glScaled(radius,radius,1);
        // Swept, tapered blades with a twisted section and real edge thickness.
        // All blades are batched into one draw, not one tessellator flush per blade.
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setBrightness(0x00F000F0);
        for(int segment=0;segment<BLADE_MESH.length;segment++) {
            double[][] p=BLADE_MESH[segment];
            quad(t,p,0,2,3,1,.27F+r*.73F,.27F+g*.73F,.27F+b*.73F);
            quad(t,p,4,5,7,6,.18F+r*.55F,.18F+g*.55F,.18F+b*.55F);
            quad(t,p,0,4,6,2,.48F+r*.45F,.48F+g*.45F,.48F+b*.45F);
            quad(t,p,1,3,7,5,.12F+r*.50F,.12F+g*.50F,.12F+b*.50F);
            if(segment%5==4)quad(t,p,2,6,7,3,.48F+r*.45F,.48F+g*.45F,.48F+b*.45F);
        }
        t.draw();
        cylinder(.20,-.09,.12,.43F,.49F,.53F);
        ring(0,.20,-.095,.58F,.64F,.68F);
        GL11.glPopMatrix();
    }
    private static double[][][] createBladeMesh(){
        double[][][] mesh=new double[18*5][][];
        for(int blade=0;blade<18;blade++)for(int segment=0;segment<5;segment++) {
            double a=segment/5D,c=(segment+1)/5D;
            double[][] p=new double[8][];
            for(int end=0;end<2;end++)for(int edge=0;edge<2;edge++){
                double f=end==0?a:c,rad=.18+.82*f;
                double theta=blade*Math.PI/9+.25*f*f+(edge==0?-.025:.19-.04*f);
                double depth=(edge==0?-.065:.065)*(1-.55*f);
                int index=end*2+edge;
                p[index]=new double[]{Math.cos(theta)*rad,Math.sin(theta)*rad,depth};
                p[index+4]=new double[]{p[index][0],p[index][1],depth+.035};
            }
            mesh[blade*5+segment]=p;
        }
        return mesh;
    }
    private static void quad(Tessellator t,double[][] p,int a,int b,int c,int d,float r,float g,float blue){
        t.setColorOpaque_F(r,g,blue);
        t.addVertexWithUV(p[a][0],p[a][1],p[a][2],0,0);
        t.addVertexWithUV(p[b][0],p[b][1],p[b][2],0,1);
        t.addVertexWithUV(p[c][0],p[c][1],p[c][2],1,1);
        t.addVertexWithUV(p[d][0],p[d][1],p[d][2],1,0);
    }
    private static void cylinder(double radius,double z0,double z1,float r,float g,float b){
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setBrightness(0x00F000F0);
        for(int i=0;i<32;i++) {
            double a=i*Math.PI/16,c=(i+1)*Math.PI/16;
            float shade=(float)(.70+.30*Math.cos(a-.7));
            t.setColorOpaque_F(r*shade,g*shade,b*shade);
            t.addVertexWithUV(Math.cos(a)*radius,Math.sin(a)*radius,z0,0,0);
            t.addVertexWithUV(Math.cos(c)*radius,Math.sin(c)*radius,z0,1,0);
            t.addVertexWithUV(Math.cos(c)*radius,Math.sin(c)*radius,z1,1,1);
            t.addVertexWithUV(Math.cos(a)*radius,Math.sin(a)*radius,z1,0,1);
        }t.draw();
    }
    private void steam(double time){
        view.clear();GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,view);
        // Camera-facing basis in the already rotated turbine coordinate system.
        double rx=view.get(0),ry=view.get(4),rz=view.get(8);
        double ux=view.get(1),uy=view.get(5),uz=view.get(9);
        Minecraft.getMinecraft().getTextureManager().bindTexture(PARTICLES);
        GL11.glPushAttrib(GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_COLOR_BUFFER_BIT);
        GL11.glDepthMask(false);GL11.glAlphaFunc(GL11.GL_GREATER,.005F);
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setBrightness(0x00D000D0);
        // Bounded local sprites, never world particles/entities or server-side work.
        for(int i=0;i<18;i++){
            double age=(time*.009+i*.61803398875)%1;
            double x=Math.sin(i*2.4)*1.1,y=-.45+age*1.75,z=1.0+age*6.8;
            double size=.16+age*.30;
            float alpha=(float)(Math.sin(age*Math.PI)*.19);
            int sprite=7-Math.min(7,(int)(age*8));
            double u0=sprite/16D,u1=u0+1/16D,v0=0,v1=1/16D;
            t.setColorRGBA_F(.82F,.91F,.95F,alpha);
            t.addVertexWithUV(x+(-rx-ux)*size,y+(-ry-uy)*size,z+(-rz-uz)*size,u0,v1);
            t.addVertexWithUV(x+(rx-ux)*size,y+(ry-uy)*size,z+(rz-uz)*size,u1,v1);
            t.addVertexWithUV(x+(rx+ux)*size,y+(ry+uy)*size,z+(rz+uz)*size,u1,v0);
            t.addVertexWithUV(x+(-rx+ux)*size,y+(-ry+uy)*size,z+(-rz+uz)*size,u0,v0);
        }
        t.draw();GL11.glPopAttrib();bindMetal();
    }
    private static void ring(double inner,double outer,double z,float r,float g,float b){
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setBrightness(0x00F000F0);t.setColorOpaque_F(r,g,b);
        for(int i=0;i<32;i++){
            double a=i*Math.PI/16,c=(i+1)*Math.PI/16;
            t.addVertexWithUV(Math.cos(a)*inner,Math.sin(a)*inner,z,0,0);
            t.addVertexWithUV(Math.cos(a)*outer,Math.sin(a)*outer,z,0,1);
            t.addVertexWithUV(Math.cos(c)*outer,Math.sin(c)*outer,z,1,1);
            t.addVertexWithUV(Math.cos(c)*inner,Math.sin(c)*inner,z,1,0);
        }t.draw();
    }
    private static void box(double x0,double y0,double z0,double x1,double y1,double z1,float r,float g,float b){
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setBrightness(0x00F000F0);t.setColorOpaque_F(.15F+r*.85F,.15F+g*.85F,.15F+b*.85F);
        double[][] p={{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0},{x0,y0,z1},{x1,y0,z1},{x1,y1,z1},{x0,y1,z1}};
        int[][] faces={{0,1,2,3},{5,4,7,6},{4,0,3,7},{1,5,6,2},{3,2,6,7},{4,5,1,0}};
        for(int[] face:faces)for(int i=0;i<4;i++){double[] v=p[face[i]];t.addVertexWithUV(v[0],v[1],v[2],i==1||i==2?1:0,i>=2?1:0);}t.draw();
    }
}
