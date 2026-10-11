package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.minecraft1202.Module.Client.Shared.BackportEntityModel;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.BackportModelPart;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulRabbit;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

/** Complete Minecraft 1.8+ rabbit geometry adapted through the named-part model bridge. */
public final class ModelBountifulRabbit extends BackportEntityModel<EntityBountifulRabbit> {
    private static final float DEG = (float) Math.PI / 180.0F;
    private final BackportModelPart leftFoot, rightFoot, leftThigh, rightThigh, body;
    private final BackportModelPart leftArm, rightArm, head, leftEar, rightEar, tail, nose;

    public ModelBountifulRabbit() {
        super(64, 32);
        leftFoot = part("left_foot", 26, 24).mirror(true).box(-1, 5.5F, -3.7F, 2, 1, 7)
                .pos(3, 17.5F, 3.7F).storePose();
        rightFoot = part("right_foot", 8, 24).mirror(true).box(-1, 5.5F, -3.7F, 2, 1, 7)
                .pos(-3, 17.5F, 3.7F).storePose();
        leftThigh = part("left_thigh", 30, 15).mirror(true).box(-1, 0, 0, 2, 4, 5)
                .pos(3, 17.5F, 3.7F).rotation(-20 * DEG, 0, 0).storePose();
        rightThigh = part("right_thigh", 16, 15).mirror(true).box(-1, 0, 0, 2, 4, 5)
                .pos(-3, 17.5F, 3.7F).rotation(-20 * DEG, 0, 0).storePose();
        body = part("body", 0, 0).mirror(true).box(-3, -2, -10, 6, 5, 10).pos(0, 19, 8)
                .rotation(-20 * DEG, 0, 0).storePose();
        leftArm = part("left_arm", 8, 15).mirror(true).box(-1, 0, -1, 2, 7, 2).pos(3, 17, -1)
                .rotation(-10 * DEG, 0, 0).storePose();
        rightArm = part("right_arm", 0, 15).mirror(true).box(-1, 0, -1, 2, 7, 2).pos(-3, 17, -1)
                .rotation(-10 * DEG, 0, 0).storePose();
        head = part("head", 32, 0).mirror(true).box(-2.5F, -4, -5, 5, 4, 5)
                .pos(0, 16, -1).storePose();
        rightEar = part("right_ear", 52, 0).mirror(true).box(-2.5F, -9, -1, 2, 5, 1)
                .pos(0, 16, -1).storePose();
        leftEar = part("left_ear", 58, 0).mirror(true).box(0.5F, -9, -1, 2, 5, 1)
                .pos(0, 16, -1).storePose();
        tail = part("tail", 52, 6).mirror(true).box(-1.5F, -1.5F, 0, 3, 3, 2).pos(0, 20, 7)
                .rotation(-20 * DEG, 0, 0).storePose();
        nose = part("nose", 32, 9).mirror(true).box(-0.5F, -2.5F, -5.5F, 1, 1, 1)
                .pos(0, 16, -1).storePose();
    }

    @Override protected void setupAnim(EntityBountifulRabbit rabbit, float limbSwing, float limbAmount,
            float age, float yaw, float pitch) {
        float headYaw = yaw * DEG, headPitch = pitch * DEG;
        head.setYaw(headYaw); head.setPitch(headPitch);
        nose.setYaw(headYaw); nose.setPitch(headPitch);
        rightEar.setYaw(headYaw - 15 * DEG); rightEar.setPitch(headPitch);
        leftEar.setYaw(headYaw + 15 * DEG); leftEar.setPitch(headPitch);
        float hop=rabbit.getJumpCompletion(MathHelper.clamp_float(age-rabbit.ticksExisted,0.0F,1.0F));
        leftThigh.setPitch((hop * 50 - 21) * DEG); rightThigh.setPitch(leftThigh.pitch());
        leftFoot.setPitch(hop * 50 * DEG); rightFoot.setPitch(leftFoot.pitch());
        leftArm.setPitch((hop * -40 - 11) * DEG); rightArm.setPitch(leftArm.pitch());
    }

    @Override protected void renderToBuffer(EntityBountifulRabbit rabbit, float scale) {
        if (rabbit.isChild()) {
            GL11.glPushMatrix();
            GL11.glScalef(0.5666667F, 0.5666667F, 0.5666667F);
            GL11.glTranslatef(0, 1.375F, 0.125F);
            renderHead(scale);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glScalef(0.4F, 0.4F, 0.4F);
            GL11.glTranslatef(0, 2.25F, 0);
            renderBody(scale);
            GL11.glPopMatrix();
        } else {
            // Vanilla ModelRabbit scales only the adult geometry; RenderRabbit adds no scale.
            GL11.glPushMatrix();
            GL11.glScalef(0.6F, 0.6F, 0.6F);
            GL11.glTranslatef(0, 1.0F, 0);
            renderHead(scale);
            renderBody(scale);
            GL11.glPopMatrix();
        }
    }

    private void renderHead(float scale) {
        head.render(scale); leftEar.render(scale); rightEar.render(scale); nose.render(scale);
    }

    private void renderBody(float scale) {
        leftFoot.render(scale); rightFoot.render(scale); leftThigh.render(scale); rightThigh.render(scale);
        body.render(scale); leftArm.render(scale); rightArm.render(scale); tail.render(scale);
    }
}
