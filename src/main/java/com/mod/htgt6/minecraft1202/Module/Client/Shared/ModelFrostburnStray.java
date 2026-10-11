package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Skeleton model with the ranged pose used by the modern stray. */
public final class ModelFrostburnStray extends ModelSkeleton {
    public ModelFrostburnStray(float dilation) {
        super(dilation);
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing,
            float limbSwingAmount, float partialTickTime) {
        super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTickTime);
        ItemStack heldItem = entity.getHeldItem();
        // The 1.7 skeleton model enables this pose only for wither skeletons.
        aimedBow = heldItem != null && heldItem.getItem() == Items.bow;
    }

    /** Copies every state flag which affects ModelBiped#setRotationAngles. */
    public void copyRenderStateFrom(ModelBiped source) {
        onGround = source.onGround;
        isRiding = source.isRiding;
        isChild = source.isChild;
        heldItemLeft = source.heldItemLeft;
        heldItemRight = source.heldItemRight;
        aimedBow = source.aimedBow;
        isSneak = source.isSneak;
    }
}
