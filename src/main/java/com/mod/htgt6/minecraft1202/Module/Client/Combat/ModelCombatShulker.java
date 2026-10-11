package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatShulker;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/** Explicit 64x64 three-part Shulker model; named parts are validated during client init. */
public final class ModelCombatShulker extends ModelBase {
    public final ModelRenderer base;
    public final ModelRenderer lid;
    public final ModelRenderer head;

    public ModelCombatShulker() {
        textureWidth = 64; textureHeight = 64;
        base = new ModelRenderer(this, 0, 28);
        base.addBox(-8.0F, -8.0F, -8.0F, 16, 8, 16);
        base.setRotationPoint(0.0F, 24.0F, 0.0F);
        lid = new ModelRenderer(this, 0, 0);
        lid.addBox(-8.0F, -16.0F, -8.0F, 16, 12, 16);
        lid.setRotationPoint(0.0F, 24.0F, 0.0F);
        head = new ModelRenderer(this, 0, 52);
        head.addBox(-3.0F, 0.0F, -3.0F, 6, 6, 6);
        head.setRotationPoint(0.0F, 12.0F, 0.0F);
    }
    @Override public void render(Entity entity, float limb, float amount, float age, float yaw, float pitch, float scale) {
        setRotationAngles(limb, amount, age, yaw, pitch, scale, entity);
        base.render(scale); lid.render(scale); head.render(scale);
    }

    @Override public void setRotationAngles(float limb, float amount, float age, float yaw, float pitch,
            float scale, Entity entity) {
        EntityCombatShulker shulker = (EntityCombatShulker) entity;
        float peek = shulker.getClientPeekAmount(age - entity.ticksExisted);
        float phase = (0.5F + peek) * (float) Math.PI;
        float sine = MathHelper.sin(phase);
        float wobble = phase > (float) Math.PI ? MathHelper.sin(age * 0.1F) * 0.7F : 0.0F;
        lid.rotationPointY = 16.0F + sine * 8.0F + wobble;
        float turn = -1.0F + sine;
        lid.rotateAngleY = peek > 0.3F ? turn * turn * turn * turn * (float) Math.PI * 0.125F : 0.0F;
        head.rotateAngleY = yaw / 57.295776F;
        head.rotateAngleX = pitch / 57.295776F;
    }
}
