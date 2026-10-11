package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulRabbit;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public final class RenderBountifulRabbit extends RenderLiving {
    private static final String[] NATURAL_TYPES =
            {"brown", "white", "black", "white_splotched", "gold", "salt"};

    public RenderBountifulRabbit() {
        super(new ModelBountifulRabbit(), 0.3F);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        EntityBountifulRabbit rabbit = (EntityBountifulRabbit) entity;
        String customName = rabbit.hasCustomNameTag()
                ? EnumChatFormatting.getTextWithoutFormattingCodes(rabbit.getCustomNameTag()) : "";
        String texture;
        if ("Toast".equals(customName)) texture = "toast";
        else if (rabbit.getRabbitType() == 99) texture = "caerbannog";
        else texture = NATURAL_TYPES[Math.max(0, Math.min(NATURAL_TYPES.length - 1, rabbit.getRabbitType()))];
        return new ResourceLocation("htgt6", "textures/entity/bountiful/rabbit/" + texture + ".png");
    }
}
