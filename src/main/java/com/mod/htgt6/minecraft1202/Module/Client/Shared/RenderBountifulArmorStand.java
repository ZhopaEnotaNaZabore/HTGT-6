package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

/** RenderBiped is deliberately used so vanilla 1.7 armor/held-item render passes remain functional. */
public final class RenderBountifulArmorStand extends RenderBiped {
    private static final ResourceLocation TEXTURE = new ResourceLocation("htgt6", "textures/entity/bountiful/armor_stand.png");
    public RenderBountifulArmorStand() { super(new ModelBountifulArmorStand(), 0.0F); }
    @Override protected void func_82421_b() {
        field_82423_g=new ModelArmorStandArmor120(1.0F);
        field_82425_h=new ModelArmorStandArmor120(.5F);
    }
    @Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
    @Override protected void preRenderCallback(EntityLivingBase entity, float partialTicks) {
        if (((com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand) entity).isSmall())
            org.lwjgl.opengl.GL11.glScalef(0.5F, 0.5F, 0.5F);
    }
}
