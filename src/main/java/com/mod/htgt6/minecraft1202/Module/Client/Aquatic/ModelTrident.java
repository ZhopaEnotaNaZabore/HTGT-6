package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

/** Exact 1.20.2 trident geometry adapted to the 1.7.10 ModelRenderer API. */
public final class ModelTrident extends ModelBase {
    /** u, v, x, y, z, width, height, depth from 1.20.2 TridentModel#createLayer. */
    public static final float[][] REFERENCE_GEOMETRY = {
            {0, 6, -0.5F, 2.0F, -0.5F, 1, 25, 1},
            {4, 0, -1.5F, 0.0F, -0.5F, 3, 2, 1},
            {4, 3, -2.5F, -3.0F, -0.5F, 1, 4, 1},
            {0, 0, -0.5F, -4.0F, -0.5F, 1, 4, 1},
            {4, 3, 1.5F, -3.0F, -0.5F, 1, 4, 1}
    };
    public static final boolean[] REFERENCE_MIRROR = {false, false, false, false, true};
    private final ModelRenderer[] parts = new ModelRenderer[5];

    public ModelTrident() {
        textureWidth = 32;
        textureHeight = 32;

        // Exact TridentModel.createLayer geometry from Java 1.20.2. The pole
        // origin is intentionally at the head: official item/projectile
        // transforms are authored around this pivot.
        for (int i = 0; i < REFERENCE_GEOMETRY.length; i++) {
            float[] box = REFERENCE_GEOMETRY[i];
            parts[i] = part((int)box[0], (int)box[1], box[2], box[3], box[4],
                    (int)box[5], (int)box[6], (int)box[7], REFERENCE_MIRROR[i]);
        }
    }

    private ModelRenderer part(int u, int v, float x, float y, float z,
                               int width, int height, int depth, boolean mirror) {
        ModelRenderer result = new ModelRenderer(this, u, v);
        result.mirror = mirror;
        result.addBox(x, y, z, width, height, depth);
        return result;
    }

    public void render() {
        for (ModelRenderer part : parts) part.render(1.0F / 16.0F);
    }
}
