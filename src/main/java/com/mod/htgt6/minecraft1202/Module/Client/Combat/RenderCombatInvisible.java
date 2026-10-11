package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/** Particle-only entity renderer. */
public final class RenderCombatInvisible extends Render {
    @Override public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) { }
    @Override protected ResourceLocation getEntityTexture(Entity entity) { return null; }
}
