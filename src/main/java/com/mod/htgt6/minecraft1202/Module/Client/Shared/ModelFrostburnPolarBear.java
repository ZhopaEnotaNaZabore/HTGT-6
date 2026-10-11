package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.minecraft1202.Module.Client.Shared.BackportEntityModel;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.BackportModelPart;
import com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnPolarBear;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

/** Minecraft 1.10 Polar Bear geometry and standing transition on the named-part bridge. */
public final class ModelFrostburnPolarBear extends BackportEntityModel<EntityFrostburnPolarBear> {
    private static final float PI = (float) Math.PI;
    private final BackportModelPart head, body;
    private final BackportModelPart backRightLeg, backLeftLeg, frontRightLeg, frontLeftLeg;

    public ModelFrostburnPolarBear() {
        super(128, 64);
        head = part("head", 0, 0).box(-3.5F, -3.0F, -3.0F, 7, 7, 7).pos(0, 10, -16).storePose();
        BackportModelPart muzzle = part("muzzle", 0, 44).box(-2.5F, 1.0F, -6.0F, 5, 3, 3).storePose();
        BackportModelPart rightEar = part("right_ear", 26, 0).box(-4.5F, -4.0F, -1.0F, 2, 2, 1).storePose();
        BackportModelPart leftEar = part("left_ear", 26, 0).mirror(true)
                .box(2.5F, -4.0F, -1.0F, 2, 2, 1).storePose();
        head.child(muzzle).child(rightEar).child(leftEar);
        body = part("body", 0, 19).box(-5, -13, -7, 14, 14, 11)
                .textureOffset(39, 0).box(-4, -25, -7, 12, 12, 10).pos(-2, 9, 12)
                .rotation(PI / 2.0F, 0, 0).storePose();
        backRightLeg = part("back_right_leg", 50, 22).box(-2, 0, -2, 4, 10, 8)
                .pos(-4.5F, 14, 6).storePose();
        backLeftLeg = part("back_left_leg", 50, 22).box(-2, 0, -2, 4, 10, 8)
                .pos(4.5F, 14, 6).storePose();
        frontRightLeg = part("front_right_leg", 50, 40).box(-2, 0, -2, 4, 10, 6)
                .pos(-3.5F, 14, -8).storePose();
        frontLeftLeg = part("front_left_leg", 50, 40).box(-2, 0, -2, 4, 10, 6)
                .pos(3.5F, 14, -8).storePose();
    }

    @Override
    protected void setupAnim(EntityFrostburnPolarBear bear, float swing, float amount,
                             float age, float yaw, float pitch) {
        head.setYaw(yaw * PI / 180.0F);
        head.setPitch(pitch * PI / 180.0F);
        backRightLeg.setPitch(MathHelper.cos(swing * 0.6662F) * 1.4F * amount);
        backLeftLeg.setPitch(MathHelper.cos(swing * 0.6662F + PI) * 1.4F * amount);
        frontRightLeg.setPitch(MathHelper.cos(swing * 0.6662F + PI) * 1.4F * amount);
        frontLeftLeg.setPitch(MathHelper.cos(swing * 0.6662F) * 1.4F * amount);

        float partialTick = Math.max(0.0F, Math.min(1.0F, age - bear.ticksExisted));
        float standing = bear.getStandingAnimationScale(partialTick);
        standing *= standing;
        float walking = 1.0F - standing;
        body.setPitch(PI / 2.0F - standing * PI * 0.35F);
        body.setY(9.0F * walking + 11.0F * standing);
        frontRightLeg.setY(14.0F * walking - 6.0F * standing);
        frontRightLeg.setZ(-8.0F * walking - 4.0F * standing);
        frontLeftLeg.setY(frontRightLeg.y());
        frontLeftLeg.setZ(frontRightLeg.z());
        frontRightLeg.setPitch(frontRightLeg.pitch() - standing * PI * 0.45F);
        frontLeftLeg.setPitch(frontLeftLeg.pitch() - standing * PI * 0.45F);
        head.setY(10.0F * walking - (bear.isChild() ? 9.0F : 14.0F) * standing);
        head.setZ(-16.0F * walking - (bear.isChild() ? 7.0F : 3.0F) * standing);
        head.setPitch(head.pitch() + standing * PI * 0.15F);
    }

    @Override
    protected void renderToBuffer(EntityFrostburnPolarBear bear, float scale) {
        if (bear.isChild()) {
            GL11.glPushMatrix();
            GL11.glScalef(0.6666667F, 0.6666667F, 0.6666667F);
            GL11.glTranslatef(0, 1.0F, 0.25F);
            head.render(scale);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            GL11.glTranslatef(0, 1.5F, 0);
            renderBody(scale);
            GL11.glPopMatrix();
        } else {
            head.render(scale);
            renderBody(scale);
        }
    }

    private void renderBody(float scale) {
        body.render(scale);
        backRightLeg.render(scale); backLeftLeg.render(scale);
        frontRightLeg.render(scale); frontLeftLeg.render(scale);
    }
}
