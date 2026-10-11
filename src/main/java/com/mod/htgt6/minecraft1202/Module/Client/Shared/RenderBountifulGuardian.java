package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;

import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulGuardian;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Guardian renderer with the animated eye-to-target beam used by the 1.12 attack. */
public final class RenderBountifulGuardian extends RenderLiving {
    private static final ResourceLocation NORMAL = new ResourceLocation(
            "htgt6", "textures/entity/bountiful/guardian.png");
    private static final ResourceLocation ELDER = new ResourceLocation(
            "htgt6", "textures/entity/bountiful/guardian_elder.png");
    private static final ResourceLocation BEAM = new ResourceLocation(
            "htgt6", "textures/entity/bountiful/guardian_beam.png");

    public RenderBountifulGuardian() {
        super(new ModelBountifulGuardian(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return ((EntityBountifulGuardian)entity).isElder() ? ELDER : NORMAL;
    }

    @Override
    protected void preRenderCallback(EntityLivingBase entity, float partial) {
        if (((EntityBountifulGuardian)entity).isElder()) GL11.glScalef(2.35F, 2.35F, 2.35F);
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {
        super.doRender(entity, x, y, z, yaw, partial);
        EntityBountifulGuardian guardian = (EntityBountifulGuardian)entity;
        EntityLivingBase target = guardian.getBeamTarget();
        if (target != null) renderBeam(guardian, target, x, y, z, partial);
    }

    private void renderBeam(EntityBountifulGuardian guardian, EntityLivingBase target,
            double renderX, double renderY, double renderZ, float partial) {
        double guardianX = interpolate(guardian.prevPosX, guardian.posX, partial);
        double guardianY = interpolate(guardian.prevPosY, guardian.posY, partial) + guardian.getEyeHeight();
        double guardianZ = interpolate(guardian.prevPosZ, guardian.posZ, partial);
        double targetX = interpolate(target.prevPosX, target.posX, partial);
        double targetY = interpolate(target.prevPosY, target.posY, partial) + target.height * 0.5D;
        double targetZ = interpolate(target.prevPosZ, target.posZ, partial);
        double dx = targetX - guardianX;
        double dy = targetY - guardianY;
        double dz = targetZ - guardianZ;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 0.001D) return;

        float progress = guardian.getBeamProgress(partial);
        float power = progress * progress;
        int red = 64 + (int)(power * 191.0F);
        int green = 32 + (int)(power * 191.0F);
        int blue = 128 - (int)(power * 64.0F);
        float time = guardian.ticksExisted + partial;
        double scroll = -(time * 0.075D);
        double endV = length * 2.5D + scroll;
        double rotation = time * 0.05D * -1.5D;

        try (RenderStateGuard112 ignored = RenderStateGuard112.capture()) {
            bindTexture(BEAM);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
            GL11.glPushMatrix();
            try {
                GL11.glTranslated(renderX, renderY + guardian.getEyeHeight(), renderZ);
                float beamYaw = (float)Math.atan2(dz, dx);
                float beamPitch = (float)Math.acos(dy / length);
                GL11.glRotatef((float)((Math.PI / 2.0D - beamYaw) * 180.0D / Math.PI), 0, 1, 0);
                GL11.glRotatef(beamPitch * 180.0F / (float)Math.PI, 1, 0, 0);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_CULL_FACE);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

                Tessellator tessellator = Tessellator.instance;
                tessellator.startDrawingQuads();
                tessellator.setColorRGBA(red, green, blue, 255);
                addBeamFace(tessellator, rotation, 0.0D, length + 1.0D, 0.20D, scroll, endV);
                addBeamFace(tessellator, rotation + Math.PI / 2.0D, 0.0D,
                        length + 1.0D, 0.20D, scroll, endV);
                addBeamEnd(tessellator, rotation, length + 1.0D,
                        guardian.ticksExisted % 2 == 0 ? 0.5D : 0.0D);
                tessellator.draw();
            } finally {
                GL11.glPopMatrix();
            }
        }
    }

    private static void addBeamFace(Tessellator tessellator, double angle, double start,
            double end, double radius, double startV, double endV) {
        double x1 = Math.cos(angle) * radius;
        double z1 = Math.sin(angle) * radius;
        double x2 = Math.cos(angle + Math.PI) * radius;
        double z2 = Math.sin(angle + Math.PI) * radius;
        tessellator.addVertexWithUV(x1, end, z1, 0.5D, endV);
        tessellator.addVertexWithUV(x1, start, z1, 0.5D, startV);
        tessellator.addVertexWithUV(x2, start, z2, 0.0D, startV);
        tessellator.addVertexWithUV(x2, end, z2, 0.0D, endV);
    }

    private static void addBeamEnd(Tessellator tessellator, double angle, double end, double v) {
        double radius = 0.282D;
        double x1 = Math.cos(angle + Math.PI * 0.75D) * radius;
        double z1 = Math.sin(angle + Math.PI * 0.75D) * radius;
        double x2 = Math.cos(angle + Math.PI * 0.25D) * radius;
        double z2 = Math.sin(angle + Math.PI * 0.25D) * radius;
        double x3 = Math.cos(angle + Math.PI * 1.75D) * radius;
        double z3 = Math.sin(angle + Math.PI * 1.75D) * radius;
        double x4 = Math.cos(angle + Math.PI * 1.25D) * radius;
        double z4 = Math.sin(angle + Math.PI * 1.25D) * radius;
        tessellator.addVertexWithUV(x1, end, z1, 0.5D, v + 0.5D);
        tessellator.addVertexWithUV(x2, end, z2, 1.0D, v + 0.5D);
        tessellator.addVertexWithUV(x3, end, z3, 1.0D, v);
        tessellator.addVertexWithUV(x4, end, z4, 0.5D, v);
    }

    private static double interpolate(double previous, double current, float partial) {
        return previous + (current - previous) * partial;
    }
}
