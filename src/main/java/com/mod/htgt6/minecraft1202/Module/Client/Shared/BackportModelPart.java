package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

/**
 * Compatibility wrapper for modern named ModelPart geometry on Minecraft 1.7.10.
 * Coordinates and rotations intentionally match Mojang's pixel/radian model convention.
 */
public final class BackportModelPart {
    private final String name;
    private final ModelRenderer renderer;
    private float defaultX, defaultY, defaultZ, defaultPitch, defaultYaw, defaultRoll;

    public BackportModelPart(ModelBase owner, String name, int textureU, int textureV) {
        this.name = name;
        this.renderer = new ModelRenderer(owner, textureU, textureV);
    }

    public BackportModelPart box(float x, float y, float z, int width, int height, int depth) {
        renderer.addBox(x, y, z, width, height, depth);
        return this;
    }

    public BackportModelPart box(float x, float y, float z, int width, int height, int depth, float dilation) {
        renderer.addBox(x, y, z, width, height, depth, dilation);
        return this;
    }

    /** Selects the atlas origin for the next box added to this named part. */
    public BackportModelPart textureOffset(int textureU, int textureV) {
        renderer.setTextureOffset(textureU, textureV);
        return this;
    }

    public BackportModelPart pos(float x, float y, float z) {
        renderer.setRotationPoint(x, y, z);
        return this;
    }

    public BackportModelPart rotation(float pitch, float yaw, float roll) {
        renderer.rotateAngleX = pitch; renderer.rotateAngleY = yaw; renderer.rotateAngleZ = roll;
        return this;
    }

    public BackportModelPart degrees(float pitch, float yaw, float roll) {
        return rotation((float)Math.toRadians(pitch), (float)Math.toRadians(yaw), (float)Math.toRadians(roll));
    }

    public BackportModelPart mirror(boolean mirrored) { renderer.mirror = mirrored; return this; }
    public BackportModelPart child(BackportModelPart child) { renderer.addChild(child.renderer); return this; }

    /** Stores the authored pose so animation code can safely reset every frame. */
    public BackportModelPart storePose() {
        defaultX = renderer.rotationPointX; defaultY = renderer.rotationPointY; defaultZ = renderer.rotationPointZ;
        defaultPitch = renderer.rotateAngleX; defaultYaw = renderer.rotateAngleY; defaultRoll = renderer.rotateAngleZ;
        return this;
    }

    public void resetPose() { pos(defaultX, defaultY, defaultZ); rotation(defaultPitch, defaultYaw, defaultRoll); }
    public void render(float scale) { if (renderer.showModel) renderer.render(scale); }
    public String name() { return name; }
    public float x() { return renderer.rotationPointX; } public float y() { return renderer.rotationPointY; } public float z() { return renderer.rotationPointZ; }
    public float pitch() { return renderer.rotateAngleX; } public float yaw() { return renderer.rotateAngleY; } public float roll() { return renderer.rotateAngleZ; }
    public void setX(float value) { renderer.rotationPointX = value; } public void setY(float value) { renderer.rotationPointY = value; } public void setZ(float value) { renderer.rotationPointZ = value; }
    public void setPitch(float value) { renderer.rotateAngleX = value; } public void setYaw(float value) { renderer.rotateAngleY = value; } public void setRoll(float value) { renderer.rotateAngleZ = value; }
}
