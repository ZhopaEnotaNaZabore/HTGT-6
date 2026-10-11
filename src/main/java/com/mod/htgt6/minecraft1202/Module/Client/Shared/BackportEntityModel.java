package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;

/**
 * Bridge between modern EntityModel-style setupAnim and the 1.7.10 ModelBase render callback.
 * It also makes model part names verifiable instead of relying on registration-era field layout.
 */
public abstract class BackportEntityModel<T extends Entity> extends ModelBase {
    private final Map<String, BackportModelPart> parts = new LinkedHashMap<String, BackportModelPart>();

    protected BackportEntityModel(int textureWidth, int textureHeight) {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    protected final BackportModelPart part(String name, int textureU, int textureV) {
        if (parts.containsKey(name)) throw new IllegalArgumentException("Duplicate model part: " + name);
        BackportModelPart part = new BackportModelPart(this, name, textureU, textureV);
        parts.put(name, part);
        return part;
    }

    public final BackportModelPart getPart(String name) {
        BackportModelPart part = parts.get(name);
        if (part == null) throw new IllegalArgumentException("Missing model part: " + name);
        return part;
    }

    public final Map<String, BackportModelPart> parts() { return Collections.unmodifiableMap(parts); }
    public final void resetPose() { for (BackportModelPart part : parts.values()) part.resetPose(); }

    protected abstract void setupAnim(T entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch);
    protected abstract void renderToBuffer(T entity, float scale);

    @Override @SuppressWarnings("unchecked")
    public final void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale) {
        T typed = (T)entity;
        resetPose();
        setupAnim(typed, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        renderToBuffer(typed, scale);
    }
}
