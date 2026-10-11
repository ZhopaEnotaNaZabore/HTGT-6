package com.mod.htgt6.sulimited.client;

import galaxyspace.systems.SolarSystem.SolarSystemBodies;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Vacuum sky with Uranus and its narrow tilted ring system. */
public final class SkyProviderUranusMoon extends SkyProviderCharon {
    private static final ResourceLocation URANUS=new ResourceLocation("galaxyspace","textures/gui/celestialbodies/sol/uranus.png");
    private static final ResourceLocation RINGS=new ResourceLocation("galaxyspace","textures/gui/celestialbodies/sol/uranus_rings.png");
    private final float horizontal,vertical,size,ringTilt;
    public SkyProviderUranusMoon(float horizontal,float vertical,float size,float ringTilt){this.horizontal=horizontal;this.vertical=vertical;this.size=size;this.ringTilt=ringTilt;}
    protected ResourceLocation parentTexture(){return SolarSystemBodies.planetUranus!=null&&SolarSystemBodies.planetUranus.getBodyIcon()!=null?SolarSystemBodies.planetUranus.getBodyIcon():URANUS;}
    protected float parentHorizontalAngle(){return horizontal;} protected float parentVerticalAngle(){return vertical;} protected float parentSize(){return size;}
    protected void renderParentBody(Minecraft minecraft){GL11.glPushMatrix();GL11.glRotatef(horizontal,0,1,0);GL11.glRotatef(vertical,1,0,0);GL11.glPushMatrix();GL11.glRotatef(ringTilt,0,1,0);minecraft.getTextureManager().bindTexture(RINGS);drawSkyQuad(size*1.7F);GL11.glPopMatrix();minecraft.getTextureManager().bindTexture(parentTexture());drawSkyQuad(size);GL11.glPopMatrix();}
}
