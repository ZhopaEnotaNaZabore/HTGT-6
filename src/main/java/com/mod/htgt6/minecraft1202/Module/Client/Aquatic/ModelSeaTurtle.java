package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntitySeaTurtle;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public final class ModelSeaTurtle extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer eggBelly;
    private final ModelRenderer head;
    private final ModelRenderer frontLeft, frontRight, backLeft, backRight;

    public ModelSeaTurtle() {
        textureWidth = 128; textureHeight = 64;
        head = box(3, 0, -3, -1, -3, 6, 5, 6, 0, 19, -10, false);

        // Exact 1.20.1 turtle body: the atlas describes a vertical cuboid which
        // is rotated onto its belly.  Building it as an unrotated 18x7x21 box
        // made the old port look like a chest and hid the head and flippers.
        body = new ModelRenderer(this, 7, 37);
        body.addBox(-9.5F, 3.0F, -10.0F, 19, 20, 6);
        body.setRotationPoint(0.0F, 11.0F, -10.0F);
        body.rotateAngleX = (float) Math.PI / 2.0F;
        eggBelly = new ModelRenderer(this, 31, 1);
        eggBelly.addBox(-5.5F, 3.0F, -13.0F, 11, 18, 3);
        eggBelly.setRotationPoint(0.0F, 11.0F, -10.0F);
        eggBelly.rotateAngleX = (float) Math.PI / 2.0F;

        backRight = box(1, 23, -2, 0, 0, 4, 1, 10, -3.5F, 22, 11, false);
        backLeft = box(1, 12, -2, 0, 0, 4, 1, 10, 3.5F, 22, 11, false);
        frontRight = box(27, 30, -13, 0, -2, 13, 1, 5, -5, 21, -4, false);
        frontLeft = box(27, 24, 0, 0, -2, 13, 1, 5, 5, 21, -4, false);
    }

    private ModelRenderer box(int u, int v, float x, float y, float z,
                              int width, int height, int depth,
                              float pivotX, float pivotY, float pivotZ, boolean mirror) {
        ModelRenderer result = new ModelRenderer(this, u, v);
        result.mirror = mirror;
        result.addBox(x, y, z, width, height, depth);
        result.setRotationPoint(pivotX, pivotY, pivotZ);
        return result;
    }

    @Override
    public void setRotationAngles(float limb, float amount, float age, float yaw, float pitch, float scale, Entity entity) {
        head.rotateAngleX = pitch / 57.29578F; head.rotateAngleY = yaw / 57.29578F;

        // Vanilla 1.20.1 swimming pose.  Hind flippers pitch while the broad
        // front flippers roll, producing the characteristic swimming stroke.
        backRight.rotateAngleX = MathHelper.cos(limb * 0.6662F * 0.6F) * 0.5F * amount;
        backLeft.rotateAngleX = MathHelper.cos(limb * 0.6662F * 0.6F + (float) Math.PI) * 0.5F * amount;
        frontRight.rotateAngleX = frontLeft.rotateAngleX = 0.0F;
        frontLeft.rotateAngleY = frontRight.rotateAngleY = 0;
        backLeft.rotateAngleY = backRight.rotateAngleY = 0;
        backLeft.rotateAngleZ = backRight.rotateAngleZ = 0;
        frontRight.rotateAngleZ = MathHelper.cos(limb * 0.6662F * 0.6F + (float) Math.PI) * 0.5F * amount;
        frontLeft.rotateAngleZ = MathHelper.cos(limb * 0.6662F * 0.6F) * 0.5F * amount;

        if (!entity.isInWater() && entity.onGround) {
            // Vanilla land crawl: all flippers sweep sideways instead of
            // folding through the shell.
            frontRight.rotateAngleY = MathHelper.cos(limb * 5.0F + (float) Math.PI) * 8.0F * amount;
            frontLeft.rotateAngleY = MathHelper.cos(limb * 5.0F) * 8.0F * amount;
            frontRight.rotateAngleZ = frontLeft.rotateAngleZ = 0.0F;
            backRight.rotateAngleY = MathHelper.cos(limb * 5.0F + (float) Math.PI) * 3.0F * amount;
            backLeft.rotateAngleY = MathHelper.cos(limb * 5.0F) * 3.0F * amount;
            backRight.rotateAngleX = backLeft.rotateAngleX = 0.0F;
        }
    }

    @Override
    public void render(Entity entity, float limb, float amount, float age, float yaw, float pitch, float scale) {
        setRotationAngles(limb, amount, age, yaw, pitch, scale, entity);
        if (isChild) {
            GL11.glPushMatrix();
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            GL11.glTranslatef(0.0F, 24.0F * scale, 0.0F);
            renderParts(entity, scale);
            GL11.glPopMatrix();
        } else {
            renderParts(entity, scale);
        }
    }

    private void renderParts(Entity entity, float scale) {
        body.render(scale); head.render(scale);
        if (!isChild && entity instanceof EntitySeaTurtle && ((EntitySeaTurtle)entity).hasEgg()) {
            eggBelly.render(scale);
        }
        frontLeft.render(scale); frontRight.render(scale);
        backLeft.render(scale); backRight.render(scale);
    }
}
