package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.Tessellator;
/** Non-uniform CubeDeformation, needed for modern foal legs without stretching their UVs. */
final class ModernHorseBox151 extends ModelBox {
 final TexturedQuad[] faces=new TexturedQuad[6];
 ModernHorseBox151(ModelRenderer part,int u,int v,float x,float y,float z,int w,int h,int d,float dx,float dy,float dz){
  super(part,u,v,x,y,z,w,h,d,0);
  float x0=x-dx,x1=x+w+dx,y0=y-dy,y1=y+h+dy,z0=z-dz,z1=z+d+dz;
  if(part.mirror){float t=x0;x0=x1;x1=t;}
  PositionTextureVertex[] p={point(x0,y0,z0),point(x1,y0,z0),point(x1,y1,z0),point(x0,y1,z0),point(x0,y0,z1),point(x1,y0,z1),point(x1,y1,z1),point(x0,y1,z1)};
  faces[0]=face(p,new int[]{5,1,2,6},u+d+w,v+d,u+d+w+d,v+d+h,part);
  faces[1]=face(p,new int[]{0,4,7,3},u,v+d,u+d,v+d+h,part);
  faces[2]=face(p,new int[]{5,4,0,1},u+d,v,u+d+w,v+d,part);
  faces[3]=face(p,new int[]{2,3,7,6},u+d+w,v+d,u+d+w+w,v,part);
  faces[4]=face(p,new int[]{1,0,3,2},u+d,v+d,u+d+w,v+d+h,part);
  faces[5]=face(p,new int[]{4,5,6,7},u+d+w+d,v+d,u+d+w+d+w,v+d+h,part);
  if(part.mirror)for(TexturedQuad face:faces)face.flipFace();
 }
 private static PositionTextureVertex point(float x,float y,float z){return new PositionTextureVertex(x,y,z,0,0);}
 private static TexturedQuad face(PositionTextureVertex[] p,int[] ix,int u0,int v0,int u1,int v1,ModelRenderer part){return new TexturedQuad(new PositionTextureVertex[]{p[ix[0]],p[ix[1]],p[ix[2]],p[ix[3]]},u0,v0,u1,v1,part.textureWidth,part.textureHeight);}
 @Override public void render(Tessellator tess,float scale){for(TexturedQuad face:faces)face.draw(tess,scale);}
}
