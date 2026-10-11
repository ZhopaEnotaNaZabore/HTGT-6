package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.SoulSpeed116;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import org.lwjgl.opengl.GL11;
@SideOnly(Side.CLIENT)
public final class SoulSpeedClient116 {
    @SubscribeEvent public void update(LivingUpdateEvent event){
        EntityLivingBase e=event.entityLiving;
        if(!e.worldObj.isRemote||e.ticksExisted%5!=0||e.motionX==0||e.motionZ==0||e.isSneaking()||SoulSpeed116.level(e)==0)return;
        if(!SoulSpeed116.isSurface(e.worldObj.getBlock(MathHelper.floor_double(e.posX),MathHelper.floor_double(e.boundingBox.minY-.2),MathHelper.floor_double(e.posZ))))return;
        Minecraft mc=Minecraft.getMinecraft();if(mc.effectRenderer==null)return;
        mc.effectRenderer.addEffect(new Soul(e.worldObj,e.posX+(e.getRNG().nextDouble()-.5)*e.width,e.boundingBox.minY+.1,e.posZ+(e.getRNG().nextDouble()-.5)*e.width,e.motionX*-.2,e.motionZ*-.2));
        float volume=e.getRNG().nextFloat()*.4F+e.getRNG().nextFloat()>.9F?.6F:0;
        if(volume>0)e.worldObj.playSound(e.posX,e.posY,e.posZ,"htgt6:particle.soul_escape",volume,.6F+e.getRNG().nextFloat()*.4F,false);
    }
    private static final class Soul extends EntityFX {
        private static final ResourceLocation[] TEXTURES=new ResourceLocation[11];
        static {for(int i=0;i<11;i++)TEXTURES[i]=new ResourceLocation("htgt6","textures/particle/soul_"+i+".png");}
        Soul(World w,double x,double y,double z,double vx,double vz){super(w,x,y,z);motionX=motionX*.01+vx;motionY=motionY*.01+.1;motionZ=motionZ*.01+vz;particleScale*=1.5F;setPosition(x+(rand.nextFloat()-rand.nextFloat())*.05,y+(rand.nextFloat()-rand.nextFloat())*.05,z+(rand.nextFloat()-rand.nextFloat())*.05);particleMaxAge=(int)(8/(rand.nextDouble()*.8+.2))+4;}
        @Override public int getFXLayer(){return 3;}
        @Override public void onUpdate(){prevPosX=posX;prevPosY=posY;prevPosZ=posZ;if(particleAge++>=particleMaxAge){setDead();return;}moveEntity(motionX,motionY,motionZ);motionX*=.96;motionY*=.96;motionZ*=.96;}
 @Override public void renderParticle(Tessellator t,float partial,float rx,float rz,float ryz,float rxy,float rxz){
  float size=.1F*particleScale,x=(float)(prevPosX+(posX-prevPosX)*partial-interpPosX),y=(float)(prevPosY+(posY-prevPosY)*partial-interpPosY),z=(float)(prevPosZ+(posZ-prevPosZ)*partial-interpPosZ);
  try(RenderStateGuard112 guard=RenderStateGuard112.capture()){
   Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURES[Math.min(10,particleAge*10/particleMaxAge)]);
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

}
