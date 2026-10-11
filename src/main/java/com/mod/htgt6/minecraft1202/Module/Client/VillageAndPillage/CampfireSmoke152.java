package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityCampfire149;
import net.minecraft.client.Minecraft;import net.minecraft.client.particle.EntityFX;import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;import net.minecraft.world.World;import net.minecraft.init.Blocks;import org.lwjgl.opengl.GL11;
import cpw.mods.fml.relauncher.Side;import cpw.mods.fml.relauncher.SideOnly;
/** Official cosy/signal smoke: random sprite, long lifetime, tiny drift and final fade. */
@SideOnly(Side.CLIENT)
public final class CampfireSmoke152 extends EntityFX {
 private static final ResourceLocation[] TEXTURES=new ResourceLocation[12];
 static {for(int i=0;i<12;i++)TEXTURES[i]=new ResourceLocation("htgt6","textures/particle/big_smoke_"+i+".png");}
 private final int sprite;
 private static final int[] DX={0,-1,0,1},DZ={1,0,-1,0};
 public static double cookingOffsetX(int slot,int facing){int d=(slot+facing)&3;return -DX[d]*.3125+DX[(d+1)&3]*.3125;}
 public static double cookingOffsetZ(int slot,int facing){int d=(slot+facing)&3;return -DZ[d]*.3125+DZ[(d+1)&3]*.3125;}
 public CampfireSmoke152(World w,double x,double y,double z,boolean signal){
  super(w,x,y,z);particleScale*=3F;setSize(.25F,.25F);yOffset=0;setPosition(x,y,z);
  particleMaxAge=rand.nextInt(50)+(signal?280:80);particleGravity=3E-6F;
  motionX=motionZ=0;motionY=.07D+rand.nextFloat()/500F;particleAlpha=signal?.95F:.9F;sprite=rand.nextInt(12);
 }
 @Override public int getFXLayer(){return 3;}
 @Override public void onUpdate(){
  prevPosX=posX;prevPosY=posY;prevPosZ=posZ;
  if(particleAge++>=particleMaxAge||particleAlpha<=0){setDead();return;}
  motionX+=rand.nextFloat()/5000F*(rand.nextBoolean()?1:-1);motionZ+=rand.nextFloat()/5000F*(rand.nextBoolean()?1:-1);motionY-=particleGravity;
  moveEntity(motionX,motionY,motionZ);
  if(particleAge>=particleMaxAge-60&&particleAlpha>.01F)particleAlpha-=.015F;
 }
 public static void tick(TileEntityCampfire149 tile){
  World w=tile.getWorldObj();Minecraft mc=Minecraft.getMinecraft();if(w==null||!w.isRemote||mc==null||mc.effectRenderer==null)return;
  int x=tile.xCoord,y=tile.yCoord,z=tile.zCoord;boolean signal=w.getBlock(x,y-1,z)==Blocks.hay_block;
  if(w.rand.nextFloat()<.11F)for(int i=0;i<w.rand.nextInt(2)+2;i++){
   double sx=x+.5+w.rand.nextDouble()/3*(w.rand.nextBoolean()?1:-1),sz=z+.5+w.rand.nextDouble()/3*(w.rand.nextBoolean()?1:-1);
   mc.effectRenderer.addEffect(new CampfireSmoke152(w,sx,y+w.rand.nextDouble()+w.rand.nextDouble(),sz,signal));
  }
  int facing=tile.getBlockMetadata()&3;
  for(int i=0;i<4;i++)if(tile.item(i)!=null&&w.rand.nextFloat()<.2F){
   double sx=x+.5+cookingOffsetX(i,facing),sz=z+.5+cookingOffsetZ(i,facing);
   for(int k=0;k<4;k++)w.spawnParticle("smoke",sx,y+.5,sz,0,5E-4,0);
  }
 }
 @Override public void renderParticle(Tessellator t,float partial,float rx,float rz,float ryz,float rxy,float rxz){
  float size=.1F*particleScale,x=(float)(prevPosX+(posX-prevPosX)*partial-interpPosX),y=(float)(prevPosY+(posY-prevPosY)*partial-interpPosY),z=(float)(prevPosZ+(posZ-prevPosZ)*partial-interpPosZ);
  try(RenderStateGuard112 guard=RenderStateGuard112.capture()){
   Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURES[sprite]);
   // Match the modern translucent particle pass and its shader's alpha cutoff.
   GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glAlphaFunc(GL11.GL_GEQUAL,.1F);
   GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(true);GL11.glDisable(GL11.GL_LIGHTING);
   GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glColor4f(1,1,1,1);
   t.startDrawingQuads();t.setBrightness(getBrightnessForRender(partial));t.setColorRGBA_F(1,1,1,Math.max(0,particleAlpha));
   t.addVertexWithUV(x-rx*size-rxy*size,y-rz*size,z-ryz*size-rxz*size,1,1);
   t.addVertexWithUV(x-rx*size+rxy*size,y+rz*size,z-ryz*size+rxz*size,1,0);
   t.addVertexWithUV(x+rx*size+rxy*size,y+rz*size,z+ryz*size+rxz*size,0,0);
   t.addVertexWithUV(x+rx*size-rxy*size,y-rz*size,z+ryz*size-rxz*size,0,1);t.draw();
  }
 }
}
