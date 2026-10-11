package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

/** SpinAttackEffectLayer box from Minecraft 1.20.2. */
public final class ModelRiptideEffect extends ModelBase {
    private final ModelRenderer box;

    public ModelRiptideEffect() {
        textureWidth = 64;
        textureHeight = 64;
        box = new ModelRenderer(this, 0, 0);
        box.addBox(-8.0F, -16.0F, -8.0F, 16, 32, 16);
    }

    void render() { box.render(1.0F / 16.0F); }
}
