package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

/** Dragon-head geometry and texture coordinates used by post-1.9 skull rendering. */
public final class ModelDragonHead120 extends ModelBase {
    private final ModelRenderer head;
    private final ModelRenderer jaw;
    public ModelDragonHead120() {
        textureWidth = 256; textureHeight = 256;
        head = new ModelRenderer(this, "head");
        head.setTextureOffset(176, 44).addBox(-6.0F, -1.0F, -24.0F, 12, 5, 16);
        head.setTextureOffset(112, 30).addBox(-8.0F, -8.0F, -10.0F, 16, 16, 16);
        head.mirror = true;
        head.setTextureOffset(0, 0).addBox(-5.0F, -12.0F, -4.0F, 2, 4, 6);
        head.setTextureOffset(112, 0).addBox(-5.0F, -3.0F, -22.0F, 2, 2, 4);
        head.mirror = false;
        head.setTextureOffset(0, 0).addBox(3.0F, -12.0F, -4.0F, 2, 4, 6);
        head.setTextureOffset(112, 0).addBox(3.0F, -3.0F, -22.0F, 2, 2, 4);
        jaw = new ModelRenderer(this, "jaw");
        jaw.setTextureOffset(176, 65).addBox(-6.0F, 0.0F, -16.0F, 12, 4, 16);
        jaw.setRotationPoint(0.0F, 4.0F, -8.0F);
        head.addChild(jaw);
    }
    public void render(float yawDegrees, float jawOpen) {
        head.rotateAngleY = yawDegrees * (float) Math.PI / 180.0F;
        jaw.rotateAngleY = 0;
        jaw.rotateAngleX = jawOpen;
        head.render(0.0625F);
    }
}
