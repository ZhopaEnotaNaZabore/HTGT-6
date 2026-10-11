package com.mod.htgt6.minecraft1202.Module.Client.Exploration;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Geometry, UVs and transforms from the 1.20.2 LlamaSpitModel/Renderer. */
public final class RenderLlamaSpit112 extends Render {
    private static final ResourceLocation TEXTURE = new ResourceLocation("htgt6", "textures/entity/exploration_update/llama/spit.png");
    private final ModelRenderer main;

    public RenderLlamaSpit112() {
        ModelBase model = new ModelBase() { };
        model.textureWidth = 64;
        model.textureHeight = 32;
        main = new ModelRenderer(model, 0, 0);
        main.addBox(-4, 0, 0, 2, 2, 2);
        main.addBox(0, -4, 0, 2, 2, 2);
        main.addBox(0, 0, -4, 2, 2, 2);
        main.addBox(0, 0, 0, 2, 2, 2);
        main.addBox(2, 0, 0, 2, 2, 2);
        main.addBox(0, 2, 0, 2, 2, 2);
        main.addBox(0, 0, 2, 2, 2, 2);
    }

    @Override public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {
        bindTexture(TEXTURE);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x, y + .15, z);
            GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partial - 90, 0, 1, 0);
            GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partial, 0, 0, 1);
            GL11.glColor4f(1, 1, 1, 1);
            main.render(.0625F);
        } finally {
            GL11.glPopMatrix();
        }
    }

    @Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}
