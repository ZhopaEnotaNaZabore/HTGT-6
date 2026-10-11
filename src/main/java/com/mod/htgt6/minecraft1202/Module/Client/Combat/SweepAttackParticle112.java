package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/** Four-frame Minecraft 1.12 sweeping arc rendered as a camera-facing particle. */
public final class SweepAttackParticle112 extends EntityFX {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("htgt6", "textures/entity/combat_update/sweep.png");

    private SweepAttackParticle112(World world, double x, double y, double z) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        particleMaxAge = 4;
        particleScale = 1.0F;
        motionX = motionY = motionZ = 0.0D;
        noClip = true;
    }

    public static void spawn(double x, double y, double z) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.theWorld != null && minecraft.effectRenderer != null)
            minecraft.effectRenderer.addEffect(new SweepAttackParticle112(minecraft.theWorld, x, y, z));
    }

    @Override public int getFXLayer() { return 3; }

    @Override public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) setDead();
    }

    @Override public void renderParticle(Tessellator tessellator, float partialTicks,
            float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        int frame = (int) (((particleAge + partialTicks) / particleMaxAge) * 4.0F);
        if (frame < 0 || frame > 3) return;
        float minU = frame / 4.0F;
        float maxU = minU + 0.24975F;
        float minV = 0.0F;
        float maxV = 1.0F;
        float size = 0.5F * particleScale;
        float x = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
        float y = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
        float z = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        tessellator.startDrawingQuads();
        tessellator.setBrightness(getBrightnessForRender(partialTicks));
        tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, 1.0F);
        tessellator.addVertexWithUV(x - rotationX * size - rotationXY * size,
                y - rotationZ * size, z - rotationYZ * size - rotationXZ * size, maxU, maxV);
        tessellator.addVertexWithUV(x - rotationX * size + rotationXY * size,
                y + rotationZ * size, z - rotationYZ * size + rotationXZ * size, maxU, minV);
        tessellator.addVertexWithUV(x + rotationX * size + rotationXY * size,
                y + rotationZ * size, z + rotationYZ * size + rotationXZ * size, minU, minV);
        tessellator.addVertexWithUV(x + rotationX * size - rotationXY * size,
                y - rotationZ * size, z + rotationYZ * size - rotationXZ * size, minU, maxV);
        tessellator.draw();
        GL11.glPopAttrib();
    }
}
