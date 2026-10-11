package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityThrownTrident;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Keeps the thrown model pointed along its interpolated flight trajectory. */
public final class RenderThrownTrident extends Render {
    public static final float REFERENCE_YAW_OFFSET = -90.0F;
    public static final float REFERENCE_PITCH_OFFSET = 90.0F;
    static final ResourceLocation TEXTURE = new ResourceLocation(HTGT6.MOD_ID.toLowerCase(),
            "textures/entity/trident.png");
    private final ModelTrident model = new ModelTrident();

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
        try (RenderStateGuard112 ignored = RenderStateGuard112.capture()) {
            bindEntityTexture(entity);
            GL11.glPushMatrix();
            try {
                GL11.glTranslated(x, y, z);
                float interpolatedYaw = entity.prevRotationYaw
                        + (entity.rotationYaw - entity.prevRotationYaw) * partialTick;
                float interpolatedPitch = entity.prevRotationPitch
                        + (entity.rotationPitch - entity.prevRotationPitch) * partialTick;
                // ThrownTridentRenderer 1.20.2: Y(yaw - 90), then Z(pitch + 90).
                GL11.glRotatef(interpolatedYaw + REFERENCE_YAW_OFFSET, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(interpolatedPitch + REFERENCE_PITCH_OFFSET, 0.0F, 0.0F, 1.0F);
                GL11.glColor4f(1, 1, 1, 1);
                model.render();
                if (entity instanceof EntityThrownTrident
                        && ((EntityThrownTrident)entity).isEnchanted()) {
                    TridentGlintRenderer.render(model);
                }
            } finally {
                GL11.glPopMatrix();
            }
        }
    }

    @Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}
